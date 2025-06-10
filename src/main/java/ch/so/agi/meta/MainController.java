package ch.so.agi.meta;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import ch.so.agi.meta.Product;
import ch.so.agi.meta.ProductService;
import jakarta.servlet.http.HttpServletRequest;

@Controller
public class MainController {
    private Logger log = LoggerFactory.getLogger(this.getClass());

    private ProductService productService;

    public MainController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("name", "Stefan");
        return "index";
    }

    @GetMapping("/ping")
    public ResponseEntity<String> ping(@RequestHeader Map<String, String> headers, HttpServletRequest request) {
        headers.forEach((key, value) -> {
            log.info(String.format("Header '%s' = %s", key, value));
        });
        log.info("server name: " + request.getServerName());
        log.info("context path: " + request.getContextPath());
        log.info("ping"); 
        return new ResponseEntity<String>("kartenkatalog", HttpStatus.OK);
    }
    
    
    @GetMapping("/product2")
    public ModelAndView product2(@RequestHeader Map<String, String> headers, HttpServletRequest request) {
        headers.forEach((key, value) -> {
            log.info(String.format("Header '%s' = %s", key, value));
        });
        
        log.info("server name: " + request.getServerName());
        log.info("context path: " + request.getContextPath());
        
        List<Product> products = productService.findAll();
        //log.info("{}", products);
        ModelAndView mav = new ModelAndView("product2");
        mav.addObject("productList", products);
        return mav;
    }
    
    @GetMapping("/product")
    public ModelAndView products(@RequestHeader Map<String, String> headers, HttpServletRequest request) {
        headers.forEach((key, value) -> {
            log.info(String.format("Header '%s' = %s", key, value));
        });
        
        log.info("server name: " + request.getServerName());
        log.info("context path: " + request.getContextPath());
        
        List<Product> products = productService.findAll();
        //log.info("{}", products);
        ModelAndView mav = new ModelAndView("product");
        mav.addObject("productList", products);
        return mav;
    }
    
    @GetMapping("/product/{id}")
    public ModelAndView product(@PathVariable(name = "id") String id) {
        List<Product> products = productService.findById(id);
        
        // Aufgrund des Datenmodelles können mehrere Objekte zurückgeliefert werden.
        // Dies kann jedoch nur bei Layergruppen der Fall sein, nicht bei Singlelayer.
        // Falls ein Sublayer einer Layergruppe requested wurde, gibt es nur
        // ein Objekt.
        Product requestedProduct = null;
        for (var product : products) {
            if (id.equalsIgnoreCase(product.ident_part())) {
                requestedProduct = product;
                break;
            } else {
                for (var child : product.children()) {
                    if (id.equalsIgnoreCase(child.ident_part())) {
                        requestedProduct = child;
                        break;
                    }
                }
            }
        }
        
        log.info(requestedProduct.toString());
        
        ModelAndView mav = new ModelAndView("product_detail");
        mav.addObject("product", requestedProduct);
        mav.addObject("parent_id", products.get(0).ident_part());
        return mav;
    }
    
    @PostMapping(value = "/download-string-form-post", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity<byte[]> downloadStringFormPost(@RequestParam("content") String content) {
        String filename = "my_form_downloaded_string.txt";
        MediaType mediaType = MediaType.TEXT_PLAIN;
        
        byte[] contentBytes = content.getBytes(StandardCharsets.UTF_8);
//        HttpHeaders headers = new HttpHeaders();
//        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
//        headers.setContentDisposition(ContentDisposition.builder("attachment").filename("foo.qml").build());

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=generated.pdf");
        headers.add("Cache-Control", "no-cache, no-store, must-revalidate");
        headers.add("Pragma", "no-cache");
        headers.add("Expires", "0");

        return ResponseEntity.ok()
                .headers(headers)
                .contentLength(contentBytes.length)
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(contentBytes);
    }    
    
    
}
