package ch.so.agi.meta.api;

import jakarta.ws.rs.WebApplicationException;

/** Public, deliberately selected error detail; never a parser or stack trace. */
public final class CatalogProblem extends WebApplicationException {
    public CatalogProblem(int status, String detail) { super(detail, status); }
}
