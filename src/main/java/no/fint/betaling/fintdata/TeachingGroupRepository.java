package no.fint.betaling.fintdata;

import lombok.extern.slf4j.Slf4j;
import no.fint.betaling.common.config.Endpoints;
import no.fint.betaling.common.util.RestUtil;
import no.fint.model.resource.utdanning.timeplan.UndervisningsgruppeResource;
import no.fint.model.resource.utdanning.timeplan.UndervisningsgruppeResources;
import org.springframework.stereotype.Repository;

@Slf4j
@Repository
public abstract class TeachingGroupRepository extends FintResourceRepository<UndervisningsgruppeResource, UndervisningsgruppeResources> {

    public TeachingGroupRepository(RestUtil restUtil, Endpoints endpoints) {
        super(restUtil, endpoints.getTeachingGroup(), UndervisningsgruppeResources.class);
    }
}
