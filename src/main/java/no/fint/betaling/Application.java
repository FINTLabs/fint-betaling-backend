package no.fint.betaling;

import com.github.springfox.loader.EnableSpringfox;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.hateoas.config.EnableHypermediaSupport;

@SpringBootApplication
@EnableSpringfox
@EnableHypermediaSupport(type=EnableHypermediaSupport.HypermediaType.HAL)
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
