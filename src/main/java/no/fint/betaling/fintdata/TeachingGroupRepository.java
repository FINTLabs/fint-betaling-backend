package no.fint.betaling.fintdata;

import lombok.extern.slf4j.Slf4j;
import no.fint.betaling.common.config.Endpoints;
import no.fint.betaling.common.util.RestUtil;
import no.fint.model.resource.Link;
import no.fint.model.resource.utdanning.timeplan.UndervisningsgruppeResource;
import no.fint.model.resource.utdanning.timeplan.UndervisningsgruppeResources;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import java.util.Map;

@Slf4j
@Repository
public abstract class TeachingGroupRepository extends FintRepository<UndervisningsgruppeResource, UndervisningsgruppeResources> {

    private final FintRepository fintRepository;

    public TeachingGroupRepository(RestUtil restUtil, Endpoints endpoints, Class<UndervisningsgruppeResources> resourceClass, @Qualifier("fintRepository") FintRepository fintRepository) {
        cache = new FintRepository.resourceCache
        super(restUtil, endpoints, resourceClass);
        this.fintRepository = fintRepository;
    }

    public Map<Link, UndervisningsgruppeResource> getTeachingGroups() {
        return teachingGroups.get();
    }

}
