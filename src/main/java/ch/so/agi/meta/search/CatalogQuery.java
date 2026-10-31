package ch.so.agi.meta.search;

import java.util.*;

public record CatalogQuery(String q, Set<String> thema, Set<String> typ, Set<String> organisation, String sort) {
    public CatalogQuery {
        q = q == null ? "" : q;
        SearchText.tokens(q); // also enforces the raw 200-character bound before trimming
        q = q.strip();
        thema = thema == null ? Set.of() : Set.copyOf(thema);
        typ = typ == null ? Set.of() : Set.copyOf(typ);
        organisation = organisation == null ? Set.of() : Set.copyOf(organisation);
        sort = sort == null || sort.isEmpty() ? "title-asc" : sort;
        if (!Set.of("title-asc", "title-desc", "relevance").contains(sort)) throw new IllegalArgumentException("Ungültige Sortierung");
        if (sort.equals("relevance") && q.isBlank()) throw new IllegalArgumentException("Relevanz erfordert einen Suchtext");
    }
    public static CatalogQuery of(String q) { return new CatalogQuery(q, Set.of(), Set.of(), Set.of(), null); }
}
