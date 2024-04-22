package no.fint.betaling.taxcode;

import lombok.extern.slf4j.Slf4j;
import no.fint.betaling.common.config.Endpoints;
import no.fint.betaling.model.Taxcode;
import no.fint.betaling.common.util.RestUtil;
import no.fint.model.resource.Link;
import no.fint.model.resource.okonomi.kodeverk.MerverdiavgiftResources;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentSkipListMap;

@Slf4j
@Repository
public class TaxcodeRepository {

    private final Endpoints endpoints;

    private final RestUtil restUtil;

    private final ConcurrentMap<String, Taxcode> taxcodes = new ConcurrentSkipListMap<>();

    public TaxcodeRepository(Endpoints endpoints, RestUtil restUtil) {
        this.endpoints = endpoints;
        this.restUtil = restUtil;
    }

    public Taxcode getTaxcodeByUri(String uri) {
        if (taxcodes.isEmpty()) {
            updateTaxcodes();
        }
        return taxcodes.get(uri);
    }

//    public Taxcode getTaxcodeByCode(String code) {
//        return getTaxcodes()
//                .stream()
//                .filter(tax -> StringUtils.equals(code, tax.getCode()))
//                .findFirst()
//                .orElse(null);
//    }

    public Collection<Taxcode> getTaxcodes() {
        if (taxcodes.isEmpty()) {
            updateTaxcodes();
        }
        return Collections.unmodifiableCollection(taxcodes.values());
    }

    @Scheduled(initialDelay = 1000L, fixedDelayString = "${fint.betaling.refresh-rate:1200000}")
    public void updateTaxcodes() {
        log.info("Updating tax codes from {} ...", endpoints.getTaxcode());
        restUtil.getUpdates(MerverdiavgiftResources.class, endpoints.getTaxcode())
                .block()
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
