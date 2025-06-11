package ch.so.agi.meta;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.springframework.jdbc.core.RowMapper;

import ch.so.agi.meta.Product;

public class ProductRowMapper implements RowMapper<Product> {

    @Override
    public Product mapRow(ResultSet rs, int rowNum) throws SQLException {
        String id = rs.getString("p_id");
        String parent_title = null;
        String parent_ident_part = null;
        String dtype = rs.getString("p_dtype");
        String description = rs.getString("p_description");
        String description_override = rs.getString("p_description_override");
        String description_model = rs.getString("p_description_model");
        String remarks = rs.getString("p_remarks");
        String title = rs.getString("p_title");
        String identPart = rs.getString("p_ident_part");
        String derivedIdentifier = rs.getString("p_derived_identifier");
        String keywords = rs.getString("p_keywords");
        String synonyms = rs.getString("p_synonyms");
        String displayText = rs.getString("p_display_text");
        String styleServer = rs.getString("p_style_server");
        Boolean serviceDownload = rs.getBoolean("p_service_download");
        String transparency = rs.getString("p_transparency");
        String themeTitle = rs.getString("p_theme_title");
        String themeIdent = rs.getString("p_theme_ident");
        String orgName = rs.getString("p_org_name");
        String permissions = rs.getString("p_permissions");

        String cId = rs.getString("c_id");
        Product child = (cId != null) ? new Product(
                cId,
                title,
                identPart,
                rs.getString("c_dtype"),
                rs.getString("c_description"),
                rs.getString("c_description_override"),
                rs.getString("c_description_model"),
                rs.getString("c_remarks"),
                rs.getString("c_title"),
                rs.getString("c_ident_part"),
                rs.getString("c_derived_identifier"),
                rs.getString("c_keywords"),
                rs.getString("c_synonyms"),
                rs.getString("c_display_text"),
                rs.getString("c_style_server"),
                rs.getBoolean("c_service_download"),
                rs.getString("c_transparency"),
                rs.getString("c_theme_title"),
                rs.getString("c_theme_ident"),
                rs.getString("c_org_name"),
                rs.getString("c_permissions"),
                new Product[0]
        ) : null;

        return new Product(
                id, parent_title, parent_ident_part, dtype, description, description_override, description_model, remarks, title, identPart, derivedIdentifier,
                keywords, synonyms, displayText, styleServer, serviceDownload,
                transparency, themeTitle, themeIdent, orgName, permissions,
                (child != null) ? new Product[]{child} : new Product[0]
        );
    }

}
