package no.fint.betaling.repository;

import lombok.extern.slf4j.Slf4j;
import no.fint.betaling.model.Lineitem;
import no.fint.betaling.model.Taxcode;
import no.fint.betaling.util.RestUtil;
import no.fint.model.resource.Link;
import no.fint.model.resource.administrasjon.okonomi.VarelinjeResources;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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

    @Value("${fint.betaling.endpoints.lineitem}")
    private String lineitemEndpoint;

    @Autowired
    private RestUtil restUtil;

    @Autowired
    private TaxcodeRepository taxcodeRepository;

    private final ConcurrentMap<String, Lineitem> lineitems = new ConcurrentSkipListMap<>();

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
        log.info("Updating line items from {} ...", lineitemEndpoint);
        restUtil.getUpdates(VarelinjeResources.class, lineitemEndpoint)
                .getContent()
                .forEach(v -> {
                    Lineitem lineitem = new Lineitem();
                    lineitem.setItemCode(v.getSystemId().getIdentifikatorverdi());
                    lineitem.setItemPrice(v.getPris());
                    lineitem.setDescription(v.getNavn());
                    v.getMvakode()
                            .stream()
                            .map(Link::getHref)
                            .map(href -> StringUtils.substringAfterLast(href, "/"))
                            .map(taxcodeRepository::getTaxcodeByCode)
                            .filter(Objects::nonNull)
                            .map(Taxcode::getRate)
                            .forEach(lineitem::setTaxrate);
                    v.getSelfLinks()
                            .stream()
                            .map(Link::getHref)
                            .forEach(lineitem::setUri);
                    lineitems.put(lineitem.getUri(), lineitem);
                });
        log.info("Update completed, {} line items.", lineitems.size());
    }

}
