package ch.so.agi.meta;

import ch.so.agi.meta.Product;

public record Product(
        String id, 
        String parent_title,
        String parent_ident_part,
        String dtype, 
        String description, 
        String description_override, 
        String description_model, 
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
        String permissions,
        Product[] children
        ) {}
