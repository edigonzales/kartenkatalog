# Unabhängiger Review P1

## Identität und Kandidat

- **Lieferabschnitt:** P1 – Quarkus-Laufzeit, XTF-Snapshot, bestehende API, minimale Qute-Seiten.
- **AC-IDs:** AC1-01, AC1-02, AC1-03, AC1-04. **PHASENGATE:** nein.
- **OpenCode-Version (tatsächlich geprüft):** gebündelte CLI **`opencode v2.1.4`**
  (`/Applications/OpenCode.app/Contents/Resources/opencode-cli --version`, Exit 0) und
  GUI-Bundle **2.1.4** (`CFBundleShortVersionString`). Es wird **keine 2.2.x-Version** behauptet.
- **API-Provider + exakte Modellkennung aus OpenCode-Konfiguration/Modelldaten:**
  - Selektor: **`deepseek/deepseek-flash`**, Provider-ID **`deepseek`**, Anzeigename
    **DeepSeek V4.1 Flash 1017**, Status `active`.
  - Beleg: lokale OpenCode-Modelldaten (`tools.opencode.models`, Query `deepseek-flash`,
    Provider `deepseek`) → `identity/models-api.json`. Die dort ausgewiesenen Preise
    ($0.15 Input / $0.60 Output / $0.003 Cache-Read pro 1M Tokens) stimmen mit der
    offiziellen DeepSeek-Preistabelle überein.
  - Offizielle DeepSeek-Quellen: `https://api-docs.deepseek.com/quick_start/pricing/`
    nennt **`deepseek-flash` = DeepSeek-V4.1-Flash-1017**, Base URL
    **`https://api.deepseek.com`**; der Changelog-Eintrag **2026-10-17** bestätigt
    „DeepSeek V4.1 Flash has been upgraded to DeepSeek-V4.1-Flash-1017 … use
    `deepseek-flash`“. Belege: `identity/deepseek-pricing.html`,
    `identity/deepseek-pricing-excerpt.txt`, `identity/deepseek-updates.md`.
  - **Direkte Inferenz gegenüber OpenCode-Zen/Go/OpenRouter:** Der laufende
    OpenCode-Prozess (`opencode-cli serve --stdio`, PID 83151) hatte eine
    ESTABLISHED-TCP-Verbindung zu **3.173.21.63:443**; `dig +short api.deepseek.com`
    liefert genau diese IP (CloudFront). Beleg: `identity/connection-evidence.txt`.
    Die Authentisierung stammt aus dem vorhandenen gespeicherten DeepSeek-Provider;
    Credentials wurden **nicht gelesen und nicht angezeigt**. Im Auth-Store existieren
    zwar auch Provider-IDs `infomaniak` und `openrouter`, ausgewählt war aber
    `deepseek`; es wurde kein OpenRouter-/Zen-Endpunkt für die Inferenz verwendet.
  - Die Identität wurde **nicht** aus einer Modellselbstauskunft abgeleitet.
    `execution-identity.json` wurde nur als Startmetadaten gelesen und **nicht** als Identitätsbeleg
    verwendet; maßgeblich sind OpenCode-Modelldaten, die vorhandene Provider-Konfiguration/Auth-Provider-ID
    und die offiziellen DeepSeek-Seiten.
- **BASE_SHA:** `49cf211d2b1c223cf05a9a4e2118233c180179ed`
- **CANDIDATE_SHA; `git rev-parse HEAD`:** `2014f6035638730739820d391a61423a1c967bf3`
- **Review-Worktree:** `/Users/stefan/sources/kartenkatalog-review-P1` (detached HEAD)
  - `git status --porcelain=v1` vor und nach dem Review: **leer** (`logs/git-status-before.txt`,
    `logs/final-git.txt`).
  - Keine Commits, kein Push, keine Änderung an getrackten Dateien, Tests, Daten oder
    Kriterien. Build-Ausgaben unter `build/` sind erlaubt und nicht getrackt.
- **Kandidatendiff:** 48 Dateien, +736/−1095 Zeilen; vollständig gelesen
  (`candidate.diff`). Enthalten: Quarkus-Build, Parser/Snapshot/Store, REST-/Qute-Portierung,
  Entfernung von Spring/JTE/JDBC/JDBC-Tests/Docker-Tasks, neue Parser-Tests und
  `scripts/p1-process-contract.py`.
- **Prüfumgebung:** macOS 15.8.1/arm64, Temurin **OpenJDK 21.0.7+6**, Gradle **9.8.2**,
  Quarkus **3.40.1.1**. GraalVM/Native und Browser sind für P1 **nicht fällig**
  (Native/Linux = P4, Browser/UI-Gate = P3a/P3b).
- **Letzter unabhängiger PASS-SHA aus voriger Lieferung:** `cf43a66599ea38d63286ee5d95f093ad89bb08a6` (P0).
- **Externes Evidenzverzeichnis:** `/Users/stefan/sources/kartenkatalog-P1-review-evidence/2014f6035638730739820d391a61423a1c967bf3`

## Fachliche Kriterien

| AC-ID | Erfüllt / verletzt / nicht prüfbar | Eigener Code-/HTTP-/Test-/Prozess-Beleg | Finding |
|---|---|---|---|
| **AC1-01** | **Erfüllt** | `build.gradle`: Quarkus-Plugin und BOM fest **3.40.1.1**, Java-Toolchain **21**; Wrapper **9.8.2**. Frisch `./gradlew clean test quarkusBuild` → **Exit 0, 41 Tests / 0 Failures / 0 Errors / 0 Skips**. `./gradlew dependencies --configuration runtimeClasspath` → **Exit 0**; Runtime-Graph enthält nur Quarkus 3.40.1.1, Jackson/Vert.x/SmallRye, Qute und `org.jsoup:jsoup:1.23.2`; **kein** Spring, JDBC, PostgreSQL, Hikari, JTE, Hibernate, Lucene (`logs/forbidden-deps-check.txt`). `quarkusBuild` erzeugt `build/quarkus-app/quarkus-run.jar`; eigener JVM-Prozess wird in **0.896 s** ready und beantwortet HTTP. `scripts/p1-process-contract.py --dev` startet echtes `quarkusDev`, wird in **4.608 s** ready, HTTP-Smoke Exit 0. | Kein Finding. |
| **AC1-02** | **Erfüllt** | Reale Harvest-XTF bleibt unverändert: SHA-256 `de40f025368ae6fc70f5858ab42592488bca837c6942cb6ff8759e6c8803de86`; **169 Themen / 886 Ebenen**; keine QML/Keywords/Synonyme/Organisationen erfunden. Eigener Parser-/XXE-/Immutability-Probe: **49 Checks, Exit 0** (`counterprobe/parser-probe.log`). Geprüft: direkte XML-Felder (nested fremdes `org_name` wird ignoriert), `Thema_R`-Referenz schlägt redundantes `theme_ident`, `ebenen_count`, unveränderliche Listen/Maps inkl. verschachtelter Themenlisten, UUID/TID/Identifier-Duplikate (auch UUID case-insensitiv), fehlende/dangling Referenzen, doppelte/nur verschachtelte Pflichtfelder, Transparenz 0/100 vs. 101/−1, `service_download` true/false vs. ungültig, fremdes Modell. XXE: externes Entity auf echte Sentinel-Datei wird **abgelehnt**, Sentinel-Inhalt erscheint nicht in Fehlermeldung und Datei bleibt unverändert; internes DTD-Entity ebenfalls abgelehnt. JVM-Start mit eigener defekter XTF: **Exit 1**, Log enthält `Cannot load catalog XTF`, kein Ready-Endpunkt. Kein DB-/Mock-Fallback im Produktionspfad. | Kein Finding. |
| **AC1-03** | **Erfüllt** | Eigener Real-JVM-HTTP-Probe: **106 Checks, Exit 0** (`counterprobe/process-probe.log`). Alle fünf API-Formen: `/api/v1/catalog`, `/api/v1/themen`, `/api/v1/themen/{identifier}/ebenen`, `/api/v1/ebenen`, `/api/v1/ebenen/{id}`; 169/886, `layerCount`-Summe 886, Kataloggruppierung konsistent. P0-Baseline-Layer **exakt gleich**; alle 886 Layer haben den exakten Feldumfang, `keywords`/`synonyms` nie `null`, optionale Skalare sind in realen Fällen `null` (u. a. `organization`), `id == qwcLayerName == ident_part`, `sourceId` ist separate UUID und ungleich `qwcLayerName`, `themeIdentifier`/`themeTitle` aus der echten Referenz. Suche: Gross-/Kleinschreibung, Leerzeichen, Akzent-Normalisierung (eigener akzentuierter Titel), 200-Zeichen-Grenze, kein Treffer `200 []`, 201 Zeichen `400`. Unbekanntes Thema/Layer/QML `404` mit `application/problem+json`, `status/title/detail/instance`, kein Stacktrace. | Kein Finding. |
| **AC1-04** | **Erfüllt** | Eigener Security-/HTML-/QML-Probe: **32 Checks, Exit 0** (`counterprobe/security-probe.log`). `/thema` 200 HTML mit echten Themen/Links; `/themen/{id}/ebenen` 200 vollständig, mit `HX-Request: true` echtes Fragment ohne `<html>`/`<!doctype>`; `/ebene/{id}` 200 mit sicherem `https://geo.so.ch/map?l=<ident_part>` und 85-%-Transparenz; QML nur bei vorhandenem `style_server` als `application/xml`-Attachment mit RFC-5987-`filename*` und exaktem Body; fehlendes QML `404` problem+json; `/ping` `200 text/plain` „kartenkatalog“; `/q/health/live` und `/q/health/ready` UP. Eigene XSS-XTF mit `<script>`, `onerror`, `javascript:` und QML: Qute escaped Titel, Jsoup entfernt ausführbare Tags/Attribute/URLs, `RawString` nur für sanierte Beschreibung; API-`descriptionHtml` sanitiert, Roh-QML nicht im DTO. **Qute `@CheckedTemplate`-Canary** in externer Wegwerfkopie: unverändert Build Exit 0; mit `{product.notARealField}` Build **Exit 1** und `Property/method [notARealField] not found on class [CatalogPages$DetailVm]`. Readiness ist nur mit vollständig konstruiertem Snapshot erreichbar; bei defekter XTF bricht der Start ab, kein Ready. `/` liefert bewusst **302 → `/thema`** (alter Alias; siehe OBS-01), `/thema` liefert das minimale HTML. | Kein Finding. |

## Eigene neue Gegenprobe

### Gegenprobe 1: Eigene XTF-Fixture für direkte Felder, Referenzen und Immutability (nicht aus Codex-Tests übernommen)

- **Warum kritisch:** Die P1-Parserlogik wurde von DOM-`getElementsByTagNameNS` (rekursiv) auf direkte Kind-Elemente umgestellt. Eine reine `169/886`-Zählung würde nicht erkennen, wenn Felder aus verschachtelten Fremdbäumen eingesammelt oder Themen über das redundante `theme_ident` statt über `Thema_R` aufgelöst würden.
- **Eigene Eingabe:** `counterprobe/fixtures/probe-catalog.xml` – 2 Themen, 3 Ebenen; Ebene 2 enthält ein fremdes `<k:foreign><k:org_name>NestedEvil</k:org_name></k:foreign>` neben dem direkten `DirectOrgTwo`; Ebene 3 hat `theme_ident=theme-one`, referenziert aber per `Thema_R` auf `t2` (Theme `theme-two`); Optionale Felder mit Leerzeichen; Transparenz 85; `service_download=true`.
- **Hypothese/Erwartung:** Nur direkte Felder werden gelesen; `Thema_R` gewinnt; Themenunterlisten sind unveränderlich; DTO verwendet die aufgelöste Theme-Kennung; `qwcLayerName` ist `ident_part`, `sourceId` die UUID.
- **Kommando/Exit:**
  ```sh
  javac -cp "build/classes/java/main:build/quarkus-app/lib/main/org.jsoup.jsoup-1.23.2.jar" \
    -d "$EV/counterprobe/classes" "$EV/counterprobe/ParserProbe.java"
  java -cp "build/classes/java/main:build/quarkus-app/lib/main/org.jsoup.jsoup-1.23.2.jar:$EV/counterprobe/classes" \
    ParserProbe "$EV/counterprobe/fixtures/probe-catalog.xml" \
    "data/kartenkatalog-harvest-ohne-qml.xtf" "$EV/counterprobe/xxe-sentinel.txt"
  # Exit 0
  ```
- **Tatsächliche Ausgabe (Auszug):**
  ```text
  ok 21: only the direct org_name child is read, nested foreign org_name ignored
  ok 26: redundant theme_ident differs from authoritative Thema_R in fixture
  ok 27: DTO themeIdentifier follows Thema_R, not redundant theme_ident
  ok 28: DTO qwcLayerName is published ident_part
  ok 29: DTO sourceId is separate source UUID
  ok 31: own fixture nested list unmodifiable
  ok 33: XXE rejection message does not leak the secret file content
  ok 34: XXE sentinel file was not modified
  ok 46: rejected foreign model header -> Invalid catalog XTF: Unexpected publication model
  AGENT-B PARSER/XXE/IMMUTABILITY PROBE PASS (49 checks)
  ```
- **XXE-Details:** Externes Entity auf `counterprobe/xxe-sentinel.txt` (`XXE-SENTINEL-SECRET-9f3a2b`) wurde mit
  `DOCTYPE is disallowed when the feature "...disallow-doctype-decl" set to true` abgelehnt; der Sentinel-Inhalt
  erschien nicht in der Fehlermeldung, die Datei blieb unverändert. Zusätzlich wurden 9 weitere selbst erfundene
  Negativfälle ausgeführt (u. a. UUID-Duplikat mit anderer Gross-/Kleinschreibung, doppelte TID, fehlende/dangling
  Referenz, doppeltes Feld, nur verschachteltes Pflichtfeld, Grenzwerte).
- **Schlussfolgerung:** Die P1-Parser-/Snapshot-Kernverträge sind nicht nur „169/886 grün“, sondern semantisch korrekt
  und sicher.

### Gegenprobe 2: Eigener Real-JVM-API-/Startfehler-Probe (unabhängig vom Codex-Skript)

- **Warum kritisch:** Die Codex-Tests und das Codex-Prozessskript könnten dieselben Annahmen teilen. Der eigene Probe startet das gebaute JAR mit einer **eigenen Kopie** der Harvest-XTF, prüft alle API-Formen und danach das Verhalten bei Quellmutation und bei **neuen** defekten XTFs.
- **Eigene Eingabe:** Kopie von `data/kartenkatalog-harvest-ohne-qml.xtf` in ein temporäres Verzeichnis; eigene Defektfälle `own-duplicate-uuid-case`, `own-duplicate-tid`, `own-dangling-reference`, `own-truncated-xml`, `own-xxe-doctype`, `own-wrong-namespace`, Verzeichnis als `file:`-Quelle und `https://…` als Quelle.
- **Kommando/Exit:**
  ```sh
  python3 "$EV/counterprobe/independent_process_probe.py" \
    /Users/stefan/sources/kartenkatalog-review-P1 "$EV"
  # Exit 0, 106 Checks
  ```
- **Tatsächliche Ausgabe (Auszug):**
  ```text
  ok 1 real JVM becomes ready with copied harvest XTF
  ok 8 all 886 layer payloads have exact contract field set
  ok 11 qwcLayerName equals published id for all layers
  ok 12 sourceId is distinct source UUID for all layers
  ok 16 all layers resolve to a theme and its title
  ok 55 accent-insensitive search finds own selected accented title
  ok 69 / redirects to /thema
  ok 73 HTMX response is a fragment, not a full page
  ok 79 running snapshot remains complete after source is mutated
  ok 82 startup-failure own-duplicate-uuid-case: non-zero exit
  ok 97 startup-failure own-wrong-namespace: non-zero exit
  ok 106 candidate harvest data was not modified
  AGENT-B PROCESS/API PROBE PASS (106 checks)
  ```
- **Schlussfolgerung:** API, Fehlersemantik, HTML, Health und Startup-Abbruch sind am echten Prozess bestätigt;
  die Snapshot-Mutation nach dem Start beeinflusst die laufende Antwort nicht, ein frischer Start mit defekter Datei
  scheitert mit Exit ≠ 0 und ohne Ready-Endpunkt.

### Gegenprobe 3: Eigene XSS-/Qute-/QML-Probe

- **Warum kritisch:** Die vorhandenen QML-Tests decken nur `alert(1)` in einer Beschreibung ab. Eine eigene XTF mit
  `<script>`, `onerror`, `javascript:` in Titel, Beschreibung, `further_info_url`, `org_name`, `display_text` sowie
  einer QML-Datei prüft Qute-Escaping und Jsoup-Sanitization in echten HTML-/HTMX-/QML-Antworten.
- **Eigene Eingabe:** `counterprobe/fixtures/security-catalog.xml`; Theme-Titel
  `<script>alert('theme-title')</script>Sicherheitsthema`, Layer-Titel
  `<img src=x onerror=alert('layer-title')>Sicherheitslayer`, Beschreibung mit Script/Img/`javascript:`-Link,
  `further_info_url=javascript:…`, `org_name=<script>…`, Transparenz 85, QML-`style_server`.
- **Kommando/Exit:**
  ```sh
  python3 "$EV/counterprobe/security_html_probe.py" \
    /Users/stefan/sources/kartenkatalog-review-P1 "$EV" \
    "$EV/counterprobe/fixtures/security-catalog.xml"
  # Exit 0, 32 Checks
  ```
- **Tatsächliche Ausgabe (Auszug):**
  ```text
  ok 3 theme title is Qute-escaped
  ok 9 full theme page has no executable tags/event attributes/javascript URLs
  ok 12 HTMX fragment has no executable tags/event attributes/javascript URLs
  ok 16 detail page has no executable tags/event attributes/javascript URLs
  ok 17 map target encodes published id and 85 percent transparency
  ok 22 API descriptionHtml sanitized
  ok 27 QML body exact
  ok 28 QML attachment filename is RFC5987 and header-safe
  ok 29 missing QML is 404 problem+json
  ok 31 ready health with security fixture
  AGENT-B SECURITY/HTML/QML PROBE PASS (32 checks)
  ```
- **Schlussfolgerung:** Kein ausführbares HTML aus den getesteten XTF-Feldern; QML wird nur als XML-Attachment
  ausgeliefert; Qute escaped alle nicht explizit sanierten Werte.

### Gegenprobe 4: Qute-`@CheckedTemplate`-Canary in externer Wegwerfkopie

- **Warum kritisch:** AC1-04 verlangt, dass ein nicht existierender Ausdruck den **Build** scheitern lässt, nicht erst
  zur Laufzeit leer rendert. Nur ein wirklicher Build-Fehlschlag belegt die compile-time-Validierung.
- **Eigene Mutation:** Kopie des Kandidaten nach
  `/private/var/folders/rp/xw19bm096bv14zzhq2ybnttm0000gn/T/qute-canary.hHD560` (ohne `.git`/`build`);
  in `src/main/resources/templates/detail.html` wurde zusätzlich `<p>{product.notARealField}</p>` eingefügt.
- **Kommandos/Exit:**
  ```sh
  cd "$THROWAWAY_COPY"
  ./gradlew clean quarkusBuild --console=plain   # unverändert: Exit 0
  # Mutation einfügen
  ./gradlew clean quarkusBuild --console=plain   # mit Mutation: Exit 1
  ```
- **Tatsächliche Ausgabe (Auszug):**
  ```text
  detail.html:5:36 - {product.notARealField}: Property/method [notARealField] not found
  on class [ch.so.agi.meta.web.CatalogPages$DetailVm] nor handled by an extension method
  BUILD FAILED in 2s
  ```
- **Schlussfolgerung:** `@CheckedTemplate` ist wirksam; der Kandidat wurde dabei nicht verändert.

## Pflichtprüfungen

Alle Kommandos wurden im Review-Worktree `/Users/stefan/sources/kartenkatalog-review-P1` ausgeführt.
`EV` = `/Users/stefan/sources/kartenkatalog-P1-review-evidence/2014f6035638730739820d391a61423a1c967bf3`.

| Testfamilie | Ausgeführter Befehl | Exit | Tests/Failures/Errors/Skips | Rohreport | SHA-bezogen |
|---|---|---:|---|---|---|
| JUnit/Quarkus + Build (frisch) | `./gradlew clean test quarkusBuild --console=plain` | **0** | **41 / 0 / 0 / 0** | `EV/logs/clean-test-quarkusBuild.log`, `EV/junit/TEST-*.xml`, `EV/html-report/` | Ja, `2014f603…` |
| Runtime-Dependency-Graph | `./gradlew dependencies --configuration runtimeClasspath --console=plain` | **0** | Spring/JDBC/Postgres/Hikari/JTE/Hibernate/Lucene **abwesend** | `EV/logs/runtimeClasspath-dependencies.log`, `EV/logs/forbidden-deps-check.txt` | Ja, `2014f603…` |
| Echter JVM-Prozess + P0-HTTP + Health + Snapshot-Lifetime | `python3 scripts/p1-process-contract.py` | **0** | JVM ready **0.896 s**, HTTP Exit 0; 8 Startfehler je **Exit 1**, nie ready | `EV/logs/p1-process-contract.log`, `EV/process/p1-reports-final/{jvm.log,jvm-http.log,process-summary.json,*.log}` | Ja, `2014f603…` |
| Echter `quarkusDev` | `python3 scripts/p1-process-contract.py --dev` | **0** | Dev ready **4.608 s**, HTTP Exit 0 | `EV/logs/p1-process-contract-dev-rerun.log`, `EV/process/p1-reports-final/{dev.log,dev-http.log,dev-summary.json}` | Ja, `2014f603…` |
| Eigener Parser-/XXE-/Immutability-Probe | `javac … ParserProbe.java`; `java … ParserProbe …` | **0** | **49 Checks**, keine Failures | `EV/counterprobe/parser-probe.log`, `.exit.txt`, `.java`, `fixtures/probe-catalog.xml` | Ja, gegen Candidate-Klassen |
| Eigener Real-JVM-API-/Startfehler-Probe | `python3 "$EV/counterprobe/independent_process_probe.py" …` | **0** | **106 Checks**, keine Failures | `EV/counterprobe/process-probe.log`, `.exit.txt`, `EV/process/own-*.log`, `EV/process/summary.json` | Ja, gegen Candidate-JAR |
| Eigener XSS-/Qute-/QML-Probe | `python3 "$EV/counterprobe/security_html_probe.py" …` | **0** | **32 Checks**, keine Failures | `EV/counterprobe/security-probe.log`, `.exit.txt`, `EV/http/security-*.html` | Ja, gegen Candidate-JAR |
| Qute-Canary Basis | `./gradlew clean quarkusBuild` in Wegwerfkopie | **0** | Build erfolgreich | `EV/counterprobe/qute-canary-baseline.log` | Wegwerfkopie von `2014f603…` |
| Qute-Canary Mutation | dito mit `{product.notARealField}` | **1 (erwartet)** | Property not found, Build FAILED | `EV/counterprobe/qute-canary-mutated.log` | Wegwerfkopie, nicht Kandidat |
| Statisches CSS/HTML | eigener JVM-Start, `GET /catalog.css`, `GET /thema` | **0** | CSS 200 `text/css`; `/thema` 200; kein externer `http(s)`-Link im HTML | `EV/logs/css-check.txt`, `EV/http/security-theme-full.html` | Ja, `2014f603…` |
| Fixture-Integrität | `shasum -a 256 data/kartenkatalog-harvest-ohne-qml.xtf` | **0** | `de40f025…` (unverändert) | `EV/logs/final-git.txt` | Ja, `2014f603…` |
| Diff-Hygiene | `git diff --check 49cf211… 2014f603…` | **0** | keine Whitespace-Fehler | `EV/logs/final-git.txt` | Ja, `2014f603…` |

- **Fällige, aber nicht ausgeführte Checks und genaue Ursache:** **Keine P1-Pflichtprüfung** wurde ausgelassen.
  Browser-/Playwright-Gates (T3) sind erst P3a/P3b, Lucene/Facets erst P2, Native/Linux/Container erst P4.
  Diese wurden **nicht** als P1-Lücke gewertet und nicht als Erfolg behauptet.
- **Kumulatives Phasengate:** nicht anwendbar (P1, `PHASENGATE: nein`). Der P0-Vertrag wurde dennoch
  frisch gegen die laufende JVM geprüft (Baseline-Layer exakt, 169/886, alle fünf Formen, Fehlerfälle).
- **Kandidat unverändert:** `HEAD` am Ende weiterhin `2014f6035638730739820d391a61423a1c967bf3`;
  `git status --porcelain=v1` leer; keine Commits, kein Push; keine Änderung an getrackten Dateien,
  Tests, Daten, Builddateien oder Kriterien. Eigene Skripte/Fixtures liegen ausschließlich unter `EV`;
  die Qute-Mutation ausschließlich in der externen Wegwerfkopie. `build/` enthält nur erlaubte Artefakte.

## Findings

- **FINDING-01:** **Keine offene Muss-Abweichung.** Alle vier P1-Kriterien sind nach unabhängiger Quell-,
  Test-, HTTP-, Prozess- und Security-Prüfung erfüllt; die Pflichtprüfungen und alle eigenen Gegenproben sind
  erfolgreich.

**Beobachtungen ohne Abnahmewirkung (kein Finding):**

- **OBS-01:** `/` liefert **302 → `/thema`**, nicht selbst 200-HTML. Das ist der alte Alias und wird vom
  P1-Wortlaut („minimal brauchbares HTML/Antworten“) sowie vom bisherigen P0-Vertrag getragen; die vollständige
  Startseite ist ausdrücklich P3a. Kein P1-Verstoss, aber bei P3a zu ersetzen/zu prüfen.
- **OBS-02:** CORS ist nicht freigeschaltet. Der P0-Bestand hatte ebenfalls keine CORS-Konfiguration; README
  dokumentiert same-origin/Reverse-Proxy. Die P1-ACs fordern keine CORS-Freigabe. Bei der QWC2-Dev-Anbindung
  (P3b/P4) gezielt bewerten, nicht pauschal öffnen.
- **OBS-03:** Der `quarkusDev`-Kindprozess war unmittelbar nach Skriptende für ca. **1 s** noch sichtbar und
  beendete sich dann selbst; danach kein Listener und kein Prozess. Das ist eine Shutdown-Race, kein dauerhafter
  Orphan. Prozesshygiene bei Bedarf nachschärfen.
- **OBS-04:** `furtherInfoUrl` wird im JSON weiterhin roh durchgereicht (P0-Vertrag) und in P1 **nicht** als Link
  gerendert. Sobald P3a externe Links rendert, muss das Schema (insbesondere `javascript:`) vorher gefiltert werden;
  aktuell entsteht dadurch kein XSS.
- **OBS-05:** Der `Content-Disposition`-Header enthält nur `filename*=UTF-8''…`, keinen ASCII-`filename=`-Fallback.
  Moderne Browser und der P0-Test akzeptieren das; bei Bedarf für alte Clients ergänzen.
- **OBS-06:** Der Parser ist bewusst strenger als der alte DOM-Code (genau ein `ili:model`, UUID-Format,
  case-insensitive UUID-Eindeutigkeit, strikte `true`/`false`-Werte). Die reale Harvest-Datei besteht; für künftige
  XTF-Varianten ist die Strictness dokumentiert und kein P1-Fehler.

## Unabhängiges Urteil

**PASS**

- **Begründung:** Der Kandidat `2014f6035638730739820d391a61423a1c967bf3` erfüllt AC1-01, AC1-02, AC1-03 und
  AC1-04. Der frische Build/Testlauf ergibt **41/0/0/0**; der Runtime-Graph enthält **kein** Spring/JDBC/Postgres/
  Hikari/JTE und (P1-korrekt) kein Lucene. Echter JVM-Prozess und `quarkusDev` starten und liefern die P0-API-,
  HTML-, QML-, Fehler- und Health-Verträge. Der eigene 49-Check-Parser-/XXE-Probe, der eigene 106-Check-JVM-Probe
  und der eigene 32-Check-XSS-/QML-Probe bestehen; die Qute-Canary-Mutation lässt den Build erwartungsgemäß
  scheitern. Die Snapshot-Immutability und der Startabbruch bei defekten/fremden/fehlenden Quellen sind real belegt.
  Es gibt keine offene Muss-Abweichung.
- **Nächster freigebbarer Lieferabschnitt:** **P2** – erst nach diesem PASS. Der aktuelle Auftrag endet jedoch
  **vor P2**; P2 wurde in diesem Review **nicht begonnen** und keine P2-Funktion (Lucene/Facets) bewertet.
- **Geprüfter SHA:** `2014f6035638730739820d391a61423a1c967bf3`

### Rohbelege (absolut)

- Review: `/Users/stefan/sources/kartenkatalog-P1-review-evidence/2014f6035638730739820d391a61423a1c967bf3/review.md`
- Kandidatendiff: `…/candidate.diff`
- Build/Tests: `…/logs/clean-test-quarkusBuild.log`, `…/logs/clean-test-quarkusBuild.exit.txt`,
  `…/junit/TEST-*.xml`, `…/html-report/`
- Runtime-Graph: `…/logs/runtimeClasspath-dependencies.log`, `…/logs/runtimeClasspath-dependencies.exit.txt`,
  `…/logs/forbidden-deps-check.txt`
- Prozess/Dev: `…/logs/p1-process-contract.log`, `…/logs/p1-process-contract.exit.txt`,
  `…/logs/p1-process-contract-dev-rerun.log`, `…/process/p1-reports-final/`
- Eigene Gegenproben: `…/counterprobe/parser-probe.log`, `…/counterprobe/process-probe.log`,
  `…/counterprobe/security-probe.log`, `…/counterprobe/*.exit.txt`,
  `…/counterprobe/fixtures/{probe-catalog.xml,security-catalog.xml}`
- Qute-Canary: `…/counterprobe/qute-canary-baseline.log`, `…/counterprobe/qute-canary-mutated.log`,
  `…/counterprobe/qute-canary-*.exit.txt`
- Identität: `…/identity/models-api.json`, `…/identity/deepseek-pricing.html`,
  `…/identity/deepseek-pricing-excerpt.txt`, `…/identity/deepseek-updates.md`,
  `…/identity/opencode-config-redacted.txt`, `…/identity/connection-evidence.txt`
- Git/Fixture: `…/logs/git-status-before.txt`, `…/logs/final-git.txt`
