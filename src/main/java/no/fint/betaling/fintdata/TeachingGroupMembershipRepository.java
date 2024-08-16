package no.fint.betaling.fintdata;

import no.fint.betaling.common.config.Endpoints;
import no.fint.betaling.common.util.RestUtil;
import no.fint.model.resource.utdanning.timeplan.UndervisningsgruppemedlemskapResource;
import no.fint.model.resource.utdanning.timeplan.UndervisningsgruppemedlemskapResources;
import org.springframework.stereotype.Repository;

@Repository
public class TeachingGroupMembershipRepository extends FintResourceRepository<UndervisningsgruppemedlemskapResource, UndervisningsgruppemedlemskapResources> {

    public TeachingGroupMembershipRepository(RestUtil restUtil, Endpoints endpoints) {
        super(restUtil, endpoints.getTeachingGroup(), UndervisningsgruppemedlemskapResources.class);
    }
}
