package no.fint.betaling.factory

import no.fint.betaling.util.RestUtil
import no.fint.model.felles.kompleksedatatyper.Identifikator
import no.fint.model.felles.kompleksedatatyper.Kontaktinformasjon
import no.fint.model.felles.kompleksedatatyper.Personnavn
import no.fint.model.resource.Link
import no.fint.model.resource.felles.PersonResource
import no.fint.model.resource.felles.kompleksedatatyper.AdresseResource
import spock.lang.Specification

class CustomerFactorySpec extends Specification {

    private RestUtil restUtil
    private String orgId

    void setup() {
        restUtil = Mock(RestUtil)
        orgId = 'test.no'
    }

    def "Get customer given valid person resource returns customer"() {
        given:
        def person = createPerson('12345678901', 'Oslo', 'test@test.com')

        when:
        def customer = CustomerFactory.toCustomer(person)

        then:
        customer.id == '21i3v9'
    }

    def "Determine customer ID from NIN"() {
        given:
        def nin = "12345678901"

        when:
        def id = CustomerFactory.getCustomerId(nin)

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

    private static PersonResource createPerson(String id, String city, String email) {
        def nin = new Identifikator()
        nin.setIdentifikatorverdi(id)

        def name = new Personnavn()
        name.setFornavn('Ola')
        name.setEtternavn('Testesen')

        def contactInformation = new Kontaktinformasjon()
        contactInformation.setEpostadresse(email)

        def address = new AdresseResource()
        address.setAdresselinje(["gatenavn"])
        address.setPoststed(city)

        def person = new PersonResource()
        person.setFodselsnummer(nin)
        person.setNavn(name)
        person.setKontaktinformasjon(contactInformation)
        person.setPostadresse(address)

        person.addLink('self', new Link('person.self'))

        return person
    }
}
