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
    public static String themeQuery(String identifier, CatalogQuery query) {
        var parameters = new ArrayList<String>();
        if (!query.q().isEmpty()) parameters.add("q=" + encoded(query.q()));
        query.typ().stream().sorted().forEach(v -> parameters.add("typ=" + encoded(v)));
        query.organisation().stream().sorted().forEach(v -> parameters.add("organisation=" + encoded(v)));
        if (!query.sort().equals("title-asc")) parameters.add("sort=" + encoded(query.sort()));
        return theme(identifier) + (parameters.isEmpty() ? "" : "?" + String.join("&", parameters));
    }
}
