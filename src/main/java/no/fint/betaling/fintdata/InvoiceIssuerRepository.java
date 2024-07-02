package no.fint.betaling.fintdata;

import lombok.extern.slf4j.Slf4j;
import no.fint.betaling.common.config.Endpoints;
import no.fint.betaling.common.util.RestUtil;
import no.fint.betaling.model.Principal;
import no.fint.betaling.organisation.OrganisationRepository;
import no.fint.model.resource.Link;
import no.fint.model.resource.okonomi.faktura.FakturautstederResource;
import no.fint.model.resource.okonomi.faktura.FakturautstederResources;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.stream.Collectors;

@Slf4j
@Repository
public class InvoiceIssuerRepository extends FintResourceRepository<FakturautstederResource, FakturautstederResources>{

    private final LineItemRepository lineitemRepository;
    private final OrganisationRepository organisationRepository;

    private final ConcurrentMap<String, Principal> invoiceIssuers = new ConcurrentSkipListMap<>();

    public InvoiceIssuerRepository(RestUtil restUtil, Endpoints endpoints, LineItemRepository lineitemRepository, OrganisationRepository organisationRepository) {
        super(restUtil, endpoints.getInvoiceIssuer(), FakturautstederResources.class);
        this.lineitemRepository = lineitemRepository;
        this.organisationRepository = organisationRepository;
    }

    public Collection<Principal> getInvoiceIssuers() {
        if (invoiceIssuers.isEmpty()) {
            updateInvoiceIssuers();
        }
        return Collections.unmodifiableCollection(invoiceIssuers.values());
    }

    public void updateInvoiceIssuers() {
        log.info("Updating invoice issuer from {} ...", endpoint);
        restUtil.getUpdates(FakturautstederResources.class, endpoint)
                .block()
                .getContent()
                .forEach(fakturautsteder -> {
                    Principal principal = new Principal();
                    if (isEmpty()) return;
                    principal.setOrganisation(organisationRepository.getOrganisationByHref(fakturautsteder.getOrganisasjonselement().get(0).getHref()));
                    principal.setCode(fakturautsteder.getSystemId().getIdentifikatorverdi());
                    principal.setDescription(fakturautsteder.getNavn());
                    principal.setLineitems(fakturautsteder.getVare()
                            .stream()
                            .map(Link::getHref)
                            .map(lineitemRepository::getLineItemByUri)
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


}
