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

    private SkoleResource getSchool(String orgId, String schoolId) {
        Map<String, SkoleResource> schools = cacheService.getCache("schoolCache", orgId);

        SkoleResource school = schools.get(schoolId);

        if (school == null) {
            log.error("x-school-org-id: {}", schoolId);
            throw new SchoolNotFoundException("No match for x-school-org-id: " + schoolId);
        }

        return school;
    }

    public CustomerGroup getCustomerGroupBySchool(String orgId, String schoolId) {
        SkoleResource school = getSchool(orgId, schoolId);

        return createCustomerGroup(orgId, school.getNavn(), null, school.getElevforhold());
    }

    public List<CustomerGroup> getCustomerGroupsByBasisGroupsAndSchool(String orgId, String schoolId) {
        SkoleResource school = getSchool(orgId, schoolId);

        Link schoolLink = getSelfLink(school);

        Map<Link, List<BasisgruppeResource>> resources = cacheService.getCache("basisGroupCache", orgId);

        List<BasisgruppeResource> basisGroups = resources.get(schoolLink);

        if (basisGroups == null) return Collections.emptyList();

        return basisGroups.stream()
                .map(b -> createCustomerGroup(orgId, b.getNavn(), b.getBeskrivelse(), b.getElevforhold()))
                .collect(Collectors.toList());
    }

    public List<CustomerGroup> getCustomerGroupsByTeachingGroupsAndSchool(String orgId, String schoolId) {
        SkoleResource school = getSchool(orgId, schoolId);

        Link schoolLink = getSelfLink(school);

        Map<Link, List<UndervisningsgruppeResource>> resources = cacheService.getCache("teachingGroupCache", orgId);

        List<UndervisningsgruppeResource> teachingGroups = resources.get(schoolLink);

        if (teachingGroups == null) return Collections.emptyList();

        return teachingGroups.stream()
                .map(t -> createCustomerGroup(orgId, t.getNavn(), t.getBeskrivelse(), t.getElevforhold()))
                .collect(Collectors.toList());
    }

    public List<CustomerGroup> getCustomerGroupsByContactTeacherGroupsAndSchool(String orgId, String schoolId) {
        SkoleResource school = getSchool(orgId, schoolId);

        Link schoolLink = getSelfLink(school);

        Map<Link, List<KontaktlarergruppeResource>> resources = cacheService.getCache("contactTeacherGroupCache", orgId);

        List<KontaktlarergruppeResource> contactTeacherGroups = resources.get(schoolLink);

        if (contactTeacherGroups == null) return Collections.emptyList();

        return contactTeacherGroups.stream()
                .map(c -> createCustomerGroup(orgId, c.getNavn(), c.getBeskrivelse(), c.getElevforhold()))
                .collect(Collectors.toList());
    }

    private CustomerGroup createCustomerGroup(String orgId, String name, String description, List<Link> studentRelations) {
        CustomerGroup customerGroup = new CustomerGroup();
        customerGroup.setName(name);
        customerGroup.setDescription(description);
        customerGroup.setCustomers(getCustomersForGroup(orgId, studentRelations));
        return customerGroup;
    }

    private List<Customer> getCustomersForGroup(String orgId, List<Link> studentRelationLinks) {
        Map<Link, ElevforholdResource> studentRelations = cacheService.getCache("studentRelationCache", orgId);
        Map<Link, PersonResource> students = cacheService.getCache("studentCache", orgId);

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