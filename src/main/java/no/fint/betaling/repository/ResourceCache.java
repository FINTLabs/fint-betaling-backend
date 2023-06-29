package no.fint.betaling.repository;

import lombok.extern.slf4j.Slf4j;
import no.fint.betaling.util.RestUtil;
import no.fint.model.resource.AbstractCollectionResources;
import no.fint.model.resource.FintLinks;
import no.fint.model.resource.Link;
import no.fint.model.resource.utdanning.elev.SkoleressursResources;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.HashMap;
import java.util.Map;

@Slf4j
public class ResourceCache<T extends FintLinks, U extends AbstractCollectionResources<T>> {

    private final RestUtil restUtil;
    private final String endpoint;
    private final Map<Link, T> resources = new HashMap<>();
    private final Class<U> clazz;

    public ResourceCache(RestUtil restUtil, String endpoint, Class<U> clazz) {
        this.restUtil = restUtil;
        this.endpoint = endpoint;
        this.clazz = clazz;
    }

    public void update() {
        log.info("Updating ressurs from {} ...", endpoint);

        U updatedResources;

        try {
            updatedResources = restUtil.get(clazz, endpoint).block();
        } catch (WebClientResponseException ex) {
            log.error(ex.getMessage());
            return;
        }

        if (updatedResources.getTotalItems() == 0) return;

        updatedResources.getContent().forEach(resource -> resource.getSelfLinks().forEach(link -> resources.put(link, resource)));
        log.info("Update completed, {} resources.", resources.size());
    }

    public Map<Link, T> get() {
        if (resources.isEmpty()) {
            update();
        }

        return resources;
    }

    public boolean isEmpty(){
        return resources.isEmpty();
    }

}
