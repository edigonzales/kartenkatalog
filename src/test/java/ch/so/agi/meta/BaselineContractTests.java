package ch.so.agi.meta;

import java.net.URI;
import java.net.http.*;
import java.nio.file.*;
import java.security.MessageDigest;
import java.util.*;
import org.jsoup.Jsoup;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import tools.jackson.databind.*;
import static org.junit.jupiter.api.Assertions.*;

/** Real HTTP contract, deliberately independent of controller and DTO implementation. */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("xtf")
class BaselineContractTests {
    @LocalServerPort int port;
    final ObjectMapper json = new ObjectMapper();
    final JsonNode contract = json.readTree(Objects.requireNonNull(getClass().getResourceAsStream("/contract/baseline.json")));
    final HttpClient client = HttpClient.newBuilder().followRedirects(HttpClient.Redirect.NEVER).build();

    HttpResponse<String> request(String path) throws Exception {
        return client.send(HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + port + path)).GET().build(), HttpResponse.BodyHandlers.ofString());
    }
    JsonNode api(String path) throws Exception {
        var response = request(path);
        assertEquals(200, response.statusCode(), path);
        assertTrue(response.headers().firstValue("Content-Type").orElse("").startsWith("application/json"));
        return json.readTree(response.body());
    }
    static Set<String> names(JsonNode object) {
        var names = new TreeSet<String>(); object.properties().forEach(p -> names.add(p.getKey())); return names;
    }
    void fields(JsonNode actual, String key) {
        var expected = new TreeSet<String>(); contract.get(key).forEach(n -> expected.add(n.asString()));
        assertEquals(expected, names(actual));
    }
    @Test void publicPayloadsRetainNamesTypesNullsListsAndPublishedQwcIdentifier() throws Exception {
        assertEquals(contract.get("layer"), api("/api/v1/ebenen/ch.so.agi.gemeindegrenzen"));
        for (var expected : contract.get("products")) {
            var actual = api("/api/v1/ebenen/" + expected.get("id").asString());
            fields(actual, "layerFields");
            expected.properties().forEach(p -> assertEquals(p.getValue(), actual.get(p.getKey()), p.getKey()));
            assertNotEquals(actual.get("sourceId"), actual.get("qwcLayerName"));
        }
        var themes = api("/api/v1/themen");
        var theme = new ArrayList<JsonNode>();
        themes.forEach(t -> { fields(t, "themeFields"); if (t.get("identifier").asString().equals("harvest-hoheitsgrenzen")) theme.add(t); });
        assertEquals(List.of(contract.get("theme")), theme);
    }
    @Test void fullCatalogIsDeterministicAndConsistentAcrossAllFiveApiForms() throws Exception {
        assertEquals(contract.get("fixture").get("sha256").asString(), HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(Files.readAllBytes(Path.of("data/kartenkatalog-harvest-ohne-qml.xtf")))));
        var themes = api("/api/v1/themen"); var layers = api("/api/v1/ebenen"); var catalog = api("/api/v1/catalog");
        assertEquals(169, themes.size()); assertEquals(886, layers.size());
        assertEquals(Set.of("themes"), names(catalog)); assertEquals(169, catalog.get("themes").size());
        var byId = new HashMap<String, JsonNode>();
        var collator = java.text.Collator.getInstance(Locale.forLanguageTag("de-CH")); collator.setStrength(java.text.Collator.PRIMARY);
        Comparator<JsonNode> order = Comparator.comparing((JsonNode n) -> n.get("title").asString(), collator).thenComparing(n -> n.get("id").asString());
        var sorted = new ArrayList<JsonNode>(); layers.forEach(sorted::add);
        var original = List.copyOf(sorted); sorted.sort(order); assertEquals(original, sorted);
        for (var layer : layers) {
            fields(layer, "layerFields"); assertTrue(layer.get("keywords").isArray()); assertTrue(layer.get("synonyms").isArray());
            assertEquals(layer.get("id"), layer.get("qwcLayerName")); assertNull(byId.put(layer.get("id").asString(), layer));
        }
        var seen = new HashSet<String>(); int index = 0;
        for (var entry : catalog.get("themes")) {
            assertEquals(Set.of("theme", "layers"), names(entry)); assertEquals(themes.get(index++), entry.get("theme"));
            assertEquals(entry.get("theme").get("layerCount").asInt(), entry.get("layers").size());
            for (var layer : entry.get("layers")) {
                assertEquals(entry.get("theme").get("identifier"), layer.get("themeIdentifier"));
                assertEquals(byId.get(layer.get("id").asString()), layer); assertTrue(seen.add(layer.get("id").asString()));
            }
        }
        assertEquals(byId.keySet(), seen);
        var expected = catalog.get("themes");
        for (var entry : expected) if (entry.get("theme").get("identifier").asString().equals("harvest-hoheitsgrenzen"))
            assertEquals(entry.get("layers"), api("/api/v1/themen/harvest-hoheitsgrenzen/ebenen"));
        assertEquals(catalog, api("/api/v1/catalog"));
    }
    @Test void emptySearchAndBoundariesHaveSemanticAssertions() throws Exception {
        assertEquals(api("/api/v1/ebenen"), api("/api/v1/ebenen?q=%20%20"));
        assertEquals(0, api("/api/v1/ebenen?q=not-a-real-layer-548139").size());
        assertEquals(0, api("/api/v1/ebenen?q=" + "x".repeat(200)).size());
        var matches = api("/api/v1/ebenen?q=%20GEMEINDEGRENZEN%20&thema=harvest-hoheitsgrenzen");
        assertFalse(matches.isEmpty()); boolean found = false;
        for (var layer : matches) { assertEquals("harvest-hoheitsgrenzen", layer.get("themeIdentifier").asString()); found |= layer.equals(contract.get("layer")); }
        assertTrue(found); assertFalse(api("/api/v1/ebenen?q=wasser").isEmpty());
    }
    @Test void errorsKeepProblemJsonAndNeverMasqueradeAsSuccessfulEmptyLists() throws Exception {
        for (var expected : contract.get("errors")) {
            String path = expected.get("path").asString(); var response = request(path);
            assertEquals(expected.get("status").asInt(), response.statusCode(), path);
            assertTrue(response.headers().firstValue("Content-Type").orElse("").startsWith("application/problem+json"));
            var problem = json.readTree(response.body());
            for (String key : List.of("status", "title", "detail")) assertEquals(expected.get(key), problem.get(key));
            assertEquals(path.split("\\?")[0], problem.get("instance").asString());
            assertFalse(problem.has("trace")); assertFalse(problem.has("exception"));
        }
        for (String path : List.of("/ebene/unknown", "/themen/unknown/ebenen", "/ebene/unknown/style")) assertEquals(404, request(path).statusCode());
    }
    @Test void canonicalHtmlDeepLinksAndMapTargetRemainUsable() throws Exception {
        var home = request("/"); assertEquals(302, home.statusCode()); assertEquals("/thema", URI.create(home.headers().firstValue("Location").orElseThrow()).getPath());
        for (String path : List.of("/thema", "/themen/harvest-hoheitsgrenzen/ebenen", "/ebene/ch.so.agi.gemeindegrenzen")) {
            var response = request(path); assertEquals(200, response.statusCode());
            assertTrue(response.headers().firstValue("Content-Type").orElse("").startsWith("text/html"));
            var doc = Jsoup.parse(response.body()); assertTrue(doc.text().contains(path.equals("/thema") ? "Hoheitsgrenzen" : "Gemeindegrenzen"));
            if (path.startsWith("/ebene/")) {
                assertTrue(doc.select("a[href^='https://geo.so.ch/map?l=ch.so.agi.gemeindegrenzen']").size() > 0);
                assertTrue(doc.select("a[href$='/style']").isEmpty());
            }
        }
        assertEquals("kartenkatalog", request("/ping").body()); assertEquals(200, request("/ping").statusCode());
    }
}
