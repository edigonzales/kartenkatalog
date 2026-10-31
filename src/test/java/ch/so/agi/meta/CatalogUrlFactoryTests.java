package ch.so.agi.meta;
import ch.so.agi.meta.web.CatalogUrlFactory;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class CatalogUrlFactoryTests {
    @Test void externalLinksAllowOnlyAbsoluteHttpWithHostAndWithoutCredentials() {
        for(String url:new String[]{"javascript:alert(1)","data:text/html,test","//evil.org","file:/etc/passwd","https://user:pass@evil.org","http:example.org","https://example.org/\r\nX:1",""})assertNull(CatalogUrlFactory.external(url),url);
        assertNull(CatalogUrlFactory.external(null));assertEquals("https://example.org/info?x=1",CatalogUrlFactory.external("https://example.org/info?x=1"));
        assertEquals("ch.test.a%22%0D%0A",CatalogUrlFactory.encoded("ch.test.a\"\r\n"));
    }
}
