package ch.so.agi.maps;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.servlet.ModelAndView;

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
    
    
    @GetMapping("/product")
    public ModelAndView product(@RequestHeader Map<String, String> headers, HttpServletRequest request) {
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
    
    
    
}
