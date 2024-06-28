package no.fint.betaling.fintdata;

import no.fint.betaling.common.config.Endpoints;
import no.fint.betaling.common.util.RestUtil;
import no.fint.betaling.model.Lineitem;
import no.fint.betaling.model.Taxcode;
import no.fint.model.resource.Link;
import no.fint.model.resource.okonomi.kodeverk.VareResource;
import no.fint.model.resource.okonomi.kodeverk.VareResources;

import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

public class LineItemRepository extends FintResourceRepository<VareResource, VareResources> {

    private final TaxCodeRepository taxcodeRepository;

    private ConcurrentMap<String, Lineitem> lineItems = new ConcurrentHashMap<>();

    public LineItemRepository(Endpoints endpoints, RestUtil restUtil, TaxCodeRepository taxcodeRepository) {
        super(restUtil, endpoints.getTaxCode(), VareResources.class);
        this.taxcodeRepository = taxcodeRepository;
    }

    public Lineitem getLineItemByUri(String uri) {
        if (lineItems.isEmpty()) {
            update();
        }
        return lineItems.get(uri);
    }

    public Collection<Lineitem> getLineItems() {
        if (lineItems.isEmpty()) {
            update();
        }
        return Collections.unmodifiableCollection(lineItems.values());
    }

    @Override
    protected void onResourcesUpdated() {

        ConcurrentMap<String, Lineitem> updatedMap = resources
                .values()
                .stream()
                .map(this::createLineItem)
                .collect(Collectors.toConcurrentMap(Lineitem::getUri, taxcode -> taxcode));

        if (!updatedMap.isEmpty()) {
            lineItems = updatedMap;
        }
    }

    private Lineitem createLineItem(VareResource resource) {
        Lineitem lineitem = new Lineitem();
        //lineitem.setItemCode(v.getSystemId().getIdentifikatorverdi());
        lineitem.setItemCode(resource.getKode());
        lineitem.setItemPrice(resource.getPris());
        lineitem.setDescription(resource.getNavn());
        resource.getMerverdiavgift()
                .stream()
                .map(Link::getHref)
                //.map(href -> StringUtils.substringAfterLast(href, "/"))
                .map(taxcodeRepository::getTaxcodeByUri)
                .filter(Objects::nonNull)
                .map(Taxcode::getRate)
                .forEach(lineitem::setTaxrate);
        resource.getSelfLinks()
                .stream()
                .map(Link::getHref)
                .forEach(lineitem::setUri);

        return lineitem;
    }
}
