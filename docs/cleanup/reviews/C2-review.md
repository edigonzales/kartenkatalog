# Unabhängiger Review C2 – Kartenkatalog Cleanup

## 1. Identität und Umgebung

- **Lieferabschnitt:** C2 – phasenfreie Testnamen, Scripts, Reportpfade und CI. **PHASENGATE: nein** (kein C4-Gesamtgate).
- **Repository:** `edigonzales/kartenkatalog`.
- **OpenCode-GUI-Version und geprüfte Herkunft (nicht Modell-Selbstauskunft):**
  - Laufende GUI-App: **OpenCode 2.1.4** auf macOS. Belege: Prozess `/Users/stefan/Library/Application Support/ai.opencode.desktop/cli/2.1.4-arm64/opencode-cli serve --service`, Crashpad-Annotation `_version=2.1.4`, `Info.plist` `CFBundleShortVersionString = 2.1.4`, Session-Datensatz `session_v2.version = 2.1.4`.
  - Aktuelle Session: `ses_e6949c3bdffe2ZRkqpWdg8X1Ue`, Titel „Unabhängige Prüfung C2 kartenkatalog“, `agent=build`, `model={"id":"deepseek-flash","providerID":"deepseek","variant":"high"}`, `directory=/Users/stefan/sources/kartenkatalog`.
  - Direkter Provider: **`deepseek`**. Der von der laufenden 2.1.4-Instanz verwendete Katalog (`kv['models-dev:catalog']`, `updatedAt=1793529670206`) nennt für Provider `deepseek`: `api = https://api.deepseek.com`, `npm = @ai-sdk/openai-compatible`, `env = DEEPSEEK_API_KEY`.
  - Modell: `deepseek/deepseek-flash`, Anzeigename **„DeepSeek V4.1 Flash 1017“**, `release_date 2026-10-17`, `canonical_model_id deepseek/deepseek-v4.1-flash-1017`; Session-Variante `high`. Das entspricht der vom Nutzer bestätigten GUI-Auswahl.
  - **Kein OpenRouter-/Zen-Fallback:** Session-Provider ist `deepseek`, der Katalog nennt den direkten DeepSeek-Endpunkt. `auth.json` enthält zwar auch andere Provider-Schlüssel (`deepseek`, `infomaniak`, `openrouter` als reine Schlüsselnamen), die Session verwendet jedoch ausschliesslich `providerID=deepseek`. **Credentialwerte wurden nicht gelesen oder ausgegeben.**
- **BASE_SHA:** `ca7d629c65d3b856d877bf3d468e946dc752d473` (`git cat-file -t` = commit; direkter Elternknoten des Kandidaten).
- **CANDIDATE_SHA (im Review-Worktree verifiziert):** `c93714d2e08238e7549da62487c87ef5291a2df0` (`git rev-parse HEAD`).
- **Review-Worktree:** `/Users/stefan/sources/kartenkatalog-cleanup-C2-review-c93714d`, detached HEAD auf `c93714d…`. Alle Prüfkommandos liefen mit explizitem `cwd` in diesem Worktree. `git status --porcelain=v1` war vor und nach dem Review **leer**; der tracked Tree blieb unverändert. Die GUI-Session zeigt als `directory` den Haupt-Checkout `/Users/stefan/sources/kartenkatalog`; dort wurde nichts geprüft oder verändert.
- **Toolchain:** macOS 15.8.1/arm64; Temurin Java 21.0.7+6-LTS (Buildtoolchain); Gradle Wrapper 9.8.2; Python 3.9.6 mit `sys.flags.optimize == 0`; Docker Client/Server 29.8.2, Server `linux/arm64 (aarch64)`, cgroup v2; GraalVM **25.0.3** unter `/Users/stefan/.sdkman/candidates/java/25.0.3-graal` (nur für Native-Compiler gesetzt); Quarkus 3.40.1.1, Lucene 10.6.0, Playwright 1.60.0 aus unverändertem `build.gradle`.
- **Zielarchitektur der T4-Belege:** macOS/arm64 (Mach-O) und Linux/arm64 (ELF/OCI). **Es wurde kein linux/amd64-Lauf ausgeführt und kein linux/amd64-Erfolg behauptet.**

## 2. Eigene Source-/Git-/Consumeranalyse

### 2.1 Kandidatenumfang und Blob-Identität

`git diff --name-status BASE..CANDIDATE` zeigt **17 Pfade**: 8 Renames, 8 Modifikationen, 1 neue Datei. Keine Änderung an `src/main/java`, `build.gradle`, `gradle/`, `deploy/`, `.dockerignore` oder `settings.gradle`.

| Status | Pfad(e) |
|---|---|
| R | 4 Scripts, 2 Java-Testklassen, `contract/baseline.json → public-api.json`, `final/catalog.xml → integration/catalog.xml` |
| M | `.github/workflows/main.yml`, `README.md`, `docs/cleanup/STATUS.md`, `docs/quarkus-migration/OPERATIONS.md`, `catalog.css`, 2 Browser-Testklassen, Contract-README |
| A | `docs/cleanup/implementation/C2.md` |

Eigener Tree-Vergleich: BASE **137** getrackte Dateien, Kandidat **138**. `git diff --check` Exit **0**. BASE ist direkter Parent; genau ein Kandidatencommit.

### 2.2 Fixtures, Contracts und historische Evidenz

| Prüfung | Eigener Befund |
|---|---|
| `contract/baseline.json → public-api.json` | identische Git-Blob-ID `f193170aabc9082f8ba72e81675e0ca142bb8841`; SHA256 `cd1167f8870b5dae021b913c08b7643427683ed406e3018e915cbab4feff219b` |
| `final/catalog.xml → integration/catalog.xml` | identische Git-Blob-ID `1772f1544bb7ef3e4ca2c5b16c0a97fda72ac24d`; SHA256 `4f2e54495e0b852b06e5a35e76763216afa04e6da5b1d4383437c9de732ec510` |
| Harvest-XTF | Blob `bbd384d0404827a1cf8d137205d1a130009fee6e` in BASE und Kandidat; SHA256 `de40f025368ae6fc70f5858ab42592488bca837c6942cb6ff8759e6c8803de86` |
| `MockCatalogRepository` | Blob `d5e5bf7c48bbf0930095a4d9ef09c7ddcc05a048` unverändert; real von `CatalogServiceTests` als `new CatalogService(new MockCatalogRepository())` genutzt |
| Historische Dokumente `docs/quarkus-migration/**` | 26 Dateien: **25 bytegleich**, nur `OPERATIONS.md` geändert. Das ist gemäss Plan C2.1.8 zulässig, weil diese Datei bis C4 die aktive Betriebsanleitung ist. |
| Mirror-Workflow | Blob `560a5d1488f597da0e1ce9aba2021811702db1a8` in BASE und Kandidat identisch |
| Früherer PASS | C1-Review `b55d915fbca64cee05f2395424c1fdd30aa21d9a` unverändert; C0-Review `db0c32f9…` unverändert; C1-PASS auf `b3df508…` ist Vorfahre von BASE |

### 2.3 Aktive Consumer und Referenzscan

Eigener Scan über `scripts`, `src/main/java`, Templates, `catalog.css/js`, `src/test/java`, Contract-README/-JSON, Integration-XML, `.github`, `build.gradle`, `gradle`, `deploy`, aktives `README.md` und die bis C4 aktive `OPERATIONS.md`:

- **kein** Treffer für `p0-http-contract`, `p1-process-contract`, `p2-process-contract`, `p4-distribution-contract`, `BaselineContractTests`, `BaselineQmlTests`, `contract/baseline.json`, `final/catalog.xml`, `build/reports/p*`, `P3ITTest`, `P3a`, `P3b`.
- `git ls-files scripts/` enthält genau die vier neuen Fachnamen plus `versioning-contract.py`, `release-contract.py`, `verify-published-images.py`.
- `runtime-process-contract.py` ruft `subprocess.run([sys.executable, 'scripts/api-http-contract.py', url], …)`; `distribution-contract.py` ruft `subprocess.run([sys.executable, str(ROOT/'scripts/api-http-contract.py'), base], …)` und lädt `src/test/resources/integration/catalog.xml`. Beide liefen real erfolgreich.
- Nach **allen** Läufen existierte **kein** `build/reports/p0`…`p5`/`p3a`/`p3b`-Verzeichnis.
- Aktive Dokumente verweisen explizit als *historisch* auf `implementation/P4.md` bzw. `P5.md`; das ist gemäss Plan zulässig und wird in C4 archiviert.
- Lokale Links in den geänderten aktiven Dokumenten (`README.md`, Contract-README, `OPERATIONS.md`): 0 gebrochene Ziele; der Contract-README-Link auf `docs/quarkus-migration/implementation/P0.md` löst auf.

### 2.4 Parität der Harnesse und Tests

- Python-`assert`-Segmente (AST-Text) BASE → Kandidat: API **25/25 identisch**, Runtime **12/12 identisch**, Index **17/17 identisch**, Distribution **64/64**; einzige Textdifferenz ist die absichtlich neutralisierte Fehlermeldung `'P0 HTTP contract failed'` → `'API HTTP contract failed'`, die Bedingung `smoke.returncode == 0` ist identisch.
- CLI-Optionen unverändert: `runtime` `--dev`; `distribution` `--native-executable`, `--native-image`, `--jvm-image`, `--expected-version`, `--expected-revision`, `--expected-platform`, `--report-dir`; `api`/`search-index` wie vorher. Kein Timeout-, Prozessgruppen- oder Cleanup-Code entfernt.
- Java: `CatalogApiContractTests` **6** Testmethoden / 52 `assert`-Vorkommen (BASE 6/52), `CatalogQmlContractTests` **3** (BASE 3), keine `@Disabled`, kein `assumeTrue`, `build.gradle` unverändert. JUnit-XML erzeugt die neuen Suiteidentitäten, die alten nicht.
- Browser: `CatalogBrowserTests` 3 × 2 Engines = 6, `CatalogFilterBrowserTests` 4 × 2 Engines = 8; 1440×900 und 390×844, Chromium **und** Firefox, No-JS/Fokus/History/Security unverändert.

### 2.5 CI-Semantik

Eigener normalisierter Vergleich von `.github/workflows/main.yml` BASE vs. Kandidat: Nach Ersetzung **genau der fünf** geänderten Scriptpfade sind die Dateien **bytegleich** (`normdiff_exit=0`). Damit unverändert: `linux-native needs: jvm`, JVM-Test-/Browser-/Prozess-/Index-Gates, `testNative`, JVM-/Native-Image-Build, `distribution-contract` vor Login/Push und nach Pull, read-only/UID/Plattform-/Digest-Gates, drei identische Main-only-Bedingungen, beide Vierer-Tagloops (acht Tags), `paths-ignore: '**.md'`, Mirror-Workflow. Kein Push, kein Dispatch, kein Registry-Push.

## 3. Jede Muss-AC einzeln

| AC-ID | erfüllt / verletzt / nicht prüfbar | Eigener Code-/Test-/Prozessbeleg | Finding-ID |
|---|---|---|---|
| **AC2-01** | **erfüllt** | Alle vier Scripts nur unter neuen Namen (`git ls-files scripts/`); alle aktiven Aufrufer in CI, Runtime, Distribution, README, OPERATIONS, Contract-README angepasst; alter Name in aktivem Scope nicht auffindbar; nach allen Gates keine `build/reports/p*`-Verzeichnisse. | — |
| **AC2-02** | **erfüllt** | Assert-Segmente 25/12/17/64 identisch (Distribution nur neutrale Fehlermeldung); alle CLI-Optionen, Timeouts, Prozessgruppen, 8 Runtime-Fehlstarts, 4 Distribution-Negativfälle je Laufzeit erhalten; Fixtures bytegleich; reale Läufe erfolgreich. | — |
| **AC2-03** | **erfüllt** | Zwei Klassen und zwei Fixtures per Rename; Fixture-Blobs 100 % identisch; Testmethoden 6/3 unverändert; `MockCatalogRepository` Blob unverändert und real genutzt; keine Skips/Disabled; neue Suiteidentitäten im XML, alte absent. | — |
| **AC2-04** | **erfüllt** | `playwrightTest` Exit 0, **14/0/0/0**, Chromium+Firefox, Desktop 1440×900 und Mobil 390×844; 24 Screenshots und `browser-jvm.log` unter `build/reports/browser/catalog|filters`; README dokumentiert diese Pfade; `PackagedCatalogIT` 3/0/0/0 auf JVM und echtem macOS-Native-Binary. | — |
| **AC2-05** | **erfüllt** | Workflow nach Normalisierung der fünf Pfade bytegleich zu BASE; `needs: jvm`, alle JVM-/Native-/Pull-/UID-/XTF-/Digest-/`testNative`-Gates, acht Tags und drei Main-only-Publish-Bedingungen erhalten; Mirror-Blob unverändert; kein Push/Release. | — |
| **AC2-06** | **erfüllt** | Frische T1/T2/T3/T5-Läufe mit Exit 0 und echten XML-/JSON-Zahlen; T4 lokal und linux/arm64 mit je 20 realen Fällen; erwartete Fehlstarts tatsächlich Exit 1; eigene absichtlich scheiternde Canary Exit 1; keine Veröffentlichung. | — |
| **AC2-07** | **erfüllt** | Eigener Rename-/CI-Referenzscan (Original Exit 0, gebrochener Pfad Exit 1, entfernter Gate-Step Exit 1) und eigene HTTP-Response-Canary (direkt Exit 0, mutierte 168-Themen-Antwort Exit 1) auf exakt `c93714d…`; keine offene Muss-Abweichung. | — |

## 4. Selbst entwickelte Gegenprobe(n)

### 4.1 Eigene Harness-Canary: Mutation der Themenliste (nicht Codex' 404-Proxy)

- **Ziel/Hypothese:** Der neue API-HTTP-Harness muss nicht nur einen falschen 404-Status, sondern auch eine strukturell plausible, aber falsche Erfolgsantwort erkennen (fälschlich grün). Eine um ein Thema verkürzte `200`-Antwort ist ein reales Risiko, weil die Anzahl- und Vertragsassertions sonst nur zufällig greifen.
- **Neue Eingabe:** Eigener Python-stdlib-Loopback-Proxy vor einer echten, selbst gestarteten Harvest-JVM. Der Proxy leitet alle Antworten unverändert weiter, entfernt aber bei exakt `GET /api/v1/themen` das letzte Element (169 → 168) und setzt einen korrekten `Content-Length`.
- **Kommandos/Ergebnisse (cwd = Review-Worktree):**
  - `python3 scripts/api-http-contract.py http://127.0.0.1:<app>` → **Exit 0**.
  - `python3 scripts/api-http-contract.py http://127.0.0.1:<proxy>` → **Exit 1**, `AssertionError` in Zeile 39: `assert len(themes) == 169 and len(layers) == 886 and len(catalog['themes']) == 169`.
  - Treiber: `/tmp/kartenkatalog-c2-review-c93714d/own-canary.py`; Belege `own-canary-direct.log`, `own-canary-proxy.log`, `own-canary-console.log` (Proxy meldete `PROXY_MUTATED /api/v1/themen 169 -> 168`).
- **Warum nicht trivial:** Die Probe nutzt die unveränderte Kandidatendatei, eine echte JVM und eine echte mutierte HTTP-Antwort mit korrektem Erfolgsstatus/MIME. Sie hätte ein Harness, das nur den Exitcode des Servers oder einzelne Felder prüft, nicht entlarvt. Codex' C2-Canary (404→200 an `/api/v1/ebenen/unknown`) wurde bewusst **nicht** wiederholt.

### 4.2 Eigene CI-/Pfad-Canary: gebrochener Scriptpfad und entfernter Gate-Step

- **Ziel/Hypothese:** Ein Rename ist nur dann sicher, wenn ein Referenzprüfer einen falschen Scriptpfad oder einen aus der CI verschwundenen Pflicht-Gate-Step tatsächlich rot macht.
- **Neue Eingabe:** Eigener Checker `/tmp/kartenkatalog-c2-review-c93714d/ci-ref-check.py` prüft (a) Existenz jedes in `python3 scripts/…` referenzierten Scripts, (b) Anwesenheit der sechs Pflicht-Gates, (c) `--dev`-Variante und (d) genau zwei Distribution-Aufrufe. Drei Workflow-Kopien unter `/tmp`: Original, `search-index-contract.py` → nicht existierender Name, sowie entfernter „Real RAM-index rebuild“-Step.
- **Kommandos/Ergebnisse:**
  - Original-Kopie → **Exit 0** (`missing=[] absent_gates=[] dev=True dist_count=2 old=[]`).
  - Gebrochener Pfad → **Exit 1** (`missing=['scripts/search-index-contract-broken.py']`, `absent_gates=['scripts/search-index-contract.py']`).
  - Entfernter Gate-Step → **Exit 1** (`absent_gates=['scripts/search-index-contract.py']`).
- **Warum nicht trivial:** Die Probe prüft die echte CI-Datei des Kandidaten und zwei unabhängige Fehlerklassen (Pfad vs. fehlender Step). Ein bloßes `grep` auf den neuen Namen hätte den entfernten Step nicht erkannt.

## 5. Pflichtgates und Rohbelege

Alle Kommandos mit `cwd=/Users/stefan/sources/kartenkatalog-cleanup-C2-review-c93714d`. Rohbelege unter `/tmp/kartenkatalog-c2-review-c93714d/` (JUnit-Kopien in `pre-native/`).

| Gate | Kommando | Exit | Tests / Errors / Failures / Skips | Report | SHA-Bindung |
|---|---|---|---|---|---|
| T0 Tree/Diff/Hash/Consumer | `git rev-parse`, `git cat-file`, `git diff --name-status`, Blob-/SHA256-Vergleich, `git grep`, Linkprüfung | 0 (Grep „keine Treffer“ = 1, erwartet) | 137 → 138 Dateien; 8 R, 8 M, 1 A; 25/26 historische Dateien bytegleich | `evidence-summary.txt`, `historical-identity.txt`, `head.txt`, `status-before/after.txt` | `c93714d…` |
| T1 JVM/Unit/API/Paket | `./gradlew clean test quarkusBuild quarkusIntTest --console=plain` | **0** | **62 / 0 / 0 / 0** + **3 / 0 / 0 / 0** | `t1.log`, `pre-native/test-results/**`, `counts.json` | `c93714d…` |
| T2 realer JVM/HTTP | `python3 scripts/runtime-process-contract.py` | **0** | 1 Gutstart (ready 1.524 s); HTTP-Contract Exit 0; 8 Fehlstarts je **Exit 1** vor Ready | `t2-runtime.log`, `pre-native/reports/runtime/process-summary.json` | `c93714d…` |
| T2 Dev/CORS | `python3 scripts/runtime-process-contract.py --dev` | **0** | Dev ready 8.037 s; OPTIONS/GET erlaubt **200**, fremd **403**; keine Credentials | `t2-runtime-dev.log`, `pre-native/reports/runtime/dev-summary.json` | `c93714d…` |
| T2 RAM-Index | `python3 scripts/search-index-contract.py` | **0** | Harvest 886; first/second/changed je 12; broken-index **Exit 1**; keine Indexdateien | `t2-index.log`, `pre-native/reports/search-index/summary.json` | `c93714d…` |
| T3 Browser | `./gradlew playwrightTest --console=plain` | **0** | **14 / 0 / 0 / 0** (Catalog 6, Filter 8); Chromium+Firefox; 24 Screenshots | `t3-playwright.log`, `pre-native/test-results/playwrightTest/**`, `pre-native/reports/browser/**` | `c93714d…` |
| T5 Versionierung | `python3 scripts/versioning-contract.py` | **0** | 4 gültige Fälle; 5 ungültige Run-Nummern abgelehnt | `t5-versioning.log` | `c93714d…` |
| T5 Release/Fake-Registry | `python3 scripts/release-contract.py` | **0** | **6 unittest**, 0 Fehler/Skips; alle acht Tags/Negativfälle | `t5-release.log` | `c93714d…` |
| Dependencies | `./gradlew dependencies --configuration runtimeClasspath --console=plain` | **0** | Quarkus 3.40.1.1, Lucene 10.6.0; keine Spring/JDBC/PostgreSQL/DuckDB/JTE-Laufzeit | `dependencies.log` | `c93714d…` |
| T4 Native macOS/arm64 | `GRAALVM_HOME=/Users/stefan/.sdkman/candidates/java/25.0.3-graal ./gradlew clean testNative -Dquarkus.native.enabled=true -Dquarkus.package.jar.enabled=false -Dquarkus.native.native-image-xmx=4g --console=plain` | **0** | **3 / 0 / 0 / 0** Native-Pakettests; Mach-O-arm64-Binary vor Clean gesichert (`bb3ee1a0…`) | `t4-native-build.log`, `own-native/application`, `counts.json` | `c93714d…` |
| T4 lokale JVM/Native-Parität | `./gradlew quarkusBuild`; `python3 scripts/distribution-contract.py --native-executable /tmp/…/own-native/application --report-dir build/reports/cleanup-review/distribution-local` | **0** | **20 reale Fälle** (10 je Laufzeit: 5 Gut-/Restart-/Cross-Fälle, 1 geänderte XTF, 4 erwartete Fehlstarts); identische Payload-Hashes | `t4-jvm-restore.log`, `t4-local-distribution.log`, `build/reports/cleanup-review/distribution-local/summary.json` | `c93714d…` |
| T4 Linux/arm64 Images | `docker build --platform linux/arm64 --build-arg VERSION=0.2.LOCALBUILD --build-arg REVISION=c93714d… -f Dockerfile.jvm -t kk-c2-review-c93714d-jvm .`; Mandrel-Container-Build; `… -f Dockerfile.native -t kk-c2-review-c93714d-native .` | **0** | JVM-Image `5bb6c83e…`, Native-Image `1fb24d6c…`; `linux/arm64`, User `1001:0`, Version `0.2.LOCALBUILD`, Revision `c93714d…` | `docker-jvm-build.log`, `t4-linux-native-build.log`, `docker-native-build.log` | `c93714d…` |
| T4 Linux/arm64 Container-Distribution | `python3 scripts/distribution-contract.py --jvm-image kk-c2-review-c93714d-jvm --native-image kk-c2-review-c93714d-native --expected-version 0.2.LOCALBUILD --expected-revision c93714d… --expected-platform linux/arm64 --report-dir build/reports/cleanup-review/distribution-linux-arm64` | **0** | **20 echte Containerfälle** (10 je Runtime); read-only RootFS/XTF, UID `1000730000:0`, kein Indexvolume, `docker diff` leer; 4 Fehlstarts je Runtime Exit 1; Payload-Hashes JVM=Native | `t4-linux-distribution.log`, `build/reports/cleanup-review/distribution-linux-arm64/summary.json` | `c93714d…` |
| Eigene Harness-Canary | `python3 /tmp/…/own-canary.py` | Treiber **0**; direkt **0**, Proxy **1** (erwartet) | 169→168 Themen; AssertionError Zeile 39 | `own-canary*.log` | `c93714d…` |
| Eigene CI-Canary | `python3 /tmp/…/ci-ref-check.py <workflow>` | Original **0**; Pfad **1**; Step entfernt **1** | 8 Referenzen, 6 Pflicht-Gates, `--dev`, 2× Distribution | `ci-*.yml`, `ci-*.out` | `c93714d…` |
| Diff-Hygiene | `git diff --check BASE CANDIDATE`; `git status --porcelain=v1` | **0**; Status leer | — | `evidence-summary.txt`, `status-after.txt` | `c93714d…` |

- **Nicht ausgeführte fällige Prüfungen:** **keine.**
- **Bewusst nicht fällig / nicht behauptet:** linux/amd64-Release-/CI-Lauf (Umgebung ist linux/arm64; C4-Gesamtgate und echter CI-Runner stehen aus), C4-Dokumentations-/Archivgate. Der macOS-arm64-Native- und der Linux-arm64-Containerlauf werden **nicht** als amd64-Nachweis ausgegeben.
- **Native-/Linux-/Containerarchitektur:** macOS/arm64 Mach-O (`application`, SHA256 `bb3ee1a03cf07d2308969316c12cd8cd74f195ec1b7dcf3333068d95b0593ba7`) und Linux/arm64 OCI-Images (`kk-c2-review-c93714d-jvm` / `-native`, eigene Tags, Revision = echter CANDIDATE_SHA). Native- und JVM-Artefakt wurden getrennt paketiert; das Native-Binary wurde vor dem nächsten `clean` gesichert. Alle Builds und abhängigen Prozess-/Browserprüfungen liefen sequenziell; keine laufende JAR wurde durch einen Rebuild gelöscht.
- **Release-/Tag-/Triggerprüfung ohne Push:** `versioning-contract.py` und `release-contract.py` grün; CI semantisch geprüft; **kein** `git push`, Merge, Tag, Workflow-Dispatch, Registry-Push oder Release. Die acht Tagmuster und die Main-only-Publish-Bedingungen sind unverändert.

## 6. Findings

- **O1 (niedrig, nicht blockierend, Dokumentation):** `docs/cleanup/implementation/C2.md` formuliert „**26** historische Dokumente/Referenzassets … bleiben bytegleich; nur die … OPERATIONS-Datei wurde … angepasst“. Tatsächlich enthält `docs/quarkus-migration/**` **26 Dateien insgesamt**: **25 bytegleich + 1 geänderte `OPERATIONS.md`**. Repro: `git ls-tree -r --name-only CAND docs/quarkus-migration | wc -l` = 26; Blob-Vergleich ergibt 25×`SAME`, 1×`CHANGED`. Die Substanz (keine unerlaubte Änderung historischer Reviews/Implementierungen) ist korrekt; nur die Zahl sollte in einem rein dokumentarischen Follow-up korrigiert werden. Kein AC-Verstoss.
- **Keine funktionalen, Test-, CI-, Fixture- oder Provenienz-Findings.** Keine verletzte Muss-AC.
- **Nicht blockierende Beobachtung:** Aktive Dokumente verlinken `implementation/P4.md` bzw. `P5.md` ausdrücklich als historische Berichte. Das entspricht dem Plan bis C4; die Archivierung erfolgt in C4.
- **Blocker nach INFRA / MISSING_EVIDENCE / SCOPE_DECISION:** keine.

## 7. Urteil

**PASS**

- **Begründende Muss-Kriterien:** AC2-01 bis AC2-07 sind einzeln anhand eigener Git-/Blob-/Consumer-/CI-/Prozess-/Browser-/Native- und Containerbelege auf exakt `c93714d2e08238e7549da62487c87ef5291a2df0` erfüllt. Alle vier Harnesse existieren nur unter den neuen Fachnamen; alle aktiven Aufrufer einschliesslich `sys.executable`-Kette, Distribution-Fixture und CI sind angepasst. Assertions, Negativfälle, Timeouts, CLI-Optionen, Suiteidentitäten und Testzahlen sind unverändert streng; die beiden Fixtures sind bytegleich; Chromium und Firefox liefern 14/0/0/0; CI- und Mirror-Semantik sind erhalten. T1–T3 und T5 liefen frisch, T4 wurde für den technisch betroffenen Distributions-Harness mit eigenen macOS- und Linux/arm64-Artefakten real ausgeführt. Zwei eigene, nicht von Codex übernommene Canaries zeigen echte Fehlerempfindlichkeit. Der Kandidat blieb tracked-sauber.
- **Einschränkung:** Der PASS gilt für C2, **nicht** als C4-Gesamtfreigabe und **nicht** als linux/amd64-Nachweis. Die einzige Beobachtung ist die nicht blockierende Berichtsarithmetik O1.
- **Nächster zulässiger Schritt:** C2-Review und Status dürfen in einem rein dokumentarischen Commit archiviert werden; danach kann C3 gemäss Plan beginnen. **Kein Push nach `main`, kein Merge, kein Git-Tag, kein Workflow-Dispatch, keine Registry-Publikation und kein Release** ohne separate Nutzerentscheidung. C3 wurde durch diesen Review nicht begonnen.

---

**Geprüfter Kandidat:** `c93714d2e08238e7549da62487c87ef5291a2df0`  
**Geprüfte Basis:** `ca7d629c65d3b856d877bf3d468e946dc752d473`  
**Review-Worktree:** `/Users/stefan/sources/kartenkatalog-cleanup-C2-review-c93714d` (tracked clean vor/nach)  
**Rohbelege:** `/tmp/kartenkatalog-c2-review-c93714d/`
