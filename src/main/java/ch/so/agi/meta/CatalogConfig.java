package ch.so.agi.meta;

import io.smallrye.config.ConfigMapping;
import java.util.Optional;

@ConfigMapping(prefix = "catalog")
public interface CatalogConfig {
    String xtfLocation();
    Optional<String> provenanceNotice();
}
