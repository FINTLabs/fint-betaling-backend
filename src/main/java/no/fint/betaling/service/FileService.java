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

    public CustomerFileGroup getCustomersFromFile(String schoolId, byte[] file) {
        InputStream targetStream = new ByteArrayInputStream(file);
        Workbook wb = null;
        try{
            wb = WorkbookFactory.create(targetStream);
        }catch (IOException ex){
            log.error(ex.getMessage(), ex);
            return null;
        }
        Sheet sheet = wb.getSheetAt(0);
        Row row;
        int rows;
        rows = sheet.getPhysicalNumberOfRows();
        int cols = 0;
        int tmp;

        for (int i = 0; i < 10 || i < rows; i++) {
            row = sheet.getRow(i);
            if (row != null) {
                tmp = sheet.getRow(i).getPhysicalNumberOfCells();
                if (tmp > cols) cols = tmp;
            }
        }
        return extractCustomerFileGroupFromFile(rows, sheet, cols, schoolId);
    }

    private CustomerFileGroup extractCustomerFileGroupFromFile(int rows, Sheet sheet, int cols, String schoolId) {
        CustomerFileGroup customerFileGroup = new CustomerFileGroup();
        CustomerGroup customerGroup = new CustomerGroup();
        ArrayList<Customer> customers = new ArrayList<>();
        ArrayList<String> notFoundCustomers = new ArrayList<>();
        Cell cell;
        int colWithVISID = -1;
        for (int r = 0; r < rows; r++) {
            Row row = sheet.getRow(r);
            String visId = "";
            if (row != null) {
                for (int c = 0; c < cols; c++) {
                    cell = row.getCell(c);
                    if (r == 0 && cell.toString().equals("VIS-ID")) {
                        colWithVISID = c;
                    }
                    if (cell != null && r > 0 && c == colWithVISID) {
                        visId = cell.toString();
                        if (visId.contains(".")) {
                            visId = visId.split("\\.")[0];
                        }
                    }
                }
            }
            if (visId.length() > 0) {
                Customer customer = getCustomerNumberByVisId(visId, schoolId);
                if (customer != null) {
                    customers.add(customer);
                } else {
                    notFoundCustomers.add(visId);
                }
            }
        }
        if (colWithVISID == -1){
            return customerFileGroup;
        }
        customerGroup.setCustomers(customers);
        customerFileGroup.setFoundCustomers(customerGroup);
        customerFileGroup.setNotFoundCustomers(notFoundCustomers);
        return customerFileGroup;
    }

    private Customer getCustomerNumberByVisId(String visId, String schoolId) {
        SkoleResource school = getSchool(schoolId);
        Map<Link, ElevforholdResource> studentRelations = fileRepository.getStudentRelations();
        Map<Link, PersonResource> students = fileRepository.getStudents();

        List<Customer> personResources = school.getElevforhold().stream()
                .map(studentRelations::get)
                .filter(Objects::nonNull)
                .filter(elevforholdResource -> elevforholdResource.getSystemId().getIdentifikatorverdi().equals(visId))
                .map(this::getStudentLink)
                .filter(Objects::nonNull)
                .map(students::get)
                .filter(Objects::nonNull)
                .map(CustomerFactory::toCustomer)
                .distinct()
                .collect(Collectors.toList());

        return personResources.size() > 0 ? personResources.get(0) : null;
    }
}