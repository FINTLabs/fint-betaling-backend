package no.fint.betaling.common.util;

import no.vigoiks.resourceserver.security.FintJwtDefaultConverter;

public class CustomUserConverter extends FintJwtDefaultConverter {
    public CustomUserConverter() {
        this.addMapping("organizationid", "ORGID_");
        this.addMapping("organizationnumber", "ORGNR_");
        this.addMapping("employeeId", "EMPLOYEE_");
        this.addMapping("roles", "ROLE_");
    }
}
