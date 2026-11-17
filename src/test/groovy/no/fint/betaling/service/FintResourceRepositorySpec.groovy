package no.fint.betaling.service

import no.fint.betaling.fintdata.FintResourceRepository
import no.fint.betaling.common.util.RestUtil
import no.novari.fint.model.resource.Link
import no.novari.fint.model.resource.utdanning.vurdering.ElevfravarResource
import no.novari.fint.model.resource.utdanning.vurdering.ElevfravarResources
import org.springframework.web.reactive.function.client.WebClientResponseException
import reactor.core.publisher.Mono
import spock.lang.Specification

import java.util.function.Function
import java.util.concurrent.*
import java.util.concurrent.atomic.AtomicInteger
import reactor.core.publisher.Sinks

class FintResourceRepositorySpec extends Specification {

    private RestUtil restUtil

    def setup() {
        restUtil = Mock(RestUtil)
    }

    def "update with self link - success"() {
        given:
        String endpoint = "https://example.com/resources"
        FintResourceRepository<ElevfravarResource, ElevfravarResources> resourceCache = new FintResourceRepository<>(restUtil, endpoint, ElevfravarResource.class)

        ElevfravarResources elevfravarResources = new ElevfravarResources()
        elevfravarResources.addResource(createResource("test-url-1"))
        elevfravarResources.addResource(createResource("test-url-2"))
        restUtil.getWithRetry(_, _) >> Mono.just(elevfravarResources)

        when:
        resourceCache.update()

        then:
        Map<Link, ElevfravarResource> resources = resourceCache.getMap()
        resources.size() == 2
        resources.containsKey(new Link("test-url-1"))
        resources.containsKey(new Link("test-url-2"))
    }

    def "update with custom link - success"() {
        given:
        String endpoint = "https://example.com/resources"
        Function<ElevfravarResource, List<Link>> linkProvider = { resource -> resource.getElevforhold() }
        FintResourceRepository<ElevfravarResource, ElevfravarResources> resourceCache = new FintResourceRepository<>(restUtil, endpoint, ElevfravarResource.class, linkProvider)

        ElevfravarResources elevfravarResources = new ElevfravarResources()
        elevfravarResources.addResource(createResource("test-url-1", "Test1"))
        elevfravarResources.addResource(createResource("test-url-2", "Test2"))
        restUtil.getWithRetry(_, _) >> Mono.just(elevfravarResources)

        when:
        resourceCache.update()

        then:
        Map<Link, ElevfravarResource> resources = resourceCache.getMap()
        resources.size() == 2
        resources.containsKey(new Link("Test1"))
        resources.containsKey(new Link("Test2"))
    }


    def "concurrent cold-cache readers share one load, including an empty result"() {
        given:
        def cache = new FintResourceRepository(restUtil, '/resources', ElevfravarResources)
        def gate = Sinks.one()
        def fetching = new CountDownLatch(1)
        def calls = new AtomicInteger()
        def workers = Executors.newFixedThreadPool(8)
        restUtil.getWithRetry(*_) >> Mono.defer {
            calls.incrementAndGet()
            fetching.countDown()
            gate.asMono()
        }

        when:
        def tasks = (1..8).collect { workers.submit({ cache.getMap() } as Callable) }
        assert fetching.await(2, TimeUnit.SECONDS)
        gate.tryEmitValue(new ElevfravarResources())
        def maps = tasks.collect { it.get(2, TimeUnit.SECONDS) }

        then:
        maps.every { it.isEmpty() }
        cache.getMap().isEmpty()
        calls.get() == 1

        cleanup:
        workers.shutdownNow()
    }

    def "readers use existing data while a background refresh is waiting on the network"() {
        given:
        def cache = new FintResourceRepository(restUtil, '/resources', ElevfravarResources)
        def populated = new ElevfravarResources()
        populated.addResource(createResource('test-url-1'))
        def gate = Sinks.one()
        def fetching = new CountDownLatch(1)
        def workers = Executors.newFixedThreadPool(2)
        restUtil.getWithRetry(*_) >>> [Mono.just(populated), Mono.defer {
            fetching.countDown()
            gate.asMono()
        }]
        cache.update()

        when:
        def refresh = workers.submit({ cache.update() } as Callable)
        assert fetching.await(2, TimeUnit.SECONDS)
        def read = workers.submit({ cache.getMap().size() } as Callable)

        then:
        read.get(1, TimeUnit.SECONDS) == 1

        cleanup:
        gate.tryEmitValue(populated)
        workers.shutdownNow()
    }

    def "a failed refresh keeps good data while a successful empty refresh clears it"() {
        given:
        def cache = new FintResourceRepository(restUtil, '/resources', ElevfravarResources)
        def populated = new ElevfravarResources()
        populated.addResource(createResource('test-url-1'))
        restUtil.getWithRetry(*_) >>> [Mono.just(populated), Mono.error(new IllegalStateException('Unavailable')),
                                      Mono.just(new ElevfravarResources())]

        when:
        cache.update()
        cache.update()

        then:
        cache.getMap().size() == 1

        when:
        cache.update()

        then:
        cache.getMap().isEmpty()
    }

    private ElevfravarResource createResource(String href) {
        return createResource(href, null)
    }

    private ElevfravarResource createResource(String href, String elevforhold) {
        ElevfravarResource resource = new ElevfravarResource()
        resource.addSelf(new Link(href))
        if (org.apache.commons.lang3.StringUtils.isNotEmpty(elevforhold))
            resource.addElevforhold(new Link(elevforhold))
        return resource
    }

    def "update - no items"() {
        given:
        String endpoint = "https://example.com/resources"
        FintResourceRepository<ElevfravarResource, ElevfravarResources> resourceCache = new FintResourceRepository<>(restUtil, endpoint, ElevfravarResource.class)

        restUtil.getWithRetry(_, _) >> Mono.just(new ElevfravarResources())

        when:
        resourceCache.update()

        then:
        Collection<ElevfravarResource> resources = resourceCache.get()
        resources.isEmpty()
    }

    def "update - error"() {
        given:
        String endpoint = "https://example.com/resources"
        FintResourceRepository<ElevfravarResource, ElevfravarResources> resourceCache = new FintResourceRepository<>(restUtil, endpoint, ElevfravarResource.class)

        WebClientResponseException exception = Mock(WebClientResponseException)
        restUtil.getWithRetry(_, _) >> { throw exception }

        when:
        resourceCache.update()

        then:
        Collection<ElevfravarResource> resources = resourceCache.get()
        resources.isEmpty()
    }
}
