package no.fint.betaling.service


import no.fint.betaling.util.FintObjectFactory
import no.fint.betaling.util.RestUtil
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import spock.lang.Specification

class OrganisationServiceSpec extends Specification {

    RestUtil restUtil = Mock()
    OrganisationService organisationService

    def setup() {
        organisationService = new OrganisationService("/utdanning/utdanningsprogram/skole", "/administrasjon/organisasjon/organisasjonselement", restUtil)
    }

    def "Get organisation by org-number"() {
        given:
        def school = FintObjectFactory.newSchool()

        when:
        def result = organisationService.getOrganisationByOrganisationNumber("org-id")

        then:
        1 * restUtil.get(_, _) >> Mono.just(school)
        StepVerifier.create(result)
                .expectNextMatches { it.name == school.getNavn() }
                .verifyComplete()
    }
}
