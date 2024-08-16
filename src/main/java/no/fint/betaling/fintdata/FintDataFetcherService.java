package no.fint.betaling.fintdata;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class FintDataFetcherService {

    private final BasisGroupRepository basisGroupRepository;
    private final BasisGroupMembershipRepository basisGroupMembershipRepository;
    private final ContactTeacherGroupRepository contactTeacherGroupRepository;
    private final ContactTeacherGroupMembershipRepository contactTeacherGroupMembershipRepository;
    private final StudentRepository studentRepository;
    private final StudentRelationRepository studentRelationRepository;
    private final SchoolRepository schoolRepository;
    private final SchoolResourceRepository schoolResourceRepository;
    private final TeachingGroupRepository teachingGroupRepository;
    private final TeachingGroupMembershipRepository teachingGroupMembershipRepository;
    private final TaxCodeRepository taxCodeRepository;
    private final InvoiceIssuerRepository invoiceIssuerRepository;
    private final LineItemRepository lineItemRepository;

    public FintDataFetcherService(BasisGroupRepository basisGroupRepository,
                                  BasisGroupMembershipRepository basisGroupMembershipRepository,
                                  ContactTeacherGroupRepository contactTeacherGroupRepository,
                                  ContactTeacherGroupMembershipRepository contactTeacherGroupMembershipRepository,
                                  StudentRepository studentRepository,
                                  StudentRelationRepository studentRelationRepository,
                                  SchoolRepository schoolRepository,
                                  SchoolResourceRepository schoolResourceRepository,
                                  TeachingGroupRepository teachingGroupRepository,
                                  TeachingGroupMembershipRepository teachingGroupMembershipRepository,
                                  TaxCodeRepository taxCodeRepository,
                                  InvoiceIssuerRepository invoiceIssuerRepository,
                                  LineItemRepository lineItemRepository) {
        this.basisGroupRepository = basisGroupRepository;
        this.basisGroupMembershipRepository = basisGroupMembershipRepository;
        this.contactTeacherGroupRepository = contactTeacherGroupRepository;
        this.contactTeacherGroupMembershipRepository = contactTeacherGroupMembershipRepository;
        this.studentRepository = studentRepository;
        this.studentRelationRepository = studentRelationRepository;
        this.schoolRepository = schoolRepository;
        this.schoolResourceRepository = schoolResourceRepository;
        this.teachingGroupRepository = teachingGroupRepository;
        this.teachingGroupMembershipRepository = teachingGroupMembershipRepository;
        this.taxCodeRepository = taxCodeRepository;
        this.invoiceIssuerRepository = invoiceIssuerRepository;
        this.lineItemRepository = lineItemRepository;
    }

    @Scheduled(initialDelay = 1000L, fixedDelayString = "${fint.betaling.refresh-rate:3600000}")
    public void updateAll() {
        int basisGroupsUpdated = basisGroupRepository.update();
        int basisGroupMembershipsUpdated = basisGroupMembershipRepository.update();
        int contactTeacherGroupsUpdated = contactTeacherGroupRepository.update();
        int contactTeacherGroupMembershipsUpdated = contactTeacherGroupMembershipRepository.update();
        int studentRelationsUpdated = studentRelationRepository.update();
        int studentsUpdated = studentRepository.update();
        int schoolsUpdated = schoolRepository.update();
        int schoolResourcesUpdated = schoolResourceRepository.update();
        int teachingGroupsUpdated = teachingGroupRepository.update();
        int teachingGroupMembershipsUpdated = teachingGroupMembershipRepository.update();
        int taxCodesUpdated = taxCodeRepository.update();
        int lineItemsUpdated = lineItemRepository.update();
        int invoiceIssuersUpdated = invoiceIssuerRepository.update();
        log.info("Oppdateringer fullført: Basisgrupper ({}), Basisgruppemedlemskap ({}) Kontaktlærergrupper ({}), Kontaktlærergruppemedlemskap ({}), Elevforhold ({}), Elever ({}), Skoler ({}), Skoleresurser ({}), Undervisningsgrupper ({}), Undervisningsgruppemedlemskap ({}), Skattekoder ({}), Fakturautstedere ({}), Varelinjer ({})", basisGroupsUpdated, basisGroupMembershipsUpdated, contactTeacherGroupsUpdated, contactTeacherGroupMembershipsUpdated, studentRelationsUpdated, studentsUpdated, schoolsUpdated, schoolResourcesUpdated, teachingGroupsUpdated, teachingGroupMembershipsUpdated, taxCodesUpdated, invoiceIssuersUpdated, lineItemsUpdated);
    }
}
