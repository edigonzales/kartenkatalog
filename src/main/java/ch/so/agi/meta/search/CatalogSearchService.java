package ch.so.agi.meta.search;

import ch.so.agi.meta.*;
import ch.so.agi.meta.api.CatalogProblem;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.text.Collator;
import java.util.*;

@ApplicationScoped
public class CatalogSearchService {
    private final CatalogSnapshot snapshot;
    @Inject public CatalogSearchService(CatalogStore store) { this(store.current()); }
    public CatalogSearchService(CatalogSnapshot snapshot) { this.snapshot = snapshot; }

    public CatalogSearchResult search(CatalogQuery query) {
        validate(query);
        var scores = new HashMap<String, Float>();
        var candidates = textMatches(query, scores);
        var themesByTid = new HashMap<String, String>();
        snapshot.findAllThemen().forEach(t -> themesByTid.put(t.t_id(), t.identifier()));
        var collator = Collator.getInstance(Locale.forLanguageTag("de-CH")); collator.setStrength(Collator.PRIMARY);
        Comparator<Ebene> titles = Comparator.comparing(Ebene::title, collator).thenComparing(Ebene::ident_part);
        Comparator<Ebene> order = switch (query.sort()) {
            case "title-desc" -> Comparator.comparing(Ebene::title, collator.reversed()).thenComparing(Ebene::ident_part);
            case "relevance" -> Comparator.<Ebene>comparingDouble(e -> scores.getOrDefault(e.ident_part(), 0f)).reversed().thenComparing(titles);
            default -> titles;
        };
        var layers = candidates.stream().filter(e -> CatalogFacetService.matches(query.thema(), themesByTid.get(e.thema_r()))
            && CatalogFacetService.matches(query.typ(), e.dtype()) && CatalogFacetService.matches(query.organisation(), e.org_name())).sorted(order).toList();
        return new CatalogSearchResult(layers, layers.size(), query, scores);
    }
    public CatalogFacetService.Facets facets(CatalogQuery query) {
        validate(query);
        return new CatalogFacetService().calculate(query, textMatches(query, new HashMap<>()), snapshot);
    }
    private List<Ebene> textMatches(CatalogQuery query, Map<String, Float> scores) {
        if (!snapshot.searchIndex().available()) throw new SearchUnavailableException(null);
        if (query.q().isBlank()) return snapshot.findAllEbenen();
        return snapshot.searchIndex().search(query.q()).stream().map(hit -> {
            scores.put(hit.id(), hit.score());
            return snapshot.findEbeneByIdentPart(hit.id()).orElseThrow(() -> new SearchUnavailableException(new IllegalStateException("Unresolved index ID")));
        }).toList();
    }
    private void validate(CatalogQuery query) {
        for (String id : query.thema()) if (snapshot.findThema(id).isEmpty()) throw new CatalogProblem(404, "Thema nicht gefunden: " + id);
        if (!CatalogFacetService.values(snapshot.findAllEbenen(), Ebene::dtype).keySet().containsAll(query.typ()))
            throw new IllegalArgumentException("Unbekannter Produkttyp");
        if (!CatalogFacetService.values(snapshot.findAllEbenen(), Ebene::org_name).keySet().containsAll(query.organisation()))
            throw new IllegalArgumentException("Unbekannte Organisation");
    }
}
