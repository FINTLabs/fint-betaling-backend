package no.fint.betaling.service;

import lombok.extern.slf4j.Slf4j;
import no.fint.betaling.model.Kunde;
import no.fint.betaling.model.KundeFactory;
import no.fint.betaling.util.ResourceCache;
import no.fint.model.resource.felles.PersonResource;
import no.fint.model.resource.felles.PersonResources;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Service
public class CustomerService {

    @Autowired
    private CacheService cacheService;

    @Autowired
    private KundeFactory kundeFactory;

    @Value("${fint.betaling.endpoints.person}")
    private String personEndpoint;

    private ResourceCache<PersonResource> personResourceResourceCache;

    @PostConstruct
    public void init() { personResourceResourceCache = new ResourceCache<>(cacheService, personEndpoint, PersonResources.class); }

    @Scheduled(initialDelay = 10000, fixedRateString = "${fint.betaling.refresh-rate:360000}")
    public void updateCaches() {
        personResourceResourceCache.updateCaches();
    }

    public List<Kunde> getCustomers(String orgId, String filter) {
        Stream<Kunde> allCustomers = personResourceResourceCache
                .getResources(orgId)
                .stream()
                .map(kundeFactory::getKunde)
                .filter(Objects::nonNull);

        if (StringUtils.isEmpty(filter)) return allCustomers.collect(Collectors.toList());

        String finalFilter = filter.toLowerCase();
        return allCustomers
                .filter(customer ->
                customer.getFulltNavn().toLowerCase()
                        .contains(finalFilter))
                .collect(Collectors.toList());
    }
}
