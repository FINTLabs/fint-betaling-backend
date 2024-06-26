package no.fint.betaling.common;

import lombok.extern.slf4j.Slf4j;
import no.fint.betaling.common.util.RestUtil;
import no.fint.model.resource.AbstractCollectionResources;
import no.fint.model.resource.FintLinks;
import no.fint.model.resource.Link;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

@Slf4j
public class ResourceCache<T extends FintLinks, U extends AbstractCollectionResources<T>> {

    private final RestUtil restUtil;
    private final String endpoint;
    private Map<Link, T> resources = new HashMap<>();
    private final Class<U> clazz;
    private final Function<T, List<Link>> linkProvider;

    public ResourceCache(RestUtil restUtil, String endpoint, Class<U> clazz) {
        this(restUtil, endpoint, clazz, FintLinks::getSelfLinks);
    }

    public ResourceCache(RestUtil restUtil, String endpoint, Class<U> clazz, Function<T, List<Link>> linkProvider) {
        this.restUtil = restUtil;
        this.endpoint = endpoint;
        this.clazz = clazz;
        this.linkProvider = linkProvider;
    }

    public int update() {
        log.debug("Updating ressurs from {} ...", endpoint);

        U updatedResources;

        try {
            updatedResources = restUtil.getWithRetry(clazz, endpoint).block();
        } catch (WebClientResponseException ex) {
            log.error(ex.getMessage());
            return 0;
        }

        if (updatedResources == null || updatedResources.getTotalItems() == 0) return 0;

        Map<Link, T> newResources = new HashMap<>();
        updatedResources.getContent().forEach(resource -> linkProvider.apply(resource).forEach(link -> newResources.put(link, resource)));
        resources = newResources;
        log.debug("Update completed, {} resources.", resources.size());
        return resources.size();
    }

    public Map<Link, T> get() {
        if (resources.isEmpty()) {
            update();
        }

        return resources;
    }

    public boolean isEmpty() {
        return resources.isEmpty();
    }

}
