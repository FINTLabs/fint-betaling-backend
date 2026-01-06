package no.fint.betaling.fintdata;

import no.fint.betaling.common.config.Endpoints;
import no.fint.betaling.common.util.RestUtil;
import no.fint.model.resource.utdanning.elev.KlasseResource;
import no.fint.model.resource.utdanning.elev.KlasseResources;
import org.springframework.stereotype.Repository;

@Repository
public class KlasseRepository extends FintResourceRepository<KlasseResource, KlasseResources> {

    public KlasseRepository(RestUtil restUtil, Endpoints endpoints) {
        super(restUtil, endpoints.getKlasse(), KlasseResources.class);
    }
}