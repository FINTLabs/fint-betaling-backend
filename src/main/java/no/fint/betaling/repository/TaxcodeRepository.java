package no.fint.betaling.repository;

import lombok.extern.slf4j.Slf4j;
import no.fint.betaling.model.Taxcode;
import no.fint.betaling.util.RestUtil;
import no.fint.model.resource.Link;
import no.fint.model.resource.okonomi.kodeverk.MerverdiavgiftResource;
import no.fint.model.resource.okonomi.kodeverk.MerverdiavgiftResources;
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
public class TaxcodeRepository {

    @Value("${fint.betaling.endpoints.mva-code:/okonomi/kodeverk/merverdiavgift}")
    private String taxcodeEndpoint;

    @Autowired
    private RestUtil restUtil;

    private final ConcurrentMap<String, Taxcode> taxcodes = new ConcurrentSkipListMap<>();

    public Taxcode getTaxcodeByUri(String uri) {
        if (taxcodes.isEmpty()) {
            updateTaxcodes();
        }
        return taxcodes.get(uri);
    }

    public Taxcode getTaxcodeByCode(String code) {
        return getTaxcodes()
                .stream()
                .filter(tax -> StringUtils.equals(code, tax.getCode()))
                .findFirst()
                .orElse(null);
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
        restUtil.getUpdates(MerverdiavgiftResources.class, taxcodeEndpoint)
                .getContent()
                .forEach(m -> {
                    Taxcode taxcode = new Taxcode();
                    taxcode.setCode(m.getKode());
                    taxcode.setRate(m.getSats());
                    taxcode.setDescription(m.getNavn());
                    m.getSelfLinks()
                            .stream()
                            .map(Link::getHref)
                            .findFirst()
                            .ifPresent(taxcode::setUri);
                    taxcodes.put(taxcode.getUri(), taxcode);
                });
        log.info("Update completed, {} tax codes.", taxcodes.size());
    }

}
