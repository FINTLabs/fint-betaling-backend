package no.fint.betaling.service

import no.fint.betaling.model.Kunde
import no.fint.betaling.model.KundeFactory
import no.fint.model.felles.kompleksedatatyper.Identifikator
import no.fint.model.felles.kompleksedatatyper.Personnavn
import no.fint.model.resource.utdanning.elev.ElevResource
import no.fint.model.resource.utdanning.elev.ElevResources
import spock.lang.Specification

class StudentServiceSpec extends Specification {

    private KundeFactory kundeFactory
    private StudentService studentService
    private RestService restService
    private String orgId

    void setup() {
        restService = Mock(RestService)
        kundeFactory = Mock(KundeFactory)
        studentService = new StudentService(restService: restService, kundeFactory: kundeFactory, elevEndpoint: "endpoints/elev")
        orgId = 'test.no'
    }

    def "Get customers returns list"() {
        given:
        def elevResources = createElevResources(1)

        when:
        List<Kunde> listCustomers = studentService.getCustomers(orgId, "")

        then:
        1 * restService.getResource(ElevResources, _ as String, _ as String) >> elevResources
        1 * kundeFactory.getKunde('test.no', _) >> new Kunde(navn: new Personnavn(etternavn: 'Testesen'))
        listCustomers.size() == 1
    }

    def "Get customers given filter keyword returns filtered list"() {
        when:
        def listStudents = studentService.getCustomers(orgId, 'r')

        then:
        1 * restService.getResource(ElevResources, _ as String, _ as String) >> createElevResources(3)
        1 * kundeFactory.getKunde('test.no', _) >> new Kunde(navn: new Personnavn(etternavn: 'Feilsen'))
        1 * kundeFactory.getKunde('test.no', _) >> new Kunde(navn: new Personnavn(etternavn: 'Testesen'))
        1 * kundeFactory.getKunde('test.no', _) >> new Kunde(navn: new Personnavn(etternavn: 'Rettsen'))

        listStudents.size() == 1
        listStudents.get(0).navn.etternavn == 'Rettsen'
    }

    private static ElevResources createElevResources(int resources) {
        def elevResources = new ElevResources()
        def identifikator = new Identifikator()
        identifikator.setIdentifikatorverdi('123')
        for (int i = 0; i < resources; i++) {
            def elevResource = new ElevResource()
            elevResource.setSystemId(identifikator)
            elevResources.addResource()
        }
        return elevResources
    }
}
