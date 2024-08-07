package no.fint.betaling.fintdata;

import lombok.extern.slf4j.Slf4j;
import no.fint.betaling.common.config.Endpoints;
import no.fint.betaling.common.util.RestUtil;
import no.fint.betaling.model.Lineitem;
import no.fint.betaling.model.Organisation;
import no.fint.model.resource.Link;
import no.fint.model.resource.administrasjon.organisasjon.OrganisasjonselementResource;
import no.fint.model.resource.administrasjon.organisasjon.OrganisasjonselementResources;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.stream.Collectors;

@Slf4j
@Repository
public class OrganisationRepository extends FintResourceRepository<OrganisasjonselementResource, OrganisasjonselementResources> {

    private ConcurrentMap<String, Organisation> organisations = new ConcurrentSkipListMap<>();

    private ConcurrentMap<String, String> superiors = new ConcurrentSkipListMap<>();

    public OrganisationRepository(RestUtil restUtil, Endpoints endpoints) {
        super(restUtil, endpoints.getOrganisationselement(), OrganisasjonselementResources.class);
    }

    public Organisation getOrganisationByHref(String href) {
        if (organisations.isEmpty()) {
            update();
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

    @Override
    protected void onResourcesUpdated() {

        ConcurrentMap<String, Organisation> updatedOrganisations = new ConcurrentSkipListMap<>();
        ConcurrentMap<String, String> updatedSuperiors = new ConcurrentSkipListMap<>();


        resources
                .values()
                .forEach( resource -> {
                    Organisation organisation = new Organisation();
                    if (resource.getOrganisasjonsnummer() != null) {
                        organisation.setOrganisationNumber(resource.getOrganisasjonsnummer().getIdentifikatorverdi());
                    }

                    organisation.setName(resource.getNavn());

                    resource.getSelfLinks()
                            .stream()
                            .map(Link::getHref)
                            .map(StringUtils::lowerCase)
                            .forEach(link -> {
                                log.trace("{}", link);
                                updatedOrganisations.put(link, organisation);

                                resource.getOverordnet()
                                        .stream()
                                        .map(Link::getHref)
                                        .map(StringUtils::lowerCase)
                                        .forEach(superior -> updatedSuperiors.put(link, superior));
                            });
                });

        if (!updatedOrganisations.isEmpty()) {
            organisations = updatedOrganisations;
            superiors = updatedSuperiors;
        }

        log.debug("Organisation and superiors maps updated: {} organisations, {} superiors", organisations.size(), superiors.size());
    }
}
