package ch.so.agi.meta;

import io.quarkus.test.junit.QuarkusIntegrationTest;
import org.junit.jupiter.api.Test;
import java.util.List;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

/** Runs against the actual packaged artifact, never the in-process @QuarkusTest container. */
@QuarkusIntegrationTest
class PackagedCatalogIT {
    @Test void actualArtifactIndexesTheHarvestAndKeepsThePublicApi() {
        given().get("/q/health/live").then().statusCode(200).body("status",equalTo("UP"));
        given().get("/q/health/ready").then().statusCode(200).body("status",equalTo("UP"));
        List<String> ids=given().get("/api/v1/ebenen").then().statusCode(200).extract().jsonPath().getList("id");
        assertEquals(886,ids.size());assertEquals(886,ids.stream().distinct().count());
        given().get("/api/v1/themen").then().statusCode(200).body("size()",equalTo(169));
        given().get("/api/v1/catalog").then().statusCode(200).body("themes.size()",equalTo(169));
        given().get("/api/v1/ebenen/ch.so.agi.gemeindegrenzen").then().statusCode(200)
            .body("qwcLayerName",equalTo("ch.so.agi.gemeindegrenzen")).body("keywords",notNullValue()).body("organization",nullValue());
        List<String> hits=given().queryParam("q","wasser").queryParam("sort","relevance").get("/api/v1/ebenen").then().statusCode(200).extract().jsonPath().getList("id");
        assertFalse(hits.isEmpty());assertEquals(hits.size(),hits.stream().distinct().count());
        given().get("/api/v1/facets").then().statusCode(200).body("organisation.size()",equalTo(0));
    }
    @Test void httpsDescriptionLinksSurviveNativeSanitization() {
        // Real harvest link: native must support the HTTPS URL handler used internally by Jsoup.
        given().get("/api/v1/ebenen/ch.so.agi.av.bodenbedeckung_einzelobjekte").then().statusCode(200)
            .body("descriptionHtml",containsString("href=\"https://www.admin.ch/opc/de/classified-compilation/19920299/index.html#a6\""));
    }
    @Test void packagedTemplatesAssetsAndControlledErrorsWork() {
        given().get("/").then().statusCode(200).contentType("text/html").body(containsString("<!doctype html>"),containsString("catalog-state"));
        given().header("HX-Request",true).queryParam("q","Gemeindegrenzen").get("/").then().statusCode(200)
            .body(not(containsString("<!doctype html>")),containsString("catalog-state"));
        given().get("/ebene/ch.so.agi.gemeindegrenzen").then().statusCode(200).body(containsString("https://geo.so.ch/map?l=ch.so.agi.gemeindegrenzen"));
        given().get("/catalog.js").then().statusCode(200).body(containsString("htmx:afterSwap"));
        given().get("/vendor/htmx/2.0.11/htmx.min.js").then().statusCode(200);
        given().get("/api/v1/ebenen/missing").then().statusCode(404).contentType("application/problem+json");
        given().get("/ebene/ch.so.agi.gemeindegrenzen/style").then().statusCode(404);
        given().queryParam("q","x".repeat(201)).get("/api/v1/ebenen").then().statusCode(400);
        given().queryParam("typ","unknown").get("/api/v1/ebenen").then().statusCode(400);
    }
}
