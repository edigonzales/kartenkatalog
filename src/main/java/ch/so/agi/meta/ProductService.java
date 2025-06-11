package ch.so.agi.meta;

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

import ch.so.agi.meta.Product;
import ch.so.agi.meta.ProductRowMapper;

@Service
public class ProductService {
    private Logger log = LoggerFactory.getLogger(this.getClass());

    private final JdbcClient jdbcClient;

    public ProductService(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    public Product findById(String id) {
        String whereStatement = baseStatement + " WHERE p_ident_part = :id OR c_ident_part = :id";
        
        List<Product> products = jdbcClient.sql(whereStatement)
                .param("id", id)
                .query(new ProductRowMapper())
                .list();
        
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
        
        return requestedProduct;
    }
    
    public List<Product> findAll() {        
        List<Product> productList = jdbcClient.sql(baseStatement)
                .query(new ProductRowMapper())
                .list();

        Map<String, List<Product>> childrenMap = new HashMap<>();
        Map<String, Product> parentMap = new HashMap<>();

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
                    parent.id(), parent.parent_title(), parent.parent_ident_part(), parent.dtype(), parent.description(), parent.description_override(), parent.description_model(), parent.remarks(),
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

        return finalList; 
    }

    private String baseStatement = """
SELECT 
    *
FROM
    agi_kartenkatalog_pub_v1.kartenkatalog_produkt_mutter_kind
            """;
}
