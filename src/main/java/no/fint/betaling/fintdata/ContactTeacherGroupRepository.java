package no.fint.betaling.fintdata;

import no.fint.betaling.common.config.Endpoints;
import no.fint.betaling.common.util.RestUtil;
import no.fint.model.resource.utdanning.elev.KontaktlarergruppeResource;
import no.fint.model.resource.utdanning.elev.KontaktlarergruppeResources;
import org.springframework.stereotype.Repository;

@Repository
public class ContactTeacherGroupRepository extends FintResourceRepository<KontaktlarergruppeResource, KontaktlarergruppeResources> {

    public ContactTeacherGroupRepository(RestUtil restUtil, Endpoints endpoints) {
        super(restUtil, endpoints.getContactTeacherGroup(), KontaktlarergruppeResources.class);
    }
}