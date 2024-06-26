package no.fint.betaling.common.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

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

    @Value("${fint.betaling.endpoints.teaching-group:/utdanning/timeplan/undervisningsgruppe}")
    private String teachingGroup;

    @Value("${fint.betaling.endpoints.contact-teacher-group:/utdanning/elev/kontaktlarergruppe}")
    private String contactTeacherGroup;

    @Value("${fint.betaling.endpoints.student-relation:/utdanning/elev/elevforhold}")
    private String studentRelation;

    @Value("${fint.betaling.endpoints.person:/utdanning/elev/person}")
    private String person;

    @Value("${fint.betaling.endpoints.organisation:/administrasjon/organisasjon/organisasjonselement}")
    private String organisationselement;

    @Value("${fint.betaling.endpoints.lineitem:/okonomi/kodeverk/vare}")
    private String vare;

    @Value("${fint.betaling.endpoints.mva-code:/okonomi/kodeverk/merverdiavgift}")
    private String taxcode;

    @Value("${fint.betaling.endpoints.skoleressurs:/utdanning/elev/skoleressurs}")
    private String schoolResource;

    public Map<String, String> getAllEndpoints() {
        Map<String, String> endpointsMap = new HashMap<>();
        endpointsMap.put("invoiceIssuer", invoiceIssuer);
        endpointsMap.put("employee", employee);
        endpointsMap.put("school", school);
        endpointsMap.put("basisGroup", basisGroup);
        endpointsMap.put("teachingGroup", teachingGroup);
        endpointsMap.put("contactTeacherGroup", contactTeacherGroup);
        endpointsMap.put("studentRelation", studentRelation);
        endpointsMap.put("person", person);
        endpointsMap.put("organisationselement", organisationselement);
        endpointsMap.put("vare", vare);
        endpointsMap.put("taxcode", taxcode);
        endpointsMap.put("schoolResource", schoolResource);
        return endpointsMap;
    }

}
