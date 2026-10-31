package ch.so.agi.meta.api;

import jakarta.ws.rs.core.*;
import jakarta.ws.rs.ext.*;
import java.util.Map;

@Provider
public class QueryExceptionMapper implements ExceptionMapper<IllegalArgumentException> {
    @Context UriInfo uri;
    @Override public Response toResponse(IllegalArgumentException error) {
        return Response.status(400).type("application/problem+json").entity(Map.of("status", 400,
            "title", "Bad Request", "detail", error.getMessage(), "instance", uri.getRequestUri().getPath())).build();
    }
}
