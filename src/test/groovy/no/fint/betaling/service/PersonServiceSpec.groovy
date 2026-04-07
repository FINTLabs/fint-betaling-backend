package no.fint.betaling.service

import no.fint.betaling.fintdata.StudentRepository
import no.fint.betaling.organisation.PersonService
import no.novari.fint.model.felles.kompleksedatatyper.Identifikator
import no.novari.fint.model.resource.Link
import no.novari.fint.model.resource.felles.PersonResource
import spock.lang.Specification

class PersonServiceSpec extends Specification {

    def 'Get Person Link by ID'() {
        given:
        def studentRepository = Mock(StudentRepository)
        def personService = new PersonService(studentRepository)

        when:
        def result = personService.getPersonLinkById('21i3v9')

        then:
        result.href == 'link.to.Person'
        1 * studentRepository.get() >> Collections.singletonList(createPerson())
    }

    PersonResource createPerson() {
        def r = new PersonResource(fodselsnummer: new Identifikator(identifikatorverdi: '12345678901'))
        r.addSelf(Link.with('link.to.Person'))
        return r
    }
}
