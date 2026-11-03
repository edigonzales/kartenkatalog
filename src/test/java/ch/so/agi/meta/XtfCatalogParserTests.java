package ch.so.agi.meta;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import static org.junit.jupiter.api.Assertions.*;

class XtfCatalogParserTests {
    static String fixture() throws IOException { return Files.readString(Path.of("src/test/resources/contract/with-qml.xml")); }
    static CatalogSnapshot parse(String xml) { return new XtfCatalogParser().parse(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8))); }
    @Test void suppliedTransferCountsAndAllNestedCollectionsAreImmutable() throws Exception {
        try (var in = new CatalogXtfSource("file:data/kartenkatalog-harvest-ohne-qml.xtf").open();
             var snapshot = new XtfCatalogParser().parse(in)) {
            assertEquals(169, snapshot.findAllThemen().size()); assertEquals(886, snapshot.findAllEbenen().size());
            assertEquals(886, snapshot.findAllThemen().stream().mapToInt(Thema::ebenen_count).sum());
            assertEquals(886, snapshot.layersByIdentifier().size()); assertEquals(169, snapshot.themesByIdentifier().size());
            assertTrue(snapshot.findAllEbenen().stream().allMatch(e -> e.style_server() == null));
            assertThrows(UnsupportedOperationException.class, () -> snapshot.findAllThemen().clear());
            assertThrows(UnsupportedOperationException.class, () -> snapshot.findAllEbenen().clear());
            assertThrows(UnsupportedOperationException.class, () -> snapshot.layersByIdentifier().clear());
            assertThrows(UnsupportedOperationException.class, () -> snapshot.themesByIdentifier().clear());
            assertThrows(UnsupportedOperationException.class, () -> snapshot.layersByTheme().clear());
            assertThrows(UnsupportedOperationException.class, () -> snapshot.findEbenenByThemaIdentifier("harvest-hoheitsgrenzen").clear());
            assertTrue(snapshot.findEbenenByThemaIdentifier("missing").isEmpty());
        }
    }
    @Test void optionalBlankFieldsAreNullAndReferencesBeatRedundantThemeText() throws Exception {
        try (var snapshot = parse(fixture().replace("<k:title>QML Test</k:title>", "<k:title>QML Test</k:title><k:keywords>  </k:keywords><k:theme_ident>wrong</k:theme_ident>"))) {
        var e = snapshot.findEbeneByIdentPart("ch.test.qml").orElseThrow();
        assertNull(e.keywords()); assertEquals("test-theme", e.thema_r());
        assertEquals(1, snapshot.findThema("contract").orElseThrow().ebenen_count());
        assertEquals("contract", ch.so.agi.meta.api.CatalogDtos.Layer.from(e, snapshot.findThema("contract").orElseThrow()).themeIdentifier());
        }
    }
    @Test void optionalContactIsPreservedInEveryThemeDtoAndMissingContactStaysNull() throws Exception {
        try (var snapshot = parse(fixture())) {
            var theme = snapshot.findThema("contract").orElseThrow();
            assertNull(theme.contact());
            assertNull(ch.so.agi.meta.api.CatalogDtos.Theme.from(theme).contact());
        }
        String xml = fixture().replace("</k:Thema>", "<k:contact>mailto:agi@bd.so.ch</k:contact></k:Thema>");
        try (var snapshot = parse(xml)) {
            var theme = snapshot.findThema("contract").orElseThrow();
            assertEquals("mailto:agi@bd.so.ch", theme.contact());
            assertEquals("mailto:agi@bd.so.ch", ch.so.agi.meta.api.CatalogDtos.Theme.from(theme).contact());
        }
        try (var snapshot = parse(fixture().replace("</k:Thema>", "<k:contact> </k:contact></k:Thema>"))) {
            assertNull(snapshot.findThema("contract").orElseThrow().contact());
        }
    }
    @Test void contactRejectsOtherSchemesMultipleRecipientsHeadersAndMalformedAddresses() throws Exception {
        for (String value : List.of("agi@bd.so.ch", "https://so.ch", "javascript:alert(1)", "mailto:",
                "mailto:a@so.ch,b@so.ch", "mailto:a@so.ch?subject=hi", "mailto:a@so.ch%0Abcc:b@so.ch",
                "mailto:a@-invalid.ch", "mailto:a@so..ch", "mailto:a b@so.ch")) {
            String xml = fixture().replace("</k:Thema>", "<k:contact>" + value + "</k:contact></k:Thema>");
            assertThrows(IllegalArgumentException.class, () -> parse(xml), value);
        }
        assertThrows(IllegalArgumentException.class, () -> parse(fixture().replace("</k:Thema>",
            "<k:contact>mailto:a@so.ch</k:contact><k:contact>mailto:b@so.ch</k:contact></k:Thema>")));
    }
    static Stream<String> malformed() throws IOException {
        String xml = fixture();
        String layer = xml.substring(xml.indexOf("    <k:Ebene"), xml.indexOf("    </k:Ebene>") + "    </k:Ebene>".length());
        String theme = xml.substring(xml.indexOf("    <k:Thema"), xml.indexOf("</k:Thema>") + "</k:Thema>".length());
        return Stream.of(
            "<transfer/>", xml.replace("/xtf/2.4/INTERLIS", "/xtf/2.3/INTERLIS"),
            xml.replace("<ili:model>SO_AGI_Kartenkatalog_Publikation_20251210</ili:model>", "<ili:model>Foreign</ili:model>"),
            xml.replace("ili:ref=\"test-theme\"", "ili:ref=\"missing\""),
            xml.replace("<k:Thema_R ili:ref=\"test-theme\"/>", ""),
            xml.replace("<k:Thema_R ili:ref=\"test-theme\"/>", "<k:Thema_R ili:ref=\"test-theme\"/><k:Thema_R ili:ref=\"test-theme\"/>"),
            xml.replace("<k:title>QML Test</k:title>", ""),
            xml.replace("<k:title>QML Test</k:title>", "<wrapper><k:title>QML Test</k:title></wrapper>"),
            xml.replace("<k:title>QML Test</k:title>", "<k:title><k:title>QML Test</k:title></k:title>"),
            xml.replace("<k:ident_part>ch.test.qml</k:ident_part>", "<k:ident_part> </k:ident_part>"),
            xml.replace("<k:dtype>datasetview</k:dtype>", ""),
            xml.replace("85ee5e38-804c-4014-bb9b-490a2a3ea768", "not-a-uuid"),
            xml.replace("<k:title>QML Test</k:title>", "<k:title>QML Test</k:title><k:transparency>101</k:transparency>"),
            xml.replace("<k:title>QML Test</k:title>", "<k:title>QML Test</k:title><k:transparency>-1</k:transparency>"),
            xml.replace("<k:title>QML Test</k:title>", "<k:title>QML Test</k:title><k:service_download>maybe</k:service_download>"),
            xml.replace("ili:tid=\"test-layer\"", "ili:tid=\"test-theme\""),
            xml.replace("ili:tid=\"test-layer\"", ""),
            xml.replace("</k:Kartenkatalog>", layer + "</k:Kartenkatalog>"),
            xml.replace("</k:Kartenkatalog>", layer.replace("test-layer", "another-tid").replace("85ee5e38-804c-4014-bb9b-490a2a3ea768", "95ee5e38-804c-4014-bb9b-490a2a3ea768") + "</k:Kartenkatalog>"),
            xml.replace("</k:Kartenkatalog>", layer.replace("test-layer", "another-tid").replace("ch.test.qml", "ch.test.other") + "</k:Kartenkatalog>"),
            xml.replace("</k:Kartenkatalog>", theme.replace("test-theme", "another-theme") + "</k:Kartenkatalog>"),
            xml.replace("<k:Ebene", "<wrapper><k:Ebene").replace("</k:Ebene>", "</k:Ebene></wrapper>"),
            xml.replace("<?xml version=\"1.0\" encoding=\"UTF-8\"?>", "<!DOCTYPE transfer [<!ENTITY x SYSTEM 'file:///etc/passwd'>]>").replace("QML Test", "&x;")
        );
    }
    @ParameterizedTest @MethodSource("malformed") void rejectsInvalidTransfer(String xml) {
        assertThrows(IllegalArgumentException.class, () -> parse(xml));
    }
    @Test void sourceNeverFallsBackOrFetchesHttp() {
        for (String location : List.of("https://example.invalid/catalog.xtf", "data/catalog.xtf", "classpath:missing.xml", "file:/nonexistent/catalog.xtf"))
            assertThrows(Exception.class, () -> new CatalogXtfSource(location).open());
    }
}
