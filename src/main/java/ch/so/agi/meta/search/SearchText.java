package ch.so.agi.meta.search;

import java.text.Normalizer;
import java.util.*;

public final class SearchText {
    private SearchText() {}
    public static String normalize(String text) {
        if (text == null) return "";
        return Normalizer.normalize(text.toLowerCase(Locale.ROOT).replace("ß", "ss"), Normalizer.Form.NFD)
            .replaceAll("\\p{M}", "").replaceAll("[^\\p{L}\\p{N}]+", " ").strip();
    }
    public static List<String> tokens(String text) {
        if (text != null && text.length() > 200) throw new IllegalArgumentException("Suchtext darf höchstens 200 Zeichen enthalten");
        if (text == null || text.isBlank()) return List.of();
        String normalized = normalize(text);
        if (normalized.isEmpty()) throw new IllegalArgumentException("Suchtext muss Buchstaben oder Zahlen enthalten");
        var tokens = Arrays.stream(normalized.split(" +")).distinct().toList();
        if (tokens.stream().noneMatch(t -> t.codePointCount(0, t.length()) >= 2))
            throw new IllegalArgumentException("Suchtext muss mindestens einen Begriff mit zwei Zeichen enthalten");
        return tokens;
    }
}
