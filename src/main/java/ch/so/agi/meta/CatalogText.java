package ch.so.agi.meta;

import java.util.Arrays;
import java.util.List;
import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;

/** Shared presentation rules for HTML and JSON; stored metadata is never modified. */
public final class CatalogText {
    private CatalogText() {}
    public static String description(Ebene e) {
        for (String value : new String[]{e.adescription(), e.description_override(), e.description_model()}) {
            if (value != null && !value.isBlank()) return value;
        }
        return null;
    }
    public static String html(String value) {
        return value == null ? null : Jsoup.clean(value, Safelist.basic().addEnforcedAttribute("a", "rel", "noopener noreferrer"));
    }
    public static String plain(String value) { return value == null ? null : Jsoup.parse(value).text(); }
    public static List<String> terms(String value) {
        return value == null ? List.of() : Arrays.stream(value.split(",")).map(String::strip).filter(s -> !s.isEmpty()).distinct().toList();
    }
}
