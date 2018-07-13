package no.fint.betaling.service

import no.fint.betaling.model.Betaling
import no.fint.model.felles.kompleksedatatyper.Identifikator
import no.fint.model.resource.administrasjon.okonomi.FakturagrunnlagResource
import no.fint.model.resource.administrasjon.okonomi.FakturagrunnlagResources
import no.fint.model.resource.administrasjon.okonomi.FakturalinjeResource
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.Update
import org.springframework.http.ResponseEntity
import spock.lang.Specification
import sun.plugin.javascript.navig.Link

class InvoiceServiceSpec extends Specification {
    private InvoiceService invoiceService
    private RestService restService
    private MongoService mongoService

    void setup() {
        restService = Mock(RestService) {
            getResource(_ as Class<FakturagrunnlagResource>, _ as String, 'valid.org') >> createInvoice()
            setResource(_ as Class<FakturagrunnlagResource>, _ as String, _ as FakturagrunnlagResource, _ as String) >> {
                ResponseEntity.ok().headers().location(new URI('http', 'valid.host', '/path', '')).build()
            }
        }
        mongoService = Mock(MongoService)
        invoiceService = new InvoiceService(
                restService: restService,
                mongoService: mongoService,
                invoiceEndpoint: 'enpoints/invoice'
        )
    }

    def "Send invoices given valid orgId sends invoices and updates payments"() {
        when:
        invoiceService.sendInvoices('valid.org')

        then:
        1 * mongoService.getPayments(_ as String, _ as Query) >> [createPayment(false)]
        1 * mongoService.updatePayment('valid.org', _ as Query, _ as Update)
    }

    def "Update invoice status given valid orgId updates payments"() {
        when:
        invoiceService.updateInvoiceStatus('valid.org')

        then:
        1 * mongoService.getPayments(_ as String, _ as Query) >> [createPayment(true)]
        1 * mongoService.updatePayment(_ as String, _ as Query, _ as Update)
    }


    def "Get invoice given valid org id returns list of invoices"() {
        given:
        def invoiceResources = new FakturagrunnlagResources()
        invoiceResources.addResource(createInvoice())

        when:
        def invoices = invoiceService.getInvoices('valid.org')

        then:
        1 * restService.getResource(_ as Class<FakturagrunnlagResources>, _ as String, _ as String) >> invoiceResources
        invoices.size() == 1
        invoices.get(0).ordrenummer.identifikatorverdi == 'testOrder'
        invoices.get(0).fakturalinjer.get(0).pris == 1000
    }

    def "Set invoice given valid invoice returns valid response"() {
        when:
        def response = invoiceService.setInvoice('valid.org', createInvoice())

        then:
        response.getStatusCode().is2xxSuccessful()
        response.getHeaders().getLocation().getHost() == 'valid.host'
    }

    def "Get status given payment with valid location uri returns invoice"() {
        when:
        def invoice = invoiceService.getStatus(
                'valid.org',
                new Betaling(location: new URI('http', 'valid.location', '/path', ''))
        )

        then:
        invoice.ordrenummer.identifikatorverdi == 'testOrder'
    }

    def "Update invoice given valid invoice behaves as expected"() {
        when:
        invoiceService.updateInvoice('valid.org', createInvoice())

        then:
        1 * mongoService.updatePayment(_ as String, _ as Query, _ as Update)
    }

    def "Get payments passes arguments to mongoservice"() {
        when:
        invoiceService.getPayments('valid.org', new Query())

        then:
        1 * mongoService.getPayments('valid.org', _ as Query)
    }

    def "Update payment passes arguments to mongoservice"() {
        when:
        invoiceService.updatePayment('valid.org', new Query(), new Update())

        then:
        1 * mongoService.updatePayment('valid.org', _ as Query, _ as Update)
    }

    private static FakturagrunnlagResource createInvoice() {
        def resource = new FakturagrunnlagResource(
                ordrenummer: new Identifikator(identifikatorverdi: 'testOrder'),
                fakturalinjer: [
                        new FakturalinjeResource(
                                antall: 1,
                                fritekst: ['testLine'],
                                pris: 1000L
                        )
                ]
        )
        return resource
    }

    private static Betaling createPayment(boolean sent) {
        def payment = new Betaling()
        payment.setFakturagrunnlag(createInvoice())
        payment.location = new URI('http','host.test','/location','')
        payment.sentTilEksterntSystem = sent
        return payment
    }
}
