package ch.so.agi.meta;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("xtf")
class CatalogApiTests {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;

    @Test void suppliedTransferExposesAllThemesAndLayers() throws Exception {
        mvc.perform(get("/api/v1/themen")).andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(169)));
        mvc.perform(get("/api/v1/ebenen")).andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(886)));
        var response = mvc.perform(get("/api/v1/catalog")).andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        var themes = json.readTree(response).get("themes");
        int count = 0;
        for (var entry : themes) {
            assertEquals(entry.get("theme").get("layerCount").asInt(), entry.get("layers").size());
            count += entry.get("layers").size();
            for (var layer : entry.get("layers")) assertEquals(entry.get("theme").get("identifier"), layer.get("themeIdentifier"));
        }
        assertEquals(886, count);
    }
    @Test void layerContractUsesStableQwcNameAndNoInternalFields() throws Exception {
        mvc.perform(get("/api/v1/ebenen/ch.so.agi.gemeindegrenzen"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.id").value("ch.so.agi.gemeindegrenzen"))
                .andExpect(jsonPath("$.qwcLayerName").value("ch.so.agi.gemeindegrenzen"))
                .andExpect(jsonPath("$.keywords").isArray()).andExpect(jsonPath("$.description").isString())
                .andExpect(jsonPath("$.themeIdentifier").value("harvest-hoheitsgrenzen"))
                .andExpect(jsonPath("$.remarks").doesNotExist()).andExpect(jsonPath("$.style_server").doesNotExist());
    }
    @Test void searchAndThemeFilters() throws Exception {
        mvc.perform(get("/api/v1/ebenen").param("q", "  GEMEINDEGRENZEN  "))
                .andExpect(status().isOk()).andExpect(jsonPath("$[*].id", hasItem("ch.so.agi.gemeindegrenzen")));
        mvc.perform(get("/api/v1/themen/harvest-hoheitsgrenzen/ebenen"))
                .andExpect(status().isOk()).andExpect(jsonPath("$[*].themeIdentifier", everyItem(is("harvest-hoheitsgrenzen"))));
        mvc.perform(get("/api/v1/ebenen").param("q", "not-a-real-layer-548139"))
                .andExpect(status().isOk()).andExpect(content().json("[]"));
        mvc.perform(get("/api/v1/ebenen").param("q", "x".repeat(201))).andExpect(status().isBadRequest());
    }
    @Test void missingResourcesAndAbsentQmlAre404() throws Exception {
        for (String route : new String[]{"/api/v1/ebenen/unknown", "/api/v1/themen/unknown/ebenen", "/ebene/unknown", "/ebene/ch.so.agi.gemeindegrenzen/style"}) {
            mvc.perform(get(route)).andExpect(status().isNotFound());
        }
        mvc.perform(get("/api/v1/ebenen/unknown")).andExpect(content().contentTypeCompatibleWith("application/problem+json"));
        mvc.perform(get("/api/v1/ebenen").param("thema", "unknown")).andExpect(status().isNotFound());
    }
    @Test void precompiledHtmlStillRendersRealData() throws Exception {
        mvc.perform(get("/thema")).andExpect(status().isOk()).andExpect(content().string(containsString("Hoheitsgrenzen")))
                .andExpect(content().string(not(containsString("Lorem ipsum"))));
        mvc.perform(get("/themen/harvest-hoheitsgrenzen/ebenen")).andExpect(status().isOk()).andExpect(content().string(containsString("Gemeindegrenzen")));
        mvc.perform(get("/ebene/ch.so.agi.gemeindegrenzen")).andExpect(status().isOk()).andExpect(content().string(containsString("Gemeindegrenzen")));
    }
}
