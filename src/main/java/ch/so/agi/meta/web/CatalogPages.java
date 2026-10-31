package ch.so.agi.meta.web;

import ch.so.agi.meta.*;
import ch.so.agi.meta.api.CatalogProblem;
import io.quarkus.qute.*;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Path("/")
@Produces(MediaType.TEXT_HTML)
public class CatalogPages {
    private final CatalogService catalog;
    public CatalogPages(CatalogService catalog) { this.catalog = catalog; }

    public record ThemeVm(String identifier, String title, int count, String href) {}
    public record LayerVm(String title, String href, String mapUrl, RawString description) {}
    public record DetailVm(String title, String id, String type, String mapUrl, String styleUrl, RawString description) {}
    public record ThemePageVm(String title, List<LayerVm> layers) {}

    @CheckedTemplate(basePath = "")
    public static class Templates {
        public static native TemplateInstance catalog(List<ThemeVm> themes);
        public static native TemplateInstance theme(ThemePageVm page);
        public static native TemplateInstance layers(List<LayerVm> layers);
        public static native TemplateInstance detail(DetailVm product);
    }
    @GET public Response home() { return Response.status(302).location(java.net.URI.create("/thema")).build(); }
    @GET @Path("thema") public TemplateInstance themes() {
        return Templates.catalog(catalog.findAllThemen().stream().map(t -> new ThemeVm(t.identifier(), t.title(), t.ebenen_count(),
            "/themen/" + encoded(t.identifier()) + "/ebenen")).toList());
    }
    @GET @Path("themen/{identifier}/ebenen")
    public TemplateInstance theme(@PathParam("identifier") String identifier, @HeaderParam("HX-Request") String hx) {
        var theme = catalog.findThema(identifier).orElseThrow(() -> new CatalogProblem(404, "Thema nicht gefunden"));
        var layers = catalog.findEbenenByThemaIdentifier(identifier).stream().map(e -> new LayerVm(e.title(),
            "/ebene/" + encoded(e.ident_part()), mapUrl(e), safeDescription(e))).toList();
        return "true".equalsIgnoreCase(hx) ? Templates.layers(layers) : Templates.theme(new ThemePageVm(theme.title(), layers));
    }
    @GET @Path("ebene/{id}") public TemplateInstance detail(@PathParam("id") String id) {
        var e = requireLayer(id);
        return Templates.detail(new DetailVm(e.title(), e.ident_part(), e.dtype(), mapUrl(e),
            e.style_server() == null ? null : "/ebene/" + encoded(e.ident_part()) + "/style", safeDescription(e)));
    }
    @GET @Path("ebene/{id}/style") @Produces(MediaType.APPLICATION_XML)
    public Response style(@PathParam("id") String id) {
        var e = requireLayer(id);
        if (e.style_server() == null || e.style_server().isBlank()) throw new CatalogProblem(404, "Kein QML vorhanden");
        // RFC 5987: encode metadata as a parameter, never concatenate it into a quoted header.
        return Response.ok(e.style_server().getBytes(StandardCharsets.UTF_8)).type(MediaType.APPLICATION_XML)
            .header("Content-Disposition", "attachment; filename*=UTF-8''" + encoded(e.ident_part() + ".qml")).build();
    }
    @GET @Path("ping") @Produces(MediaType.TEXT_PLAIN) public String ping() { return "kartenkatalog"; }
    private Ebene requireLayer(String id) {
        return catalog.findEbeneByIdentPart(id).orElseThrow(() -> new CatalogProblem(404, "Kartenebene nicht gefunden"));
    }
    private static RawString safeDescription(Ebene e) {
        String sanitized = CatalogText.html(CatalogText.description(e));
        return new RawString(sanitized == null ? "" : sanitized);
    }
    static String encoded(String value) { return URLEncoder.encode(value, StandardCharsets.UTF_8).replace("+", "%20"); }
    static String mapUrl(Ebene e) {
        String value = e.ident_part() + (e.transparency() == null || e.transparency() == 0 ? "" : "[" + e.transparency() + "]");
        return "https://geo.so.ch/map?l=" + encoded(value);
    }
}
