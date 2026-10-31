package ch.so.agi.meta;

import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.health.*;

@Readiness
@ApplicationScoped
public class CatalogReadiness implements HealthCheck {
    private final CatalogStore store;
    public CatalogReadiness(CatalogStore store) { this.store = store; }
    @Override public HealthCheckResponse call() {
        return HealthCheckResponse.named("catalog-snapshot").status(store.current().searchIndex().available()).build();
    }
}
