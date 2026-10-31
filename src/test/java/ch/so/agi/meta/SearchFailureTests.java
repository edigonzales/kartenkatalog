package ch.so.agi.meta;

import java.util.Map;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;
import io.quarkus.test.junit.*;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@QuarkusTest
@TestProfile(SearchFailureTests.FailureProfile.class)
class SearchFailureTests {
    public static class FailureProfile implements QuarkusTestProfile {
        @Override public Map<String, String> getConfigOverrides() { return Map.of("catalog.xtf-location", "classpath:search/catalog.xml"); }
    }
    @Inject CatalogStore store;
    @Test void unavailableIndexIs503NeverAnEmptySuccessAndReadinessIsDown() {
        store.current().close();
        for (String path : new String[]{"/api/v1/ebenen", "/api/v1/ebenen?q=atlas", "/api/v1/facets", "/api/v1/facets?q=atlas"})
            given().get(path).then().statusCode(503).contentType("application/problem+json")
                .body("status", equalTo(503)).body("title", equalTo("Service Unavailable")).body("trace", nullValue());
        given().get("/").then().statusCode(503).contentType("application/problem+json");
        given().header("HX-Request","true").get("/themen/hydro/ebenen").then().statusCode(503).contentType("application/problem+json");
        given().get("/q/health/ready").then().statusCode(503).body("status", equalTo("DOWN"));
        given().get("/api/v1/ebenen/ch.test.exact").then().statusCode(200).body("id", equalTo("ch.test.exact"));
    }
}
