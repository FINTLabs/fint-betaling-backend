package no.fint.betaling.group;

import no.fint.betaling.fintdata.*;
import no.fint.betaling.model.Customer;
import no.fint.betaling.model.CustomerGroup;
import no.fint.betaling.organisation.CustomerFactory;
import no.fint.model.resource.Link;
import no.fint.model.resource.utdanning.elev.KlasseResource;
import no.fint.model.resource.utdanning.utdanningsprogram.SkoleResource;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class KlasseService extends GroupService {

    private final SchoolRepository schoolRepository;

    private final KlasseRepository klasseRepository;

    private final klassemedlemskapRepository klassemedlemskapRepository;

    private final StudentRepository studentRepository;

    private final StudentRelationRepository studentRelationRepository;

    public KlasseService(SchoolRepository schoolRepository, KlasseRepository klasseRepository, klassemedlemskapRepository klassemedlemskapRepository, StudentRepository studentRepository, StudentRelationRepository studentRelationRepository) {
        this.schoolRepository = schoolRepository;
        this.klasseRepository = klasseRepository;
        this.klassemedlemskapRepository = klassemedlemskapRepository;
        this.studentRepository = studentRepository;
        this.studentRelationRepository = studentRelationRepository;
    }

    public List<CustomerGroup> getFromKlasse(String schoolId) {
        SkoleResource school = getSchool(schoolRepository, schoolId);

        return school.getKlasse().stream()
                .map(klasseRepository::getResourceByLink)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(this::createCustomerGroup)
                .collect(Collectors.toList());
    }

    private CustomerGroup createCustomerGroup(KlasseResource klasse) {
        CustomerGroup customerGroup = new CustomerGroup();
        customerGroup.setName(klasse.getNavn());
        customerGroup.setDescription(klasse.getBeskrivelse());
        customerGroup.setCustomers(getCustomersForKlasse(klasse.getKlassemedlemskap()));
        return customerGroup;
    }

    private List<Customer> getCustomersForKlasse(List<Link> klassemedlemskapList) {
        if (klassemedlemskapList == null) {
            return Collections.emptyList();
        }
        final Date today = new Date();

        return klassemedlemskapList.stream()
                .map(klassemedlemskapRepository::getResourceByLink)
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
