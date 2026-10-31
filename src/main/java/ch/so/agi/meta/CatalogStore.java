package ch.so.agi.meta;

import java.util.*;
import io.quarkus.runtime.Startup;
import jakarta.enterprise.context.ApplicationScoped;
import org.jboss.logging.Logger;

/** Eager construction: Quarkus must abort startup before serving a partial catalog. */
@Startup
@ApplicationScoped
public class CatalogStore implements CatalogRepository {
    private final CatalogSnapshot snapshot;
    public CatalogStore(CatalogConfig config) {
        long start = System.nanoTime();
        try (var input = new CatalogXtfSource(config.xtfLocation()).open()) {
            snapshot = new XtfCatalogParser().parse(input);
        } catch (Exception error) {
            throw new IllegalStateException("Cannot load catalog XTF: " + config.xtfLocation(), error);
        }
        Logger.getLogger(CatalogStore.class).infof("Catalog loaded: %d themes, %d layers in %d ms from %s",
            snapshot.findAllThemen().size(), snapshot.findAllEbenen().size(), (System.nanoTime() - start) / 1_000_000, config.xtfLocation());
    }
    public CatalogSnapshot current() { return snapshot; }
    @Override public List<Thema> findAllThemen() { return snapshot.findAllThemen(); }
    @Override public List<Ebene> findAllEbenen() { return snapshot.findAllEbenen(); }
    @Override public Optional<Thema> findThema(String id) { return snapshot.findThema(id); }
    @Override public Optional<Ebene> findEbeneByIdentPart(String id) { return snapshot.findEbeneByIdentPart(id); }
    @Override public List<Ebene> findEbenenByThemaIdentifier(String id) { return snapshot.findEbenenByThemaIdentifier(id); }
}
