package ch.so.agi.meta.search;
import ch.so.agi.meta.Ebene;
import java.util.*;
public record CatalogSearchResult(List<Ebene> layers, int total, CatalogQuery query, Map<String, Float> scores) {
    public CatalogSearchResult { layers = List.copyOf(layers); scores = Map.copyOf(scores); }
}
