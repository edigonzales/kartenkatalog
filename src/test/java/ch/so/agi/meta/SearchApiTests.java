package ch.so.agi.meta;

import java.util.Map;
import org.junit.jupiter.api.Test;
import io.quarkus.test.junit.*;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@QuarkusTest
@TestProfile(SearchApiTests.SearchProfile.class)
class SearchApiTests {
    public static class SearchProfile implements QuarkusTestProfile {
        @Override public Map<String, String> getConfigOverrides() { return Map.of("catalog.xtf-location", "classpath:search/catalog.xml"); }
    }
    @Test void httpLuceneRankingCrossFieldAndNormalization() {
        given().queryParam("q", "atlas").queryParam("sort", "relevance").get("/api/v1/ebenen").then().statusCode(200)
            .body("id", contains("atlas", "ch.test.exact", "ch.test.prefix", "ch.test.substring", "ch.test.keyword", "ch.test.synonym", "ch.test.org", "ch.test.theme", "ch.test.description"));
        given().queryParam("q", "WASSERKARTE, Bäche").get("/api/v1/ebenen").then().statusCode(200).body("id", contains("ch.test.cross"));
        given().queryParam("q", "AV").get("/api/v1/ebenen").then().statusCode(200).body("id", hasItems("ch.test.av", "ch.test.cross"));
        given().queryParam("q", "Strasse Bodenprobe").get("/api/v1/ebenen").then().statusCode(200).body("id", contains("ch.test.cross"));
        given().queryParam("q", "atlas impossible").get("/api/v1/ebenen").then().statusCode(200).body(equalTo("[]"));
    }
    @Test void repeatedFiltersAreOrWithinAndBetweenGroups() {
        given().queryParam("thema", "hydro", "plan").queryParam("typ", "extwms", "layergroup").queryParam("organisation", "Amt A", "Amt B")
            .get("/api/v1/ebenen").then().statusCode(200).body("id", containsInAnyOrder("ch.test.prefix", "ch.test.keyword", "ch.test.av"));
        given().queryParam("thema", "plan").queryParam("q", "Fluss").get("/api/v1/ebenen").then().statusCode(200).body(equalTo("[]"));
    }
    @Test void httpFacetsUseOtherGroupsAndRetainSelectionAtZero() {
        given().queryParam("q", "atlas").queryParam("thema", "hydro").queryParam("typ", "datasetview").get("/api/v1/facets").then().statusCode(200)
            .body("thema.find {it.value == 'hydro'}.count", equalTo(2))
            .body("thema.find {it.value == 'plan'}.count", equalTo(3))
            .body("typ.find {it.value == 'layergroup'}.count", equalTo(1))
            .body("organisation.find {it.value == 'Amt A'}.count", equalTo(2));
        given().queryParam("q", "nomatch").queryParam("thema", "hydro").get("/api/v1/facets").then().statusCode(200)
            .body("thema.find {it.value == 'hydro'}.selected", equalTo(true)).body("thema.find {it.value == 'hydro'}.count", equalTo(0));
    }
    @Test void invalidParametersHaveControlledProblemResponses() {
        for (String path : new String[]{"/api/v1/ebenen", "/api/v1/facets"}) {
            for (String q : new String[]{"x", "***", "x".repeat(201), " ".repeat(201)})
                given().queryParam("q", q).get(path).then().statusCode(400).contentType("application/problem+json").body("status", equalTo(400));
            given().queryParam("sort", "latest").get(path).then().statusCode(400);
            given().queryParam("sort", "relevance").get(path).then().statusCode(400);
            given().queryParam("typ", "missing").get(path).then().statusCode(400);
            given().queryParam("organisation", "missing").get(path).then().statusCode(400);
            given().queryParam("thema", "missing").get(path).then().statusCode(404).contentType("application/problem+json");
        }
    }
}
