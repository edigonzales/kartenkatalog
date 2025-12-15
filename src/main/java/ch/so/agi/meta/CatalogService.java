package ch.so.agi.meta;

import java.text.Collator;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import org.springframework.stereotype.Service;

@Service
public class CatalogService {
    private final CatalogRepository catalogRepository;
    private final Collator germanCollator;

    public CatalogService(CatalogRepository catalogRepository) {
        this.catalogRepository = catalogRepository;
        this.germanCollator = Collator.getInstance(Locale.forLanguageTag("de-CH"));
        this.germanCollator.setStrength(Collator.PRIMARY);
    }

    public List<Thema> findAllThemen() {
        return catalogRepository.findAllThemen();
    }

    public List<Ebene> findEbenenByThemaIdentifier(String identifier) {
        return catalogRepository.findEbenenByThemaIdentifier(identifier).stream()
                .sorted(Comparator.comparing(Ebene::title, germanCollator))
                .toList();
    }

    public Optional<Ebene> findEbeneByIdentPart(String identPart) {
        return catalogRepository.findEbeneByIdentPart(identPart);
    }
}
