package no.fint.betaling.fintdata;

import lombok.extern.slf4j.Slf4j;
import no.fint.betaling.common.config.Endpoints;
import no.fint.betaling.common.util.RestUtil;
import no.fint.betaling.model.Lineitem;
import no.fint.betaling.model.Principal;
import no.fint.betaling.organisation.OrganisationRepository;
import no.fint.model.resource.Link;
import no.fint.model.resource.okonomi.faktura.FakturautstederResource;
import no.fint.model.resource.okonomi.faktura.FakturautstederResources;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.stream.Collectors;

@Slf4j
@Repository
public class InvoiceIssuerRepository extends FintResourceRepository<FakturautstederResource, FakturautstederResources> {

    private final LineItemRepository lineitemRepository;
    private final OrganisationRepository organisationRepository;

    private List<Principal> invoiceIssuers = new ArrayList<>();

    public InvoiceIssuerRepository(RestUtil restUtil, Endpoints endpoints, LineItemRepository lineitemRepository, OrganisationRepository organisationRepository) {
        super(restUtil, endpoints.getInvoiceIssuer(), FakturautstederResources.class);
        this.lineitemRepository = lineitemRepository;
        this.organisationRepository = organisationRepository;
    }

    public Collection<Principal> getInvoiceIssuers() {
        if (invoiceIssuers.isEmpty()) {
            update();
        }
        return Collections.unmodifiableCollection(invoiceIssuers);
    }

    @Override
    protected void onResourcesUpdated() {
        List<Principal> updatedList = resources
                .values()
                .stream()
                .map(this::createPrincipal)
                .collect(Collectors.toList());

        if (!updatedList.isEmpty()) {
            invoiceIssuers = updatedList;
        }
    }

    private Principal createPrincipal(FakturautstederResource fakturautsteder) {
        Principal principal = new Principal();
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

        return principal;
    }
}
