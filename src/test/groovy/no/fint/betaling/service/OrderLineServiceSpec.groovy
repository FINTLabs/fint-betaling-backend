package no.fint.betaling.service

import no.fint.model.administrasjon.okonomi.Varelinje
import no.fint.model.felles.kompleksedatatyper.Identifikator
import no.fint.model.resource.administrasjon.kompleksedatatyper.KontostrengResource
import no.fint.model.resource.administrasjon.okonomi.VarelinjeResource
import no.fint.model.resource.administrasjon.okonomi.VarelinjeResources
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.client.RestTemplate
import spock.lang.Specification

class OrderLineServiceSpec extends Specification {

    private RestTemplate restTemplate
    private OrderLineService orderLineService

    void setup() {
        restTemplate = Mock(RestTemplate)
        orderLineService = new OrderLineService(restTemplate: restTemplate)
    }

    def "Get order lines given valid orgId returns list of Varelinje"() {
        given:
        def orderLineResources = new VarelinjeResources()
        orderLineResources.addResource(new VarelinjeResource(
                enhet: 'test',
                kontering: new KontostrengResource(),
                pris: 1000L,
                kode: 'code',
                navn: 'Test',
                systemId: new Identifikator(identifikatorverdi: 'test')
        ))

        when:
        def orderLines = orderLineService.getOrderLines('valid.org')

        then:
        1 * restTemplate.exchange(_, _, _, _) >> ResponseEntity.ok(orderLineResources)
        orderLines.size() == 1
        orderLines.get(0) instanceof VarelinjeResource
    }

    def "Set order line given valid ordId and valid order line returns true"() {
        when:
        def didSet = orderLineService.setOrderLine('valid.org', new Varelinje(navn: 'valid order'))

        then:
        1 * restTemplate.exchange(_, _, _, _) >> ResponseEntity.status(HttpStatus.OK).build()
        didSet
    }

    def "Get order lines given invalid orgId returns empty list"() {
        when:
        def orderLines = orderLineService.getOrderLines('invalid.org')

        then:
        1 * restTemplate.exchange(_, _, _, _) >> ResponseEntity.ok(new VarelinjeResources())
        orderLines.size() == 0
    }
}