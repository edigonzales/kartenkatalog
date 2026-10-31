package ch.so.agi.meta.web;

import ch.so.agi.meta.*;
import ch.so.agi.meta.api.CatalogProblem;
import ch.so.agi.meta.search.CatalogQuery;
import io.quarkus.qute.*;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import static ch.so.agi.meta.web.CatalogViewModels.*;

@Path("/")
@Produces(MediaType.TEXT_HTML)
public class CatalogPages {
    private final CatalogService catalog;
    private final CatalogPageVmFactory views;
    public CatalogPages(CatalogService catalog, CatalogPageVmFactory views) { this.catalog=catalog;this.views=views; }
    @CheckedTemplate(basePath="")
    public static class Templates {
        public static native TemplateInstance catalog(CatalogPage page);
        public static native TemplateInstance theme(ThemePage page);
        public static native TemplateInstance layers(List<LayerRow> layers);
        public static native TemplateInstance detail(Detail product);
    }
    @GET public TemplateInstance home(@QueryParam("q") @DefaultValue("") String q,
        @QueryParam("thema") List<String> themes,@QueryParam("typ") List<String> types,@QueryParam("organisation") List<String> orgs,@QueryParam("sort") String sort) {
        return Templates.catalog(views.catalog(query(q,themes,types,orgs,sort)));
    }
    @GET @Path("thema") public TemplateInstance themes(@QueryParam("q") @DefaultValue("") String q,
        @QueryParam("thema") List<String> themes,@QueryParam("typ") List<String> types,@QueryParam("organisation") List<String> orgs,@QueryParam("sort") String sort) {
        return home(q,themes,types,orgs,sort);
    }
    @GET @Path("themen/{identifier}/ebenen")
    public Response theme(@PathParam("identifier") String identifier,@HeaderParam("HX-Request") String hx,
        @QueryParam("q") @DefaultValue("") String q,@QueryParam("typ") List<String> types,
        @QueryParam("organisation") List<String> orgs,@QueryParam("sort") String sort) {
        var page=views.theme(identifier,query(q,List.of(),types,orgs,sort));
        return Response.ok("true".equalsIgnoreCase(hx)?Templates.layers(page.layers()):Templates.theme(page))
            .header("Vary","HX-Request").build();
    }
    @GET @Path("ebene/{id}") public TemplateInstance detail(@PathParam("id") String id) { return Templates.detail(views.detail(id)); }
    @GET @Path("ebene/{id}/style") @Produces(MediaType.APPLICATION_XML)
    public Response style(@PathParam("id") String id) {
        var e=catalog.findEbeneByIdentPart(id).orElseThrow(()->new CatalogProblem(404,"Kartenebene nicht gefunden"));
        if(e.style_server()==null||e.style_server().isBlank())throw new CatalogProblem(404,"Kein QML vorhanden");
        return Response.ok(e.style_server().getBytes(StandardCharsets.UTF_8)).type(MediaType.APPLICATION_XML)
            .header("Content-Disposition","attachment; filename*=UTF-8''"+CatalogUrlFactory.encoded(e.ident_part()+".qml")).build();
    }
    @GET @Path("ping") @Produces(MediaType.TEXT_PLAIN) public String ping(){return "kartenkatalog";}
    private static CatalogQuery query(String q,List<String> themes,List<String> types,List<String> orgs,String sort) {
        // A blank option in the simple GET form means no restriction; the API contract remains unchanged.
        return new CatalogQuery(q,selected(themes),selected(types),selected(orgs),sort);
    }
    private static Set<String> selected(List<String> values){return values==null?Set.of():values.stream().filter(v->!v.isBlank()).collect(java.util.stream.Collectors.toUnmodifiableSet());}
}
