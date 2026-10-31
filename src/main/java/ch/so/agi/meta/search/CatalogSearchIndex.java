package ch.so.agi.meta.search;
import java.util.List;
public interface CatalogSearchIndex extends AutoCloseable {
    List<SearchHit> search(String text);
    boolean available();
    @Override void close();
}
