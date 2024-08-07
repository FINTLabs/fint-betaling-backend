package no.fint.betaling.fintdata;

import no.fint.betaling.common.config.Endpoints;
import no.fint.betaling.common.util.RestUtil;
import no.fint.model.resource.felles.PersonResource;
import no.fint.model.resource.felles.PersonResources;
import org.springframework.stereotype.Repository;

@Repository
public class StudentRepository extends FintResourceRepository<PersonResource, PersonResources> {

    public StudentRepository(RestUtil restUtil, Endpoints endpoints) {
        super(restUtil, endpoints.getPerson(), PersonResources.class, PersonResource::getElev);
    }
}