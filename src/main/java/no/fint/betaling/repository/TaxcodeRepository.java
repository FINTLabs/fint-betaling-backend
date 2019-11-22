package no.fint.betaling.repository;

import lombok.extern.slf4j.Slf4j;
import no.fint.betaling.model.Taxcode;
import no.fint.betaling.util.RestUtil;
import no.fint.model.resource.Link;
import no.fint.model.resource.administrasjon.okonomi.MvakodeResources;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Repository;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentSkipListMap;

@Slf4j
@Repository
public class TaxcodeRepository {

    @Value("${fint.betaling.endpoints.mva-code}")
    private URI taxcodeEndpoint;

    @Autowired
    private RestUtil restUtil;

    private final ConcurrentMap<URI, Taxcode> taxcodes = new ConcurrentSkipListMap<>();

    public Taxcode getTaxcodeByUri(URI uri) {
        if (taxcodes.isEmpty()) {
            updateTaxcodes();
        }
        return taxcodes.get(uri);
    }

    public Collection<Taxcode> getTaxcodes() {
        if (taxcodes.isEmpty()) {
            updateTaxcodes();
        }
        return Collections.unmodifiableCollection(taxcodes.values());
    }

    @Scheduled(initialDelay = 1000L, fixedDelayString = "${fint.betaling.refresh-rate:1200000}")
    public void updateTaxcodes() {
        log.info("Updating tax codes from {} ...", taxcodeEndpoint);
        restUtil.getUpdates(MvakodeResources.class, taxcodeEndpoint)
                .getContent()
                .forEach(m -> {
                    Taxcode taxcode = new Taxcode();
                    taxcode.setCode(m.getKode());
                    taxcode.setRate(m.getPromille());
                    taxcode.setDescription(m.getNavn());
                    m.getSelfLinks()
                            .stream()
                            .map(Link::getHref)
                            .map(UriComponentsBuilder::fromUriString)
                            .map(UriComponentsBuilder::build)
                            .map(UriComponents::toUri)
                            .findFirst().ifPresent(taxcode::setUri);
                    taxcodes.put(taxcode.getUri(), taxcode);
                });
        log.info("Update completed, {} tax codes.", taxcodes.size());
    }

}
