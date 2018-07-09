package no.fint.betaling;

import com.github.springfox.loader.EnableSpringfox;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.oauth2.client.EnableOAuth2Sso;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
//@EnableScheduling
@EnableSpringfox
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
