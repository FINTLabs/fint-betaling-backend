package no.fint.betaling.common.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "fint.betaling")
public class ApplicationProperties {

    private Boolean demo;

    private String demoUserEmployeeId;

    private String demoUserOrgId;

}
