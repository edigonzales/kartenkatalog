package ch.so.agi.maps;

import java.util.List;

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
        List<Product> products = jdbcClient.sql(baseStatement).query(Product.class).list();
        return products;
    }

    private String baseStatement = """
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
    ps.display_text IN ('WGC, QGIS u. WMS', 'Nur WMS', 'WGC u. QGIS')
    AND 
    dp.dtype NOT IN ('simiProduct_Map') 
GROUP BY 
    1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16
ORDER BY
     dp.title          
            """;
}
