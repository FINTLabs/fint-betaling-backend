package no.fint.betaling.fintdata;

import lombok.extern.slf4j.Slf4j;
import no.fint.betaling.common.util.RestUtil;
import no.novari.fint.model.resource.AbstractCollectionResources;
import no.novari.fint.model.resource.FintLinks;
import no.novari.fint.model.resource.Link;
import org.springframework.util.StringUtils;

import java.util.*;
import java.time.Duration;
import java.util.function.Function;

@Slf4j
public class FintResourceRepository<T extends FintLinks, U extends AbstractCollectionResources<T>> {

    protected final RestUtil restUtil;
    protected final String endpoint;
    protected volatile Map<Link, T> resources = Map.of();
    private volatile boolean initialized;
    private long lastLoadAttempt;
    private static final Duration LOAD_TIMEOUT = Duration.ofMinutes(2);
    private static final Duration FAILED_LOAD_RETRY_DELAY = Duration.ofSeconds(30);
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

    public synchronized int update() {
        log.debug("Updating ressurs from {} ...", endpoint);
        U updatedResources;

        try {
            updatedResources = restUtil.getWithRetry(resourcesClass, endpoint).block(LOAD_TIMEOUT);
        } catch (RuntimeException ex) {
            log.error("Failed to update resources from {}; retaining the previous cache", endpoint, ex);
            return 0;
        } finally {
            lastLoadAttempt = System.nanoTime();
        }

        if (updatedResources == null) return 0;

        Map<Link, T> newResources = new HashMap<>();
        updatedResources.getContent().forEach(resource -> linkProvider.apply(resource).forEach(link -> newResources.put(link, resource)));
        resources = newResources;
        initialized = true;
        log.debug("Update completed, {} resources.", resources.size());
        onResourcesUpdated();
        return resources.size();
    }

    public Collection<T> get() {
        ensureLoaded();
        return resources.values();
    }

    public Map<Link, T> getMap() {
        ensureLoaded();
        return resources;
    }

    private void ensureLoaded() {
        if (initialized || !resources.isEmpty()) return;
        synchronized (this) {
            if (!initialized && resources.isEmpty()
                    && (lastLoadAttempt == 0 || System.nanoTime() - lastLoadAttempt >= FAILED_LOAD_RETRY_DELAY.toNanos())) {
                update();
            }
        }
    }

    public Optional<T> getResourceByLink(Link link) {
        if (link == null || !StringUtils.hasText(link.getHref())) {
            return Optional.empty();
        }

        return Optional.ofNullable(resources.get(link));
    }

    public boolean isEmpty() {
        return resources.isEmpty();
    }

    protected void onResourcesUpdated() {
    }

}
