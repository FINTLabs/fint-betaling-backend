package no.fint.betaling.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.util.ISO8601DateFormat;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import no.fint.oauth.OAuthConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;

@Configuration
@Import(OAuthConfig.class)
public class Config {
    private final ObjectMapper objectMapper;

    public Config(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @ConditionalOnProperty(
            name = {"fint.oauth.enabled"},
            matchIfMissing = true,
            havingValue = "false"
    )
    @Bean
    public RestTemplate getRestTemplate() {
        return new RestTemplate();
    }

    @PostConstruct
    public void init() {
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        objectMapper.setDateFormat(new ISO8601DateFormat());
    }
}
