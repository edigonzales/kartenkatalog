# Kartenkatalog – gründliche Bereinigung nach der Quarkus-Migration

**Verbindlicher Umsetzungs- und Abnahmeplan · Version 1.0 · 1. November 2026**  
**Repository:** <https://github.com/edigonzales/kartenkatalog>  
**Implementierung:** Codex im **bestehenden Repository**  
**Unabhängige Prüfung:** OpenCode 2.x auf macOS, DeepSeek API, **DeepSeek 4.1 Flash 1017** (konkret gewählte Provider-/Modellkennung dokumentieren)  
**Verfahrensvorbild:** die erfolgreich verwendeten Quarkus-/INTERLIS-Umsetzungspläne mit lokalem Kandidatencommit, unabhängigem Review und `PASS`/`FAIL`/`BLOCKED`.

**Recherchebasis:** öffentlicher `main` am 1.11.2026, Commit `f37b19598d806294121f681a65c2dd93079ce520` (184 Git-Tree-Einträge). **Keine Start-Festschreibung**: Zu Arbeitsbeginn `git rev-parse HEAD`, `git status` und das aktuelle Dateiinventar erneut ermitteln. Auch seit diesem Audit vorgenommene Änderungen respektieren. Historische Prüfergebnisse beziehen sich ausschliesslich auf ihren damaligen SHA.

## 0. Auftrag, Ergebnis und absolute Grenzen

Der Kartenkatalog ist bereits auf **Quarkus 3.40.1.1 / Java 21 / Gradle**, **XTF-only**, einen **immutable CatalogSnapshot** mit **Apache Lucene 10.6.0 auf `ByteBuffersDirectory`**, **Qute**, **HTMX** und **GraalVM Native Image** migriert. Die Anwendung hat öffentliche `/api/v1`-Verträge für das getrennte QWC2-Plugin, Browserprüfungen, JVM-/Native-Integrationstests und eine produktiv genutzte Docker-Hub-Releasepipeline. **Dieser Auftrag ist ein Cleanup, keine zweite Migration.**

### 0.1 Positiv definiertes Ziel

Nach dem Cleanup:

1. befinden sich im aktiven Root **keine entbehrlichen alten Datenbank-, XTF- oder GUI-Mockup-Artefakte**;
2. haben **Java-Produktionsklassen, Java-Testklassen, aktive Testressourcen, Scripts und ihre generierten Reportpfade** fachlich aussagekräftige, **phasenunabhängige Namen** (`P3ITTest`, `p0-http-contract.py` usw. dürfen dort nicht mehr vorkommen);
3. existieren **keine stillgelegten Spring-/JTE-/JDBC-/DuckDB-/PostgreSQL-Implementierungen** im aktiven Build, ohne dass noch relevante Test- oder Betriebsfunktionen verlorengehen;
4. sind `README.md`, Architektur-, Betriebs- und Testdokumentation **Ist-Beschreibungen** der funktionierenden Anwendung und ihrer aktuellen Befehle;
5. werden die wertvollen **historischen Implementierungs- und unabhängigen Reviewberichte unverändert und nachvollziehbar archiviert**;
6. ist eine bekannte **inhaltlich irreführende Laufzeitmeldung** (fest eingebauter Hinweis auf den unvollständigen Harvest) durch eine korrekte, explizite Konfiguration ersetzt;
7. bleiben **alle bestehenden funktionalen, Sicherheits-, Browser-, JVM-, Native-, Docker- und Releaseverträge** mindestens ebenso streng nachprüfbar wie vorher.

### 0.2 Strikte Nicht-Ziele

- **Kein neues Produktrepo** und keine Parallelimplementierung.
- **Keine** neue Datenbank, ORM, Cache-Infrastruktur, Suchserver, Persistenz des Lucene-Index, Dateisystemindex oder Reload-Mechanik.
- **Keine** fachlichen Änderungen an Suchlogik, Lucene-Ranking, Facets, Metadaten, XTF-Modell, API-Feldern oder Darstellungsfunktionen, sofern nicht die explizite Provenienzkorrektur betroffen ist.
- **Keine** Quarkus-/Lucene-/Playwright-/Gradle-/GraalVM-Versionsaktualisierung im Rahmen dieses Auftrags; insbesondere **Lucene 10.6.0**, `ByteBuffersDirectory`, Java 21, Quarkus 3.40.1.1, Playwright 1.60.0 beibehalten.
- **Keine** Umstellung von Python auf Java oder umgekehrt; die vier Python-Prozessharnesses sind aktiv und sollen **umbenannt, nicht entfernt oder neu erfunden** werden.
- **Keine** Neuimplementierung der SODATA-Oberfläche, kein genereller Architekturumbau, keine Neuformatierung aller Quellen.
- **Keine** Änderungen an bestehenden öffentlichen Releases oder Docker-Hub-Tags ohne separaten Auftrag.
- Keine historischen Reviewberichte, Testbelege oder frühere SHA-Aussagen nachträglich umdeuten.

### 0.3 Geprüfte technische Invarianten – nie verlieren

| Bereich | Unveränderlicher fachlicher Vertrag |
|---|---|
| Katalog | `data/kartenkatalog-harvest-ohne-qml.xtf` als aktive Entwicklungsfixture, 169 Themen/886 Kartenebenen **nur für diese Fixture**. Daten und Hash unverändert; echte Produktions-XTF kann abweichen. |
| XTF/Start | XTF 2.4/Modell `SO_AGI_Kartenkatalog_Publikation_20251210`, sichere Parserfehler (XXE/DTD, doppelte IDs/UUIDs, falsche Themenreferenzen), kein DB- oder Mock-Fallback; ungültiger Input verhindert Ready/Start. |
| Suche | Lucene 10.6.0, `ByteBuffersDirectory`, `GermanAnalyzer`, AND über Suchwörter/OR über Felder, definierte Rangfolge, sichere Querybehandlung, keine Indexdateien. |
| API | `/api/v1/catalog`, `/api/v1/themen`, `/api/v1/themen/{identifier}/ebenen`, `/api/v1/ebenen`, `/api/v1/ebenen/{id}`, `/api/v1/facets` inkl. JSON-Feldnamen, `null`/`[]`, Publikations-ID `qwcLayerName == id`, Filter/Suche/Sortierung/Problem-JSON. |
| HTML | `/`, `/thema`, `/themen/{identifier}/ebenen`, `/ebene/{id}`, `/ebene/{id}/style`, `/ping`, Qute/HTMX, No-JS-Fallback, Link-/HTML-Sanitization, QML-Download nur falls vorhanden. |
| Accessibility/UX | Native Themen-`details`, Desktop-Filter, mobiles Filtersheet, Chips, Browser-History, Fokus, HTMX-Fragmente, Chromium+Firefox/1440×900 und 390×844. |
| Runtime | JVM + echtes GraalVM/Mandrel Native Image; native Lucene-Reflection-Hint; Readiness/Liveness; read-only XTF-/RootFS/Arbitrary-UID-Container; **keine** persistierten Indizes/DB-Verbindungen. |
| Release | `0.2.N` durch GitHub-Run-Nummer, native und JVM je vier Tags, OCI-Metadaten, Docker-Hub-Digestprüfung, Pull-/HTTP-Smoke; Release-Pipeline nicht abschwächen. |
| Provenienz | Die mitgelieferte Harvest-XTF ist `INCOMPLETE` und enthält synthetische Kennungen; keine davon als echte SIMI-Daten ausgeben. |

**Wichtig zur Produktionssicherheit:** Der aktuelle `.github/workflows/main.yml` führt auf `main` nach Testgates automatisch Docker-Hub-Pushes aus; `.github/workflows/mirror.yml` spiegelt Pushes nach Codeberg. **Kein Push nach `main`, kein Merge, kein Tag und kein Workflow-Dispatch durch Codex oder OpenCode.** Cleanup-Commits bleiben bis zur ausdrücklichen Nutzerentscheidung lokal. Tests, die Releases prüfen, verwenden trockene/fake Registry-Antworten oder read-only Verifikation; sie dürfen **nicht** publizieren. Nach Nutzerfreigabe für einen späteren Main-Push ist dessen Publikationswirkung ausdrücklich erneut zu berücksichtigen.

## 1. Ausgangsinventar und verbindliche Einzelentscheidungen

Die folgenden Pfade stammen aus dem recherchierten Git-Tree. Vor jedem `git rm`/`git mv` muss der Agent ihre tatsächliche **aktive** Nutzung mit `git grep`, Imports, Build-/CI-Definitionen, Dokumentenlinks und Prozess-/Browser-Gates prüfen. Ein fehlender GitHub-Code-Suchtreffer alleine ist **kein** Löschbeweis. Wenn einzigartige notwendige Daten entdeckt werden, **nicht vernichten**; mit Dateiname, Consumer und Entscheidungsfrage `BLOCKED (SCOPE_DECISION)` melden.

### 1.1 Root-Dateien

| Pfad | Vorgeschriebene Behandlung | Vorher nachweisen |
|---|---|---|
| `kartenkatalog.duckdb.zip` (ca. 1,23 MB) | **Löschen (`git rm`)**; keine aktive DuckDB-Laufzeit. | Archivinhalt kurz auflisten; kein aktiver Build-/Test-/Container-/Doku-Consumer; Daten ggf. über Git-Historie verfügbar. |
| `kartenkatalog.xtf.zip` (ca. 1,50 MB) | **Löschen (`git rm`)**; das ist nicht die aktuelle XTF. | Inhalt/Modell und tatsächliche XTF-Quelle unterscheiden; aktive Tests lesen `data/...`, nicht Root-Archiv. |
| `mockup_C_tree_clean.png` (ca. 91 KB) | **Löschen (`git rm`)**; altes UI-Mockup. | Keine Verwendung in aktiven Qute-/CSS-/JavaScript-/Dokumentationsseiten; die späteren SODATA-Referenzscreenshots im historischen Nachweis **nicht** damit verwechseln. |
| `data/kartenkatalog-harvest-ohne-qml.xtf` | **Behalten, Byteinhalt unverändert.** | Hash vor/nach jedem Abschnitt; kein Umbenennen, kein Beautifying, kein synthetisches Auffüllen. |

**Keine Git-Historienumschreibung:** `git rm` aus aktuellem Tree genügt. `git filter-repo`, BFG und Force-Push sind ausserhalb des Auftrags.

### 1.2 Aktive Prüfscripts – alle behalten, semantisch umbenennen

| Aktuell | Sollname | Funktion, die zwingend erhalten bleibt |
|---|---|---|
| `scripts/p0-http-contract.py` | `scripts/api-http-contract.py` | Direkte HTTP-/JSON-/HTML-/Problem-JSON-Vertragstests am realen Prozess mit `src/test/resources/contract/...`. |
| `scripts/p1-process-contract.py` | `scripts/runtime-process-contract.py` | JVM-App-Start, Health, ungültige XTF/Quelle, Startabbrüche und `--dev`-CORS (erlaubte UND nicht erlaubte Origins). |
| `scripts/p2-process-contract.py` | `scripts/search-index-contract.py` | Lucene-Index aus XTF, Neustart-Determinismus, geänderte Quelle, Read-only/kein Index-Volume, echte Fehlstartprobe und Suche/Facets. |
| `scripts/p4-distribution-contract.py` | `scripts/distribution-contract.py` | Echte JVM-/Native-/Dockerprozess-Parität, Cross-Layer-Fixture, QML-Bodies, negative Starts, UID/RootFS, OCI-/Plattformprüfung. |
| `scripts/versioning-contract.py` | **Behalten** | Versionsformat, gültige/ungültige CI-Run-Nummern. |
| `scripts/release-contract.py` | **Behalten** | Registry-Verifikationsvertrag mit Fake-Antworten. |
| `scripts/verify-published-images.py` | **Behalten** | Read-only-Prüfung veröffentlichter Tags/Digests. |

`p0`/`p1`/`p2`/`p4` dürfen nach der Umbenennung in aktiven Filenamen, Funktionsnamen, Kommentaren, Ergebnislabels und Reportverzeichnissen **nicht mehr den aktuellen Betrieb beschreiben**. **Evidenz aus der abgeschlossenen Migration bleibt hingegen historisch unverändert** und darf solche Namen weiterhin enthalten.

### 1.3 Aktive Java-Tests und Testressourcen

| Aktuell | Soll | Bedingung |
|---|---|---|
| `src/test/java/ch/so/agi/meta/BaselineContractTests.java` | `CatalogApiContractTests.java` | Klasse, Referenzen und Testausführung umbenennen; die inhaltlichen API-/HTTP-Assertions **unverändert streng** lassen. |
| `src/test/java/ch/so/agi/meta/BaselineQmlTests.java` | `CatalogQmlContractTests.java` | Quarkus-Testprofil/Nested-Class/Quellverweise korrekt umbenennen; QML-Bytes, Header, XSS-Negativtest erhalten. |
| `src/test/resources/contract/baseline.json` | `src/test/resources/contract/public-api.json` | Bytes und fachlichen JSON-Vertrag **bytegleich** erhalten, sämtliche Lader/Aufrufer anpassen. |
| `src/test/resources/final/catalog.xml` | `src/test/resources/integration/catalog.xml` | Echte Cross-Layer-Testfixture unverändert übernehmen und in Distributionsharness ersetzen. |
| `src/test/resources/contract/README.md` | **Inhalt aktualisieren** | «Spring/XTF baseline (P0)» zu aktiver «Öffentlicher API-Vertrag»; keine P0-/Spring-Startanleitung. Historische Entstehung im Archiv dokumentiert. |
| `src/test/java/ch/so/agi/meta/MockCatalogRepository.java` | **Behalten** | Aktives Testdouble für `CatalogServiceTests`, keine produktive Mock-Fallback-Implementierung. |
| `src/test/resources/search/catalog.xml`, `web/security.xml`, `contract/with-qml.xml` | **Behalten** | Kleine synthetische Fixtures für Keywords/Organisationen, XSS, QML, negative Fälle. |
| `src/integrationTest/java/ch/so/agi/meta/PackagedCatalogIT.java` | **Behalten** | Echtes paketiertes JVM-/Native-Integrationstestgate, fachlich neutral benannt. |

Andere produktive Klassen haben beim Audit bereits fachliche Namen. **Insbesondere existiert `P3ITTest` auf dem geprüften `main` nicht.** Ein Agent darf nicht aus einem früheren Bericht folgern, dass er eine solche Klasse löschen müsse; jede Datei muss real vorhanden sein. Die bestehenden Klassen `CatalogStore`, `CatalogSnapshot`, `CatalogService`, `CatalogRepository`, `XtfCatalogParser`, `LuceneCatalogSearchIndex` und `LuceneNativeConfiguration` erfüllen reale Funktionen und dürfen **nicht** pauschal als Migrationsballast entfernt werden.

### 1.4 Berichte/CI-/Betriebspfade

| Ist | Soll | Aufrufer |
|---|---|---|
| `build/reports/p1/` | `build/reports/runtime/` | `runtime-process-contract.py` + aktive Doku |
| `build/reports/p2/` | `build/reports/search-index/` | `search-index-contract.py` + aktive Doku |
| `build/reports/p3a/screenshots/` / `.../browser-jvm.log` | `build/reports/browser/catalog/` | `CatalogBrowserTests` + Doku |
| `build/reports/p3b/screenshots/` / `.../browser-jvm.log` | `build/reports/browser/filters/` | `CatalogFilterBrowserTests` + Doku |
| `build/reports/p4/` | `build/reports/distribution/` | `distribution-contract.py` + Doku |
| `build/reports/release/` | **Behalten** | Release-/Registry-Tests, gehört zum fachlichen Vorgang, nicht zu einer Migrationsphase. |
| `.github/workflows/main.yml` | **Aufrufe aktualisieren** | Alle neuen Scriptpfade müssen die bisherigen Gates ersetzen; **niemals** entfernen. |
| `.github/workflows/mirror.yml` | **Behalten** | Codeberg-Mirror, nicht bereinigungsbedingt entfernen. |
| `build.gradle`, `gradle/versioning.gradle`, Quarkus-Gradle-Wrapper | **Behalten** | Keine unnötigen Buildversionsänderungen. |

**Pfadnamen sind Sollwerte für neue Logs**, keine Aufforderung zur Umschreibung alter Rohbelege in `docs/history/` oder damals generierten Artefakten.

### 1.5 Dokumente und Bildmaterial

**Aktuell zu halten:** `README.md`, `data/README.md`, neu `docs/architecture.md`, `docs/operations.md`, `docs/testing.md`, `docs/history/README.md`, und der kurze Cleanup-Status `docs/cleanup/STATUS.md`.

**Historisch unverändert zu archivieren:** die Dateien aus `docs/quarkus-migration/` (Originalplan, Codex-/OpenCode-Prompts, `STATUS.md`, `implementation/P0..P5`, `implementation/RELEASE`, `reviews/P0..P5`, `reviews/RELEASE-*`, Referenzbilder und `reference/README.md`). `docs/legacy-notes.md` ebenfalls archivieren. Historische SODATA-Referenzscreenshots **nicht** mit dem alten Root-Mockup verwechseln. Alle notwendigen MIT-/Vendor-Lizenzdateien im aktiven `META-INF/resources/vendor/` erhalten.

**Vorgeschlagene endgültige Dokumentstruktur:**

```text
README.md                          # Produktüberblick, aktive Links
LICENSE
data/
  README.md                         # unveränderte Fixture-Provenienz
  kartenkatalog-harvest-ohne-qml.xtf
docs/
  architecture.md                   # aktueller Ist-Datenfluss
  operations.md                     # aktuelle Start-/Container-/Release-Anleitung
  testing.md                        # aktuelle Tests, Skripte, Browser, Native
  history/
    README.md                       # Provenienz und Archivierungsregeln
    legacy-notes.md                 # alte Recherche, historisch
    quarkus-migration/              # Originale als unveränderte Archivkopie
      CODEX_STARTPROMPT.md
      EXECUTION_PLAN.md
      OPENCODE_REVIEW_PROMPT.md
      OPERATIONS.md                 # historische Fassung; Links im Archiv bleiben erhalten
      STATUS.md
      implementation/
      reviews/
      reference/
  cleanup/                          # Plan, Status und aktuelle unabhängige Cleanup-Berichte
    EXECUTION_PLAN.md
    CODEX_STARTPROMPT.md
    OPENCODE_REVIEW_PROMPT.md
    STATUS.md
    implementation/
    reviews/
```

Bei der finalen Dokuphase **gesamten** historischen Ordner mit `git mv docs/quarkus-migration docs/history/quarkus-migration` verschieben. Die frühere `OPERATIONS.md` **im Archiv unverändert belassen** und separat eine aktualisierte `docs/operations.md` schaffen. So bleiben deren historische relative Links innerhalb des gemeinsam verschobenen Baums erhalten; eine doppelte *aktive* Betriebsanleitung entsteht nicht. `docs/legacy-notes.md` nach `docs/history/legacy-notes.md` mit `git mv` verschieben. Bei ausserhalb/innerhalb des Archivs nicht mehr auflösbaren Links eine **separate Mapping-/README-Datei** erstellen, nicht die alten Prüfberichte byteweise umschreiben.

## 2. Architekturprüfung im Code: gezielt, mit klaren Negativentscheidungen

Eine Bereinigung ist **nicht** gleichbedeutend mit möglichst wenigen Klassen. Vor jedem Source-Refactoring muss der Agent anhand tatsächlicher Aufrufstellen zeigen, dass mindestens eine Ebene redundant und ihr Entfernen ohne Verhaltenseffekt ist. Für den vorliegenden Auftrag sind folgende Prüfungen ausdrücklich vorgeschrieben:

| Klasse/Element | Konkrete Prüfung | Standardentscheidung |
|---|---|---|
| `CatalogStore` | `@Startup`-Initialisierung aus `CatalogXtfSource`, hält genau einen Snapshot, loggt Indexeigenschaften, `@PreDestroy` schliesst Reader. | **Behalten**, nicht zu einer lazy/persistierenden Variante umbauen. |
| `CatalogSnapshot` | Immutability und Maps für Thema/Layer; baut Lucene einmalig; `CatalogRepository`-Interface für Tests. | **Behalten**; kein zweiter Index oder In-Request-Parsing. |
| `CatalogRepository` | Testdouble `MockCatalogRepository`, `CatalogService`-Konstruktor, Store/Snapshot-Delegation prüfen. | **Behalten**, wenn Interface reale Testbarkeit/Isolation ermöglicht; doppelte Implementierung nicht ohne eindeutigen Nutzen umbauen. |
| `CatalogService` | Sortierung `de-CH`, Thema-/Layer-Lookups. | **Behalten**, keine Verschiebung von Fachlogik in REST/Qute. |
| `CatalogConfig` / `application.properties` | Standard-XTF-Pfad sowohl als `@WithDefault` als auch in Properties. Prüfen, welcher bei Quarkus zur Laufzeit tatsächlich wirkt. | **Eine** autoritative Default-Quelle wählen (siehe C3), Env-Override erhalten. |
| `CatalogPageVmFactory.chrome(...)`, `CatalogViewModels.Chrome`, `templates/layout.html` | Fest verdrahteter «Harvest unvollständig»-Footer gilt auch bei anderer XTF. | **Gezielte Korrektur** mit expliziter optionaler Konfiguration, ohne Datenvollständigkeit zu erraten. |
| `CatalogXtfSource`, `XtfCatalogParser` | File/Classpath-only, XXE, Id-/Referenzprüfung, Fail-fast. | **Behalten**, kein pauschaler generischer Parserwechsel. |
| `CatalogSearchIndexBuilder`, `LuceneCatalogSearchIndex` | Index RAM, Read-only, Native-Laufzeit und Ressourcenfreigabe. | **Behalten**, ohne Ranking-/Stemming-Neudefinition. |
| `LuceneNativeConfiguration` | Reflektionsregistrierung für nativen Lucene-Start. | **Zwingend behalten**, solange ein echter Native-Test nicht die Entbehrlichkeit beweist; im Cleanup keine Entfernung vorgesehen. |
| `CatalogApiController`, `CatalogPages`, `CatalogDtos` | API- und HTML-Verträge, `@CheckedTemplate`, Queryvalidierung, sichere Detail-/Kartenlinks. | **Behalten**, kein URL-/DTO-Bruch. |
| JS/CSS/vendor | HTMX-Filter, Browser History, lokal eingepackte Assets und Lizenzen. | **Behalten**, kein unaufgefordertes Framework-Upgrade. |

**Kein zusätzliches Redesign** des DI-Schnittstellenbaums: Das Ziel ist das Entfernen historischer **Benennungen/Artefakte**, nicht ein vermeintlich „schöneres“ neues Domainmodell. Wenn nach Inspektion keine bewiesene tote Produktivklasse übrigbleibt, ist **„keine weitere Klassenlöschung“ das korrekte Ergebnis**.

### 2.1 Explizite Provenienz statt harter Footer-Behauptung

Betroffene konkrete Dateien/Verträge:

- `src/main/java/ch/so/agi/meta/CatalogConfig.java`: `@ConfigMapping(prefix="catalog")` um ein **optionales** Textattribut ergänzen, z. B. `String provenanceNotice()` bei Konfigurationsschlüssel `catalog.provenance-notice`, oder gleichwertig minimalistischen Quarkus-Config-Zugriff verwenden.
- `src/main/resources/application.properties`: `catalog.xtf-location=${CATALOG_XTF_LOCATION:file:./data/kartenkatalog-harvest-ohne-qml.xtf}` als **einzigen** XTF-Default belassen, das redundante `@WithDefault` am Mapping entfernen. Neues Notice-Feld standardmässig **leer**: `catalog.provenance-notice=${CATALOG_PROVENANCE_NOTICE:}` (tatsächliche SmallRye-Config-Bindung durch Test nachweisen).
- `src/main/java/ch/so/agi/meta/web/CatalogViewModels.java`: `Chrome` erhält genau eine optionale `provenanceNotice`-Eigenschaft, falls dies der schlankste Weg ist.
- `src/main/java/ch/so/agi/meta/web/CatalogPageVmFactory.java`: `CatalogConfig` per CDI im Konstruktor injizieren; die bisherige `private static Chrome chrome(...)`-Factory zur Instanzmethode machen und die optionale Notice in alle relevanten Seiten-ViewModels übertragen. Falls das zu einer unnötigen Kaskade führt, gleichwertige kleinere Lösung mit Testbeleg verwenden.
- `src/main/resources/templates/layout.html`: Den *immer sichtbaren* Satz `Datenhinweis: Der mitgelieferte Harvest ist unvollständig...` ersetzen durch ein Qute-`{#if ...}` über die konfigurierte Notice mit **gewöhnlichem Escaping**. **Kein** `RawString` für diese Konfiguration. Bei leerer Notice entsteht **gar kein** Warnhinweis.
- Dokumentation: Das ausgelieferte `data/...` ist weiter nachweislich unvollständig. Wer lokal ausdrücklich die Warnung anzeigen will, setzt `CATALOG_PROVENANCE_NOTICE` selbst. Für eine nicht nachgewiesen vollständige oder andere XTF wird **nie** aus Dateiname, Quellhost oder angenommenen Metadaten eine Vollständigkeitsbehauptung abgeleitet.

Akzeptanztests für diese Korrektur (mindestens): Standardkonfiguration mit leerer Notice → **kein harter Harvest-Satz** im HTML; mit bewusst gesetzter Notice → exakt diese Note auf Start- und Detailseite; eine `<script>`-artige Notice → escaped, **nicht ausführbar**; externe XTF über `CATALOG_XTF_LOCATION` → korrekte Daten und **keine** fälschliche vorgegebene Provenienz; JSON-API, Facets und Kartenlink unverändert. Keine XTF-Datenmutation.

## 3. Verbindlicher Agenten- und Reviewprozess

Die Umsetzung wird in **fünf nacheinander abgenommenen Lieferabschnitten C0–C4** durchgeführt. `C` steht ausschliesslich für Cleanup-Aufträge, nicht für neue Klassen-/Dateinamen im Produkt. **Jede Lieferung hat einen eigenständigen `PASS` durch OpenCode/DeepSeek**; der letzte C4-Review ist zugleich **kumulative Gesamtabnahme aller C0–C4-Ziele**. Historische Quarkus-P0–P5-Reviews bleiben historische Belege, aber ersetzen den neuen Review nicht.

### 3.1 Einheitlicher Zyklus

1. **Aktuellen Stand prüfen:** `pwd`, `git status --porcelain=v1`, `git rev-parse HEAD`, `git branch --show-current`, `git ls-files`; Worktree-/Fremdänderungen erhalten. Bereits vorliegendes unabhängiges `PASS` für vorherige Lieferung mit SHA/Prüfbericht nachweisen.
2. **Kriterien benennen:** Die AC-IDs der Lieferung auflisten; betroffene Dateien, aktive Aufrufer und das unverändert erwartete Verhalten dokumentieren. Vorher-Nachweis sichern (Dateinamen, SHA256 wichtiger Fixtures, `git grep`, ggf. Build/HTTP).
3. **Nur begrenzten Umfang ändern:** `git mv` für Umbenennungen, `git rm` nur nach belegter Nichtverwendung. Keine automatisierte globale Zeichenkettenersetzung in historischen Reviewtexten. Keine Änderung des XTF-/JSON-Fixtureinhalts.
4. **Pflichtprüfungen frisch ausführen:** Exakte Kommandos, Exitcodes, JUnit-Zahlen `tests/failures/errors/skipped`, Prozess-/Browser-/Native-Ergebnisse und Pfade der Rohberichte notieren. Tests dürfen nicht mit `-x test`, `@Disabled`, stillen Exceptions oder absichtlich lockeren Erwartungen grün werden.
5. **Implementierungsbericht:** `docs/cleanup/implementation/C<N>.md` mit Kriterienmatrix, BASE_SHA, tatsächlich geänderten Dateien, vorher/nachher, bewusst nicht entfernten Pfaden, Testbelegen, offener Frage.
6. **Lokaler Commit:** `git diff --check`; nur die beauftragten Änderungen committen. Dann vollen `CANDIDATE_SHA = git rev-parse HEAD` lesen. Keinen eigenen SHA in den Commitinhalt hineinschreiben.
7. **Unabhängiger Handoff:** Codex übergibt neuen Basis-/Kandidaten-SHA, Kriterien, Bericht und `docs/cleanup/OPENCODE_REVIEW_PROMPT.md`. Review in **frischem OpenCode-2.x-GUI-Kontext und sauberem separaten Worktree**. Der Reviewer führt **mindestens eine eigene, nicht von Codex kopierte Gegenprobe** aus.
8. **Nur echtes `PASS`:** Codex kann den Reviewer nicht selbst ersetzen. Falls OpenCode GUI nicht automatisiert steuerbar ist, Ausgabe `AWAITING_REVIEW` mit kopierbarem Handoff und **sofort STOPP**. `FAIL` → begründet korrigieren, neuen Commit und neuen unabhängigen Review. `BLOCKED` → konkrete Voraussetzung/Entscheidung herstellen, kein Phasenwechsel.
9. **Nach PASS:** Ein **rein dokumentarischer** Status-/Review-Archivierungscommit ist erlaubt, wenn er keine ausführbaren Quellen, Scripts, Builddateien, Testdaten oder Erwartungen verändert. Der tatsächliche abgenommene Produkt-SHA bleibt im Bericht sichtbar. Bei produktiver Änderung erneuter Review.

**Nur drei Reviewurteile:** `PASS`, `FAIL`, `BLOCKED`. `BLOCKED` darf den Grund ergänzen: `INFRA`, `MISSING_EVIDENCE`, `SCOPE_DECISION`, ist aber kein viertes Urteil. Keine selbsterteilte Freigabe und kein „Tests waren damals grün“ als aktueller Beleg.

### 3.2 Test-Ebenen, nicht alles in jeder kleinen Lieferung

| Gate | Mindestnachweis | Wann fällig |
|---|---|---|
| T0 – Baum-/Referenz-/Dokumentanalyse | `git ls-files`, `git grep`, Dateiverweise, Git-Diff, Blob-/SHA256-Checks, Doc-Link-Prüfung. | C0–C4 immer soweit betroffen. |
| T1 – Unit + API | `./gradlew clean test quarkusBuild quarkusIntTest`; JSON-Vertrag, Parser, Fehlverhalten. | C0 Baseline; C1, C2, C3, C4 auf Kandidat. |
| T2 – Echte Prozesse | `python3 scripts/{...}` gegen reale JVM/Dev-App, Startup-/Index-/CORS-/Fehlergates. | C0 Baseline; C2 neue Pfade, C3, C4 voll. |
| T3 – UI-Browser | `./gradlew playwrightTest` Chromium+Firefox in den vorhandenen Viewports; echte Screenshots/DOM-/Security-/History-Assertions. | C0 Baseline wenn möglich, C2, C3 und C4 verpflichtend. |
| T4 – Native/Distribution | GraalVM/Mandrel-25 Native-Build und echter `testNative`-HTTP-Test; echte JVM-/Native-/Container-Parität, read-only/UID; Zielarchitektur klar benennen. | C4 **verbindliches kumulatives Gate**; bei C2/C3 nach Änderung distributionsrelevanter Semantik ebenfalls gezielt. |
| T5 – Release-Harness | Versionstests, Fake-Registry-Vertrag, Workflow-/Trigger-Audit, erwartete Tags/Digests und keine Veröffentlichung vor Freigabe. | C0 inventarisieren; C2 und C4 verpflichtend. |

**Native/CI-Verfügbarkeit:** Der unabhängige Schlussreview benötigt einen frischen, realen Native-/Linux-Distributionsnachweis für den Kandidaten-SHA. Falls er auf macOS nicht möglich ist, darf ein mit SHA eindeutig zugeordnetes **nicht veröffentlichendes** PR-/Branch-CI-Gate verwendet werden – aber nur nach ausdrücklicher Nutzerfreigabe zum Push und ohne `main`-Merge. Ohne eigenen Test oder verifizierbares aktuelles CI-Artefakt: `BLOCKED (INFRA)`, niemals `PASS`. Ein altes 0.2.73-Release belegt nur die Vergangenheit. Die Benutzerentscheidung zum späteren Main-Push/automatischen Release ist von der unabhängigen Codefreigabe getrennt.

### 3.3 Reproduzierbare Befehle (bei C0 reale Existenz/Verfügbarkeit prüfen)

```bash
# Inventar / Arbeitsbasis
git status --porcelain=v1
git rev-parse HEAD
git ls-files
rg -n '(^|[^[:alnum:]])(P0|P1|P2|P3a|P3b|P4|P5|P3ITTest)([^[:alnum:]]|$)' \
  scripts src .github build.gradle gradle deploy README.md || true

# JVM / HTTP / Suche / Browser – Namen bis C1 alt, ab C2 neu
./gradlew clean test quarkusBuild quarkusIntTest --console=plain
./gradlew playwrightTest --console=plain
python3 scripts/api-http-contract.py http://127.0.0.1:8083  # nur bei bereits gestarteter App
python3 scripts/runtime-process-contract.py
python3 scripts/runtime-process-contract.py --dev
python3 scripts/search-index-contract.py
python3 scripts/versioning-contract.py
python3 scripts/release-contract.py
./gradlew dependencies --configuration runtimeClasspath

# Native / Container (Build-/Imagepfade und Architektur vor Ausführung prüfen)
./gradlew testNative \
  -Dquarkus.native.enabled=true \
  -Dquarkus.package.jar.enabled=false \
  -Dquarkus.native.container-build=true \
  -Dquarkus.native.container-runtime=docker \
  -Dquarkus.native.native-image-xmx=4g
# Danach reale JVM- und Native-Images getrennt bauen; exakte Verfahren in docs/operations.md.
python3 scripts/distribution-contract.py --jvm-image <getestetes-jvm-image> \
  --native-image <getestetes-native-image> \
  --expected-platform linux/amd64 --report-dir build/reports/distribution/cleanup
```

Die aktualisierten Scriptnamen sind **erst nach C2 gültig**. C0 und C1 verwenden die **vorhandenen alten** Namen und protokollieren dies ausdrücklich; keine imaginären erfolgreichen Aufrufe behaupten. Der `api-http-contract.py`-Aufruf gegen Port 8083 ist nur gültig, wenn dort tatsächlich eine gestartete Anwendung läuft; alternativ startet der Runtime-Harness den Prozess selbst. C2/C4 müssen zudem zeigen, dass die Umbenennung **keinen** Pflicht-Test aus `.github/workflows/main.yml` verschwinden liess.

**Warum kein `python -O`:** Einige ältere Prozessskripte nutzen `assert`; ihre Ausführung mit deaktivierten Assertions wäre ein Schein-PASS. Vor der Umbenennung bereits vorhandene Harness-Negativkontrollen erhalten und insbesondere zusätzlich einen absichtlich fehlschlagenden Probeaufruf nachweisen.

## 4. Lieferung C0 – Bestandsaufnahme, Schutz der bestehenden Verträge

**Zweck:** Ein kleines, belastbares Vorher-Bild erzeugen und versteckte Consumers finden. **Noch keine Löschungen, Umbenennungen oder Quarkus-Produktänderungen.** C0 ist bewusst ein begrenzter Baseline-Pilot, nicht ein Vorwand für eine monatelange Dokumentationsphase.

### C0.1 Konkrete Arbeiten

1. Den wirklich aktuellen Git-Commit und Branch ermitteln. `git status` prüfen, fremde uncommittete Änderungen nicht vermischen. Verwendete JDK-, Gradle-, Python-, Docker-/Podman-, Native-Toolchain- und Browser-Versionen erfassen; nicht verfügbare Tools exakt nennen.
2. `git ls-files -z` und `git grep -n` nutzen, um **alle sieben Python-Scripts**, ihre wechselseitigen Aufrufer, Gradle, `.github/workflows/main.yml`, Dockerfiles, `deploy/openshift.yaml`, Java-Tests, Testfixtures und aktive Doku aufzulisten. Auch relative Pfade in `subprocess.run`, `Path(...)` und HTTP-Smokes prüfen. Dies ist die Beweiskette für die späteren `git mv`.
3. Die drei Root-Artefakte auflisten (`unzip -l` für ZIPs), `git grep` ausserhalb rein historischer Docs ausführen. Zwischen dem alten `kartenkatalog.xtf.zip` und dem aktiven `data/kartenkatalog-harvest-ohne-qml.xtf` explizit unterscheiden. Keine alten Daten mit der aktiven Fixture ersetzen.
4. `src/main/java` und `src/test/java` **alle** Dateien nach Phasennamen/alten Frameworks durchsuchen. Ergebnis muss ausdrücklich enthalten: `P3ITTest` vorhanden oder nicht; **kein** erfundener Löschauftrag für nicht existierende Klassen.
5. Für die aktive XTF und `src/test/resources/contract/baseline.json` SHA256 und Feld-/Objektzahl sichern. Keine production-basierten Zählannahmen; 169/886 nur als Fixturevertrag behandeln.
6. Die laufenden Tests/Prozesse im vorhandenen Stand tatsächlich soweit möglich ausführen, inklusive `./gradlew clean test quarkusBuild quarkusIntTest`, `playwrightTest`, `scripts/p0-*`, `scripts/p1-*`, `scripts/p2-*`, `versioning-contract.py` und `release-contract.py` (auf dem aktuellen Stand). **Keine** externe Publikation auslösen. Browser-/Native-Baseline darf ein dokumentiertes `BLOCKED (INFRA)` sein, wenn sie erst im finalen Gate nachgeholt werden muss; C0 als Pilot hat nur die unten definierten fälligen Pflichten.
7. In `docs/cleanup/implementation/C0.md` eine **einzige knappe KEEP / DELETE / RENAME / ARCHIVE / OPTIONAL_REFACTOR-Matrix** erstellen: Pfad, Evidenz, Consumer, Entscheidung, Ersatz, Test/Negativtest, Risiken. Migrationsrisiken vorab vermerken, aber noch kein neues Metareviewsystem bauen.
8. `docs/cleanup/STATUS.md` mit `C0`, `IN_PROGRESS` und vorherigem unabhängigem PASS `none` initialisieren. Nicht den historischen P5-PASS als Cleanup-PASS ausgeben.

### C0-Abnahmekriterien

| ID | Verbindliche, vom Reviewer eigenständig prüfbare Bedingung |
|---|---|
| **AC0-01** | Repository-HEAD, Sauberkeit, Toolchain und **vollständige aktive** Dateien-/Consumerkarte sind echt ermittelt, nicht aus dem Audit ungeprüft übernommen; automatische Main-Push-/Docker-Hub-Publikation und Mirror-Effekt ausdrücklich erkannt. |
| **AC0-02** | Drei Root-Artefakte und zwei XTF-Orte sind sauber unterschieden; der aktive Datenfixture-Hash und die Test-Contract-Bytes sind reproduzierbar festgehalten; kein Löschbeweis nur durch GitHub-Code-Suche. |
| **AC0-03** | Aktuelle Quarkus-API-/Such-/Browser-/JVM-Basistests und vorhandene Prozess-/Release-Scripts sind mit realen Aufrufen, Exitcodes, Zahlen und nicht ausgeführten Pflichten dokumentiert; keine vorgetäuschte Native-Abnahme. |
| **AC0-04** | Sämtliche vorhandenen Produktiv- und Test-Klassennamen wurden auf Phasenbezeichnungen und echte Legacy-Verwendungen geprüft; `P3ITTest`-Negativbefund nur bei tatsächlicher Abwesenheit. |
| **AC0-05** | Unabhängiger OpenCode-Review prüft mindestens einen **selbst gewählten Querverweis oder negativen Vertrag**, den Codex' Inventur sonst übersehen könnte, und verifiziert C0-Candidate-SHA/Commit. |

**C0-Testgate:** T0 komplett, T1 frisch, die ohne externe Freigabe verfügbaren Prozess-/Versionierungs-/Release-Vertragstests und ein aussagekräftiger Browser-Smoke (oder `BLOCKED` bei dessen hier zwingender Verfügbarkeit). **Kein eigenmächtiger C1-Start vor PASS.**

## 5. Lieferung C1 – Historische Root-Artefakte und `.gitignore` säubern

**Zweck:** Offensichtliche unbenutzte Dateien aus dem aktiven Tree entfernen, aber Quellfixtures und Buildartefaktverträge nicht berühren. **Keine** Umbenennung der aktiven Scripts, keine UI-Codeänderung.

### C1.1 Konkrete Arbeiten

1. Nach erneuter Consumerprüfung `git rm -- kartenkatalog.duckdb.zip kartenkatalog.xtf.zip mockup_C_tree_clean.png` ausführen. **Keine** Verschiebung der Inhalte in eine neue aktive `data/old`-Ablage; historisches Git ist ausreichend. Falls der Inhaltscheck einen einzigen derzeit nötigen Consumer identifiziert, genau diesen Pfad behandeln und nicht blind alle drei entfernen.
2. `.gitignore` von generischen und produktfremden Vorlagen befreien. **Explizit entfernen**, soweit real noch vorhanden: `!datahub_key.xtf`, `!datahub.xtf`, `!datahub*.xtf` und alte Spring-/STS-/Maven-/Groovy-/Toptal-Patchblöcke ohne Nutzen. **Bewusst erhalten:** Gradle/Quarkus-Outputs, Java `.class`, Logs, IDE-Dateien, macOS-Dateien, Python-`__pycache__`, virtuelle Umgebungen, lokale `.env`/Secrets und temporäre XTFs. Die eine versionierte Test-XTF muss weiter getrackt werden können (`*.xtf` + Ausnahme `!data/kartenkatalog-harvest-ohne-qml.xtf`).
3. `.dockerignore` **nur inspizieren**, nicht aufgrund des Namens löschen: Das aktuelle Whitelist-Modell ist für `build/quarkus-app` und Native-Binary nötig. `gradle/wrapper/gradle-wrapper.jar` muss auch bei generischem `*.jar`-Ignore im Git getrackt bleiben.
4. Per `git check-ignore -v` **positive und negative Fälle** testen: erwartete Fixture nicht ignoriert, neue beliebige XTF ignoriert, `.gradle/`, `build/`, `.env`, IDE-Dateien ignoriert; `src/main/resources/application.properties`, `gradlew`, `gradle/wrapper/gradle-wrapper.jar` nicht ignoriert. Da `git check-ignore` für bereits getrackte Dateien einen anderen Pfad haben kann, ggf. `--no-index` verwenden und die Semantik sauber dokumentieren.
5. No-Ghost-References: aktive Quell-/Test-/CI-/Dokupfade verwenden die drei gelöschten Root-Dateien nicht mehr; `data/README.md`, Fixture und Lizenzen bytegleich.

### C1-Abnahmekriterien

| ID | Verbindliche Bedingung |
|---|---|
| **AC1-01** | Die drei nachgewiesen ungenutzten Root-Dateien sind aus `git ls-files` entfernt; es gibt **keinen** aktiven Verbraucher; keine History-Rewrite/Force-Push; keine notwendigen Daten sind verloren. |
| **AC1-02** | Nur die lokale Test-XTF bleibt als beabsichtigte versionierte XTF erhalten; Hash, Modell, 169/886-Fixturnachweise und notwendige Testressourcen unverändert. |
| **AC1-03** | `.gitignore` ist projektspezifisch; `git check-ignore` positive/negative Canarys beweisen, dass weder Source/Wrapper noch die erlaubte XTF versehentlich ignoriert werden. `.dockerignore`-Kontrolle dokumentiert. |
| **AC1-04** | JVM-`test quarkusBuild quarkusIntTest` und die bestehenden Kernprozesschecks bestehen mit **denselben** fachlichen Assertions; keine Testabschwächung; `git diff --check` sauber. |
| **AC1-05** | Unabhängiger Reviewer kontrolliert die tatsächliche Nichtverwendung mindestens eines gelöschten Artefakts und führt einen eigenständigen Ignore-/Fixture-Gegenfall aus; korrekter SHA/Handoff. |

## 6. Lieferung C2 – Phasenfreie Testnamen, Scripts, Reportpfade und CI

**Zweck:** Die nötigen Tests bleiben erhalten, werden aber **nicht mehr als Überbleibsel von P0/P1/P2/P3a/P3b/P4 bezeichnet**. Dies ist die risikoreichste refaktorierende Lieferung, weil aktive Scripts sich gegenseitig aufrufen und CI bei einem echten Main-Push releasen würde. **Kein Push.**

### C2.1 Renaming ohne Verhaltensänderung

1. Die exakte Rename-Tabelle in §1.2/1.3 mit `git mv` vollständig umsetzen. Python-Modul-/Dateinamen sind kein öffentliches API, aber der Workflow und die lokalen Befehle schon; alle Aufrufer korrigieren.
2. In `scripts/runtime-process-contract.py` den subprocess-Aufruf des HTTP-Vertrags auf **`sys.executable`** + neuen Dateinamen umstellen, statt `python3` fest im Script zu verdrahten; die Python-Ausführung auf macOS/Linux muss gleich bleiben. Keine Umstellung auf ein neues Testframework.
3. In `scripts/distribution-contract.py` alle internen Aufrufer des HTTP-Scripts und die Testfixture `src/test/resources/integration/catalog.xml` anpassen. Gesondert JVM-/Native-Modi, `--native-executable`, `--jvm-image`, `--native-image`, `--expected-version`, `--expected-revision`, `--expected-platform`, `--report-dir` prüfen; **CLI-Optionen nicht entfernen**.
4. Phase-Labels in temporären Laufzeit-/Container-Namen (`kartenkatalog-p4-*` usw.), Ausgaben und Reportpfaden durch fachliche Namen ersetzen. Die tatsächliche Testfallanzahl, die Erfolgs-/Fehlersemantik, JSON- und HTTP-Assertions, Zeitüberschreitungen und Prozessbereinigung bleiben intakt. Nicht versehentlich `assert`-Anweisungen verlieren oder die Scripts mit optimiertem Python starten.
5. `BaselineContractTests` + `BaselineQmlTests` umbenennen, einschliesslich Java-Klassenname, nested `QuarkusTestProfile`, geänderter Ressourcenreferenz und einer eventuellen `--tests`-Selektion. **Nur Namen/Imports/Pfade ändern**, nicht den Inhalt der öffentlichen Golden-Assertions. `public-api.json` und `integration/catalog.xml` bytegleich vor/nach nachweisen. `MockCatalogRepository` bleibt als Testdouble.
6. In `CatalogBrowserTests` und `CatalogFilterBrowserTests` die Reportverzeichnisse auf `build/reports/browser/catalog` und `browser/filters` umstellen, einschliesslich `browser-jvm.log`/Screenshots. Browser-Selectoren, Viewports, Engines und Assertions **nicht** abändern.
7. `.github/workflows/main.yml` an **jeder** aktiven Script-Aufrufstelle anpassen. Die `jvm`-Jobstruktur, `linux-native`-Abhängigkeit (`needs: jvm`), alle vier Pflichtkategorien, `testNative`, Image-Digest-Pins, read-only/UID-/Plattformvertrag, Docker-Hub-Tag-/Push-/Pullverifikation, Main-Only-Publish und `pull_request`-No-Publish **unverändert** erhalten. `paths-ignore: '**.md'` nicht beiläufig entfernen; CI-/Mirror-Verhalten dokumentieren.
8. In **aktiven** `README.md`, der bestehenden `docs/quarkus-migration/OPERATIONS.md` (bis C4 noch aktiv), `src/test/resources/contract/README.md` und ggf. Quellcodekommentaren die neuen Scripts/Logpfade aufnehmen. **Nicht** `docs/quarkus-migration/implementation/**`, `reviews/**`, `reference/**` für ein schönes Grep-Ergebnis umschreiben.
9. Einen Negativ-Canary ausführen: mindestens einen Harness-Aufruf mit absichtlich falschem HTTP-/JSON-Resultat bzw. ungültiger XTF/Indexzustand prüfen. Der neue Harness muss **nicht** mit Erfolg enden. Bereits vorhandene Negativtests sind zu erhalten; zusätzlich eine unabhängige Prüfer-Gegenprobe.

### C2.2 Ausdrücklich unveränderte Release-Grenzen

- Während dieser Lieferung **niemals** `git push origin main`, Release-Workflow auslösen, Docker Hub pushen oder Codeberg spiegeln.
- `python3 scripts/versioning-contract.py` prüft weiterhin gültige/ungültige Run-Nummern.
- `python3 scripts/release-contract.py` prüft weiterhin Fake-Registry-Szenarien.
- `scripts/verify-published-images.py` wird durch das Cleanup **nicht** zu einer Registry-Upload-Funktion.
- `build/reports/release/` und veröffentlichte `0.2.73`-Digests bleiben historische oder read-only Nachweise; **nicht** als Erfolg für den neuen Kandidaten-SHA zählen.
- Für CI-Trigger- und Tag-Tests dürfen Fake-Outputs/temporäre Runner verwendet werden; Secrets nie ausgeben, keine unautorisierte Anmeldung an Docker Hub.

### C2-Abnahmekriterien

| ID | Verbindliche Bedingung |
|---|---|
| **AC2-01** | Alle vier Scripts existieren nur unter den neuen Fachnamen; alle aktiven Aufrufer sind angepasst, keine phasenbenannten aktiven Scripts oder `build/reports/p*`-Verzeichnisse werden neu erzeugt. |
| **AC2-02** | HTTP-/Runtime-/Index-/Distribution-Harnesses behalten funktionale Assertions, Negativfälle, Timeout/Prozess-Cleanup und ihre CLI-Optionen; unabhängiger Vergleich vorher/nachher identischer Fixtures. |
| **AC2-03** | Die zwei Baseline-Testklassen und zwei Fixtures sind fachlich umbenannt; XML/JSON-Inhalt und Testsemantik byte-/verhaltensgleich; keine selektiv übersprungenen Tests. |
| **AC2-04** | Browser-/Integrationstests laufen mit Chromium **und** Firefox, Desktop/Mobile, ohne geänderte Assertions; Screenshots/Logs werden unter fachlichen Verzeichnissen erzeugt und aktiv dokumentiert. |
| **AC2-05** | `.github/workflows/main.yml` ruft sämtliche bestehenden neuen Pfade auf, behält alle JVM-/Native-/Pull-/UID-/XTF-/Tag-Gates und die **unveränderte** Main-Only-Publish-Semantik; Mirror-Workflow bleibt funktionsfähig. |
| **AC2-06** | Frischer JVM-/Browser-/Prozess-/Versionierungs-/Release-Harness-Gesamtlauf mit korrekten Rohreports, echten Exitcodes, `tests/failures/errors/skipped` und mindestens einem absichtlich scheiternden Negativtest; keine Veröffentlichung. |
| **AC2-07** | OpenCode führt eigenständig einen Rename-/CI-Referenzscan und mindestens eine fälschlich-grüne Harness-/CI-Canary aus; exakter Kandidaten-SHA; keine offenen Muss-Abweichungen. |

**C2-Gate:** T0–T3 + T5 vollständig, T4 soweit vom refaktorierten Distribution-Harness technisch betroffen/frisch verfügbar. Ist die angekündigte Python/CI-Regression nicht ausführbar, **kein** PASS.

## 7. Lieferung C3 – Minimale, nachweisbare Produktivcode-Bereinigung

**Zweck:** Technische Restaltlasten in der *aktuellen* Quarkus-Anwendung beseitigen, soweit eindeutig nachgewiesen; primär die irreführende Provenienzmeldung und doppelte XTF-Defaultdefinition. **Nicht** die bewährte Daten-/Sucharchitektur neu aufrollen.

### C3.1 Umsetzung bis auf Klassen-/Methodenebene

1. `CatalogConfig.xtfLocation()` vs `src/main/resources/application.properties` untersuchen. Genau **eine** Defaultdefinition belassen, bevorzugt die bestehende `catalog.xtf-location=${CATALOG_XTF_LOCATION:file:./data/kartenkatalog-harvest-ohne-qml.xtf}` in Properties, das redundante `@WithDefault` an `CatalogConfig.xtfLocation()` entfernen. Den Quarkus-Default, eine externe `file:`-Quelle, eine `classpath:`-Quelle, unbekannte Quelle und das Startverhalten mit Tests prüfen. **Keine HTTP-Quellen einführen.**
2. Optionales Provenienzfeld wie in §2.1 umsetzen: `CatalogConfig.provenanceNotice()`, dazu `CATALOG_PROVENANCE_NOTICE`. Default leer, keine automatische `INCOMPLETE`-/`COMPLETE`-Ableitung aus irgendeinem anderen Feld.
3. `CatalogPageVmFactory.chrome(...)` und `CatalogViewModels.Chrome` minimal erweitern, sodass **alle** relevanten Qute-Seiten dieselbe konfigurierte Notice erhalten. Den bisherigen statischen Footer in `templates/layout.html` konditional ausgeben und **Qute-Escaping** behalten. `CatalogPages`-Endpoints und Qute-`@CheckedTemplate`-Verträge bleiben bestehen.
4. Tests hinzufügen/ergänzen: (a) ohne Notice keine Harvest-Meldung auf `/` und `/ebene/...`, (b) explizite Notice sichtbar, (c) `<>`/HTML-JS escaped, keine XSS, (d) externe XTF verändert nicht irreführend die Provenienz, (e) `catalog.xtf-location`/`CATALOG_XTF_LOCATION` wird weiter korrekt ausgewertet, (f) `/api/v1/catalog`, `/api/v1/ebenen`, Facets, Detail und `/q/health/ready` weiterhin identisch. **Keinen** Testinhalt blind an neue fehlerhafte Ausgabe anpassen.
5. Alle Produktionsklassen unter `src/main/java/ch/so/agi/meta/` und HTML-/JS-/CSS-Ressourcen nach **tatsächlich unbenutzten** Methoden, Klassen, alten Frameworknamen, TODOs, Auskommentiertem und Mehrfachabstraktionen prüfen. Jede vorgeschlagene Löschung muss im Implementierungsbericht einen vollständigen Caller-/Test-/Native-Nachweis besitzen; andernfalls **BEHALTEN**. `CatalogRepository`/`CatalogStore`/`CatalogSnapshot` und Testdouble zählen nicht automatisch als tote Duplikate.
6. Native-spezifische Reflection-Registrierung, `@Startup`-Startfehler, `@PreDestroy`-Indexschluss, `GermanAnalyzer`, Lucene-10.6.0-Build, `JsonProblem`/Exception-Mapper und `quarkus.jackson.serialization-inclusion=always` **nicht** entfernen. Prüfe spezifisch, dass die Browser- und API-Verträge nicht vom Provenienztext abhängen.
7. Das Quarkus-Verhalten auch mit einer *anderen* lokalen XTF-Fixture (`src/test/resources/search/catalog.xml` oder `contract/with-qml.xml`) testen, damit die Footer-Korrektur nicht nur den bekannten Harvest behandelt. Test fixture darf intern synthetisch sein; keine Änderung der Version des ausgelieferten Harvest.

### C3-Abnahmekriterien

| ID | Verbindliche Bedingung |
|---|---|
| **AC3-01** | XTF-Default nur einmal definiert; expliziter file-/classpath-Override und fehlende/falsche Quelle behalten erwartetes Fail-fast-Verhalten. |
| **AC3-02** | Footer enthält ohne explizite Konfiguration **keine** pauschale Harvest-/Vollständigkeitsbehauptung; gesetzte Notice wird auf allen relevanten Seiten korrekt angezeigt und gegen HTML/JS-Injektion escaped. |
| **AC3-03** | Bei externer XTF-Quelle erscheint keine falsche Provenienz; API, Suchtreffer, Facets, Themen- und Detailseiten bleiben davon unabhängig. |
| **AC3-04** | Jede zusätzliche Quellcode-Löschung ist durch Caller-/Verhaltens-/Native-Nachweis gerechtfertigt; keine funktionierenden DI-Abstraktionen, Fixtures, Lizenzen, Lucene-/Native-Hints oder Sicherheitstests voreilig entfernt. |
| **AC3-05** | Reale JVM-/Qute-/HTML-/Parser-/API-/Browser-/Runtime-/Index-Prozessprüfungen frisch erfolgreich, einschliesslich eigener XSS-/fehlender-Datei-Gegenfälle; keine Versions-/DTO-/Ranking-Änderung. |
| **AC3-06** | Unabhängiger Reviewer entwickelt eine **neue** Provenienz-/XSS- oder alternative-XTF-Gegenprobe und prüft die tatsächliche Source-Bindung/Quarkus-`@CheckedTemplate`-Buildprüfung am Kandidaten-SHA. |

**C3-Gate:** T0, T1, T2, T3 frisch. T4 gezielt für Provenienz und Native-HTML auf verfügbarem Binary; T4 gesamt verpflichtend in C4. Sollten neue Produktivklassen über das klar begrenzte Footer-/Konfigurationsproblem hinaus geändert werden, ist die Notwendigkeit im Bericht zu begründen und die betroffene Testmatrix zu erweitern.

## 8. Lieferung C4 – Aktuelle Dokumentation, verlässliches Archiv und Gesamtabnahme

**Zweck:** Die Projektdokumentation auf den Ist-Zustand bringen, abgeschlossene Migration nachvollziehbar archivieren und den **vollständigen, aktuellen Produktstand** erneut unabhängig abnehmen.

### C4.1 Aktive Dokumentation

**`README.md`:** Einstieg für die tatsächlich vorhandene Anwendung; Java/Quarkus/Lucene-Versionen, XTF als einzige Katalogquelle, Dev-/JVM-Start, HTTP-/QWC2-API, kurzer Link zur Benutzeroberfläche, Hinweise zur unvollständigen *mitgelieferten* Fixture, aktive Tests, Docker/Native/CI und **Releasewirkung von Pushes auf `main`**; ausführliche Kommandos verlinken, nicht alles duplizieren. Keine veralteten Spring-Pfade, keine `P0/P1/P2/P3a/P3b/P4/P5`-Scriptnamen, keine historischen Umsetzungsaufforderungen als aktuelle Anleitung.

**`docs/architecture.md`:** *Ist-Dokument* auf Grundlage des aktuellen Quellcodes, keine Wunscharchitektur: `CatalogXtfSource.open → XtfCatalogParser.parse → CatalogSnapshot → CatalogSearchIndexBuilder.build(ByteBuffersDirectory/GermanAnalyzer) → CatalogStore` (fail-fast) → `CatalogService`/`CatalogSearchService`/`CatalogFacetService` → REST-API/`CatalogPageVmFactory`/Qute/HTMX. Regeln für `Map`-Detailzugriff vs Lucene-Treffer/Index, read-only/no reload, `CatalogReadiness`, Native-/Sicherheitsgrenzen. **Keine** falsche Aussage «Dateiindex» oder «automatische Datenaktualisierung». Ein kleines aktuelles Mermaid-Diagramm ist sinnvoll, aber keine zweite Dokumentationsplattform.

**`docs/operations.md`:** Aus der bisherigen `docs/quarkus-migration/OPERATIONS.md` als neue **aktive** Betriebsanleitung herstellen: korrekte Befehle für Java21, QuarkusDev, externe XTF, `CATALOG_PROVENANCE_NOTICE`, Health, QWC2-CORS, JVM-/Native-/Linux-Container, OpenShift-UID/read-only XTF, Index-RAM/kein DB-Secret, Versionierung, Docker-Hub `0.2.N`, Release-Gating, echte Linux-amd64- und bestehende 0.2.73-Belege. Alle geänderten `scripts/*.py`-Aufrufe und Reportpfade aktualisieren. Verweise zu historischen Nachweisen auf `docs/history/quarkus-migration/...` zeigen lassen.

**`docs/testing.md`:** Reproduzierbare Testmatrix und Befehle für Unit/HTTP, Packaged-IT, Browser Chrom/Firefox, JVM-/Dev-CORS-/Index-, Native-/Distribution-, Release-/Versionierungs-Contracts. Darin ausdrücklich dokumentieren, dass `api-http-contract.py` einen **bereits laufenden** Server benötigt, die anderen Prozessharnesses ihre Server starten, `--expected-platform` die Zielarchitektur und `python -O` den Harness-Schutz beeinflusst. Zeige, wo die **aktuellen** Reports entstehen und wie ein fehlschlagender Test diagnostiziert wird.

**`docs/history/README.md`:** Was und warum archiviert wurde, Liste der Originalpapiere, Nachweis der von Git erhaltenen Inhalte/SHAs, historische Aussagekraft der P5/RELEASE-Reviews, SODATA-Referenzbilder. **Nicht** mit aktueller Laufzeit-/CI-Anleitung verwechseln.

**`src/test/resources/contract/README.md`:** Fachliche Beschreibung des bleibenden JSON-Golden-Contracts unter neuem neutralen Namen, Quarkus-Java-Tests, HTTP-Script und Mutation-Canary. Historische Entstehung nur als Link ins Archiv; keine operative Spring-Anleitung.

**`data/README.md`:** Inhaltliche Provenienz und `INCOMPLETE`-Status bleiben wahr. Keine stillschweigende Umetikettierung zu vollständigem Produktionsbestand. Nur wirklich notwendige, verifizierte Linkkorrekturen erlaubt.

### C4.2 Archivierungsregeln

1. **Vor dem Verschieben** Hash- oder `git rev-parse HEAD:<pfad>`-Blobtabelle für alle historischen Dateien sichern (mindestens Reviewberichte, Implementation, Plan, Prompts, `STATUS.md`, Referenzbilder). Die Originale dürfen durch das Verschieben **nicht** inhaltlich geändert werden.
2. `git mv docs/quarkus-migration docs/history/quarkus-migration` und `git mv docs/legacy-notes.md docs/history/legacy-notes.md` verwenden. Die alte `OPERATIONS.md` verbleibt *historisch* unter `docs/history/quarkus-migration/OPERATIONS.md`; die neue `docs/operations.md` ist die einzige zu pflegende operative Kopie.
3. Historische relative Verweise und Referenzbilder stichprobenweise auf funktionierende Auflösung prüfen. Alle **neuen/aktiven** Verweise auf die neuen Pfade umstellen. Falls extern/alt historisch ein früherer Pfad zitiert wird, diesen Bruch in `docs/history/README.md` kartieren, statt Originalberichte umzuschreiben.
4. **Keine historischen unabhängigen Reviewberichte löschen oder editieren**, auch wenn alte `P*`-Namen oder frühere `FAIL`/`BLOCKED`-Ergebnisse darin stehen. Besonders die ersten Release-Reviews mit korrigierten Findings sind beweiskräftig.
5. `docs/cleanup/EXECUTION_PLAN.md`, `CODEX_STARTPROMPT.md`, `OPENCODE_REVIEW_PROMPT.md` sind während des neuen Cleanup bewusst **aktive Prozessdokumente** und dürfen Phasenkennungen enthalten. Sie sind keine Java-Klassen oder produktiven Testartefakte. Nach C4-PASS nur als historischer Cleanup-Nachweis stehen lassen bzw. in einem rein dokumentarischen, separat nachvollzogenen Follow-up archivieren.
6. **Neuen** Dokumentationsindex aufbauen, aber keine neuen permanenten wöchentlichen Status-/Inventar-Generatoren, kein zweites Evidenzframework. `docs/cleanup/STATUS.md` ist die einzige lebende Übersicht während der Bereinigung.

### C4.3 Vollständige Finalprüfung – exakt auf dem Kandidaten-SHA

A. Git-/Datei-/Doku-Gate: Root-Artefakte fehlen; aktive Aufrufer/`*.java`/`*.py`/aktives README/CI verwenden keine Phasenkennungen. `git grep` zwischen **historischen** Belegen und **aktiven** Quellen unterscheiden. Historische Blobidentität der Archivberichte prüfen. Hash der XTF und JSON-/XML-Contracts kontrollieren.

B. JVM: `./gradlew clean test quarkusBuild quarkusIntTest`; alle aktiven Unit-/API-/Qute-/XTF-/Security-/Index- und Packaged-JVM-Tests frisch. Fehlende/geskippte Tests dürfen nicht durch geänderte Testselektion verschwinden; Baseline-Fallanzahl/Wirksamkeit vergleichen.

C. Prozesse/Browser: `./gradlew playwrightTest`, `python3 scripts/runtime-process-contract.py`, `--dev`, `search-index-contract.py`; tatsächlich gestartete Prozesse, alle negativen XTF-/Lucene-/CORS-Checks, No-JS/HTML-/HTMX-Funktion und Chromium/Firefox mit unveränderten Viewports.

D. Native/Container: neues **echtes** Native Image und seine `testNative`-Integration; echtes JVM-/Native-Linux-Image getrennt bauen und den `scripts/distribution-contract.py`-Prozessvertrag für die Zielarchitektur ausführen. **Mindestens** Startup, XTF-Änderung/Fehler, API+Facets, Ranking, QML-Bodies, HTML/HTMX, Readiness, read-only RootFS/XTF, beliebige UID und kein Indexvolume. Kein anderes Lucene-Verzeichnis oder abgesenkter Native-Hint. Linux/amd64-Releasevertrag nur dann als belegt markieren, wenn tatsächlich linux/amd64 auf **dem Kandidaten-SHA** ausgeführt wurde.

E. CI/Release: YAML strukturell und semantisch prüfen; `linux-native needs: jvm`, alle Gates vor `docker/login-action`/`docker push`, `push`/`workflow_dispatch` nur auf `main` (für Push), PR ohne Release, exakte acht Tags, Digest- und Pull-/Runtimetest. `versioning-contract.py`, `release-contract.py` mit Negativcanary. **Keine** echte Veröffentlichung als Cleanup-Test.

F. Unabhängige Zielprüfung: Reviewer liest selbst Source-/Test-/Build-/Workflowdiff und den neuen Dokumentationssatz, bestätigt jede AC-ID C0–C4, entwickelt **mindestens zwei neue übergreifende Gegenproben**, eine Test-/CI-/Pfad-Falle und eine fachlich relevante Produktiv-/Provenienz-/Native-Falle. Einen blossen grünen Testbericht **nicht** als Zielnachweis akzeptieren.

### C4-Abnahmekriterien

| ID | Verbindliche Bedingung |
|---|---|
| **AC4-01** | `README.md`, `docs/architecture.md`, `docs/operations.md`, `docs/testing.md`, Daten-/Contract-README sind **aktuell**, intern konsistent, ohne veraltete ausführbare Namen/Anleitungen und mit richtigen QWC2-/Quarkus-/XTF-/Lucene-/Releasegrenzen. |
| **AC4-02** | Vollständige historische Migration/Release-Dokumentation inkl. unabhängigen `FAIL`/`PASS`-Berichten, Referenzbildern, bisherigen Prompts und `legacy-notes.md` ist unter `docs/history/` erhalten; die Prüfberichte sind bytegleich (Blob-/SHA256-Beleg), keine stillen Inhaltsänderungen. |
| **AC4-03** | Alle neuen/aktiven Links, Dokumentpfade, Dateinamen, Testfixtures und Reportpfade sind gültig; ein Link-/Referenz-Canary würde falsche alte Pfade aufdecken; alte Archivlinks transparent eingeordnet. |
| **AC4-04** | Aktive Java-/Script-/Test-/CI-Namen sind phasenfrei; Root-Artefakte weg; keine aktive Spring-/JTE-/JDBC-/PostgreSQL-/DuckDB-Abhängigkeit oder versteckte neue Persistenz. Historische Belege ausdrücklich aus dem Scan ausgenommen. |
| **AC4-05** | Frischer T1-/T2-/T3-Lauf inkl. Negativ-Tests für Parser, Index, XSS, API, Qute, HTMX, Browser-History, CORS; tatsächliche Exitcodes/Anzahlen/Skips sind nachvollziehbar und die Testabdeckung wurde nicht heimlich reduziert. |
| **AC4-06** | Echte T4-Prüfung auf Kandidaten-SHA: Native-Binary startet/indiziert/sucht; JVM-/Native-/Container-Parität inkl. Read-only/UID/fehlender oder geänderter XTF; Zielarchitektur präzise nachgewiesen, nicht erfunden. |
| **AC4-07** | T5-Versionierungs-/Release- und CI-Trigger-Verträge inkl. mindestens einer absichtlich fehlschlagenden Probe erhalten; keine Docker-Hub-/Codeberg-Publikation durch die Agenten; nachfolgende Main-Push-Releasewirkung dokumentiert. |
| **AC4-08** | **Unabhängiger kumulativer OpenCode-/DeepSeek-Gesamtreview** auf exaktem finalem Kandidaten-SHA prüft alle **31 Muss-Kriterien** AC0-01..05, AC1-01..05, AC2-01..07, AC3-01..06, AC4-01..08, sämtliche regressionsrelevanten Ziele, einen echten Native-/CI-Beleg und mindestens zwei neue Gegenproben. Nur sein `PASS` ist die Cleanup-Freigabe. |

### 8.1 Besonderheit: Reviewbericht archivieren, ohne sich selbst zu reviewen

C4 enthält die Produkt-/Dokuänderungen und `implementation/C4.md`, **noch nicht** den später geschriebenen unabhängigen `reviews/C4-review.md` oder den finalen PASS-SHA in der geprüften Datei. Nach dem Review darf ein **reiner Dokucommit** `reviews/C4-review.md` und `STATUS.md` mit dem originalen C4-Kandidaten-SHA archivieren. Kein erneuter Review nur wegen des originalgetreu übernommenen Berichts; **jede** Änderung an Produktcode, Tests, Workflow, Build, Harnesses oder Daten danach erfordert einen **neuen** geprüften SHA. Der spätere Dokucommit darf historische Release-SHAs nicht als seinen eigenen Produkttestnachweis ausgeben.

## 9. Review-Handoff und Berichtsverträge

**Vereinheitlichte Pfade, kein Schein-Reviewframework:**

```text
docs/cleanup/
  EXECUTION_PLAN.md
  CODEX_STARTPROMPT.md
  OPENCODE_REVIEW_PROMPT.md
  STATUS.md
  implementation/C0.md ... C4.md
  reviews/C0-review.md ... C4-review.md
```

**Implementierungsbericht `implementation/C<N>.md` (vor Commit):**

```text
# Implementierung C<N> – <fachliches Ziel>
Basis-SHA (vollständig):
Kriterien AC...: <jede einzelne>
Vorher-Ist/Sources und geprüfte aktive Consumers:
Entscheidung KEEP / DELETE / RENAME / ARCHIVE / OPTIONAL_REFACTOR, inklusive Negativnachweis:
Git-Diff / geänderte Dateien / Auswirkungen auf öffentliche Verträge:
Datensatz-/Contract-Hash und Vergleich vorher/nachher:
JVM-/API-/Browser-/Prozess-/Native-/Release-Gates fällig:
Tatsächlich ausgeführte Kommandos + Exitcode + Tests/Failures/Errors/Skipped:
Prozess-/Bild-/Log-/JUnit-/CI-Rohberichte (Pfad):
Negative Canary mit tatsächlichem Ergebnis:
Fällige, aber nicht ausgeführte Prüfung + konkreter Grund:
Quarkus-/Qute-/Lucene-/Native-/Docker-Regressionen:
Risiken, offene Nutzerentscheidungen, bewusst nicht entfernte Kandidaten:
Nach OpenCode-PASS nächste Lieferung:
```

**Handoff nach dem Produktcommit (nicht in den eigenen Kandidatencommit schreiben):**

```text
Repository: edigonzales/kartenkatalog
Lieferabschnitt: C<N>
BASE_SHA: <40-stellig>
CANDIDATE_SHA: <40-stellig>
AC_IDS: <exakt>
PHASENGATE: ja nur bei C4, sonst nein
Implementierungsbericht: docs/cleanup/implementation/C<N>.md
Review-Anweisung: docs/cleanup/OPENCODE_REVIEW_PROMPT.md
Letzter unabhängig bestätigter PASS-SHA: <voriger / none>
Nicht ausgeführte fällige Pflichtprüfungen: <keine oder exakt>
Status: AWAITING_REVIEW
```

**Reviewer-Bericht `reviews/C<N>-review.md`:**

```text
Lieferabschnitt / kumulatives Gate:
OpenCode-Version, im GUI tatsächlich ausgewählter Provider/Modellkennung:
Review-Worktree, BASE_SHA, CANDIDATE_SHA, git status:
Je AC-ID: erfüllt/verletzt/nicht prüfbar + eigener Code-/Test-/Prozessbeleg:
Geprüfte fehlende Files/aktive Referenzen/historische Blobidentität:
Eigene neue Gegenprobe(n): Hypothese, Input, Kommando, erwarteter / tatsächlicher Ausgang:
Pflichtgates mit Exitcode, Testzahlen, Errors, Skips, Rohreports:
Native-Zielarchitektur, Build-/Ausführungsquelle, SHA-Bindung:
CI-/Release-Trigger-/Tag-/Read-only-Vertragsprüfung, kein Push/Release:
Findings: ID, Schwere, Repro, Ursache, erforderliche Korrektur:
Nicht prüfbare Pflichtpunkte und konkrete Blocker:
Urteil exakt PASS / FAIL / BLOCKED:
Begründung und Freigabe / Rückgabe an Codex:
```

### 9.1 OpenCode auf macOS konkret

Der Nutzer verwendet **OpenCode 2.x als GUI** auf macOS und hat den DeepSeek-Provider bereits dort konfiguriert. Es ist **kein** neu einzurichtender API-Key in `DEEPSEEK_API_KEY`, keine manuelle CLI-Pflicht und kein OpenRouter-/Zen-Fallback erforderlich. Ein tatsächlich vorhandenes CLI kann nur dann benutzt werden, wenn der Nutzer dies selbst freigibt. Für den Review z. B.:

```bash
# separater schreibgeschützter Prüf-Checkout; keine Änderungen an Candidate
REVIEW_DIR="$(mktemp -d /tmp/kartenkatalog-cleanup-review-XXXXXX)"
git worktree add --detach "$REVIEW_DIR" <CANDIDATE_SHA>
git -C "$REVIEW_DIR" rev-parse HEAD
git -C "$REVIEW_DIR" status --porcelain=v1
```

Diesen Worktree in der OpenCode-GUI öffnen, Reviewprompt mit Handoff einfügen, konkrete Modellkennung aus GUI/Session-Eigenschaften ermitteln; **nicht** aus einer Modell-Selbstauskunft ableiten. Eigene Testdaten und Logs in separatem `build/reports/cleanup-review/` oder `/tmp` erzeugen. Test-/Buildprozesse dürfen dort temporäre `build/`-Dateien schreiben; **getrackte** Kandidatendateien müssen unverändert bleiben. Keine Commits oder Merge aus dem Reviewkontext.

## 10. Abschlussdefinition

Der Auftrag ist **erst** erfüllt, wenn:

- alle C0–C4-Lieferungen selbst getestet, lokal committet, unabhängig geprüft und mit tatsächlichem `PASS` auf **dem jeweiligen** Kandidaten-SHA versehen sind;
- der finale Review **alle 31 ACs** mit eigener Zielprüfung und frischer T1–T5-Evidenz bestätigt;
- die aktive Dokumentation den echten neuen technischen Zustand abbildet und die historischen Nachweise erhalten bleiben;
- das eigene Cleanup nicht einen erneuten P0–P5-Projektballast geschaffen hat;
- keine Test-/CI-/Releasefunktionen durch «schnelleres Aufräumen» still fehlen;
- **kein** unbeauftragter Main-Push, Codeberg-Mirror, Docker-Hub-Release oder Tag erfolgt ist.

**Nicht als Erfolgsmass benutzen:** Zahl gelöschter Klassen, Zahl gekürzter Tests, wenige Dokumentdateien oder grüner Build ohne echte negativen Proben. **Erfolgsmass:** kleineres, fachlich verständlich benanntes und sauber dokumentiertes Repository bei unveränderter bzw. belegter Qualität.
