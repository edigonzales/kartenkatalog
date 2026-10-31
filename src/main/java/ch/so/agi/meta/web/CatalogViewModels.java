package ch.so.agi.meta.web;

import io.quarkus.qute.RawString;
import java.util.List;

public final class CatalogViewModels {
    private CatalogViewModels() {}
    public record Crumb(String title, String href) {}
    public record Chrome(List<Crumb> crumbs) {}
    public record Option(String value, String label, boolean selected) {}
    public record ThemeRow(String identifier, String title, String description, int count, String href, String panelId) {}
    public record CatalogPage(Chrome chrome, List<ThemeRow> themes, int total, String q,
                              List<Option> themeOptions, List<Option> typeOptions, List<Option> organizationOptions) {}
    public record LayerRow(String title, String href, String mapUrl, String type, String description) {}
    public record ThemePage(Chrome chrome, String title, RawString description, String furtherInfoUrl, List<LayerRow> layers) {}
    public record Detail(Chrome chrome, String title, String id, String type, String mapUrl, String styleUrl,
                         RawString description, List<String> keywords, List<String> synonyms, String organization,
                         String publicationScope, String download, String themeTitle, String themeHref, String furtherInfoUrl) {}
}
