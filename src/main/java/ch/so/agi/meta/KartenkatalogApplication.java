package ch.so.agi.meta;

import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ImportRuntimeHints;


@ImportRuntimeHints(ResourceRuntimeHints.class)
@SpringBootApplication
@RegisterReflectionForBinding({ ch.so.agi.meta.Thema.class, ch.so.agi.meta.Ebene.class,
    ch.so.agi.meta.api.CatalogDtos.Theme.class, ch.so.agi.meta.api.CatalogDtos.Layer.class,
    ch.so.agi.meta.api.CatalogDtos.ThemeLayers.class, ch.so.agi.meta.api.CatalogDtos.Catalog.class })
public class KartenkatalogApplication {

    public static void main(String[] args) {
        SpringApplication.run(KartenkatalogApplication.class, args);
    }

}
