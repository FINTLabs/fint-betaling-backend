package no.fint.betaling.fintdata;

import lombok.extern.slf4j.Slf4j;
import no.fint.betaling.common.config.Endpoints;
import no.fint.betaling.common.util.RestUtil;
import no.fint.model.resource.Link;
import no.fint.model.resource.utdanning.timeplan.UndervisningsgruppeResource;
import no.fint.model.resource.utdanning.timeplan.UndervisningsgruppeResources;
import org.springframework.stereotype.Repository;

import java.util.Map;

@Slf4j
@Repository
public class TeachingGroupRepository {

    private final ResourceCache<UndervisningsgruppeResource, UndervisningsgruppeResources> teachingGroups;

    public TeachingGroupRepository(RestUtil restUtil, Endpoints endpoints) {
        teachingGroups = new ResourceCache<>(restUtil, endpoints.getTeachingGroup(), UndervisningsgruppeResources.class);
    }

    public void update() {
        int teachingGroupUpdated = teachingGroups.update();
    }

    public Map<Link, UndervisningsgruppeResource> getTeachingGroups() {
        return teachingGroups.get();
    }
}
