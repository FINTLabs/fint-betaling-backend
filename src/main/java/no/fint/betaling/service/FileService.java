package no.fint.betaling.service;

import lombok.extern.slf4j.Slf4j;
import no.fint.betaling.exception.SchoolNotFoundException;
import no.fint.betaling.factory.CustomerFactory;
import no.fint.betaling.model.Customer;
import no.fint.betaling.model.CustomerGroup;
import no.fint.betaling.repository.FileRepository;
import no.fint.betaling.util.CustomerFileGroup;
import no.fint.model.felles.kompleksedatatyper.Identifikator;
import no.fint.model.resource.Link;
import no.fint.model.resource.felles.PersonResource;
import no.fint.model.resource.utdanning.elev.ElevforholdResource;
import no.fint.model.resource.utdanning.utdanningsprogram.SkoleResource;
import org.apache.poi.ss.usermodel.*;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
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
        CustomerGroup customerGroup = createCustomerGroup(school);
        List<Customer> customerList = customerGroup.getCustomers().stream().filter(customer -> studentMatchStudentNumber(customer, customerNumbers)).collect(Collectors.toList());
        customerGroup.setCustomers(customerList);
        return customerGroup;
    }

    private boolean studentMatchStudentNumber(Customer customer, ArrayList<String> customerNumbers) {
        return customerNumbers.contains(customer.getId());
    }

    private CustomerGroup createCustomerGroup(SkoleResource school) {
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

    public CustomerFileGroup getCustomersFromFile(String schoolId, byte[] file) throws IOException {
        CustomerFileGroup customerFileGroup = new CustomerFileGroup();
        ArrayList<String> customerNumbers = new ArrayList<>();
        InputStream targetStream = new ByteArrayInputStream(file);
        ArrayList<String> notFoundCustomers = new ArrayList<>();

        Workbook wb = WorkbookFactory.create(targetStream);

        Sheet sheet = wb.getSheetAt(0);
        Row row;
        Cell cell;
        int rows;
        rows = sheet.getPhysicalNumberOfRows();

        int cols = 0;
        int tmp = 0;

        for (int i = 0; i < 10 || i < rows; i++) {
            row = sheet.getRow(i);
            if (row != null) {
                tmp = sheet.getRow(i).getPhysicalNumberOfCells();
                if (tmp > cols) cols = tmp;
            }
        }

        for (int r = 0; r < rows; r++) {
            row = sheet.getRow(r);
            if (row != null) {
                String firstName = "";
                String lastName = "";
                String birthDate = "";
                for (int c = 0; c < cols; c++) {
                    cell = row.getCell(c);
                    if (cell != null && r > 0) {
                        if (c == 0) {
                            firstName = cell.toString();
                        }
                        if (c == 1) {
                            lastName = cell.toString();
                        }
                        if (c == 3) {
                            birthDate = cell.toString();
                        }
                    }
                }
                if (firstName.length() > 0 && lastName.length() > 0) {
                    String customerNumberByNameAndDate = getCustomerNumberByNameAndDate(firstName, lastName, birthDate, schoolId);
                    if (customerNumberByNameAndDate != null) {
                        customerNumbers.add(customerNumberByNameAndDate);
                    } else notFoundCustomers.add(lastName + ", " + firstName);
                }
            }
        }
        customerFileGroup.setFoundCustomers(getCustomers(schoolId, customerNumbers));
        customerFileGroup.setNotFoundCustomers(notFoundCustomers);
        return customerFileGroup;
    }

    public String getCustomerNumberByNameAndDate(String firstname, String lastName, String birthDate, String schoolId) {
        SkoleResource school = getSchool(schoolId);
        CustomerGroup customerGroup = createCustomerGroup(school);
        List<Customer> customerList = customerGroup.getCustomers().stream().filter(customer -> studentMatchNameAndBirthDate(firstname, lastName, birthDate, customer)).collect(Collectors.toList());
        return customerList.size() > 0 ? customerList.get(0).getId() : null;
    }

    private boolean studentMatchNameAndBirthDate(String firstname, String lastName, String birthDate, Customer customer) {
        String name = lastName + ", " + firstname;
        return customer.getName().equals(name);
    }
}