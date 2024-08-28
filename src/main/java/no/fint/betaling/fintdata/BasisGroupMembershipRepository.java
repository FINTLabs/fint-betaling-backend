package no.fint.betaling.fintdata;

import no.fint.betaling.common.config.Endpoints;
import no.fint.betaling.common.util.RestUtil;
import no.fint.model.resource.utdanning.elev.BasisgruppemedlemskapResource;
import no.fint.model.resource.utdanning.elev.BasisgruppemedlemskapResources;
import org.springframework.stereotype.Repository;

@Repository
public class BasisGroupMembershipRepository extends FintResourceRepository<BasisgruppemedlemskapResource, BasisgruppemedlemskapResources> {

    public BasisGroupMembershipRepository(RestUtil restUtil, Endpoints endpoints) {
        super(restUtil, endpoints.getBasisGroupMembership(), BasisgruppemedlemskapResources.class);
    }
}