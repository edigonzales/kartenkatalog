package ch.so.agi.meta;

import java.util.List;
import java.util.Optional;

import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Repository
@Profile("!mock")
public class JdbcCatalogRepository implements CatalogRepository {
    private final JdbcClient jdbcClient;

    public JdbcCatalogRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    @Override
    public List<Thema> findAllThemen() {
        String sql = """
                SELECT
                    t_id,
                    identifier,
                    title,
                    adescription,
                    keywords,
                    synonyms,
                    further_info_url,
                    data_owner
                FROM agi_kartenkatalog_v2.kartenkatalog_thema
                ORDER BY title
                """;

        return jdbcClient.sql(sql)
                .query(Thema.class)
                .list();
    }

    @Override
    public List<Ebene> findEbenenByThemaIdentifier(String identifier) {
        String sql = """
                SELECT
                    e.t_id,
                    e.id,
                    e.dtype,
                    e.adescription,
                    e.description_override,
                    e.description_model,
                    e.remarks,
                    e.title,
                    e.ident_part,
                    e.derived_identifier,
                    e.keywords,
                    e.synonyms,
                    e.display_text,
                    e.style_server,
                    e.service_download,
                    e.transparency,
                    e.theme_title,
                    e.theme_ident,
                    e.org_name,
                    e.permissions,
                    e.thema_r
                FROM agi_kartenkatalog_v2.kartenkatalog_ebene e
                JOIN agi_kartenkatalog_v2.kartenkatalog_thema t ON e.thema_r = t.t_id
                WHERE t.identifier = :identifier
                ORDER BY e.title
                """;
        
        return jdbcClient.sql(sql)
                .param("identifier", identifier)
                .query(Ebene.class)
                .list();
    }

    @Override
    public Optional<Ebene> findEbeneByIdentPart(String identPart) {
        String sql = """
                SELECT
                    t_id,
                    id,
                    dtype,
                    adescription,
                    description_override,
                    description_model,
                    remarks,
                    title,
                    ident_part,
                    derived_identifier,
                    keywords,
                    synonyms,
                    display_text,
                    style_server,
                    service_download,
                    transparency,
                    theme_title,
                    theme_ident,
                    org_name,
                    permissions,
                    thema_r
                FROM agi_kartenkatalog_v2.kartenkatalog_ebene
                WHERE ident_part = :identPart
                """;

        return jdbcClient.sql(sql)
                .param("identPart", identPart)
                .query(Ebene.class)
                .optional();
    }
}
