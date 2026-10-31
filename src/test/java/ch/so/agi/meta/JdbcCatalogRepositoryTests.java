package ch.so.agi.meta;

import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.jdbc.core.simple.JdbcClient;
import static org.junit.jupiter.api.Assertions.*;

class JdbcCatalogRepositoryTests {
    @Test void mapsRecordsCountsEmptyThemesAndBindsIdentifiers() {
        var source = new DriverManagerDataSource("jdbc:h2:mem:" + UUID.randomUUID() + ";MODE=PostgreSQL;DB_CLOSE_DELAY=-1", "sa", "");
        var jdbc = JdbcClient.create(source);
        jdbc.sql("CREATE SCHEMA agi_kartenkatalog_v2").update();
        jdbc.sql("""
            CREATE TABLE agi_kartenkatalog_v2.kartenkatalog_thema (
                t_id BIGINT PRIMARY KEY, identifier VARCHAR, title VARCHAR, adescription VARCHAR,
                keywords VARCHAR, synonyms VARCHAR, further_info_url VARCHAR, data_owner VARCHAR)
            """).update();
        jdbc.sql("""
            CREATE TABLE agi_kartenkatalog_v2.kartenkatalog_ebene (
                t_id BIGINT PRIMARY KEY, id VARCHAR, dtype VARCHAR, adescription VARCHAR,
                description_override VARCHAR, description_model VARCHAR, remarks VARCHAR, title VARCHAR,
                ident_part VARCHAR, derived_identifier VARCHAR, keywords VARCHAR, synonyms VARCHAR,
                display_text VARCHAR, style_server VARCHAR, service_download BOOLEAN, transparency INTEGER,
                theme_title VARCHAR, theme_ident VARCHAR, org_name VARCHAR, permissions VARCHAR, thema_r BIGINT)
            """).update();
        jdbc.sql("INSERT INTO agi_kartenkatalog_v2.kartenkatalog_thema(t_id, identifier, title) VALUES (1, 'thema', 'Thema'), (2, 'empty', 'Leer')").update();
        jdbc.sql("""
            INSERT INTO agi_kartenkatalog_v2.kartenkatalog_ebene(t_id,id,title,ident_part,thema_r,transparency)
            VALUES (100,'source-id','Gemeindegrenzen','ch.so.agi.gemeindegrenzen',1,30)
            """).update();
        var repository = new JdbcCatalogRepository(jdbc);
        assertEquals(1, repository.findAllEbenen().size());
        assertEquals(1, repository.findAllThemen().stream().filter(t -> t.identifier().equals("thema")).findFirst().orElseThrow().ebenen_count());
        assertEquals(0, repository.findAllThemen().stream().filter(t -> t.identifier().equals("empty")).findFirst().orElseThrow().ebenen_count());
        var layer = repository.findEbenenByThemaIdentifier("thema").getFirst();
        assertEquals("source-id", layer.id());
        assertEquals("1", layer.thema_r());
        assertEquals(30, layer.transparency());
        assertEquals(layer, repository.findEbeneByIdentPart("ch.so.agi.gemeindegrenzen").orElseThrow());
        assertTrue(repository.findEbeneByIdentPart("' OR '1'='1").isEmpty());
        assertTrue(repository.findEbenenByThemaIdentifier("' OR '1'='1").isEmpty());
    }
}
