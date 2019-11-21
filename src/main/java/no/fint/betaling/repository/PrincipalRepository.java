package no.fint.betaling.repository;

import lombok.extern.slf4j.Slf4j;
import no.fint.betaling.model.Lineitem;
import no.fint.betaling.model.Principal;
import no.fint.betaling.util.RestUtil;
import no.fint.betaling.util.UriUtil;
import no.fint.model.resource.Link;
import no.fint.model.resource.administrasjon.okonomi.OppdragsgiverResources;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Repository;

import java.net.URI;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.stream.Collectors;

@Slf4j
@Repository
public class PrincipalRepository {

    @Value("${fint.betaling.endpoints.principal}")
    private URI principalEndpoint;

    @Autowired
    private RestUtil restUtil;

    @Autowired
    private LineitemRepository lineitemRepository;

    private final ConcurrentMap<URI, Principal> principals = new ConcurrentSkipListMap<>();

    public Principal getPrincipalByUri(URI uri) {
        if (principals.isEmpty()) {
            updatePrincipals();
        }
        return principals.get(uri);
    }

    public Collection<Principal> getPrincipals() {
        if (principals.isEmpty()) {
            updatePrincipals();
        }
        return Collections.unmodifiableCollection(principals.values());
    }

    @Scheduled(initialDelay = 1000L, fixedDelayString = "${fint.betaling.refresh-rate:1200000}")
    public void updatePrincipals() {
        log.info("Updating principals from {} ...", principalEndpoint);
        restUtil.getUpdates(OppdragsgiverResources.class, principalEndpoint)
                .getContent()
                .forEach(o -> {
                    Principal principal = new Principal();
                    principal.setCode(o.getSystemId().getIdentifikatorverdi());
                    principal.setDescription(o.getNavn());
                    principal.setLineitems(o.getVarelinje()
                            .stream()
                            .map(Link::getHref)
                            .map(UriUtil::parseUri)
                            .map(lineitemRepository::getLineitemByUri)
                            .map(Lineitem::getCode)
                            .collect(Collectors.toSet()));
                    o.getSelfLinks()
                            .stream()
                            .map(Link::getHref)
                            .map(UriUtil::parseUri)
                            .findFirst().ifPresent(principal::setUri);
                    principals.put(principal.getUri(), principal);
                });
        log.info("Update completed, {} principals.", principals.size());
    }

}
