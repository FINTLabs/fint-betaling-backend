package no.fint.betaling.model

import no.fint.betaling.service.RestService
import no.fint.model.felles.Person
import no.fint.model.felles.kompleksedatatyper.Adresse
import no.fint.model.felles.kompleksedatatyper.Identifikator
import no.fint.model.felles.kompleksedatatyper.Kontaktinformasjon
import no.fint.model.felles.kompleksedatatyper.Personnavn
import no.fint.model.resource.Link
import no.fint.model.resource.utdanning.elev.ElevResource
import spock.lang.Specification

class KundeFactorySpec extends Specification {

    private RestService restService
    private KundeFactory factory
    private String orgId

    void setup() {
        restService = Mock(RestService)
        factory = new KundeFactory(restService: restService)
        orgId = 'test.no'
    }

    def "Get kunde given resource with links"() {
        given:
        def validUrl = 'http://localhost/person'
        def person = createPerson('12345678901', 'Oslo', 'test@test.com')
        def resource = new ElevResource()
        resource.addPerson(Link.with(validUrl))

        when:
        def kunde = factory.getKunde(orgId, resource)

        then:
        1 * restService.getResource(Person, validUrl, 'test.no') >> person
        kunde.kundenummer == '12345678901'
        kunde.postadresse.poststed == 'Oslo'
        kunde.kontaktinformasjon.epostadresse == 'test@test.com'
    }

    def "Get kunde given resource with invalid link throws InvalidResponseException"() {
        given:
        def invalidUrl = 'invalid.url'
        def invalidResource = new ElevResource()
        invalidResource.addPerson(Link.with(invalidUrl))

        when:
        factory.getKunde(orgId, invalidResource)

        then:
        1 * restService.getResource(Person, invalidUrl, 'test.no') >> {
            throw new InvalidResponseException('test exception', new Exception())
        }
        thrown(InvalidResponseException)
    }

    private static Person createPerson(String kundenummer, String poststed, String epostadresse) {

        def fodselsnummer = new Identifikator()
        fodselsnummer.setIdentifikatorverdi(kundenummer)

        def personnavn = new Personnavn()
        personnavn.setFornavn('Ola')
        personnavn.setEtternavn('Testesen')

        def kontaktinformasjon = new Kontaktinformasjon()
        kontaktinformasjon.setEpostadresse(epostadresse)

        def adresse = new Adresse()
        adresse.setPoststed(poststed)

        def person = new Person()
        person.setFodselsnummer(fodselsnummer)
        person.setNavn(personnavn)
        person.setKontaktinformasjon(kontaktinformasjon)
        person.setPostadresse(adresse)

        return person
    }
}
