package ch.so.agi.meta.web;

import ch.so.agi.meta.Ebene;
import ch.so.agi.meta.search.CatalogQuery;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

public final class CatalogUrlFactory {
    private CatalogUrlFactory() {}
    public static String encoded(String value) { return URLEncoder.encode(value, StandardCharsets.UTF_8).replace("+", "%20"); }
    public static String theme(String identifier) { return "/themen/" + encoded(identifier) + "/ebenen"; }
    public static String layer(String identifier) { return "/ebene/" + encoded(identifier); }
    public static String mapUrl(Ebene layer) {
        String value = layer.ident_part() + (layer.transparency() == null || layer.transparency() == 0 ? "" : "[" + layer.transparency() + "]");
        return "https://geo.so.ch/map?l=" + encoded(value);
    }
    /** Only explicitly absolute HTTP(S) metadata links may become hrefs. JSON stays unchanged. */
    public static String external(String value) {
        if (value == null || value.isBlank()) return null;
        try {
            URI uri = URI.create(value.strip());
            if (!Set.of("http", "https").contains(Objects.toString(uri.getScheme(), "").toLowerCase(Locale.ROOT))
                || uri.getHost() == null || uri.getRawUserInfo() != null) return null;
            return uri.toASCIIString();
        } catch (IllegalArgumentException error) { return null; }
    }
    public static String listUrl(CatalogQuery query) { return "/" + queryString(query, true); }
    public static String withoutFilter(CatalogQuery query, String kind, String value) {
        var themes = new HashSet<>(query.thema()); var types = new HashSet<>(query.typ()); var orgs = new HashSet<>(query.organisation());
        switch (kind) {
            case "thema" -> themes.remove(value);
            case "typ" -> types.remove(value);
            case "organisation" -> orgs.remove(value);
            case "q" -> { }
            default -> throw new IllegalArgumentException("Unbekannte Filtergruppe");
        }
        String q = kind.equals("q") ? "" : query.q();
        String sort = q.isBlank() && query.sort().equals("relevance") ? "title-asc" : query.sort();
        return listUrl(new CatalogQuery(q, themes, types, orgs, sort));
    }
    public static String themeQuery(String identifier, CatalogQuery query) { return theme(identifier) + queryString(query, false); }
    private static String queryString(CatalogQuery query, boolean includeThemes) {
        var parameters = new ArrayList<String>();
        if (!query.q().isEmpty()) parameters.add("q=" + encoded(query.q()));
        if (includeThemes) query.thema().stream().sorted().forEach(v -> parameters.add("thema=" + encoded(v)));
        query.typ().stream().sorted().forEach(v -> parameters.add("typ=" + encoded(v)));
        query.organisation().stream().sorted().forEach(v -> parameters.add("organisation=" + encoded(v)));
        if (!query.sort().equals("title-asc")) parameters.add("sort=" + encoded(query.sort()));
        return parameters.isEmpty() ? "" : "?" + String.join("&", parameters);
    }
}
