package no.fint.betaling.fintdata;

import no.fint.betaling.common.config.Endpoints;
import no.fint.betaling.common.util.RestUtil;
import no.fint.model.resource.utdanning.elev.SkoleressursResource;
import no.fint.model.resource.utdanning.elev.SkoleressursResources;
import org.springframework.stereotype.Repository;

@Repository
public class SchoolResourceRepository extends FintResourceRepository<SkoleressursResource, SkoleressursResources> {

    public SchoolResourceRepository(RestUtil restUtil, Endpoints endpoints) {
        super(restUtil, endpoints.getSchoolResource(), SkoleressursResources.class, SkoleressursResource::getPersonalressurs);
    }
}
