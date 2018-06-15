package no.fint.betaling;

import com.mongodb.MongoClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.web.client.RestTemplate;

@Configuration
public class Config {

    @Bean
    public RestTemplate getRestTemplate() {
        return new RestTemplate();
    }

    @Bean
    public MongoTemplate getMongoTemplate() {
        return new MongoTemplate(new MongoClient("localhost:27017"), "FINT-Betaling");
    }
}
