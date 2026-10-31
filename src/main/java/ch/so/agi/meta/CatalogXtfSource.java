package ch.so.agi.meta;

import java.io.*;
import java.nio.file.*;

/** Only explicit local sources; never downloads or falls back to demo data. */
public record CatalogXtfSource(String location) {
    public InputStream open() throws IOException {
        if (location.startsWith("file:")) return Files.newInputStream(Path.of(location.substring(5)));
        if (location.startsWith("classpath:")) {
            String name = location.substring(10).replaceFirst("^/", "");
            var stream = Thread.currentThread().getContextClassLoader().getResourceAsStream(name);
            if (stream == null) throw new FileNotFoundException("Catalog classpath resource not found: " + name);
            return stream;
        }
        throw new IllegalArgumentException("catalog.xtf-location must start with file: or classpath:");
    }
}
