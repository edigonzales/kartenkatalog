package ch.so.agi.meta.api;

import ch.so.agi.meta.search.SearchUnavailableException;
import jakarta.ws.rs.core.*;
import jakarta.ws.rs.ext.*;
import java.util.Map;

@Provider
public class SearchExceptionMapper implements ExceptionMapper<SearchUnavailableException> {
    @Context UriInfo uri;
    @Override public Response toResponse(SearchUnavailableException error) {
        return Response.status(503).type("application/problem+json").entity(Map.of("status", 503,
            "title", "Service Unavailable", "detail", "Katalogsuche ist nicht verfügbar", "instance", uri.getRequestUri().getPath())).build();
    }
}
