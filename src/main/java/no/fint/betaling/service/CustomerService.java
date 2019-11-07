package no.fint.betaling.service;

import lombok.extern.slf4j.Slf4j;
import no.fint.betaling.factory.CustomerFactory;
import no.fint.betaling.model.Customer;
import no.fint.model.resource.Link;
import no.fint.model.resource.felles.PersonResource;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@SuppressWarnings("unchecked")
public class CustomerService {

    /*
    To be deleted???
     */

    private final CacheManager cacheManager;

    public CustomerService(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    public List<Customer> getCustomers(String orgId, String filter) {
        Cache cache = cacheManager.getCache("studentCache");
        Map<Link, PersonResource> customers = (Map<Link, PersonResource>) cache.get(orgId).get();

        if (StringUtils.isEmpty(filter))
            return customers.values().stream()
                    .map(CustomerFactory::toCustomer)
                    .collect(Collectors.toList());

        return customers.values().stream()
                .map(CustomerFactory::toCustomer)
                .filter(customer -> customer.getName().toLowerCase().contains(filter.toLowerCase()))
                .collect(Collectors.toList());
    }
}
