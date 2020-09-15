package no.fint.betaling.service;

import lombok.extern.slf4j.Slf4j;
import no.fint.betaling.exception.SchoolNotFoundException;
import no.fint.betaling.factory.CustomerFactory;
import no.fint.betaling.model.Customer;
import no.fint.betaling.model.CustomerGroup;
import no.fint.betaling.repository.FileRepository;
import no.fint.model.felles.kompleksedatatyper.Identifikator;
import no.fint.model.resource.Link;
import no.fint.model.resource.felles.PersonResource;
import no.fint.model.resource.utdanning.elev.ElevforholdResource;
import no.fint.model.resource.utdanning.utdanningsprogram.SkoleResource;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Slf4j
@Service
public class FileService {

    private final FileRepository fileRepository;

    public FileService(FileRepository fileRepository) {
        this.fileRepository = fileRepository;
    }

    public CustomerGroup getCustomers(String schoolId, ArrayList<String> customerNumbers) {
        Map<Link, PersonResource> students = fileRepository.getStudents();
        SkoleResource school = getSchool(schoolId);
        CustomerGroup customerGroup = createCustomerGroup(school, students, customerNumbers);
        List<Customer> customerList = customerGroup.getCustomers().stream().filter(customer -> studentMatchStudentNumber(customer, customerNumbers)).collect(Collectors.toList());
        customerGroup.setCustomers(customerList);
        return customerGroup;
    }

    private boolean studentMatchStudentNumber(Customer customer, ArrayList<String> customerNumbers) {
        return customerNumbers.contains(customer.getId());
    }

    private CustomerGroup createCustomerGroup(SkoleResource school, Map<Link, PersonResource> students, ArrayList<String> customerNumbers) {
        CustomerGroup customerGroup = new CustomerGroup();
        customerGroup.setName(school.getNavn());
        customerGroup.setDescription(null);
        customerGroup.setCustomers(getCustomersForGroup(school.getElevforhold()));
        return customerGroup;
    }

    private SkoleResource getSchool(String schoolId) {
        return fileRepository.getSchools().values().stream()
                .filter(hasOrganisationNumber(schoolId))
                .findFirst()
                .orElseThrow(() -> new SchoolNotFoundException(String.format("x-school-org-id: %s", schoolId)));
    }

    private Predicate<SkoleResource> hasOrganisationNumber(String schoolId) {
        return s -> Optional.ofNullable(s.getOrganisasjonsnummer())
                .map(Identifikator::getIdentifikatorverdi)
                .map(schoolId::equals)
                .orElse(false);
    }

    private List<Customer> getCustomersForGroup(List<Link> studentRelationLinks) {
        if (studentRelationLinks == null) {
            return Collections.emptyList();
        }
        Map<Link, ElevforholdResource> studentRelations = fileRepository.getStudentRelations();
        Map<Link, PersonResource> students = fileRepository.getStudents();

        return studentRelationLinks.stream()
                .map(studentRelations::get)
                .filter(Objects::nonNull)
                .map(this::getStudentLink)
                .filter(Objects::nonNull)
                .map(students::get)
                .filter(Objects::nonNull)
                .map(CustomerFactory::toCustomer)
                .distinct()
                .collect(Collectors.toList());
    }

    private Link getStudentLink(ElevforholdResource resource) {
        return resource.getElev().stream().findFirst().orElse(null);
    }

    public CustomerGroup getCustomersFromFile(String schoolId, byte[] file) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(file)));
        ArrayList<String> customerNumbers = new ArrayList<>();
        String row;
        while ((row = reader.readLine()) != null) {
            String[] data = row.split(",");
            customerNumbers.addAll(Arrays.asList(data));
        }
        reader.close();
        return getCustomers(schoolId, customerNumbers);
    }
}