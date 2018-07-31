package no.fint.betaling.model

import no.fint.betaling.service.RestService
import no.fint.model.felles.kompleksedatatyper.Identifikator
import no.fint.model.felles.kompleksedatatyper.Kontaktinformasjon
import no.fint.model.felles.kompleksedatatyper.Personnavn
import no.fint.model.resource.Link
import no.fint.model.resource.felles.PersonResource
import no.fint.model.resource.felles.kompleksedatatyper.AdresseResource
import spock.lang.Specification

class KundeFactorySpec extends Specification {

    private RestService restService
    private KundeFactory factory
    private String orgId

    void setup() {
        restService = Mock(RestService)
        factory = new KundeFactory()
        orgId = 'test.no'
    }

    def "Get customer given valid person resource returns customer"() {
        given:
        def person = createPerson('12345678901', 'Oslo', 'test@test.com')

        when:
        def kunde = factory.getKunde(person)

        then:
        kunde.kundeid == '21i3v9'
    }

    def "Determine customer ID from NIN"() {
        given:
        def nin = "12345678901"

        when:
        def id = KundeFactory.getCustomerId(nin)

        then:
        id == '21i3v9'
    }

    def "Get Personnavn as String"() {
        given:
        def navn1 = new Personnavn(fornavn: "Sture", etternavn: "Hansen")
        def navn2 = new Personnavn(fornavn: "Sture", mellomnavn: "Pettersen", etternavn: "Hansen")
        def navn3 = new Personnavn(etternavn: "Hansen")

        when:
        def result = KundeFactory.getPersonnavnAsString(navn1)

        then:
        result == "Hansen, Sture"

        when:
        result = KundeFactory.getPersonnavnAsString(navn2)

        then:
        result == "Hansen, Sture Pettersen"

        when:
        result = KundeFactory.getPersonnavnAsString(navn3)

        then:
        result == "Hansen"
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
