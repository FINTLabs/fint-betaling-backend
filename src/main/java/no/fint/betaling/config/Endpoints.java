package no.fint.betaling.config;

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

}
