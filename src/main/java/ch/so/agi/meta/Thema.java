package ch.so.agi.meta;

public record Thema(
        String t_id,
        String identifier,
        String title,
        String adescription,
        String keywords,
        String synonyms,
        String further_info_url,
        String data_owner,
        int ebenen_count
) {}
