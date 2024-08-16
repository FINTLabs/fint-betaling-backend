package no.fint.betaling.fintdata;

import no.fint.betaling.common.config.Endpoints;
import no.fint.betaling.common.util.RestUtil;
import no.fint.model.resource.utdanning.elev.KontaktlarergruppemedlemskapResource;
import no.fint.model.resource.utdanning.elev.KontaktlarergruppemedlemskapResources;
import org.springframework.stereotype.Repository;

@Repository
public class ContactTeacherGroupMembershipRepository extends FintResourceRepository<KontaktlarergruppemedlemskapResource, KontaktlarergruppemedlemskapResources> {

    public ContactTeacherGroupMembershipRepository(RestUtil restUtil, Endpoints endpoints) {
        super(restUtil, endpoints.getContactTeacherGroup(), KontaktlarergruppemedlemskapResources.class);
    }
}