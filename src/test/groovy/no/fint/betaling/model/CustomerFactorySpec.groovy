package no.fint.betaling.model

import no.fint.betaling.factory.CustomerFactory
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
    private CustomerFactory factory
    private String orgId

    void setup() {
        restUtil = Mock(RestUtil)
        factory = new CustomerFactory()
        orgId = 'test.no'
    }

    def "Get customer given valid person resource returns customer"() {
        given:
        def person = createPerson('12345678901', 'Oslo', 'test@test.com')

        when:
        def kunde = factory.toCustomer(person)

        then:
        kunde.kundenummer == '21i3v9'
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
        def navn = new Personnavn()
        if (fornavn) {
            navn.setFornavn(fornavn)
        }
        if (mellomnavn) {
            navn.setMellomnavn(mellomnavn)
        }
        if (etternavn) {
            navn.setEtternavn(etternavn)
        }

        when:
        def result = CustomerFactory.getDisplayName(navn)

        then:
        result == personnavn

        where:
        fornavn | mellomnavn  | etternavn || personnavn
        'Sture' | null        | 'Hansen'  || 'Hansen, Sture'
        'Sture' | 'Pettersen' | 'Hansen'  || 'Hansen, Sture Pettersen'
        null    | null        | 'Hansen'  || 'Hansen'
        null    | null        | null      || ''
    }

    private static PersonResource createPerson(String kundenummer, String poststed, String epostadresse) {
        def fodselsnummer = new Identifikator()
        fodselsnummer.setIdentifikatorverdi(kundenummer)

        def personnavn = new Personnavn()
        personnavn.setFornavn('Ola')
        personnavn.setEtternavn('Testesen')

        def kontaktinformasjon = new Kontaktinformasjon()
        kontaktinformasjon.setEpostadresse(epostadresse)

        def adresse = new AdresseResource()
        adresse.setPoststed(poststed)

        def person = new PersonResource()
        person.setFodselsnummer(fodselsnummer)
        person.setNavn(personnavn)
        person.setKontaktinformasjon(kontaktinformasjon)
        person.setPostadresse(adresse)

        person.addLink('self', new Link('person.self'))

        return person
    }
}
