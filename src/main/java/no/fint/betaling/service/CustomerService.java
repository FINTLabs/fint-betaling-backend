package no.fint.betaling.service;

import lombok.extern.slf4j.Slf4j;
import no.fint.betaling.factory.CustomerFactory;
import no.fint.betaling.model.Customer;
import no.fint.model.resource.Link;
import no.fint.model.resource.felles.PersonResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class CustomerService {

    /*
    To be deleted???

    private final CacheService cacheService;

    public CustomerService(CacheService cacheService) {
        this.cacheService = cacheService;
    }

    public List<Customer> getCustomers(String filter) {
        Map<Link, PersonResource> customers = cacheService.getResources("students");

        if (StringUtils.isEmpty(filter))
            return customers.values().stream()
                    .map(CustomerFactory::toCustomer)
                    .collect(Collectors.toList());

        return customers.values().stream()
                .map(CustomerFactory::toCustomer)
                .filter(customer -> customer.getName().toLowerCase().contains(filter.toLowerCase()))
                .collect(Collectors.toList());
    }
     */
}
