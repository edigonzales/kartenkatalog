package ch.so.agi.meta;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.servlet.ModelAndView;

import jakarta.servlet.http.HttpServletRequest;

@Controller
public class MainController {
    private Logger log = LoggerFactory.getLogger(this.getClass());

    private final CatalogService catalogService;

    public MainController(CatalogService catalogService) {
        this.catalogService = catalogService;
    }

    @GetMapping("/")
    public String home(Model model) {
        return "redirect:/thema";
    }

    @GetMapping("/ping")
    public ResponseEntity<String> ping(@RequestHeader Map<String, String> headers, HttpServletRequest request) {
        headers.forEach((key, value) -> {
            log.info(String.format("Header '%s' = %s", key, value));
        });
        log.info("server name: " + request.getServerName());
        log.info("context path: " + request.getContextPath());
        log.info("ping");
        return new ResponseEntity<>("kartenkatalog", HttpStatus.OK);
    }

    @GetMapping("/thema")
    public ModelAndView showThemen(@RequestHeader Map<String, String> headers, HttpServletRequest request) {
        headers.forEach((key, value) -> log.info(String.format("Header '%s' = %s", key, value)));

        log.info("server name: " + request.getServerName());
        log.info("context path: " + request.getContextPath());

        List<Thema> themen = catalogService.findAllThemen();
        ModelAndView mav = new ModelAndView("thema2");
        mav.addObject("themen", themen);
        return mav;
    }

    @GetMapping("/themen/{identifier}/ebenen")
    public ModelAndView loadEbenen(@PathVariable(name = "identifier") String identifier) {
        List<Ebene> ebenen = catalogService.findEbenenByThemaIdentifier(identifier);
        String domIdentifier = sanitizeIdentifier(identifier);
        
        log.info("Anzahl Ebenen <{}>: {}", identifier, ebenen.size());

        ModelAndView mav = new ModelAndView("theme-layers");
        mav.addObject("ebenen", ebenen);
        mav.addObject("identifier", identifier);
        mav.addObject("domIdentifier", domIdentifier);
        return mav;
    }

    @GetMapping("/ebene/{id}")
    public ModelAndView showEbeneDetail(@PathVariable(name = "id") String id) {
        Ebene ebene = catalogService.findEbeneByIdentPart(id).orElseThrow();

        ModelAndView mav = new ModelAndView("productdetail");
        mav.addObject("product", ebene);
        return mav;
    }

    @GetMapping(value = "/ebene/{id}/style")
    public ResponseEntity<?> downloadStyle(@PathVariable(name = "id") String id) {
        Ebene ebene = catalogService.findEbeneByIdentPart(id).orElseThrow();

        byte[] contentBytes = ebene.style_server().getBytes(StandardCharsets.UTF_8);

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + id + ".qml");
        headers.setContentType(MediaType.TEXT_XML);

        return new ResponseEntity<>(contentBytes, headers, HttpStatus.OK);
    }

    private String sanitizeIdentifier(String identifier) {
        return identifier.replaceAll("[^A-Za-z0-9_-]", "_");
    }
}
