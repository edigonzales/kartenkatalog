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
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import static ch.so.agi.meta.api.CatalogDtos.*;

@RestController
@RequestMapping(value = "/api/v1", produces = "application/json")
public class CatalogApiController {
    private final CatalogService catalog;
    public CatalogApiController(CatalogService catalog) { this.catalog = catalog; }

    @GetMapping("/themen")
    public List<Theme> themes() { return catalog.findAllThemen().stream().map(Theme::from).toList(); }

    @GetMapping("/themen/{identifier}/ebenen")
    public List<Layer> themeLayers(@PathVariable String identifier) {
        Thema theme = requireTheme(identifier);
        return catalog.findEbenenByThemaIdentifier(identifier).stream().map(e -> Layer.from(e, theme)).toList();
    }

    @GetMapping("/ebenen/{id}")
    public Layer layer(@PathVariable String id) {
        Ebene layer = catalog.findEbeneByIdentPart(id).orElseThrow(() -> notFound("Kartenebene", id));
        return Layer.from(layer, themeFor(layer, themesById()));
    }

    @GetMapping("/ebenen")
    public List<Layer> layers(@RequestParam(defaultValue = "") String q,
                             @RequestParam(required = false) String thema) {
        if (q.length() > 200) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Suchtext darf höchstens 200 Zeichen enthalten");
        var themes = themesById();
        var layers = thema == null ? catalog.findAllEbenen() : catalog.findEbenenByThemaIdentifier(requireTheme(thema).identifier());
        String query = normalize(q.strip());
        return layers.stream().map(e -> Layer.from(e, themeFor(e, themes)))
                .filter(e -> query.isEmpty() || normalize(String.join(" ", e.id(), e.title(),
                        e.description() == null ? "" : e.description(), String.join(" ", e.keywords()), String.join(" ", e.synonyms()))).contains(query))
                .toList();
    }

    @GetMapping("/catalog")
    public Catalog catalog() {
        // Two repository reads, rather than one additional SQL query per theme.
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
    private static ResponseStatusException notFound(String kind, String id) {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, kind + " nicht gefunden: " + id);
    }
    private static String normalize(String text) {
        return Normalizer.normalize(text, Normalizer.Form.NFD).replaceAll("\\p{M}", "").toLowerCase(Locale.ROOT);
    }
}
