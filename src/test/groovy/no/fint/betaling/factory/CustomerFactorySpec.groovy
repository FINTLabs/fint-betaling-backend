package no.fint.betaling.factory

import no.fint.betaling.util.FintObjectFactory
import no.fint.betaling.util.RestUtil
import no.fint.model.felles.kompleksedatatyper.Identifikator
import no.fint.model.felles.kompleksedatatyper.Personnavn
import no.fint.model.resource.felles.PersonResource
import spock.lang.Specification

class CustomerFactorySpec extends Specification {
    private RestUtil restUtil
    private FintObjectFactory fintObjectFactory

    void setup() {
        restUtil = Mock()
        fintObjectFactory = new FintObjectFactory()
    }

    def "Get customer given valid person resource returns customer"() {
        given:
        def person = fintObjectFactory.newStudent()

        when:
        def customer = CustomerFactory.toCustomer(person)

        then:
        customer.id == '21i3v9'
    }

    def "Determine customer ID from NIN"() {
        given:
        def nin = "12345678901"

        when:
        def id = CustomerFactory.getCustomerId(new PersonResource(fodselsnummer: new Identifikator(identifikatorverdi: nin)))

        then:
        id == '21i3v9'
    }

    def "Get Personnavn as String"() {
        given:
        def name = new Personnavn()
        if (fornavn) {
            name.setFornavn(fornavn)
        }
        if (mellomnavn) {
            name.setMellomnavn(mellomnavn)
        }
        if (etternavn) {
            name.setEtternavn(etternavn)
        }

        when:
        def result = CustomerFactory.getDisplayName(name)

        then:
        result == personnavn

        where:
        fornavn | mellomnavn  | etternavn || personnavn
        'Sture' | null        | 'Hansen'  || 'Hansen, Sture'
        'Sture' | 'Pettersen' | 'Hansen'  || 'Hansen, Sture Pettersen'
        null    | null        | 'Hansen'  || 'Hansen'
        null    | null        | null      || ''
    }
}
