package no.fint.betaling.fintdata;

import lombok.extern.slf4j.Slf4j;
import no.fint.betaling.common.config.Endpoints;
import no.fint.betaling.common.util.RestUtil;
import no.fint.model.resource.AbstractCollectionResources;
import no.fint.model.resource.FintLinks;
import org.springframework.stereotype.Repository;

@Repository
@Slf4j
public abstract class FintRepository<T extends FintLinks, U extends AbstractCollectionResources<T>> {

    protected final ResourceCache<T, U> resourceCache;

    public FintRepository(RestUtil restUtil, Endpoints endpoints, Class<U> resourceClass) {
        resourceCache = new ResourceCache<>(restUtil, endpoints.getAllEndpoints().get(resourceClass.getName()), resourceClass);
    }

    public void updateAll() {
        int resourceUpdated = resourceCache.update();
        log.info("updated resource, {}",resourceUpdated);

    }
}
