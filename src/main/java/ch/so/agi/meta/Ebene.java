package ch.so.agi.meta;

public record Ebene(
        String id,
        String dtype,
        String adescription,
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
        Integer transparency,
        String theme_title,
        String theme_ident,
        String org_name,
        String permissions,
        String thema_r
) {}
