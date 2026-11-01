# Unabhängiger Review C4 – Kartenkatalog Cleanup

## 1. Identität und Umgebung

- **Lieferabschnitt / Gate:** C4 – aktive Dokumentation, Archiv und kumulative Gesamtabnahme. **PHASENGATE: ja.**
- **Repository:** `edigonzales/kartenkatalog`.
- **OpenCode-GUI-Version und Herkunft (überprüfbare Metadaten, keine Modellselbstauskunft):**
  - Laufende GUI-App: **OpenCode 2.1.4** auf macOS. Belege: `/Applications/OpenCode.app/Contents/Info.plist` `CFBundleShortVersionString = 2.1.4`; laufender Dienst `/Users/stefan/Library/Application Support/ai.opencode.desktop/cli/2.1.4-arm64/opencode-cli serve --service`; Crashpad `_version=2.1.4`.
  - Aktuelle Session: `ses_e68f897b2ffe23AHtR1zYlbT85`, Titel „Kumulativer C4-Gesamtreview kartenkatalog“, `version=2.1.4`, `agent=build`, `model={"id":"deepseek-flash","providerID":"deepseek","variant":"high"}`.
  - Direkter Provider: **`deepseek`**, `api=https://api.deepseek.com`, npm `@ai-sdk/openai-compatible`, Env-Name `DEEPSEEK_API_KEY` (kein Credentialwert gelesen/ausgegeben).
  - Modell: `deepseek/deepseek-flash`, Anzeigename **„DeepSeek V4.1 Flash 1017“**, Release `2026-10-17`, `canonical_model_id=deepseek/deepseek-v4.1-flash-1017`, Variante `high`.
  - Kein OpenRouter-/Zen-Fallback, kein OpenCode-CLI-Aufruf. Die GUI-Session nennt als `directory` den Haupt-Checkout; **alle Repositorykommandos dieses Reviews liefen mit explizitem `cwd` im separaten C4-Review-Worktree.** Belege: `/tmp/kartenkatalog-c4-review-0b1704c/evidence/identity/`.
- **BASE_SHA:** `ec57d04b8688617ddfe3a36070f23d15459fce30` (`git cat-file -t` = commit; direkter Elternknoten des Kandidaten).
- **CANDIDATE_SHA (im Worktree verifiziert):** `0b1704c72113a9872a10cf68110f506c6592a547` (`git rev-parse HEAD`; `HEAD^` = BASE).
- **Review-Worktree:** `/Users/stefan/sources/kartenkatalog-cleanup-C4-review-0b1704c`, detached HEAD auf `0b1704c…`. `git status --porcelain=v1` war **vor und nach** allen Prüfungen leer; HEAD blieb unverändert. Keine Datei des Kandidaten wurde verändert.
- **Toolchain:** macOS 15.8.1/arm64; Temurin Java 21.0.7+6-LTS; Gradle Wrapper 9.8.2; Python 3.9.6 mit `sys.flags.optimize == 0`; Playwright 1.60.0 Chromium+Firefox; Docker 29.8.2, Server **linux/arm64**; GraalVM **25.0.3** unter `/Users/stefan/.sdkman/candidates/java/25.0.3-graal`; Quarkus 3.40.1.1, Lucene 10.6.0 aus unverändertem `build.gradle`. Kein `AGENTS.md` im Repository.

## 2. Eigene Source-/Git-/Consumeranalyse

- **Kandidatenumfang:** `git diff --name-status BASE..CANDIDATE` enthält ausschliesslich:
  - aktive Dokumente `README.md`, `docs/architecture.md` (neu), `docs/operations.md` (neu), `docs/testing.md` (neu), `docs/history/README.md` (neu), `docs/cleanup/STATUS.md`, `docs/cleanup/implementation/C4.md` (neu), `src/test/resources/contract/README.md`;
  - die unveränderte Archivverschiebung von 26 Dateien aus `docs/quarkus-migration/**` nach `docs/history/quarkus-migration/**` plus `docs/legacy-notes.md` → `docs/history/legacy-notes.md`.
  - **Keine** Änderung an Java-/Python-/JS-/CSS-/Template-/Build-/CI-/Deploy-/Daten-/Testdateien; keine Versionsänderung.
- **Archividentität:** eigener Blobvergleich aller **27** historischen Originale gegen `BASE`:
  - 26 Dateien unter `docs/quarkus-migration/**` und `docs/legacy-notes.md`;
  - alle `base_blob == candidate_blob`; 27/27 **OK**, keine fehlende/geänderte Datei. Vollständige Tabelle: `/tmp/kartenkatalog-c4-review-0b1704c/evidence/archive-identity.txt`.
  - Der frühere Release-`FAIL` bleibt in `reviews/RELEASE-initial-review.md` erhalten; die drei SODATA-PNGs, Prompts, Status, `OPERATIONS.md`, alle P0–P5-Implementierungen und Reviews sind bytegleich.
  - Historische lokale Links: 25/25 auflösbar (inkl. `../reviews/...`, `../OPERATIONS.md`, Referenzbilder); aktive Links: 45/45 auflösbar, inklusive Heading-Anker. Der alte `docs/quarkus-migration/`-Pfad wird in aktiven Dokumenten nicht mehr verwendet; die neue Pfadzuordnung steht in `docs/history/README.md`.
- **Geschützte Daten/Fixtures:** Harvest-SHA256 `de40f025…3de86`, `data/README.md` `374451…4736c`, `public-api.json` `cd1167f…f219b`, `integration/catalog.xml` `4f2e54…c510`; 169 `<k:Thema>` / 886 `<k:Ebene>`; keine Änderung.
- **C2-Parität:** `baseline.json` und `public-api.json` haben denselben Git-Blob `f193170…`; `final/catalog.xml` und `integration/catalog.xml` denselben Blob `1772f15…`; `with-qml.xml` unverändert. `MockCatalogRepository` ist vorhanden. Alte Scriptnamen `p0-*`, `p1-*`, `p2-*`, `p4-*` existieren nicht mehr; die vier neuen Harnesse sind vorhanden.
- **Aktive Namen/Abhängigkeiten:** Scan über aktive `src`, `scripts`, `.github`, `build.gradle`, `deploy`, aktive Doku und `README` findet keine phasenbasierten **ausführbaren** Namen (`p0-…`, `p1-…`, `p2-…`, `p4-…`, `BaselineContractTests`, `BaselineQmlTests`, `P3ITTest`, `build/reports/p*`). Historische Berichte/Archive sind ausgenommen. `./gradlew dependencies` zeigt Quarkus 3.40.1.1, Lucene 10.6.0, Jsoup; keine Spring-/JTE-/JDBC-/PostgreSQL-/DuckDB-Laufzeit. Keine FSDirectory/MMapDirectory/Dateipersistenz, keine DB-Konfiguration.
- **Root-/Ignore-Zustand:** die drei Root-Artefakte sind aus dem Tree und Dateisystem entfernt; nur `data/kartenkatalog-harvest-ohne-qml.xtf` ist als XTF getrackt. `git check-ignore -q --no-index`:
  - ignoriert: `data/anderes.xtf`, `.env`, `build/x`, `.gradle/x`, `.idea/x`, `__pycache__/x.pyc`;
  - **nicht** ignoriert: die erlaubte Harvest-XTF, `src/main/resources/application.properties`, `gradlew`, `gradle/wrapper/gradle-wrapper.jar`.
  - Belege: `/tmp/kartenkatalog-c4-review-0b1704c/evidence/t0-summary.txt`.
- **Doku-Ist-Abgleich:** README/Architektur/Betrieb/Testing/History/Daten-/Contract-README wurden gegen Source, Build, CI, Dockerfiles, Workflow und tatsächliche Befehle geprüft. Versionen, Routen, DTO-/QWC-Namen, Null-/Array-Semantik, Ranking/Facet-Regeln, XTF-Default, Notice-Escaping, Native-/Container-Grenzen, acht Tags, Main-only-Publish und Mirror-Wirkung stimmen überein. Die einzige formale Abweichung ist F1 (`git diff --check`).

## 3. Jede Muss-AC einzeln

| AC-ID | erfüllt / verletzt / nicht prüfbar | Eigener Code-/Test-/Prozessbeleg | Finding-ID |
|---|---|---|---|
| **AC0-01** | **erfüllt** | BASE/CANDIDATE per `cat-file`/`rev-parse`; separater Worktree; tracked status vor/nach leer; `git ls-files` 147; CI `main.yml` mit Main-only-Publish, `mirror.yml` mit Codeberg-Push; Toolchain real erfasst. C0-Review auf `1378d3f…` bestätigt denselben Umfang. | — |
| **AC0-02** | **erfüllt** | C0-Baum `1378d3f…`: drei Root-Artefakte vorhanden (DuckDB-ZIP mit `kartenkatalog.duckdb`, XTF-ZIP mit altem XTF 2.3, PNG 1500×950); keine aktiven Consumer ausserhalb der Prozessdokumente. Aktuelle Harvest `de40f025…`; Contract-Bytes `f193170…`; kein Löschbeweis nur über GitHub-Suche. | — |
| **AC0-03** | **erfüllt** | C0-Review dokumentiert frische T1 62/0/0/0 + Paket 3/0/0/0, T2/T3/T5 und ausdrücklich **kein** Native-Gate. C4 frisch: 66/0/0/0 + 3/0/0/0, Runtime/Dev/Index und 14/0/0/0; keine erfundene Native-Abnahme. | — |
| **AC0-04** | **erfüllt** | `git ls-tree`/`git grep` über C0- und aktuellen Baum: `P3ITTest` abwesend; aktive Testklassen fachlich benannt; keine erfundene Löschklasse; Phasenscan aktuell ohne aktive Treffer. | — |
| **AC0-05** | **erfüllt** | `docs/cleanup/reviews/C0-review.md` ist ein echter unabhängiger PASS auf `1378d3f5…`, mit eigener Golden-Contract-Mutation (Original 0, mutiert 1) und Referenzketten-Canary; Blob in BASE und Candidate identisch `db0c32f…`. | — |
| **AC1-01** | **erfüllt** | C1-Kandidat `b3df508…` löscht die drei Root-Artefakte; aktueller Tree/Dateisystem ohne sie; `git grep` findet keinen aktiven Consumer; Git-Objekte bleiben in der Historie; kein History-Rewrite/Force-Push; App startet ohne sie. | — |
| **AC1-02** | **erfüllt** | Nur die Harvest-XTF ist getrackt; SHA256 `de40f025…`, Modell `SO_AGI_Kartenkatalog_Publikation_20251210`, 169/886; `data/README.md` und Fixtures unverändert. | — |
| **AC1-03** | **erfüllt** | `.gitignore` projektspezifisch; positive/negative `check-ignore`-Canaries (siehe §2); `.dockerignore`-Whitelist passt zu JVM-/Native-Dockerfiles; JVM-/Native-Image-Builds erfolgreich. | — |
| **AC1-04** | **verletzt** | JVM-/API-/Prozess-Teil erfüllt: T1 66/0/0/0 + 3/0/0/0, T2 Runtime/Dev/Index grün, keine Testabschwächung. **Aber** `git diff --check ec57d04… 0b1704c…` endet mit **Exit 2**: `docs/architecture.md:61: new blank line at EOF.` Der kumulative Abschlussdiff ist damit nicht sauber. | **F1** |
| **AC1-05** | **erfüllt** | `docs/cleanup/reviews/C1-review.md` ist ein echter PASS auf `b3df508…` mit eigener Ignore-/Fixture-Gegenprobe und realer JVM-/API-Probe; Blob `b55d915…` in BASE/Candidate unverändert. | — |
| **AC2-01** | **erfüllt** | Nur `scripts/{api-http,runtime-process,search-index,distribution,versioning,release,verify-published-images}-contract.py` bzw. `verify-published-images.py`; alte `p0/p1/p2/p4`-Dateien fehlen; CI/Doku rufen die neuen Pfade; `build/reports/runtime`, `search-index`, `browser/catalog`, `browser/filters`, `distribution` werden erzeugt, keine `build/reports/p*`. | — |
| **AC2-02** | **erfüllt** | Runtime-/Index-/Distributionsharness behalten Assertions, Negativfälle, Timeouts/Prozessgruppen-Cleanup und CLI-Optionen; `runtime-process-contract.py` nutzt `sys.executable` + `api-http-contract.py`; Distribution nutzt `integration/catalog.xml`; eigener API-Golden-Mutationscanary Exit 1. | — |
| **AC2-03** | **erfüllt** | `CatalogApiContractTests`/`CatalogQmlContractTests` ersetzen die Baseline-Klassen; `public-api.json`/`baseline.json` Blob `f193170…`; `integration/catalog.xml`/`final/catalog.xml` Blob `1772f15…`; 66/3 Tests, 0 Failures/Errors/Skips; `MockCatalogRepository` erhalten. | — |
| **AC2-04** | **erfüllt** | `playwrightTest` 14/0/0/0: `CatalogBrowserTests` 6, `CatalogFilterBrowserTests` 8; Chromium **und** Firefox, 1440×900/390×844; Screenshots unter `build/reports/browser/catalog/` und `browser/filters/`; No-JS/History/Fokus/Fehler/XSS geprüft. | — |
| **AC2-05** | **erfüllt** | Struktureller CI-Audit: `linux-native: needs: jvm`; alle JVM-/Browser-/Runtime-/Dev-/Index-Gates vor Native/Publish; gepinnter Mandrel-Builder; `--platform linux/amd64`, `--expected-platform linux/amd64`, `--expected-revision "$GITHUB_SHA"`; Login/Publish/Verify nur Main-Push/Dispatch; zwei Vierer-Tagloops = acht Tags; Mirror auf Push. Kein Push ausgelöst. | — |
| **AC2-06** | **erfüllt** | Frische T1/T2/T3/T5 in C4; echte Exitcodes und XML-Zahlen; Runtime 8 erwartete Fehlstarts Exit 1; Indexfehlstart Exit 1; mindestens eine absichtlich fehlschlagende Probe (API-Golden-Mutation Exit 1); keine Publikation. | — |
| **AC2-07** | **erfüllt** | C2-Review ist echter PASS auf `c93714d…` mit eigenem Rename-/CI-Scan und HTTP-Mutationscanary; eigener C4-Probe-1-Workflow-/Doku-/Golden-Canary bestätigt die Empfindlichkeit erneut. | — |
| **AC3-01** | **erfüllt** | Nur `application.properties` definiert den XTF-Default; `CatalogConfig` ohne `@WithDefault`; Default-`file:`-Start, `CATALOG_XTF_LOCATION`/Property-Override, `classpath:`-Test und fehlende/`https:`-Quelle mit Exit 1 vor Ready real geprüft. | — |
| **AC3-02** | **erfüllt** | Ohne Notice kein `.provenance`-Absatz und kein Harvest-Satz auf `/`, `/thema`, Themenseite, Detail; gesetzte Notice auf allen vier Vollseiten sichtbar; gefährliche `<img onerror>`/`<script>`-Notice escaped, kein ausführbarer Knoten; T1 und eigener Probe 2. | — |
| **AC3-03** | **erfüllt** | Alternative XTF (3 Ebenen, nicht Harvest) ohne falsche Harvest-Behauptung; Suche/Filter/Facets/API/Detail unabhängig; JVM/Native-Payloads und QML byte-/hashgleich; eigene Probe 2. | — |
| **AC3-04** | **erfüllt** | C3-Diff enthält keine Klassenlöschung; `CatalogStore`/`Repository`/`Snapshot`, `MockCatalogRepository`, `LuceneNativeConfiguration`, Sanitizer, DTOs, Fixtures, Lizenzen und Native-Hinweise erhalten; keine neue Persistenz. | — |
| **AC3-05** | **erfüllt** | Frische T1 66/3, T2 Runtime/Dev/Index, T3 14 Browser, T4 3 Native-Paket + 20 lokale + 20 Linux-Distributionsfälle; XSS-/Alternativ-XTF-/Missing-Source-Fälle; keine Versions-/DTO-/Rankingänderung. | — |
| **AC3-06** | **erfüllt** | C3-Review auf `f2d85d7…` mit eigenem neuen XSS-/Alternativ-XTF-/Classpath-/`@CheckedTemplate`-Negativtest; eigener C4-Probe 2 auf `0b1704c…` mit alternativer XTF, gefährlicher Notice und JVM/Native-Parität. | — |
| **AC4-01** | **erfüllt** | README/Architektur/Operations/Testing/History/Daten-/Contract-README mit aktuellen Versionen, Routen, Befehlen, XTF-/Lucene-/QWC2-/Releasegrenzen; 42 Links der sieben aktiven Dokumente und 45 inkl. STATUS gültig; keine veralteten aktiven Script-/Testnamen. | — |
| **AC4-02** | **erfüllt** | 27/27 historische Originale bytegleich gegen BASE; früherer FAIL, spätere Reviews, OPERATIONS, Prompts, STATUS, drei Referenzbilder und `legacy-notes.md` erhalten; keine historische Datei editiert. | — |
| **AC4-03** | **erfüllt** | 45 aktive lokale Links/Anker und 25 historische lokale Links auflösbar; Link-/CI-Canary erkennt absichtlich falschen aktiven Pfad mit Exit 1; alte Archivpfade transparent im History-Index; Report-/Script-/Fixture-Pfade real vorhanden. | — |
| **AC4-04** | **erfüllt** | Root-Artefakte weg; aktive Java-/Script-/Test-/CI-Namen phasenfrei; keine Spring-/JTE-/JDBC-/PostgreSQL-/DuckDB-Abhängigkeit oder neue Persistenz; historische Belege ausgenommen; `git ls-files` 147. | — |
| **AC4-05** | **erfüllt** | T1 66/0/0/0 + 3/0/0/0; T2 Runtime/Dev/Index; T3 14/0/0/0 Chromium+Firefox; XML-Zahlen und Rohbelege gesichert; negative Parser-/Index-/XSS-/API-/CORS-/HTMX-/Browserfälle; keine Skips. | — |
| **AC4-06** | **erfüllt** | Frischer macOS/arm64-`testNative` 3/0/0/0; frischer Linux/arm64-Mandrel-Native-Build (ELF aarch64); neue JVM-/Native-Linux/arm64-Images mit Revision = vollem Candidate-SHA; 20 lokale + 20 Linux-Distributionsfälle; API/Suche/Ranking/Facets/QML/HTML/HTMX, Readiness, Read-only, UID `1000730000:0`, tmpfs, kein Indexvolume; **kein** amd64-Erfolg behauptet. | — |
| **AC4-07** | **erfüllt** | `versioning-contract.py` 4 gültige + 5 abgewiesene Laufnummern; `release-contract.py` 6/0/0/0 Fake-Registry inkl. falscher Tags/Digests/Plattformen; CI-Struktur-/Trigger-/Tag-Audit; absichtlich fehlschlagende Pfad-/API-Proben Exit 1; keine Docker-Hub-/Codeberg-Publikation; Main-Push-Wirkung dokumentiert. | — |
| **AC4-08** | **erfüllt (Review durchgeführt; Gesamtfreigabe verweigert)** | Kumulativer Review aller 31 ACs auf exakt `0b1704c…`; zwei eigene neue Gegenproben (Probe 1 und 2); Native-/CI-Belege; wegen **F1** wird kein PASS erteilt. | **F1** |

## 4. Selbst entwickelte Gegenprobe(n)

### 4.1 Probe 1 – CI-/Doku-/Testharness-Pfadfalle (eigene Probe, nicht Codex)

- **Ziel/Hypothese:** Der neue CI-/Doku-/Harness-Prüfpfad muss einen absichtlich gebrochenen Script-/Doku-/Golden-Pfad rot machen, während der Originalkandidat grün bleibt.
- **Eingaben und Herkunft:**
  - Original: `.github/workflows/main.yml` und aktive Dokumente des Kandidaten.
  - Mutation A: temporäre Workflowkopie mit `scripts/search-index-contract.py` → `scripts/nonexistent-search-contract.py`.
  - Mutation B: temporäre Doku-Kopie mit `README.md`-Link `docs/testing.md` → `docs/nonexistent-testing.md`.
  - Mutation C: temporäre Kopie von `scripts/api-http-contract.py` und `public-api.json`, mit `layer.qwcLayerName = BROKEN-MUTATED-QWC-LAYER-NAME`, gegen eine **echte, selbst gestartete JVM** aus dem Kandidaten-JAR.
- **Erwartung:** Original jeweils Exit 0; Mutationen jeweils Exit ≠ 0.
- **Tatsächlich:** `workflow_original=0`, `workflow_mutated_missing_script=1`; `docs_original=0`, `docs_mutated_missing_link=1`; `api_original=0`, `api_mutated_golden_field=1`. Gesamt **Exit 0** (alle Erwartungen erfüllt).
- **Warum nicht trivial:** echte JVM, echte Golden-Bytes, zwei unabhängige Fehlerklassen (Pfad vs. fehlender Gate-Text) und ein realer HTTP-Harness; keine blosse String-Wiederholung.
- **Rohbelege:** `/tmp/kartenkatalog-c4-review-0b1704c/logs/probe1.log`, `evidence/probes/probe1/results.json`, `api-original.log`, `api-mutated.log`, `main-mutated.yml`.

### 4.2 Probe 2 – alternative XTF, gefährliche Notice, JVM/Native-Parität

- **Ziel/Hypothese:** Eine **alternative** lokale XTF darf keine Harvest-Provenienz vortäuschen; eine gefährliche Notice muss auf allen Vollseiten escaped bleiben; Suche, Filter, Facets, API und QML müssen zwischen frisch gebauten JVM- und Native-Linux/arm64-Images identisch sein.
- **Neue Eingabe und Herkunft:** In `/tmp/…/probes/probe2/source/catalog.xtf` aus `src/test/resources/integration/catalog.xml` abgeleitet (Kandidat unverändert): Titel `Kumulativ Gewässersonde`, Keywords `Kumulativ, Sonde`, Synonym `Bach`, Beschreibung `Sondenbeschreibung Bach Kumulativ`, QML `<qgis version="3.40"><renderer-v2 type="singleSymbol"/></qgis>`; Notice exakt `<img src=x onerror=alert(1)> & <script>alert(2)</script>`.
- **Fälle:** 3 Ebenen/2 Themen; Suche `Kumulativ` (Titel+Keyword), `Sondenbeschreibung` (Beschreibung), Filter `thema=natur&typ=datasetview`, Facets `q=Kumulativ`; vier Vollseiten `/`, `/thema`, `/themen/natur/ebenen`, `/ebene/ch.final.karte`; HTMX-Fragment; QML; Detailseite; Container-Read-only/UID/tmpfs/Mounts. Beide Images mit `--user 1000730000:0`, `--read-only`, tmpfs `/tmp`, read-only `/catalog`, keine Volumes.
- **Ergebnis:** JVM **Exit 0**, Native **Exit 0**. Suche 2 Treffer (erster `ch.final.gewaesser`), Beschreibung 1 Treffer, Filter 1 Treffer, Facets `natur=2`/`planung=0`, Typ `datasetview=1`/`extwms=1`. Keine Harvest-Behauptung, keine rohe `<img … onerror>`-/`<script>alert(2)</script>`-Ausgabe; die escapte Notice ist auf allen Vollseiten vorhanden. QML exakt 62 Bytes. 13 kanonische API-/HTML-/QML-Hashes JVM == Native; Container-Inspektion `User=1000730000:0`, `ReadonlyRootfs=true`, nur `/tmp`-tmpfs, keine RW-Mounts. Beide Container entfernt.
- **Warum nicht trivial:** echte alternative XTF, echte gefährliche Konfiguration, vollständige Cross-Layer-Kette und echte JVM-/Native-Containerparität; hätte eine Harvest-only- oder nicht escapende Implementierung entlarvt.
- **Rohbelege:** `/tmp/kartenkatalog-c4-review-0b1704c/logs/probe2.log`, `evidence/probes/probe2/results.json`, `source/catalog.xtf`.

## 5. Pflichtgates und Rohbelege

| Gate | Kommando | Exit | Tests / Failures / Errors / Skips | Report | SHA-Bindung |
|---|---|---|---|---|---|
| T0 Git/Archiv/Links | eigener Blob-, Link- und `git diff --check`-Lauf | Archiv 27/27 OK; Links 45/0 und 25/0; **diff-check 2** | — | `evidence/archive-identity.txt`, `linkcheck-active.txt`, `linkcheck-history.txt`, `diff-check.txt` | `0b1704c…` |
| T0 Root/Ignore | `git ls-files`, `git check-ignore -q --no-index` | 0 wie erwartet | Root-Artefakte absent; Ignore-Canaries korrekt | `evidence/t0-summary.txt` | `0b1704c…` |
| T1 JVM/API | `./gradlew clean test quarkusBuild quarkusIntTest --console=plain` | **0** | **66/0/0/0** + **3/0/0/0** | `logs/jvm.log`, `evidence/t1/test-results`, JAR `evidence/t1/quarkus-run.jar` | `0b1704c…` |
| T2 Runtime | `python3 scripts/runtime-process-contract.py` | **0** | 1 Gutstart; **8** erwartete Fehlstarts Exit 1 | `logs/runtime.log`, `evidence/t2/runtime` | `0b1704c…` |
| T2 Dev/CORS | `python3 scripts/runtime-process-contract.py --dev` | **0** | OPTIONS/GET 200 für erlaubten Origin; 403 für fremden; keine Credentials | `logs/runtime-dev.log`, `evidence/t2/runtime` | `0b1704c…` |
| T2 Index | `python3 scripts/search-index-contract.py` | **0** | Harvest 886; 12/12/12; changed; Indexfehlstart Exit 1; keine Indexdateien | `logs/search-index.log`, `evidence/t2/search-index` | `0b1704c…` |
| T3 Browser | `./gradlew playwrightTest --console=plain` | **0** | **14/0/0/0**; Chromium+Firefox; 1440×900/390×844 | `logs/browser.log`, `evidence/t3` | `0b1704c…` |
| T4 macOS Native | `GRAALVM_HOME=…/25.0.3-graal ./gradlew clean testNative -Dquarkus.native.enabled=true -Dquarkus.package.jar.enabled=false -Dquarkus.native.native-image-xmx=4g` | **0** | **3/0/0/0** `PackagedCatalogIT`; Mach-O arm64 | `logs/native-macos.log`, `evidence/t4-macos` | `0b1704c…` |
| T4 lokale Distribution | `python3 scripts/distribution-contract.py --native-executable …/application --report-dir …/local-distribution` | **0** | **20** Fälle (10 je JVM/Native); API/Suche/Facets/QML/HTML/HTMX/Fehlstarts | `logs/local-distribution.log`, `evidence/local-distribution/summary.json` | `0b1704c…` |
| T4 Linux Native-Build | `./gradlew quarkusBuild -Dquarkus.native.enabled=true -Dquarkus.package.jar.enabled=false -Dquarkus.native.container-build=true -Dquarkus.native.container-runtime=docker -Dquarkus.native.builder-image=quay.io/…@sha256:29a5… -Dquarkus.native.native-image-xmx=4g` | **0** | ELF 64-bit ARM aarch64, SHA256 `6448bf…` | `logs/native-linux.log`, `evidence/t4-linux` | `0b1704c…` |
| T4 Linux Images | `docker build --platform linux/arm64 … VERSION=0.2.LOCALBUILD REVISION=<CANDIDATE> Dockerfile.jvm/.native` | **0** | JVM/Native `linux/arm64`, `USER 1001:0`, Revision = `0b1704c…` | `logs/docker-jvm.log`, `logs/docker-native.log` | `0b1704c…` |
| T4 Linux Distribution | `python3 scripts/distribution-contract.py --jvm-image … --native-image … --expected-version 0.2.LOCALBUILD --expected-revision 0b1704c… --expected-platform linux/arm64 --report-dir …/linux-distribution` | **0** | **20** Fälle; Read-only/UID `1000730000:0`/tmpfs/kein Index; API/Suche/Ranking/Facets/QML/HTML/HTMX; Startupversion | `logs/linux-distribution.log`, `evidence/linux-distribution/summary.json` | `0b1704c…` |
| T5 Versionierung | `python3 scripts/versioning-contract.py` | **0** | 4 gültige, 5 ungültige Run-Nummern abgewiesen | `logs/versioning.log` | `0b1704c…` |
| T5 Release/Fake-Registry | `python3 scripts/release-contract.py` | **0** | 6/0/0/0; acht Tags, falsche Tags/Digests/Plattformen abgewiesen | `logs/release.log` | `0b1704c…` |
| T5 CI-Struktur | eigener Workflow-Audit + Probe 1 | **0** (Original), Mutationen 1 | `needs: jvm`, gepinnter Builder, eine amd64-Plattform, `--expected-revision`, 3× Main-only, 8 Tags, Mirror | `evidence/probes/probe1` | `0b1704c…` |
| Abhängigkeiten | `./gradlew dependencies --configuration runtimeClasspath --console=plain` | **0** | Quarkus 3.40.1.1/Lucene 10.6.0; keine DB/Spring/JTE | `logs/dependencies.log` | `0b1704c…` |
| Eigene Probe 1 | `python3 /tmp/…/probe1.py` | **0** | Original 0/0/0; Mutationen 1/1/1 | `evidence/probes/probe1/results.json` | `0b1704c…` |
| Eigene Probe 2 | `python3 /tmp/…/probe2.py` | **0** | JVM/Native je 12+ Prüfungen; 13 Hash-Paritäten; Container clean | `evidence/probes/probe2/results.json` | `0b1704c…` |
| **Diff-Hygiene** | `git diff --check ec57d04… 0b1704c…` | **2** | `docs/architecture.md:61: new blank line at EOF.` | `evidence/diff-check.txt` | `0b1704c…` |

**Nicht ausgeführte fällige Prüfungen:** keine fachlich fällige Prüfung wurde ausgelassen. **Nicht** ausgeführt und ausdrücklich **nicht** als Erfolg behauptet: Linux/amd64-Native-/Containerlauf (lokale Engine ist linux/arm64; der Releaseworkflow zielt amd64). Native-Browsermatrix (`-PcatalogExecutable`) ist optional und wurde nicht als Pflichtgate behauptet.

**Native-/Linux-/Containerarchitektur und echte Ausführung:**

- `testNative`: **macOS/arm64**, Mach-O 64-bit executable arm64, SHA256 `0e24d82b…`, 3/0/0/0.
- Linux-Native: **linux/arm64**, ELF 64-bit LSB ARM aarch64, SHA256 `6448bfcd…`, gepinnter Mandrel-25-Builder; JVM-/Native-Images `linux/arm64`, Labels `org.opencontainers.image.revision = 0b1704c…`, Version `0.2.LOCALBUILD`.
- **Kein linux/amd64-Erfolg** behauptet. Die Releasepipeline ist separat als amd64-Ziel geprüft, nicht als lokaler Kandidatenlauf.

**Release-/Tag-/Triggerprüfung ohne Push:** `main.yml` und `mirror.yml` statisch und semantisch geprüft; Login/Publish/Verify ausschliesslich `refs/heads/main` + `push`/`workflow_dispatch`; PR ohne Publish; zwei Vierer-Tagloops; Digest-/Pull-/Runtime-Gates vor Publish; Mirror auf Push. Kein `git push`, Merge, Tag, `workflow_dispatch`, Docker-Hub-/Codeberg-Push, Registry-Upload oder Release ausgelöst. Eigene lokale Images wurden nach dem Test entfernt.

## 6. Findings

### F1 – `git diff --check` des finalen Kandidaten nicht sauber (abnahmerelevant, niedrige Schwere)

- **AC-Bezug:** **AC1-04** (Klausel „`git diff --check` sauber“); deshalb keine C4-Gesamtfreigabe.
- **Pfad:** `docs/architecture.md:61`.
- **Reproduktion:**
  ```sh
  cd /Users/stefan/sources/kartenkatalog-cleanup-C4-review-0b1704c
  git diff --check ec57d04b8688617ddfe3a36070f23d15459fce30 0b1704c72113a9872a10cf68110f506c6592a547
  ```
- **Erwartet:** Exit **0** (Plan §3.1 Schritt 6; Reviewprompt §1.1).
- **Tatsächlich:** Exit **2**, Ausgabe:
  ```text
  docs/architecture.md:61: new blank line at EOF.
  ```
- **Ursache:** Die neue Datei endet mit zwei `\n` (eine zusätzliche Leerzeile am Dateiende); die anderen in diesem Lauf geprüften aktiven Dokumente enden mit genau einem `\n`.
- **Erforderliche Nacharbeit:** Die zusätzliche Leerzeile am EOF in `docs/architecture.md` entfernen (Datei mit genau einem finalen Newline), neuen Kandidaten committen und den C4-Review auf dem neuen SHA wiederholen. Es sind **keine** Produkt-, Test-, Native- oder CI-Änderungen erforderlich; der aktuelle Kandidat wird durch den Reviewer nicht verändert.
- **Abgrenzung:** Die früheren Archivierungscommits mit erhaltenen Markdown-Hardbreak-Leerzeichen sind wegen der byteweisen Originaltreue separat zu behandeln; die zusätzliche Leerzeile in der neuen aktiven Architekturdatei hat keine solche Rechtfertigung.

**Keine weiteren funktionalen, Test-, CI-, Fixture-, Provenienz-, XSS-, Native-, Container- oder Release-Findings.** **Blocker nach INFRA / MISSING_EVIDENCE / SCOPE_DECISION:** keine.

## 7. Urteil

**FAIL**

- **Begründende Muss-Kriterien:** **AC1-04** ist wegen des tatsächlichen `git diff --check`-Exit 2 (F1) nicht erfüllt. Damit ist auch die kumulative Cleanup-Freigabe (AC4-08) nicht erteilt. Alle übrigen geprüften ACs, T1–T5 und die zwei eigenen neuen Gegenproben sind belegt und erfolgreich; es gibt keinen Native-, Test-, Archiv-, Link-, Provenienz- oder Releasefehler.
- **Nächster zulässiger Schritt:** Codex entfernt ausschliesslich die zusätzliche Leerzeile am EOF in `docs/architecture.md`, committed einen neuen Kandidaten und lässt den vollständigen C4-Review auf dem neuen SHA erneut durchführen. Danach ist ein PASS möglich; die volle T1–T5-Prüfung muss auf dem neuen SHA wiederholt werden.
- **Grenze:** Dieser Review ist **keine** Freigabe für einen Push nach `main`, einen Merge, ein Tag, einen Workflow-Dispatch, eine Docker-Hub-/Codeberg-Veröffentlichung oder ein Release. Der Kandidat blieb tracked-sauber; HEAD ist unverändert `0b1704c72113a9872a10cf68110f506c6592a547`.

---

**Geprüfter Kandidat:** `0b1704c72113a9872a10cf68110f506c6592a547`  
**Geprüfte Basis:** `ec57d04b8688617ddfe3a36070f23d15459fce30`  
**Review-Worktree:** `/Users/stefan/sources/kartenkatalog-cleanup-C4-review-0b1704c` (tracked clean vor/nach; HEAD unverändert)  
**Rohbelege:** `/tmp/kartenkatalog-c4-review-0b1704c/`  
**Reviewbericht:** `/Users/stefan/sources/kartenkatalog-C4-review-0b1704c.md`
