package ch.so.agi.meta;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class CatalogTextTests {
    @Test void preservesFormattingButRemovesExecutableHtml() {
        String html = CatalogText.html("<p>Hello <b>world</b><script>alert(1)</script><a href='javascript:alert(1)' onclick='steal()'>x</a></p>");
        assertTrue(html.contains("<b>world</b>"));
        assertFalse(html.contains("<script"));
        assertFalse(html.contains("javascript:"));
        assertFalse(html.contains("onclick"));
        assertNull(CatalogText.html(null));
        assertEquals("Hello world", CatalogText.plain("<p>Hello <b>world</b></p>"));
    }
    @Test void termsIgnoreWhitespaceDuplicatesAndEmptyEntries() {
        assertEquals(java.util.List.of("Wasser", "Fluss"), CatalogText.terms(" Wasser, ,Fluss,Wasser,"));
        assertTrue(CatalogText.terms(null).isEmpty());
    }
}
