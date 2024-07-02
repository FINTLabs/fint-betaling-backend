package no.fint.betaling.fintdata;

import no.fint.betaling.common.config.Endpoints;
import no.fint.betaling.common.util.RestUtil;
import no.fint.betaling.model.Taxcode;
import no.fint.model.resource.Link;
import no.fint.model.resource.okonomi.kodeverk.MerverdiavgiftResource;
import no.fint.model.resource.okonomi.kodeverk.MerverdiavgiftResources;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

@Repository
public class TaxCodeRepository extends FintResourceRepository<MerverdiavgiftResource, MerverdiavgiftResources> {

    private ConcurrentMap<String, Taxcode> taxcodes = new ConcurrentHashMap<>();

    public TaxCodeRepository(RestUtil restUtil, Endpoints endpoints) {
        super(restUtil, endpoints.getTaxCode(), MerverdiavgiftResources.class);
    }

    public Taxcode getTaxcodeByUri(String uri) {
        if (taxcodes.isEmpty()) {
            update();
        }

        return taxcodes.get(uri);
    }

    public Collection<Taxcode> getTaxcodes() {
        if (taxcodes.isEmpty()) {
            update();
        }

        return Collections.unmodifiableCollection(taxcodes.values());
    }

    @Override
    protected void onResourcesUpdated() {

        ConcurrentMap<String, Taxcode> updatedMap = resources
                .values()
                .stream()
                .map(this::createTaxcode)
                .collect(Collectors.toConcurrentMap(Taxcode::getUri, taxcode -> taxcode));

        if (!updatedMap.isEmpty()) {
            taxcodes = updatedMap;
        }
    }

    private Taxcode createTaxcode(MerverdiavgiftResource resource) {
        Taxcode taxcode = new Taxcode();
        taxcode.setCode(resource.getKode());
        taxcode.setRate(resource.getSats());
        taxcode.setDescription(resource.getNavn());
        getSelflink(resource).ifPresent(taxcode::setUri);
        return taxcode;
    }

    private Optional<String> getSelflink(MerverdiavgiftResource resource) {
        return resource.getSelfLinks()
                .stream()
                .map(Link::getHref)
                .findFirst();
    }
}