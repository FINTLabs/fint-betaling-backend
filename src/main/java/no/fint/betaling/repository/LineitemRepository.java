package no.fint.betaling.repository;

import lombok.extern.slf4j.Slf4j;
import no.fint.betaling.config.Endpoints;
import no.fint.betaling.model.Lineitem;
import no.fint.betaling.model.Taxcode;
import no.fint.betaling.util.RestUtil;
import no.fint.model.resource.Link;
import no.fint.model.resource.okonomi.kodeverk.VareResources;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentSkipListMap;

@Repository
@Slf4j
public class LineitemRepository {

    private final Endpoints endpoints;

    private final RestUtil restUtil;

    private final TaxcodeRepository taxcodeRepository;

    private final ConcurrentMap<String, Lineitem> lineitems = new ConcurrentSkipListMap<>();

    public LineitemRepository(Endpoints endpoints, RestUtil restUtil, TaxcodeRepository taxcodeRepository) {
        this.endpoints = endpoints;
        this.restUtil = restUtil;
        this.taxcodeRepository = taxcodeRepository;
    }

    public Lineitem getLineitemByUri(String uri) {
        if (lineitems.isEmpty()) {
            updateLineitems();
        }
        return lineitems.get(uri);
    }

    public Collection<Lineitem> getLineitems() {
        if (lineitems.isEmpty()) {
            updateLineitems();
        }
        return Collections.unmodifiableCollection(lineitems.values());
    }

    @Scheduled(initialDelay = 1000L, fixedDelayString = "${fint.betaling.refresh-rate:1200000}")
    public void updateLineitems() {
        log.info("Updating vare from {} ...", endpoints.getVare());
        try {
            restUtil.getUpdates(VareResources.class, endpoints.getVare())
                    .block()
                    .getContent()
                    .forEach(v -> {
                        Lineitem lineitem = new Lineitem();
                        //lineitem.setItemCode(v.getSystemId().getIdentifikatorverdi());
                        lineitem.setItemCode(v.getKode());
                        lineitem.setItemPrice(v.getPris());
                        lineitem.setDescription(v.getNavn());
                        v.getMerverdiavgift()
                                .stream()
                                .map(Link::getHref)
                                //.map(href -> StringUtils.substringAfterLast(href, "/"))
                                .map(taxcodeRepository::getTaxcodeByUri)
                                .filter(Objects::nonNull)
                                .map(Taxcode::getRate)
                                .forEach(lineitem::setTaxrate);
                        v.getSelfLinks()
                                .stream()
                                .map(Link::getHref)
                                .forEach(lineitem::setUri);
                        lineitems.put(lineitem.getUri(), lineitem);
                    });

            log.info("Update completed, {} varer.", lineitems.size());

        } catch (Exception exception) {
            log.error("Updating vare failed: " + exception.getMessage(), exception);
        }
    }
}
