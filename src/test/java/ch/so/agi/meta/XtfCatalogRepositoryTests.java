package ch.so.agi.meta;

import org.junit.jupiter.api.Test;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.FileSystemResource;
import java.nio.charset.StandardCharsets;
import static org.junit.jupiter.api.Assertions.*;

class XtfCatalogRepositoryTests {
    @Test void suppliedFileResolvesAllReferencesAndCounts() {
        var repository = new XtfCatalogRepository(new FileSystemResource("data/kartenkatalog-harvest-ohne-qml.xtf"));
        assertEquals(169, repository.findAllThemen().size());
        assertEquals(886, repository.findAllEbenen().size());
        assertEquals(886, repository.findAllThemen().stream().mapToInt(Thema::ebenen_count).sum());
        assertTrue(repository.findAllEbenen().stream().allMatch(e -> e.style_server() == null));
        assertTrue(repository.findEbenenByThemaIdentifier("missing").isEmpty());
    }
    @Test void rejectsExternalEntitiesAndWrongModel() {
        for (String xml : new String[]{"<!DOCTYPE transfer [<!ENTITY x SYSTEM 'file:///etc/passwd'>]><transfer>&x;</transfer>", "<transfer/>"}) {
            assertThrows(IllegalStateException.class, () -> new XtfCatalogRepository(new ByteArrayResource(xml.getBytes(StandardCharsets.UTF_8))));
        }
    }
    @Test void rejectsDanglingThemeReference() throws Exception {
        String xml = java.nio.file.Files.readString(java.nio.file.Path.of("data/kartenkatalog-harvest-ohne-qml.xtf"));
        xml = xml.replaceFirst("ili:ref=\"[^\"]+\"", "ili:ref=\"missing\"");
        var input = new ByteArrayResource(xml.getBytes(StandardCharsets.UTF_8));
        assertThrows(IllegalStateException.class, () -> new XtfCatalogRepository(input));
    }
}
