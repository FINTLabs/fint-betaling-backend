package no.fint.betaling.service

import no.fint.betaling.model.InvalidResponseException
import no.fint.betaling.model.Kunde
import no.fint.betaling.model.KundeFactory
import no.fint.model.felles.kompleksedatatyper.Identifikator
import no.fint.model.resource.utdanning.elev.ElevResource
import no.fint.model.resource.utdanning.elev.ElevResources
import org.springframework.http.ResponseEntity
import org.springframework.web.client.RestClientException
import org.springframework.web.client.RestTemplate
import spock.lang.Specification

class StudentServiceSpec extends Specification {

    private RestTemplate restTemplate
    private KundeFactory kundeFactory
    private StudentService studentService

    void setup() {
        restTemplate = Mock(RestTemplate)
        kundeFactory = Mock(KundeFactory)
        studentService = new StudentService(studentEndpoint: 'http://localhost', restTemplate: restTemplate, kundeFactory: kundeFactory)
    }

    def "Get customers given invalid response throws InvalidResponseException"() {
        when:
        studentService.getCustomers('')

        then:
        1 * restTemplate.exchange(_, _, _, _) >> { throw new RestClientException('test exception') }
        thrown(InvalidResponseException)
    }


    def "Get customers returns list"() {
        given:
        def response = createElevResources()

        when:
        List<Kunde> listCustomers = studentService.getCustomers('')

        then:
        1 * restTemplate.exchange(_, _, _, _) >> ResponseEntity.ok(response)
        1 * kundeFactory.getKunde(_) >> new Kunde()
        listCustomers.size() == 1
    }

    private static ElevResources createElevResources() {
        def elevResources = new ElevResources()
        def identifikator = new Identifikator()
        identifikator.setIdentifikatorverdi('123')
        def elevResource = new ElevResource()
        elevResource.setSystemId(identifikator)
        elevResources.addResource()
        elevResources
    }
}
