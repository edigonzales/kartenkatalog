package ch.so.agi.meta;

import io.quarkus.test.junit.*;
import jakarta.inject.Inject;
import java.util.*;
import org.jsoup.Jsoup;
import org.junit.jupiter.api.Test;
import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
@TestProfile(CatalogProvenanceTests.NoticeProfile.class)
class CatalogProvenanceTests {
    static final String NOTICE="Hinweis <script>alert(17)</script><img src=x onerror=alert(17)> & \"Quelle\"";
    @Inject CatalogConfig config;
    public static class NoticeProfile implements QuarkusTestProfile {
        @Override public Map<String,String> getConfigOverrides() {
            return Map.of("catalog.xtf-location","classpath:contract/with-qml.xml","catalog.provenance-notice",NOTICE);
        }
    }
    @Test void configMappingResolvesExplicitClasspathSourceAndOptionalNotice() {
        assertEquals("classpath:contract/with-qml.xml",config.xtfLocation());
        assertEquals(Optional.of(NOTICE),config.provenanceNotice());
        assertEquals(1,given().get("/api/v1/ebenen").then().statusCode(200).extract().jsonPath().getList("id").size());
    }
    @Test void configuredNoticeIsLiteralTextOnEveryFullPage() {
        for(String path:List.of("/","/thema","/themen/contract/ebenen","/ebene/ch.test.qml")) {
            String html=given().get(path).then().statusCode(200).extract().asString();
            var doc=Jsoup.parse(html);
            assertEquals(1,doc.select("footer .provenance").size());
            assertEquals(NOTICE,doc.select("footer .provenance").text());
            assertTrue(doc.select("script:not([src]),[onerror],img[src=x]").isEmpty());
            assertTrue(html.contains("&lt;script&gt;alert(17)&lt;/script&gt;"));
            assertFalse(html.contains("Der mitgelieferte Harvest ist unvollständig"));
        }
    }
    @Test void noticeDoesNotLeakIntoTheApiOrChangeQmlAndMapLinks() {
        String json=given().get("/api/v1/ebenen/ch.test.qml").then().statusCode(200).extract().asString();
        assertFalse(json.contains("provenance")); assertFalse(json.contains("alert(17)"));
        var layer=io.restassured.path.json.JsonPath.from(json);
        assertEquals("ch.test.qml",layer.getString("qwcLayerName"));
        assertEquals("Sicher formatiert",layer.getString("description"));
        assertEquals("<qgis version=\"3.40\"><renderer-v2 type=\"singleSymbol\"/></qgis>",given().get("/ebene/ch.test.qml/style").then().statusCode(200).contentType("application/xml").extract().asString());
        var doc=Jsoup.parse(given().get("/ebene/ch.test.qml").asString());
        assertEquals("https://geo.so.ch/map?l=ch.test.qml",doc.select("a.primary-action").attr("href"));
        assertEquals("UP",given().get("/q/health/ready").then().statusCode(200).extract().jsonPath().getString("status"));
    }
}
