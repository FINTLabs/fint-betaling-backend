package no.fint.betaling.service;

import lombok.extern.slf4j.Slf4j;
import no.fint.betaling.model.Kunde;
import no.fint.model.resource.Link;
import no.fint.model.resource.felles.PersonResource;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class CustomerService {
    private final CacheManager cacheManager;

    public CustomerService(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    @SuppressWarnings("unchecked")
    public List<Kunde> getCustomers(String orgId, String filter) {
        Cache cache = cacheManager.getCache("studentsMap");
        Map<Link, PersonResource> customers = (Map<Link, PersonResource>) cache.get(orgId).get();

        if (StringUtils.isEmpty(filter))
            return customers.values().stream()
                    .map(Kunde::of)
                    .collect(Collectors.toList());

        return customers.values().stream()
                .map(Kunde::of)
                .filter(customer -> customer.getFulltNavn().toLowerCase().contains(filter.toLowerCase()))
                .collect(Collectors.toList());
    }
}
