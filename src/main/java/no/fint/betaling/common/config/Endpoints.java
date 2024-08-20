package no.fint.betaling.common.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
public class Endpoints {

    @Value("${fint.betaling.endpoints.principal:/okonomi/faktura/fakturautsteder}")
    private String invoiceIssuer;

    @Value("${fint.betaling.endpoints.employee:/administrasjon/personal/personalressurs}")
    private String employee;

    @Value("${fint.betaling.endpoints.school:/utdanning/utdanningsprogram/skole}")
    private String school;

    @Value("${fint.betaling.endpoints.basis-group:/utdanning/elev/basisgruppe}")
    private String basisGroup;

    @Value("${fint.betaling.endpoints.basis-group-membership:/utdanning/elev/basisgruppemedlemskap}")
    private String basisGroupMembership;

    @Value("${fint.betaling.endpoints.teaching-group:/utdanning/timeplan/undervisningsgruppe}")
    private String teachingGroup;

    @Value("${fint.betaling.endpoints.teaching-group-membership:/utdanning/timeplan/undervisningsgruppemedlemskap}")
    private String teachingGroupMembership;

    @Value("${fint.betaling.endpoints.contact-teacher-group:/utdanning/elev/kontaktlarergruppe}")
    private String contactTeacherGroup;

    @Value("${fint.betaling.endpoints.contact-teacher-group-membership:/utdanning/elev/kontaktlarergruppemedlemskap}")
    private String contactTeacherGroupMembership;

    @Value("${fint.betaling.endpoints.student-relation:/utdanning/elev/elevforhold}")
    private String studentRelation;

    @Value("${fint.betaling.endpoints.person:/utdanning/elev/person}")
    private String person;

    @Value("${fint.betaling.endpoints.organisation:/administrasjon/organisasjon/organisasjonselement}")
    private String organisationselement;

    @Value("${fint.betaling.endpoints.lineitem:/okonomi/kodeverk/vare}")
    private String vare;

    @Value("${fint.betaling.endpoints.mva-code:/okonomi/kodeverk/merverdiavgift}")
    private String taxCode;

    @Value("${fint.betaling.endpoints.skoleressurs:/utdanning/elev/skoleressurs}")
    private String schoolResource;
}
