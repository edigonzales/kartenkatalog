package ch.so.agi.meta;

import java.util.Map;
import org.jsoup.Jsoup;
import org.junit.jupiter.api.Test;
import io.quarkus.test.junit.*;
import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
@TestProfile(CatalogWebTests.WebProfile.class)
class CatalogWebTests {
    public static class WebProfile implements QuarkusTestProfile {
        @Override public Map<String,String> getConfigOverrides(){return Map.of("catalog.xtf-location","classpath:web/security.xml");}
    }
    @Test void safeMetadataAndLinksRenderWithNoExecutableSourceHtml() {
        for(String path:new String[]{"/","/thema","/themen/security/ebenen","/ebene/ch.test.security"}) {
            String html=given().get(path).then().statusCode(200).contentType("text/html").extract().asString();
            var doc=Jsoup.parse(html);
            assertTrue(doc.select("script:not([src])").isEmpty());
            assertTrue(doc.select("[onerror],[onclick],a[href^=javascript],img[src=x]").isEmpty());
            assertFalse(html.contains("href=\"javascript:"));
        }
        var detail=Jsoup.parse(given().get("/ebene/ch.test.security").asString());
        assertEquals("formatiert",detail.select("strong").text());
        assertEquals("https://geo.so.ch/map?l=ch.test.security%5B85%5D",detail.select("a.primary-action").attr("href"));
        assertEquals("noopener noreferrer",detail.select("a.primary-action").attr("rel"));
        assertEquals(1,detail.select("a[href='/ebene/ch.test.security/style']").size());
        assertTrue(detail.text().contains("Wasser"));assertTrue(detail.text().contains("Synonyme"));
        var minimal=Jsoup.parse(given().get("/ebene/ch.test.empty").asString());
        assertEquals(0,minimal.select("#keywords-heading,#synonyms-heading,#description-heading,a[download]").size());
        assertFalse(minimal.text().contains("Organisation"));assertFalse(minimal.text().contains("Verfügbarkeit"));
        assertFalse(minimal.text().contains("Download"));
        assertEquals(1,minimal.select("a[href='https://example.org/info?x=1&y=2']").size());
    }
    @Test void reflectedSearchTextIsEscapedInsideTheForm() {
        String payload="\"><img src=x onerror=alert(7)>";
        var doc=Jsoup.parse(given().queryParam("q",payload).get("/").then().statusCode(200).extract().asString());
        assertEquals(payload,doc.select("#catalog-search").val());
        assertTrue(doc.select("img[src=x],[onerror],script:not([src])").isEmpty());
    }
    @Test void listIsNativeDisclosureWithFallbackAndFragmentsVaryByRequestHeader() {
        var root=Jsoup.parse(given().get("/").then().statusCode(200).extract().asString());
        assertEquals(2,root.select("details.theme-row").size());
        assertEquals(2,root.select(".theme-row summary[aria-controls]").size());
        assertEquals(2,root.select(".fallback-link a[href^='/themen/']").size());
        assertEquals(0,root.select(".layer-row").size());
        assertEquals(1,root.select(".skip-link[href='#main-content']").size());
        String full=given().get("/themen/security/ebenen").then().statusCode(200).header("Vary","HX-Request").extract().asString();
        String fragment=given().header("HX-Request","true").get("/themen/security/ebenen").then().statusCode(200).header("Vary","HX-Request").extract().asString();
        assertTrue(full.contains("<!doctype html>"));assertFalse(fragment.contains("<html"));
        assertEquals(1,Jsoup.parse(fragment).select(".layer-row").size());
        given().get("/ebene/unknown").then().statusCode(404);
        given().header("HX-Request","true").get("/themen/unknown/ebenen").then().statusCode(404);
    }
}
