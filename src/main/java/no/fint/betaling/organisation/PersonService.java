package no.fint.betaling.organisation;

import lombok.extern.slf4j.Slf4j;
import no.fint.betaling.common.exception.PersonNotFoundException;
import no.fint.betaling.group.GroupRepository;
import no.fint.model.resource.Link;
import no.fint.model.resource.felles.PersonResource;
import no.fint.model.resource.okonomi.faktura.FakturamottakerResource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class PersonService {

    private final GroupRepository groupRepository;

    public PersonService(GroupRepository groupRepository) {
        this.groupRepository = groupRepository;
    }

    @Cacheable("persons")
    public PersonResource getPersonById(String id) {
        return groupRepository
                .getStudents()
                .values()
                .parallelStream()
                .filter(p -> StringUtils.equals(id, CustomerFactory.getCustomerId(p)))
                .findAny()
                .orElseThrow(() -> new PersonNotFoundException(id));
    }

    public FakturamottakerResource getFakturamottakerByPersonId(String id) {
        FakturamottakerResource mottaker = new FakturamottakerResource();
        mottaker.addPerson(getPersonLinkById(id));
        return mottaker;
    }

    public Link getPersonLinkById(String id) {
        return getPersonById(id)
                .getSelfLinks()
                .stream()
                .findFirst()
                .orElse(null);
    }

}
