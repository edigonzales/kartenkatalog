package ch.so.agi.meta.api;

import java.text.Normalizer;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import ch.so.agi.meta.CatalogService;
import ch.so.agi.meta.Ebene;
import ch.so.agi.meta.Thema;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;

import static ch.so.agi.meta.api.CatalogDtos.*;

@Path("/api/v1")
@Produces(MediaType.APPLICATION_JSON)
public class CatalogApiController {
    private final CatalogService catalog;
    public CatalogApiController(CatalogService catalog) { this.catalog = catalog; }

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
                             @QueryParam("thema") String thema) {
        if (q.length() > 200) throw new CatalogProblem(400, "Suchtext darf höchstens 200 Zeichen enthalten");
        var themes = themesById();
        var layers = thema == null ? catalog.findAllEbenen() : catalog.findEbenenByThemaIdentifier(requireTheme(thema).identifier());
        String query = normalize(q.strip());
        return layers.stream().map(e -> Layer.from(e, themeFor(e, themes)))
                .filter(e -> query.isEmpty() || normalize(String.join(" ", e.id(), e.title(),
                        e.description() == null ? "" : e.description(), String.join(" ", e.keywords()), String.join(" ", e.synonyms()))).contains(query))
                .toList();
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
    private static String normalize(String text) {
        return Normalizer.normalize(text, Normalizer.Form.NFD).replaceAll("\\p{M}", "").toLowerCase(Locale.ROOT);
    }
}
