package ch.so.agi.meta;

import io.quarkus.test.junit.*;
import org.junit.jupiter.api.Test;
import org.jsoup.Jsoup;
import java.util.*;
import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
@TestProfile(CatalogFilterWebTests.Profile.class)
class CatalogFilterWebTests {
    private io.restassured.specification.RequestSpecification request() {
        return given().urlEncodingEnabled(false).config(io.restassured.RestAssured.config().paramConfig(
            io.restassured.config.ParamConfig.paramConfig().queryParamsUpdateStrategy(io.restassured.config.ParamConfig.UpdateStrategy.MERGE)));
    }
    public static class Profile implements QuarkusTestProfile {
        public Map<String,String> getConfigOverrides(){return Map.of("catalog.xtf-location","classpath:search/catalog.xml");}
    }
    @Test void repeatedFiltersHaveIdenticalJsonFullHtmlAndFragmentResultsAndFacetCounts() {
        String query="q=Atlas&thema=hydro&thema=plan&typ=datasetview&typ=extwms&organisation=Amt%20A";
        var ids=request().get("/api/v1/ebenen?"+query).then().statusCode(200).extract().jsonPath().getList("id",String.class);
        assertEquals(List.of("ch.test.exact","atlas"),ids);
        for(boolean hx:List.of(false,true)) {
            String html=request().header("HX-Request",hx).get("/?"+query).then().statusCode(200).contentType("text/html;charset=UTF-8")
                .header("Vary","HX-Request, HX-History-Restore-Request").extract().asString();
            assertEquals(!hx,html.contains("<!doctype html>"));
            var doc=Jsoup.parse(html);
            assertEquals(1,doc.select("#catalog-state").size());
            assertEquals(5,doc.select("input[checked]").size());assertEquals(6,doc.select(".filter-chip").size());
            assertEquals(1,doc.select(".theme-row").size());assertTrue(doc.select("#results-heading").text().startsWith("2 Kartenebenen"));
            String expansion=doc.select(".theme-row").attr("hx-get");
            var rows=Jsoup.parse(request().header("HX-Request",true).get(expansion).then().statusCode(200).extract().asString());
            assertEquals(ids.stream().map(id->"/ebene/"+id).toList(),rows.select(".layer-summary h3 a").eachAttr("href"));
            assertEquals(0,doc.select("[name=aktualisiert]").size());
            for(var group:doc.select(".filter-group")) {
                var facets=request().get("/api/v1/facets?"+query).jsonPath().getList(group.attr("data-kind"),Map.class);
                for(var option:group.select("label")) {
                    String value=option.select("input").val();
                    var expected=facets.stream().filter(f->f.get("value").equals(value)).findFirst().orElseThrow();
                    assertEquals(expected.get("count").toString(),option.select(".facet-count").text());
                }
            }
            String removeSearch=doc.select(".search-clear").attr("href");
            assertFalse(removeSearch.contains("q="));assertTrue(removeSearch.contains("thema=hydro"));assertTrue(removeSearch.contains("organisation=Amt%20A"));
        }
    }
    @Test void historyRestoreReturnsFullDocumentAndSelectedZeroCountOptionsRemainRemovable() {
        String full=request().header("HX-Request",true).header("HX-History-Restore-Request",true).get("/?q=Atlas").asString();
        assertTrue(full.contains("<!doctype html>"));
        var empty=Jsoup.parse(request().get("/?q=notfoundxyz&thema=hydro&typ=extwms").asString());
        assertEquals(2,empty.select("input[checked]").size());assertEquals(3,empty.select(".filter-chip").size());
        assertEquals(0,empty.select("input[disabled]").size());assertTrue(empty.select(".empty-state").text().contains("Keine Kartenebenen"));
        request().header("HX-Request",true).get("/?typ=bogus").then().statusCode(400).contentType("application/problem+json");
        request().header("HX-Request",true).get("/?thema=bogus").then().statusCode(404);
        request().get("/?q="+"x".repeat(201)).then().statusCode(400);
    }
    @Test void controlsUseLocalBootstrapSvgsAndNoJavascriptSubmitFallbacks() {
        var doc=Jsoup.parse(request().get("/?q=Atlas").asString());
        assertEquals(1,doc.select(".search-field .bi-search").size());
        assertEquals(3,doc.select(".filter-chevron .bi-chevron-down").size());
        assertEquals(1,doc.select(".reset-link .bi-arrow-clockwise").size());
        assertEquals("Filter zurücksetzen",doc.select(".reset-link").text());
        assertEquals(1,doc.select(".search-field noscript").size());
        assertEquals(1,doc.select(".sort-control noscript").size());
        assertTrue(doc.select("svg.bi").stream().allMatch(svg -> svg.attr("aria-hidden").equals("true")));
        var css=request().get("/fonts.css").then().statusCode(200).extract().asString();
        assertFalse(css.contains("base64"));assertEquals(4,css.split("font-display: swap",-1).length-1);
        request().get("/vendor/bootstrap-icons/1.13.1/LICENSE").then().statusCode(200);
    }

}
