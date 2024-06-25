package no.fint.betaling.fintdata;

import lombok.extern.slf4j.Slf4j;
import no.fint.betaling.common.config.Endpoints;
import no.fint.betaling.common.util.RestUtil;
import no.fint.model.resource.Link;
import no.fint.model.resource.utdanning.elev.SkoleressursResource;
import no.fint.model.resource.utdanning.elev.SkoleressursResources;
import no.fint.model.resource.utdanning.utdanningsprogram.SkoleResource;
import no.fint.model.resource.utdanning.utdanningsprogram.SkoleResources;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Repository
public class SchoolRepository {

    private final ResourceCache<SkoleResource, SkoleResources> schools;
    private final ResourceCache<SkoleressursResource, SkoleressursResources> schoolresources;

    public SchoolRepository(RestUtil restUtil, Endpoints endpoints) {
        schools = new ResourceCache<>(restUtil, endpoints.getSchool(), SkoleResources.class);
        schoolresources = new ResourceCache<>(restUtil, endpoints.getSchoolResource(), SkoleressursResources.class, SkoleressursResource::getPersonalressurs);
    }


    public void updateSchool() {
        int schoolsUpdated = schools.update();
        int schoolResourceUpdated = schoolresources.update();
        log.info("Updates completed, schools ({}), schoolresource ({})", schoolsUpdated, schoolResourceUpdated);
    }

    public Map<Link, SkoleResource> getSchools() {
        return schools.get();
    }

    public List<SkoleResource> getDistinctSchools() {
        return getSchools().values().stream().distinct().collect(Collectors.toList());
    }

    public Map<Link, SkoleressursResource> getSchoolresources() {
        return schoolresources.get();
    }

}
