package no.fint.betaling.group;

import no.fint.betaling.fintdata.SchoolRepository;
import no.fint.betaling.fintdata.StudentRelationRepository;
import no.fint.betaling.fintdata.StudentRepository;
import no.fint.betaling.model.Customer;
import no.fint.betaling.model.CustomerGroup;
import no.fint.betaling.organisation.CustomerFactory;
import no.fint.model.resource.Link;
import no.fint.model.resource.utdanning.utdanningsprogram.SkoleResource;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class SchoolGroupService extends GroupService {

    private final SchoolRepository schoolRepository;

    private final StudentRepository studentRepository;

    private final StudentRelationRepository studentRelationRepository;

    public SchoolGroupService(SchoolRepository schoolRepository, StudentRepository studentRepository, StudentRelationRepository studentRelationRepository) {
        this.schoolRepository = schoolRepository;
        this.studentRepository = studentRepository;
        this.studentRelationRepository = studentRelationRepository;
    }

    public CustomerGroup getFromSchool(String schoolId) {
        SkoleResource school = getSchool(schoolRepository, schoolId);

        return createCustomerGroup(school);
    }

    private CustomerGroup createCustomerGroup(SkoleResource school) {
        CustomerGroup customerGroup = new CustomerGroup();
        customerGroup.setName(school.getNavn());
        customerGroup.setDescription(null);
        customerGroup.setCustomers(getCustomersFromElevforhold(school));
        return customerGroup;
    }

    private List<Customer> getCustomersFromElevforhold(SkoleResource school) {
        List<Link> studentRelationLinks = school.getElevforhold();

        if (studentRelationLinks == null) {
            return Collections.emptyList();
        }

        final Date today = new Date();

        return studentRelationLinks.stream()
                .map(studentRelationRepository::getResourceByLink)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .filter(studentRelation -> isActive(studentRelation.getGyldighetsperiode(), today))
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
