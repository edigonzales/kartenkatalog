package ch.so.agi.meta;

import java.util.List;
import java.util.Optional;

public interface CatalogRepository {
    default Optional<Thema> findThema(String identifier) {
        return findAllThemen().stream().filter(t -> t.identifier().equals(identifier)).findFirst();
    }
    List<Thema> findAllThemen();

    List<Ebene> findAllEbenen();

    List<Ebene> findEbenenByThemaIdentifier(String identifier);

    Optional<Ebene> findEbeneByIdentPart(String identPart);
}
