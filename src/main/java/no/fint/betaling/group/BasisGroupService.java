package no.fint.betaling.group;

import no.fint.betaling.fintdata.*;
import no.fint.betaling.model.Customer;
import no.fint.betaling.model.CustomerGroup;
import no.fint.betaling.organisation.CustomerFactory;
import no.fint.model.resource.Link;
import no.fint.model.resource.utdanning.elev.BasisgruppeResource;
import no.fint.model.resource.utdanning.utdanningsprogram.SkoleResource;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class BasisGroupService extends GroupService {

    private final SchoolRepository schoolRepository;

    private final BasisGroupRepository basisGroupRepository;

    private final BasisGroupMembershipRepository basisGroupMembershipRepository;

    private final StudentRepository studentRepository;

    private final StudentRelationRepository studentRelationRepository;

    public BasisGroupService(SchoolRepository schoolRepository, BasisGroupRepository basisGroupRepository, BasisGroupMembershipRepository basisGroupMembershipRepository, StudentRepository studentRepository, StudentRelationRepository studentRelationRepository) {
        this.schoolRepository = schoolRepository;
        this.basisGroupRepository = basisGroupRepository;
        this.basisGroupMembershipRepository = basisGroupMembershipRepository;
        this.studentRepository = studentRepository;
        this.studentRelationRepository = studentRelationRepository;
    }

    public List<CustomerGroup> getFromBasisGroups(String schoolId) {
        SkoleResource school = getSchool(schoolRepository, schoolId);

        return school.getBasisgruppe().stream()
                .map(basisGroupRepository::getResourceByLink)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(this::createCustomerGroup)
                .collect(Collectors.toList());
    }

    private CustomerGroup createCustomerGroup(BasisgruppeResource group) {
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
                .map(basisGroupMembershipRepository::getResourceByLink)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .filter(membeship -> isActive(membeship.getGyldighetsperiode(), today))
                .map(this::getElevforholdLink)
                .filter(Objects::nonNull)
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
