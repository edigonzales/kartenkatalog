package ch.so.agi.meta;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.Test;

class CatalogServiceTests {

    private final CatalogService catalogService = new CatalogService(new MockCatalogRepository());

    @Test
    void returnsAllThemen() {
        List<Thema> themen = catalogService.findAllThemen();

        assertEquals(2, themen.size());
        assertEquals("Geologie", themen.get(0).title());
    }

    @Test
    void returnsSortedEbenenForThema() {
        List<Ebene> ebenen = catalogService.findEbenenByThemaIdentifier("planung");

        assertEquals(2, ebenen.size());
        assertEquals("Bauzonen", ebenen.getFirst().title());
        assertEquals("Nutzungsplanung", ebenen.getLast().title());
    }

    @Test
    void findsEbeneByIdentPart() {
        assertTrue(catalogService.findEbeneByIdentPart("ch.so.planung.bauzonen").isPresent());
    }
}
