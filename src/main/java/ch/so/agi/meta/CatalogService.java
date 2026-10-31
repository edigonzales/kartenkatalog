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
    public CatalogService(CatalogRepository catalogRepository) { this.catalogRepository = catalogRepository; }

    // Collator is mutable and not thread-safe: each operation owns its comparator.
    private static Comparator<String> titles() {
        var collator = Collator.getInstance(Locale.forLanguageTag("de-CH"));
        collator.setStrength(Collator.PRIMARY);
        return Comparator.nullsLast(collator::compare);
    }
    public List<Thema> findAllThemen() {
        return catalogRepository.findAllThemen().stream()
                .sorted(Comparator.comparing(Thema::title, titles()).thenComparing(Thema::identifier)).toList();
    }
    public List<Ebene> findAllEbenen() { return sorted(catalogRepository.findAllEbenen()); }
    public List<Ebene> findEbenenByThemaIdentifier(String identifier) {
        return sorted(catalogRepository.findEbenenByThemaIdentifier(identifier));
    }
    private List<Ebene> sorted(List<Ebene> layers) {
        return layers.stream().sorted(Comparator.comparing(Ebene::title, titles()).thenComparing(Ebene::ident_part)).toList();
    }
    public Optional<Ebene> findEbeneByIdentPart(String identPart) { return catalogRepository.findEbeneByIdentPart(identPart); }
    public Optional<Thema> findThema(String identifier) {
        return findAllThemen().stream().filter(t -> t.identifier().equals(identifier)).findFirst();
    }
}
