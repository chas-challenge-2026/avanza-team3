package se.comerit.avanza;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class AvanzaPortalApplication {
    public static void main(String[] args) {
        SpringApplication.run(AvanzaPortalApplication.class, args);
    }
}
