package ch.so.agi.meta.api;

import java.util.List;
import ch.so.agi.meta.CatalogText;
import ch.so.agi.meta.Ebene;
import ch.so.agi.meta.Thema;

/** Public JSON contract independent of the XTF storage representation. */
public final class CatalogDtos {
    private CatalogDtos() {}
    public record Theme(String identifier, String title, String description, String descriptionHtml,
                        List<String> keywords, List<String> synonyms, String furtherInfoUrl,
                        String dataOwner, String contact, int layerCount) {
        public static Theme from(Thema t) {
            return new Theme(t.identifier(), t.title(), CatalogText.plain(t.adescription()), CatalogText.html(t.adescription()),
                    CatalogText.terms(t.keywords()), CatalogText.terms(t.synonyms()), t.further_info_url(), t.data_owner(), t.contact(), t.ebenen_count());
        }
    }
    public record Layer(String id, String sourceId, String type, String title, String description,
                        String descriptionHtml, List<String> keywords, List<String> synonyms,
                        String qwcLayerName, String derivedIdentifier, Integer transparency,
                        String themeIdentifier, String themeTitle, String organization,
                        String publicationScope, Boolean downloadAvailable) {
        public static Layer from(Ebene e, Thema t) {
            String description = CatalogText.description(e);
            return new Layer(e.ident_part(), e.id(), e.dtype(), e.title(), CatalogText.plain(description), CatalogText.html(description),
                    CatalogText.terms(e.keywords()), CatalogText.terms(e.synonyms()), e.ident_part(), e.derived_identifier(),
                    e.transparency(), t.identifier(), t.title(), e.org_name(), e.display_text(), e.service_download());
        }
    }
    public record ThemeLayers(Theme theme, List<Layer> layers) {}
    public record Catalog(List<ThemeLayers> themes) {}
}
