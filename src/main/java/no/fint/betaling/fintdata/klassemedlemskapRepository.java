package no.fint.betaling.fintdata;

import no.fint.betaling.common.config.Endpoints;
import no.fint.betaling.common.util.RestUtil;
import no.novari.fint.model.resource.utdanning.elev.KlassemedlemskapResource;
import no.novari.fint.model.resource.utdanning.elev.KlassemedlemskapResources;
import org.springframework.stereotype.Repository;

@Repository
public class klassemedlemskapRepository extends FintResourceRepository<KlassemedlemskapResource, KlassemedlemskapResources> {

    public klassemedlemskapRepository(RestUtil restUtil, Endpoints endpoints) {
        super(restUtil, endpoints.getKlassemedlemskap(), KlassemedlemskapResources.class);
    }
}