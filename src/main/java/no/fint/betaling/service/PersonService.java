package no.fint.betaling.service;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import lombok.extern.slf4j.Slf4j;
import no.fint.betaling.exception.PersonNotFoundException;
import no.fint.betaling.factory.CustomerFactory;
import no.fint.betaling.repository.GroupRepository;
import no.fint.model.resource.Link;
import no.fint.model.resource.felles.PersonResource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutionException;

@Service
@Slf4j
public class PersonService extends CacheLoader<String, PersonResource> {

    private final GroupRepository groupRepository;
    private final LoadingCache<String, PersonResource> personCache;

    public PersonService(
            GroupRepository groupRepository,
            @Value("${fint.betaling.cache-spec:expireAfterWrite=30m}") String spec) {
        this.groupRepository = groupRepository;
        personCache = CacheBuilder.from(spec).build(this);
    }

    public PersonResource getPersonById(String id) {
        try {
            return personCache.get(id);
        } catch (ExecutionException e) {
            log.debug(id, e);
            return null;
        }
    }

    public Link getPersonLinkById(String id) {
        return getPersonById(id)
                .getSelfLinks()
                .stream()
                .findFirst()
                .orElse(null);
    }

    @Override
    public PersonResource load(String key) {
        return groupRepository
                .getStudents()
                .values()
                .parallelStream()
                .filter(p -> StringUtils.equals(key, CustomerFactory.getCustomerId(p)))
                .findAny()
                .orElseThrow(() -> new PersonNotFoundException(key));
    }
}
