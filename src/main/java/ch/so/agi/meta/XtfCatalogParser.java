package ch.so.agi.meta;

import java.io.InputStream;
import java.util.*;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Element;

/** Model-specific INTERLIS 2.4 DOM reader, ported from XtfCatalogRepository. */
public final class XtfCatalogParser {
    private static final String ILI = "http://www.interlis.ch/xtf/2.4/INTERLIS";
    private static final String MODEL_NAME = "SO_AGI_Kartenkatalog_Publikation_20251210";
    private static final String MODEL = "http://www.interlis.ch/xtf/2.4/" + MODEL_NAME;

    public CatalogSnapshot parse(InputStream input) {
        try {
            var factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
            factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            factory.setXIncludeAware(false);
            factory.setExpandEntityReferences(false);
            factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
            factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
            var root = factory.newDocumentBuilder().parse(input).getDocumentElement();
            if (!is(root, ILI, "transfer")) throw invalid("Expected INTERLIS 2.4 transfer");
            var models = one(one(root, ILI, "headersection"), ILI, "models");
            var model = one(models, ILI, "model");
            if (!MODEL_NAME.equals(model.getTextContent().strip())) throw invalid("Unexpected publication model");
            var data = one(root, ILI, "datasection");
            var themeNodes = new ArrayList<Element>();
            var layerNodes = new ArrayList<Element>();
            var tids = new HashSet<String>();
            for (var basket : children(data)) {
                if (!is(basket, MODEL, "Kartenkatalog")) throw invalid("Unexpected model basket: " + basket.getTagName());
                for (var object : children(basket)) {
                    String tid = object.getAttributeNS(ILI, "tid");
                    if (tid.isBlank() || !tids.add(tid)) throw invalid("Missing or duplicate object TID: " + tid);
                    if (is(object, MODEL, "Thema")) themeNodes.add(object);
                    else if (is(object, MODEL, "Ebene")) layerNodes.add(object);
                    else throw invalid("Unexpected catalog object: " + object.getTagName());
                }
            }
            if (themeNodes.isEmpty()) throw invalid("Transfer contains no publication themes");
            var layers = new ArrayList<Ebene>();
            var identifiers = new HashSet<String>();
            var sourceIds = new HashSet<String>();
            for (var e : layerNodes) {
                String name = required(e, "ident_part");
                if (!identifiers.add(name)) throw invalid("Duplicate layer identifier: " + name);
                String id = required(e, "id");
                if (!id.matches("[0-9a-fA-F]{8}(-[0-9a-fA-F]{4}){3}-[0-9a-fA-F]{12}") || !sourceIds.add(id.toLowerCase(Locale.ROOT)))
                    throw invalid("Invalid or duplicate layer UUID: " + id);
                var reference = one(e, MODEL, "Thema_R");
                String themeId = reference.getAttributeNS(ILI, "ref");
                if (themeId.isBlank()) throw invalid("Missing theme reference: " + name);
                String transparency = text(e, "transparency");
                Integer value = transparency == null ? null : Integer.valueOf(transparency);
                if (value != null && (value < 0 || value > 100)) throw invalid("Invalid transparency: " + name);
                String download = text(e, "service_download");
                if (download != null && !Set.of("true", "false").contains(download)) throw invalid("Invalid service_download: " + name);
                layers.add(new Ebene(id, required(e, "dtype"), text(e, "description"),
                    text(e, "description_override"), text(e, "description_model"), text(e, "remarks"),
                    required(e, "title"), name, text(e, "derived_identifier"), text(e, "keywords"), text(e, "synonyms"),
                    text(e, "display_text"), text(e, "style_server"), download == null ? null : Boolean.valueOf(download),
                    value, text(e, "theme_title"), text(e, "theme_ident"), text(e, "org_name"), text(e, "permissions"), themeId));
            }
            var themes = new ArrayList<Thema>();
            var themeIds = new HashSet<String>();
            var themeTids = new HashSet<String>();
            for (var e : themeNodes) {
                String tid = e.getAttributeNS(ILI, "tid");
                String identifier = required(e, "identifier");
                if (!themeIds.add(identifier)) throw invalid("Duplicate theme identifier: " + identifier);
                themeTids.add(tid);
                int count = (int) layers.stream().filter(layer -> tid.equals(layer.thema_r())).count();
                themes.add(new Thema(tid, identifier, required(e, "title"), text(e, "description"), text(e, "keywords"),
                    text(e, "synonyms"), text(e, "further_info_url"), text(e, "data_owner"), count));
            }
            for (var layer : layers) if (!themeTids.contains(layer.thema_r()))
                throw invalid("Unresolved theme reference for " + layer.ident_part() + ": " + layer.thema_r());
            return new CatalogSnapshot(themes, layers);
        } catch (Exception error) {
            throw new IllegalArgumentException("Invalid catalog XTF: " + error.getMessage(), error);
        }
    }
    private static boolean is(Element element, String ns, String name) {
        return ns.equals(element.getNamespaceURI()) && name.equals(element.getLocalName());
    }
    private static List<Element> children(Element element) {
        var result = new ArrayList<Element>();
        for (var n = element.getFirstChild(); n != null; n = n.getNextSibling()) if (n instanceof Element e) result.add(e);
        return result;
    }
    private static Element one(Element parent, String ns, String name) {
        var matches = children(parent).stream().filter(e -> is(e, ns, name)).toList();
        if (matches.size() != 1) throw invalid("Expected one " + name + " in " + parent.getTagName());
        return matches.getFirst();
    }
    private static String text(Element parent, String name) {
        var matches = children(parent).stream().filter(e -> is(e, MODEL, name)).toList();
        if (matches.size() > 1) throw invalid("Duplicate field " + name + " in " + parent.getTagName());
        if (matches.isEmpty()) return null;
        if (!children(matches.getFirst()).isEmpty()) throw invalid("Nested content in field: " + name);
        String value = matches.getFirst().getTextContent().strip();
        return value.isEmpty() ? null : value;
    }
    private static String required(Element parent, String name) {
        String value = text(parent, name);
        if (value == null) throw invalid("Missing XTF field: " + name);
        return value;
    }
    private static IllegalArgumentException invalid(String message) { return new IllegalArgumentException(message); }
}
