# Unabhängiger Review P3b

## Identität und Kandidat

- **Lieferabschnitt:** P3b – Filter-/Such-UX, HTMX, Responsive und **kumulatives UI-Phasengate P3a + P3b**.
- **AC-IDs:** AC3B-01, AC3B-02, AC3B-03, AC3B-04 **kumulativ** AC3A-01, AC3A-02, AC3A-03, AC3A-04.
- **Rolle:** Agent B, unabhängiger Prüfer. Keine Produktimplementierung, keine Folgephase, keine Änderung des Kandidaten.
- **OpenCode-Version (tatsächlich geprüft):** gebündelte CLI **`opencode v2.1.4`**
  (`/Applications/OpenCode.app/Contents/Resources/opencode-cli --version`, Exit 0). Es wird **keine 2.2.x-Version** behauptet.
- **API-Provider + exakte Modellkennung (aus lokalen OpenCode-Sitzungs-/Modelldaten und Konfiguration, nicht aus Modellselbstauskunft):**
  - **Session-ID `ses_e6c582dc0ffemS1G4yvXXFtWCv`**, Titel „P3b independent cumulative UI review c030009c“, Version **2.1.4**,
    Modell in der Sitzungszeile `session_v2`: **`{"id":"deepseek-flash","providerID":"deepseek"}`**
    (`identity/session-v2-row.json`). Alle **78 Assistant-Nachrichten** dieser Sitzung tragen
    `providerID=deepseek`, `modelID=deepseek-flash`, `variant=default`
    (`identity/session-message-model-counts.json`); die User-/Synthetic-Nachrichten haben erwartungsgemäss keine Modelldaten.
  - **Lokale OpenCode-Modelldaten** (`tools.opencode.models`, Provider `deepseek`): Selektor
    **`deepseek/deepseek-flash`**, Anzeigename **„DeepSeek V4.1 Flash 1017“**, Status `active`
    (`identity/models-provider-deepseek.json`; der lokale Model-Cache nennt Provider-API
    `https://api.deepseek.com` und `@ai-sdk/openai-compatible`). Die globale Konfiguration
    `~/.config/opencode/opencode.jsonc` enthält **keinen** DeepSeek-Override; DeepSeek kommt aus der lokalen
    OpenCode-Modellkonfiguration. Es wurde **keine** Credential-Datei und keine Key-Umgebungsvariable gelesen oder ausgegeben.
  - **Direkter DeepSeek-Endpunkt:** Der CLI-Serverprozess (PID 44672) hatte während des Laufs eine
    **ESTABLISHED-TCP-Verbindung zu `3.173.21.63:443`**; `dig +short api.deepseek.com` liefert genau diese
    CloudFront-IP (`identity/provider-connection.txt`, `identity/api-deepseek-dns.txt`). Es war **kein**
    Zen-/Go-/OpenRouter-Endpunkt als Provider konfiguriert; die Session-/Provider-Metadaten weisen ausschliesslich
    `deepseek` aus. Andere Verbindungen des Prozesses (lokales Ollama/Tooling) ändern die Provider-/Modellwahl nicht.
  - **Offizielle DeepSeek-Quelle:** `https://api-docs.deepseek.com/quick_start/pricing/` (abgerufen 31.10.2026,
    archiviert als `identity/deepseek-pricing.html` und Auszug `identity/deepseek-pricing-excerpt.txt`) nennt
    **`deepseek-flash` = `DeepSeek-V4.1-Flash-1017`**, Basis-URL `https://api.deepseek.com`. Das deckt sich mit
    den lokalen OpenCode-Modelldaten.
  - **Sitzungsverzeichnis-Metadatum:** Die `session_v2`-Zeile nennt als Projektverzeichnis
    `/Users/stefan/sources/kartenkatalog`; der CLI-Serverprozess hatte `cwd=/Users/stefan/sources/kartenkatalog-review-P3b`
    (`lsof`), und **alle** Prüfkommandos liefen explizit im Review-Worktree. Der Kandidat blieb unverändert.
  - `execution-identity.json` wurde nur als Startmetadatum zur Kenntnis genommen und **nicht** als Identitätsbeleg verwendet.
- **BASE_SHA:** `82ff569acad954e8a65212fef7a9ec25f0e4b5e8`
- **CANDIDATE_SHA; `git rev-parse HEAD`:** `c030009c361995baeab0d83ef76fd763ec131a84`
- **Review-Worktree:** `/Users/stefan/sources/kartenkatalog-review-P3b` (detached HEAD auf exakt dem Kandidaten).
  - `git status --porcelain=v1` vor und nach dem Review: **leer** (`raw/git-status-before.txt`, `raw/git-status-after.txt`).
  - Keine Commits/Pushes; `git rev-list --count c030009c…..HEAD` = **0**. Keine Änderung an getrackten Quellen,
    Tests, Daten oder Build-/Kriteriendateien. Eigene Skripte/Fixtures/Mutationen liegen ausschliesslich im externen
    Evidenzverzeichnis bzw. in einer externen Wegwerfkopie. `build/` enthält nur erlaubte Build-Artefakte.
- **Kandidatendiff:** **22 Dateien, +572/−112**; vollständig als `raw/candidate.diff` (980 Zeilen) archiviert und
  risikobasiert gelesen (Web-Ressourcen, URL-Factory, ViewModels, Qute-Templates, CSS/JS, Browser-/HTTP-Tests,
  Build/CI, Doku, zwei gepinnte Referenz-PNGs).
- **Prüfumgebung:** macOS 15.8.1/arm64, Temurin **OpenJDK 21.0.7+6**, Gradle **9.8.2**, Quarkus **3.40.1.1**,
  Lucene **10.6.0**, Playwright **1.60.0**, echtes Chromium **und** Firefox. Native/Linux/Container sind für P3b
  **nicht** fällig und wurden nicht vorgezogen.
- **Letzter unabhängiger PASS-SHA aus voriger Lieferung:** `9dfde0ffb2ad6f94bb71bdd278beb174eb3f9ee6` (P3a).
- **Externes Evidenzverzeichnis:**
  `/Users/stefan/sources/kartenkatalog-P3b-review-evidence/c030009c361995baeab0d83ef76fd763ec131a84`

## Fachliche Kriterien

| AC-ID | Erfüllt / verletzt / nicht prüfbar | Eigener Code-/HTTP-/Test-/Browser-/Native-Beleg | Finding |
|---|---|---|---|
| **AC3A-01** | **Erfüllt** | `CatalogPages.Templates` nutzt `@CheckedTemplate(basePath="")` mit typisierten `CatalogPage`, `CatalogState` (`catalogState.html`), `ThemePage`, `List<LayerRow>`, `Detail`. **Eigene Build-Canary** in externer Wegwerfkopie: unveränderte Kopie `./gradlew quarkusBuild` **Exit 0**; danach `{option.count}` → `{option.missingFacetCount}` in `catalogState.html` → **Exit 1** mit `Property/method [missingFacetCount] not found on class [ch.so.agi.meta.web.CatalogViewModels$Option]` (`logs/qute-canary-baseline.log`, `logs/qute-canary-mutated.log`; Kandidat danach unverändert). Geteiltes `layout.html` mit lokalem `so-header`/`so-breadcrumb`, Skip-Link auf `#main-content`, Footer und Provenienzhinweis. Assets same-origin; eigener Chromium-/Firefox-Lauf protokolliert **keinen** externen Runtime-Request. Lizenzen/Herkunft lokal gepflegt; Runtime-Graph ohne JTE/Spring/JDBC/Postgres/Hikari. | Kein Finding. |
| **AC3A-02** | **Erfüllt** | `/` liefert **200 `text/html;charset=UTF-8`**; `/thema` ist **200-Alias**. Reale Harvest: **169 native `details.theme-row`**, initial **0 `.layer-row`**, Anzeige **886 Kartenebenen in 169 Themen** (eigener HTTP-Check + `logs/p3a-browser-jvm.log`). Aufklappen per Tastatur/Enter lädt die Unterzeilen; `aria-expanded`/`aria-controls` vorhanden; erneutes Schliessen/Öffnen erzeugt keinen zweiten Fragmentrequest (`hx-trigger="toggle once"`). No-JS: Fallback-Header/-Breadcrumb sichtbar, natives `details` funktioniert, `fallback-link` führt auf `/themen/{id}/ebenen` mit echten Ebenen; GET-Suche funktioniert. Fragment/Full-Page: `HX-Request: true` → Fragment ohne `<html>`; `false`/fehlend → Vollseite; `Vary` enthält `HX-Request, HX-History-Restore-Request`. Unbekanntes Thema/Layer → **404 `application/problem+json`**. Eigene Probe: eigene XTF-Expansion exakt gleich JSON-Reihenfolge. | Kein Finding. |
| **AC3A-03** | **Erfüllt** | Eigene externe XTF (`probe/p3b-probe.xml`) mit `<script>`/`onerror`/`javascript:` in Thema-/Ebenen-Titel, Beschreibung und Organisation, fehlenden Feldern, `transparency=85/100`, echtem QML und minimaler Ebene. Ergebnis über echtes HTTP und Chromium/Firefox: Titel/Organisation/Theme-Beschreibung **escaped**; `<script>`, `onerror`, `javascript:`, `img src=x` werden **nicht ausführbar** ausgegeben; nur die Jsoup-sanierte Beschreibung wird als `RawString` gerendert (`<strong>` bleibt, `<script>`/`onerror` verschwinden); QML-Link nur bei echtem `style_server`, `/ebene/probe.fluss/style` → **200 XML-Attachment** mit kodiertem Dateinamen, `probe.wald` → **404**; Kartenlink exakt `https://geo.so.ch/map?l=probe.fluss%5B85%5D` bzw. `…gebaeude%5B100%5D`; absolute HTTP(S)-`further_info_url` bleibt mit `rel="noopener"`, `javascript:` wird verworfen; minimale Ebene zeigt **keine** leeren Organisation-/Verfügbarkeits-/Download-/QML-Abschnitte. Reale Harvest enthält keine `org_name`/`display_text`/`service_download`-Werte; es werden keine erfunden. | Kein Finding. |
| **AC3A-04** | **Erfüllt** | Frischer Pflichtlauf (`./gradlew clean test quarkusBuild playwrightTest --console=plain`) am Kandidaten: **62 Unit/HTTP-Tests 0/0/0** und **14 echte Browser-Tests 0/0/0** (Exit 0, 2m23s). Chromium **und** Firefox starten selbst gebaute `build/quarkus-app/quarkus-run.jar`-Prozesse; Logs: 169 Themen/886 Ebenen (Harvest) und 3 Themen/12 Ebenen (Such-Fixture). Screenshots frisch in `build/reports/p3a/screenshots/` und `build/reports/p3b/screenshots/`, extern kopiert. Visueller Abgleich mit den echten SODATA-Referenzbildern wurde an den tatsächlichen Bildern vorgenommen (Header, Breadcrumb, Suche, Filterband, Trefferzahl, Listentypografie, Plus/Minus-Expansion, Leerzustand, Detailseite). | Kein Finding. |
| **AC3B-01** | **Erfüllt** | Filter **Thema**, **Produkttyp** und **Organisation** aus echten Werten: Harvest-UI zeigt **keine** Organisation (keine echten `org_name`-Werte), eigene No-Org-Fixture zeigt ebenfalls **keine** `Organisation`-Gruppe/Checkboxen; eigene Org-Fixture zeigt echte Gruppen mit den echten Werten. **Kein** `Aktualisiert`-Feld und keine Datumsattrappe (HTML und JSON). Dynamische Facetcounts exakt gleich JSON für alle Gruppen/Options, inkl. 0-Optionen: eigene Query `thema=natur&thema=verkehr&typ=datasetview&sort=title-desc` ergab thema 2/1/1/0, typ 3/1/1/1, organisation 2/1/0 und UI-Counts identisch (`probe/probe-console.log`). OR innerhalb/AND zwischen Gruppen, Mehrfachwahl, einzelne Chips, selektives Entfernen genau eines Werts, globaler Reset, Textsuche und Sortierung (`Titel A–Z`/`Z–A`, `Relevanz` nur mit Suchtext) funktionieren. HTML-Vollseite, HTMX-Fragment und JSON liefern dieselben Ebenen-IDs/-Reihenfolge und Counts. | Kein Finding. |
| **AC3B-02** | **Erfüllt** | HTMX-Fragment enthält **kein** `<!doctype>`, kein `<html>`/`<body>`, genau **ein** `#catalog-state` und die echte Filter-/Themenliste; Vollseite enthält den State nur eingebettet (keine verschachtelte Vollseite). `Vary: HX-Request, HX-History-Restore-Request`; History-Restore liefert Vollseite. URL-Aktualisierung via `hx-push-url`, GET-Form `method=get`; Browser-Back, Reload und kopierter Deep-Link erhalten Filter/Chips/Sortierung (eigene Chromium-/Firefox-Probe). Expansion bleibt bei Filter-/Chip-/Sortieränderung offen und lädt mit neuen Queryparametern; entfernte Themen verschwinden. Ohne JS: native Checkbox-Gruppen, vollständiger GET-Submit, Chips als normale Links, echter Themen-Fallback-Link, kein Overflow. Fehlerfall: eigener HTTP-503 für das Suchfragment lässt Ergebnisse/URL bestehen und zeigt den sichtbaren Fehlerhinweis. | Kein Finding. |
| **AC3B-03** | **Erfüllt** | **Chromium + Firefox**, **1440×900** und **390×844**, 14 echte Playwright-Tests (6 kumulative P3a + 8 P3b), **0 Failures/0 Errors/0 Skips**; beide Engines in den JUnit-XML-Namen nachgewiesen. Desktop: Popover per Klick/Enter, **kein Layout-Shift** (eigene Messung: Ergebnis-Y vor/nach Öffnen identisch), nur ein Popover, Escape/Aussenklick schliessen, Fokus kehrt zum Trigger zurück. Mobil: natives modales `dialog`-Sheet, Fokus bleibt enthalten, Escape schliesst und stellt den Auslöserfokus wieder her, sticky Apply, kein horizontaler Overflow. Leerzustand, Suchtext-Clear, Reset, Detail-/Kartenlink, Chips/Expansion und HTML-Ausgabe geprüft. Frische Screenshots beider Engines tatsächlich geöffnet und mit den gepinnten SODATA-Bildern verglichen (siehe unten). | Kein Finding. |
| **AC3B-04** | **Erfüllt** | **Kumulatives UI-Gate P3a+P3b** am exakt aktuellen SHA: alle acht AC3A/AC3B-Kriterien erfüllt; frische T1/T2/T3 (62 Unit/HTTP, 14 Browser), frische JVM-/Dev-/RAM-Index-Prozesse, API-/HTTP-/JSON-Verträge und Runtime-Graph grün. Eigener **neuer** Browser-/Filter-Gegenfall (eigene XTF mit echten plus fehlenden Werten, eigene No-Org-Fixture, reale Harvest; Chromium+Firefox; URL/Mehrfachfilter/Chip/History/Sheet/No-JS/JSON-Parität/Sicherheit) mit **212/212 Checks, Exit 0**. | Kein Finding. |

### Visueller Abgleich (tatsächlich betrachtete Bilder)

Referenzbilder (echte SODATA-App am Commit `9db0920e58613ece3bccf5e024199c016705d5de`, Herkunft in
`docs/quarkus-migration/reference/README.md`): `sodata-desktop.png`, `sodata-filter-desktop.png`,
`sodata-filter-mobile.png`. Neue Rohbilder: `screenshots/p3a/{chromium,firefox}-{catalog-desktop,catalog-expanded-desktop,detail-desktop,catalog-mobile,detail-mobile}.png`
und `screenshots/p3b/{chromium,firefox}-{desktop-popover,desktop-selected-expanded,desktop-empty,mobile-sheet,mobile-empty,mobile-expanded,mobile-detail}.png`;
eigene Probe: `probe/screenshots/{chromium,firefox}-{desktop-deeplink,desktop-popover,desktop-expanded,desktop-empty,mobile-sheet,mobile-empty,mobile-expanded,mobile-detail}.png`.
Alle wurden mit dem Bild-Lesetool geöffnet, nicht nach Dateigrösse beurteilt.

| Merkmal | Konkreter visueller Abgleich |
|---|---|
| **Header** | Gleiche lokale Solothurn-Komponente: links Logo/„Kanton solothurn“, rechts „Regierung/Gerichte/Parlament/Karriere/my.so.ch“; zweite Zeile „Services/Verwaltung“ mit roter aktiver Unterstreichung. Die neue Seite übernimmt Typografie, Abstände und Farbe; nur der aktive Bereichspfad ist Kartenkatalog-konform. |
| **Breadcrumb** | Gleiche Chevron-/Rot-Grau-Sprache und 14-px-Typografie. Referenz `so.ch › Datenportal › Daten und Statistik`; neu `so.ch › Verwaltung › Amt für Geoinformation › Kartenkatalog` bzw. `… › Hydrologie › Atlas`. Der Pfad ist fachlich adaptiert, die visuelle Hierarchie bleibt. |
| **Suchfeld** | Referenz: 62 px hohes, helles Suchfeld mit Lupe und Lösch-X. Neu: gleiche Höhe/Fläche/Lupe/Platzhalter; zusätzlich ein sichtbarer dunkler „Suchen“-Button für die normale GET-Form (No-JS). Bei aktivem Suchtext erscheint das X. Kein CDN-Icon, kein Layout-Shift. |
| **Filterband/Popover** | Referenz: drei Selects (Thema/Fachstelle/Publikationsdatum) plus „Filter zurücksetzen“; Popover mit Kopf „Thema“, X, Checkbox-Zeilen mit Counts rechts. Neu: Thema/Produkttyp (Organisation nur bei echten Werten), ruhige gerahmte Kategorien mit „Alle/N ausgewählt“ und Chevron; Popover mit Checkbox-Zeilen und Counts rechts, Fokusring, „Auswahl anwenden“. **Bewusste, dokumentierte Abweichungen:** kein Publikationsdatum/keine „Aktualisiert“-Attrappe; kein sichtbares X im Popover (Escape/Aussenklick), „Zurücksetzen“ als Link statt umrahmter Button. Funktional gleichwertig. |
| **Chips/Expansion** | Einzelne graue entfernbare Chips („Suche: …“, „Thema: …“, „Produkttyp: …“, „Organisation: …“), erhaltene Auswahl/Count; Referenz nutzt dieselbe Chip-Sprache. Themenzeilen mit Plus/Minus, fetten Titeln, gedämpften Kurztexten und Ebenenzahl rechts; nach Aufklappen echte Unterzeilen mit Produktart-Badge, „Details“ und „Karte öffnen ↗“. Ebenenreihenfolge folgt Titel A–Z/Z–A/Relevanz, Themen bleiben A–Z. |
| **Leerzustand** | „0 Kartenebenen in 0 Themen“, klare Meldung „Keine Kartenebenen gefunden“, Erklärung und „Alle Themen anzeigen“; Suchfeld/Filter/Chips bleiben sichtbar. Kein leerer weisser Bereich, keine erfundenen Treffer. |
| **Mobile-Sheet** | Referenz: weisses Overlay mit „Filter“/X, Gruppen als Karten, Checkboxen/Counts, rotem „Ergebnisse anzeigen“ und „Alle zurücksetzen“. Neu: natives modales Bottom-Sheet mit „Karten filtern“/X, denselben Gruppen, scrollbaren Listen, sticky dunklem „Filter anwenden“ und Reset-Link im scrollbaren Bereich; max. 90dvh statt voller Referenzhöhe. **Bewusste Abweichung:** CTA dunkel statt rot, Reset nicht sticky. Fokusbegrenzung/Rückkehr, Schliessen und Overflow sind korrekt. |
| **Details** | Nicht in der Desktop-Referenz, aber konsistent zur Designsprache: Breadcrumb + Zurück-Link, Typ-Badge, grosse H1, roter „Im Kartenportal öffnen“-Button, Beschreibung, Schlagwörter/Synonyme, rechte Sidebar mit Layername/Produkttyp/Thema und nur echten Zusatzfeldern. Keine leeren Org-/Verfügbarkeits-/Download-/QML-Abschnitte; der Harvest-Layer zeigt keine QML. Mobile einspaltig, kein Overflow. |

## Eigene neue Gegenprobe

### Warum gerade dieser Gegenfall die Phase kritisch testet

Die P3b-Risiken liegen nicht in der blossen Existenz von Filtern oder Screenshots, sondern in (a) falschen
Facetcounts bei AND/OR und Mehrfachwahl, (b) Filter-/Chip-/URL-/History-Zustand über HTML, HTMX und JSON,
(c) stabiler Expansion bei Querywechsel, (d) Popover-/Sheet-Fokus und Layout-Shift, (e) No-JS-GET-Fallback,
sowie (f) XSS/Link-Schemes/QML/Map und echten vs. fehlenden Metadaten. Deshalb wurde eine **eigene**, vom
Kandidaten unabhängige XTF mit echten plus bewusst fehlenden Werten und eine zweite No-Org-Fixture verwendet –
nicht die Codex-Testfixture und kein Codex-Test umbenannt.

### Eigene Eingabe (reproduzierbar)

- `probe/p3b-probe.xml` – 4 Themen, 8 Ebenen:
  - **Natur & Landschaft** (`natur`): Flusskataster (datasetview, Org „Amt für Umwelt“, Keywords/Synonyme,
    Beschreibung mit `<script>`/`onerror`, `transparency=85`, echtes QML, `service_download=true`),
    Waldfläche (layergroup, **keine** Organisation, **keine** Beschreibung), Gewässerlinie (datasetview, Org Umwelt).
  - **Verkehr** (`verkehr`): Strassenachse (datasetview, Org „Amt für Verkehr“), Tunnel (facadelayer, keine Org),
    Bushaltestelle (extwms, Org Verkehr); `further_info_url=https://example.org/verkehr?x=1&y=2`.
  - **Bauen & Wohnen** (`bauen`): Gebäude (datasetview, Titel `<img src=x onerror=alert('title')>Gebäude`,
    Org `<script>alert('org')</script>Amt X`, `transparency=100`), Bauzone (layergroup, keine Org).
  - **Leeres Thema** (`leer`): echte Thema-Zeile mit **0** Ebenen.
  - Thema Natur: Beschreibung mit `<script>`/`onerror`/`javascript:`-Link, `further_info_url=javascript:…`.
- `probe/p3b-probe-noorg.xml` – 2 Themen/2 Ebenen, **keine** `org_name`-Werte.
- Reale Harvest `data/kartenkatalog-harvest-ohne-qml.xtf` (unverändert) als dritte Quelle.

### Hypothese / Erwartung

Dynamische Facetcounts mit OR innerhalb/AND zwischen Gruppen und Ausschluss der eigenen Kategorie; korrekte
Mehrfachwahl/Chips/Reset; exakte JSON-/HTML-/HTMX-Parität; Fragment ohne Vollseite; URL/Back/Reload/Deep-Link;
stabile Expansion; Popover ohne Shift; mobiles Sheet mit Fokus/Schliessen/Overflow; No-JS-GET-Submit; keine
ausführbare XSS, keine `javascript:`-Links, QML/Map nur bei echten Werten; keine erfundene Organisation und
kein Datumsfilter; unveränderte JSON-Baseline/Harvest.

### Exakte Schritte/Befehle und Exit

```sh
# Im Review-Worktree; gebautes Kandidaten-JAR, externe Fixtures, echter JVM
cd /Users/stefan/sources/kartenkatalog-review-P3b
EV=/Users/stefan/sources/kartenkatalog-P3b-review-evidence/c030009c361995baeab0d83ef76fd763ec131a84
CP=$(cat "$EV/probe/test-runtime-classpath.txt")
javac -cp "$CP" -d "$EV/probe/classes" "$EV/probe/P3bProbe.java"
java -cp "$CP:$EV/probe/classes" P3bProbe \
  "$PWD/build/quarkus-app/quarkus-run.jar" \
  "$EV/probe/p3b-probe.xml" \
  "$EV/probe/p3b-probe-noorg.xml" \
  "$PWD/data/kartenkatalog-harvest-ohne-qml.xtf" \
  "$EV/probe"
# Exit 0
```

Das Java-Programm startet selbst drei getrennte gebaute JVM-Prozesse (eigene Fixture, No-Org-Fixture, Harvest),
wartet auf `/q/health/ready`, führt HTTP-/JSON-/Facet-/Sicherheitsprüfungen und echte Chromium-/Firefox-Flows
(Desktop/Filter/Popover/Expansion/History/No-JS/Mobile/Detail) aus und beendet alle Prozesse wieder.

### Tatsächliche Ausgabe / Rohreport

```text
PROBE OK: 212 checks, 0 failures
PROBE_EXIT=0
```

**212/212 Checks** (85 HTTP-/JSON-/Facet-/Sicherheits-/Fixture-Checks plus 127 Browser-Interaktionen in
Chromium und Firefox). Auszug aus `probe/probe-console.log`:

```text
PASS JSON multi-filter ids and title-desc order [expected=[probe.strasse, probe.gewaesser, probe.fluss], actual=[probe.strasse, probe.gewaesser, probe.fluss]]
PASS thema facet natur honours other filters [expected=2, actual=2]
PASS thema facet verkehr honours other filters [expected=1, actual=1]
PASS thema facet bauen honours other filters [expected=1, actual=1]
PASS thema facet leer is zero [expected=0, actual=0]
PASS typ facet datasetview honours selected themes [expected=3, actual=3]
PASS typ facet extwms honours selected themes [expected=1, actual=1]
PASS org facet Umwelt honours themes and type [expected=2, actual=2]
PASS org facet Verkehr honours themes and type [expected=1, actual=1]
PASS org facet with the real XSS-labelled value has zero under the other filters [expected=0, actual=0]
PASS UI thema facet count for 'natur' equals JSON [expected=2, actual=2]
PASS UI lists every real organisation facet value [expected=[<script>alert('org')</script>Amt X, Amt für Umwelt, Amt für Verkehr], actual=[<script>alert('org')</script>Amt X, Amt für Umwelt, Amt für Verkehr]]
PASS HTMX fragment has no doctype
PASS HTMX fragment has no html/body wrapper
PASS HTMX fragment has exactly one catalog state [expected=1, actual=1]
PASS history restore returns a full document
PASS expanded natur rows equal JSON theme layers [expected=[/ebene/probe.gewaesser, /ebene/probe.fluss], actual=[/ebene/probe.gewaesser, /ebene/probe.fluss]]
PASS map URL encodes real transparency 85 [expected=https://geo.so.ch/map?l=probe.fluss%5B85%5D, actual=https://geo.so.ch/map?l=probe.fluss%5B85%5D]
PASS QML link only for real style_server [expected=/ebene/probe.fluss/style, actual=/ebene/probe.fluss/style]
PASS QML download status [expected=200, actual=200]
PASS QML content type: application/xml
PASS QML attachment filename
PASS missing QML is 404 [expected=404, actual=404]
PASS XSS title is escaped text
PASS XSS organisation is escaped text
PASS theme description keeps safe strong tag
PASS theme description script removed [expected=0, actual=0]
PASS absolute https further_info_url kept with rel=noopener
PASS unknown theme is 404 [expected=404, actual=404]
PASS unknown theme problem+json: application/problem+json
PASS unknown layer is 404 [expected=404, actual=404]
PASS unknown product type is 400 [expected=400, actual=400]
PASS unknown theme filter is 404 [expected=404, actual=404]
PASS overlong q is 400 [expected=400, actual=400]
PASS no empty Organisation group without real values [expected=0, actual=0]
PASS no empty Organisation checkboxes [expected=0, actual=0]
PASS harvest renders 169 real theme rows [expected=169, actual=169]
PASS harvest heading reports 886/169
PASS qwcLayerName still equals the published layer id
PASS chromium: first Tab reaches skip link [expected=Zum Inhalt springen, actual=Zum Inhalt springen]
PASS chromium: skip link focuses main [expected=main-content, actual=main-content]
PASS chromium: deep link selects two themes and one type [expected=3, actual=3]
PASS chromium: deep link shows exactly two matching themes [expected=2, actual=2]
PASS chromium: deep link shows exactly three chips [expected=3, actual=3]
PASS chromium: popover does not shift the results [expected=674.375, actual=674.375]
PASS chromium: only one popover open [expected=1, actual=1]
PASS chromium: Escape closes popover
PASS chromium: Escape returns focus to the trigger summary [expected=panel-typ, actual=panel-typ]
PASS chromium: outside click closes popover [expected=0, actual=0]
PASS chromium: expanded rows follow title-desc order [expected=[/ebene/probe.gewaesser, /ebene/probe.fluss], actual=[/ebene/probe.gewaesser, /ebene/probe.fluss]]
PASS chromium: chip removes exactly one theme value
PASS chromium: reopened expansion uses the new query [expected=[/ebene/probe.gewaesser, /ebene/probe.fluss], actual=[/ebene/probe.gewaesser, /ebene/probe.fluss]]
PASS chromium: Back restores three selected filters [expected=3, actual=3]
PASS chromium: Back restores three chips [expected=3, actual=3]
PASS chromium: Reload restores three selected filters [expected=3, actual=3]
PASS chromium: relevance option is selected with search text [expected=relevance, actual=relevance]
PASS chromium: clearing search removes q and invalid relevance sort
PASS chromium: clearing search falls back to title A-Z [expected=title-asc, actual=title-asc]
PASS chromium: sort change reloads the open theme in Z-A order [expected=[/ebene/probe.wald, /ebene/probe.gewaesser, /ebene/probe.fluss], actual=[/ebene/probe.wald, /ebene/probe.gewaesser, /ebene/probe.fluss]]
PASS chromium: empty state keeps removable chips [expected=2, actual=2]
PASS chromium: reflected payload stays plain text in the field [expected="><img src=x onerror=alert(9)>, actual="><img src=x onerror=alert(9)>]
PASS chromium: reflected payload creates no markup [expected=0, actual=0]
PASS chromium: no external runtime asset request []
PASS chromium: no page JavaScript error []
PASS chromium: Escape closes the sheet
PASS chromium: Escape restores focus to the trigger [expected=mobile-filter-trigger, actual=mobile-filter-trigger]
PASS chromium no-JS: repeated GET checkboxes submit all three themes
PASS chromium no-JS: all four selections survive the GET submit [expected=4, actual=4]
PASS chromium no-JS: theme fallback link works
PASS firefox: popover does not shift the results [expected=674.4000244140625, actual=674.4000244140625]
PASS firefox: focus stays inside the sheet
PASS firefox: Escape restores focus to the trigger [expected=mobile-filter-trigger, actual=mobile-filter-trigger]
PASS firefox no-JS: theme page lists the filtered real layers [expected=2, actual=2]
```

Eigene Screenshots (extern, tatsächlich betrachtet): `probe/screenshots/chromium-desktop-deeplink.png`,
`chromium-desktop-popover.png`, `chromium-desktop-expanded.png`, `chromium-desktop-empty.png`,
`chromium-mobile-sheet.png`, `chromium-mobile-empty.png`, `chromium-mobile-expanded.png`,
`chromium-mobile-detail.png` sowie die entsprechenden Firefox-Bilder.

### Schlussfolgerung

Die eigenen Gegenfälle bestätigen unabhängig die dynamischen Facetcounts, OR/AND-Mehrfachfilter, Chips/Reset,
JSON-/HTML-/HTMX-Parität, die Fragment-/History-/Deep-Link-/Expansion-Verträge, Popover/Sheet/Fokus/Overflow,
den No-JS-GET-Fallback sowie die XSS-/Link-/QML-/Map-/Metadaten-Behandlung. Es gab **keinen** ausführbaren XSS,
keinen `javascript:`-/`data:`-Link, keine erfundene Organisation, keinen Datumsfilter und keine falsche
Facet-/Ebenenzahl. Es ist **kein Produktfinding** aus der eigenen Probe entstanden.

## Pflichtprüfungen

Alle Kommandos im Review-Worktree `/Users/stefan/sources/kartenkatalog-review-P3b`. `EV` =
`/Users/stefan/sources/kartenkatalog-P3b-review-evidence/c030009c361995baeab0d83ef76fd763ec131a84`.

| Testfamilie | Ausgeführter Befehl | Exit | Tests/Failures/Errors/Skips | Rohreport | SHA-bezogen |
|---|---|---:|---|---|---|
| JUnit/Quarkus + Build + echter Browser (Pflichtlauf) | `./gradlew clean test quarkusBuild playwrightTest --console=plain` | **0** | **62 / 0 / 0 / 0** Unit/HTTP, **14 / 0 / 0 / 0** Browser (6 P3a + 8 P3b, je Chromium/Firefox) | `raw/gradle-clean-test-build-playwright.log`, `build-reports/junit-test/`, `build-reports/junit-playwright/`, `build-reports/html-test/`, `build-reports/html-playwright/` | Ja, `c030009c…` |
| Echter JVM + P0-HTTP + Health + 8 Startfehler | `python3 scripts/p1-process-contract.py` | **0** | JVM ready **1.098 s**; 8 defekte Quellen je Exit 1, nie ready; P0-HTTP-Vertrag inkl. `/`-200 und `/thema` grün | `logs/p1-process.log`, `build-reports/p1-raw/` | Ja |
| Echter `quarkusDev` | `python3 scripts/p1-process-contract.py --dev` | **0** | Dev ready **5.574 s**; P0-HTTP-Vertrag grün | `logs/p1-process-dev.log`, `logs/p1-process-dev-exit.txt` | Ja |
| P2 RAM-Index/Neustart/read-only/defekte XTF | `python3 scripts/p2-process-contract.py` | **0** | Harvest 886 Dokumente, ready **1.015 s**, Index **316 ms**, RSS **197824 KiB**, mediane Suche **19.211 ms**; Fixture 12 Dok. ready 0.799/0.788 s, Index 139/140 ms; geänderte XTF ändert Ranking; Index-Startfehler Exit 1; keine persistierten Indexdateien | `logs/p2-process.log`, `build-reports/p2-raw/` | Ja |
| Runtime-Dependency-Graph | `./gradlew dependencies --configuration runtimeClasspath --console=plain` | **0** | Nur `lucene-core:10.6.0` und `lucene-analysis-common:10.6.0`; **kein** Queryparser, Spring, JDBC, Postgres, Hikari, JTE, Playwright | `logs/dependencies-runtimeClasspath.log` | Ja |
| Qute-`@CheckedTemplate`-Canary | externe Wegwerfkopie: `./gradlew quarkusBuild`; dann `{option.count}` → `{option.missingFacetCount}` in `catalogState.html` | **0 Basis / 1 Mutation** | Mutation: `Property/method [missingFacetCount] not found on class [ch.qos… CatalogViewModels$Option]`; Kandidat unverändert | `logs/qute-canary-baseline.log`, `logs/qute-canary-mutated.log`, `logs/qute-canary-exits.txt` | Ja, gegen Kandidatenklassen |
| Eigene HTTP-/Browser-Gegenprobe | `java -cp … P3bProbe …` (siehe oben) | **0** | **212/212** Checks (85 HTTP/JSON/Security/Fixture + 127 Browser), eigene XTF + No-Org-Fixture + Harvest, echter JVM und echtes Chromium/Firefox | `probe/probe-console.log`, `probe/probe-exit.txt`, `probe/screenshots/` | Ja, gegen Kandidaten-JAR |
| Git-/Fixture-Hygiene | `git rev-parse HEAD`, `git status --porcelain=v1`, `git rev-list --count`, `git diff --check`, `shasum -a 256` | **0** | HEAD = Kandidat; Status leer; 0 Commits seit Kandidat; Diff-Check sauber; `baseline.json` SHA-256 `cd1167f8…` und Harvest `de40f025…` **byte-identisch** zum Basiscommit | `raw/git-status-before.txt`, `raw/git-status-after.txt`, `raw/candidate.diff` | Ja |
| Browser-Installation/Prozesse | `installPlaywrightBrowsers` (Task-Dependency von `playwrightTest`) | **0** | Chromium **und** Firefox installiert; getrennte JVMs: Harvest 169 Themen/886 Ebenen (`logs/p3a-browser-jvm.log`) und Such-Fixture 3 Themen/12 Ebenen (`logs/p3b-browser-jvm.log`) | `logs/p3a-browser-jvm.log`, `logs/p3b-browser-jvm.log` | Ja |
| Identity | `tools.opencode.models`, `session_v2`/`session_message`, lokaler Model-Cache, `dig`, `lsof`, offizielle DeepSeek-Preisseite | **0** | `deepseek/deepseek-flash` = DeepSeek V4.1 Flash 1017; Session-Modell `deepseek-flash`/`deepseek`; direkte Verbindung `3.173.21.63:443` = `api.deepseek.com`; **keine** Credentials gelesen | `identity/` | Ja, Sitzungsmetadaten |

- **Fällige, aber nicht ausgeführte Checks und genaue Ursache:** Keine P3b-Pflichtprüfung wurde ausgelassen.
  **P4 Native/Linux/Container** sind hier noch **nicht** fällig und wurden weder ausgeführt noch freigegeben;
  der Review stoppt ausdrücklich **vor P4**.
- **Kumulatives UI-Phasengate P3a+P3b:** Ja, alle acht AC3A/AC3B-Kriterien am selben SHA geprüft; frische
  T1/T2/T3; P3a-UI-Funktionen (Layout, No-JS-Links, Details, QML/Map, XSS) sind in den neuen Läufen und im
  eigenen Gegenfall weiterhin grün. Keine regressiven Wechselwirkungen festgestellt.
- **Prozesshygiene:** Alle selbst gestarteten JVMs, Chromium-/Firefox-Prozesse und der eigene Probe-Prozess wurden
  beendet; die App-Ports sind frei. Der Gradle-Daemon wird am Ende mit `./gradlew --stop` beendet.

## Findings

**FINDING-01: Keine offene Muss-Abweichung.** AC3A-01 bis AC3A-04 und AC3B-01 bis AC3B-04 sind nach
unabhängiger Quell-, HTTP-, JSON-, Build-, Browser-, Sicherheits- und Prozessprüfung erfüllt; alle P3b-Pflichtprüfungen und die eigene Gegenprobe sind erfolgreich. Es gibt **kein FAIL** und **kein BLOCKED**.

**Beobachtungen ohne Abnahmewirkung (kein Finding):**

- **OBS-01 (eigene Probe-Harness, kein Produktfehler):** Die ersten beiden Läufe der eigenen Probe hingen im
  No-JS-Kontext an der zusätzlichen Screenshot-Hilfe `document.fonts.ready`; dies ist derselbe Harness-Effekt,
  den der Codex-Bericht für den No-JS-Firefox-Lauf dokumentiert. Zwei Zwischenläufe zeigten ausserdem zwei
  falsche **Probe**-Erwartungen (Theme-Zeilenanzahl und XSS-Org-Facettenkey); korrigiert wurde ausschliesslich
  die externe Probe, nie der Kandidat. Nach Entfernen des Font-Warteaufrufs im No-JS-Screenshot-Helfer lief die
  Probe **212/212, Exit 0**. Alle No-JS-Funktionsassertions blieben aktiv. Rohbeleg: `probe/probe-console.log`,
  `probe/probe-exit.txt`.
- **OBS-02 (asynchrones `aria-expanded` beim Schliessen des mobilen Sheets):** Nach `Escape` schliesst das native
  `dialog` sofort und der Fokus kehrt sofort zum Auslöser zurück; das `aria-expanded="false"` des Auslösers wird
  im `close`-Event gesetzt und kann unmittelbar nach dem Tastendruck für einen Tick noch `true` sein (in Chromium
  beobachtet, in Firefox nicht). Nach dem `close`-Task ist der Wert korrekt `false`; die eigene Probe wartet
  darauf. Kein sichtbarer Funktions-/Fokusfehler, keine AC-Verletzung.
- **OBS-03 (bewusste visuelle Adaptationen):** Das Desktop-Popover hat keinen sichtbaren X-Button/Header, der
  Reset ist ein Link statt eines umrahmten Buttons, und das mobile Sheet verwendet einen dunklen „Filter anwenden“-
  CTA statt des roten „Ergebnisse anzeigen“. Diese Unterschiede sind in `implementation/P3b.md` und
  `reference/README.md` als fachliche Adaptation dokumentiert; Funktion (Schliessen per Escape/Aussenklick,
  Reset, Apply, Fokus) ist vollständig vorhanden.
- **OBS-04 (0-Count-Option „Leeres Thema“):** Die eigene Fixture enthält ein echtes XTF-Thema ohne Ebenen; es
  erscheint als 0-Count-Option im Themenfilter. Das ist ein **echter Quellwert**, kein erfundener Filter; die
  P2-/P3b-Vorgabe „0-Optionen bleiben wählbar/entfernbar“ ist erfüllt. Im realen Harvest gibt es keine erfundene
  Organisation und keinen Datumsfilter.

## Unabhängiges Urteil

**PASS**

- **Begründung:** Der Kandidat `c030009c361995baeab0d83ef76fd763ec131a84` erfüllt **AC3B-01, AC3B-02, AC3B-03,
  AC3B-04** und kumulativ **AC3A-01, AC3A-02, AC3A-03, AC3A-04**. Das **kumulative UI-Gesamtziel P3a + P3b** ist
  bestätigt: SODATA-nahe, lokal assetierte Oberfläche; echte aufklappbare Themenliste mit korrekten Ebenen und
  Counts; sinnvolle Filter nur aus echten Werten; dynamische Facetcounts mit OR/AND; Mehrfachwahl, einzelne
  Chips/Reset, Textsuche und A–Z/Z–A/Relevanz-Sortierung; identische Ergebnisse in HTML, HTMX und JSON; saubere
  Fragmente ohne verschachtelte Vollseite; URL-/Back-/Reload-/Deep-Link-Zustand; stabile Expansion; funktionierender
  No-JS-GET-Fallback; Desktop-Popover und mobiles Sheet mit Fokus/Schliessen/Overflow; lokale Assets/Header/
  Breadcrumb/Footer/Skip-Link; bedingte Metadaten; sichere XSS-/Link-/QML-/Map-Ausgabe. Der finale Pflichtlauf ist
  **62/0/0/0 + 14/0/0/0**; P1-/P2-/Dev-/Runtime-Graph-Prüfungen sind frisch grün, JSON-Baseline und Harvest
  byte-identisch. Die eigene neue Gegenprobe bestätigt **212/212** Checks in Chromium und Firefox. Die einzigen
  Auffälligkeiten sind ein Probe-Harness-Font-Stall (OBS-01), ein kurzes asynchrones `aria-expanded`-Timing
  (OBS-02) und dokumentierte visuelle Adaptationen (OBS-03/04); keine davon ist eine offene Muss-Abweichung.
- **Nächster freigebbarer Lieferabschnitt:** **P4** – erst nach diesem PASS. Der aktuelle Auftrag endet jedoch
  hier: **P4 Native/Linux/Container wurde nicht begonnen, nicht ausgeführt und nicht freigegeben.** Vor P4 wird
  ausdrücklich gestoppt.
- **Geprüfter SHA:** `c030009c361995baeab0d83ef76fd763ec131a84`
- **Bericht (absolut):**
  `/Users/stefan/sources/kartenkatalog-P3b-review-evidence/c030009c361995baeab0d83ef76fd763ec131a84/review.md`

### Rohbelege (absolut)

- Review: `/Users/stefan/sources/kartenkatalog-P3b-review-evidence/c030009c361995baeab0d83ef76fd763ec131a84/review.md`
- Kandidatendiff: `…/raw/candidate.diff`
- Build/Tests: `…/raw/gradle-clean-test-build-playwright.log`, `…/build-reports/junit-test/TEST-*.xml`,
  `…/build-reports/junit-playwright/TEST-*.xml`, `…/build-reports/html-test/`, `…/build-reports/html-playwright/`
- Prozesse: `…/logs/p1-process.log`, `…/logs/p1-process-dev.log`, `…/logs/p2-process.log`,
  `…/build-reports/p1-raw/`, `…/build-reports/p2-raw/`, `…/logs/dependencies-runtimeClasspath.log`
- Qute-Canary: `…/logs/qute-canary-baseline.log`, `…/logs/qute-canary-mutated.log`, `…/logs/qute-canary-exits.txt`
- Eigene Gegenprobe: `…/probe/p3b-probe.xml`, `…/probe/p3b-probe-noorg.xml`, `…/probe/P3bProbe.java`,
  `…/probe/probe-console.log`, `…/probe/probe-exit.txt`, `…/probe/screenshots/`
- Browser-Screenshots: `…/screenshots/p3a/`, `…/screenshots/p3b/`; Referenz:
  `/Users/stefan/sources/kartenkatalog-review-P3b/docs/quarkus-migration/reference/`
- Identity: `…/identity/session-v2-row.json`, `…/identity/session-message-model-counts.json`,
  `…/identity/models-provider-deepseek.json`, `…/identity/provider-connection.txt`,
  `…/identity/deepseek-pricing-excerpt.txt`, `…/identity/cli-version.txt`
- Git: `…/raw/git-status-before.txt`, `…/raw/git-status-after.txt`, `…/raw/git-head-before.txt`
