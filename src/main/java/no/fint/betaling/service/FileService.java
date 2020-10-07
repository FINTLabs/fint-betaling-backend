package no.fint.betaling.service;

import com.sun.xml.internal.ws.server.UnsupportedMediaException;
import lombok.extern.slf4j.Slf4j;
import no.fint.betaling.exception.NoVISIDColumnException;
import no.fint.betaling.exception.SchoolNotFoundException;
import no.fint.betaling.exception.UnableToReadFileException;
import no.fint.betaling.factory.CustomerFactory;
import no.fint.betaling.model.Customer;
import no.fint.betaling.model.CustomerGroup;
import no.fint.betaling.repository.GroupRepository;
import no.fint.betaling.util.CustomerFileGroup;
import no.fint.model.felles.kompleksedatatyper.Identifikator;
import no.fint.model.resource.Link;
import no.fint.model.resource.felles.PersonResource;
import no.fint.model.resource.utdanning.elev.ElevforholdResource;
import no.fint.model.resource.utdanning.utdanningsprogram.SkoleResource;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.tika.Tika;
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

    private final GroupRepository groupRepository;

    public FileService(GroupRepository groupRepository) {
        this.groupRepository = groupRepository;
    }

    private SkoleResource getSchool(String schoolId) {
        return groupRepository.getSchools().values().stream()
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

    private Link getStudentLink(ElevforholdResource resource) {
        return resource.getElev().stream().findFirst().orElse(null);
    }

    public CustomerFileGroup getCustomersFromFile(String schoolId, byte[] file) throws UnableToReadFileException, NoVISIDColumnException {
        if(!isTypeOfTypeExcel(new Tika().detect(file))){
            throw new UnsupportedMediaException(new Tika().detect(file));
        }
        InputStream targetStream = new ByteArrayInputStream(file);
        Workbook wb;
        try {
            wb = WorkbookFactory.create(targetStream);
        } catch (IOException ex) {
            log.info(ex.getMessage(), ex);
            throw new UnableToReadFileException(ex.getMessage());
        }
        Sheet sheet = wb.getSheetAt(0);
        return extractCustomerFileGroupFromFile(sheet, schoolId);
    }

    private CustomerFileGroup extractCustomerFileGroupFromFile(Sheet sheet, String schoolId) throws NoVISIDColumnException {
        CustomerGroup customerGroup = new CustomerGroup();
        ArrayList<Customer> customers = new ArrayList<>();
        ArrayList<String> notFoundCustomers = new ArrayList<>();

        Cell visIdCell = getVisIdCell(sheet);

        IntStream
                .range(visIdCell.getRowIndex() + 1, sheet.getPhysicalNumberOfRows())
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
        return new CustomerFileGroup(customerGroup, notFoundCustomers);
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
        Map<Link, ElevforholdResource> studentRelations = groupRepository.getStudentRelations();
        Map<Link, PersonResource> students = groupRepository.getStudents();

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