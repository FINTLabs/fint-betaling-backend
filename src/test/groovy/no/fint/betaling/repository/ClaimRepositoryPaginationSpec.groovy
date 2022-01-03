package no.fint.betaling.repository

import no.fint.betaling.util.BetalingObjectFactory
import org.springframework.data.mongodb.core.MongoTemplate
import spock.lang.Specification

class ClaimRepositoryPaginationSpec extends Specification {

    private MongoTemplate mongoTemplate
    private ClaimRepository claimRepository
    private BetalingObjectFactory betalingObjectFactory;

    void setup() {
        mongoTemplate = Mock(MongoTemplate)
        claimRepository = new ClaimRepository(mongoTemplate: mongoTemplate)
        betalingObjectFactory = new BetalingObjectFactory()
    }

    def "First page"() {

    }
}
