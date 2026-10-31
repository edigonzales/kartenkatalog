# Unabhängiger Review P3a

## Identität und Kandidat

- **Lieferabschnitt:** P3a – Qute-/SODATA-Design: Startseite, Liste, Aufklappen, Details.
- **AC-IDs:** AC3A-01, AC3A-02, AC3A-03, AC3A-04. **PHASENGATE:** nein (kumulatives UI-Gate erst P3b).
- **Rolle:** Agent B, unabhängiger Prüfer. Keine Produktimplementierung, keine Folgephase, keine Änderung des Kandidaten.
- **OpenCode-Version (tatsächlich geprüft):** gebündelte CLI **`opencode v2.1.4`**
  (`/Applications/OpenCode.app/Contents/Resources/opencode-cli --version`, Exit 0). Es wird **keine 2.2.x-Version** behauptet.
- **API-Provider + exakte Modellkennung (aus tatsächlichen OpenCode-Sitzungs-/Modelldaten und Konfiguration, nicht aus Modellselbstauskunft):**
  - **Session-ID `ses_e6c7c0516ffes4UOI8kmQ6Cszb`**, Titel „P3a independent visual review 9dfde0ff“, Verzeichnis
    `/Users/stefan/sources/kartenkatalog-review-P3a`, Session-Version **2.1.4**. In der lokalen OpenCode-Sessionablage
    (`~/.local/share/opencode/opencode.db`) tragen **alle Assistant-Nachrichten** (bei der Erfassung 84) exakt
    `providerID=deepseek`, `modelID=deepseek-flash`; die eine User-Nachricht hat erwartungsgemäss keine Modelldaten
    (`identity/session-model-metadata.{json,txt}`).
  - **Lokale OpenCode-Modelldaten** (`tools.opencode.models`, Provider `deepseek`): Selektor
    **`deepseek/deepseek-flash`**, Anzeigename **„DeepSeek V4.1 Flash 1017“**, Status `active`
    (`identity/models-api.json`).
  - **Direkter DeepSeek-Endpunkt:** Die gebündelte CLI definiert für den Provider `deepseek` die Base-URL
    **`https://api.deepseek.com/v1`** (`identity/provider-connection.txt`). Während des Laufs bestand im
    CLI-Prozess (PID 3601) eine TCP-Verbindung zu **`3.173.21.63:443`**; `dig +short api.deepseek.com` liefert
    genau diese CloudFront-IP (`identity/connection-evidence.txt`). Es war **kein** Zen-/Go-/OpenRouter-Endpunkt
    als Provider konfiguriert; die Session-/Provider-Metadaten weisen ausschliesslich `deepseek` aus. Andere
    Verbindungen des OpenCode-Prozesses (u. a. lokales Ollama/Tooling) ändern die Provider-/Modellwahl nicht.
  - **Offizielle DeepSeek-Quelle:** `https://api-docs.deepseek.com/quick_start/pricing/` (abgerufen 31.10.2026,
    archiviert als `identity/deepseek-pricing.html` und Auszug `identity/deepseek-pricing-excerpt.txt`) nennt
    **`deepseek-flash` = `DeepSeek-V4.1-Flash-1017`**, Basis-URL `https://api.deepseek.com`. Das deckt sich mit
    den lokalen OpenCode-Modelldaten.
  - **Credentials:** Es wurde **keine** neue Installation, keine Key-Umgebungsvariable und **keine** Credential-Datei
    gelesen oder ausgegeben. `execution-identity.json` wurde nur als Startmetadatum zur Kenntnis genommen und
    **nicht** als Identitätsbeleg verwendet.
- **BASE_SHA:** `047da2ce91a29bfabf0ba7f6138154ca3bd959d9`
- **CANDIDATE_SHA; `git rev-parse HEAD`:** `9dfde0ffb2ad6f94bb71bdd278beb174eb3f9ee6`
- **Review-Worktree:** `/Users/stefan/sources/kartenkatalog-review-P3a` (detached HEAD auf exakt dem Kandidaten).
  - `git status --porcelain=v1` vor und nach dem Review: **leer** (`logs/00-git-before.txt`, `logs/99-git-after.txt`).
  - Keine Commits/Pushes; `git log 9dfde0ff..HEAD` = **0** Commits. Keine Änderung an getrackten Quellen, Tests,
    Daten oder Build-/Kriteriendateien. Eigene Skripte/Fixtures/Mutationen liegen ausschliesslich im externen
    Evidenzverzeichnis bzw. in einer externen Wegwerfkopie. `build/` enthält nur erlaubte Build-Artefakte.
- **Kandidatendiff:** 38 Dateien, **+1538/−93**; vollständig als `candidate.diff` (1941 Zeilen) archiviert und
  risikobasiert gelesen (Qute-Templates, ViewModels, URL-Factory, CSS/JS, Vendoring, Tests, CI, Doku).
- **Prüfumgebung:** macOS 15.8.1/arm64, Temurin **OpenJDK 21.0.7+6**, Gradle **9.8.2**, Quarkus **3.40.1.1**,
  Lucene **10.6.0**, Playwright **1.60.0**, echtes Chromium. Native/Linux und Firefox sind für P3a **nicht**
  fällig (Native/Linux = P4; Firefox-Gesamtgate = P3b) und werden hier nicht als Kriterium behauptet.
- **Letzter unabhängiger PASS-SHA aus voriger Lieferung:** `a9dde956b4937916cec862c6c672cf26dd30636f` (P2).
- **Externes Evidenzverzeichnis:**
  `/Users/stefan/sources/kartenkatalog-P3a-review-evidence/9dfde0ffb2ad6f94bb71bdd278beb174eb3f9ee6`

## Fachliche Kriterien

| AC-ID | Erfüllt / verletzt / nicht prüfbar | Eigener Code-/HTTP-/Test-/Browser-Beleg | Finding |
|---|---|---|---|
| **AC3A-01** | **Erfüllt** | `CatalogPages.Templates` nutzt `@CheckedTemplate(basePath="")` mit typisierten `CatalogPage`, `ThemePage`, `List<LayerRow>`, `Detail`; `CatalogPageVmFactory` bereitet die ViewModels auf. **Eigene Build-Canary** in externer Wegwerfkopie: unveränderte Kopie `./gradlew quarkusBuild` **Exit 0**; danach `{product.missingFieldForCanary}` in `detail.html` → **Exit 1** mit `QuteProcessor#processTemplateErrors`: `Property/method [missingFieldForCanary] not found on class [ch.so.agi.meta.web.CatalogViewModels$Detail]` (`logs/08-qute-canary-baseline.log`, `logs/09-qute-canary-mutated.log`). Geteiltes `layout.html` mit lokalem `so-header`/`so-breadcrumb`, Skip-Link auf `#main-content`, Footer und Provenienzhinweis. Assets same-origin unter `/vendor/…`, `/fonts.css`, `/catalog.css`, `/kantons-logo.svg`; Browser-Test und eigene Chromium-Probe protokollieren **keinen** externen Runtime-Request (`probe/browser-console.log`). Lizenzen liegen lokal: `vendor/SODATA-LICENSE`, `vendor/htmx/2.0.11/LICENSE`, `vendor/so-web-components/0.1.10/LICENSE` + Herkunfts-README; `catalog.css` trägt Quellen-/MIT-Hinweis. Runtime-Graph ohne JTE/Spring/JDBC/Postgres/Hikari. | Kein Finding. |
| **AC3A-02** | **Erfüllt** | `/` liefert **200 `text/html`** (Vollseite, kein alter 302); `/thema` ist **200-Alias** derselben Seite. Reale Harvest: **169 native `details.theme-row`**, initial **0 `.layer-row`**, Anzeige **886 Kartenebenen in 169 Themen**; die erste Zeile „Abbaustellen“ zeigt „1 Ebene“, Aufklappen per Tastatur/Enter lädt genau **1** Unterzeile, `aria-expanded=true`, `aria-controls` vorhanden; erneutes Schliessen/Öffnen erzeugt **keinen** zweiten Fragmentrequest (`hx-trigger="toggle once"`). No-JS: Fallback-Header/-Breadcrumb sichtbar, natives `details` funktioniert, `fallback-link` führt auf `/themen/{id}/ebenen` mit echten Ebenen; GET-Suche funktioniert. Fragment/Full-Page: `HX-Request: true`/`TRUE` → Fragment ohne `<html>`; `false`/fehlend → Vollseite; `Vary` enthält `HX-Request` (neben `Accept`). Unbekanntes Thema/Layer → **404 `application/problem+json`**. Browser: `CatalogBrowserTests` 3/3 grün; eigene Chromium-Probe 35/35. | Kein Finding. |
| **AC3A-03** | **Erfüllt** | Eigene XTF-Fixture (`probe/p3a-probe.xml`) mit `<script>`/`onerror` in Titel, Beschreibung, Organisation, `javascript:`-`further_info_url`, fehlenden Feldern, `transparency=85`, echtem QML und minimaler Ebene. Ergebnis über echtes HTTP und Chromium: Titel/Organisation/Theme-Beschreibung werden **escaped**; `<script>`, `onerror`, `javascript:`, `img src=x` werden **nicht ausführbar** ausgegeben; nur die Jsoup-sanierte Beschreibung wird als `RawString` gerendert (`<strong>fett</strong>` bleibt, `<script>`/`onerror`/`javascript:` verschwinden); QML-Link nur bei echtem `style_server`, `/ebene/ch.probe.alpha/style` → 200 XML-Attachment mit korrekt kodiertem Dateinamen, minimale Ebene → 404; Kartenlink exakt `https://geo.so.ch/map?l=ch.probe.alpha%5B85%5D`; absolute HTTP(S)-`further_info_url` bleibt, `javascript:` wird verworfen; minimale Ebene zeigt **keine** leeren Organisation-/Verfügbarkeits-/Download-/QML-Abschnitte. Reale Harvest enthält keine `org_name`/`display_text`/`service_download`-Werte; es werden keine erfunden. | Kein Finding. |
| **AC3A-04** | **Erfüllt** | Frischer Pflichtlauf (`./gradlew clean test quarkusBuild playwrightTest`) am Kandidaten: **59 Unit/HTTP-Tests 0 Failures/0 Errors/0 Skips** und **3 echte Chromium-Tests 0/0/0** (finaler Lauf Exit 0, 1m 1s). Chromium startet ein selbst gebautes `build/quarkus-app/quarkus-run.jar` im separaten Prozess; Log: 169 Themen/886 Ebenen, Quarkus 3.40.1.1. Screenshots frisch in `build/reports/p3a/screenshots/` (Desktop 1440×900: Start, Expansion, Detail; Mobile 390×844: Start, Detail) und extern kopiert; zusätzlich eigener Leerzustand-Screenshot. Visueller Abgleich mit der echten SODATA-Referenz (`reference/sodata-desktop.png`) wurde an den tatsächlichen Bildern vorgenommen (Header, Breadcrumb, Suche, Filterband, Trefferzahl, Listentypografie, Plus/Minus-Expansion, Leerzustand, Detailseite). Eigene neue Gegenprobe: **59/59** HTTP/Prozess-Checks und **35/35** Chromium-Checks gegen eigene Fixture, Exit 0. | Kein Finding. |

### Visueller Abgleich (tatsächlich betrachtete Bilder)

Referenz: `screenshots/reference-sodata-desktop.png` (echte SODATA-App, 1440×900), zusätzlich als
Session-Anhang; neue Rohbilder: `screenshots/catalog-desktop.png`, `catalog-expanded-desktop.png`,
`detail-desktop.png` (1440×900), `catalog-mobile.png`, `detail-mobile.png` (390×844), eigener Leerzustand
`screenshots/candidate-empty-desktop.png`. Alle wurden mit dem Bild-Lesetool geöffnet und verglichen, nicht nur
nach Dateigrösse beurteilt.

| Merkmal | Konkreter visueller Abgleich |
|---|---|
| **Header** | Gleiche lokale Solothurn-Komponente: links Logo/„Kanton solothurn“, rechts „Regierung/Gerichte/Parlament/Karriere/my.so.ch“; zweite Zeile „Services/Verwaltung“ mit roter aktiver Unterstreichung. Die neue Seite übernimmt Typografie, Abstände und Farbe; nur der aktive Bereichspfad ist Kartenkatalog-konform. |
| **Breadcrumb** | Gleiche Chevron-/Rot-Grau-Sprache und 14-px-Typografie. Referenz `so.ch › Datenportal › Daten und Statistik`; neu `so.ch › Verwaltung › Amt für Geoinformation › Kartenkatalog` bzw. `… › Hoheitsgrenzen › Gemeindegrenzen`. Der Pfad ist fachlich adaptiert, die visuelle Hierarchie bleibt. |
| **Suche** | Referenz: 62 px hohes, helles Suchfeld mit Lupe und Lösch-X. Neu: gleiche Höhe/Fläche/Lupe/Platzhalter, zusätzlich ein sichtbarer dunkler „Suchen“-Button für die normale GET-Form (No-JS). Kein Layout-Shift, keine CDN-Icons. |
| **Filterband** | Referenz: Thema, Fachstelle/Amt, Publikationsdatum, „Filter zurücksetzen“. Neu: Thema und Produkttyp als echte Selects plus „Zurücksetzen“; **kein** Datumsfilter (nicht sinnvoll), **keine** Organisation (im Harvest keine echten Werte). Die reduzierten Kategorien sind planentsprechend, keine leeren Attrappen. |
| **Trefferzahl/Listenkopf** | Referenz: „62 Einträge gefunden“ + Kachel-/Listenumschalter + „Sortieren nach“. Neu: „886 Kartenebenen in 169 Themen“ + „Listenansicht · Titel A–Z“; der Umschalter/Sortierer ist P3b. Listkopf „Thema / Kartenebene“ + „Umfang“ statt Datum/Download; fachlich begründet. |
| **Zeilen** | Ruhige Trennlinien, Plus-Symbol links, fette Titel, gedämpfte Kurzbeschreibung, rechts Umfang; die Referenz-Datums-/Download-Spalten sind durch die echte Ebenenzahl bzw. Detail-/Kartenlinks ersetzt. Typografie und Weissraum wirken erkennbar wie die Referenz. |
| **Expansion** | Nach Enter/Tastatur öffnet sich die Zeile mit Minus-Symbol; darunter erscheint die echte Unterzeile mit Produktart-Badge, „Details“ und „Karte öffnen ↗“. Der Fokusring (3 px blau) ist im Screenshot sichtbar; `aria-expanded=true`. Die Referenz nutzt dasselbe Plus/Minus-Muster. |
| **Leerzustand** | Eigener Screenshot `candidate-empty-desktop.png`: „0 Kartenebenen in 0 Themen“, klare Meldung „Keine Kartenebenen gefunden“, Erklärung und „Alle Themen anzeigen“; Suchfeld/Filter bleiben sichtbar. Kein leerer weisser Bereich, keine erfundenen Treffer. |
| **Details** | Nicht in der Desktop-Referenz, aber konsistent zur Designsprache: Breadcrumb + Zurück-Link, Typ-Badge, grosse H1, roter „Im Kartenportal öffnen“-Button, Beschreibung, Schlagwörter/Synonyme, rechte Sidebar mit Layername/Produkttyp/Thema und nur echten Zusatzfeldern. Keine leeren Org-/Verfügbarkeits-/QML-Abschnitte; der Harvest-Layer zeigt keine QML. |
| **Mobil 390×844** | Header klappt auf Burger-Menü, Breadcrumb bricht um, Suchfeld/Filterspalten bleiben bedienbar, Liste ohne horizontales Overflow; Detail einspaltig mit voller Breite. Die Screenshots zeigen keinen abgeschnittenen Hauptinhalt. |

## Eigene neue Gegenprobe

### Warum gerade dieser Gegenfall die Phase kritisch testet

Die P3a-Risiken liegen nicht in der blossen Existenz von Qute oder eines Screenshots, sondern in (a) ausführbarem
XTF-HTML über mehrere Ausgabekontexte (Titel, Beschreibung, Organisation, Breadcrumb, Optionen), (b) falschen
oder erfundenen Metadaten/QML/Organisationen, (c) ungeprüften Link-Schemes (`javascript:`, `data:`,
protokollrelativ, Zugangsdaten), (d) No-JS-/Asset-Fallback und Fokus/Tastatur, (e) Fragment-vs.-Vollseite und
`Vary` sowie (f) falschen Ebenen-Anzahlen bei Filterung. Deshalb wurde eine **eigene**, vom Kandidaten
unabhängige XTF-Fixture mit bewusst bösartigen und bewusst fehlenden Feldern verwendet – nicht die
Codex-Testfixture und kein Codex-Test umbenannt.

### Eigene Eingabe (reproduzierbar)

`probe/p3a-probe.xml` – 3 Themen, 2 Ebenen:

- `Probe-Thema` (identifier `probe`) mit HTML-Beschreibung und `further_info_url=javascript:alert(1)`.
- `probe-empty` mit Titel `<script>alert('theme')</script>Leeres Thema`, ohne Beschreibung, mit
  `further_info_url=https://example.org/theme?x=1&y=2`.
- `probe-no-layers` mit 0 Ebenen.
- Ebene `ch.probe.alpha` (`datasetview`): Titel `<img src=x onerror=alert('title')>Alpha`, Beschreibung mit
  `<script>`, `<img onerror>`, `<a href="javascript:...">`, sicherem `<a href="https://example.org/good">`,
  Keywords/Synonyme, `org_name=<img src=x onerror=alert('org')>Amt`, `display_text=Öffentlich`,
  `service_download=true`, `transparency=85`, echtes QML.
- Ebene `ch.probe.beta` (`extwms`): nur Pflichtfelder, keine Beschreibung/Keywords/Synonyme/Organisation/
  Verfügbarkeit/Download/QML.

### Hypothese / Erwartung

Kein XSS/ausführbares Link-Schema; nur echte QML-/Organisations-/Verfügbarkeits-/Download-Werte; korrekte
Ebenenanzahlen; No-JS-Fallback und Tastaturbedienung; Fragment ohne Vollseite und mit `Vary: HX-Request`;
Asset-Ausfall zeigt den lokalen Fallback; JSON-Vertrag bleibt intakt.

### Exakte Schritte/Befehle und Exit

```sh
# Im Review-Worktree gebautes JAR, externe Fixture, eigener Port/Prozess:
cd /Users/stefan/sources/kartenkatalog-review-P3a
# (Das Python-Probe startet/stoppt die JVM selbst.)
python3 /Users/stefan/sources/kartenkatalog-P3a-review-evidence/9dfde0ffb2ad6f94bb71bdd278beb174eb3f9ee6/probe/p3a_probe.py
# Exit 0
```

Das Python-Probe startet `java -jar build/quarkus-app/quarkus-run.jar` mit
`CATALOG_XTF_LOCATION=file:<eigene Fixture>` in einem externen Arbeitsverzeichnis, führt die HTTP-Checks aus und
ruft anschliessend die eigene Playwright-Java-Probe (`P3aBrowserProbe.java`, echtes Chromium) gegen dieselbe
JVM auf. Danach wird der Prozess beendet.

### Tatsächliche Ausgabe / Rohreport

```text
PROBE OK: 59/59 checks, own fixture, real JVM and Chromium
BROWSER PROBE OK: 35/35 Chromium checks
PROBE_EXIT=0
```

Auszug der geprüften Punkte (vollständig `probe/http-console.log`, `probe/probe-summary.json`):

```text
PASS GET / 200 text/html
PASS three theme disclosures
PASS real layer counts 2/0/0 are rendered
PASS reflected q is HTML-escaped in the form value
PASS reflected q produced no executable tag
PASS empty state on no hits
PASS theme full page Vary HX-Request
PASS HX-Request true returns fragment 200
PASS fragment has exactly two layer rows
PASS HX-Request value is case-insensitive
PASS HX-Request false returns full page
PASS unknown theme fragment is 404 problem+json
PASS dangerous tags/attributes stripped
PASS no javascript/data href survives
PASS safe formatting survives Jsoup
PASS safe external anchor survives
PASS map link encodes transparency 85
PASS QML 200 XML attachment
PASS QML filename is encoded and correct
PASS missing QML is 404
PASS absolute http(s) further_info_url survives
PASS javascript: further_info_url is absent
PASS type filter N=1 selects extwms and keeps one theme
PASS unknown HTML theme filter is 404 problem+json
PASS relevance without q is 400
PASS q over 200 chars is 400
PASS JSON API remains 200 with two layers
PASS JSON qwcLayerName preserved
PASS JSON descriptionHtml is sanitised
PASS Chromium: first Tab focuses skip link
PASS Chromium: skip link focuses main
PASS Chromium: expansion loads exactly two real layer rows
PASS Chromium: aria-expanded true after expansion
PASS Chromium: no executable XSS in expanded list
PASS Chromium: reopen keeps exactly two rows (no duplicate swap)
PASS Chromium: detail contains no executable source markup
PASS Chromium: minimal detail omits empty Organisation/Verfügbarkeit/Download
PASS Chromium: no external runtime request was attempted
PASS No-JS: native details expansion works
PASS No-JS: fallback deep link reaches theme page
PASS No-JS: GET search submits and reflects q
PASS Mobile 390: no horizontal page overflow
PASS Asset fallback: header fallback visible when component module fails
PASS Asset fallback: breadcrumb fallback visible when component module fails
```

Eigene Screenshots (extern, tatsächlich betrachtet):

- `screenshots/probe-desktop.png` (eigene Fixture, 1440×900)
- `screenshots/probe-expanded-desktop.png` (Expansion, XSS-Titel als Text)
- `screenshots/probe-detail-desktop.png` (Detail mit escaped Organisation, QML, Map-Link)
- `screenshots/probe-nojs-theme.png` (No-JS-Fallback)
- `screenshots/probe-mobile.png` (390×844)
- `screenshots/candidate-empty-desktop.png` (Kandidaten-Harvest, Leerzustand)

### Schlussfolgerung

Die eigenen Gegenfälle bestätigen die Sanitization-/Escaping-Kette, die bedingten Metadaten, die
QML-/Map-/Extern-Link-Behandlung, die No-JS-/Asset-Fallback-Pfade, die Fokus-/Tastaturbedienung sowie
Fragment/Full-Page/`Vary` unabhängig von den Codex-Tests. Es gab **keinen** ausführbaren XSS, keinen
`javascript:`-/`data:`-Link, keine erfundene Organisation/QML und keine falsche Ebenenzahl.

## Pflichtprüfungen

Alle Kommandos im Review-Worktree `/Users/stefan/sources/kartenkatalog-review-P3a`. `EV` =
`/Users/stefan/sources/kartenkatalog-P3a-review-evidence/9dfde0ffb2ad6f94bb71bdd278beb174eb3f9ee6`.

| Testfamilie | Ausgeführter Befehl | Exit | Tests/Failures/Errors/Skips | Rohreport | SHA-bezogen |
|---|---|---:|---|---|---|
| JUnit/Quarkus + Build + echter Chromium (finaler Pflichtlauf) | `./gradlew clean test quarkusBuild playwrightTest` (Lauf 3) | **0** | **59 / 0 / 0 / 0** Unit/HTTP, **3 / 0 / 0 / 0** Chromium | `EV/logs/11-clean-test-quarkusBuild-playwright-run3.log`, `EV/build-reports/junit/`, `EV/build-reports/playwright/`, `EV/build-reports/html-*` | Ja, `9dfde0ff…` |
| Erster Pflichtlauf (Transient, siehe OBS-01) | derselbe Befehl (Lauf 1) | **1** | 3 Chromium-Tests, **1 Failure** (30 s Navigation-Timeout in `CatalogBrowserTests.java:83`), 0 Errors, 0 Skips | `EV/logs/01-clean-test-quarkusBuild-playwright.log`, `EV/probe/flake-run1-failure.txt` | Ja |
| Zweiter Pflichtlauf | derselbe Befehl (Lauf 2) | **0** | 59/0/0/0 + 3/0/0/0 | `EV/logs/03-clean-test-quarkusBuild-playwright-run2.log` | Ja |
| Fokussierte Wiederholungen des transienten Tests | `./gradlew playwrightTest --tests '…noJavascriptFallbackAndSimpleGetSearchRemainUsable'` + 3× `--rerun-tasks` | **0** (4×) | 1/0/0/0 je Lauf | `EV/logs/02-playwright-nojs-retry.log`, `EV/logs/04-nojs-repeat-{1,2,3}.log` | Ja |
| Runtime-Dependency-Graph | `./gradlew dependencies --configuration runtimeClasspath` | **0** | nur `lucene-core:10.6.0`, `lucene-analysis-common:10.6.0`; **kein** Queryparser, Spring, JDBC, Postgres, Hikari, JTE, Playwright | `EV/logs/15-dependencies-runtimeClasspath-final.log` | Ja |
| Echter JVM + P0-HTTP + Health + 8 Startfehler | `python3 scripts/p1-process-contract.py` | **0** | JVM ready **1.298 s**; 8 defekte Quellen je Exit 1, nie ready; P0-HTTP-Vertrag inkl. `/`-200 und `/thema` grün | `EV/logs/12-p1-process-final.log`, `EV/build-reports/p1-raw/` | Ja |
| Echter `quarkusDev` | `python3 scripts/p1-process-contract.py --dev` | **0** | Dev ready **5.177 s**; P0-HTTP-Vertrag grün | `EV/logs/13-p1-process-dev-final.log`, `EV/build-reports/p1-raw/` | Ja |
| P2 RAM-Index/Neustart/read-only/defekte XTF | `python3 scripts/p2-process-contract.py` | **0** | Harvest 886 Dokumente, ready 1.107 s, Index 342 ms; Fixture 12 Dok. ready 0.790/0.787 s, Index 142/139 ms; geänderte XTF ändert Ranking; Index-Startfehler Exit 1; keine persistierten Indexdateien | `EV/logs/14-p2-process-final.log`, `EV/build-reports/p2-raw/` | Ja |
| Qute-`@CheckedTemplate`-Canary | externe Wegwerfkopie: `./gradlew quarkusBuild`; dann fehlendes Feld im Detailtemplate | **0 Basis / 1 Mutation** | Mutation scheitert mit `Property/method [missingFieldForCanary] not found on class [CatalogViewModels$Detail]`; Kandidat unverändert | `EV/logs/08-qute-canary-baseline.log`, `EV/logs/09-qute-canary-mutated.log`, `EV/throwaway/qute-canary/` | Ja, gegen Kandidatenklassen |
| Eigene HTTP-/Chromium-Gegenprobe | `python3 "$EV/probe/p3a_probe.py"` | **0** | **59/59** HTTP-/Prozess-Checks, **35/35** Chromium-Checks, eigene Fixture, echter JVM + Chromium | `EV/probe/http-console.log`, `EV/probe/browser-console.log`, `EV/probe/probe-summary.json`, `EV/screenshots/probe-*.png` | Ja, gegen Kandidaten-JAR |
| Git-/Fixture-Hygiene | `git rev-parse HEAD`, `git status --porcelain=v1`, `git diff --check`, `shasum -a 256` | **0** | HEAD = Kandidat; Status leer; Diff-Check sauber; `baseline.json` und Harvest-XTF byte-identisch zum Basiscommit; 0 Commits seit Kandidat | `EV/logs/00-git-before.txt`, `EV/logs/99-git-after.txt` | Ja |

- **Fällige, aber nicht ausgeführte Checks und genaue Ursache:** Keine P3a-Pflichtprüfung wurde ausgelassen.
  Firefox, Desktop-Filterpopover, Mobile-Filtersheet, Chips, URL-History/Mehrfachfilter und das kumulative
  UI-Gesamtgate sind ausdrücklich **P3b**; Native/Linux/Container sind **P4**. Sie wurden weder als P3a-Lücke
  gewertet noch als Freigabe behauptet.
- **Kumulatives Phasengate:** Nicht anwendbar (`PHASENGATE: nein`). Die P0-/P1-/P2-Verträge wurden dennoch frisch
  mitgeprüft: `BaselineContractTests`, `CatalogWebTests`, `SearchFailureTests`, `scripts/p1-process-contract.py`,
  `scripts/p2-process-contract.py`, `quarkusDev` und Dependency-Graph. `src/test/resources/contract/baseline.json`
  (SHA-256 `cd1167f8…`) und die Harvest-XTF (`de40f025…`) sind **unverändert**; die API-/DTO-/Suchquellen zeigen
  keinen Diff.
- **Prozesshygiene:** Alle selbst gestarteten JVMs/Chromium-Prozesse wurden beendet; Ports 18090/18777/18888 sind
  frei; der Gradle-Daemon wurde mit `./gradlew --stop` beendet (`EV/logs/99-gradle-stop.log`).

## Findings

**FINDING-01: Keine offene Muss-Abweichung.** AC3A-01 bis AC3A-04 sind nach unabhängiger Quell-, HTTP-, Build-,
Browser-, Sicherheits- und Prozessprüfung erfüllt; alle P3a-Pflichtprüfungen und die eigene Gegenprobe sind
erfolgreich.

**Beobachtungen ohne Abnahmewirkung (kein Finding):**

- **OBS-01 (transienter Browser-Test, nicht reproduzierbar):** Im **ersten** frischen Pflichtlauf schlug
  `CatalogBrowserTests.noJavascriptFallbackAndSimpleGetSearchRemainUsable` einmalig mit
  `Timeout 30000ms exceeded` beim zweiten `page.navigate(base+"/")` (Zeile 83) fehl; die JVM war gesund
  (169/886, keine Exception), die anderen zwei Chromium-Tests liefen. Derselbe Test bestand danach **6×**
  (fokussierter Retry, 3× `--rerun-tasks`, zwei weitere vollständige Clean-Läufe), jeweils Exit 0. Es handelt
  sich um einen nicht reproduzierbaren Test-/Browser-Stall, nicht um einen beobachteten Produktfehler. Der
  finale Pflichtlauf ist **59/0/0/0 + 3/0/0/0**. Rohbeleg: `EV/probe/flake-run1-failure.txt`,
  `EV/logs/01-…`, `02-…`, `03-…`, `04-…`, `11-…`.
- **OBS-02 (Content-Type ohne charset auf der Themen-/Fragmentroute):** `/` und `/ebene/{id}` liefern
  `text/html;charset=UTF-8`; die Themenroute `/themen/{id}/ebenen` (Voll- und Fragmentantwort) liefert
  `text/html` **ohne** `charset`-Parameter. Die Bytes sind UTF-8 und die Vollseite enthält
  `<meta charset="UTF-8">`; der Browser-Test und die eigene Probe rendern Umlaute korrekt. Die P0-/P3a-Tests
  prüfen nur den Medientyp `text/html`. Das ist eine kleine Abweichung von der Formulierung in
  `EXECUTION_PLAN.md` §3.4 (`text/html;charset=UTF-8`), aber kein beobachteter Funktions-/Sicherheitsfehler und
  keine AC-Verletzung. Empfehlung für eine spätere Bereinigung: `charset=UTF-8` explizit setzen.
- **OBS-03 (Label „Verfügbarkeit“):** Die Detailseite mappt `display_text` (SIMI-Publikationsscope, z. B.
  „WGC, QGIS u. WMS“) auf das Label „Verfügbarkeit“. Das ist ein echter Quellwert, kein erfundener Wert; im
  gelieferten Harvest ist das Feld durchgehend leer, daher im realen UI nicht sichtbar. Die Bezeichnung ist
  semantisch etwas unscharf, aber durch den Plan („Verfügbarkeit soweit vorhanden“) gedeckt.
- **OBS-04 (No-JS-Header):** Der No-JS-/Asset-Fallback des Headers zeigt ein funktionierendes Logo, aber nicht
  die vollständige Top-/Section-Navigation. Breadcrumb-Fallback, Skip-Link, Footer und die No-JS-Themenliste
  funktionieren; die P3a-Anforderung an den No-JS-Deep-Link ist erfüllt. Die vollständige Header-Navigation ohne
  JS ist nicht gefordert.
- **OBS-05 (Berichtspfad):** Der Codex-Bericht verweist auf `build/reports/p3a/clean-test-browser.log`; dieser
  Pfad existiert nach einem `clean` nicht mehr. Die frischen Rohbelege dieses Reviews liegen stattdessen unter
  `EV/logs/` und `EV/build-reports/`; kein Produktfehler.

## Unabhängiges Urteil

**PASS**

- **Begründung:** Der Kandidat `9dfde0ffb2ad6f94bb71bdd278beb174eb3f9ee6` erfüllt AC3A-01, AC3A-02, AC3A-03
  und AC3A-04. Die Qute-Verträge sind echt typisiert (Build-Canary scheitert bei fehlendem Feld), das geteilte
  lokale Layout enthält Header/Breadcrumb/Skip-Link/Footer und Lizenzen ohne Runtime-CDN, die Startseite ist eine
  echte aufklappbare Themenliste mit korrekten Anzahlen und funktionierenden No-JS-/Deep-Link-Pfaden, die
  Metadaten-/QML-/Map-/Extern-Link-Ausgabe ist bedingt und sicher, und die echten Chromium-Screenshots sowie der
  visuelle SODATA-Vergleich sind vorhanden. Der finale Pflichtlauf ist **59/0/0/0 + 3/0/0/0**; P0-/P1-/P2-Verträge,
  RAM-Index, Health/Startfehler und `quarkusDev` sind frisch grün. Die eigene neue Gegenprobe (eigene
  XTF-Fixture, echter JVM und echtes Chromium) bestätigt **59/59** HTTP-/Prozess- und **35/35** Chromium-Checks.
  Die einzige Auffälligkeit ist ein einmaliger, nicht reproduzierbarer Playwright-Navigationstimeout (OBS-01),
  der in sieben Folgeläufen nicht auftrat; er ist kein reproduzierbarer Produktfehler. OBS-02 bis OBS-05 sind
  minor und ohne AC-Wirkung.
- **Nächster freigebbarer Lieferabschnitt:** **P3b** – erst nach diesem PASS. Der aktuelle Auftrag endet jedoch
  hier: **P3b wurde nicht begonnen**, Firefox/Filter-/History-/Mobile-Sheet-Gate und Native/Linux wurden nicht
  vorgezogen oder freigegeben.
- **Geprüfter SHA:** `9dfde0ffb2ad6f94bb71bdd278beb174eb3f9ee6`

### Rohbelege (absolut)

- Review: `/Users/stefan/sources/kartenkatalog-P3a-review-evidence/9dfde0ffb2ad6f94bb71bdd278beb174eb3f9ee6/review.md`
- Kandidatendiff: `…/candidate.diff`
- Build/Tests: `…/logs/11-clean-test-quarkusBuild-playwright-run3.log`, `…/build-reports/junit/TEST-*.xml`,
  `…/build-reports/playwright/TEST-*.xml`, `…/build-reports/html-test/`, `…/build-reports/html-playwright/`
- Browser-Screenshots: `…/screenshots/catalog-desktop.png`, `catalog-expanded-desktop.png`, `detail-desktop.png`,
  `catalog-mobile.png`, `detail-mobile.png`, `candidate-empty-desktop.png`; eigene:
  `probe-desktop.png`, `probe-expanded-desktop.png`, `probe-detail-desktop.png`, `probe-nojs-theme.png`,
  `probe-mobile.png`; Referenz: `reference-sodata-desktop.png`
- Prozesse: `…/logs/12-p1-process-final.log`, `…/logs/13-p1-process-dev-final.log`,
  `…/logs/14-p2-process-final.log`, `…/build-reports/p1-raw/`, `…/build-reports/p2-raw/`
- Qute-Canary: `…/logs/08-qute-canary-baseline.log`, `…/logs/09-qute-canary-mutated.log`,
  `…/throwaway/qute-canary/`
- Eigene Gegenprobe: `…/probe/p3a-probe.xml`, `…/probe/p3a_probe.py`, `…/probe/P3aBrowserProbe.java`,
  `…/probe/http-console.log`, `…/probe/browser-console.log`, `…/probe/probe-summary.json`,
  `…/probe/flake-run1-failure.txt`
- Dependency-Graph: `…/logs/15-dependencies-runtimeClasspath-final.log`
- Identität: `…/identity/models-api.json`, `…/identity/session-model-metadata.{json,txt}`,
  `…/identity/provider-connection.txt`, `…/identity/connection-evidence.txt`,
  `…/identity/deepseek-pricing.html`, `…/identity/deepseek-pricing-excerpt.txt`,
  `…/identity/cli-version.txt`
- Git: `…/logs/00-git-before.txt`, `…/logs/99-git-after.txt`, `…/logs/99-gradle-stop.log`
