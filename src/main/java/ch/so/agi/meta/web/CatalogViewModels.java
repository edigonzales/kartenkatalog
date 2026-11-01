package ch.so.agi.meta.web;

import io.quarkus.qute.RawString;
import java.util.List;

public final class CatalogViewModels {
    private CatalogViewModels() {}
    public record Crumb(String title, String href) {}
    public record Chrome(List<Crumb> crumbs, String provenanceNotice) {}
    public record Option(String value, String label, int count, boolean selected, String id) {}
    public record FilterGroup(String kind, String label, List<Option> options, int selectedCount) {}
    public record Chip(String label, String href) {}
    public record ThemeRow(String identifier, String title, String description, int count, String href, String panelId) {}
    public record CatalogPage(Chrome chrome, List<ThemeRow> themes, int total, String q,
                              List<FilterGroup> filters, List<Chip> chips, String clearSearchUrl, List<Option> sorts) {}
    public record LayerRow(String title, String href, String mapUrl, String type, String description) {}
    public record ThemePage(Chrome chrome, String title, RawString description, String furtherInfoUrl, List<LayerRow> layers) {}
    public record Detail(Chrome chrome, String title, String id, String type, String mapUrl, String styleUrl,
                         RawString description, List<String> keywords, List<String> synonyms, String organization,
                         String publicationScope, String download, String themeTitle, String themeHref, String furtherInfoUrl) {}
}
