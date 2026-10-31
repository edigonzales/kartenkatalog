package ch.so.agi.meta;

import ch.so.agi.meta.api.CatalogApiController;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class CatalogSearchTests {
    private final CatalogApiController api = new CatalogApiController(new CatalogService(new MockCatalogRepository()));
    @Test void searchesKeywordsSynonymsAndCombinesThemeFilter() {
        assertEquals(1, api.layers("  GESTEIN ", null).size());
        assertEquals(2, api.layers("Planung", "planung").size());
        assertTrue(api.layers("Gestein", "planung").isEmpty());
        assertEquals(4, api.layers("", null).size());
    }
    @Test void stableSortAndLookupRemainConsistentDuringConcurrentRequests() {
        java.util.stream.IntStream.range(0, 100).parallel().forEach(i -> {
            var layers = api.layers("", "planung");
            assertEquals("Bauzonen", layers.getFirst().title());
            assertEquals("Nutzungsplanung", layers.getLast().title());
        });
    }
}
