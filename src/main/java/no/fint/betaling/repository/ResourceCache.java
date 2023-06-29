package no.fint.betaling.repository;

import lombok.extern.slf4j.Slf4j;
import no.fint.betaling.util.RestUtil;
import no.fint.model.resource.FintLinks;
import no.fint.model.resource.Link;
import no.fint.model.resource.utdanning.elev.SkoleressursResources;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.HashMap;
import java.util.Map;

@Slf4j
public class ResourceCache<T extends FintLinks> {

    private final RestUtil restUtil;
    private final String endpoint;
    private final Map<Link, T> resources = new HashMap<>();
    private final Class<T> clazz;

    public ResourceCache(RestUtil restUtil, String endpoint, Class<T> clazz) {
        this.restUtil = restUtil;
        this.endpoint = endpoint;
        this.clazz = clazz;
    }

    public Map<Link, T> update() {
        log.info("Updating skoleressurs from {} ...", endpoint);

        GenericCollectionResoures<T> updatedResources;

        try {
            updatedResources = restUtil.get(clazz, endpoint).block();
        } catch (WebClientResponseException ex) {
            log.error(ex.getMessage());
            return null;
        }

        if (updatedResources.getTotalItems() == 0) return null;

        updatedResources.getContent().forEach(r -> r.getSelfLinks(link -> resources.put(link, r)));
        log.info("Update completed, {} resources.", resources.size());

        return resources;
    }

    public Map<Link, T> get() {
        if (resources.isEmpty()) {
            update();
        }

        return resources;
    }
}
