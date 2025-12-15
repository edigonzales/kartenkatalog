package ch.so.agi.meta;

import java.util.List;
import java.util.Optional;

public interface CatalogRepository {
    List<Thema> findAllThemen();

    List<Ebene> findEbenenByThemaIdentifier(String identifier);

    Optional<Ebene> findEbeneByIdentPart(String identPart);
}
