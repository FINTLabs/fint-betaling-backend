package no.fint.betaling.config;

import com.mongodb.MongoClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.web.client.RestTemplate;

@Configuration
public class Config {

    private static final String MONGOHOST = "localhost";
    private static final int MONGOPORT = 27017;
    private static final String MONGODATABASENAME = "Fint-Betaling";


    @Bean
    public RestTemplate getRestTemplate() {
        return new RestTemplate();
    }

    @Bean
    public MongoTemplate getMongoTemplate() {
        return new MongoTemplate(new MongoClient(MONGOHOST, MONGOPORT), MONGODATABASENAME);
    }
}
