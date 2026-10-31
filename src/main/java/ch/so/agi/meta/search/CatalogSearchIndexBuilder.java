package ch.so.agi.meta.search;

import ch.so.agi.meta.*;
import java.util.*;
import org.apache.lucene.analysis.de.GermanAnalyzer;
import org.apache.lucene.index.*;
import org.apache.lucene.store.ByteBuffersDirectory;

public final class CatalogSearchIndexBuilder {
    public LuceneCatalogSearchIndex build(List<Ebene> layers, Map<String, Thema> themesByTid) {
        long start = System.nanoTime();
        var directory = new ByteBuffersDirectory();
        GermanAnalyzer analyzer = null;
        DirectoryReader reader = null;
        try {
            // Preserve every AND token, including German stopwords; keep German normalization/stemming.
            analyzer = new GermanAnalyzer(org.apache.lucene.analysis.CharArraySet.EMPTY_SET);
            var ids = new HashSet<String>();
            try (var writer = new IndexWriter(directory, new IndexWriterConfig(analyzer))) {
                for (var layer : layers) {
                    if (!ids.add(layer.ident_part())) throw new IllegalArgumentException("Duplicate index ID: " + layer.ident_part());
                    var theme = themesByTid.get(layer.thema_r());
                    if (theme == null) throw new IllegalArgumentException("Unresolved index theme: " + layer.ident_part());
                    writer.addDocument(CatalogDocumentMapper.toDocument(layer, theme));
                }
            }
            reader = DirectoryReader.open(directory);
            if (reader.numDocs() != layers.size()) throw new IllegalStateException("Incomplete catalog index");
            return new LuceneCatalogSearchIndex(directory, analyzer, reader, Set.copyOf(ids), (System.nanoTime() - start) / 1_000_000);
        } catch (Exception error) {
            for (AutoCloseable resource : new AutoCloseable[]{reader, analyzer, directory}) {
                if (resource != null) try { resource.close(); } catch (Exception close) { error.addSuppressed(close); }
            }
            throw new IllegalStateException("Cannot build catalog RAM index", error);
        }
    }
}
