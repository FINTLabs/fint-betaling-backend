package no.fint.betaling.config;

import lombok.Data;

@Data
public class OrganisationConfig {
    private String orgId;
    private Long nextOrderNumberForOrganisation;
}