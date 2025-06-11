package ch.so.agi.meta;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
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
    public ModelAndView showProducts(@RequestHeader Map<String, String> headers, HttpServletRequest request) {
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
    public ModelAndView showProductDetail(@PathVariable(name = "id") String id) {
        Product product = productService.findById(id);
        
        ModelAndView mav = new ModelAndView("productdetail");
        mav.addObject("product", product);
        return mav;
    }
    
    @GetMapping(value = "/product/{id}/style")
    public ResponseEntity<?> downloadStyle(@PathVariable(name = "id") String id) {
        Product product = productService.findById(id);

        byte[] contentBytes = product.style_server().getBytes(StandardCharsets.UTF_8);

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename="+id+".qml");
        headers.setContentType(MediaType.TEXT_XML);

        return new ResponseEntity<>(contentBytes, headers, HttpStatus.OK);
    }    
        
    
}
