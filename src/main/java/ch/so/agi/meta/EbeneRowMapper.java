package ch.so.agi.meta;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

public class EbeneRowMapper implements RowMapper<Ebene> {
    @Override
    public Ebene mapRow(ResultSet rs, int rowNum) throws SQLException {
        return new Ebene(
                rs.getString("t_id"),
                rs.getString("dtype"),
                rs.getString("adescription"),
                rs.getString("description_override"),
                rs.getString("description_model"),
                rs.getString("remarks"),
                rs.getString("title"),
                rs.getString("ident_part"),
                rs.getString("derived_identifier"),
                rs.getString("keywords"),
                rs.getString("synonyms"),
                rs.getString("display_text"),
                rs.getString("style_server"),
                rs.getObject("service_download") != null ? rs.getBoolean("service_download") : null,
                rs.getObject("transparency") != null ? rs.getInt("transparency") : null,
                rs.getString("theme_title"),
                rs.getString("theme_ident"),
                rs.getString("org_name"),
                rs.getString("permissions"),
                rs.getString("thema_r")
        );
    }
}
