package ch.so.agi.meta;

import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportRuntimeHints;
import org.springframework.web.filter.ForwardedHeaderFilter;

import ch.so.agi.meta.ResourceRuntimeHints;

@ImportRuntimeHints(ResourceRuntimeHints.class)
@Configuration
@SpringBootApplication
@RegisterReflectionForBinding({ ch.so.agi.meta.Thema.class, ch.so.agi.meta.Ebene.class })
public class KartenkatalogApplication {

    public static void main(String[] args) {
        SpringApplication.run(KartenkatalogApplication.class, args);
    }

    @Bean
    ForwardedHeaderFilter forwardedHeaderFilter() {
        return new ForwardedHeaderFilter();
    } 
}
