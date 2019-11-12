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

    private final CacheService cacheService;

    public CustomerService(CacheService cacheService) {
        this.cacheService = cacheService;
    }

    public List<Customer> getCustomers(String orgId, String filter) {
        Map<Link, PersonResource> customers = cacheService.getCache("studentCache", orgId);

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
