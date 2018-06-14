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

    void setup() {
        restService = Mock(RestService)
        kundeFactory = Mock(KundeFactory)
        studentService = new StudentService(restService: restService, kundeFactory: kundeFactory)
    }

    def "Get customers returns list"() {
        given:
        def elevResources = createElevResources(1)

        when:
        List<Kunde> listCustomers = studentService.getCustomers()

        then:
        1 * restService.getElevResources() >> elevResources
        1 * kundeFactory.getKunde(_) >> new Kunde()
        listCustomers.size() == 1
    }

    def "Get customers given filter keyword returns filtered list"() {
        when:
        def listStudents = studentService.getCustomers('r')

        then:
        1 * restService.getElevResources() >> createElevResources(3)
        1 * kundeFactory.getKunde(_) >> new Kunde(navn: new Personnavn(etternavn: 'Feilsen'))
        1 * kundeFactory.getKunde(_) >> new Kunde(navn: new Personnavn(etternavn: 'Testesen'))
        1 * kundeFactory.getKunde(_) >> new Kunde(navn: new Personnavn(etternavn: 'Rettsen'))

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
