package no.fint.betaling.integration

import ch.qos.logback.classic.Logger
import ch.qos.logback.classic.spi.IThrowableProxy
import ch.qos.logback.core.read.ListAppender
import no.fint.betaling.claim.*
import no.fint.betaling.common.util.FintClient
import no.fint.betaling.common.util.RestUtil
import no.fint.betaling.model.Claim
import no.fint.betaling.model.ClaimStatus
import no.novari.fint.model.resource.okonomi.faktura.FakturagrunnlagResource
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import org.slf4j.LoggerFactory
import org.springframework.http.MediaType
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.test.util.ReflectionTestUtils
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.scheduler.Schedulers
import reactor.netty.http.client.HttpClient
import reactor.netty.resources.ConnectionProvider
import spock.lang.Specification

import java.time.Duration
import java.util.concurrent.Callable
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.function.BiFunction

/**
 * Reproduces the production connection-pool failure through ClaimController's HTTP endpoint.
 *
 * This is an integration test rather than an end-to-end test: it uses the real controller,
 * ClaimRestService, RestUtil, WebClient and Reactor Netty connection pool. Persistence and invoice
 * mapping are mocked because they do not participate in connection acquisition, while the remote
 * FINT API is represented by a real HTTP server whose deliberately slow responses hold connections.
 */
class ClaimSendIntegrationSpec extends Specification {

    private static final int MAX_CONNECTIONS = 100
    private static final int CLAIMS_PER_REQUEST = 160

    MockWebServer fintServer
    ConnectionProvider connectionProvider
    List<ConnectionProvider> connectionProviders = []
    WebTestClient webTestClient
    def executor
    ListAppender poolFailureLogs

    void setup() {
        fintServer = new MockWebServer()
        fintServer.dispatcher = new Dispatcher() {
            @Override
            MockResponse dispatch(RecordedRequest request) {
                // Keep all 100 pool connections occupied while both endpoint calls fan out.
                return new MockResponse()
                        .setResponseCode(201)
                        .setHeadersDelay(2, TimeUnit.SECONDS)
            }
        }
        fintServer.start()

        connectionProvider = ConnectionProvider.builder("claim-send-reproduction")
                .maxConnections(MAX_CONNECTIONS)
                // Intentionally omit pendingAcquireMaxCount: production does the same, so Reactor
                // uses its default of 2 * maxConnections, i.e. exactly 200.
                .build()
        connectionProviders.add(connectionProvider)

        webTestClient = createWebTestClient(connectionProvider)

        poolFailureLogs = new ListAppender()
        poolFailureLogs.start()
        ((Logger) LoggerFactory.getLogger(ClaimRestService)).addAppender(poolFailureLogs)
        executor = Executors.newFixedThreadPool(2)
    }

    void cleanup() {
        executor?.shutdownNow()
        ((Logger) LoggerFactory.getLogger(ClaimRestService)).detachAppender(poolFailureLogs)
        poolFailureLogs?.stop()
        connectionProviders.each { provider ->
            provider.disposeLater().block(Duration.ofSeconds(5))
        }
        fintServer?.shutdown()
    }

    def 'concurrent POSTs to claim send complete without exhausting the connection pool'() {
        given: 'two endpoint calls which together request 320 connections from the same host'
        def firstOrderNumbers = (1L..CLAIMS_PER_REQUEST).toList()
        def secondOrderNumbers = ((CLAIMS_PER_REQUEST + 1L)..(CLAIMS_PER_REQUEST * 2L)).toList()
        def ready = new CountDownLatch(2)
        def start = new CountDownLatch(1)

        when: 'both calls subscribe to their response Flux at the same time'
        def first = executor.submit({ -> invokeSendEndpoint(firstOrderNumbers, ready, start) } as Callable<Map>)
        def second = executor.submit({ -> invokeSendEndpoint(secondOrderNumbers, ready, start) } as Callable<Map>)
        assert ready.await(5, TimeUnit.SECONDS)
        start.countDown()

        def outcomes = [
                first.get(20, TimeUnit.SECONDS),
                second.get(20, TimeUnit.SECONDS)
        ]

        then: 'the upstream was saturated rather than failing before any HTTP connections were made'
        fintServer.requestCount >= MAX_CONNECTIONS

        and: 'no request should be rejected by the connection pool'
        def poolFailure = poolFailureLogs.list
                .collectMany { event -> throwableMessages(event.throwableProxy) }
                .find { it.contains('Pending acquire queue has reached its maximum size of 200') }

        assert !poolFailure: "Reproduced connection pool exhaustion: ${poolFailure}"

        and: 'both endpoint calls should complete successfully'
        outcomes.every { outcome ->
            outcome.error == null && outcome.status?.is2xxSuccessful()
        }
    }

    def 'claim send completes before the 45000ms pending acquire timeout'() {
        given: 'a production-sized pool whose real 45-second timer is accelerated for this test'
        ConnectionProvider timeoutProvider = ConnectionProvider.builder("claim-send-timeout-reproduction")
                .maxConnections(MAX_CONNECTIONS)
                .pendingAcquireTimeout(Duration.ofSeconds(45))
                .pendingAcquireTimer({ Runnable timeoutTask, Duration ignored ->
                    Schedulers.parallel().schedule(timeoutTask, 1, TimeUnit.SECONDS)
                } as BiFunction)
                .build()
        connectionProviders.add(timeoutProvider)
        WebTestClient timeoutWebTestClient = createWebTestClient(timeoutProvider)
        def orderNumbers = (1L..CLAIMS_PER_REQUEST).toList()

        when: 'one endpoint call occupies 100 connections and leaves 60 acquisitions pending'
        def outcome = invokeSendEndpoint(timeoutWebTestClient, orderNumbers)

        then: 'the pool was saturated without overflowing its 200-entry pending queue'
        fintServer.requestCount >= MAX_CONNECTIONS

        and: 'no acquisition should remain pending for the configured timeout'
        def poolTimeout = poolFailureLogs.list
                .collectMany { event -> throwableMessages(event.throwableProxy) }
                .find { it == 'Pool#acquire(Duration) has been pending for more than the configured timeout of 45000ms' }

        assert !poolTimeout: "Reproduced connection pool timeout: ${poolTimeout}"

        and: 'the endpoint call should complete successfully'
        outcome.error == null && outcome.status?.is2xxSuccessful()
    }

    private Map invokeSendEndpoint(List<Long> orderNumbers, CountDownLatch ready, CountDownLatch start) {
        ready.countDown()
        assert start.await(5, TimeUnit.SECONDS)

        return invokeSendEndpoint(webTestClient, orderNumbers)
    }

    private static Map invokeSendEndpoint(WebTestClient client, List<Long> orderNumbers) {

        try {
            def result = client.post()
                    .uri('/claim/send')
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(orderNumbers)
                    .exchange()
                    .expectBody(byte[].class)
                    .returnResult()
            return [status: result.status]
        } catch (Throwable error) {
            // A response-body failure can surface as a client exception if the 201 headers were
            // committed before the controller's Flux failed.
            return [error: error]
        }
    }

    private WebTestClient createWebTestClient(ConnectionProvider provider) {
        String baseUrl = fintServer.url("/").toString()
        def webClient = WebClient.builder()
                .baseUrl(baseUrl)
                .clientConnector(new ReactorClientHttpConnector(
                        HttpClient.create(provider).responseTimeout(Duration.ofSeconds(10))))
                .build()

        def restUtil = new RestUtil(webClient)
        restUtil.setBaseUrl(baseUrl)
        ReflectionTestUtils.setField(restUtil, "orgId", "fintlabs.no")

        ClaimDatabaseService claimDatabaseService = Mock()
        InvoiceFactory invoiceFactory = Mock()
        ClaimRepository claimRepository = Mock()
        ClaimRestStatusService claimRestStatusService = Mock()
        FintClient fintClient = Mock()

        List<Claim> claims = (1L..(CLAIMS_PER_REQUEST * 2L)).collect { orderNumber ->
            new Claim(orderNumber: orderNumber, claimStatus: ClaimStatus.STORED)
        }
        claimDatabaseService.getUnsentClaims() >> claims
        invoiceFactory.createInvoice(_ as Claim) >> new FakturagrunnlagResource()

        def claimRestService = new ClaimRestService(
                restUtil,
                fintClient,
                invoiceFactory,
                claimRepository,
                claimDatabaseService,
                claimRestStatusService)
        ReflectionTestUtils.setField(claimRestService, "invoiceEndpoint", "/invoice")
        def controller = new ClaimController(
                claimDatabaseService,
                claimRestService,
                Mock(ScheduleService),
                claimRestStatusService)

        return WebTestClient.bindToController(controller)
                .configureClient()
                .responseTimeout(Duration.ofSeconds(15))
                .build()
    }

    private static List<String> throwableMessages(IThrowableProxy throwable) {
        List<String> messages = []
        IThrowableProxy current = throwable
        while (current != null) {
            messages.add(current.message ?: '')
            current = current.cause
        }
        return messages
    }
}
