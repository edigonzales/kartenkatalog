# Unabhängiger Review C0 – Kartenkatalog Cleanup

## 1. Identität und Umgebung

- **Lieferabschnitt:** C0 – Bestandsaufnahme/Schutz der bestehenden Verträge. **PHASENGATE: nein.**
- **Repository:** `edigonzales/kartenkatalog`
- **BASE_SHA:** `377fa52bbfcb3026ce77c64c7db78e6163be0b4c`
- **CANDIDATE_SHA:** `1378d3f5b2bb7a210efa5f704c94a4e43ba23643` (Commit, Kind von BASE; im Review-Worktree per `git rev-parse HEAD` verifiziert)
- **Review-Worktree:** `/Users/stefan/sources/kartenkatalog-cleanup-C0-review-1378d3f`, detached HEAD. `git status --porcelain=v1` war vor und nach allen Prüfungen leer; der Kandidaten-HEAD blieb unverändert. Die GUI-Session trägt als `directory` den Haupt-Checkout `/Users/stefan/sources/kartenkatalog`; alle Review-Kommandos, Tests und Proben liefen jedoch ausschliesslich mit cwd im separaten Review-Worktree. Beide Checkouts standen auf demselben Kandidaten-SHA und waren danach tracked-sauber.
- **Kandidaten-Diff:** ausschliesslich `A docs/cleanup/STATUS.md` und `A docs/cleanup/implementation/C0.md` (134 Zeilen, 0 Löschungen, keine Produkt-/Test-/Build-/CI-/Datenänderung). `git diff --check` Exit 0.
- **OpenCode-Identität (nicht Modell-Selbstauskunft):**
  - GUI-App: **OpenCode 2.1.4** auf macOS. Belege: laufender Dienst `/Users/stefan/Library/Application Support/ai.opencode.desktop/cli/2.1.4-arm64/opencode-cli serve --service`, Crashpad-Annotation `_version=2.1.4`, Session-Datensatz `session_v2.version = 2.1.4`.
  - Aktuelle Session: `ses_e69905fe2ffekFoiydSmF5Mhwj`, Titel „Unabhängige Prüfung von C0“, `agent=build`, `model={"id":"deepseek-flash","providerID":"deepseek","variant":"high"}`.
  - Provider: **DeepSeek**, direkter Provider-Endpunkt `https://api.deepseek.com` (Modellkatalog) bzw. `https://api.deepseek.com/v1` (Provider-Route im App-Bundle), npm `@ai-sdk/openai-compatible`. `auth.json` enthält einen `deepseek`-Eintrag; **keine Credentialwerte gelesen oder ausgegeben**.
  - Modellkatalog: `deepseek/deepseek-flash` = **„DeepSeek V4.1 Flash“**, Familie `deepseek-flash`, `release_date 2026-09-10`. Die im Auftrag genannte Endung „1017“ ist in Session-/Provider-/App-Metadaten nicht separat exponiert; die tatsächlich gewählte Kennung ist eindeutig `deepseek/deepseek-flash`, Variante `high`. Es wurde keine Modell-Selbstauskunft als Identitätsbeleg verwendet.
  - Hinweis: Das im PATH liegende CLI-Binary meldet `opencode v2.0.28`; der für diesen Review tatsächlich laufende GUI-Kontext ist die App 2.1.4 (Session-Metadaten und Prozesspfad). Beides ist OpenCode 2.x; die GUI-/Session-Metadaten sind für die Review-Identität maßgeblich.
- **Toolchain:** macOS 15.8.1 / arm64; Temurin Java 21.0.7+6-LTS; Gradle Wrapper 9.8.2; Python 3.9.6 mit `sys.flags.optimize == 0`; Playwright-Java 1.60.0 mit Chromium und Firefox; Docker Client/Engine 29.8.2, Docker Desktop 4.94.0 (Server linux/arm64); Oracle GraalVM 25.0.3+9.1 unter `/Users/stefan/.sdkman/candidates/java/25.0.3-graal/bin/native-image`; kein `podman` im PATH.
- **Zielarchitektur:** C0 erhebt nur die lokale macOS-/JVM-/Browser-Baseline. Es wurde **kein** Native-/Linux-Container-Gate ausgeführt oder behauptet; das ist in C0 nicht fällig und bleibt C4 vorbehalten.

## 2. Eigene Source-/Git-/Consumeranalyse

### 2.1 Ist/Soll und Kandidatenumfang

- Der Kandidat enthält nur den C0-Bericht und `STATUS.md`. Die Produkt-, Test-, Build-, CI- und Datenbäume sind gegenüber BASE unverändert. Damit sind die frisch ausgeführten Tests zugleich Baseline-Tests des Produktbaums.
- `git ls-files`: BASE 135 Dateien, Kandidat 137 Dateien (die zwei neuen Markdown-Dateien). Produktions-Java 33, Test-Java 14 (inkl. `MockCatalogRepository`), Integrationstest-Java 1, Python-Scripts 7. Diese Zahlen stimmen mit dem C0-Bericht überein.
- `docs/cleanup/STATUS.md` steht korrekt auf `C0`, `AWAITING_REVIEW`, letzter unabhängiger Cleanup-PASS `none`; der historische P5-PASS wird nicht als Cleanup-PASS ausgegeben.

### 2.2 Root-Artefakte, XTF-Orte und Fixtures

| Prüfung | Ergebnis |
|---|---|
| `kartenkatalog.duckdb.zip` | vorhanden; `unzip -l`: nur `kartenkatalog.duckdb`, 21'245'952 Byte, 14.03.2025. Kein aktiver Consumer. |
| `kartenkatalog.xtf.zip` | vorhanden; `unzip -l`: nur `kartenkatalog.xtf`, 18'579'582 Byte. Header: XTF **2.3**, Modell `SO_AGI_Kartenkatalog_20250311`, Sender `ili2duckdb-5.2.2-…`; SHA256 der entpackten Datei `9777d5561de03530f3082826bee25f1f6c62ab06aaa9b717ca9c326741f233e2`. |
| `mockup_C_tree_clean.png` | vorhanden; kein aktiver Asset-/Doku-Consumer. Historische SODATA-Bilder liegen separat unter `docs/quarkus-migration/reference/`. |
| `data/kartenkatalog-harvest-ohne-qml.xtf` | aktive Fixture; XTF **2.4**, Modell `SO_AGI_Kartenkatalog_Publikation_20251210`; 169 `<k:Thema>` und 886 `<k:Ebene>`; SHA256 `de40f025368ae6fc70f5858ab42592488bca837c6942cb6ff8759e6c8803de86` vor und nach allen Tests unverändert. |
| `src/test/resources/contract/baseline.json` | SHA256 `cd1167f8870b5dae021b913c08b7643427683ed406e3018e915cbab4feff219b`; 16 `layerFields`, 9 `themeFields`, 4 `products`, 5 `errors`. |
| `src/test/resources/final/catalog.xml` | SHA256 `4f2e54495e0b852b06e5a35e76763216afa04e6da5b1d4383437c9de732ec510`; 2 Themen / 3 Ebenen (Cross-Layer-Fixture). |

`git grep` nach den drei exakten Rootnamen ausserhalb `docs/quarkus-migration` und `docs/cleanup` liefert Exit 1 (keine Treffer). Die breitere Suche findet nur historische `legacy-notes.md`-Befehle und die ZIP-Binärdatei selbst sowie den unabhängigen PVC-Namen `kartenkatalog-xtf`. Die Nichtverwendung ist damit lokal über Quellen, Build, Scripts, CI, Docker/Deploy und Tests belegt, nicht allein über eine GitHub-Code-Suche.

### 2.3 Aktive Consumers und Referenzketten

- Python-Kette: `p1-process-contract.py` ruft `python3 scripts/p0-http-contract.py` in Zeile 75 auf; `p4-distribution-contract.py` ruft denselben Vertrag über `sys.executable` in Zeile 323 auf. `release-contract.py` importiert `verify-published-images.py`; `versioning-contract.py` ruft die reale Gradle-`printVersion`-Task auf.
- Fixture-Kette: `p4` verwendet Harvest, `search/catalog.xml`, `contract/with-qml.xml` und **`final/catalog.xml`**; `p1` verwendet Harvest und `with-qml.xml` für die Negativmutationen; `p2` verwendet Harvest und `search/catalog.xml`.
- Die p1-Mutationsanker wurden unabhängig geprüft: `SO_AGI_Kartenkatalog_Publikation_20251210`, `ili:ref="test-theme"`, `test-layer`, `85ee5e38-804c-4014-bb9b-490a2a3ea768`, XML-Deklaration und `QML Test` sind in `with-qml.xml` tatsächlich vorhanden und jede Ersetzung verändert den Inhalt. Die Negativfälle sind damit nicht stillschweigend No-Ops.
- CI: `jvm` führt Version/Release-Vertrag, `clean test quarkusBuild quarkusIntTest`, `playwrightTest`, p1, p1 `--dev`, p2 aus. `linux-native` hat `needs: jvm`; danach JVM-Image, `testNative` mit gepinntem Mandrel-Builder, Native-Image und p4 mit `linux/amd64`. Login, Publish und Registry-/Pull-Schritt haben identisch `if: github.ref == 'refs/heads/main' && (github.event_name == 'push' || github.event_name == 'workflow_dispatch')`. Vier Native- und vier JVM-Tags, acht Manifestprüfungen, Pull-/p4-Smoke. PR veröffentlicht nichts; `paths-ignore: '**.md'` nimmt reine Markdown-Pushes vom CI-Lauf aus. `mirror.yml` spiegelt Pushes nach Codeberg.
- Dokumentlink-Prüfung der aktiven Dateien `README.md`, `data/README.md`, `docs/quarkus-migration/OPERATIONS.md`, `src/test/resources/contract/README.md`, `docs/cleanup/STATUS.md`, `docs/cleanup/implementation/C0.md`: 10 lokale Links, 0 gebrochen.
- Externe Codex-Evidenz unter `/Users/stefan/sources/kartenkatalog-C0-evidence/377fa52…/` existiert und ist plausibel: `inventory.z` ist echt NUL-separiert (135 Einträge), `tracked-sha256.txt` hat 135 Zeilen, `junit-counts.json` und Gate-Logs sind vorhanden. Diese Belege wurden nicht als Ersatz für die eigene Testausführung behandelt.
- Historische Blobidentität: C0 verschiebt oder verändert keine historischen Dateien. Die im C0-Bericht genannten P5-/Pre-Publication-/0.2.73-SHAs (`a6ded99…`, `96e266d…`, `f37b195…`) existieren als Commits und sind Vorfahren des Kandidaten; sie wurden nicht als aktueller Kandidatenbeleg verwendet.

### 2.4 Klassennamen/Legacy

- `P3ITTest` existiert weder als Datei noch als aktive Referenz. `git ls-files | grep -i P3ITTest` und `git grep -n P3ITTest` ausserhalb der Cleanup-Prozessdokumente liefern keine Treffer; Treffer stehen nur in `docs/cleanup/CODEX_STARTPROMPT.md`, `EXECUTION_PLAN.md`, `OPENCODE_REVIEW_PROMPT.md` und im C0-Bericht selbst (Prozessdokumente, keine Produktklassen).
- Vorhanden und korrekt als aktive Reste dokumentiert: `BaselineContractTests`, `BaselineQmlTests`, `src/test/resources/final/`, Phase-Labels/Reportpfade `p0`/`p1`/`p2`/`p3a`/`p3b`/`p4`, QML-Testprofil, CSS-Kommentar mit P3b-Bezug. Diese sind bewusst C2/C4 zugeordnet, nicht C0.
- Keine Spring-/JTE-/JDBC-/DuckDB-/PostgreSQL-Imports oder Laufzeitabhängigkeiten im aktiven Java-/Gradle-Code. `runtimeClasspath` enthält Quarkus 3.40.1.1, Qute, REST/Jackson, SmallRye Health, Jsoup und Lucene 10.6.0; ein unscharfer `jte`-Treffer ist kein JTE-Import. Der Parser-Kommentar `ported from XtfCatalogRepository` ist historisch und begründet keinen Löschauftrag.
- `.gitignore`-Ist (nur dokumentiert, nicht C0-Änderung): `.env` ist **nicht** ignoriert; `build/`, `.gradle/`, beliebige `*.xtf` sind ignoriert; die aktive Harvest-Fixture und `gradle/wrapper/gradle-wrapper.jar` sind explizit ausgenommen; `application.properties`, `gradlew` und Testfixtures sind nicht ignoriert. Das ist ein korrekt erkannter C1-Punkt.

## 3. Jede Muss-AC einzeln

| AC-ID | erfüllt / verletzt / nicht prüfbar | Eigener Code-/Test-/Prozessbeleg | Finding-ID |
|---|---|---|---|
| **AC0-01** | **erfüllt** | BASE/CANDIDATE per `git cat-file`/`rev-list` verifiziert; Kandidat ist Kind von BASE; Worktree sauber; 137 getrackte Dateien im Kandidaten (135 in BASE), 33/14/1 Java-Dateien und 7 Scripts selbst gezählt; Toolchain und CI-/Mirror-Publish-Semantik selbst gelesen. Die Consumerkarte deckt Scripts, wechselseitige `subprocess`-Aufrufe, CI, Docker/Deploy, Tests, Fixtures und aktive Doku ab. | — |
| **AC0-02** | **erfüllt** | Drei Root-Artefakte real vorhanden und per `unzip -l`/XML geprüft; altes XTF 2.3/20250311 vom aktiven XTF 2.4/20251210 getrennt; Hashes von Fixture, Golden-JSON und Cross-Layer-XML vor/nach unverändert; 169/886 und 16/9/4/5 selbst ausgezählt; `git grep` Exit 1 für aktive Root-Namen; kein Löschbeweis nur über GitHub. | — |
| **AC0-03** | **erfüllt** | Frische eigene Läufe: T1 62/0/0/0 + Paket 3/0/0/0; Browser 14/0/0/0; p1 0 mit 1 Gutstart + 8 Fehlstarts; p1 `--dev` 0 mit 200/403-CORS; p2 0 mit 886/12-Dokumenten, Neustartdeterminismus, Quellenänderung und echtem Indexfehlstart; Versionierung 0 mit 4+5 Fällen; Release 0 mit 6 Fake-Registry-Tests; Dependencies 0 ohne Legacy-Laufzeit. Native/Container nicht ausgeführt und nicht behauptet. | — |
| **AC0-04** | **erfüllt** | `P3ITTest` nach realem Dateibaum und aktivem Grep abwesend; 33 Produktions-, 14 Test- und 1 Integrationstestdatei einzeln gelistet; aktive Phasenreste (`Baseline*`, `final/`, p0–p4, p3a/p3b) korrekt dokumentiert; keine Legacy-Imports/-Dependencies; kein erfundener Löschauftrag. | — |
| **AC0-05** | **erfüllt** | Unabhängiger Review im separaten Worktree auf exakt `1378d3f…`; eigene neue Golden-Contract-Mutation gegen eine echte JVM-App (Original Exit 0, mutiert Exit 1); zusätzliche Referenzketten-/Mutationsanker-Probe (Exit 0) mit negativer Canary (Exit 1). Keine Codex-Probe kopiert. | — |

## 4. Selbst entwickelte Gegenprobe(n)

### 4.1 Gegenprobe A – Golden-Contract-Feldmutation gegen echte JVM-App

- **Ziel/Hypothese:** Der HTTP-Vertrag `p0-http-contract.py` ist nicht nur gegenüber einem manipulierten Serverfeld (Codex' `qwcLayerName`-Mutation) empfindlich, sondern auch gegenüber einer Änderung der erwarteten Feldmenge im Golden-Contract. Eine solche Mutation würde eine nur oberflächlich grüne Prüfung aufdecken.
- **Neue Eingabe:** Kopie von `scripts/p0-http-contract.py` und `src/test/resources/contract/baseline.json` unter `/tmp/kartenkatalog-c0-golden-QqL1wc/`; im Golden-JSON wurde `layerFields` um das absichtlich nicht existierende Feld `deliberately_absent_field` ergänzt. Die getrackte Datei blieb unverändert.
- **App:** echte JVM-App aus `build/quarkus-app/quarkus-run.jar` mit einer temporären Kopie der Harvest-XTF auf Loopback-Port 54608; `/q/health/ready` wurde abgewartet.
- **Kommando/Ergebnis:**
  - Original-Golden: `python3 …/p0-http-contract.py http://127.0.0.1:54608` → **Exit 0**, „All P0 process contract assertions succeeded.“
  - Mutiertes Golden: gleicher Aufruf → **Exit 1**, `AssertionError` bei `assert set(e) == set(CONTRACT['layerFields'])` nach erfolgreichen API-Aufrufen.
- **Warum nicht trivial:** Es wird nicht einfach ein bereits vorhandener Negativtest wiederholt; die Erwartungsseite des Golden-Contracts wird verändert, während die reale App und das unveränderte Script laufen. Der Test kann dadurch nur bestehen, wenn er die Feldmenge tatsächlich prüft. Zusätzlich belegt der Original-Lauf die reale 169/886-Fixture und die API-/HTML-Verträge auf einer echten JVM.
- **Rohbeleg:** `/tmp/kartenkatalog-c0-review-1378d3f/probe-golden-mutation.log`; Skript `/tmp/kartenkatalog-c0-review-1378d3f/probe-golden-mutation.sh`.

### 4.2 Gegenprobe B – Referenzketten- und Mutationsanker-Audit mit negativer Canary

- **Ziel/Hypothese:** Eine übersehene Pfad-/Fixture-Referenz oder ein wirkungsloser p1-Mutationsanker könnte die Inventur und die Negativtests scheinbar grün lassen. Geprüft wurden alle von den aktiven Scripts/CI referenzierten Eingabepfade sowie die sechs p1-Ersetzungsanker.
- **Ergebnis auf dem Kandidaten:** 13 Eingabepfade vorhanden (u. a. `scripts/p0-http-contract.py`, `data/…harvest…xtf`, `src/test/resources/contract/baseline.json`, `with-qml.xml`, `final/catalog.xml`, `search/catalog.xml`, `build/quarkus-app/quarkus-run.jar`); 6/6 Mutationsanker vorhanden und wirksam. **Exit 0**.
- **Negative Canary:** In einer temporären Kopie von `p1-process-contract.py` wurde der p0-Pfad auf `scripts/absent-http-contract.py` verändert. Derselbe Prüfer meldet `MISS scripts/absent-http-contract.py` und **Exit 1**. Damit ist nachgewiesen, dass die Referenzprüfung eine gebrochene Kette tatsächlich erkennt.
- **Rohbelege:** `/tmp/kartenkatalog-c0-review-1378d3f/probe-reference-chain.log` (Exit 0) und `probe-reference-chain-canary.log` (Exit 1); Skript `probe-reference-chain.py`.

## 5. Pflichtgates und Rohbelege

Alle Kommandos, sofern nicht anders angegeben, im Review-Worktree
`/Users/stefan/sources/kartenkatalog-cleanup-C0-review-1378d3f`.
Rohkopien: `/tmp/kartenkatalog-c0-review-1378d3f/evidence/` (79 Dateien, ca. 2,3 MB) und die dort belassenen `build/`-Reports.

| Gate | Kommando | Exit | Tests / Errors / Failures / Skips | Report | SHA-Bindung |
|---|---|---|---|---|---|
| T0 Git/Diff | `git rev-parse HEAD`; `git status --porcelain=v1`; `git diff --stat BASE..CANDIDATE`; `git diff --check BASE CANDIDATE` | 0; Status leer; Diff-Check 0 | 2 Docs, 134 Insertions; keine Produktänderung | `/tmp/…/final-state.log` | `1378d3f…` |
| T0 Inventar | `git ls-files \| wc -l`; Java-/Script-Zählung; `git grep` Root-Artefakte/Phasen/Legacy | 0; erwartete Grep-Exit 1 bei „keine Treffer“ | 137 Dateien; 33/14/1 Java; 7 Scripts; `P3ITTest` 0 Treffer | `final-state.log`, `probe-reference-chain.log` | `1378d3f…` |
| T0 Fixtures | `shasum -a 256 …`; `unzip -l …`; XML-/JSON-Zählung | 0 | XTF `de40f…`, JSON `cd11…`, XML `4f2e…`; 169/886; 16/9/4/5 | `/tmp/…/probe-reference-chain.log` | BASE = CANDIDATE |
| T1 JVM/API | `./gradlew clean test quarkusBuild quarkusIntTest --console=plain` | **0** (`BUILD SUCCESSFUL`; zusätzlicher `./gradlew test quarkusIntTest` Exit 0) | `test` **62/0/0/0**; `quarkusIntTest` **3/0/0/0** | `build/test-results/test`, `build/test-results/quarkusIntTest`, `/tmp/…/t1.log` | `1378d3f…` |
| T2 JVM-Prozess | `python3 scripts/p1-process-contract.py` | **0** | 1 Gutstart (ready 3.59 s, HTTP Exit 0); 8 erwartete Fehlstarts je Exit 1 vor Ready | `build/reports/p1/process-summary.json`, `…/p1/*.log`, `/tmp/…/p1.log` | `1378d3f…` |
| T2 Dev/CORS | `python3 scripts/p1-process-contract.py --dev` | **0** | 1 Dev-Gutstart (8.265 s); OPTIONS/GET `http://localhost:8081` 200, fremder Origin 403; keine Credentials | `build/reports/p1/dev-summary.json`, `…/p1/dev*.log`, `/tmp/…/p1-dev.log` | `1378d3f…` |
| T2 RAM-Index | `python3 scripts/p2-process-contract.py` | **0** | Harvest 886 Dokumente; first/second/changed je 12; Ranking stabil; Quellenänderung wirksam; broken-index Exit 1; keine Indexdateien im cwd | `build/reports/p2/summary.json`, `…/p2/*.log`, `/tmp/…/p2.log` | `1378d3f…` |
| T3 Browser | `./gradlew playwrightTest --console=plain` | **0** | **14/0/0/0**: CatalogBrowser 6, FilterBrowser 8; Chromium **und** Firefox; 1440×900 und 390×844; No-JS/History/Fokus/XSS/Fehler | `build/test-results/playwrightTest`, `build/reports/p3a/`, `build/reports/p3b/`, 24 Screenshots, `/tmp/…/playwright.log` | `1378d3f…` |
| T5 Versionierung | `python3 scripts/versioning-contract.py` | **0** | 4 gültige Fälle (`local`, `github`, `legacy-only`, `github-wins`), 5 ungültige Run-Nummern erwartet abgelehnt | `/tmp/…/versioning.log` | `1378d3f…` |
| T5 Release | `python3 scripts/release-contract.py` | **0** | 6/0/0/0 unittest; Fake-Registry, 8 Tags, Varianten-/Missing-/Plattform-/Versionsfehler | `/tmp/…/release.log` | `1378d3f…` |
| Runtime-Abhängigkeiten | `./gradlew dependencies --configuration runtimeClasspath --console=plain` | **0** | Quarkus 3.40.1.1, Lucene 10.6.0, Jsoup; keine Spring-/JDBC-/DuckDB-/PostgreSQL-Treffer | `/tmp/…/dependencies.log` | `1378d3f…` |
| Eigene Probe A | Golden-Contract-Mutation, echte JVM-App | Original **0**, mutiert **1** | Original 200/169/886-Verträge; mutiert `AssertionError` an `layerFields` | `/tmp/…/probe-golden-mutation.log` | `1378d3f…` |
| Eigene Probe B | Referenzketten-Audit + Canary | Kandidat **0**, Canary **1** | 13/13 Pfade, 6/6 Anker; Canary erkennt fehlenden Pfad | `/tmp/…/probe-reference-chain*.log` | `1378d3f…` |

**JUnit-Einzelzahlen (frisch, nach dem Lauf erneut aus XML gelesen):**
`BaselineContractTests 6`, `BaselineQmlTests 3`, `CatalogFilterWebTests 2`, `CatalogSearchTests 10`, `CatalogServiceTests 3`, `CatalogTextTests 2`, `CatalogUrlFactoryTests 2`, `CatalogWebTests 3`, `SearchApiTests 4`, `SearchFailureTests 1`, `XtfCatalogParserTests 26` → Summe **62**, Failures/Errors/Skips **0/0/0**. `PackagedCatalogIT 3/0/0/0`. Browser: `CatalogBrowserTests 6/0/0/0`, `CatalogFilterBrowserTests 8/0/0/0` → **14/0/0/0**. Keine `@Disabled`-, `assumeTrue`- oder leere Erfolgsassertions gefunden; die einzigen Tag-Selektionen sind die bestehenden `excludeTags 'playwright'` im normalen `test` und `includeTags 'playwright'` in `playwrightTest`.

**Nicht ausgeführte fällige Prüfungen:** keine für C0. **Nicht ausgeführt und nicht behauptet:** Native-Build, `testNative`, p4-JVM-/Native-/Container-Gesamtmatrix und echter Registry-Abruf. Diese sind in C0 nicht fällig (T4/C4) und werden nicht als Baseline-Erfolg ausgegeben.

**Native-/Linux-/Containerarchitektur:** Kein Native-/Container-Lauf in C0. Docker Desktop 4.94.0 / Engine 29.8.2 (linux/arm64) und Oracle GraalVM 25.0.3 sind vorhanden, wurden aber nicht als Native-/linux-amd64-Beleg verwendet. Es wurde keine Zielarchitektur behauptet.

**Release-/Triggerprüfung ohne Push:** `main.yml` und `mirror.yml` wurden statisch und semantisch geprüft (jvm-Gates, `linux-native needs: jvm`, acht Tags, Main-only-Publish, PR ohne Publish, Mirror auf Push). Es wurde **kein** `git push`, Merge, Tag, `workflow_dispatch`, Registry-Login/-Upload oder Release ausgelöst.

## 6. Findings

**Keine Muss-AC-Verletzung und kein Blocker.**

Nicht blockierende Beobachtungen (bereits im C0-Bericht korrekt als spätere Arbeiten ausgewiesen):

| ID | Schwere | Beobachtung | Einordnung |
|---|---|---|---|
| O1 | Info | Die Endung „1017“ der Modellbezeichnung ist in Session-/Provider-/App-Metadaten nicht separat exponiert; verifiziert ist `deepseek/deepseek-flash` = „DeepSeek V4.1 Flash“, Variante `high`, direkter Endpunkt `api.deepseek.com`. | Keine Produktabweichung; Identität über Metadaten belegt, keine Selbstauskunft. |
| O2 | Info | `p2-process-contract.py` ist nicht gegen einen gleichzeitig laufenden Paket-Rebuild isoliert; der C0-Bericht legt den überlappenden Fehlversuch offen und wiederholte den Lauf sequenziell erfolgreich. | Kein C0-Muss; für C2/C4 sequenzielle Orchestrierung beibehalten. |
| O3 | Info | `.env` ist derzeit nicht von `.gitignore` erfasst. | Korrekt als C1-Arbeit dokumentiert; AC1-03. |
| O4 | Info | Doppelter XTF-Default (`@WithDefault` + Properties) und pauschaler Harvest-Footer bleiben aktiv. | Korrekt als C3-Arbeit dokumentiert; AC3-01/02. |
| O5 | Info | Aktive Phasennamen (`p0`–`p4`, `p3a`/`p3b`, `Baseline*`, `final/`) bleiben in Scripts/Tests/README/CSS. | Korrekt als C2/C4-Arbeit dokumentiert; C0 darf sie noch führen. |
| O6 | Info | Der Klassenkommentar in `CatalogBrowserTests` sagt „actual Chromium“, obwohl auch Firefox gestartet wird. | Kosmetisch; die tatsächliche Matrix ist korrekt und beide Engines liefen. |
| O7 | Info | Die konkrete Beispielzuordnung im C0-Bericht („`jte`-Treffer in `press(„Tab“)`“) ist nicht exakt; der unscharfe Treffer liegt in Font-/Base64-Daten. | Die Schlussfolgerung „kein JTE-Import“ bleibt korrekt; keine AC-Auswirkung. |

**Blocker nach INFRA / MISSING_EVIDENCE / SCOPE_DECISION:** keine.

## 7. Urteil

**PASS**

- **Begründende Muss-Kriterien:** AC0-01, AC0-02, AC0-03, AC0-04 und AC0-05 sind jeweils anhand eigener Git-/Source-/Hash-/Prozess-/JUnit-/Browser-Belege erfüllt. Der Kandidat ist exakt `1378d3f5b2bb7a210efa5f704c94a4e43ba23643`, der Worktree blieb sauber, und die eigene Gegenprobe hat einen echten negativen Vertrag nachgewiesen.
- **Nächster zulässiger Schritt:** C0 ist unabhängig abgenommen. C1 darf erst nach Rückmeldung des Nutzers beginnen; dieser PASS ist **keine** Freigabe für einen Push nach `main`, einen Merge, ein Tag, einen Workflow-Dispatch oder eine Docker-Hub-/Codeberg-Veröffentlichung. Die spätere Main-Push-Releasewirkung bleibt eine separate Nutzerentscheidung.
- **Berichtspfad:** `/Users/stefan/sources/kartenkatalog-C0-review-1378d3f.md` (ausserhalb des unveränderlichen Kandidaten).
