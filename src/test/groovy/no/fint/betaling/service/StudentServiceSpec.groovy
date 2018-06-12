package no.fint.betaling.service

import no.fint.betaling.model.Kunde
import no.fint.betaling.model.KundeFactory
import no.fint.model.felles.kompleksedatatyper.Adresse
import no.fint.model.felles.kompleksedatatyper.Personnavn
import no.fint.model.utdanning.elev.Elev
import org.springframework.hateoas.Link
import org.springframework.hateoas.Resource
import org.springframework.hateoas.Resources
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.client.RestTemplate
import spock.lang.Specification

class StudentServiceSpec extends Specification {


    def "Get customers returns list"() {
        given:
        def restTemplate = Mock(RestTemplate)
        def kundeFactory = Mock(KundeFactory)
        def studentService = new StudentService(restTemplate: restTemplate, kundeFactory: kundeFactory)
        def response = createResponseResourcesResourceElev()

        when:
        List<Kunde> listCustomers = studentService.getCustomers()

        then:
        1 * restTemplate.exchange(_, _, _, _) >> response
        1 * kundeFactory.getKunde(_) >> new Kunde()
        listCustomers
    }

    private static ResponseEntity<Resources<Resource<Elev>>> createResponseResourcesResourceElev() {
        def student = new Elev()
        def resource = new Resource(student)
        def listStudent = new ArrayList()
        listStudent.add(resource)
        def resources = new Resources(listStudent, new Link())
        return new ResponseEntity<Resources<Resource<Elev>>>(resources, HttpStatus.OK)
    }
}
