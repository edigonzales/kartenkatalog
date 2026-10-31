package ch.so.agi.meta;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Repository;
import org.w3c.dom.Element;

/** Read-only snapshot of the supplied INTERLIS 2.4 publication transfer. */
@Repository
@Profile("xtf")
public class XtfCatalogRepository implements CatalogRepository {
    private static final String ILI = "http://www.interlis.ch/xtf/2.4/INTERLIS";
    private static final String MODEL = "http://www.interlis.ch/xtf/2.4/SO_AGI_Kartenkatalog_Publikation_20251210";
    private final List<Thema> themen;
    private final List<Ebene> ebenen;

    public XtfCatalogRepository(@Value("${catalog.xtf-location}") Resource resource) {
        try (InputStream input = resource.getInputStream()) {
            var factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
            factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
            factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
            var document = factory.newDocumentBuilder().parse(input);
            var root = document.getDocumentElement();
            if (!ILI.equals(root.getNamespaceURI()) || !"transfer".equals(root.getLocalName())) {
                throw new IllegalArgumentException("Expected INTERLIS 2.4 transfer");
            }
            var themeNodes = document.getElementsByTagNameNS(MODEL, "Thema");
            var layerNodes = document.getElementsByTagNameNS(MODEL, "Ebene");
            if (themeNodes.getLength() == 0) throw new IllegalArgumentException("Transfer contains no publication themes");
            var layers = new ArrayList<Ebene>();
            var names = new HashSet<String>();
            for (int i = 0; i < layerNodes.getLength(); i++) {
                var e = (Element) layerNodes.item(i);
                String name = required(e, "ident_part");
                if (!names.add(name)) throw new IllegalArgumentException("Duplicate layer identifier: " + name);
                var reference = e.getElementsByTagNameNS(MODEL, "Thema_R");
                String themeId = reference.getLength() == 1 ? ((Element) reference.item(0)).getAttributeNS(ILI, "ref") : "";
                if (themeId.isBlank()) throw new IllegalArgumentException("Missing theme reference: " + name);
                String transparency = text(e, "transparency");
                Integer value = transparency == null ? null : Integer.valueOf(transparency);
                if (value != null && (value < 0 || value > 100)) throw new IllegalArgumentException("Invalid transparency: " + name);
                layers.add(new Ebene(required(e, "id"), required(e, "dtype"), text(e, "description"),
                        text(e, "description_override"), text(e, "description_model"), text(e, "remarks"),
                        required(e, "title"), name, text(e, "derived_identifier"), text(e, "keywords"),
                        text(e, "synonyms"), text(e, "display_text"), text(e, "style_server"),
                        text(e, "service_download") == null ? null : Boolean.valueOf(text(e, "service_download")),
                        value, text(e, "theme_title"), text(e, "theme_ident"), text(e, "org_name"),
                        text(e, "permissions"), themeId));
            }
            var themes = new ArrayList<Thema>();
            Map<String, String> identifiers = new HashMap<>();
            var uniqueIdentifiers = new HashSet<String>();
            for (int i = 0; i < themeNodes.getLength(); i++) {
                var e = (Element) themeNodes.item(i);
                String tid = e.getAttributeNS(ILI, "tid");
                String identifier = required(e, "identifier");
                if (tid.isBlank() || identifiers.putIfAbsent(tid, identifier) != null || !uniqueIdentifiers.add(identifier)) {
                    throw new IllegalArgumentException("Missing or duplicate theme identifier: " + identifier);
                }
                int count = (int) layers.stream().filter(layer -> tid.equals(layer.thema_r())).count();
                themes.add(new Thema(tid, identifier, required(e, "title"), text(e, "description"),
                        text(e, "keywords"), text(e, "synonyms"), text(e, "further_info_url"), text(e, "data_owner"), count));
            }
            if (layers.stream().anyMatch(layer -> !identifiers.containsKey(layer.thema_r()))) {
                throw new IllegalArgumentException("Unresolved theme reference in transfer");
            }
            this.themen = List.copyOf(themes);
            this.ebenen = List.copyOf(layers);
        } catch (Exception e) {
            throw new IllegalStateException("Cannot load catalog XTF: " + resource.getDescription(), e);
        }
    }

    private static String text(Element parent, String name) {
        var elements = parent.getElementsByTagNameNS(MODEL, name);
        if (elements.getLength() == 0) return null;
        String value = elements.item(0).getTextContent().strip();
        return value.isEmpty() ? null : value;
    }
    private static String required(Element parent, String name) {
        String value = text(parent, name);
        if (value == null) throw new IllegalArgumentException("Missing XTF field: " + name);
        return value;
    }
    @Override public List<Thema> findAllThemen() { return themen; }
    @Override public List<Ebene> findAllEbenen() { return ebenen; }
    @Override public List<Ebene> findEbenenByThemaIdentifier(String identifier) {
        return themen.stream().filter(t -> t.identifier().equals(identifier)).findFirst()
                .map(t -> ebenen.stream().filter(e -> e.thema_r().equals(t.t_id())).toList()).orElse(List.of());
    }
    @Override public Optional<Ebene> findEbeneByIdentPart(String identPart) {
        return ebenen.stream().filter(e -> e.ident_part().equals(identPart)).findFirst();
    }
}
