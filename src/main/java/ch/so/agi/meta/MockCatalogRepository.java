package ch.so.agi.meta;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

@Repository
@Profile("mock")
public class MockCatalogRepository implements CatalogRepository {
    private final List<Thema> themen;
    private final List<Ebene> ebenen;

    public MockCatalogRepository() {
        this.themen = List.of(
                new Thema("t-1", "geologie", "Geologie", "Übersicht zu geologischen Themen.", "Geologie,Stein", "Fels,Gestein", "https://geo.so.ch/geoportal", "Amt für Geologie", 3),
                new Thema("t-2", "planung", "Planung", "Informationen rund um Raumplanung.", "Planung,Räumlich", "Ortsplanung,Zonen", "https://geo.so.ch/planung", "Amt für Raumplanung", 1)
        );

        this.ebenen = List.of(
                new Ebene("e-1", "simiProduct_Layer", "Kantonsgeologie", null, null, "", "Kantonsgeologie", "ch.so.geologie.karte", "kg1", "Geologie", "Gestein", "public", null, true, 10, "Geologie", "geologie", "AfG", "public", "t-1"),
                new Ebene("e-2", "simiProduct_Layer", "Bohrungen", null, null, "", "Bohrungen", "ch.so.geologie.bohrungen", "bo1", "Bohrung", "Bohrloch", "restricted", null, false, 0, "Geologie", "geologie", "AfG", "intern", "t-1"),
                new Ebene("e-3", "simiProduct_Layer", "Nutzungsplanung", null, null, "", "Nutzungsplanung", "ch.so.planung.nutz", "np1", "Planung", "Zonen", "public", null, true, 0, "Planung", "planung", "ARP", "public", "t-2"),
                new Ebene("e-4", "simiProduct_Layer", "Bauzonen", null, null, "", "Bauzonen", "ch.so.planung.bauzonen", "bz1", "Planung", "Bau", "public", null, true, 25, "Planung", "planung", "ARP", "public", "t-2")
        );
    }

    @Override
    public List<Thema> findAllThemen() {
        return themen;
    }

    @Override
    public List<Ebene> findEbenenByThemaIdentifier(String identifier) {
        return ebenen.stream()
                .filter(ebene -> ebene.thema_r().equals(identifier) || ebene.theme_ident().equals(identifier))
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Ebene> findEbeneByIdentPart(String identPart) {
        return ebenen.stream()
                .filter(ebene -> ebene.ident_part().equalsIgnoreCase(identPart))
                .findFirst();
    }
}
