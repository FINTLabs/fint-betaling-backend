package no.fint.betaling.fintdata;

import lombok.extern.slf4j.Slf4j;
import no.fint.betaling.common.util.RestUtil;
import no.fint.betaling.model.Taxcode;
import no.fint.model.resource.AbstractCollectionResources;
import no.fint.model.resource.FintLinks;
import no.fint.model.resource.Link;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

@Slf4j
public class FintResourceRepository<T extends FintLinks, U extends AbstractCollectionResources<T>> {

    private final RestUtil restUtil;
    private final String endpoint;
    protected Map<Link, T> resources = new HashMap<>();
    private final Class<U> resourcesClass;
    private final Function<T, List<Link>> linkProvider;

    public FintResourceRepository(RestUtil restUtil, String endpoint, Class<U> resourcesClass) {
        this(restUtil, endpoint, resourcesClass, FintLinks::getSelfLinks);
    }

    public FintResourceRepository(RestUtil restUtil, String endpoint, Class<U> resourcesClass, Function<T, List<Link>> linkProvider) {
        this.restUtil = restUtil;
        this.endpoint = endpoint;
        this.resourcesClass = resourcesClass;
        this.linkProvider = linkProvider;
    }

    public int update() {
        log.debug("Updating ressurs from {} ...", endpoint);

        U updatedResources;

        try {
            updatedResources = restUtil.getWithRetry(resourcesClass, endpoint).block();
        } catch (WebClientResponseException ex) {
            log.error(ex.getMessage());
            return 0;
        }

        if (updatedResources == null || updatedResources.getTotalItems() == 0) return 0;

        Map<Link, T> newResources = new HashMap<>();
        updatedResources.getContent().forEach(resource -> linkProvider.apply(resource).forEach(link -> newResources.put(link, resource)));
        resources = newResources;
        log.debug("Update completed, {} resources.", resources.size());
        onResourcesUpdated();
        return resources.size();
    }

    public Collection<T> get() {
        if (resources.isEmpty()) {
            update();
        }

        return resources.values();
    }

    public Map<Link, T> getMap() {
        if (resources.isEmpty()) {
            update();
        }

        return resources;
    }

    public T getResourceByLink(Link link) {
        return resources.get(link);
    }

    public boolean isEmpty() {
        return resources.isEmpty();
    }

    protected void onResourcesUpdated() {
    }

}
