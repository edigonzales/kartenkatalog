package ch.so.agi.meta.api;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import jakarta.ws.rs.ext.*;
import java.util.Map;

@Provider
public class ProblemExceptionMapper implements ExceptionMapper<WebApplicationException> {
    @Context UriInfo uri;
    @Override public Response toResponse(WebApplicationException error) {
        int status = error.getResponse().getStatus();
        String title = error.getResponse().getStatusInfo().getReasonPhrase();
        String detail = error instanceof CatalogProblem ? error.getMessage() : title;
        return Response.status(status).type("application/problem+json").entity(Map.of(
            "status", status, "title", title, "detail", detail, "instance", uri.getRequestUri().getPath())).build();
    }
}
