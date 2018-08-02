package no.fint.betaling.util;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import no.fint.betaling.service.CacheService;
import no.fint.model.FintMainObject;
import no.fint.model.resource.AbstractCollectionResources;
import no.fint.model.resource.FintLinks;

import java.util.*;

@Slf4j
@AllArgsConstructor
public class ResourceCache<T extends FintMainObject & FintLinks> {

    private final Map<String, List<T>> cache = Collections.synchronizedMap(new HashMap<>());

    private CacheService cacheService;
    private String endpoint;
    private Class<? extends AbstractCollectionResources<T>> type;

    public void updateCaches() {
        cache.forEach(this::updateCache);
    }

    private void updateCache(String orgId, List<T> list) {
        log.info("{}: Updating {} cache.", orgId, type.getSimpleName());
        AbstractCollectionResources<T> resources = cacheService.getUpdates(type, endpoint, orgId);
        log.info("{}: Found {} entries", orgId, resources.getTotalItems());
        list.addAll(resources.getContent());
        log.info("{}: New size {}", orgId, list.size());
    }

    public List<T> getResources(String orgId) {
        return cache.computeIfAbsent(orgId, o -> {
            List<T> l = Collections.synchronizedList(new ArrayList<>());
            updateCache(orgId, l);
            return l;
        });
    }

}
