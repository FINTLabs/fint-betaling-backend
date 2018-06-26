package no.fint.betaling.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;

@Configuration
public class Config {

    private static final String MONGOHOST = "localhost";
    private static final String MONGOPORT = "27017";
    private static final String MONGODATABASENAME = "Fint-Betaling";


    @Bean
    public RestTemplate getRestTemplate() {
        return new RestTemplate();
    }

    @Bean
    public MongoTemplate getMongoTemplate() {
        String connectionString = String.format("mongodb://%s:%s/%s", MONGOHOST, MONGOPORT, MONGODATABASENAME);
        return new MongoTemplate(new MongoClient(new MongoClientURI(connectionString)), MONGODATABASENAME);
    }

    @Autowired
    private ObjectMapper objectMapper;

    @PostConstruct
    public void init() {
        objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
    }
}

