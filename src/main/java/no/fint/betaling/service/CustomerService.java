package no.fint.betaling.service;

import lombok.extern.slf4j.Slf4j;
import no.fint.betaling.model.InvalidResponseException;
import no.fint.betaling.model.Kunde;
import no.fint.betaling.model.KundeFactory;
import no.fint.model.resource.felles.PersonResource;
import no.fint.model.resource.felles.PersonResources;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class CustomerService {

    @Autowired
    private CacheService cacheService;

    @Autowired
    private KundeFactory kundeFactory;

    @Value("${fint.betaling.endpoints.person}")
    private String personEndpoint;

    private Map<String, List<Kunde>> customerCache = Collections.synchronizedMap(new HashMap<>());

    @Scheduled(initialDelay = 10000, fixedRateString = "${fint.betaling.refresh-rate:500000}")
    public void updateCaches() {
        customerCache.forEach(this::updateCache);
    }

    private void updateCache(String orgId, List<Kunde> customerList) {
        log.info("{}: Updating cache.", orgId);
        PersonResources personResources = cacheService.getUpdates(PersonResources.class, personEndpoint, orgId);
        log.info("{}: Found {} entries", orgId, personResources.getTotalItems());
        personResources.getContent().stream().map(kundeFactory::getKunde).filter(Objects::nonNull).forEach(customerList::add);
        log.info("{}: New size {}", orgId, customerList.size());
    }

    public List<Kunde> getCustomers(String orgId, String filter) {
        List<Kunde> allCustomers = customerCache.computeIfAbsent(orgId, o -> {
            List<Kunde> l = Collections.synchronizedList(new ArrayList<>());
            updateCache(orgId, l);
            return l;
        });

        if (StringUtils.isEmpty(filter)) return allCustomers;

        String finalFilter = filter.toLowerCase();
        return allCustomers.stream().filter(customer ->
                customer.getNavn().toLowerCase()
                        .contains(finalFilter))
                .collect(Collectors.toList());
    }
}
