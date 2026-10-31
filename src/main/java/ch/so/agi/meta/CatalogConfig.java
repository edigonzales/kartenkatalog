package ch.so.agi.meta;

import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;

@ConfigMapping(prefix = "catalog")
public interface CatalogConfig {
    @WithDefault("file:./data/kartenkatalog-harvest-ohne-qml.xtf")
    String xtfLocation();
}
