package no.fint.betaling.fintdata;

import no.fint.betaling.common.config.Endpoints;
import no.fint.betaling.common.util.RestUtil;
import no.fint.model.resource.utdanning.elev.ElevforholdResource;
import no.fint.model.resource.utdanning.elev.ElevforholdResources;

public class StudentRelationRepository extends FintResourceRepository<ElevforholdResource, ElevforholdResources> {

    public StudentRelationRepository(RestUtil restUtil, Endpoints endpoints) {
        super(restUtil, endpoints.getStudentRelation(), ElevforholdResources.class);
    }
}