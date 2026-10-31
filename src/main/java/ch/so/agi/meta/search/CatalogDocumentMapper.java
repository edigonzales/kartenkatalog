package ch.so.agi.meta.search;

import ch.so.agi.meta.*;
import org.apache.lucene.document.*;

public final class CatalogDocumentMapper {
    private CatalogDocumentMapper() {}
    public static Document toDocument(Ebene layer, Thema theme) {
        var doc = new Document();
        doc.add(new StringField("id", layer.ident_part(), Field.Store.YES));
        exact(doc, "identifier", layer.ident_part());
        exact(doc, "title", layer.title());
        for (String keyword : CatalogText.terms(layer.keywords())) exact(doc, "keywords", keyword);
        for (String synonym : CatalogText.terms(layer.synonyms())) exact(doc, "synonyms", synonym);
        exact(doc, "theme", theme.title()); exact(doc, "theme", theme.identifier());
        exact(doc, "organization", layer.org_name());
        doc.add(new StringField("type", layer.dtype(), Field.Store.NO));
        String description = CatalogText.plain(CatalogText.description(layer));
        if (description != null) doc.add(new TextField("description", SearchText.normalize(description), Field.Store.NO));
        return doc;
    }
    private static void exact(Document doc, String field, String text) {
        String value = SearchText.normalize(text);
        if (!value.isEmpty()) doc.add(new StringField(field, value, Field.Store.NO));
    }
}
