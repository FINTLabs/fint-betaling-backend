package no.fint.betaling.service;

import lombok.extern.slf4j.Slf4j;
import no.fint.betaling.exception.SchoolNotFoundException;
import no.fint.betaling.factory.CustomerFactory;
import no.fint.betaling.model.Customer;
import no.fint.betaling.model.CustomerGroup;
import no.fint.model.resource.FintLinks;
import no.fint.model.resource.Link;
import no.fint.model.resource.felles.PersonResource;
import no.fint.model.resource.utdanning.elev.*;
import no.fint.model.resource.utdanning.timeplan.UndervisningsgruppeResource;
import no.fint.model.resource.utdanning.utdanningsprogram.SkoleResource;
import no.fint.model.utdanning.basisklasser.Gruppe;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@SuppressWarnings("unchecked")
public class GroupService {

    private final CacheService cacheService;

    public GroupService(CacheService cacheService) {
        this.cacheService = cacheService;
    }

    private SkoleResource getSchool(String schoolId) {
        Map<String, SkoleResource> schools = cacheService.getResources("schools");

        SkoleResource school = schools.get(schoolId);

        if (school == null) {
            log.error("x-school-org-id: {}", schoolId);
            throw new SchoolNotFoundException("x-school-org-id: " + schoolId);
        }

        return school;
    }

    public CustomerGroup getCustomerGroupBySchool(String schoolId) {
        SkoleResource school = getSchool(schoolId);

        return createCustomerGroup(school);
    }

    public List<CustomerGroup> getCustomerGroupsByBasisGroupsAndSchool(String schoolId) {
        SkoleResource school = getSchool(schoolId);

        Link schoolLink = getSelfLink(school);

        Map<Link, List<BasisgruppeResource>> resources = cacheService.getResources("basisGroups");

        List<BasisgruppeResource> basisGroups = resources.get(schoolLink);

        if (basisGroups == null) return Collections.emptyList();

        return basisGroups.stream().map(this::createCustomerGroup).collect(Collectors.toList());
    }

    public List<CustomerGroup> getCustomerGroupsByTeachingGroupsAndSchool(String schoolId) {
        SkoleResource school = getSchool(schoolId);

        Link schoolLink = getSelfLink(school);

        Map<Link, List<UndervisningsgruppeResource>> resources = cacheService.getResources("teachingGroups");

        List<UndervisningsgruppeResource> teachingGroups = resources.get(schoolLink);

        if (teachingGroups == null) return Collections.emptyList();

        return teachingGroups.stream().map(this::createCustomerGroup).collect(Collectors.toList());
    }

    public List<CustomerGroup> getCustomerGroupsByContactTeacherGroupsAndSchool(String schoolId) {
        SkoleResource school = getSchool(schoolId);

        Link schoolLink = getSelfLink(school);

        Map<Link, List<KontaktlarergruppeResource>> resources = cacheService.getResources("contactTeacherGroups");

        List<KontaktlarergruppeResource> contactTeacherGroups = resources.get(schoolLink);

        if (contactTeacherGroups == null) return Collections.emptyList();

        return contactTeacherGroups.stream().map(this::createCustomerGroup).collect(Collectors.toList());
    }

    private<T extends Gruppe & FintLinks> CustomerGroup createCustomerGroup(T group) {
        CustomerGroup customerGroup = new CustomerGroup();
        customerGroup.setName(group.getNavn());
        customerGroup.setDescription(group.getBeskrivelse());
        customerGroup.setCustomers(getCustomersForGroup(group.getLinks().get("elevforhold")));
        return customerGroup;
    }

    private CustomerGroup createCustomerGroup(SkoleResource school) {
        CustomerGroup customerGroup = new CustomerGroup();
        customerGroup.setName(school.getNavn());
        customerGroup.setDescription(null);
        customerGroup.setCustomers(getCustomersForGroup(school.getElevforhold()));
        return customerGroup;
    }

    private List<Customer> getCustomersForGroup(List<Link> studentRelationLinks) {
        Map<Link, ElevforholdResource> studentRelations = cacheService.getResources("studentRelations");
        Map<Link, PersonResource> students = cacheService.getResources("students");

        return studentRelationLinks.stream()
                .map(studentRelations::get)
                .filter(Objects::nonNull)
                .map(this::getStudentLink)
                .filter(Objects::nonNull)
                .map(students::get)
                .filter(Objects::nonNull)
                .map(CustomerFactory::toCustomer)
                .collect(Collectors.toList());
    }

    private <T extends FintLinks> Link getSelfLink(T resource) {
        return resource.getSelfLinks().stream().findAny().orElse(null);
    }

    private Link getStudentLink(ElevforholdResource resource) {
        return resource.getElev().stream().findAny().orElse(null);
    }
}