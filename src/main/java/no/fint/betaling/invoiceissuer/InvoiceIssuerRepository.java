package no.fint.betaling.invoiceissuer;

import lombok.extern.slf4j.Slf4j;
import no.fint.betaling.claim.LineitemRepository;
import no.fint.betaling.common.config.Endpoints;
import no.fint.betaling.model.Principal;
import no.fint.betaling.common.util.RestUtil;
import no.fint.betaling.organisation.OrganisationRepository;
import no.fint.model.resource.Link;
import no.fint.model.resource.okonomi.faktura.FakturautstederResource;
import no.fint.model.resource.okonomi.faktura.FakturautstederResources;
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
public class InvoiceIssuerRepository {

    private final Endpoints endpoints;

    private final RestUtil restUtil;

    private final LineitemRepository lineitemRepository;

    private final OrganisationRepository organisationRepository;

    private final ConcurrentMap<String, Principal> invoiceIssuers = new ConcurrentSkipListMap<>();

    public InvoiceIssuerRepository(RestUtil restUtil, LineitemRepository lineitemRepository, OrganisationRepository organisationRepository, Endpoints endpoints) {
        this.restUtil = restUtil;
        this.lineitemRepository = lineitemRepository;
        this.organisationRepository = organisationRepository;
        this.endpoints = endpoints;
    }

    public Collection<Principal> getInvoiceIssuers() {
        if (invoiceIssuers.isEmpty()) {
            updateInvoiceIssuers();
        }
        return Collections.unmodifiableCollection(invoiceIssuers.values());
    }

    @Scheduled(initialDelay = 1000L, fixedDelayString = "${fint.betaling.refresh-rate:1200000}")
    public void updateInvoiceIssuers() {
        log.info("Updating invoice issuer from {} ...", endpoints.getInvoiceIssuer());
        restUtil.getUpdates(FakturautstederResources.class, endpoints.getInvoiceIssuer())
                .block()
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
                    invoiceIssuers.put(principal.getUri(), principal);
                });
        log.info("Update completed, {} invoice issuers.", invoiceIssuers.size());
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
