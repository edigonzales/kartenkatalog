package ch.so.agi.maps;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Service;

@Service
public class ProductService {
    private Logger log = LoggerFactory.getLogger(this.getClass());

    private final JdbcClient jdbcClient;

    public ProductService(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    public List<Product> findAll() {        
//        List<Product> products = jdbcClient.sql(baseStatement).query(Product.class).list();
        
        System.out.println(System.currentTimeMillis());

        List<Product> productList = jdbcClient.sql(baseStatement)
                .query(new ProductRowMapper())
                .list();
        System.out.println(System.currentTimeMillis());
        
        //log.info("{}", productList);

        Map<String, List<Product>> childrenMap = new HashMap<>();
        Map<String, Product> parentMap = new HashMap<>();
        
        System.out.println(System.currentTimeMillis());

        // Process rows into parent-child hierarchy
        for (Product product : productList) {
            parentMap.putIfAbsent(product.id(), product); // Store parent

            // Ensure children list exists
            childrenMap.computeIfAbsent(product.id(), k -> new ArrayList<>());

            // Add children if present
            for (Product child : product.children()) {
                childrenMap.get(product.id()).add(child);
            }
        }

        // Build final list with sorted parents and assigned children
        List<Product> finalList = parentMap.values().stream()
                .sorted(Comparator.comparing(Product::title)) // Sort parents by title
                .map(parent -> new Product(
                    parent.id(), parent.dtype(), parent.description(), parent.remarks(),
                    parent.title(), parent.ident_part(), parent.derived_identifier(),
                    parent.keywords(), parent.synonyms(), parent.display_text(),
                    parent.style_server(), parent.service_download(),
                    parent.transparency(), parent.theme_title(), parent.theme_ident(),
                    parent.org_name(), parent.permissions(),
                    childrenMap.getOrDefault(parent.id(), List.of())
                        .stream()
                        .sorted(Comparator.comparing(Product::title)) // Sort children by title
                        .toArray(Product[]::new)
                ))
                .collect(Collectors.toList());


        System.out.println(System.currentTimeMillis());
        System.out.println("***********");
        
        return finalList; 
    }

    private String baseStatement = """
SELECT 
    *
FROM
    agi_kartenkatalog_pub_v1.kartenkatalog_produkt_mutter_kind
            """;
}
