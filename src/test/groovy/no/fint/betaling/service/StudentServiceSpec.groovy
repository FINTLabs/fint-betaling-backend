package no.fint.betaling.service

import no.fint.betaling.model.Kunde
import no.fint.model.felles.kompleksedatatyper.Identifikator
import no.fint.model.felles.kompleksedatatyper.Personnavn
import no.fint.model.resource.Link
import no.fint.model.resource.felles.PersonResource
import no.fint.model.resource.felles.PersonResources
import spock.lang.Specification

class StudentServiceSpec extends Specification {

    private StudentService studentService
    private RestService restService
    private String orgId

    void setup() {
        restService = Mock(RestService)
        studentService = new StudentService(restService: restService, personEndpoint: "endpoints/person")
        orgId = 'test.no'
    }

    def "Get customers returns list"() {
        given:
        def personResources = createPersonResources(1, ['Testesen'])

        when:
        List<Kunde> listCustomers = studentService.getCustomers(orgId, "")

        then:
        1 * restService.getResource(_ as Class<PersonResources>, _ as String, _ as String) >> personResources
        listCustomers.size() == 1
    }

    def "Get customers given filter keyword returns filtered list"() {
        given:
        def lastnames = ['Feilsen', 'Testesen', 'Rettsen']
        when:
        def listStudents = studentService.getCustomers(orgId, 'r')

        then:
        1 * restService.getResource(_ as Class<PersonResources>, _ as String, _ as String) >> createPersonResources(3, lastnames)

        listStudents.size() == 1
        listStudents.get(0).navn.etternavn == 'Rettsen'
    }

    private static PersonResources createPersonResources(int resources, List<String> lastnames) {
        def personResources = new PersonResources()
        for (int i = 0; i < resources; i++) {
            def personResource = new PersonResource(
                    navn: new Personnavn(fornavn: 'Ola', etternavn: lastnames.get(i)),
                    fodselsnummer: new Identifikator(identifikatorverdi: '12345678901')
            )
            personResources.addResource(personResource)
            personResource.addLink('self', Link.with('link.to.PersonResource'))
        }
        return personResources
    }
}
