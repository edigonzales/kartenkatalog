package ch.so.agi.meta;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/** One validated catalog. All collections (including nested lists) are immutable. */
public final class CatalogSnapshot implements CatalogRepository, AutoCloseable {
    private final ch.so.agi.meta.search.CatalogSearchIndex searchIndex;
    private final List<Thema> themes;
    private final List<Ebene> layers;
    private final Map<String, Thema> themesByIdentifier;
    private final Map<String, Ebene> layersByIdentifier;
    private final Map<String, List<Ebene>> layersByTheme;

    CatalogSnapshot(List<Thema> themes, List<Ebene> layers) {
        this.themes = List.copyOf(themes);
        this.layers = List.copyOf(layers);
        themesByIdentifier = Map.copyOf(themes.stream().collect(Collectors.toMap(Thema::identifier, Function.identity())));
        layersByIdentifier = Map.copyOf(layers.stream().collect(Collectors.toMap(Ebene::ident_part, Function.identity())));
        var grouped = layers.stream().collect(Collectors.groupingBy(Ebene::thema_r));
        var byTheme = new HashMap<String, List<Ebene>>();
        for (var theme : themes) byTheme.put(theme.identifier(), List.copyOf(grouped.getOrDefault(theme.t_id(), List.of())));
        layersByTheme = Map.copyOf(byTheme);
        var byTid = themes.stream().collect(Collectors.toMap(Thema::t_id, Function.identity()));
        searchIndex = new ch.so.agi.meta.search.CatalogSearchIndexBuilder().build(this.layers, byTid);
    }
    public ch.so.agi.meta.search.CatalogSearchIndex searchIndex() { return searchIndex; }
    @Override public void close() { searchIndex.close(); }
    public Map<String, Thema> themesByIdentifier() { return themesByIdentifier; }
    public Map<String, Ebene> layersByIdentifier() { return layersByIdentifier; }
    public Map<String, List<Ebene>> layersByTheme() { return layersByTheme; }
    @Override public List<Thema> findAllThemen() { return themes; }
    @Override public List<Ebene> findAllEbenen() { return layers; }
    @Override public Optional<Thema> findThema(String identifier) { return Optional.ofNullable(themesByIdentifier.get(identifier)); }
    @Override public Optional<Ebene> findEbeneByIdentPart(String id) { return Optional.ofNullable(layersByIdentifier.get(id)); }
    @Override public List<Ebene> findEbenenByThemaIdentifier(String identifier) { return layersByTheme.getOrDefault(identifier, List.of()); }
}
