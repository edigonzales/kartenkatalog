package ch.so.agi.meta;
import ch.so.agi.meta.web.CatalogUrlFactory;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class CatalogUrlFactoryTests {
    @Test void chipUrlsPreserveRepeatedFiltersAndEncodeEachValue() {
        var query=new ch.so.agi.meta.search.CatalogQuery("AV & Wasser",java.util.Set.of("a/b","c"),java.util.Set.of("extwms"),java.util.Set.of("Amt & A"),"relevance");
        assertEquals("/?q=AV%20%26%20Wasser&thema=a%2Fb&thema=c&typ=extwms&organisation=Amt%20%26%20A&sort=relevance",CatalogUrlFactory.listUrl(query));
        assertEquals("/?thema=a%2Fb&thema=c&typ=extwms&organisation=Amt%20%26%20A",CatalogUrlFactory.withoutFilter(query,"q",""));
        assertFalse(CatalogUrlFactory.withoutFilter(query,"thema","c").contains("thema=c"));
        assertTrue(CatalogUrlFactory.withoutFilter(query,"thema","c").contains("thema=a%2Fb"));
    }
    @Test void externalLinksAllowOnlyAbsoluteHttpWithHostAndWithoutCredentials() {
        for(String url:new String[]{"javascript:alert(1)","data:text/html,test","//evil.org","file:/etc/passwd","https://user:pass@evil.org","http:example.org","https://example.org/\r\nX:1",""})assertNull(CatalogUrlFactory.external(url),url);
        assertNull(CatalogUrlFactory.external(null));assertEquals("https://example.org/info?x=1",CatalogUrlFactory.external("https://example.org/info?x=1"));
        assertEquals("ch.test.a%22%0D%0A",CatalogUrlFactory.encoded("ch.test.a\"\r\n"));
    }
}
