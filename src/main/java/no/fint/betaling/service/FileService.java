package no.fint.betaling.service;

import com.sun.xml.internal.ws.server.UnsupportedMediaException;
import lombok.extern.slf4j.Slf4j;
import no.fint.betaling.exception.InsufficientDataException;
import no.fint.betaling.exception.NoVISIDColumnException;
import no.fint.betaling.exception.UnableToReadFileException;
import no.fint.betaling.model.Customer;
import no.fint.betaling.model.CustomerGroup;
import no.fint.betaling.util.CustomerFileGroup;
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
import java.util.ArrayList;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.IntStream;

@Slf4j
@Service
public class FileService {

    @Value("${fint.betaling.dnd.file-types:application/vnd.openxmlformats-officedocument.spreadsheetml.sheet,application/vnd.ms-excel}")
    private String[] fileTypes;
    @Value("${fint.betaling.dnd.VIS-ID:VIS-ID}")
    private String visId;

    public Sheet getSheetFromBytes(byte[] file) throws UnableToReadFileException {
        String contentType = new Tika().detect(file);
        if (!isTypeOfTypeExcel(contentType)) {
            throw new UnsupportedMediaException(contentType);
        }
        InputStream targetStream = new ByteArrayInputStream(file);
        Workbook wb;
        try {
            wb = WorkbookFactory.create(targetStream);
        } catch (IOException ex) {
            log.info(ex.getMessage(), ex);
            throw new UnableToReadFileException(ex.getMessage());
        }
        return wb.getSheetAt(0);
    }

    public CustomerFileGroup extractCustomerFileGroupFromSheet(Sheet sheet, Map<String, Customer> customersInput) throws NoVISIDColumnException, InsufficientDataException {
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
                        getCustomerNumberByVisId(s, customersInput)
                                .map(customers::add)
                                .orElseGet(() -> notFoundCustomers.add(s))
                );

        customerGroup.setCustomers(customers);
        CustomerFileGroup customerFileGroup = new CustomerFileGroup(customerGroup, notFoundCustomers);
        if (!customerFileGroup.hasCustomers()){
            throw new InsufficientDataException();
        }
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

    private Optional<Customer> getCustomerNumberByVisId(String visId, Map<String,Customer> customers) {
        return Optional.ofNullable(customers.get(visId));
    }

    private boolean isTypeOfTypeExcel(String contentType) {
        return StringUtils.equalsAny(contentType, fileTypes);
    }
}