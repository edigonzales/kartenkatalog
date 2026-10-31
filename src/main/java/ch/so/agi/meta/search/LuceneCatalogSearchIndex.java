package ch.so.agi.meta.search;

import java.io.IOException;
import java.util.*;
import org.apache.lucene.analysis.de.GermanAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.index.*;
import org.apache.lucene.search.*;
import org.apache.lucene.store.*;

/** Fixed field tiers avoid BM25/document-length accidents; Lucene supplies only IDs and scores. */
public final class LuceneCatalogSearchIndex implements CatalogSearchIndex {
    static final float EXACT_IDENTIFIER = 128, EXACT_TITLE = 64, TITLE_PREFIX = 32,
        TITLE_SUBSTRING = 16, KEYWORD = 8, THEME_OR_ORGANIZATION = 4, DESCRIPTION = 1;
    private final ByteBuffersDirectory directory;
    private final GermanAnalyzer analyzer;
    private final DirectoryReader reader;
    private final IndexSearcher searcher;
    private final Set<String> ids;
    private final long buildMillis;
    private volatile boolean closed;
    LuceneCatalogSearchIndex(ByteBuffersDirectory directory, GermanAnalyzer analyzer, DirectoryReader reader, Set<String> ids, long buildMillis) {
        this.buildMillis = buildMillis;
        this.directory = directory; this.analyzer = analyzer; this.reader = reader;
        this.searcher = new IndexSearcher(reader); this.ids = ids;
    }
    public long buildMillis() { return buildMillis; }
    public String directoryType() { return directory.getClass().getSimpleName(); }
    public String analyzerType() { return analyzer.getClass().getSimpleName(); }
    public int documentCount() { return reader.numDocs(); }
    public Set<String> ids() { return ids; }
    @Override public boolean available() { return !closed && reader.getRefCount() > 0; }
    @Override public List<SearchHit> search(String text) {
        if (!available()) throw new SearchUnavailableException(null);
        var tokens = SearchText.tokens(text);
        if (tokens.isEmpty()) return List.of(); // The service handles an unfiltered list without a Lucene match-all query.
        try {
            var candidates = new BooleanQuery.Builder();
            var rankings = new ArrayList<Query>();
            String full = SearchText.normalize(text);
            rankings.add(boost(new TermQuery(new Term("identifier", full)), EXACT_IDENTIFIER));
            rankings.add(boost(new TermQuery(new Term("title", full)), EXACT_TITLE));
            rankings.add(boost(new PrefixQuery(new Term("title", full)), TITLE_PREFIX));
            for (String token : tokens) {
                var alternatives = new ArrayList<Query>();
                for (String field : List.of("identifier", "title", "keywords", "synonyms", "theme", "organization")) {
                    Query contains = new WildcardQuery(new Term(field, "*" + token + "*"));
                    alternatives.add(contains);
                    float weight = switch (field) {
                        case "identifier", "title" -> TITLE_SUBSTRING;
                        case "keywords", "synonyms" -> KEYWORD;
                        default -> THEME_OR_ORGANIZATION;
                    };
                    rankings.add(boost(contains, weight));
                }
                try (var stream = analyzer.tokenStream("description", token)) {
                    var term = stream.addAttribute(CharTermAttribute.class);
                    stream.reset();
                    while (stream.incrementToken()) {
                        Query description = new TermQuery(new Term("description", term.toString()));
                        alternatives.add(description); rankings.add(boost(description, DESCRIPTION));
                    }
                    stream.end();
                }
                candidates.add(new DisjunctionMaxQuery(alternatives, 0), BooleanClause.Occur.MUST);
            }
            Query query = new BooleanQuery.Builder().add(candidates.build(), BooleanClause.Occur.FILTER)
                .add(new DisjunctionMaxQuery(rankings, 0), BooleanClause.Occur.MUST).build();
            if (reader.numDocs() == 0) return List.of();
            var found = searcher.search(query, reader.numDocs());
            var hits = new ArrayList<SearchHit>();
            var stored = searcher.storedFields();
            for (var hit : found.scoreDocs) hits.add(new SearchHit(stored.document(hit.doc).get("id"), hit.score));
            return List.copyOf(hits);
        } catch (IOException | AlreadyClosedException error) {
            throw new SearchUnavailableException(error);
        }
    }
    private static Query boost(Query query, float weight) { return new BoostQuery(new ConstantScoreQuery(query), weight); }
    @Override public synchronized void close() {
        if (closed) return;
        closed = true;
        try { org.apache.lucene.util.IOUtils.close(reader, analyzer, directory); }
        catch (IOException error) { throw new SearchUnavailableException(error); }
    }
}
