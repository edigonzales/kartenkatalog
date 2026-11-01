package ch.so.agi.meta;

import java.util.Map;
import org.junit.jupiter.api.Test;
import io.quarkus.test.junit.*;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@QuarkusTest
@TestProfile(CatalogQmlContractTests.QmlProfile.class)
class CatalogQmlContractTests {
    public static class QmlProfile implements QuarkusTestProfile {
        @Override public Map<String, String> getConfigOverrides() {
            return Map.of("catalog.xtf-location", "classpath:contract/with-qml.xml");
        }
    }
    @Test void qmlIsAnExactXmlAttachmentNotHtml() {
        given().get("/ebene/ch.test.qml/style").then().statusCode(200).contentType("application/xml")
            .header("Content-Disposition", allOf(startsWith("attachment;"), containsString("ch.test.qml.qml")))
            .body(equalTo("<qgis version=\"3.40\"><renderer-v2 type=\"singleSymbol\"/></qgis>"));
    }
    @Test void descriptionsAreSanitizedAndQmlStaysOutOfPublicJson() {
        given().get("/ebene/ch.test.qml").then().statusCode(200)
            .body(containsString("<strong>formatiert</strong>"), not(containsString("alert(1)")), containsString("ch.test.qml/style"));
        given().get("/api/v1/ebenen/ch.test.qml").then().statusCode(200)
            .body("description", equalTo("Sicher formatiert"))
            .body("descriptionHtml", equalTo("<p>Sicher <strong>formatiert</strong></p>"))
            .body("style_server", nullValue());
    }
    @Test void fullPageAndHtmxFragmentHaveDifferentDocumentsAndSafeAbsoluteLinks() {
        given().get("/themen/contract/ebenen").then().statusCode(200).body(containsString("<!doctype html>"), containsString("href=\"/ebene/ch.test.qml\""));
        given().header("HX-Request", "true").get("/themen/contract/ebenen").then().statusCode(200)
            .body(not(containsString("<html")), containsString("href=\"/ebene/ch.test.qml\""));
        given().get("/q/health/ready").then().statusCode(200).body("status", equalTo("UP"));
    }
}
