package no.fint.betaling.fintdata;

import no.fint.betaling.common.config.Endpoints;
import no.fint.betaling.common.util.RestUtil;
import no.fint.model.resource.utdanning.elev.BasisgruppeResource;
import no.fint.model.resource.utdanning.elev.BasisgruppeResources;

public class BasisGroupRepository extends FintResourceRepository<BasisgruppeResource, BasisgruppeResources> {

    public BasisGroupRepository(RestUtil restUtil, Endpoints endpoints) {
        super(restUtil, endpoints.getBasisGroup(), BasisgruppeResources.class);
    }
}