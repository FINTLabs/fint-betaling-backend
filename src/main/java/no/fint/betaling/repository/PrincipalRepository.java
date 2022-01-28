package no.fint.betaling.repository;

import lombok.extern.slf4j.Slf4j;
import no.fint.betaling.model.Principal;
import no.fint.betaling.util.FintEndpointsRepository;
import no.fint.model.resource.Link;
import no.fint.model.resource.okonomi.faktura.FakturautstederResource;
import no.fint.model.resource.okonomi.faktura.FakturautstederResources;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.stream.Collectors;


@Slf4j
@Repository
public class PrincipalRepository {

    @Value("${fint.betaling.endpoints.principal:/okonomi/faktura/fakturautsteder}")
    private String principalEndpoint;

    @Autowired
    private FintEndpointsRepository fintEndpointsRepository;
    @Autowired
    private LineitemRepository lineitemRepository;
    @Autowired
    private OrganisationRepository organisationRepository;

    private final ConcurrentMap<String, Principal> principals = new ConcurrentSkipListMap<>();

    public Principal getPrincipalByUri(String uri) {
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
        fintEndpointsRepository.getUpdates(FakturautstederResources.class, principalEndpoint)
                .getContent()
                .forEach(fakturautsteder -> {
                    Principal principal = new Principal();
                    if (isOrganisasjonselementMissing(fakturautsteder)) return;
                    principal.setOrganisation(organisationRepository.getOrganisationByHref(fakturautsteder.getOrganisasjonselement().get(0).getHref()));
                    principal.setCode(fakturautsteder.getSystemId().getIdentifikatorverdi());
                    principal.setDescription(fakturautsteder.getNavn());
                    principal.setLineitems(fakturautsteder.getVare()
                            .stream()
                            .map(Link::getHref)
                            .map(lineitemRepository::getLineitemByUri)
                            .filter(Objects::nonNull)
                            .collect(Collectors.toSet()));
                    fakturautsteder.getSelfLinks()
                            .stream()
                            .map(Link::getHref)
                            .forEach(principal::setUri);
                    principals.put(principal.getUri(), principal);
                });
        log.info("Update completed, {} principals.", principals.size());
    }

    private boolean isOrganisasjonselementMissing(FakturautstederResource fakturautsteder) {
        if (fakturautsteder.getOrganisasjonselement().isEmpty()) {
            log.warn(String.format(
                    "Skipping fakturautsteder %s \"%s\" because organisasjonselement is missing.",
                    fakturautsteder.getNavn(),
                    fakturautsteder.getSystemId().getIdentifikatorverdi())
            );
            return true;
        }
        return false;
    }

}
