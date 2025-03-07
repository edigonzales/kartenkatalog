package ch.so.agi.maps;

public record Product(
        String id, 
        String dtype, 
        String description, 
        String remarks, 
        String title, 
        String ident_part,
        String derived_identifier,
        String keywords,
        String synonyms,
        String display_text,
        String style_server,
        Boolean service_download,
        String transparency,
        String theme_title,
        String theme_ident,
        String org_name,
        String permissions
        ) {}
