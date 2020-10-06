package no.fint.betaling.service;

import lombok.extern.slf4j.Slf4j;
import no.fint.betaling.exception.NoVISIDColumnException;
import no.fint.betaling.exception.SchoolNotFoundException;
import no.fint.betaling.exception.UnableToReadFileException;
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
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Slf4j
@Service
public class FileService {

    @Value("${fint.betaling.dnd.file-types:application/vnd.openxmlformats-officedocument.spreadsheetml.sheet,application/vnd.ms-excel}")
    private String[] fileTypes;
    @Value("${fint.betaling.dnd.VIS-ID:VIS-ID}")
    private String visId;

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

    public CustomerFileGroup getCustomersFromFile(String schoolId, byte[] file) throws UnableToReadFileException, NoVISIDColumnException {
        InputStream targetStream = new ByteArrayInputStream(file);
        Workbook wb;
        try {
            wb = WorkbookFactory.create(targetStream);
        } catch (IOException ex) {
            log.error(ex.getMessage(), ex);
            throw new UnableToReadFileException(ex.getMessage());
        }
        Sheet sheet = wb.getSheetAt(0);
        return extractCustomerFileGroupFromFile(sheet, schoolId);
    }

    private CustomerFileGroup extractCustomerFileGroupFromFile(Sheet sheet, String schoolId) throws NoVISIDColumnException {
        CustomerFileGroup customerFileGroup = new CustomerFileGroup();
        CustomerGroup customerGroup = new CustomerGroup();
        ArrayList<Customer> customers = new ArrayList<>();
        ArrayList<String> notFoundCustomers = new ArrayList<>();

        Cell visIdCell = getVisIdCell(sheet);

        IntStream
                .range(visIdCell.getRowIndex()+1, sheet.getPhysicalNumberOfRows())
                .mapToObj(sheet::getRow)
                .filter(Objects::nonNull)
                .map(row -> row.getCell(visIdCell.getColumnIndex()))
                .filter(Objects::nonNull)
                .map(cell1 -> String.format("%.0f", cell1.getNumericCellValue()))
                .forEach(s ->
                    getCustomerNumberByVisId(s, schoolId)
                            .map(customers::add)
                            .orElseGet(() -> notFoundCustomers.add(s))
                );

        customerGroup.setCustomers(customers);
        customerFileGroup.setFoundCustomers(customerGroup);
        customerFileGroup.setNotFoundCustomers(notFoundCustomers);
        return customerFileGroup;
    }

    private Cell getVisIdCell(Sheet sheet) throws NoVISIDColumnException {
        return IntStream
                    .range(0, sheet.getPhysicalNumberOfRows())
                    .mapToObj(sheet::getRow)
                    .filter(Objects::nonNull)
                    .flatMap(row ->
                            IntStream
                                    .range(0, row.getPhysicalNumberOfCells())
                                    .mapToObj(row::getCell)
                                    .filter(Objects::nonNull)
                                    .filter(cell1 -> cell1.getStringCellValue().equals(visId)))
                    .findFirst()
                    .orElseThrow(NoVISIDColumnException::new);
    }

    private Optional<Customer> getCustomerNumberByVisId(String visId, String schoolId) {
        SkoleResource school = getSchool(schoolId);
        Map<Link, ElevforholdResource> studentRelations = fileRepository.getStudentRelations();
        Map<Link, PersonResource> students = fileRepository.getStudents();

        return school.getElevforhold().stream()
                .map(studentRelations::get)
                .filter(Objects::nonNull)
                .filter(elevforholdResource -> elevforholdResource.getSystemId().getIdentifikatorverdi().equals(visId))
                .map(this::getStudentLink)
                .filter(Objects::nonNull)
                .map(students::get)
                .filter(Objects::nonNull)
                .map(CustomerFactory::toCustomer)
                .distinct()
                .findFirst();
    }

    public boolean isTypeOfTypeExcel(String contentType) {
        return StringUtils.equalsAny(contentType, fileTypes);
    }
}