package no.fint.betaling.model

import no.fint.betaling.service.RestService
import no.fint.model.felles.Person
import no.fint.model.felles.kompleksedatatyper.Adresse
import no.fint.model.felles.kompleksedatatyper.Identifikator
import no.fint.model.felles.kompleksedatatyper.Kontaktinformasjon
import no.fint.model.felles.kompleksedatatyper.Personnavn
import no.fint.model.resource.Link
import no.fint.model.resource.felles.PersonResource
import no.fint.model.resource.felles.PersonResources
import no.fint.model.resource.felles.kompleksedatatyper.AdresseResource
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
        def person = createPerson('12345678901', 'Oslo', 'test@test.com')

        when:
        def kunde = factory.getKunde(person)

        then:
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
        1 * restService.getResource(PersonResource, invalidUrl, 'test.no') >> {
            throw new InvalidResponseException('test exception', new Exception())
        }
        thrown(InvalidResponseException)
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
