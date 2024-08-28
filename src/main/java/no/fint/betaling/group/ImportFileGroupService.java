package no.fint.betaling.group;

import no.fint.betaling.fintdata.SchoolRepository;
import no.fint.betaling.fintdata.StudentRelationRepository;
import no.fint.betaling.fintdata.StudentRepository;
import no.fint.betaling.model.Customer;
import no.fint.betaling.organisation.CustomerFactory;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ImportFileGroupService extends GroupService {

    private final SchoolRepository schoolRepository;

    private final StudentRepository studentRepository;

    private final StudentRelationRepository studentRelationRepository;

    public ImportFileGroupService(SchoolRepository schoolRepository, StudentRelationRepository studentRelationRepository, StudentRepository studentRepository) {
        this.schoolRepository = schoolRepository;
        this.studentRelationRepository = studentRelationRepository;
        this.studentRepository = studentRepository;
    }

    public Map<String, Customer> getCustomersForSchoolWithVisIdKey(String schoolId) {
        return getSchool(schoolRepository, schoolId)
                .getElevforhold()
                .stream()
                .map(studentRelationRepository::getResourceByLink)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .filter(e -> e.getSystemId() != null && e.getSystemId().getIdentifikatorverdi() != null)
                .collect(Collectors.toMap(
                        e -> e.getSystemId().getIdentifikatorverdi(),
                        e -> e.getElev()
                                .stream()
                                .filter(Objects::nonNull)
                                .map(studentRepository::getResourceByLink)
                                .filter(Optional::isPresent)
                                .map(Optional::get)
                                .map(CustomerFactory::toCustomer)
                                .distinct()
                                .findFirst()
                                .orElseThrow(IllegalStateException::new)
                ));
    }
}
