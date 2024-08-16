package no.fint.betaling.group;

import no.fint.betaling.fintdata.*;
import no.fint.betaling.model.Customer;
import no.fint.betaling.model.CustomerGroup;
import no.fint.betaling.organisation.CustomerFactory;
import no.fint.model.resource.Link;
import no.fint.model.resource.utdanning.timeplan.UndervisningsgruppeResource;
import no.fint.model.resource.utdanning.utdanningsprogram.SkoleResource;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class TeachingGroupService extends GroupService {

    private final SchoolRepository schoolRepository;

    private final TeachingGroupRepository teachingGroupRepository;

    private final TeachingGroupMembershipRepository teachingGroupMembershipRepository;

    private final StudentRepository studentRepository;

    private final StudentRelationRepository studentRelationRepository;

    public TeachingGroupService(SchoolRepository schoolRepository, TeachingGroupRepository teachingGroupRepository, TeachingGroupMembershipRepository teachingGroupMembershipRepository, StudentRepository studentRepository, StudentRelationRepository studentRelationRepository) {
        this.schoolRepository = schoolRepository;
        this.teachingGroupRepository = teachingGroupRepository;
        this.teachingGroupMembershipRepository = teachingGroupMembershipRepository;
        this.studentRepository = studentRepository;
        this.studentRelationRepository = studentRelationRepository;
    }

    public List<CustomerGroup> getFromTeachingGroups(String schoolId) {
        SkoleResource school = getSchool(schoolRepository, schoolId);

        return school.getUndervisningsgruppe().stream()
                .map(teachingGroupRepository::getResourceByLink)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(this::createCustomerGroup)
                .collect(Collectors.toList());
    }

    private CustomerGroup createCustomerGroup(UndervisningsgruppeResource group) {
        CustomerGroup customerGroup = new CustomerGroup();
        customerGroup.setName(group.getNavn());
        customerGroup.setDescription(group.getBeskrivelse());
        customerGroup.setCustomers(getCustomersForGroup(group.getGruppemedlemskap()));
        return customerGroup;
    }

    private List<Customer> getCustomersForGroup(List<Link> groupMembershipList) {

        if (groupMembershipList == null) {
            return Collections.emptyList();
        }
        final Date today = new Date();

        return groupMembershipList.stream()
                .map(teachingGroupMembershipRepository::getResourceByLink)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .filter(membership -> isActive(membership.getGyldighetsperiode(), today))
                .map(this::getElevforholdLink)
                .map(studentRelationRepository::getResourceByLink)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(this::getStudentLink)
                .filter(Objects::nonNull)
                .map(studentRepository::getResourceByLink)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(CustomerFactory::toCustomer)
                .distinct()
                .collect(Collectors.toList());
    }
}
