package no.fint.betaling.service

import no.fint.betaling.model.Kunde
import no.fint.betaling.model.KundeFactory
import no.fint.model.felles.kompleksedatatyper.Identifikator
import no.fint.model.felles.kompleksedatatyper.Personnavn
import no.fint.model.resource.felles.PersonResource
import no.fint.model.resource.felles.PersonResources
import spock.lang.Specification

class StudentServiceSpec extends Specification {

    private KundeFactory kundeFactory
    private StudentService studentService
    private RestService restService
    private String orgId

    void setup() {
        restService = Mock(RestService)
        kundeFactory = Mock(KundeFactory)
        studentService = new StudentService(restService: restService, kundeFactory: kundeFactory, personEndpoint: "endpoints/person")
        orgId = 'test.no'
    }

    def "Get customers returns list"() {
        given:
        def personResources = createPersonResources(1)

        when:
        List<Kunde> listCustomers = studentService.getCustomers(orgId, "")

        then:
        1 * restService.getResource(_ as Class<PersonResources>, _ as String, _ as String) >> personResources
        1 * kundeFactory.getKunde(_ as PersonResource) >> new Kunde(navn: new Personnavn(etternavn: 'Testesen'))
        listCustomers.size() == 1
    }

    def "Get customers given filter keyword returns filtered list"() {
        when:
        def listStudents = studentService.getCustomers(orgId, 'r')

        then:
        1 * restService.getResource(_ as Class<PersonResources>, _ as String, _ as String) >> createPersonResources(3)
        1 * kundeFactory.getKunde(_ as PersonResource) >> new Kunde(navn: new Personnavn(etternavn: 'Feilsen'))
        1 * kundeFactory.getKunde(_ as PersonResource) >> new Kunde(navn: new Personnavn(etternavn: 'Testesen'))
        1 * kundeFactory.getKunde(_ as PersonResource) >> new Kunde(navn: new Personnavn(etternavn: 'Rettsen'))

        listStudents.size() == 1
        listStudents.get(0).navn.etternavn == 'Rettsen'
    }

    private static PersonResources createPersonResources(int resources) {
        def personResources = new PersonResources()
        for (int i = 0; i < resources; i++) {
            def personResource = new PersonResource(
                    navn: new Personnavn(fornavn: 'Ola', etternavn: 'Testesen'),
                    fodselsnummer: new Identifikator(identifikatorverdi: '12345678901')
            )
            personResources.addResource(personResource)
        }
        return personResources
    }
}
