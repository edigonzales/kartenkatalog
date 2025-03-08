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
WITH parents AS 
(
    SELECT 
        dp.id,
        dp.dtype,
        dp.description,
        dp.remarks,
        dp.title,
        dp.ident_part,
        dp.derived_identifier,
        dp.keywords,
        dp.synonyms,
        ps.display_text,
        dsw.style_server,
        dsw.service_download,
        sa.transparency,
        st.title AS theme_title,
        st.identifier AS theme_ident,
        sou."name" AS org_name,
        json_agg(r."name") FILTER (WHERE r."name" IS NOT NULL) AS permissions
    FROM 
        simi.simiproduct_data_product AS dp 
        LEFT JOIN simi.simiproduct_data_product_pub_scope AS ps 
        ON dp.pub_scope_id = ps.id 
        LEFT JOIN simi.simidata_data_set_view AS dsw 
        ON dsw.id = dp.id
        LEFT JOIN simi.simiproduct_single_actor AS sa  
        ON sa.id = dp.id
        LEFT JOIN simi.simiiam_permission AS perm
        ON perm.data_set_view_id = dsw.id
        LEFT JOIN simi.simiiam_role AS r 
        ON perm.role_id = r.id
        LEFT JOIN simi.simitheme_theme_publication AS stp 
        ON dp.theme_publication_id = stp.id 
        LEFT JOIN simi.simitheme_theme AS st 
        ON stp.theme_id = st.id
        LEFT JOIN simi.simitheme_org_unit AS sou 
        ON st.data_owner_id = sou.id
    WHERE 
        dp.dtype NOT IN ('simiProduct_Map')
        AND
        ps.display_text IN ('WGC, QGIS u. WMS', 'Nur WMS', 'WGC u. QGIS')
    GROUP BY 
        1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16
)
,
children AS 
(
    SELECT 
        lg.id AS lg_id,
        dp.id,
        dp.dtype,
        dp.description,
        dp.remarks,
        dp.title,
        dp.ident_part,
        dp.derived_identifier,
        dp.keywords,
        dp.synonyms,
        ps.display_text,
        dsw.style_server,
        dsw.service_download,
        sa.transparency,
        st.title AS theme_title,
        st.identifier AS theme_ident,
        sou."name" AS org_name,
        json_agg(r."name") FILTER (WHERE r."name" IS NOT NULL) AS permissions
    FROM 
        simi.simiproduct_layer_group AS lg 
        LEFT JOIN simi.simiproduct_properties_in_list AS pil 
        ON lg.id = pil.product_list_id
        LEFT JOIN simi.simiproduct_data_product AS dp 
        ON pil.single_actor_id = dp.id 
        LEFT JOIN simi.simiproduct_data_product_pub_scope AS ps 
        ON dp.pub_scope_id = ps.id 
        LEFT JOIN simi.simidata_data_set_view AS dsw 
        ON dsw.id = dp.id
        LEFT JOIN simi.simiproduct_single_actor AS sa  
        ON sa.id = dp.id
        LEFT JOIN simi.simiiam_permission AS perm
        ON perm.data_set_view_id = dsw.id
        LEFT JOIN simi.simiiam_role AS r 
        ON perm.role_id = r.id
        LEFT JOIN simi.simitheme_theme_publication AS stp 
        ON dp.theme_publication_id = stp.id 
        LEFT JOIN simi.simitheme_theme AS st 
        ON stp.theme_id = st.id
        LEFT JOIN simi.simitheme_org_unit AS sou 
        ON st.data_owner_id = sou.id
    GROUP BY 
        1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17
)
SELECT 
    p.id AS p_id,
    p.dtype AS p_dtype,
    p.description AS p_description,
    p.remarks AS p_remarks,
    p.title AS p_title,
    p.ident_part AS p_ident_part,
    p.derived_identifier AS p_derived_identifier,
    p.keywords AS p_keywords,
    p.synonyms AS p_synonyms,
    p.display_text AS p_display_text,
    p.style_server AS p_style_server,
    p.service_download AS p_service_download,
    p.transparency AS p_transparency,
    p.theme_title AS p_theme_title,
    p.theme_ident AS p_theme_ident,
    p.org_name AS p_org_name,
    p.permissions AS p_permissions,
    c.id AS c_id,
    c.dtype AS c_dtype,
    c.description AS c_description,
    c.remarks AS c_remarks,
    c.title AS c_title,
    c.ident_part AS c_ident_part,
    c.derived_identifier AS c_derived_identifier,
    c.keywords AS c_keywords,
    c.synonyms AS c_synonyms,
    c.display_text AS c_display_text,
    c.style_server AS c_style_server,
    c.service_download AS c_service_download,
    c.transparency AS c_transparency,
    c.theme_title AS c_theme_title,
    c.theme_ident AS c_theme_ident,
    c.org_name AS c_org_name,
    c.permissions AS c_permissions
FROM
    parents AS p 
    LEFT JOIN children AS c
    ON p.id = c.lg_id
--ORDER BY 
 --   p.title, c.title        
            """;
}
