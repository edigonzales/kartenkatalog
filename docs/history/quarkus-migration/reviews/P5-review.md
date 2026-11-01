# Unabhängiger Review P5 FINAL – kumulative Gesamtabnahme P0–P5

## 1. Identität, Provider und Kandidat

- **Prüfer:** OpenCode CLI **2.1.4** (`/Applications/OpenCode.app/Contents/Resources/opencode-cli --version`), gestartet als unabhängiger Reviewer (nicht Implementierungsagent). Der Nutzer hat die CLI ausdrücklich erlaubt. Es wurde **kein** OpenCode-gehostetes Modell verwendet.
- **Session:** `ses_e6b2668b0ffe9s5ipx1xIbv4vg` (Titel „P5 final cumulative independent review a6ded992“). Session-Export: `identity/session-export.json`; kompakte Identität: `identity/cli-session.txt`.
- **Tatsächlicher Provider/Modell aus Session und Provider-Metadaten:** `providerID=deepseek`, `id=deepseek-flash`, Variante `default`. Provider-Metadaten (`identity/provider-models-metadata.json`): Provider **DeepSeek**, `api=https://api.deepseek.com`, npm `@ai-sdk/openai-compatible`, Doku `https://api-docs.deepseek.com/quick_start/pricing`. Damit ist es **nicht** der OpenCode-/Zen-/Go-/OpenRouter-Pfad.
- **Offizielle Modellzuordnung:** Die DeepSeek-Doku (am 2026-11-01 abgerufen) nennt für den Modellnamen `deepseek-flash` die Version **DeepSeek-V4.1-Flash-1017**; `deepseek-v4-pro` ist eine andere Modellversion. Das passt exakt zur Sessionkennung `deepseek-flash`.
- **Direktverbindung:** DNS `api.deepseek.com` → `3.173.21.63`; während der Session bestand eine echte TLS-Verbindung des CLI-Servers zu `3.173.21.63:443`. Die Session weist echte Kosten von **0,03208122 USD** aus, also einen tatsächlichen DeepSeek-API-Aufruf und keinen kostenlosen Fremdprovider. Belege: `identity/connection.txt`, `identity/session-export.json`.
- **Credentials:** Es wurden keine gespeicherten Credentials gelesen, ausgegeben oder neu eingerichtet. Die Identitätsprüfung erfolgte über Sessionmetadaten, Provider-Metadaten, DNS/TCP und die offizielle Doku.
- **Wichtiger cwd-Hinweis:** Der Session-Datensatz nennt als `location.directory` noch `/Users/stefan/sources/kartenkatalog` (Hauptcheckout), nicht den Review-Worktree. Deshalb wurde **jeder** Repository-/Build-/Test-/Probe-Prozess explizit mit `cd /Users/stefan/sources/kartenkatalog-review-P5` gestartet. Die tatsächlichen Prozesse liefen nachweislich im Review-Worktree; `pwd`, `git rev-parse HEAD` und der leere `git status` wurden vor und nach der Prüfung erhoben.
- **BASE_SHA:** `bdb22973557d785c7c46d90595a924b91db44a43`
- **CANDIDATE_SHA:** `a6ded992019cedce3d448f996518cadeeb3defbc`
- **Review-HEAD:** `a6ded992019cedce3d448f996518cadeeb3defbc` (exakt).
- **Review-Worktree:** `/Users/stefan/sources/kartenkatalog-review-P5`; `git status --porcelain` vorher und nachher **leer**; `git diff --check` Exit 0. Keine getrackten Source-, Test-, Build-, Daten- oder Kriterienänderungen; keine Commits/Push/Deploy. Belege: `identity/final-worktree.txt`, `evidence-summary.json`.
- **P5-Diff BASE..CANDIDATE:** 16 Dateien, 248 Einfügungen, 613 Löschungen. Inhalt: P5-Dokumentation, CI-/CORS-Probe, P4-Harness-Erweiterung, Kommentar in `CatalogDtos`, Entfernen von sechs alten Spring-`static/`-Dateien und JTE-Ignore, neue Test-Fixture `src/test/resources/final/catalog.xml`. Keine Änderung der fachlichen Java-Logik, der öffentlichen DTO-Felder, der Templates oder der Such-/Filterlogik. Der Kandidat wurde vollständig gelesen, nicht nur der Diff.
- **AGENTS.md:** Im Repo und in den geprüften Elternpfaden nicht vorhanden.
- **Umgebung:** macOS 15.8.1/arm64, Temurin JDK **21.0.7+6**, Gradle **9.8.2**, Docker **29.8.2** (`linux/arm64`), Python 3.9.6, Playwright **1.60.0** (Chromium/Firefox). Lokal vorhanden, aber für die Linux-Abnahme nicht als Compiler verwendet: Oracle GraalVM **25.0.3**. Linux-Native-Compiler im gepinnten Builder: **Mandrel 25.0.4.1-Final**, GCC 11.5, aarch64. Anwendungsklassen: **Java-Major 65** (`javap -verbose`).

## 2. Eigenständige Code- und Verhaltensanalyse

### 2.1 XTF 2.4, Validierung und immutabler Snapshot

- `CatalogXtfSource` akzeptiert ausschließlich `file:`/`classpath:` und wirft sonst; kein HTTP-Bootabruf, kein Mock-/DB-Fallback (`CatalogXtfSource.java`).
- `XtfCatalogParser` prüft INTERLIS-Namespace und Modell `SO_AGI_Kartenkatalog_Publikation_20251210`, genau eine Header-/Datasection, Basket-Namespace, eindeutige TIDs, UUIDs, Pflichtfelder, `Thema_R`, doppelte Layer-Identifier, Transparency 0..100 und `service_download`. DTD wird über `disallow-doctype-decl` verboten, externe Entities/Dtd/Schema sind deaktiviert, XInclude aus. Die Negativfälle (falsches Modell, fehlende/doppelte Referenz, XXE, ungültige UUID, Nested-Content, Duplikate) sind in `XtfCatalogParserTests` abgedeckt und wurden frisch ausgeführt.
- `CatalogSnapshot` kopiert Themen, Layer, Maps und verschachtelte Listen mit `List.copyOf`/`Map.copyOf`; `CatalogSnapshotTests`-nahe Assertions in `XtfCatalogParserTests.suppliedTransferCountsAndAllNestedCollectionsAreImmutable` prüfen UnsupportedOperationException auf allen Collections. Es gibt keinen Reload-Pfad und keine `AtomicReference`-/Lock-Maschinerie.
- `CatalogStore` ist `@Startup @ApplicationScoped`, baut den Snapshot einmalig im Konstruktor, wirft bei Fehler eine `IllegalStateException` (Quarkus-Start bricht ab) und schließt den Index in `@PreDestroy`. `CatalogReadiness` meldet `DOWN`, sobald der Index nicht verfügbar ist.
- P1-Prozessprobe: Nach dem Start wurde die XTF-Quelldatei im temporären Verzeichnis durch `<invalid/>` ersetzt; der laufende Prozess lieferte weiterhin 886 Ebenen und `ready=UP`. Das belegt Snapshot-Lebensdauer ohne Parsing pro Request (`logs/03-p1-process.log`, `reports/p1`).

### 2.2 Lucene 10.6.0, Heap-Index, Suche und Facets

- `build.gradle` pinnt `quarkus-bom:3.40.1.1`, `lucene-core:10.6.0`, `lucene-analysis-common:10.6.0`; `lucene-queryparser` ist nicht vorhanden. Der aufgelöste Runtime-Graph (`logs/02-dependencies.log`) enthält nur diese beiden Lucene-Artefakte und keine Spring-/JTE-/JDBC-/PostgreSQL-/Hikari-/Hibernate-Artefakte.
- `CatalogSearchIndexBuilder` verwendet ausschließlich `new ByteBuffersDirectory()` und `new GermanAnalyzer(CharArraySet.EMPTY_SET)`; kein `FSDirectory`/`MMapDirectory`. Writer wird vor dem Reader geschlossen, Reader/Analyzer/Directory werden auf Erfolgs- und Fehlerpfaden geschlossen (`LuceneCatalogSearchIndex.close`, Builder-Catch).
- `CatalogDocumentMapper` erzeugt genau ein Dokument pro `Ebene`, speichert `id=ident_part`, indexiert Identifier/Titel/Keywords/Synonyme/Thema/Organisation als normalisierte `StringField`s, `dtype` als Filterfeld und die Jsoup-bereinigte Beschreibung als `TextField` mit GermanAnalyzer.
- `LuceneCatalogSearchIndex.search` konstruiert kontrollierte `BooleanQuery`/`TermQuery`/`PrefixQuery`/`WildcardQuery`/`DisjunctionMaxQuery`; keine Queryparser-Syntax. Jeder Suchtoken muss über mindestens ein Feld matchen (AND über Tokens), Felder sind OR-verknüpft. Feste Rankingstufen: Identifier 128 > exakter Titel 64 > Titelpräfix 32 > Titelteilstring 16 > Keyword/Synonym 8 > Thema/Organisation 4 > Beschreibung 1. `SearchText` normalisiert Groß-/Kleinschreibung, Umlaute/Akzente, ß→ss, Interpunktion und Leerraum; `q` ist auf 200 Zeichen begrenzt, Einzeichen-/Sonderzeichensuche ist 400, `AV` ist ein gültiger Zwei-Zeichen-Fall.
- `CatalogSearchService` löst Treffer über die Snapshot-Map in Fachobjekte auf, filtert danach, sortiert de-CH per Collator (Titel A–Z/Z–A, Relevanz nur mit q) und behandelt Indexausfall als `SearchUnavailableException` (503), nie als leere Erfolgsliste.
- `CatalogFacetService` zählt je Kategorie unter Suchtext und allen **anderen** Filtergruppen, lässt die eigene Kategorie aus, behält ausgewählte 0-Treffer-Werte und liefert stabile Quellwerte als IDs. Organisation wird nur aus echten `org_name`-Werten gebildet; der Harvest hat keine Organisation und blendet die Gruppe im UI aus.
- Eigene Native-Gegenprobe (unten) bestätigt Cross-Field-AND, Mehrfachfilter, dynamische Facets, fehlende Metadaten bei gültigem Thema, Ranking, HTML/HTMX/Detail/QML und XSS-Sanitization.

### 2.3 API/QWC2, Fehler und HTML

- `CatalogApiController` bietet `/api/v1/themen`, `/themen/{identifier}/ebenen`, `/ebenen/{id}`, `/ebenen` und `/catalog`; die fünf Formen wurden über den P0-Smoke und JUnit/HTTP frisch geprüft. `CatalogDtos` hält die P0-Feldnamen, Null-/Array-Semantik und `qwcLayerName = ident_part`; `sourceId` bleibt die separate UUID. Die P5-Änderung an `CatalogDtos` ist nur ein Kommentar.
- `ProblemExceptionMapper`/`QueryExceptionMapper`/`SearchExceptionMapper` liefern `application/problem+json` mit 400/404/503 ohne Stacktrace/Quelltext. Unbekannte Themen bleiben 404; unbekannte Typen/Organisationen/Sortierung 400; unbekannter Layer/QML 404; Indexausfall 503.
- P0-Baseline `src/test/resources/contract/baseline.json` ist unverändert (`cd1167f8…`), Harvest unverändert (`de40f025…`). Der P0-Smoke wurde frisch gegen den aktuellen JVM-Prozess, den Dev-Prozess und beide Container-Runtimes ausgeführt (Exit 0); die Vertragsmutationen (Feldumbenennung/404→200) wurden als Canary erkannt.
- HTML: `/` und `/thema` liefern die vollständige Katalogseite; `/themen/{identifier}/ebenen` liefert ohne HTMX die Vollseite und mit `HX-Request: true` nur das Ebenenfragment; `/ebene/{id}` Detail; `/ebene/{id}/style` exaktes XML-Attachment oder 404; `/ping`; `/q/health/live|ready`.
- `@CheckedTemplate` in `CatalogPages` bindet `CatalogPage`, `ThemePage`, `Detail`, `ThemePage.layers` typisiert. Ein externer Qute-Build-Canary mit `{product.nonexistentReviewerField}` scheitert wie erforderlich mit `QuteProcessor.processTemplateErrors`/`Property/method … not found`; der unveränderte externe Copy-Build ist grün.
- `CatalogPageVmFactory.safe(...)` ist die einzige `RawString`-Quelle für Beschreibungen und ruft zuerst `CatalogText.html` (Jsoup `Safelist.basic`, `rel=noopener noreferrer`) auf. Kein Template verwendet `raw`; alle anderen Werte werden von Qute escaped. `CatalogUrlFactory.external` lässt nur absolute HTTP(S)-URLs mit Host ohne Userinfo zu; `javascript:`/`data:`/`file:`/protokollrelative URLs werden verworfen. `CatalogText.html` entfernt `<script>`, `onerror`, `onclick` und `javascript:`-Hrefs; ein eigener nativer Sicherheitscanary bestätigt das.
- UI: native `details`-Themenzeilen mit `aria-controls`, serverseitigen Fallback-Links und HTMX-Nachladen; Such-/Filterband mit GET-Form, Mehrfach-Checkboxen, Chips, Reset, Sortierung, Desktop-Popover und mobilem nativen `<dialog>`-Sheet. `catalog.js` hält `aria-expanded`, Fokus, History/Restore, Fehlerzustand und Expand-Zustand; `HX-History-Restore-Request` liefert die Vollseite, nicht das Fragment. Ohne JavaScript bleiben Formular, native Details und Themenlinks nutzbar. Es gibt keinen Datumsfilter, keine erfundenen Organisationen/Downloads, keine CDN-Laufzeitabhängigkeit.

### 2.4 Build, Cleanup und Betrieb

- Quarkus **3.40.1.1**, Java-Toolchain **21**, Gradle **9.8.2**, Lucene **10.6.0**; `./gradlew --version` und `javap` Major 65.
- Aktive Quellen/Templates/Dockerfiles/CI enthalten keine Spring-/JTE-/JDBC-/PostgreSQL-/Hikari-/Hibernate-Nutzung. Der einzige `grep`-Treffer in `fonts.css` ist ein zufälliges „jte“ in einem Base64-Fontblob, kein Code. Historische Erwähnungen in `docs/` sind nach AC5-01 zulässig.
- Docker: JVM-Image auf Temurin 21 UBI9, Native-Image auf UBI9 micro, beide mit gepinnten Digests, Non-Root, `/catalog` read-only, `/tmp` als flüchtiges tmpfs. `deploy/openshift.yaml` ist eine Vorlage mit `runAsNonRoot`, read-only RootFS, `ALL`-Cap-Drop, `emptyDir`-RAM-Scratch und read-only XTF-PVC; kein DB-Secret.
- README/OPERATIONS beschreiben Quarkus-Dev/JVM/Native, XTF-Quelle, unvollständige Harvest-Daten, API, QWC2-Dev-CORS, Linux-Container, Health, Restart und Fehlerdiagnose. Die tatsächlich verwendeten Kernkommandos wurden ausgeführt; die QWC2-CORS-Freigabe ist gezielt und nicht pauschal.

## 3. Abnahmekriterien P0–P5 (28 ACs)

Jede Zeile wurde unabhängig an Code und tatsächlichem Verhalten geprüft. „Erfüllt“ bedeutet nicht „Test grün“, sondern eigener Code-/HTTP-/Prozess-/Browser-/Native-Beleg.

| AC-ID | Ergebnis | Konkreter eigener Beleg | Finding |
|---|---|---|---|
| **AC0-01** | erfüllt | P0-Bericht/Matrix gelesen; aktueller xtf-Start, `test`, `/api/v1/catalog`, `/ebenen`, `/themen`, `/thema`, `/ebene/{id}` frisch über `p1`/P0-Smoke; HEAD/Version/Struktur erfasst. | keines |
| **AC0-02** | erfüllt | `src/test/resources/contract/baseline.json` (Felder, Null/Arrays, Produkte, 400/404) unverändert; P0-Smoke gegen aktuelle App Exit 0; Canary mit Feldumbenennung/404→200 erkannt. | keines |
| **AC0-03** | erfüllt | P0-Refactoring-Matrix KEEP/PORT/REMOVE und SODATA-UI-Referenz in `implementation/P0.md`; tatsächliche Quellen/Templates gelesen. | keines |
| **AC0-04** | erfüllt | Eigene P0-Gegenprobe: `qwcLayerName == id`, unbekannter Layer 404, fehlendes QML 404, Problem-JSON; Handoff/HEAD/SHA stimmen. | keines |
| **AC1-01** | erfüllt | Frischer `clean test quarkusBuild quarkusIntTest playwrightTest` (Exit 0); `quarkusDev` via `p1 --dev`; JVM-Prozess/JVM-Image; Runtime-Graph ohne Spring/JTE/JDBC/Postgres/Hikari; Quarkus 3.40.1.1/JDK21. | keines |
| **AC1-02** | erfüllt | Harvest 169/886; Parser-/XXE-/Referenz-/Duplikat-/Fremdmodell-/Missing-Fälle in JUnit und `p1` (8 harte Startabbrüche); eigener gebrochener Themenverweis im Native-Start (Exit 1) und kein Fallback. | keines |
| **AC1-03** | erfüllt | P0-Smoke alle fünf API-Formen, Feld-/Null-/Array-Vertrag, `qwcLayerName`, 400/404/Problem-JSON; Canaries. | keines |
| **AC1-04** | erfüllt | `/`, `/thema`, `/themen/{id}/ebenen`, `/ebene/{id}`, QML/404, `/ping`, Health; Qute-Build-Canary; kein Mock-/DB-Fallback. | keines |
| **AC2-01** | erfüllt | Dependency-Graph nur Lucene core/analysis-common 10.6.0; Tests/Logs `ByteBuffersDirectory`/`GermanAnalyzer`; 886 Dokumente/IDs; kein Diskindex (p2/p4/own scratch leer); deterministischer Rebuild; Indexausfall 503/DOWN; kein Queryparser. | keines |
| **AC2-02** | erfüllt | JUnit-Suche + eigene Native-Probe: Identifier/Titel/Substring/Keywords/Synonyme/Thema/Organisation/Beschreibung, Umlaute/ß, AND-Tokens/OR-Felder, `AV`, Cross-Field, Ranking-Tie; fehlende Metadaten in separater Fixture. | keines |
| **AC2-03** | erfüllt | JUnit/HTTP + eigene Native-Probe: `thema`/`typ`/`organisation` mehrfach, OR/AND, Null/fehlend, dynamische Facets (eigene Gruppe ausgenommen), stabile IDs, `q` 200/400, leere Schnittmenge `[]`. | keines |
| **AC2-04** | erfüllt | P0/1-API-Regression; Sortierung A–Z/Z–A/Relevanz; separates JVM-Prozess-Smoke mit frischem RAM-Index; eigene Native-Cross-Layer-Probe; frische JVM-Reports. | keines |
| **AC3A-01** | erfüllt | `@CheckedTemplate` typisiert; externer Qute-Canary schlägt bei unbekanntem Feld fehl; lokales Layout/Header/Breadcrumb/Footer/Skip-Link/CSS; keine JTE/CDN-Laufzeit. | keines |
| **AC3A-02** | erfüllt | Themenliste als native `details` mit echten Ebenen/Counts, `aria-controls`, Fallback-Link; No-JS-Browserprobe; Themen-/Detailseiten und 404. | keines |
| **AC3A-03** | erfüllt | Detailseite nur mit echten Feldern, Kartenlink/Transparenz, QML nur bei Vorhandensein; eigene XSS-/Link-Schema-Gegenprobe (script/onerror/javascript entfernt, HTTPS-Link bleibt). | keines |
| **AC3A-04** | erfüllt | Chromium-Desktop-Smoke, visuelle Screenshots, eigener XSS-/Deep-Link-Gegenfall; kumulativer JVM/API-Lauf grün. | keines |
| **AC3B-01** | erfüllt | UI/JSON/HTMX-Parität in JUnit und eigener Native-Probe; Thema/Produkttyp/Organisation nur echte Werte, Mehrfachwahl/Reset/Chips/Counts, kein Datumsfilter, leere Gruppen ausgeblendet. | keines |
| **AC3B-02** | erfüllt | HTMX-Fragmente ohne Dokumenthülle, `#catalog-state`/`.theme-layers`, URL-Push; eigene Mobile-Browserprobe für Back/Reload/Deep-Link, Chip-Entfernung, Expand-Stabilität; No-JS-GET. | keines |
| **AC3B-03** | erfüllt | Frische 14 Playwright-Tests Chromium+Firefox Desktop 1440×900/Mobile 390×844; eigene 47 Browser-Checks; Fokus/Popover/Sheet/Overflow/Empty/Reset/Link; Screenshots und Referenzvergleich. | keines |
| **AC3B-04** | erfüllt | Kumulative UI-Prüfung P3a+P3b am finalen SHA; eigener neuer Mobile-/History-/Risiko-Browserfall; T1/T2/T3 frisch; alle AC3A/B bewertet. | keines |
| **AC4-01** | erfüllt | Linux/aarch64-ELF mit Quarkus 3.40.1.1/Lucene 10.6.0; Mandrel 25.0.4.1 im gepinnten Builder; Native-Start loggt `ByteBuffersDirectory`/`GermanAnalyzer`, 886 Dokumente, Index 119 ms; kein 10.3.2/FSDirectory. | keines |
| **AC4-02** | erfüllt | 20 echte getrennte JVM-/Native-Container-Prozesse: Listen/Detail/Suche/Ranking/Filter/HTMX/JSON-Parität, exakte QML-Bodies, Cross-Layer-Fixture, Restart/Quelländerung; defekte XML/XXE/Referenz/Index beenden Start; keine Indexdateien. | keines |
| **AC4-03** | erfüllt | Health live/ready, Startlog mit Quelle/Counts/Lucene/Indexdauer/Heap, keine Secrets; reproduzierbarer lokaler Build, CI-Workflow vorhanden (nicht remote ausgeführt); Linux-Container als UID `1000730000:0`, read-only RootFS/XTF, RAM-Scratch; Runbook/Restart. | keines |
| **AC4-04** | erfüllt | Eigene Native-Cross-Layer-/Startfehlerprobe (67 Checks) und eigene Native-Browserprobe (47 Checks); frische T1–T4; Security-/Dependency-Prüfung; kein Pflichtskip. | keines |
| **AC5-01** | erfüllt | Runtime-Graph ohne Spring/JTE/JDBC/Postgres/Hikari/Hibernate; aktive Quellen/Templates/Dockerfiles/CI bereinigt; `grep` nur historische Docs/Base64-Font-Fehltreffer; Worktree sauber. | keines |
| **AC5-02** | erfüllt | README/OPERATIONS aktuell; Dev/JVM/Native, XTF-Quelle, Unvollständigkeitswarnung, API, QWC2-CORS, Deployment, Tests, Troubleshooting; Kernkommandos tatsächlich ausgeführt. | Forward-Link auf `reviews/P5-review.md` ist erwartbarer Docs-Punkt nach diesem Review; kein AC-Verstoss. |
| **AC5-03** | erfüllt | Vollständige Matrix am finalen SHA: 62/3/14 JUnit/Integration/Browser, 20 Verteilungsprozesse, 67 Native-Checks, 47 Browser-Checks, Canaries; 0 Fehler/0 Failures/0 unzulässige Skips; JVM und Native indexieren aus XTF. | keines |
| **AC5-04** | erfüllt | Unabhängige Gesamtabnahme aller Ziele, P0-Vertrag, DB-Freiheit, Suche/Facets, SODATA-UI, Qute-Sicherheit, Native-/Linux-Betrieb, eigene neue Cross-Layer-Gegenprobe; ausdrückliches PASS unten. | keines |

## 4. Eigene neue Cross-Layer-Gegenprobe (Native-Prozess)

**Warum dieser Fall kritisch ist:** Er kombiniert die riskantesten Schichten – eigene XTF mit fehlenden optionalen Metadaten bei gültigem Thema, Cross-Field-Suche, Mehrfachfilter über Gruppen, dynamische Facets, Qute-Vollseite/HTMX/Detail/Karte/QML und harte Startfehler – in einem echten Linux-Native-Container. Er ist weder eine Umbenennung noch eine Wiederholung der Kandidaten-Fixture: eigene Themen `umwelt`/`verkehr`, eigene Layer `review.probe.hochwasser`, `review.probe.damm`, `review.probe.luft`, `review.probe.verkehr`, eigene Query und eigene Assertions.

- **Fixture:** `probe/review-cross.xml`; **Probeskript:** `probe/native-cross-probe.py`.
- **Query:** `q=Hochwasser Damm&thema=umwelt&typ=datasetview&typ=extwms&organisation=Amt für Umwelt` → genau `review.probe.hochwasser`; ohne Organisation → `review.probe.hochwasser` + `review.probe.damm`; `thema=verkehr` → `[]`.
- **Fehlende Metadaten:** `review.probe.damm` hat gültiges Thema `umwelt`, aber keine Organisation/Beschreibung/Keywords/Transparenz/Download; `review.probe.luft` hat QML; `review.probe.hochwasser` hat unsicheres HTML (`script`, `onerror`, `javascript:`) und einen sicheren HTTPS-Link.
- **Command (cwd Review-Worktree):** `python3 /Users/stefan/sources/kartenkatalog-P5-review-evidence/a6ded992019cedce3d448f996518cadeeb3defbc/probe/native-cross-probe.py`
- **Exit:** `0`; **Ergebnis:** **67/67 Checks passed** (`probe/native-cross-result.json`, `logs/10-own-native-cross.log`).
- **Beobachtete Kernpunkte:** 4 eindeutige Layer; `qwcLayerName==id`; Cross-Field-AND; `Inundation`/`Feinstaub`/`LUFTQUALITÄT`; exakter Titel `Damm Zustand` zuerst; Mehrfachfilter; leere Schnittmenge; dynamische Facet-Counts; Vollseite/HTMX; Detail ohne leere Felder; Kartenlink mit `%5B35%5D`; exakter QML-Body; XSS entfernt; 404/400 Problem-JSON; read-only RootFS/XTF, UID `1000730000:0`, `CapDrop=ALL`, kein Indexvolume, `docker diff` leer, keine Lucene-Dateien im Scratch; defekter Themenverweis beendet Native-Start mit Exit 1 und `Cannot load catalog XTF`.
- **Interpretation:** Die Kombination aus Snapshot, Suche, Filtern, Facets, Qute/HTMX und Native-Startfehler verhält sich konsistent; es gibt keine regressiven Wechselwirkungen.

## 5. Eigene Browserprobe und visueller Vergleich

- **Command (cwd Review-Worktree):** `java -cp "<Evidence>/probe/classes:<Test-Runtime-Classpath>" BrowserProbe`
- **Exit:** `0`; **Ergebnis:** **47/47 Browser-Checks** (`logs/12-own-browser-probe.log`). Geprüft wurden Chromium und Firefox bei **390×844** sowie Chromium bei **1440×900**, jeweils gegen den echten Native-Container mit eigener Fixture.
- **Risikokombination/Mobile/History:** Suchtext + `thema=umwelt` + `typ=datasetview` + `typ=extwms` + `organisation=Amt für Umwelt`; mobiles modales Sheet, 4 Checkboxen/5 Chips, 1 Thema/1 Ebene; Expand; Detail; Kartenlink; Back nach Detail; Reload; Chip-Entfernung → 2 Ebenen; erneutes Back → 4 Checkboxen; No-JS-Fallback; keine horizontalen Overflows; keine externen Runtime-Requests; keine Page-Errors.
- **Desktop:** Popover öffnet ohne Layout-Shift; Escape schließt; URL/Chips korrekt; kein Overflow.
- **Frische Screenshots:** `probe/screenshots/chromium-mobile-sheet.png`, `chromium-mobile-results.png`, `chromium-mobile-detail.png`, `chromium-desktop-filter.png`, `chromium-desktop-results.png`, `firefox-mobile-sheet.png`, `firefox-mobile-results.png`, `firefox-mobile-detail.png`. Zusätzlich Rohscreenshots der Kandidatenläufe unter `junit/p3a`/`junit/p3b`.
- **Sichtprüfung (Bilder tatsächlich geöffnet):**
  - `docs/quarkus-migration/reference/sodata-desktop.png`: Solothurn-Header, Breadcrumb, große Überschrift/Lead, breites Suchband, Filter-Dropdowns, Ergebniszahl, ruhige Listenzeile mit Plus-Expansion. Die frische Harvest-Desktop-Aufnahme zeigt dieselbe Struktur mit „Kartenkatalog“, Themen/„Kartenebene“ statt Datensätzen und ohne Datums-/Download-Spalten – die im Plan begründeten fachlichen Unterschiede.
  - `sodata-filter-desktop.png`: Popover mit Checkboxen/Counts, keine Layout-Verschiebung. Die frische Desktop-Filteraufnahme zeigt dasselbe Muster, inklusive dynamischer Counts (z. B. „Umwelt & Wasser 1“, „Verkehr 0“) und ohne „Aktualisiert“.
  - `sodata-filter-mobile.png`: mobiles Filtersheet mit Gruppen, Counts, Reset/Apply. Die frische mobile Sheet-Aufnahme zeigt das native modale Sheet mit Gruppen/Counts/Apply; der Reset-Link liegt im scrollbaren Sheet unter den Gruppen, zusätzlich existieren Chips und der Such-Clear-Button. Das ist eine bewusste, funktional gleichwertige mobile Lösung, kein fehlender Reset.
- **Befund:** Keine visuelle oder funktionale Abweichung, die ein AC verletzt. Die fachlichen Unterschiede (Themen/Kartenebenen, Produkttyp, echte Organisationswerte, keine Datums-/Download-Attrappen) sind planentsprechend.

## 6. Negative Canaries

| Canary | Erwartung | Tatsächlicher Lauf | Exit | Beleg |
|---|---|---|---|---|
| Vertragsfeld `qwcLayerName` | Ein Proxy benennt das Feld in `legacyLayerName` um; der unveränderte P0-Smoke muss scheitern. | Control gegen unveränderte App **0**; Feldmutation **1** mit `AssertionError` beim Layer-Vergleich. | 0 / 1 | `probe/contract-canary.py`, `probe/canary-field.log`, `logs/13-contract-canary.log` |
| 404-Vertrag | Ein Proxy macht `/api/v1/ebenen/unknown` von 404 zu 200; P0-Smoke muss scheitern. | Control **0**; 404→200 **1** mit `AssertionError (… 200, 404 …)`. | 0 / 1 | `probe/canary-404.log` |
| Falscher Filter | Korrekte Filter liefern `[]` bzw. genau einen Treffer; eine bewusst falsche Erwartung (Thema ignoriert) muss scheitern. | Korrekte Checks für `thema=verkehr`, `extwms+organisation`, Matching-Filter und HTML-Empty-State bestanden; falsche Erwartung mit `CANARY EXPECTED FAILURE: theme filter was ignored` beendet den Lauf. | 1 (erwartet) | `probe/filter-canary.py`, `logs/14-filter-canary.log` |
| Unsicheres HTML | `<script>`, `onerror`, `javascript:` dürfen im Detail nicht erscheinen; `<strong>` und sicherer HTTPS-Link bleiben. | Korrekte Sanitization bestanden; bewusst falsche „XSS vorhanden“-Erwartung schlug fehl. | 0 (Canary korrekt) | `probe/security-canary.py`, `logs/17-security-canary.log` |
| Themenreferenz | XTF mit `Thema_R ref="missing"` muss den Start vor Ready beenden. | JVM/Native-Start Exit 1, Log `Cannot load catalog XTF`; eigene Native-Probe ebenfalls Exit 1. | 1 (erwartet) | `probe/review-cross-broken.xml`, `logs/17-security-canary.log`, `probe/native-cross-result.json` |
| Qute-typisiertes Template | Externer Copy-Build mit unbekanntem Feld `{product.nonexistentReviewerField}` muss fehlschlagen; Baseline-Copy muss bauen. | Baseline-Copy `quarkusBuild` **0**; mutierte Copy **1** mit `QuteProcessor.processTemplateErrors`: `Property/method [nonexistentReviewerField] not found`. Kandidat unverändert. | 0 / 1 | `logs/15-qute-canary-baseline.log`, `logs/16-qute-canary-mutated.log`, `probe/qute-canary/` |
| Zusätzliche Randparameter | Leere Filterparameter dürfen keinen 500 erzeugen. | `?thema=`, `?typ=`, `?organisation=`, `?q=`, `?sort=`, Duplikate und `/?thema=` lieferten 200 mit sinnvoller Semantik; keine 500. | 0 | `logs/18-edge-params.log` |

Die Canaries ersetzen keine der Kandidatentests; sie sind zusätzliche, eigene Fehlgrün-Prüfungen. Der Kandidat wurde für den Qute-Canary über `git archive HEAD` in ein externes Verzeichnis kopiert und dort mutiert; der Review-Worktree blieb sauber.

## 7. Pflichttests, frische JUnit-Zahlen und Rohreports

Alle Befehle wurden mit `cwd=/Users/stefan/sources/kartenkatalog-review-P5` auf dem exakten Kandidaten-SHA ausgeführt. XML-Zahlen wurden selbst aus den `TEST-*.xml`-Wurzelattributen summiert (nicht aus Konsolen-Summaries).

| Prüfung / Befehl | Exit | Tests / Failures / Errors / Skips | Rohreport |
|---|---:|---|---|
| `./gradlew clean test quarkusBuild quarkusIntTest playwrightTest --console=plain` | **0** | **62/0/0/0** Unit+HTTP; **3/0/0/0** JVM-Paket; **14/0/0/0** Browser | `logs/01-jvm-gates.log`, `junit/test`, `junit/quarkusIntTest`, `junit/playwrightTest`, `junit/html-reports` |
| `./gradlew dependencies --configuration runtimeClasspath --console=plain` | **0** | nur `lucene-core:10.6.0` + `lucene-analysis-common:10.6.0`; kein Queryparser/Spring/JTE/JDBC/DB | `logs/02-dependencies.log` |
| `python3 scripts/p1-process-contract.py` | **0** | JVM-Start/Netz/Health/P0-Vertrag; 8 harte Startabbrüche (malformed, wrong-model, dangling-reference, duplicate-layer, xxe, missing-file, missing-classpath, http-source) | `logs/03-p1-process.log`, `reports/p1` |
| `python3 scripts/p1-process-contract.py --dev` | **0** | echte `quarkusDev`-App; CORS OPTIONS/GET `http://localhost:8081` 200 mit exaktem Allow-Origin; `http://localhost:9999` 403 ohne Allow-Origin; keine Credentials-Freigabe | `logs/04-p1-dev-cors.log`, `reports/p1/dev-summary.json` |
| `python3 scripts/p2-process-contract.py` | **0** | Harvest/Neustart/Quelländerung/read-only/kein Diskindex; echter Index-Startfehler Exit 1 | `logs/05-p2-process.log`, `reports/p2` |
| `docker build -f src/main/docker/Dockerfile.jvm -t kartenkatalog:p5-review-a6ded992-jvm .` | **0** | JVM-Image `linux/arm64` `sha256:4a0f84…` | `logs/06-jvm-image.log`, `identity/jvm-image.txt` |
| `./gradlew quarkusBuild -Dquarkus.native.enabled=true -Dquarkus.package.jar.enabled=false -Dquarkus.native.container-build=true -Dquarkus.native.container-runtime=docker -Dquarkus.native.builder-image=quay.io/quarkus/ubi9-quarkus-mandrel-builder-image@sha256:29a5078c94489623ef7e554aad2cbff70a4ff872ac6d60a46f5538697ed82a30 -Dquarkus.native.native-image-xmx=4g --console=plain` | **0** | echtes Linux/aarch64-ELF; Mandrel 25.0.4.1-Final; 3m54s Native-Image | `logs/07-linux-native-build.log`, `identity/native-elf.txt` |
| `docker build -f src/main/docker/Dockerfile.native -t kartenkatalog:p5-review-a6ded992-native .` | **0** | Native-Image `linux/arm64` `sha256:42b367…` | `logs/08-native-image.log`, `identity/native-image.txt` |
| `python3 scripts/p4-distribution-contract.py --jvm-image kartenkatalog:p5-review-a6ded992-jvm --native-image kartenkatalog:p5-review-a6ded992-native --report-dir <Evidence>/distribution` | **0** | **20** echte getrennte JVM-/Native-Container-Prozesse (10 je Runtime); Payload-Hashes identisch; defekte XTF/XXE/Referenz/Index je Runtime Exit 1 | `logs/09-distribution-contract.log`, `distribution/summary.json`, `distribution/*.log`, `distribution/*-tmp-files.json` |
| Eigene Native-Cross-Layer-Probe | **0** | **67/67** eigene Checks | `logs/10-own-native-cross.log`, `probe/native-cross-result.json` |
| Eigene Browserprobe | **0** | **47/47** eigene Checks (Chromium+Firefox mobile, Chromium desktop) | `logs/12-own-browser-probe.log`, `probe/screenshots`, `probe/browser-native.log` |
| Vertrags-/404-Canary | Control **0**, Feld **1**, 404 **1** | erwartete Fehlgrünerkennung | `logs/13-contract-canary.log`, `probe/canary-*.log` |
| Filter-Canary | **1** (erwartet) | korrekte Semantik bestanden, falsche Erwartung scheitert | `logs/14-filter-canary.log` |
| Qute-Canary | Baseline **0**, mutiert **1** | typisierte Qute-Validierung greift | `logs/15-qute-canary-baseline.log`, `logs/16-qute-canary-mutated.log` |
| Security-/Themenreferenz-Canary | **0** (korrekte Sanitization; gebrochener Start Exit 1) | XSS inert, fehlende Referenz harter Startfehler | `logs/17-security-canary.log` |
| Randparameter | **0** | leere Filter 200, keine 500 | `logs/18-edge-params.log` |

**Frisch gezählte JUnit-Rohzahlen:** `test=62, failures=0, errors=0, skipped=0` (11 XML-Dateien); `quarkusIntTest=3/0/0/0` (1 XML); `playwrightTest=14/0/0/0` (2 XML). Es gibt keine `@Disabled`, keine `assumeTrue`/`Assumptions` und keine als Skip getarnten Pflichtfälle. `test` schließt die mit `@Tag("playwright")` markierten Fälle bewusst aus; `playwrightTest` führt sie vollständig aus. `build/test-results` wurde nach dem Lauf nach `junit/` kopiert.

**Nicht ausgeführte, aber nicht pflichtige Prüfungen:** macOS-`testNative`/macOS-Native-Browser (optional; die Pflicht-T4 wurde auf Linux/arm64 erbracht), amd64, OpenShift-Cluster, Remote-CI. Diese werden **nicht** als Erfolg behauptet. Die fälligen Linux-Native-/Container-/Prozessprüfungen wurden vollständig ausgeführt und sind kein Skip.

## 8. Native-, Linux- und Read-only-Belege

- **Native-ELF:** `build/kartenkatalog-0.0.LOCALBUILD-runner`: `ELF 64-bit LSB executable, ARM aarch64, dynamically linked, GNU/Linux 3.7.0, stripped` (`identity/native-elf.txt`).
- **Builder:** `quay.io/quarkus/ubi9-quarkus-mandrel-builder-image@sha256:29a5078c94489623ef7e554aad2cbff70a4ff872ac6d60a46f5538697ed82a30`; Mandrel **25.0.4.1-Final**, GCC 11.5.0, target armv8.1-a (`logs/07-linux-native-build.log`). Die Anwendung bleibt Java-21/Major 65.
- **Native-Startlog (echter Container):** `Catalog loaded: 169 themes, 886 layers; Lucene 10.6.0 ByteBuffersDirectory GermanAnalyzer; index 119 ms, catalog startup 254 ms, heap used 11520 KiB from file:/catalog/harvest.xml`; `native (powered by Quarkus 3.40.1.1)`. JVM-Log entsprechend `on JVM (powered by Quarkus 3.40.1.1)` mit 429 ms Index (`distribution/native-harvest.log`, `distribution/jvm-harvest.log`).
- **Parität:** 20/20 Prozesse; Harvest-Payload-Hash **`80a7d59f710dc9794fb3da72275c1653f19c0b229090165cc35abb23094642d9`**, Cross-Layer-Hash **`f2a1d6f3f12b5a3923f09aab581bcdd2aa3b8776eda53ebdaaa46dfc4ec1fd92`**; QML-Body exakt. JVM und Native liefern dieselben JSON-/Ranking-/Facet-Payloads (`distribution/summary.json`).
- **Read-only/Sicherheit:** Container als `1000730000:0`, `--read-only`, `--cap-drop=ALL`, `--security-opt=no-new-privileges`, XTF-Bindmount read-only, nur `/tmp` als 64-MiB-tmpfs. `docker diff` leer; alle `*-tmp-files.json` leer; keine `segments_*`, `write.lock` oder Lucene-Dateien. Die eigene Native-Probe bestätigt dies zusätzlich.
- **Startfehler:** malformed/wrong model/XXE/fehlende Referenz/Indexfehler beenden beide Runtimes vor Ready mit Exit 1 und der erwarteten Ursache; keine JVM-Überdeckung.
- **Messwerte (nur Beobachtung, keine Benchmark-Zusage):** Linux/arm64 JVM ready 2,4 s/Index 429 ms/RSS ~135 MiB; Linux/arm64 Native ready 0,9 s/Index 119 ms/RSS ~102 MiB; Suchmedian 8–24 ms. Die Werte unterscheiden sich von den Agent-A-Messwerten, sind aber konsistent mit der Aussage, dass der RAM-Index beim Start neu entsteht.

## 9. Findings und Beobachtungen

**Keine blockierenden Findings.** Es gibt keinen nachgewiesenen Verstoß gegen ein Muss-Kriterium, keine fehlende Pflichtprüfung und keine unzulässigen Skips.

Nicht blockierende Beobachtungen:

1. **Session-cwd:** Der CLI-Session-Datensatz zeigt noch `/Users/stefan/sources/kartenkatalog` als Startverzeichnis. Das wurde durch explizites `cd` in jeden Repository-/Build-/Test-/Probe-Prozess kompensiert; `HEAD`, Worktree-Status und alle Rohreports belegen den Review-Worktree. Kein Kandidatenfehler.
2. **Agent-A-Behauptungen zu macOS-Native/Browser:** Die P5-Implementierung nennt zusätzliche macOS-Native-/Native-Browsernachweise. Der Reviewer hat diese nicht als eigene Ausführung übernommen; die Pflicht-T4 wurde unabhängig auf Linux/arm64 und mit eigener Native-Browserprobe erbracht. Kein AC-Verstoss.
3. **Docs-Forward-Link:** `OPERATIONS.md` verweist bereits auf `reviews/P5-review.md`, das im Kandidaten noch nicht liegt. Das ist der erwartbare, nachgelagerte Docs-Punkt; der Prüfbericht wird außerhalb des Kandidaten geschrieben. Kein Produktfehler.
4. **Mobile Reset:** Der Reset-Link liegt im mobilen Sheet im scrollbaren Bereich unter den Filtergruppen; zusätzlich gibt es Chips, Such-Clear und die normale GET-Form. Die eigene Browserprobe nutzt Chips/History erfolgreich. Kein fehlender Reset, aber ein möglicher UX-Feinschliff für eine spätere Phase.
5. **`BaselineQmlTests`:** Die Assertion `body("style_server", nullValue())` ist für ein nicht im DTO existierendes Feld schwach; die eigentliche Abwesenheit von `style_server` wird durch die exakte Feldmengenprüfung in `BaselineContractTests` geschützt. Kein Vertragsverstoss.
6. **Fokus-/Outline-Darstellung:** Die expandierte Themenzeile zeigt im Screenshot einen großen Fokusrahmen; das ist eine sichtbare Fokuszustands-Ausprägung, kein Layoutfehler. Kein AC-Verstoss.
7. **Native-/Container-Messwerte:** Die frischen Messwerte weichen von den Agent-A-Zahlen ab; es werden keine Speedup-/Benchmark-Garantien behauptet.

## 10. Kumulatives Phasengate P0–P5

- **P0:** Vertrag, Hashes und Baseline unverändert; P0-Smoke frisch gegen die aktuelle App; eigene Canaries erkennen Feld- und 404-Regressionen.
- **P1:** Quarkus/JDK21, JVM-/Dev-Prozess, XTF-Validierung, API/HTML/Health, Startfehler und DB-Freiheit.
- **P2:** Lucene 10.6.0/`ByteBuffersDirectory`/`GermanAnalyzer`, Ranking, Token-AND, Facets, Index-Neubau ohne Disk, 503/DOWN.
- **P3a/P3b:** Qute-@CheckedTemplate, SODATA-nahe responsive UI, Listenexpansion, Filter/Popover/Sheet, HTMX/No-JS/Back/Reload, Chips/Reset, XSS-/Link-Sicherheit, Chromium/Firefox Desktop/Mobile.
- **P4:** Linux/aarch64-Native, JVM-/Native-Parität, read-only Container, Health, Startfehler, keine Indexdateien.
- **P5:** Cleanup und Dependency-Graph ohne Altlasten; README/Runbook; die gesamte Matrix wurde am finalen SHA erneut ausgeführt; keine Codeänderung nach dem nativen Lauf (P5-Änderungen sind Doku/Skripte/Static-Cleanup/Test-Fixture; Native wurde danach frisch gebaut und geprüft).

**Wechselwirkungen:** Keine regressiven Wechselwirkungen festgestellt. Die eigene Cross-Layer-Probe und die 20 Verteilungsprozesse decken die kritischen Kombinationen Snapshot → Suche → Filter → Facets → Qute/HTMX → Detail/QML → Native-Start ab. Der P0-Vertrag bleibt in JVM und Native identisch.

## 11. Unabhängiges Urteil

**PASS**

Der finale Kandidat **`a6ded992019cedce3d448f996518cadeeb3defbc`** erfüllt alle 28 Abnahmekriterien AC0-01…AC5-04 kumulativ. Die fälligen T1–T4-Gates wurden auf genau diesem SHA frisch ausgeführt: **62/0/0/0** Unit/HTTP, **3/0/0/0** JVM-Integration, **14/0/0/0** Browser, **20/20** echte JVM-/Native-Verteilungsprozesse, **67/67** eigene Native-Cross-Layer-Checks, **47/47** eigene Browser-Checks sowie alle Negativ-Canaries. Es gibt **0 Fehler, 0 Failures, 0 unzulässige Skips**; der Review-Worktree ist sauber und unverändert.

Mit diesem PASS ist das Projektziel der Migration erreicht; es gibt keinen Folgeabschnitt. Der Prüfbericht liegt außerhalb des Kandidaten unter `/Users/stefan/sources/kartenkatalog-P5-review-evidence/a6ded992019cedce3d448f996518cadeeb3defbc/review.md`. Ein späterer reiner Dokumentationscommit für diesen Bericht ist möglich; jede Code-/Test-/Buildänderung würde einen neuen SHA, einen neuen Review und einen erneuten Native-Check erfordern. Kein amd64-, Cluster- oder Remote-CI-Erfolg wird behauptet.
