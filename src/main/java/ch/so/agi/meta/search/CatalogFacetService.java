package ch.so.agi.meta.search;

import ch.so.agi.meta.*;
import java.text.Collator;
import java.util.*;
import java.util.function.Function;

public final class CatalogFacetService {
    public record Facet(String value, String label, int count, boolean selected) {}
    public record Facets(List<Facet> thema, List<Facet> typ, List<Facet> organisation) {}

    public Facets calculate(CatalogQuery query, List<Ebene> textMatches, CatalogSnapshot snapshot) {
        var themeForTid = new HashMap<String, String>();
        snapshot.findAllThemen().forEach(t -> themeForTid.put(t.t_id(), t.identifier()));
        Function<Ebene, String> theme = e -> themeForTid.get(e.thema_r());
        var themeLabels = new HashMap<String, String>();
        snapshot.findAllThemen().forEach(t -> themeLabels.put(t.identifier(), t.title()));
        return new Facets(
            facets(themeLabels, textMatches, e -> matches(query.typ(), e.dtype()) && matches(query.organisation(), e.org_name()), theme, query.thema()),
            facets(values(snapshot.findAllEbenen(), Ebene::dtype), textMatches,
                e -> matches(query.thema(), theme.apply(e)) && matches(query.organisation(), e.org_name()), Ebene::dtype, query.typ()),
            facets(values(snapshot.findAllEbenen(), Ebene::org_name), textMatches,
                e -> matches(query.thema(), theme.apply(e)) && matches(query.typ(), e.dtype()), Ebene::org_name, query.organisation()));
    }
    static boolean matches(Set<String> selected, String value) { return selected.isEmpty() || (value != null && selected.contains(value)); }
    static Map<String, String> values(List<Ebene> layers, Function<Ebene, String> field) {
        var result = new HashMap<String, String>();
        for (var layer : layers) {
            String value = field.apply(layer);
            if (value != null && !value.isBlank()) result.put(value, value);
        }
        return result;
    }
    private List<Facet> facets(Map<String, String> labels, List<Ebene> candidates, java.util.function.Predicate<Ebene> others,
                               Function<Ebene, String> value, Set<String> selected) {
        var counts = new HashMap<String, Integer>();
        for (var e : candidates) if (others.test(e)) {
            String key = value.apply(e);
            if (key != null && !key.isBlank()) counts.merge(key, 1, Integer::sum);
        }
        var collator = Collator.getInstance(Locale.forLanguageTag("de-CH")); collator.setStrength(Collator.PRIMARY);
        return labels.entrySet().stream().sorted(Map.Entry.<String, String>comparingByValue(collator).thenComparing(Map.Entry.comparingByKey()))
            .map(e -> new Facet(e.getKey(), e.getValue(), counts.getOrDefault(e.getKey(), 0), selected.contains(e.getKey()))).toList();
    }
}
