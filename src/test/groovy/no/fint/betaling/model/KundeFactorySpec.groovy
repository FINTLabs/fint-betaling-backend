package no.fint.betaling.model

import no.fint.betaling.service.PersonService
import no.fint.model.felles.Person
import no.fint.model.felles.kompleksedatatyper.Adresse
import no.fint.model.felles.kompleksedatatyper.Identifikator
import no.fint.model.felles.kompleksedatatyper.Kontaktinformasjon
import no.fint.model.felles.kompleksedatatyper.Personnavn
import no.fint.model.utdanning.elev.Elev
import org.springframework.hateoas.Link
import org.springframework.hateoas.Resource
import spock.lang.Specification

class KundeFactorySpec extends Specification {

    private PersonService personService
    private KundeFactory factory

    void setup() {
        personService = Mock(PersonService)
        factory = new KundeFactory(personService: personService)
    }

    def "Get kunde given resource with links"() {
        given:
        def validUrl = 'http://localhost/person'
        def person = createPerson('12345678901', 'Oslo', 'test@test.com')
        def resource = new Resource<Elev>(new Elev(), new Link(validUrl, 'person'))

        when:
        def kunde = factory.getKunde(resource)

        then:
        1 * personService.getPerson(validUrl) >> person
        kunde.kundenummer == '12345678901'
        kunde.postadresse.poststed == 'Oslo'
        kunde.kontaktinformasjon.epostadresse == 'test@test.com'
    }

    def "Get kunde given resource with invalid link throws InvalidResponseException"() {
        given:
        def invalidUrl = 'invalid.url'
        def invalidResource = new Resource<Elev>(new Elev(), new Link(invalidUrl, 'person'))

        when:
        factory.getKunde(invalidResource)

        then:
        1 * personService.getPerson(invalidUrl) >> { throw new InvalidResponseException('test exception',new Exception())}
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
