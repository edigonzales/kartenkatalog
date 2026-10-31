package ch.so.agi.meta;

import java.nio.charset.StandardCharsets;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class MainController {
    private final CatalogService catalogService;
    public MainController(CatalogService catalogService) { this.catalogService = catalogService; }

    @GetMapping("/")
    public String home() { return "redirect:/thema"; }
    @GetMapping("/ping")
    public ResponseEntity<String> ping() { return ResponseEntity.ok("kartenkatalog"); }
    @GetMapping("/thema")
    public ModelAndView showThemen() { return new ModelAndView("thema", "themen", catalogService.findAllThemen()); }
    @GetMapping("/themen/{identifier}/ebenen")
    public ModelAndView loadEbenen(@PathVariable String identifier) {
        catalogService.findThema(identifier).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Thema nicht gefunden"));
        return new ModelAndView("theme-layers", "ebenen", catalogService.findEbenenByThemaIdentifier(identifier));
    }
    @GetMapping("/ebene/{id}")
    public ModelAndView showEbeneDetail(@PathVariable String id) { return new ModelAndView("productdetail", "product", requireLayer(id)); }
    @GetMapping("/ebene/{id}/style")
    public ResponseEntity<byte[]> downloadStyle(@PathVariable String id) {
        String style = requireLayer(id).style_server();
        if (style == null || style.isBlank()) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Kein QML vorhanden");
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_XML)
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment().filename(id + ".qml", StandardCharsets.UTF_8).build().toString())
                .body(style.getBytes(StandardCharsets.UTF_8));
    }
    private Ebene requireLayer(String id) {
        return catalogService.findEbeneByIdentPart(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Kartenebene nicht gefunden"));
    }
}
