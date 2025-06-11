# kartenkatalog

## Fragen

- Falls nicht einzeln publiziert, sollte man nicht einen Web GIS Client Link zur Verfügung stellen? Resp. den der Layergruppe?
- Was soll gefixed werden / verbessert werden? Inhaltlich? Gibt es Muster? -> Zusammenfassung von Beschreibung der Einzellayer in der Layergruppe (z.B.). Soll die Zusammenfassung immer gleich beginne "die Layergruppe beinhaltet ..."
- Was machen mit vielen leeren Beschreibungen?
- Was machen mit Keywords / Synonmye?
- Was darf in Beschreibung verwendet werden?
- Ggf GIS-Koordinatoren fragen.
- Was soll alles in die Liste? (ich glaube data service habe ich nicht, oder? Ah doch, muss ich mir noch anschauen, plus es gibt das Attribut in Product, schräg: "ch.so.afu.altlasten.standorte.data_v2" kann ich eingeloggt im WGC in der URL angeben und es lädt). -> falls "nur WMS" funktioniert es nicht, z.B. "Dataservice-Konfig für UPlusLuft-Anlagen". Ist wohl bei altlasten falsch konfiguriert und geht trotzdem irgendwo unter, damit es nicht in der Suche vorkommt.


## Vorgaben

- Immer gleiche Anführungszeichen.
- Keine fetten Schriften
- keine leeren Strings, z.B. komische leere badges? (abwasser abwasserleitungen) (-> eher DB).


## Technisches

- Product braucht noch Titel/Name des Parents
- Leere Felder zeigen? Damit klar ist == null
- Klassen machen für min-height (badge, text).


https://github.com/davidoesch/geoservice_harvester_poc/blob/main/index.html#L132
https://dev.to/rpkr/different-ways-to-send-a-file-as-a-response-in-spring-boot-for-a-rest-api-43g7
https://github.com/oracle/graal/issues/7444

-Dspring.profiles.active=prod
--spring.profiles.active=prod


./build/native/nativeCompile/kartenkatalog --spring.datasource.url=jdbc:postgresql://geodb.rootso.org:5432/pub --spring.datasource.username=yyyyyyyyyy --spring.datasource.password=xxxxxxxxxxx --spring.profiles.active=prod --logging.level.org.springframework.web.servlet=trace


TODO: Modell MTEXT!!!!! bei qgis? oder allen.


-> simiproduct_product_list -> simiproduct_properties_in_list -> attr: product_list_id ist id der layergruppe

CTE:
- parents (mit where filter)
- ein left join sollte reichen
- gruppieren in Java


```
java -jar /Users/stefan/apps/ili2pg-5.3.0/ili2pg-5.3.0.jar --dbhost localhost --dbport 54322 --dbdatabase pub --dbusr ddluser --dbpwd ddluser --dbschema agi_kartenkatalog_pub_v1 --nameByTopic --defaultSrsCode 2056 --createFk --createFkIdx --createGeomIdx --strokeArcs --models SO_AGI_Kartenkatalog_20250311 --modeldir ".;https://models.geo.admin.ch" --doSchemaImport --import kartenkatalog.xtf
```



```
java -jar /Users/stefan/apps/ili2duckdb-5.2.2/ili2duckdb-5.2.2.jar --dbfile kartenkatalog.duckdb --strokeArcs --nameByTopic --dbschema foo --defaultSrsCode 2056 --modeldir "/Users/stefan/sources/kartenkatalog;https://models.geo.admin.ch" --models SO_AGI_Kartenkatalog_20250311 --schemaimport
```

```
java -jar /Users/stefan/apps/ili2duckdb-5.2.2/ili2duckdb-5.2.2.jar --dbfile kartenkatalog.duckdb --dbschema foo --defaultSrsCode 2056 --modeldir "/Users/stefan/sources/kartenkatalog;https://models.geo.admin.ch" --models SO_AGI_Kartenkatalog_20250311 --export kartenkatalog.xtf
```


```
--ATTACH 'dbname=simi user=bjsvwzie password=XXXXXXXXXXX host=geodb.rootso.org' AS simidb (TYPE postgres);


--EXPLAIN



CREATE TEMP TABLE mytable AS 
WITH parents AS 
(
    SELECT 
        dp.id,
        dp.dtype,
        dp.description,
        pg.description_override,
        pg.description_model,
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
        json(list(r."name") FILTER (WHERE r."name" IS NOT NULL)) AS permissions--,
        --json_agg(r."name") FILTER (WHERE r."name" IS NOT NULL) AS permissions
    FROM 
        simidb.simi.simiproduct_data_product AS dp 
        LEFT JOIN simidb.simi.simiproduct_data_product_pub_scope AS ps 
        ON dp.pub_scope_id = ps.id 
        LEFT JOIN simidb.simi.simidata_data_set_view AS dsw 
        ON dsw.id = dp.id
        LEFT JOIN simidb.simi.simiproduct_single_actor AS sa  
        ON sa.id = dp.id
        LEFT JOIN simidb.simi.simiiam_permission AS perm
        ON perm.data_set_view_id = dsw.id
        LEFT JOIN simidb.simi.simiiam_role AS r 
        ON perm.role_id = r.id
        LEFT JOIN simidb.simi.simitheme_theme_publication AS stp 
        ON dp.theme_publication_id = stp.id 
        LEFT JOIN simidb.simi.simitheme_theme AS st 
        ON stp.theme_id = st.id
        LEFT JOIN simidb.simi.simitheme_org_unit AS sou 
        ON st.data_owner_id = sou.id

        LEFT JOIN simidb.simi.simidata_table_view AS tv 
        ON dp.id = tv.id 
        LEFT JOIN simidb.simi.simidata_postgres_table AS pg 
        ON tv.postgres_table_id = pg.id

    WHERE 
        dp.dtype NOT IN ('simiProduct_Map')
        AND
        ps.display_text IN ('WGC, QGIS u. WMS', 'Nur WMS', 'WGC u. QGIS')
    GROUP BY 
        1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18
)
,
children AS 
(
    SELECT 
        lg.id AS lg_id,
        dp.id,
        dp.dtype,
        dp.description,
        pg.description_override,
        pg.description_model,
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
        json(list(r."name") FILTER (WHERE r."name" IS NOT NULL)) AS permissions--,
        --json_agg(r."name") FILTER (WHERE r."name" IS NOT NULL) AS permissions
    FROM 
        simidb.simi.simiproduct_layer_group AS lg 
        LEFT JOIN simidb.simi.simiproduct_properties_in_list AS pil 
        ON lg.id = pil.product_list_id
        LEFT JOIN simidb.simi.simiproduct_data_product AS dp 
        ON pil.single_actor_id = dp.id 
        LEFT JOIN simidb.simi.simiproduct_data_product_pub_scope AS ps 
        ON dp.pub_scope_id = ps.id 
        LEFT JOIN simidb.simi.simidata_data_set_view AS dsw 
        ON dsw.id = dp.id
        LEFT JOIN simidb.simi.simiproduct_single_actor AS sa  
        ON sa.id = dp.id
        LEFT JOIN simidb.simi.simiiam_permission AS perm
        ON perm.data_set_view_id = dsw.id
        LEFT JOIN simidb.simi.simiiam_role AS r 
        ON perm.role_id = r.id
        LEFT JOIN simidb.simi.simitheme_theme_publication AS stp 
        ON dp.theme_publication_id = stp.id 
        LEFT JOIN simidb.simi.simitheme_theme AS st 
        ON stp.theme_id = st.id
        LEFT JOIN simidb.simi.simitheme_org_unit AS sou 
        ON st.data_owner_id = sou.id

        LEFT JOIN simidb.simi.simidata_table_view AS tv 
        ON dp.id = tv.id 
        LEFT JOIN simidb.simi.simidata_postgres_table AS pg 
        ON tv.postgres_table_id = pg.id

    GROUP BY 
        1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19
)
SELECT 
    p.id AS p_id,
    p.dtype AS p_dtype,
    p.description AS p_description,
    p.description_override AS p_description_override,
    p.description_model AS p_description_model,
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
    c.description_override AS c_description_override,
    c.description_model AS c_description_model,
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
ORDER BY 
    p.title, c.title   
;

INSERT INTO 
    foo.kartenkatalog_produkt_mutter_kind 
(
    p_id,
    p_dtype,
    p_description,
    p_description_override,
    p_description_model,
    p_remarks,
    p_title,
    p_ident_part,
    p_derived_identifier,
    p_keywords,
    p_synonyms,
    p_display_text,
    p_style_server,
    p_service_download,
    p_transparency,
    p_theme_title,
    p_theme_ident,
    p_org_name,
    p_permissions,
    c_id,
    c_dtype,
    c_description,
    c_description_override,
    c_description_model,
    c_remarks,
    c_title,
    c_ident_part,
    c_derived_identifier,
    c_keywords,
    c_synonyms,
    c_display_text,
    c_style_server,
    c_service_download,
    c_transparency,
    c_theme_title,
    c_theme_ident,
    c_org_name,
    c_permissions
)
SELECT 
    p_id,
    p_dtype,
    p_description,
    p_description_override,
    p_description_model,
    p_remarks,
    p_title,
    p_ident_part,
    p_derived_identifier,
    p_keywords,
    p_synonyms,
    p_display_text,
    p_style_server,
    p_service_download,
    p_transparency,
    p_theme_title,
    p_theme_ident,
    p_org_name,
    p_permissions,
    c_id,
    c_dtype,
    c_description,
    c_description_override,
    c_description_model,
    c_remarks,
    c_title,
    c_ident_part,
    c_derived_identifier,
    c_keywords,
    c_synonyms,
    c_display_text,
    c_style_server,
    c_service_download,
    c_transparency,
    c_theme_title,
    c_theme_ident,
    c_org_name,
    c_permissions
FROM 
    mytable
;
```