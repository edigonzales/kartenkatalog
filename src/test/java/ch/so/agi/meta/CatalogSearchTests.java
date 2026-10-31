package ch.so.agi.meta;

import ch.so.agi.meta.search.*;
import java.util.*;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

class CatalogSearchTests {
    CatalogSnapshot snapshot;
    CatalogSearchService service;
    @BeforeEach void load() throws Exception {
        try (var input = new CatalogXtfSource("classpath:search/catalog.xml").open()) { snapshot = new XtfCatalogParser().parse(input); }
        service = new CatalogSearchService(snapshot);
    }
    @AfterEach void close() { snapshot.close(); }
    List<String> ids(String q) { return service.search(CatalogQuery.of(q)).layers().stream().map(Ebene::ident_part).toList(); }
    CatalogQuery query(String q, Set<String> themes, Set<String> types, Set<String> orgs, String sort) { return new CatalogQuery(q, themes, types, orgs, sort); }
    @Test void realIndexVersionHeapDirectoryIdsAndDeterministicRebuild() throws Exception {
        var index = (LuceneCatalogSearchIndex) snapshot.searchIndex();
        assertEquals("10.6.0", org.apache.lucene.util.Version.LATEST.toString());
        assertEquals("ByteBuffersDirectory", index.directoryType()); assertEquals("GermanAnalyzer", index.analyzerType());
        assertEquals(12, index.documentCount()); assertEquals(snapshot.layersByIdentifier().keySet(), index.ids());
        try (var input = new CatalogXtfSource("classpath:search/catalog.xml").open(); var rebuilt = new XtfCatalogParser().parse(input)) {
            assertEquals(index.ids(), ((LuceneCatalogSearchIndex)rebuilt.searchIndex()).ids());
            assertEquals(service.search(query("atlas", Set.of(), Set.of(), Set.of(), "relevance")).layers(),
                new CatalogSearchService(rebuilt).search(query("atlas", Set.of(), Set.of(), Set.of(), "relevance")).layers());
        }
    }
    @Test void identifierTitleKeywordSynonymThemeOrgDescriptionAndCrossFieldAnd() {
        assertEquals(List.of("ch.test.cross"), ids("  WASSERKARTE   Fluss  "));
        assertEquals(List.of("ch.test.cross"), ids("wasserkarte BÄCHE"));
        assertEquals(List.of("ch.test.cross"), ids("muSter")); // Müster -> muster
        assertEquals(List.of("ch.test.cross"), ids("STRASSE Bodenprobe")); // GermanAnalyzer stemming
        assertTrue(ids("AV").containsAll(List.of("ch.test.av", "ch.test.cross")));
        assertEquals(List.of("ch.test.description"), ids("BAUME"));
        assertEquals(List.of("ch.test.description"), ids("atlas der"));
        assertTrue(ids("hydrologie").contains("ch.test.exact"));
        assertTrue(ids("Amt B").contains("ch.test.cross"));
    }
    @Test void fixedRankingTiersAndStableTitleTies() {
        var result = service.search(query("atlas", Set.of(), Set.of(), Set.of(), "relevance"));
        assertEquals(List.of("atlas", "ch.test.exact", "ch.test.prefix", "ch.test.substring", "ch.test.keyword", "ch.test.synonym",
            "ch.test.org", "ch.test.theme", "ch.test.description"), result.layers().stream().map(Ebene::ident_part).toList());
        assertTrue(result.scores().get("atlas") > result.scores().get("ch.test.exact"));
        assertFalse(ids("atlas impossible").iterator().hasNext());
        assertEquals(0, ids("evilmarker").size());
    }
    @Test void filtersUseOrWithinAndAcrossGroupsAndHandleNullOrganizations() {
        var r = service.search(query("", Set.of("hydro", "plan"), Set.of("extwms", "layergroup"), Set.of("Amt A", "Amt B"), null));
        assertEquals(Set.of("ch.test.prefix", "ch.test.keyword", "ch.test.av"), new HashSet<>(r.layers().stream().map(Ebene::ident_part).toList()));
        assertEquals(List.of("ch.test.cross"), service.search(query("Fluss", Set.of("hydro"), Set.of("datasetview"), Set.of("Amt B"), null)).layers().stream().map(Ebene::ident_part).toList());
        assertEquals(0, service.search(query("Fluss", Set.of("plan"), Set.of(), Set.of(), null)).total());
    }
    @Test void facetsExcludeOwnFilterButRespectOtherGroupsAndSearchText() {
        var q = query("atlas", Set.of("hydro"), Set.of("datasetview"), Set.of(), null);
        var f = service.facets(q);
        assertEquals(2, count(f.thema(), "hydro")); assertEquals(3, count(f.thema(), "plan")); assertEquals(0, count(f.thema(), "atlas-theme"));
        assertEquals(2, count(f.typ(), "datasetview")); assertEquals(1, count(f.typ(), "layergroup"));
        assertEquals(2, count(f.organisation(), "Amt A")); assertEquals(0, count(f.organisation(), "Amt B"));
        assertTrue(f.thema().stream().filter(v -> v.value().equals("hydro")).findFirst().orElseThrow().selected());
        var none = service.facets(query("impossible", Set.of("hydro"), Set.of(), Set.of(), null));
        assertTrue(none.thema().stream().allMatch(v -> v.count()==0)); assertTrue(none.thema().stream().anyMatch(CatalogFacetService.Facet::selected));
    }
    static int count(List<CatalogFacetService.Facet> facets, String id) { return facets.stream().filter(f -> f.value().equals(id)).findFirst().orElseThrow().count(); }
    @Test void validationEmptyQueryAndSortDirectionsAreExplicit() {
        assertEquals(12, ids(" ").size()); assertTrue(ids("x".repeat(200)).isEmpty());
        for (String q : List.of("x", "a b", "***", "x".repeat(201), " ".repeat(201))) assertThrows(IllegalArgumentException.class, () -> CatalogQuery.of(q));
        assertThrows(IllegalArgumentException.class, () -> query("", Set.of(), Set.of(), Set.of(), "relevance"));
        assertThrows(IllegalArgumentException.class, () -> service.search(query("", Set.of(), Set.of("unknown"), Set.of(), null)));
        assertThrows(IllegalArgumentException.class, () -> service.search(query("", Set.of(), Set.of(), Set.of("unknown"), null)));
        var asc=ids("atlas"); var desc=service.search(query("atlas", Set.of(), Set.of(), Set.of(), "title-desc")).layers().stream().map(Ebene::ident_part).toList();
        var reversed=new ArrayList<>(asc); Collections.reverse(reversed); assertEquals(reversed, desc);
    }
    @Test void closedIndexFailsSearchAndFacetsEvenForBlankQueryAndClosesIdempotently() {
        snapshot.close(); assertFalse(snapshot.searchIndex().available());
        for(String q : List.of("", "atlas")) {
            assertThrows(SearchUnavailableException.class, () -> service.search(CatalogQuery.of(q)));
            assertThrows(SearchUnavailableException.class, () -> service.facets(CatalogQuery.of(q)));
        }
        snapshot.close();
    }
    @Test void concurrentSearchHasStableSortingAndNoMutableAnalyzerSharing() {
        var expected=ids("atlas"); java.util.stream.IntStream.range(0,100).parallel().forEach(i -> assertEquals(expected,ids("atlas")));
    }
    @Test void sameScoreAndCollationTieAlwaysUsesIdentifierIncludingDescendingTitleSort() {
        var theme=new Thema("t", "t", "Hydrologie", null, null, null, null, null, 2);
        var a=new Ebene("00000000-0000-0000-0000-000000000001", "datasetview", null,null,null,null,"Atlas","ch.test.a",null,null,null,null,null,null,null,null,null,null,null,"t");
        var z=new Ebene("00000000-0000-0000-0000-000000000002", "datasetview", null,null,null,null,"Ätlas","ch.test.z",null,null,null,null,null,null,null,null,null,null,null,"t");
        try(var tied=new CatalogSnapshot(List.of(theme),List.of(z,a))) {
            var svc=new CatalogSearchService(tied);
            for(String sort:List.of("title-asc","title-desc","relevance"))
                assertEquals(List.of("ch.test.a","ch.test.z"),svc.search(query("atlas",Set.of(),Set.of(),Set.of(),sort)).layers().stream().map(Ebene::ident_part).toList());
        }
    }
    @Test void builderRejectsDuplicateIdsAndMissingReferencesWithoutFallingBack() {
        var layer=snapshot.findAllEbenen().getFirst();
        var byTid=new HashMap<String,Thema>(); snapshot.findAllThemen().forEach(t->byTid.put(t.t_id(),t));
        var builder=new CatalogSearchIndexBuilder();
        assertThrows(IllegalStateException.class,()->builder.build(List.of(layer,layer),byTid));
        assertThrows(IllegalStateException.class,()->builder.build(List.of(layer),Map.of()));
        try(var empty=builder.build(List.of(),Map.of())) {
            assertEquals(0,empty.documentCount()); assertTrue(empty.search("atlas").isEmpty());
        }
    }

}
