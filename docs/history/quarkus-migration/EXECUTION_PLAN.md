# Kartenkatalog – Quarkus-/Lucene-Refactoring: verbindliche Umsetzungsspezifikation

**Stand:** 31. Oktober 2026 · **Version:** 1.1 · **Auftraggeber:** Projekt `edigonzales/kartenkatalog`  
**Ausführung:** Codex · **Unabhängige Abnahme:** OpenCode 2.x (macOS GUI), Provider **DeepSeek API**, Modell **DeepSeek 4.1 Flash 1017** (exakte im GUI gewählte Modellkennung protokollieren).  
**Repository:** https://github.com/edigonzales/kartenkatalog  
**Bei Erstellung untersuchter Repo-Commit:** `c273b6e9f42e6d04803965fe1e3b06e2ba823d34`. Dies ist **kein** Start-Pin: Codex hat den tatsächlichen Stand beim Start selbst festzustellen.  
**UI-/Architekturreferenz:** https://github.com/sogis/datenportal-sodata – untersuchter Commit `9db0920e58613ece3bccf5e024199c016705d5de` (Codegestaltung und Techniken als Referenz, nicht das komplette Projekt kopieren).  
**Operative Dokumente:** `docs/quarkus-migration/CODEX_STARTPROMPT.md`, `docs/quarkus-migration/OPENCODE_REVIEW_PROMPT.md` und diese Datei.

## 0. Auftrag, Grenzen und verbindliche Architekturentscheidungen

Ziel ist die **bestehende** Kartenkatalog-Anwendung im **gleichen GitHub-Repository** von Spring Boot 4/JTE/JDBC auf eine kleine **Quarkus**-Anwendung umzustellen. Kein Greenfield-Nebenrepo und kein paralleles Spring/Quarkus-Produkt. Der Katalog besteht aus einer INTERLIS-2.4-XTF-Datei; Themen und Kartenprodukte werden **einmalig beim Start** sicher eingelesen, validiert und als **immutable In-Memory-Snapshot** bereitgestellt. Für die Volltextsuche entsteht bei jedem Start ein **neuer Apache-Lucene-10.6.0-Index im Java-Heap** (`ByteBuffersDirectory`). **Nur die XTF ist persistent**, nicht der Lucene-Index: weder eine separate Indexdatei noch ein persistentes Lucene-Volume werden angelegt oder vorausgesetzt. Es gibt **keine Datenbank**, keine notwendige PostgreSQL-Instanz, keinen periodischen Synchronisationsprozess und zunächst **keinen administrativen Reload-Endpunkt**: neue XTF + Neustart/Deployment genügt. Bestehende REST-API und sinnvoll weiterzuverwendende HTML-URLs bleiben kompatibel.

**Verbindliche Technikentscheidungen:**

| Aspekt | Vorgabe / Begründung |
|---|---|
| Quarkus | **3.40.1.1** aus der 3.40-LTS-Linie, BOM und Gradle-Plugin auf derselben geprüften Version; Version nicht still dynamisieren. Offizielle Quellen: https://quarkus.io/releases/ und https://github.com/quarkusio/quarkus/releases/tag/3.40.1.1 |
| Java | **21** (derzeitige Kartenkatalog-Toolchain); Lucene 10 erfordert mindestens Java 21. Keine unnötige Umstellung auf Java 25 während der Frameworkmigration. |
| Build | Vorhandenen **Gradle Wrapper** (im untersuchten Stand 9.8.2) fortführen, soweit mit Quarkus-Plugin kompatibel. Keine Maven-Migration innerhalb dieses Auftrags. |
| HTTP/API | **Quarkus REST** / REST Jackson; für HTML Quarkus REST + Qute; für Verfügbarkeit SmallRye Health. Keine Spring-Kompatibilitätserweiterung. |
| Templates | **Qute** mit `@CheckedTemplate`, Layouts und typsicheren ViewModels; JTE wird nach Ersatz vollständig entfernt. Qute-Markup wird standardmässig escaped, HTML nur nach nachweisbarer Sanitization als sichere Ausgabe. |
| Suche | **Apache Lucene 10.6.0 verbindlich pinnen**, nicht die ältere Referenzversion 10.3.2 aus `datenportal-sodata` übernehmen. Nur `lucene-core` und `lucene-analysis-common` zunächst einbinden; `lucene-queryparser` nur bei nachgewiesenem Bedarf. **`ByteBuffersDirectory` (Java-Heap, nicht dateibasiert)**, `GermanAnalyzer`, eindeutige ID-Felder, gewichtete Suchfelder. |
| DOM/XML | Bestehende modellbezogene XTF-Parsing-Logik **wiederverwenden** und nach CDI/Quarkus portieren; wenn StAX sinnvoll erscheint, erst nach Verhaltenstests tauschen. Kein eigener generischer INTERLIS-Compiler/Validator. XXE/DTD unterbinden. |
| Katalogstatus | Alle HTTP-Anfragen lesen dieselbe konsistente, beim Start fertig erstellte Momentaufnahme. Wenn XTF/Index fehlerhaft, darf die App **nicht erfolgreich starten** oder „healthy/ready“ behaupten. |
| Runtime | Quarkus JVM **und echtes GraalVM Native Image** sind verbindlich. Auf Apple Silicon darf nativ lokal getestet werden, für Linux/OpenShift ein separates Linux-Artefakt bauen/testen. Kein blosser `nativeCompile`-Erfolg ohne echten HTTP-Prozesstest. |
| UI | Servergerenderte Qute-Seiten + **HTMX**, das Design von `datenportal-sodata` so weit wie sinnvoll nachbilden: Startseite mit **aufklappbarer Listenansicht**, Such-/Filterband, Trefferzahl, Filter-Chips, mobile Filteroberfläche, Detailansicht. Kein neues SPA-Framework. |
| Deployment | XTF vorzugsweise als externer read-only File-Mount, lokale Dev-Standards aus `data/`. Assets same-origin, nicht von ungesicherten Live-CDNs abhängig. |
| Scope | **Kartenkatalog** (Spring/Quarkus-App). Das separate QWC2-Plugin-Repository `edigonzales/qwc-kartenkatalog` wird **nicht** migriert; der API-Vertrag dafür wird erhalten und per Tests geschützt. |

**Warum 10.6.0 und ein RAM-Index?** `sogis/datenportal-sodata` verwendet derzeit Lucene 10.3.2 in `build.gradle`, aber dessen `CatalogSearchIndexBuilder` erzeugt den Index tatsächlich mit `new ByteBuffersDirectory()` im **Java-Heap**. Diese Architektur übernehmen wir, **nicht** den historischen Versions-Pin. Der überschaubare XTF-Katalog wird beim Prozessstart erneut geparst und indexiert; der Aufwand für Dateisystempersistenz, MMap-/FSDirectory-Konfiguration, Formatmigration oder Index-Synchronisation wäre hier unnötig. `MMapDirectory`/`FSDirectory` und persistierte Lucene-Indexdateien sind kein stiller Fallback. Das muss mit Prozess-, JVM- und Native-Tests nachgewiesen werden; Messergebnisse für Heap/RSS/Start- und Indexierungsdauer dokumentieren. Ein Benchmark mit dateibasiertem Index ist nur bei nachgewiesenem Bedarf und gesonderter Freigabe zulässig.

**Begründete Ausnahmen nur nach dokumentierter Evidenz und Nutzerfreigabe:** andere Quarkus-/Lucene-Version, anderer Build, anderer HTTP-/Template-Stack, Wegfall bestehender API-Felder oder Pflichtgates, eine neue DB, oder neues Reload-System. Ein Agent darf Abnahmekriterien niemals allein zur Erreichung von `PASS` abschwächen.

### 0.1 Existierender Stand – zuerst wiederverwenden, nicht wegwerfen

Zum untersuchten Stand existieren bereits:

- `src/main/java/ch/so/agi/meta/XtfCatalogRepository.java`: XTF 2.4 mit XXE-Schutz, 169 Themen/886 Kartenebenen der **mitgelieferten unvollständigen Harvest-Datei**, Validierung von IDs/Referenzen.
- `CatalogRepository`, `CatalogService`, `Thema`, `Ebene`, `CatalogText`: Daten-/Abfragevertrag, HTML-Sanitization über Jsoup, Synonym-/Keyword-Aufbereitung.
- `src/main/java/ch/so/agi/meta/api/CatalogApiController.java` und `CatalogDtos.java`: bereits implementierte JSON-API.
- `src/main/resources/application-xtf.properties`: **bereits DB-freier** XTF-Betrieb; `data/kartenkatalog-harvest-ohne-qml.xtf`.
- `JdbcCatalogRepository`, Spring-Controller, JTE-Templates, Spring-Gradle-/Native-Build: werden **ersetzt**, nicht weiter betrieben.
- Tests wie `XtfCatalogRepositoryTests`, `CatalogApiTests`, `CatalogSearchTests` und `CatalogTextTests`: Verhalten und Sicherheitsanforderungen übernehmen und auf Quarkus-Testinfrastruktur portieren.

Die aktuelle Harvest-XTF ist **kein vollständiger SIMI-Produktions-Snapshot**: teilweise synthetische IDs/`harvest-*`-Themen, teils fehlende Keywords, Synonyme, QML, Organisationen und Berechtigungen. **Keine fehlenden Daten erfinden.** Tests dürfen diese begrenzten Fixtures benutzen, aber nie behaupten, die Datei sei ein vollständiger Produktionsbestand. Die Anzahl 169/886 ist **nur** Eigenschaft der mitgelieferten Fixture, kein festes Produktionsinvariant.

### 0.2 Nicht-Ziele

- Keine Migration des QWC2-Viewers oder des QWC2-Katalog-Plugins; dessen erwartete JSON-API bleibt unverändert nutzbar.
- Kein SIMI-Harvester und kein XTF-Rekonstruktions- oder Export-Job in der Laufzeit-App.
- Keine Datenbank, keine Suchserver (Elasticsearch/Solr), kein Extern-Lucene-Prozess, kein Auth-/Rechtemodell auf Basis unvollständiger Harvest-Felder.
- Keine globalen JavaScript-Frameworks oder komplexes Asset-Bundling; kein vollständiger Port von `datenportal-sodata`.
- Keine unbestätigten „Verbesserungen“ der Daten; insbesondere nicht aus WMS-Abstrakts fehlende SIMI-Schlüssel und Berechtigungen vortäuschen.

## 1. Ergebnis und öffentliche Verträge

### 1.1 API-Kompatibilität – darf nicht brechen

Die vorhandenen Endpunkte des Kartenkatalogs sind die Basis. JSON-Ausgaben bleiben in Feldnamen, Typen und Null-/Listenverhalten gleich, sofern nicht **explizit additiv** ergänzt:

| HTTP | Endpoint | Vertrag |
|---|---|---|
| GET | `/api/v1/catalog` | Objekt `{ "themes": [{ "theme": {…}, "layers": […] }] }`, deterministisch sortiert |
| GET | `/api/v1/themen` | Array der Themen, inklusive `layerCount`, `description`, `descriptionHtml` |
| GET | `/api/v1/themen/{identifier}/ebenen` | Array der zum Thema gehörenden Kartenebenen |
| GET | `/api/v1/ebenen` | Array der Kartenebenen; `q` und optional `thema` bleiben vorhanden; **additive** Facet-/Sortierparameter siehe Abschnitt 5 |
| GET | `/api/v1/ebenen/{id}` | Detail einer Kartenebene per `ident_part` |

Der bestehende `CatalogDtos.Layer` enthält insbesondere `id`, `sourceId`, `type`, `title`, `description`, `descriptionHtml`, `keywords`, `synonyms`, `qwcLayerName`, `derivedIdentifier`, `transparency`, `themeIdentifier`, `themeTitle`, `organization`, `publicationScope`, `downloadAvailable`. **`qwcLayerName` muss identisch zum publikationsfähigen Katalog-Layeridentifier bleiben**; nicht durch synthetische XTF-OIDs ersetzen. Das `CatalogDtos.Theme`-Format ebenso unverändert halten. Optionale skalare Felder dürfen `null` sein, Keyword-/Synonymlisten nie `null`. Quarkus/Jackson-Defaults auf beobachtetes bisheriges Nullverhalten abstimmen.

Fehlerverhalten: `404` für unbekanntes Thema/Layer/QML, `400` für ungültige Query-Parameter (z. B. `q` > 200 Zeichen), `503` für im Betrieb nicht verfügbare Suche (nicht als `[]` verschleiern). Bestehende Problem-JSON-Semantik (`application/problem+json`) erhalten. Grundlegende API-/HTML-Cors-Konfiguration für lokalen QWC2-Dev-Client nur gezielt freischalten; Produktion bevorzugt same-origin Reverse Proxy.

### 1.2 HTML-URLs – sanfte Migration

| URL | Soll nach Migration |
|---|---|
| `/` | Vollständige neue Katalog-Startseite, `200 text/html` |
| `/thema` | Weiterleitung oder Alias zur Startseite, alte Deep Links nicht abbrechen |
| `/themen/{identifier}/ebenen` | Funktionierende Themen-Liste: vollständige Seite ohne HTMX, validiertes HTML-Fragment mit HTMX |
| `/ebene/{id}` | neue Detailansicht mit Metadaten und sicherem Absprung zu `https://geo.so.ch/map?l=...` |
| `/ebene/{id}/style` | QML nur wenn tatsächlich im XTF vorhanden, sonst `404`; korrekter XML-MIME-Type und sichere Content-Disposition |
| `/ping` | bestehendes einfaches Statussignal weiterhin `200` oder klar dokumentierter Alias; neue Health-Endpoints zusätzlich |
| `/q/health/live`, `/q/health/ready` | SmallRye Health; `ready` nur wenn Snapshot und Index vollständig bereitstehen |

Externe Links (`further_info_url`, QWC2-URL, HTML-Metadaten) werden gegen unzulässige Schemas/`javascript:` abgesichert, HTML aus XTF vor einer möglichen Qute-`raw`/`RawString`-Ausgabe explizit mit Jsoup saniert. Qute-HTML-Escaping bleibt sonst aktiv. Roh-QML darf nicht unkontrolliert in HTML eingebettet werden.

### 1.3 Performance und Betrieb

Der aktuelle Datensatz von **886 Ebenen/169 Themen** passt in den Prozessspeicher. Im Speicher sollen mindestens `Map<String, Ebene>`, `Map<String, Thema>`, `Map<String, List<Ebene>>` und der Lucene-Index liegen. Detailabfragen erfolgen aus Maps, **nicht** über Lucene; Lucene liefert IDs und Scores. Keine Dateizugriffe/XTF-Parsing pro HTTP-Request. Noch kein inkrementelles Lucene-Update: bei Neustart alles neu erstellen. Messwerte für Startup, JVM-/Native-RSS und representative Suchanfragen dokumentieren, keine erdachten Speedup-Versprechen.

## 2. Referenzdesign von `datenportal-sodata` – adaptieren, nicht blind kopieren

**Konkret inspizierte Vorlagen (Stand des Referenzcommits oben):**

- `src/main/jte/pages/catalog.jte`, `components/resultsShell.jte`, `components/entryTable.jte`: grosszügige Startseite, Ergebnisliste, expandierbare Zeilen.
- `components/searchForm.jte`, `components/filterBar.jte`, `fragments/catalogResults.jte`: grosses Suchfeld, fachliche Filter, HTMX-Fragmente, aktive Filter-Chips und History-URLs.
- `src/main/resources/static/css/catalog.css`: `dp-catalog-*`, `dp-search`, `dp-filter-*`, `dp-entry-table-*`, Responsive Layout, Fokuszustände.
- `src/main/resources/static/js/catalog-filters.js`: Desktop-Popover, mobile Filter, URL/HTMX-Verhalten.
- `src/main/jte/layouts/main.jte`: Solothurner Header/Breadcrumb, Skip-Link, Footer, lokale Assets.
- `src/test/java/ch/so/agi/datenportal/web/CatalogFiltersPlaywrightTest.java`: echte Browserprüfungen (Chromium/Firefox), Responsive, URL-State, Popover und Listen-Expansion.
- `src/main/java/.../catalog/importxtf/XtfPublishedCatalogParser.java`, `.../search/CatalogSearchIndexBuilder.java`, `.../search/LuceneCatalogSearchIndex.java`, `.../catalog/domain/CatalogSnapshot.java`: Architekturideen und Tests.

**Lizenz:** `datenportal-sodata` und `kartenkatalog` sind MIT-lizenziert. Bei wesentlicher Übernahme von CSS-/JavaScript-Abschnitten Lizenzhinweise/Quelle ordnungsgemäss erhalten; nur benötigte Teile übernehmen. Das bestehende Solothurn-Designsystem `so-web-components` (in der Referenz lokal vendort) bevorzugen; keine zur Laufzeit notwendige CDN-Abhängigkeit. Referenzierte Versionen und Herkunft im Repo dokumentieren.

### 2.1 Vorgesehene Benutzeroberfläche

- **Startseite:** Seite «Kartenkatalog», Kopfbereich und Breadcrumb wie `datenportal-sodata`; erklärender Lead; gut sichtbares Suchfeld und Filterband; Treffer-/Themenzahl; **standardmässig Listenansicht mit aufklappbaren thematischen Zeilen** (nicht Karten-Kacheln als Default). Themenbezeichnung, Kurzbeschreibung und Anzahl Kartenebenen pro Zeile; nach Aufklappen die zugehörigen Kartenebenen als untergeordnete Zeilen (Name, Kurztext, Produktart, Detail- und Kartenlink). Eine Karte zur Vorschau ist **nicht** obligatorisch.
- **Suche:** auf Ebene **und** sinnvoll auf Themen; wenn Textsuche/Filter aktiv, dürfen nur passende Zeilen/Ebenen angezeigt werden; leere Themen nicht stehenlassen. Filterzustand in der URL, Zurücksetzen/Chips und echte Zurück-Navigation. Suchtreffer nicht künstlich nach „neueste“ sortieren – der Katalog besitzt kein verlässliches Änderungsdatum.
- **Fachliche Filter (erste Version):** `Thema` (Pflicht), `Produkttyp` (`datasetview`, `layergroup`, `facadelayer`, `extwms` etc., nur tatsächlich vorkommende Werte), `Organisation/Amt` **nur wenn der Datenstand nennenswerte, echte Werte enthält**. Einen «Aktualisiert»-Filter aus SODATA **nicht** übernehmen. Optional `Verfügbarkeit`/`Download` erst nach Prüfung der Vollständigkeit und verlässlichen Semantik der Quellfelder. Keine Dummy-Filter für vollständig leere Felder.
- **Verhalten:** Zwischen Filtergruppen AND; innerhalb einer Filtergruppe OR. Dynamische Facet-Zahlen nach Suchbegriff und jeweils anderen Filtern; Mehrfachwahl, aktiver Zustand sichtbar; bei leerem Trefferbild klare Meldung und Reset. Einheitliche Sortierung «Titel A–Z» (Standard), optional «Titel Z–A» und «Relevanz» bei aktivem Suchtext; keine irreführende Datums-Sortierung.
- **Aufklappverhalten:** keyboard-/screenreader-zugängliche Buttons/`details`, `aria-expanded` und `aria-controls`. Eine grosse Themenliste muss nicht 886 vollständige Detailbeschreibungen in die Startseite rendern: Themenübersicht initial, Inhalte bei Expansion via leichtgewichtiges HTMX-Fragment laden. Ohne JavaScript bleibt ein normaler Link zur Themen-Detailseite nutzbar.
- **Responsive:** Desktop-Filterpopover ohne Layout-Shift, mobile Filtersheet/einfacher gleichwertiger Fallback, kein horizontales Overflow der wichtigsten Bedienelemente, sichtbare Fokuszustände, klare Labels, ausreichende Kontraste.
- **Details:** thematisch gruppierte Metadaten (Beschreibung, Schlagwörter, Synonyme, Organisation, Typ, Verfügbarkeit soweit vorhanden); Kartenlink; ggf. QML-Download. Keine leeren Felder/Badges. Links und Beschreibungstext aus der XTF sicher rendern.
- **Provenienz:** Die mitgelieferte Harvest-Datei ist unvollständig. Im UI kein unzutreffender Status «vollständig aus SIMI», keine Anzeige erfundener Organisation, Berechtigungen oder QML. «Nicht zugeordnet» ist ein aus dem Harvest stammender Datenzustand, keine eigene redaktionelle Themennorm.

Die visuelle Bewertung ist **kein diffuses subjektives „gefällt mir“**: bei Phase 4 Referenzseiten und neue Seiten nebeneinander bei 1440×900 sowie 390×844 dokumentieren; Header, Breadcrumb, Suchfeld, Filterlayout, Tabellen-/Listentypographie, Expandierbarkeit, Leerzustände und Mobile-Sheet einzeln prüfen. Anpassungen an den Kartenkatalog sind begründet, weil Datums-/Download-Spalten aus SODATA nicht sinnvoll sind.

## 3. Architektur und Klassen-/Methodenspezifikation

### 3.1 Paketstruktur (Ziel; sinngemässe Umbenennung erlaubt, keine Duplikatabstraktionen)

```text
src/main/java/ch/so/agi/meta/
  model/              Thema, Ebene (bestehende Datenverträge), ParsedCatalog, CatalogSnapshot
  xtf/                CatalogXtfSource, XtfCatalogParser, CatalogXtfValidator
  service/            CatalogStore, CatalogService, CatalogBootstrap
  search/             CatalogSearchIndex, LuceneCatalogSearchIndex,
                      CatalogSearchIndexBuilder, CatalogDocumentMapper,
                      CatalogSearchService, CatalogFacetService,
                      CatalogQuery, CatalogSearchResult, SearchHit
  api/                CatalogApiResource, CatalogDtos, ProblemExceptionMapper
  web/                CatalogPageResource, ThemePageResource, LayerDetailResource,
                      CatalogPageVmFactory, CatalogQueryParams, CatalogUrlFactory,
                      HtmlSanitizer/der bestehende CatalogText
  config/             CatalogConfig
src/main/resources/
  application.properties
  templates/          layout.html, catalog.html, theme-layers.html,
                      layer-detail.html, fragments/results.html,
                      fragments/filter-popover.html, fragments/mobile-filters.html
  META-INF/resources/ css/, js/, vendor/   (Quarkus-Static-Asset-Pfad)
```

Codex darf die bestehenden `ch.so.agi.meta`-Records und öffentliche DTOs im bisherigen Package belassen, falls dies die Migration reduziert. **Keine reine Namens-/Package-Umschreibung als Selbstzweck.** Verantwortungen und Methoden müssen aber nachprüfbar getrennt sein.

### 3.2 XTF-Laden und read-only Domain

`CatalogConfig` (`@ConfigMapping(prefix="catalog")` oder gleichwertiges Quarkus Config):
- `xtfLocation()` – Pflichtpfad `file:` oder `classpath:`; Default lokal `file:./data/kartenkatalog-harvest-ohne-qml.xtf`; Produktionspfad aus `CATALOG_XTF_LOCATION`. HTTP-Quelle **nicht** still beim Boot laden; später separat spezifizieren, wenn wirklich benötigt.
- Eindeutige Konfigurationsfehler beim Start; keine automatischen DB-/Mock-Fallbacks in Prod.

`CatalogXtfSource`:
- `InputStream open()` – liest angegebene Quelle; `file:` und `classpath:` mit deterministischer Fehlermeldung; `try-with-resources`.
- Keine XTF-Veränderung, kein DB-Import, keine Netzwerkseitenkanäle.

`XtfCatalogParser` (bestehende `XtfCatalogRepository`-Parserlogik möglichst portieren):
- `ParsedCatalog parse(InputStream)` – **INTERLIS 2.4**, Namespace und Modellnamen `SO_AGI_Kartenkatalog_Publikation_20251210` prüfen; Thema-/Ebene-Objekte und `Thema_R`-Referenz lesen. XML-Schutz: DTD und externe Entities unterbinden (XXE-Test).
- Nur direkte erwartete Elemente lesen, kein unerwartetes Einfangen aus verschachtelten fremden XML-Bäumen; Änderungen gegenüber dem vorhandenen DOM-Code nur mit entsprechenden Negativtests.
- `id` (UUID), `ident_part`, `title`, `dtype` und `Thema_R` prüfen; `transparency` 0..100; nicht definierte optionale Felder `null`; begründete Behandlung von XML-Leerstrings.

`CatalogXtfValidator`:
- `void validate(ParsedCatalog)` – doppelte fachliche Layer-Identifier, Themen-Identifier, TIDs, fehlende/mehrdeutige Thema-Referenzen sowie ungültige Pflichtwerte ausschliessen; bekannte Datenlücken **nicht** mit synthetischen Ersatzwerten füllen.
- Bei fehlerhaftem Transfer **harte Fehlermeldung** mit Objekt/Identifier und Ursache, App nicht ready.

`CatalogSnapshot` (immutable, `AutoCloseable`):
- `List<Thema> themes()`, `List<Ebene> layers()`, `Map<String, Thema> themesByIdentifier()`, `Map<String, Ebene> layersByIdentifier()`, `Map<String, List<Ebene>> layersByTheme()`; `CatalogSearchIndex searchIndex()`; `sourceDescription`, `loadedAt`, `contentSha256` wenn sinnvoll.
- `Optional<Thema> findTheme(String)`, `Optional<Ebene> findLayer(String)`, `List<Ebene> layersForTheme(String)` als O(1)-Map-Zugriffe plus Listenzugriff.
- Listen/Maps defensive immutable kopieren. Lucene/Reader beim Shutdown schliessen. **Kein Reload in MVP**: keine vorsorgliche komplexe `AtomicReference`/Lock-Maschinerie.

`CatalogBootstrap` / `CatalogStore`:
- `CatalogSnapshot loadOnce()` – beim Quarkus-Startup öffnen, parsen, validieren, Index aufbauen, erst dann im `@ApplicationScoped` Store veröffentlichen; bei Fehler Startup abbrechen. Quarkus-Startupmechanismus (`StartupEvent`/`@Startup`) bewusst wählen und testen.
- `CatalogSnapshot current()` – nur nach erfolgreichem Aufbau; `close()` bei `ShutdownEvent`.
- Falls Tests separate Quellen benötigen, pure `load`/`build` Methoden testbar injizieren; keine globalen statischen Mutablen.

`CatalogService`:
- `List<Thema> findAllThemen()`, `List<Ebene> findAllEbenen()`, `List<Ebene> findEbenenByThemaIdentifier(String)`, `Optional<Ebene> findEbeneByIdentPart(String)`, `Optional<Thema> findThema(String)` – bisherige fachliche Semantik weiter anbieten; konsistente de-CH-Sortierung mit Identifier als Tie-Breaker; `Collator` nicht über Threads teilen.

### 3.3 Lucene-Index und Suchlogik

`CatalogDocumentMapper.toDocument(Ebene, Thema)`:
- Pro fachlicher Kartenebene **genau ein Lucene-Dokument**, `ident_part` als gespeicherter Primärschlüssel; verschachtelte Layergruppe nicht doppelt als Teilprodukt indexieren.
- Gewichtete/normalisierte Felder: `identifier` (höchste Präzision), `title`, `keywords`, `synonyms`, `theme title/identifier`, `org_name`, `description` (niedrigeres Gewicht), `dtype` als exakter Filterwert. `CatalogText.plain(...)`/Jsoup vor Beschreibungsindexierung; HTML-Tags nicht indexieren.
- Bei fehlenden Feldern nichts erfinden. Keywords/Synonyme als separate Begriffe neben Titel indexieren; kein leeres Dokument.

`CatalogSearchIndexBuilder.build(List<Ebene>, Map<String, Thema>)`:
- **Lucene-Version 10.6.0** für alle verwendeten Lucene-Artefakte; keine `10.3.2`-Übernahme aus SODATA und keine gemischten Lucene-Versionen im Runtime-Classpath.
- `ByteBuffersDirectory`, `GermanAnalyzer`, `IndexWriter`, `DirectoryReader`, `IndexSearcher`; nach vollständigem Indexaufbau Writer schliessen, den Reader schreibgeschützt für parallele Anfragen verwenden; Reader/Analyzer/Directory beim Shutdown und auf Fehlerpfaden schliessen.
- **Keinen dateibasierten Index** anlegen (`MMapDirectory`, `FSDirectory` oder periodischer Index-Export). Die XTF darf auf einem read-only Dateisystem liegen; Lucene benötigt kein schreibbares Volume. Jeder App-Neustart baut denselben Index aus der XTF neu auf.
- Im Test bei Indexaufbau sowohl **Typ des Verzeichnisses** als auch Dokumentzahl, deterministische ID-Menge, sicheren Ressourcenabschluss und den Wiederaufbau im neuen Prozess prüfen. Kein ausgeblendeter Wechsel auf FSDirectory, wenn die JVM-/Native-Initialisierung scheitert.
- `lucene-queryparser` **zunächst nicht aufnehmen**; kontrollierte `BooleanQuery`-/`TermQuery`-/`WildcardQuery`-Konstruktion verwenden. Eine zusätzliche Parser-Abhängigkeit nur mit begründetem Nutzen und zielgerichteten Tests. Zeichen-/Längenlimits und deterministische Tokenisierung.

`LuceneCatalogSearchIndex.search(String)`:
- Normalisierung: Gross-/Kleinschreibung, ä/ö/ü/é-Akzente, `ß`/`ss`, Interpunktion, mehrfache Leerzeichen; nicht auf Rechtschreibkorrektur spekulieren.
- **AND über Such-Tokens**, **OR über Such-Felder**. Titel/Identifier/Keywords/Synonyme sollen auch **Substring** unterstützen, während längere Beschreibungen auch über Wort-Tokens gefunden werden. Der zweite Token kann in einem anderen Feld stehen. Keine unbegrenzte Suche durch leeres oder 1-Zeichen-Fragment; zwei Zeichen wie `AV` müssen fachlich sinnvoll behandelt werden.
- Treffer-Ranking bei aktiver Texteingabe: exakter Identifier > exakter Titel > Titelpräfix > Titelteilstring > Keyword/Synonym > Thema/Organisation > Beschreibung. Gewichtung als Konstanten und Tests, nicht mit zufälliger Iteratorordnung.
- `List<SearchHit>` mit eindeutiger ID + Score, nicht die gesamte Ebene kopieren; Search-Reader wird bei Fehler **nicht** als leeres Resultat behandelt.
- Für nur ~900 Dokumente darf eine einfach verständliche Wildcard-/Substringlösung verwendet werden, sofern Query-Sicherheit und reale Tests stimmen; keine ausufernde N-Gram-Indexarchitektur ohne Messwert.

`CatalogSearchService.search(CatalogQuery)`:
- Wenn kein sinnvoller Suchtext: gesamte sortierte Liste; sonst Lucene-Treffer in Map rückauflösen, Filter anwenden, sortieren. Standard `Titel A–Z`, optional `Titel Z–A` und bei Textsuche `Relevanz`.
- `CatalogQuery` immutable (`q`, `thema[]`, `organisation[]`, `typ[]`, `sort`, optional `page/size` nur bei expliziter Entscheidung). Aus Vertrag bestehendes `?thema=<ein-wert>` beibehalten, Mehrfachnennung unterstützen. `q` max. 200 Zeichen, leeres `q` kein Indexfehler.
- `CatalogSearchResult` enthält Ergebnisliste, `total`, effektive Query und ggf. Relevanzwerte. Filtersemantik: AND zwischen Kategorien, OR innerhalb. Unbekannte Filterwerte ergeben 0 Treffer oder nachvollziehbares 400 (einheitlich und getestet); **keine** breiten Serverfehler.

`CatalogFacetService.calculate(CatalogQuery, List<Ebene>, ...)`:
- Themen aus echten Thema-Referenzen, Typen aus `dtype`, Organisation nur bei echten `org_name`-Werten. Stabile Auswahl-IDs vs. übersetzte Anzeigenamen.
- Bei dynamischen Counts alle aktiven Filter **ausser** der gerade berechneten Kategorie berücksichtigen; Mehrfachauswahl bleibt erhalten. Eine Kategorie ohne echten Dateninhalt im UI ausblenden.

### 3.4 Quarkus REST und Qute-UI

`CatalogApiResource` (`@Path("/api/v1")`, Quarkus REST, Jackson):
- Methoden `themes()`, `themeLayers(identifier)`, `layer(id)`, `layers(q, thema, organisation, typ, sort)`, `catalog()`; **JSON-DTOs aus `CatalogDtos` wiederverwenden**, präzise Null- und HTTP-Statusverträge.
- Die drei neuen Filterparameter werden optional und additiv eingeführt; alte Clients erhalten die alten Felder und weiterhin funktionierende URLs.
- `CatalogProblemMapper` liefert Fehler im bisherigen Problem-JSON-Format, mit 400/404/503 und ohne Stacktrace/Quelltextleak.

`CatalogPageResource`/`ThemePageResource`:
- `GET /`, `/thema`, `GET /themen/{identifier}/ebenen`, `GET /ebene/{id}`, `GET /ebene/{id}/style`; korrektes `text/html;charset=UTF-8`/QML Content-Type; kein Zugriff auf mutable Requestdaten von anderen Threads.
- `@CheckedTemplate` mit typisierten `CatalogPageVm`, `ThemeRowVm`, `LayerRowVm`, `FilterPanelVm`, `DetailVm`, `CatalogQueryParams`. Wenn Ausdruck nicht existiert, **Build muss fehlschlagen** (nicht allgemeines `.data("x", Object)` für alles).
- Bei `HX-Request: true` nur entsprechendes HTML-Fragment liefern; ansonsten vollständige Seite. Für HTMX Status/Fehler keine leere Fragmente bei Indexausfall.
- Keine JS-abhängige Kernfunktion: `<form method="get">` für Suche/Filter, normale Links zu Themen/Details. HTMX schrittweise optimieren.

`CatalogUrlFactory`:
- `String listUrl(CatalogQuery)` und `String withoutFilter(CatalogQuery, kind, value)` bauen URLs mit sauberem Query-Encoding und erhalten andere Filter; keine handgebaute fehleranfällige Query-String-Konkatenation.
- `String mapUrl(Ebene)` kodiert `l=ident_part` plus vorhandene Transparenz nach dem **bereits im Kartenkatalog verwendeten URL-Vertrag** (0..100 transparent). Keine beliebigen Fremd-URLs aus XTF als ausführbare Links übernehmen.

`CatalogText` / `HtmlSanitizer`:
- Bisherige `description(Ebene)`-Priorität erhalten: vorhandene `description` vor `description_override` vor `description_model`.
- `plain(String)` extrahiert Klartext; `html(String)` sanitisiert HTML (`Jsoup`-Safelist). `keywords/synonyms` mit Leerzeichen, Duplikaten behandeln.
- Qute `RawString` oder `raw` **nur** für nachweisbar sanitisierten HTML-Wert; ungeprüfte Beschreibungen in HTML sind verboten. Linkziel-Schemes testen, `rel="noopener noreferrer"` und ggf. `target` safe.

### 3.5 Gradle-/Native-/Containerverträge

Erwartete Quarkus-Extensions: `io.quarkus:quarkus-rest-jackson`, `io.quarkus:quarkus-rest-qute`, `io.quarkus:quarkus-qute` falls nicht transitiv vorhanden, `io.quarkus:quarkus-smallrye-health`, `io.quarkus:quarkus-arc` nur wenn nötig, Test: `quarkus-junit5`, Rest Assured/JUnit5 und Playwright nach realem Bedarf.

**Lucene-Abhängigkeitsvertrag** (Beispielausschnitt, in das bestehende Gradle-Build integrieren):

```groovy
def luceneVersion = '10.6.0'

dependencies {
    implementation "org.apache.lucene:lucene-core:${luceneVersion}"
    implementation "org.apache.lucene:lucene-analysis-common:${luceneVersion}"
}
```

Weder `lucene-queryparser` noch ein Lucene-Dateisystem-Index sind standardmässig erforderlich. Den **auflösbaren Dependency-Graph** nachprüfen; Quarkus Native muss genau diese Lucene-Version unterstützen. Ein GraalVM-Hint für `org.apache.lucene.util.AttributeFactory` ist aus SODATA bekannt, wird aber für **Lucene 10.6.0 unter Quarkus** nur verwendet, wenn der konkrete Native-Test dies erfordert. **Keine** `spring-*`, `spring-boot-*`, `jdbc-*`, `postgresql`-Runtime, Hikari, Hibernate oder JTE im finalen Dependency-Graph.

Benannte Entwicklungsbefehle sind zunächst Zielbefehle, müssen nach Build-Setup wirklich existieren und erfolgreich laufen:

```bash
./gradlew clean test
./gradlew quarkusDev
./gradlew quarkusBuild
./gradlew playwrightTest          # im Projekt neu definiert, mit Java Playwright wie bei sodata
./gradlew quarkusIntTest          # tatsächliche Quarkus Integration Tests
./gradlew testNative             # native Bild + echte Tests, konfigurierte GraalVM/Mandrel-Umgebung
```

Quarkus Native-Aufruf und Gradle-Testtasks anhand der **3.40.1.1**-Dokumentation prüfen: https://quarkus.io/guides/gradle-tooling/ und https://quarkus.io/guides/building-native-image/. Lucene/GraalVM-Konfiguration (inkl. ggf. Runtime-Initialisierung von `org.apache.lucene.util.AttributeFactory`) nur anhand eines **nachgewiesenen** Native-Build-/Runtimefehlers ergänzen, nicht blind aus Spring kopieren. Der echte native HTTP-Prozess muss XTF parsen, Index aufbauen, HTML und API ausliefern, eine Suche beantworten und fehlenden Layer mit 404 behandeln.

Prod-Betrieb mit externem read-only XTF-Pfad, klaren Env-/Config-Beispielen, HTTP Healthchecks, ohne DB-Secrets. JVM und Linux-Native-Container ausführbar und an OpenShift orientiert, Containerstart als Nicht-Root, keine Privilegien. Keine Docker-Builds vorziehen, bevor JVM-Verträge stimmen.

## 4. Tests – fachliche und technische Ebenen

**T1: Unit/Parser/Domain/JUnit** – normale und negative XTFs, Thema-Referenzen, Escape, Lookup, Sortierung, Facets, Query-Builder; zusätzlich **Lucene 10.6.0**, `ByteBuffersDirectory`, exakte Indexdokumente, fehlender Persistenzpfad und deterministischer Neuaufbau aus derselben XTF. Mit der gelieferten XTF zusätzlich Smoke-Counts 169/886; daneben **sehr kleine** handgeschriebene 2.4-XTF-Testfixtures für Randfälle.

**T2: Quarkus REST-/Template-Integration** – `@QuarkusTest` + HTTP-Client, exakt benannte Endpunkte und JSON-Felder, `404/400/503`, HTML-Escaping, QML, HTMX-Fragment-Header, Start mit fehlerhafter XTF muss fehlschlagen. JSON-Vertrag gegen vor der Migration gesicherte **baseline Fixtures** prüfen; nicht bloss `200`.

**T3: Browser/UX** – mindestens Chromium und Firefox über Playwright; Browser tatsächlich öffnen, Desktop und Mobile testen. Suche, Mehrfachfilter, URL-Back/Reload, Reset, Akkordeon, Detail-/Kartenlink, No-JS-Fallback, leerer Zustand, fehlende Metadaten; Layout/Referenzvergleich bei Desktop und Mobile. Playwright nicht einfach per Tag skippen; `playwrightTest` als ausdrücklicher Pflicht-Gradle-Task.

**T4: Distribution/Native** – gebaute Quarkus-JVM im **separaten Prozess** sowie eine **wirkliche** GraalVM-Native-Executable im separaten Prozess, bei beiden **Lucene 10.6.0 auf `ByteBuffersDirectory`** tatsächlich beim Start aufbauen und per HTTP suchen (einschliesslich Treffer-/Ranking-Parität), auch mit **read-only Index-/Datenverzeichnis ohne persistierte Lucene-Dateien**; Indexausfall & Defekt-XTF mit korrekt fehlschlagendem Start; `quarkusIntTest`, `testNative` und/oder gleichwertige nachvollziehbare Befehle. Linux-Image-Check für OpenShift; lokal macOS arm64-Native als zusätzliche Evidenz, nicht als Linux-Abnahme.

**Negativ-Canaries / Fehlgrün-Erkennung:** eine mutierte Themenreferenz muss Parsing-/Startup-Test scheitern lassen; ein manipuliertes HTML-`<script>` darf nicht unescaped erscheinen; bewusst geänderter DTO-Feldname muss API-Vertragstest scheitern lassen; ein absichtlich falsch gesetzter Filter muss Ergebnistest zum Scheitern bringen; ein nicht ausführbarer Pflicht-Browser-/Native-Test ist **BLOCKED**, nie PASS. Keine künstliche riesige Evidenzplattform aufbauen.

**Tests gelten nur bei tatsächlicher Ausführung**. Ein historischer Bericht ersetzt keinen aktuellen Exitcode. Tests nicht `@Disabled`, `-x test`, `--continue` mit Fehler ignorieren oder Assertions an ein neues fehlerhaftes Verhalten angleichen. An Phasenenden laufen alle bisher fälligen T1/T2 und bei UI-Phase T3, bei finaler Phase zusätzlich T4 neu am aktuellen SHA.

## 5. Arbeits- und Reviewprozess (nach GRETL-Muster; verbindlich)

**A = Codex (Implementierung).** **B = OpenCode GUI 2.x, DeepSeek API / DeepSeek 4.1 Flash 1017 (unabhängiger Reviewer).** Kein anderer Codex-Thread, keine Selbstprüfung durch denselben Agenten anstelle von B. Von B gewählte Provider-/Modell-Einstellung in OpenCode GUI belegen (nur Modellbehauptung im Chat genügt nicht). Ein korrekt eingerichtetes GUI benötigt **keine** neue `DEEPSEEK_API_KEY`-Umgebungsvariable im Codex-Terminal. Keine CLI-/Automationsintegration erfinden: wenn Codex das GUI nicht tatsächlich selbst bedienen kann, **sicher anhalten und einen manuellen Handoff liefern**.

**Genau drei Reviewergebnisse: `PASS`, `FAIL`, `BLOCKED`.** Eine Phase/Lieferung ist **erst dann abgeschlossen**, wenn ihr unabhängiger Review **alle** Ziele, Risiken und Tests mit `PASS` bestätigt hat. Die Reviewprüfung ist nicht bloss Testausführung: der Reviewer muss zusätzlich tatsächlichen Code, API-/UX-Ziele, Sicherheit, Betriebsverträge und eigene Gegenbeispiele überprüfen.

**Ablauf pro Lieferung:**

1. Agent A prüft `pwd`, `git status --short`, `git rev-parse HEAD`, Gradle/Java/GraalVM, lokale Änderungen, aktives `AGENTS.md` (falls vorhanden), Plan und letzten echten Review. **Uncommittete fremde Änderungen unangetastet lassen.** Falls Git-Worktree unsauber, nicht blind löschen.
2. A implementiert **genau eine** fachlich geschlossene Lieferung und deren T1/T2/T3/T4-Gates. Fällige Tests frisch und mit echten Assertions ausführen.
3. A schreibt `docs/quarkus-migration/implementation/<ID>.md` vor dem Commit mit Ausgangs-SHA, AC-IDs, tatsächlichen Änderungen, Testkommandos **und Exitcodes**, Anzahl Tests, Failures/Errors/Skips, Abweichungen, nicht ausgeführten Pflichtchecks und Berichtspfaden.
4. A committet Code, Tests und Implementierungsbericht **lokal**, liest vollständigen `CANDIDATE_SHA`. Keinen selbstreferenziellen Kandidaten-SHA in die Datei schreiben, die zum Commit gehört; den Handoff separat ablegen/ausgeben.
5. B öffnet ein **separates, sauberes Git-Worktree** am `CANDIDATE_SHA` (auf macOS z. B. `git worktree add --detach /tmp/kartenkatalog-review-<ID> <CANDIDATE_SHA>`), startet in **neuem OpenCode-GUI-Kontext** mit dem konkreten Prüfauftrag. B darf Kandidatencode und Tests **nicht verändern**. Eigene neue Gegenproben nur im eigenen separaten Reviewbereich.
6. B prüft jedes AC einzeln: Quellcode, tatsächliches Verhalten, Testassertions und eigene Gegenprobe sowie aktuelle Rohreports. Bei letzter Lieferung einer Phase **kumulative Gates** aller bisherigen Lieferungen derselben Phase (nicht nur die letzten Diff-Zeilen).
7. B erstellt Reviewbericht ausserhalb des geprüften Commits: `docs/quarkus-migration/reviews/<ID>-review.md` kann **nach** dem Review als reiner Dokumentationscommit übernommen werden. Solcher Docs-Commit ersetzt/ändert den geprüften Code-SHA nicht.
8. **`PASS`**: nächster Lieferabschnitt darf beginnen. **`FAIL`**: derselbe Lieferabschnitt wird korrigiert, neuer Codecommit, kompletter neuer Review. **`BLOCKED`**: fehlende Voraussetzungen konkret nennen und nicht fortfahren. Ein fehlendes GraalVM, Playwright, Modellprovider oder fehlende Baseline ist ein echter Blocker, keine stillschweigende Freigabe.

Zwischen einem und dem nächsten Lieferabschnitt **keine stillen Autopasses**. Wenn der Benutzer die OpenCode-Prüfung manuell startet, bleibt Codex bis zur tatsächlich übergebenen `PASS`-Abnahme im Zustand `AWAITING_REVIEW`. Keine Hintergrundarbeit versprechen. Nur echte Nutzerentscheidungen, nicht technische Routinen, werden zur Eskalation vorgelegt; einen technischen FAIL eigenständig nacharbeiten.

**Status:** eine knappe `docs/quarkus-migration/STATUS.md` mit aktuellem ID, Zustand (`PLANNED`, `IN_PROGRESS`, `AWAITING_REVIEW`, `PASS`, `FAIL`, `BLOCKED`), letztem bestätigtem PASS-SHA, Blockern und nächstem Schritt. Kein zusätzliches Ticketsystem, keine unnötigen riesigen Compliance-Dokumente. Rohberichte unter `build/reports/`/CI-Artefakten, nicht kiloweise ins Repo.

## 6. Verbindliche Lieferabschnitte und Abnahmekriterien

Reihenfolge ist **P0 → P1 → P2 → P3a → P3b → P4 → P5**. P3a und P3b bilden gemeinsam die UI-Phase 3. **Jeder** Abschnitt hat einen eigenen unabhängigen Review. Der P3b-Review umfasst das gesamte UI-Phasengate. P5-Review umfasst den **kumulativen Gesamtabschluss**. Nach P0-Review beginnt P1 nur bei bestätigtem `PASS`.

### P0 – Baseline und Migrationsinventur (ohne Produktfunktionalität zu verändern)

**Auftrag:** aktuelle Spring-App auf `xtf` starten, API-/HTML-Vertrag vermessen, Tests laufen lassen, Daten-/Sicherheitshinweise erfassen; Referenz-UI und Build-/Native-Situation dokumentieren. Keine neue parallele Architektur.

| Kriterium | Abnahme |
|---|---|
| **AC0-01** | Tatsächlicher HEAD/Buildversion/Quellstruktur erfasst; aktueller `xtf`-Start, `./gradlew test`, HTTP `/api/v1/catalog`, `/api/v1/ebenen`, `/api/v1/themen`, HTML `/thema`, `/ebene/{id}` wurden **ausgeführt** oder konkrete Baseline-Blocker nachgewiesen. |
| **AC0-02** | Minimaler **maschinenlesbarer API-Contract** für bekannte 886er-Fixture: DTO-Feldnamen, `null`/leere Listen, Statuscodes, repräsentative Thema-/Ebene-/QML-Fälle; Tests/Fixtures in `src/test/resources/contract/`, keine riesige Vollkopie sensibler Live-Daten. |
| **AC0-03** | Kurze Refactoring-Matrix (`KEEP/PORT/REMOVE`) für XTF, Datenrecords, Services, REST, HTML, Tests, JTE, JDBC, Spring, Container; UI-Referenzliste aus `datenportal-sodata`, kein unnötiger Gesamtcodeabdruck. |
| **AC0-04** | Prüfer verifiziert Baseline selbst, erkennt mindestens einen Vertrag, den eine blosse `200`-Prüfung nicht entdeckt (z. B. `qwcLayerName`, fehlendes QML, `404`); Implementierungscommit/Handoff korrekt. |

### P1 – Quarkus-Laufzeit, XTF-Snapshot, bestehende API und minimale HTML-Seiten

**Auftrag:** Build-Plugin/Dependencies auf Quarkus umstellen, sichere XTF-Lade-/Validierungslogik und read-only Katalogservice portieren, REST-API verhaltensgleich anbieten, minimale Qute-Seiten für bisherige Deep Links, alte Spring-Runtime vollständig entfernen. **Keine Lucene-Suche als Scheinabschluss**: bis P2 nur die bewusst dokumentierte bisherige lineare Suche hinter gleichem API-Vertrag.

| Kriterium | Abnahme |
|---|---|
| **AC1-01** | `./gradlew test`, `quarkusBuild`, `quarkusDev` und startbares JVM-Artefakt; Quarkus 3.40.1.1, JDK21, kein Spring/JDBC/Postgres/Hikari/JTE zur Laufzeit; `./gradlew dependencies --configuration runtimeClasspath` geprüft. |
| **AC1-02** | Reale XTF 169/886; Parser validiert Modell/Referenzen/ID/XXE und lehnt defekte, doppelte, fremde/fehlende Dateien beim Start ab; kein DB-/Mock-Fallback. Kleinfixtures mit Positiv- und Negativfällen. |
| **AC1-03** | `GET /api/v1/{catalog,themen,ebenen,...}` und alle fünf API-Formen mit den **P0-Contract-Assertions**; Fehler-Codes und JSON (auch `null`/Arrays) erhalten; keine Umbenennung von `qwcLayerName`. |
| **AC1-04** | `/`, `/thema`, `/themen/{id}/ebenen`, `/ebene/{id}`, QML/404 und `/ping` geben minimal brauchbares HTML/Antworten; Qute compile-time validation; `ready` nur mit vollständigem Snapshot. Prüfer führt **eigene** API-/XXE-/Startfehlprobe aus. |

### P2 – In-Memory-Lucene, Suche und fachliche Facets

**Auftrag:** Indexaufbau beim Start, Suchservice und konsistente Facets; REST-API-Suche auf neue Engine umstellen. Noch keine aufwendige UI, aber vollständiger API-/Suchvertrag.

| Kriterium | Abnahme |
|---|---|
| **AC2-01** | **Lucene 10.6.0** im tatsächlich aufgelösten Dependency-Graph; genau ein Dokument je fachlicher Ebene, Counts und IDs korrekt; `ByteBuffersDirectory`/`GermanAnalyzer` nachweislich **heapbasiert, ohne Index-Dateien**, korrekte Ressourcen-Schliessung und deterministischer Neuaufbau aus XTF bei Prozessneustart. Bei Indexfehler Startup/Ready bzw. 503, niemals falsch-leere Treffer. `lucene-queryparser` nur mit belegtem Bedarf. |
| **AC2-02** | Positiv-/Negativtest für Identifier, Titel, Substring, Keywords, Synonyme, Themen, Organisation, Beschreibung, Umlaute/ß, AND-über-Tokens/OR-über-Felder, 2-Zeichen-Fall `AV`, Cross-Field-Query und Ranking-Tie-Breaker. **Fehlende** Harvest-Metadaten mit separaten synthetischen Testfixtures, nicht durch Produktionsfiktion prüfen. |
| **AC2-03** | Filter `thema`, `typ`, optional `organisation`, mehrfache Parameter, AND/OR, Null-/Missing-Handling, dynamische Facetcounts, stabile Auswahl-IDs; `q` max200/400, nicht leere XTF als 0 Treffer verfälschen. |
| **AC2-04** | API-Rückwärtskompatibilität P0/1, kontrollierte Sortierung, separater JVM-Prozess-Smoke mit **frischem RAM-Index**, aktuelle JVM-Testreports; Reviewer entwickelt **neue Gegenprobe** für Treffer-Ranking oder über Felder verteilte AND-Suche und prüft Query-/Filtercode **sowie den Nicht-Dateisystem-Indexpfad**. |

### P3a – Qute-/Sodata-Design: Startseite, Liste, Aufklappen, Details

**Auftrag:** neue Qute-Seiten im SODATA-Stil; bestehende einfache HTML-Seiten ersetzen, Links/Akkordeon/Detailansicht mit realem XTF. Suche/Filter noch in einer einfachen GET-Form möglich; vollständige HTMX-Mobile-Filter erst P3b.

| Kriterium | Abnahme |
|---|---|
| **AC3A-01** | Qute `@CheckedTemplate` (Build schlägt bei fehlenden Feldern fehl), geteiltes Layout mit lokalem Solothurn-Header, Breadcrumb, Footer, Skip-Link, CSS-Tokens/Typografie ähnlich SODATA; keine JTE-/CDN-Laufzeitpflicht. |
| **AC3A-02** | Startseite ist primär **aufklappbare Listenansicht** fachlicher Themen mit korrekter Anzahl und zugänglichen Unterzeilen; normale Links funktionieren **ohne JS**; HTML-Responses für Themen und Detailseiten inklusive fehlerhafte IDs/404. |
| **AC3A-03** | Metadaten: Beschreibungen lesbar, fehlende Angaben weggelassen, Links/QML/QWC2-Absprung korrekt; HTML/XSS-Gegenfälle (`<script>`, `onerror`, `javascript:`) sanitisiert, Qute raw nur nach Jsoup; keine künstlichen Org-/Permissions-Badges. |
| **AC3A-04** | Browser-Desktop-Smoke (mind. Chromium), visuelle Referenzscreenshots und Review der Ziele; Reviewer führt eigenen Accessibility-/XSS- oder Deep-Link-Gegenfall. Aktueller kumulativer JVM/API-Testlauf grün. |

### P3b – Filter-/Such-UX, HTMX, Responsive und unabhängiges UI-Phasengate

**Auftrag:** Such- und Filterleiste wie in SODATA mit adäquaten Kategorien, HTMX-Fragmente, Desktop-Popover, Mobile-Sheet, URL-State, Ergebniszusammenfassung, Leerzustände; kein Datumsfilter und keine nicht vorhandenen Felder.

| Kriterium | Abnahme |
|---|---|
| **AC3B-01** | Filter `Thema`, `Produkttyp` und `Organisation` nur mit echten Werten; Textsuche/Mehrfachwahl/Reset/Chips/Count und AND/OR wie P2; gleiches Ergebnis in HTML, HTMX und JSON. Keine leeren Filter, keine „Aktualisiert“-Attrappe. |
| **AC3B-02** | HTMX fragmentiert nur Ziele, kein HTML-in-HTML, URL wird aktualisiert, Browser-Back/Reload/Deep-Link erhalten Filter, Aufklappbarkeit bei Filteränderung stabil; ohne JS vollständiger GET-Submit nutzbar. |
| **AC3B-03** | Chromium+Firefox Playwright: Desktop (1440×900), Mobile (390×844), Tastatur/Fokus, Popover/Sheets, Layout-Shift/Overflow, leerer Treffer, Such-Reset, Link, HTML-Ausgabe; echte Screenshots mit Vergleich zur SODATA-Referenz. |
| **AC3B-04** | Reviewer prüft visuelles und funktionales **Gesamtziel** P3a+P3b unabhängig, macht neuen Browser-Gegenfall, führt T1+T2+T3 frisch aus und bewertet sämtliche `AC3A-*`/`AC3B-*`; UI-Phase erst mit PASS abgeschlossen. |

### P4 – Produktionsbetrieb, Native Image, realistische Integrationsprüfungen

**Auftrag:** Quarkus Native, Runtime-/Container-Konfiguration, Smoke-/Startup-Verhalten, Gesundheitsprüfungen, CI/README/Operations; möglichst einfacher OpenShift-Betrieb, keinerlei DB.

| Kriterium | Abnahme |
|---|---|
| **AC4-01** | Gradle JVM-Distribution und echte Native-Executable mit **Quarkus 3.40.1.1 und Lucene 10.6.0** erzeugt; GraalVM-/Mandrel-Setup dokumentiert und im tatsächlich geprüften Build sichtbar; **Native-Start baut den Lucene-Index mit `ByteBuffersDirectory` aus realer XTF erfolgreich auf**. Kein heimliches Downgrade auf Lucene 10.3.2 oder Wechsel zu dateibasiertem Index. |
| **AC4-02** | Echte separate HTTP-Prozesse: JVM und Native beantworten Liste, Detail, **Lucene-Suche/Ranking/Filter**, HTMX und JSON mit Parität; **keine persistierten Indexdateien**, nur XTF-Datei als Eingabe. Absichtlich fehlerhafte XTF verhindert Start/Ready; Native-Fehler nicht per JVM-Tests überdecken. |
| **AC4-03** | Health-Endpoints, Logging (Anzahl Einträge, Quelle, Indexdauer, Fehler ohne Secrets), GitHub Actions/CI oder nachvollziehbarer reproduzierbarer Local-Build; Linux-Native/Container-Smoke, nicht nur macOS-native. Externer read-only XTF-Mount und Neustartablauf dokumentiert. |
| **AC4-04** | Reviewer prüft **JVM-vs-Native-Parität** und einen selbst erfundenen Native-/Startfehlerfall; frische T1–T4 und Security-/Abhängigkeitsprüfungen, kein Pflichtskip. |

### P5 – Cleanup, Migration abgeschlossen, unabhängige Gesamtabnahme

**Auftrag:** nicht mehr benötigten Spring-Boot-/JTE-/JDBC-Code, Gradle-Tasks, Konfiguration und Doku entfernen, QWC2-API-Contract und Nutzerwege final überprüfen, Build-/Runtime-Abhängigkeiten prüfen und alle vorherigen Ziele kumulativ abnehmen.

| Kriterium | Abnahme |
|---|---|
| **AC5-01** | Kein aktiver Spring-Boot-/JTE-/JDBC-/PostgreSQL-Betriebscode oder -Dependency; `grep`/Dependency-Graph geprüft, historische Dokumentationsverweise dürfen als historische Kontexttexte bleiben; keine unabsichtlichen Git-Änderungen. |
| **AC5-02** | README/Runbook: lokale Quarkus-Dev-/JVM-/Native-Kommandos, XTF-Quellkonfiguration, Quellstand/Unvollständigkeitswarnung, API, QWC2-Dev-CORS, Deployment, Tests, Troubleshooting; alle nach Möglichkeit durch tatsächlichen Lauf geprüft. |
| **AC5-03** | Vollständige API-Regression inklusive P0-Baseline, alle XTF-/Lucene-**10.6.0**-/`ByteBuffersDirectory`-/Facet-/UI-/Qute-/Security-/Browser-/Native- und Prozess-Gates auf demselben finalen SHA; **JVM und Native indexieren bei Start aus XTF** und liefern die vereinbarten Suchergebnisse; 0 Fehler, 0 unzulässige Skips, keine ungeprüften Annahmen als PASS. |
| **AC5-04** | **Unabhängige Gesamtabnahme:** OpenCode/DeepSeek prüft die Ziele in Abschnitt 0, API-Vertrag, DB-Freiheit, Search-Qualität, SODATA-ähnliche UI, Qute-Template-Sicherheit, Betriebs-/Nativefähigkeit, Tests und alle bisherigen Phasenkriterien **kumulativ**; eigene neue Gegenprobe; ausdrückliches PASS des finalen Commits. |

**Wichtig:** Nach einer P4-Native-Abnahme auftretende Code-/Build-Änderungen in P5 verlangen **erneute** Native-Tests und einen neuen Review des finalen SHAs. Dokumentations-only-Commit mit unverändertem Reviewbericht ist die einzige Ausnahme ohne erneuten Code-Review.

## 7. Ausführung und Reviewberichte (verbindliche Formate)

### 7.1 Implementierungsbericht vor Commit

```text
Lieferabschnitt und Kriterien-IDs:
BASE_SHA (vollständig):
Geänderte Dateien und warum (inkl. public API / UI / Runtime):
Erhaltener Altvertrag und Unterschiede:
Sinnvolle neue Tests und explizite Negativ-/Gegenfälle:
Ausgeführte Kommandos, Exitcodes, JUnit Tests/Failures/Errors/Skips:
HTTP-/Browser-/Native-Prozessnachweise (wenn fällig):
Vorher-/Nachher-Beobachtungen und echte Messwerte:
Offene Probleme / nicht ausgeführte PFLICHTGATES und Begründung:
Potentielle Reviewer-Risiken:
Handoff an OpenCode nach lokalem Commit:
```

### 7.2 Review-Handoff, erst **nach** dem Commit

```text
Repo: edigonzales/kartenkatalog
Lieferabschnitt: <P0|P1|P2|P3a|P3b|P4|P5>
BASE_SHA: <vollständiger SHA>
CANDIDATE_SHA: <vollständiger SHA>
Kriterien-IDs: <konkrete ACs>
Phasengate/kumulativ: <ja/nein>
Implementierungsbericht: docs/quarkus-migration/implementation/<ID>.md
Plan: docs/quarkus-migration/EXECUTION_PLAN.md
Reviewprompt: docs/quarkus-migration/OPENCODE_REVIEW_PROMPT.md
Ausstehende Pflichtprüfungen: <keine / konkret>
```

### 7.3 Unabhängiger Reviewbericht

```text
Prüfer: OpenCode GUI 2.x / DeepSeek API / exakt gewählte Modellkennung:
Paket/Phase/Kumulativ:
BASE_SHA/CANDIDATE_SHA; git-HEAD/sauberer Review-Worktree:
Eigenständige Code-/Verhaltensanalyse:
Tabelle: JEDE AC-ID | erfüllt/verletzt/nicht prüfbar | konkreter Beleg | Finding:
Eigener NEUER Gegenfall: Input, Erwartung, Kommando, Output, Interpretation:
Pflichttests: Befehle, Exitcodes, Tests/Errors/Failures/Skips, frische Rohreports:
Browser-/Screenshot-/native-Prozessnachweise (wo fällig):
Kumulatives Phasengate und identifizierte Wechselwirkungen:
Findings und nachprüfbare Reproduktion:
Urteil: PASS / FAIL / BLOCKED (+ INFRA/MISSING_EVIDENCE/DECISION):
Freigabe oder konkrete Nacharbeit:
```

**Eine Review-Tabelle pro AC, nicht nur „Tests erfolgreich“.** Bei `BLOCKED` konkrete fehlende Voraussetzung. **Keine** vom Reviewer in den zu prüfenden Produktcommit hineingeschriebenen Teständerungen.

## 8. Dokumentation der Quellen und Gültigkeit

- Quarkus LTS/Versionen: https://quarkus.io/releases/ ; geprüfte Version: https://github.com/quarkusio/quarkus/releases/tag/3.40.1.1
- Quarkus REST/Qute mit typisierten Templates und HTML-Escaping: https://quarkus.io/guides/qute-reference/ ; https://quarkus.io/guides/qute/
- Quarkus/Gradle inklusive Native-Tests: https://quarkus.io/guides/gradle-tooling/ ; https://quarkus.io/guides/building-native-image/
- SODATA Suche/Snapshot: `sogis/datenportal-sodata` – `CatalogSearchIndexBuilder` (**`ByteBuffersDirectory` im Heap, dort noch Lucene 10.3.2**), `LuceneCatalogSearchIndex`, `CatalogSearchFields`, `CatalogDocumentMapper`, `CatalogSnapshot`, `CatalogReloadService`. Die Kartenkatalog-Migration pinnt dagegen Lucene **10.6.0**.
- SODATA UX: `src/main/jte/pages/catalog.jte`, `src/main/jte/components/{entryTable,searchForm,filterBar,resultsShell}.jte`, `src/main/resources/static/css/catalog.css`, `CatalogFiltersPlaywrightTest`.
- Kartenkatalog: bestehende README, XtfCatalogRepository, CatalogDtos, API-Controller und mitgelieferte Test-XTF.
- Verbindliches Reviewverfahren: die vom Nutzer bereitgestellten Beispiele `EXECUTION_PLAN.md`, `CODEX_STARTPROMPT.md`, `OPENCODE_REVIEW_PROMPT.md` aus dem INTERLIS-Java-Projekt dienen **als Verfahrensvorbild**, nicht als fachliche INTERLIS-G2-Anforderungen.

## 9. Jetzt beginnen

1. Diese drei Markdown-Dateien in `edigonzales/kartenkatalog/docs/quarkus-migration/` einchecken, ohne Nutzerdaten oder fremde Änderungen zu überschreiben.
2. Codex mit `CODEX_STARTPROMPT.md` beauftragen. **Nur P0** ausführen, noch nicht Quarkus vollständig migrieren.
3. Nach Codex-Commit `BASE_SHA` und `CANDIDATE_SHA` an einen **neuen** OpenCode-GUI-Kontext mit DeepSeek API / DeepSeek 4.1 Flash 1017 übergeben; `OPENCODE_REVIEW_PROMPT.md` anwenden.
4. Nur ein echtes `PASS` auf dem geprüften SHA erlaubt den Beginn von P1. Dasselbe Vorgehen bis P5 durchziehen.

**Erfolgsdefinition:** vollständig DB-freier Quarkus-Kartenkatalog aus XTF, fachlich gute Lucene-Suche, dem SODATA-Design angeglichene barrierearme Qute-/HTMX-Oberfläche, intakter QWC2-API-Vertrag, echter nativer Betrieb **und für jede Lieferung eine unabhängige, zielbezogene DeepSeek-Abnahme**.
