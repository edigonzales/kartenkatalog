package ch.so.agi.maps;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ImportRuntimeHints;

@ImportRuntimeHints(ResourceRuntimeHints.class)
@SpringBootApplication
public class KartenkatalogApplication {

    public static void main(String[] args) {
        SpringApplication.run(KartenkatalogApplication.class, args);
    }

}
