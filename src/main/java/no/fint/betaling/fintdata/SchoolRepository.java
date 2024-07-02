package no.fint.betaling.fintdata;

import lombok.extern.slf4j.Slf4j;
import no.fint.betaling.common.config.Endpoints;
import no.fint.betaling.common.util.RestUtil;
import no.fint.model.resource.utdanning.utdanningsprogram.SkoleResource;
import no.fint.model.resource.utdanning.utdanningsprogram.SkoleResources;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public class SchoolRepository extends FintResourceRepository<SkoleResource, SkoleResources> {

    public SchoolRepository(RestUtil restUtil, Endpoints endpoints) {
        super(restUtil, endpoints.getSchool(), SkoleResources.class);
    }

    public List<SkoleResource> getDistinctSchools() {
        return get().stream().distinct().collect(Collectors.toList());
    }

}
