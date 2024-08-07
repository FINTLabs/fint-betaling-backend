package no.fint.betaling.fintdata;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class FintDataFetcherService {

    private final BasisGroupRepository basisGroupRepository;
    private final ContactTeacherGroupRepository contactTeacherGroupRepository;
    private final StudentRepository studentRepository;
    private final StudentRelationRepository studentRelationRepository;
    private final SchoolRepository schoolRepository;
    private final SchoolResourceRepository schoolResourceRepository;
    private final TaxCodeRepository taxCodeRepository;
    private final InvoiceIssuerRepository invoiceIssuerRepository;
    private final LineItemRepository lineItemRepository;

    public FintDataFetcherService(BasisGroupRepository basisGroupRepository,
                                  ContactTeacherGroupRepository contactTeacherGroupRepository,
                                  StudentRepository studentRepository,
                                  StudentRelationRepository studentRelationRepository,
                                  SchoolRepository schoolRepository,
                                  SchoolResourceRepository schoolResourceRepository,
                                  TaxCodeRepository taxCodeRepository,
                                  InvoiceIssuerRepository invoiceIssuerRepository,
                                  LineItemRepository lineItemRepository) {
        this.basisGroupRepository = basisGroupRepository;
        this.contactTeacherGroupRepository = contactTeacherGroupRepository;
        this.studentRepository = studentRepository;
        this.studentRelationRepository = studentRelationRepository;
        this.schoolRepository = schoolRepository;
        this.schoolResourceRepository = schoolResourceRepository;
        this.taxCodeRepository = taxCodeRepository;
        this.invoiceIssuerRepository = invoiceIssuerRepository;
        this.lineItemRepository = lineItemRepository;
    }

    @Scheduled(initialDelay = 1000L, fixedDelayString = "${fint.betaling.refresh-rate:3600000}")
    public void updateAll() {
        int basisGroupsUpdated = basisGroupRepository.update();
        int contactTeacherGroupsUpdated = contactTeacherGroupRepository.update();
        int studentRelationsUpdated = studentRelationRepository.update();
        int studentsUpdated = studentRepository.update();
        int schoolsUpdated = schoolRepository.update();
        int schoolResourcesUpdated = schoolResourceRepository.update();
        int taxCodesUpdated = taxCodeRepository.update();
        int lineItemsUpdated = lineItemRepository.update();
        int invoiceIssuersUpdated = invoiceIssuerRepository.update();
        log.info("Oppdateringer fullført: Basisgrupper ({}), Kontaktlærergrupper ({}), Elevforhold ({}), Elever ({}), Skoler ({}), Skoleresurser ({}), Skattekoder ({}), Fakturautstedere ({}), Varelinjer ({})", basisGroupsUpdated, contactTeacherGroupsUpdated, studentRelationsUpdated, studentsUpdated, schoolsUpdated, schoolResourcesUpdated, taxCodesUpdated, invoiceIssuersUpdated, lineItemsUpdated);
    }
}
