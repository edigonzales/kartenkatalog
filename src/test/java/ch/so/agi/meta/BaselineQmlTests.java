package ch.so.agi.meta;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/** Synthetic XTF; never implies QML exists in the harvested catalog. */
@SpringBootTest(properties = "catalog.xtf-location=classpath:contract/with-qml.xml")
@AutoConfigureMockMvc
@ActiveProfiles("xtf")
class BaselineQmlTests {
    @Autowired MockMvc mvc;
    @Test void qmlIsAnExactXmlAttachmentNotHtml() throws Exception {
        mvc.perform(get("/ebene/ch.test.qml/style")).andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith("application/xml"))
            .andExpect(header().string("Content-Disposition", allOf(startsWith("attachment;"), containsString("ch.test.qml.qml"))))
            .andExpect(content().string("<qgis version=\"3.40\"><renderer-v2 type=\"singleSymbol\"/></qgis>"));
    }
    @Test void descriptionsAreSanitizedAndQmlStaysOutOfPublicJson() throws Exception {
        mvc.perform(get("/ebene/ch.test.qml")).andExpect(status().isOk())
            .andExpect(content().string(containsString("<strong>formatiert</strong>")))
            .andExpect(content().string(not(containsString("alert(1)"))))
            .andExpect(content().string(containsString("ch.test.qml/style")));
        mvc.perform(get("/api/v1/ebenen/ch.test.qml")).andExpect(status().isOk())
            .andExpect(jsonPath("$.description").value("Sicher formatiert"))
            .andExpect(jsonPath("$.descriptionHtml").value("<p>Sicher <strong>formatiert</strong></p>"))
            .andExpect(jsonPath("$.style_server").doesNotExist());
    }
}
