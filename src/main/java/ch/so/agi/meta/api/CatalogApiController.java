package ch.so.agi.meta.api;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import ch.so.agi.meta.CatalogService;
import ch.so.agi.meta.Ebene;
import ch.so.agi.meta.Thema;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;

import ch.so.agi.meta.search.*;
import java.util.Set;
import static ch.so.agi.meta.api.CatalogDtos.*;

@Path("/api/v1")
@Produces(MediaType.APPLICATION_JSON)
public class CatalogApiController {
    private final CatalogService catalog;
    private final CatalogSearchService search;
    public CatalogApiController(CatalogService catalog, CatalogSearchService search) { this.catalog = catalog; this.search = search; }

    @GET @Path("/themen")
    public List<Theme> themes() { return catalog.findAllThemen().stream().map(Theme::from).toList(); }

    @GET @Path("/themen/{identifier}/ebenen")
    public List<Layer> themeLayers(@PathParam("identifier") String identifier) {
        Thema theme = requireTheme(identifier);
        return catalog.findEbenenByThemaIdentifier(identifier).stream().map(e -> Layer.from(e, theme)).toList();
    }

    @GET @Path("/ebenen/{id}")
    public Layer layer(@PathParam("id") String id) {
        Ebene layer = catalog.findEbeneByIdentPart(id).orElseThrow(() -> notFound("Kartenebene", id));
        return Layer.from(layer, themeFor(layer, themesById()));
    }

    @GET @Path("/ebenen")
    public List<Layer> layers(@QueryParam("q") @DefaultValue("") String q,
                             @QueryParam("thema") List<String> thema, @QueryParam("typ") List<String> typ,
                             @QueryParam("organisation") List<String> organisation, @QueryParam("sort") String sort) {
        var themes = themesById();
        return search.search(query(q, thema, typ, organisation, sort)).layers().stream()
            .map(e -> Layer.from(e, themeFor(e, themes))).toList();
    }
    @GET @Path("/facets")
    public CatalogFacetService.Facets facets(@QueryParam("q") @DefaultValue("") String q,
                             @QueryParam("thema") List<String> thema, @QueryParam("typ") List<String> typ,
                             @QueryParam("organisation") List<String> organisation, @QueryParam("sort") String sort) {
        return search.facets(query(q, thema, typ, organisation, sort));
    }
    private static CatalogQuery query(String q, List<String> thema, List<String> typ, List<String> organisation, String sort) {
        return new CatalogQuery(q, Set.copyOf(thema), Set.copyOf(typ), Set.copyOf(organisation), sort);
    }

    @GET @Path("/catalog")
    public Catalog catalog() {
        // The immutable snapshot is the only source; no parsing per request.
        var themes = catalog.findAllThemen();
        var layers = catalog.findAllEbenen().stream().collect(Collectors.groupingBy(Ebene::thema_r));
        return new Catalog(themes.stream().map(t -> new ThemeLayers(Theme.from(t),
                layers.getOrDefault(t.t_id(), List.of()).stream().map(e -> Layer.from(e, t)).toList())).toList());
    }
    private Map<String, Thema> themesById() {
        return catalog.findAllThemen().stream().collect(Collectors.toMap(Thema::t_id, Function.identity()));
    }
    private Thema themeFor(Ebene layer, Map<String, Thema> themes) {
        var theme = themes.get(layer.thema_r());
        if (theme == null) throw new IllegalStateException("Unresolved catalog theme reference");
        return theme;
    }
    private Thema requireTheme(String identifier) {
        return catalog.findThema(identifier).orElseThrow(() -> notFound("Thema", identifier));
    }
    private static CatalogProblem notFound(String kind, String id) {
        return new CatalogProblem(404, kind + " nicht gefunden: " + id);
    }
}
