package no.fint.betaling.repository;

import lombok.extern.slf4j.Slf4j;
import no.fint.betaling.config.Endpoints;
import no.fint.betaling.model.Organisation;
import no.fint.betaling.util.RestUtil;
import no.fint.model.resource.Link;
import no.fint.model.resource.administrasjon.organisasjon.OrganisasjonselementResources;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentSkipListMap;

@Slf4j
@Repository
public class OrganisationRepository {

    private final Endpoints endpoints;

    private final RestUtil restUtil;

    private final ConcurrentMap<String, Organisation> organisations = new ConcurrentSkipListMap<>();

    private final ConcurrentMap<String, String> superiors = new ConcurrentSkipListMap<>();

    public OrganisationRepository(Endpoints endpoints, RestUtil restUtil) {
        this.endpoints = endpoints;
        this.restUtil = restUtil;
    }

    public Organisation getOrganisationByHref(String href) {
        if (organisations.isEmpty()) {
            updateOrganisations();
        }
        return organisations.get(href);
    }

    public Organisation getTopOrganisationByHref(String href) {
        log.debug("Get organisation by {}", href);
        Organisation organisation;
        Organisation parent;
        do {
            organisation = getOrganisationByHref(href);
            href = superiors.getOrDefault(href, href);
            parent = getOrganisationByHref(href);
            log.debug("{} vs {}", organisation, parent);
        } while (parent != organisation);
        return organisation;
    }

    public Collection<Organisation> getOrganisations() {
        if (organisations.isEmpty()) {
            updateOrganisations();
        }
        return Collections.unmodifiableCollection(organisations.values());
    }

    @Scheduled(initialDelay = 1000L, fixedDelayString = "${fint.betaling.refresh-rate:1200000}")
    public void updateOrganisations() {
        log.info("Updating organisations from {} ...", endpoints.getOrganisation());
        restUtil.getUpdates(OrganisasjonselementResources.class, endpoints.getOrganisation())
                .getContent()
                .forEach(o -> {
                    Organisation organisation = new Organisation();
                    if (o.getOrganisasjonsnummer() != null) {
                        organisation.setOrganisationNumber(o.getOrganisasjonsnummer().getIdentifikatorverdi());
                    }
                    organisation.setName(o.getNavn());
                    o.getSelfLinks()
                            .stream()
                            .map(Link::getHref)
                            .map(StringUtils::lowerCase)
                            .forEach(link -> {
                                log.debug("{}", link);
                                organisations.put(link, organisation);
                                o.getOverordnet()
                                        .stream()
                                        .map(Link::getHref)
                                        .map(StringUtils::lowerCase)
                                        .forEach(superior -> superiors.put(link, superior));
                            });
                });
        log.info("Update completed, {} organisations and {} links.", organisations.size(), superiors.size());
    }

}
