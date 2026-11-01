# Unabhängiger Review C3 – Kartenkatalog Cleanup

## 1. Identität und Umgebung

- **Lieferabschnitt:** C3 – explizite Provenienz und eine XTF-Defaultquelle. **PHASENGATE: nein** (kein C4-Gesamtgate).
- **Repository:** `edigonzales/kartenkatalog`.
- **OpenCode-GUI-Version und geprüfte Herkunft (nicht Modell-Selbstauskunft):**
  - Laufende GUI-App: **OpenCode 2.1.4** auf macOS. Belege: `Info.plist` `CFBundleShortVersionString = 2.1.4`; Prozess `/Users/stefan/Library/Application Support/ai.opencode.desktop/cli/2.1.4-arm64/opencode-cli serve --service`.
  - Aktuelle Session: `ses_e691d3974ffeLlTcwI0orTBfXs`, Titel „Unabhängiges C3-Review kartenkatalog“, `version=2.1.4`, `agent=build`, `model={"id":"deepseek-flash","providerID":"deepseek","variant":"high"}`. Die GUI-Session nennt als `directory` den Haupt-Checkout; **alle Repositorykommandos dieses Reviews liefen mit explizitem `cwd` im separaten C3-Review-Worktree**, nicht dort.
  - Direkter Provider: **`deepseek`**. Der von der laufenden 2.1.4-Instanz verwendete Katalog (`kv['models-dev:catalog']`, `updatedAt=1793532373916`, `digest=f2f089da079dbd94d318ccd2e4c6dd1941f9820342f2060397b91106a514c663`) nennt für Provider `deepseek`: `api = https://api.deepseek.com`, `npm = @ai-sdk/openai-compatible`, `env = DEEPSEEK_API_KEY`.
  - Modell: `deepseek/deepseek-flash`, Anzeigename **„DeepSeek V4.1 Flash 1017“**, `release_date 2026-10-17`, `canonical_model_id deepseek/deepseek-v4.1-flash-1017`; Session-Variante `high`.
  - **Kein OpenRouter-/Zen-Fallback:** Session-Provider ist `deepseek`, der Katalog nennt den direkten DeepSeek-Endpunkt. `auth.json` und Credentialwerte wurden **nicht gelesen oder ausgegeben**. Kein OpenCode-CLI-Aufruf, kein CLI-Fallback.
- **BASE_SHA:** `17f1c25a6c60c563525cae63a7d3064d7597f119` (`git cat-file -t` = commit; direkter Elternknoten des Kandidaten).
- **CANDIDATE_SHA (im Worktree verifiziert):** `f2d85d71422eca803b0a8367a2606d96642325cb` (`git rev-parse HEAD`; `HEAD^` = BASE).
- **Review-Worktree:** `/Users/stefan/sources/kartenkatalog-cleanup-C3-review-f2d85d7`, detached HEAD auf `f2d85d7…`. `git status --porcelain=v1` war **vor und nach** allen Tests **leer**; der tracked Tree blieb unverändert. `git diff --check BASE CANDIDATE` Exit **0**.
- **Toolchain:** macOS 15.8.1/arm64; Temurin Java 21.0.7+6-LTS (Buildtoolchain); Gradle Wrapper 9.8.2; Python 3.9.6 mit `sys.flags.optimize == 0`; GraalVM **25.0.3** unter `/Users/stefan/.sdkman/candidates/java/25.0.3-graal` (nur für den Native-Compiler gesetzt); Quarkus 3.40.1.1, Lucene 10.6.0, Playwright 1.60.0 aus unverändertem `build.gradle`. Docker 29.8.2/arm64 vorhanden, für C3 **nicht** benötigt/ausgeführt.
- **Zielarchitektur der T4-Belege:** **macOS/arm64** (Mach-O). Es wurde **kein** Linux-/Container-/linux-amd64-Lauf ausgeführt und **kein** entsprechender Erfolg behauptet. Der volle Linux-/Container-Abschluss bleibt C4.

## 2. Eigene Source-/Git-/Consumeranalyse

### 2.1 Kandidatenumfang und Blob-Identität

`git diff --name-status BASE..CANDIDATE` zeigt **13 Pfade**: 11 Modifikationen, 2 neue Dateien, **keine Löschung, kein Rename**.

| Status | Pfad(e) |
|---|---|
| M | `README.md`, `docs/cleanup/STATUS.md`, `src/integrationTest/java/ch/so/agi/meta/PackagedCatalogIT.java` |
| M | `src/main/java/ch/so/agi/meta/CatalogConfig.java`, `src/main/java/ch/so/agi/meta/web/CatalogPageVmFactory.java`, `src/main/java/ch/so/agi/meta/web/CatalogViewModels.java` |
| M | `src/main/resources/application.properties`, `src/main/resources/templates/layout.html` |
| M | `src/test/java/ch/so/agi/meta/CatalogApiContractTests.java`, `CatalogBrowserTests.java`, `CatalogWebTests.java` |
| A | `docs/cleanup/implementation/C3.md`, `src/test/java/ch/so/agi/meta/CatalogProvenanceTests.java` |

Eigener Tree-Vergleich: **128 unveränderte Pfade** sind zwischen BASE und Kandidat **blob-identisch** (`unchanged_blob_mismatches=0`). Produktions-Java-Dateien: **33 vorher / 33 nachher**; `git diff --diff-filter=D` ist leer. Keine Änderung an `build.gradle`, `gradle/`, `.github/`, `deploy/`, `scripts/`, `.dockerignore` oder `settings.gradle`.

### 2.2 Fixtures, Contracts, historische Evidenz

| Prüfung | Eigener Befund |
|---|---|
| `data/kartenkatalog-harvest-ohne-qml.xtf` | Blob/SHA256 unverändert: `de40f025368ae6fc70f5858ab42592488bca837c6942cb6ff8759e6c8803de86` |
| `data/README.md` | unverändert: `3744512640d0e2a6ec0ab13fec2929bcc541bdad35ee12a87fce7ce1a6e4736c`; INCOMPLETE-/Fixture-Aussage bleibt wahr |
| `contract/public-api.json` | unverändert: `cd1167f8870b5dae021b913c08b7643427683ed406e3018e915cbab4feff219b` |
| `integration/catalog.xml` | unverändert: `4f2e54495e0b852b06e5a35e76763216afa04e6da5b1d4383437c9de732ec510` |
| C2-Review | Blob `7828c3fef58a19028d6c4fe105fe625514f678af` in BASE und Kandidat identisch; C2-PASS `c93714d2…` ist Vorfahre von BASE (`merge-base --is-ancestor` Exit 0) |
| Native-/DTO-/Sanitizer-/Suchdateien | `LuceneNativeConfiguration`, `CatalogDtos`, `CatalogApiController`, `CatalogText`, `CatalogSearchIndexBuilder`, `LuceneCatalogSearchIndex`, `CatalogFacetService` usw. blob-identisch; native Suche/Ranking/Facets im T4-Lauf real erfolgreich |

### 2.3 Konkrete Umsetzung und aktive Consumer

- **Genau ein XTF-Default:** `catalog.xtf-location=${CATALOG_XTF_LOCATION:file:./data/kartenkatalog-harvest-ohne-qml.xtf}` nur in `src/main/resources/application.properties`. `CatalogConfig.xtfLocation()` hat **kein** `@WithDefault` mehr; `grep` findet in `src/main` keine weitere Defaultdefinition und kein weiteres `@WithDefault`/`ConfigProperty`.
- **Ein optionales Notice-Feld:** `CatalogConfig.provenanceNotice()` ist `Optional<String>`; `application.properties` setzt `catalog.provenance-notice=${CATALOG_PROVENANCE_NOTICE:}` mit leerem Default. Keine Ableitung aus XTF-Pfad, Dateiname oder Metadaten.
- **Ein gemeinsamer Chrome:** `CatalogPageVmFactory` injiziert `CatalogConfig` per CDI, `chrome()` ist Instanzmethode und gibt `config.provenanceNotice().orElse(null)` als normalen `String` an `Chrome`. `/`, `/thema`, `/themen/{identifier}/ebenen` und `/ebene/{id}` verwenden `catalog.html`, `theme.html` bzw. `detail.html`, alle mit `{#include layout chrome=…}`.
- **Escaping:** `layout.html` rendert `{#if chrome.provenanceNotice}<p class="provenance">{chrome.provenanceNotice}</p>{/if}`. Für die Konfiguration wird **kein** `RawString` verwendet; `RawString` bleibt ausschliesslich für bereits sanierte XTF-Beschreibungen (`safe(...)`) bestehen.
- **Unverändert:** `CatalogPages`/`@CheckedTemplate`, API-Routen/DTOs, Lucene-/Native-Hints, Sicherheitsparser, `CatalogStore`/`CatalogSnapshot`/`CatalogRepository`/`MockCatalogRepository`, Fixtures, Lizenzen und Vendorassets. Keine zusätzliche Produktivklasse gelöscht oder zusammengelegt.

## 3. Jede Muss-AC einzeln

| AC-ID | erfüllt / verletzt / nicht prüfbar | Eigener Code-/Test-/Prozessbeleg | Finding-ID |
|---|---|---|---|
| **AC3-01** | **erfüllt** | Nur `application.properties` definiert den XTF-Default; `CatalogConfig` ohne `@WithDefault`. Reale JVM-/Native-Starts mit Default, `file:`-XTF, `classpath:`-XTF (eigener neuer Quarkus-Test), `CATALOG_XTF_LOCATION` und System-Property-Override erfolgreich. Fehlende Datei, fehlende classpath-Ressource und `https:`-Quelle brachen jeweils mit **Exit 1 vor Ready** ab (`Cannot load catalog XTF`). | — |
| **AC3-02** | **erfüllt** | Ohne Notice kein `.provenance`-Absatz und kein harter Harvest-Satz auf `/`, `/thema`, Themenseite und Detailseite (Default-Harvest und alternative XTF). Mit Notice erscheint exakt der konfigurierte Text auf allen vier Vollseiten; `<script>`, `<img onerror>`, `</p>`, `<svg onload>`, `&`, `"` und `'` wurden als Literaltext ausgegeben, kein ausführbarer Knoten. Explizit leere Notice erzeugt keinen Absatz. | — |
| **AC3-03** | **erfüllt** | Alternative `search/catalog.xml` (12 Ebenen, ohne Harvest-Hinweis) zeigt keine falsche Provenienz. Vollständige API-/Katalog-/Themen-/Detail-/Facet-/Such-Payloads sind mit und ohne Notice **hashgleich**; Ranking (`q=atlas&sort=relevance` → `atlas` zuerst), Facets, Kartenlink und QML-Bytes bleiben unverändert. JVM und Native liefern identische Hashes. | — |
| **AC3-04** | **erfüllt** | Keine Löschung/Rename im Diff; 33 Produktionsklassen vor/nach. `CatalogRepository`/`CatalogStore`/`CatalogSnapshot`, `MockCatalogRepository`, Native-Reflection, Sanitizer, DTOs, Fixtures und Lizenzen blob-identisch. Native Suche und DI funktionieren real. | — |
| **AC3-05** | **erfüllt** | Frische T1–T3 und gezielte T4-Läufe: T1 **66/0/0/0** + **3/0/0/0**; T3 **14/0/0/0** Chromium+Firefox; T2 Runtime/Dev-CORS/Index erfolgreich inkl. 8 bzw. 1 erwarteter Fehlstarts; T4 **3/0/0/0** Native-Paket, **20** lokale JVM/Native-Distributionsfälle. Eigene XSS-/Alternativ-XTF-/fehlende-Quelle-Prozesse **10 pro Runtime** erfolgreich. Keine Versions-/DTO-/Rankingänderung. | — |
| **AC3-06** | **erfüllt** | Eigener neuer `own-provenance-probe.py` (nicht Codex' Probe) auf exakt `f2d85d7…`; eigener neuer Classpath-Test in temporärer Kopie Exit 0; absichtlich gebrochenes `@CheckedTemplate` (`{page.doesNotExistC3Canary}`) lässt `quarkusBuild` mit Qute-Templatefehler **Exit 1** werden. Quelle, Buildprüfung und SHA selbst verifiziert. | — |

## 4. Selbst entwickelte Gegenproben

### 4.1 Eigene Provenienz-/XSS-/Alternativ-XTF-Probe (JVM und Native)

- **Ziel/Hypothese:** Eine konfigurierte Notice muss auf allen vier Vollseiten als Text erscheinen und darf weder HTML-Knoten noch API-Inhalte verändern; eine alternative lokale XTF darf ohne Notice keine Harvest-Behauptung erzeugen; leere Notice und fehlende Quellen müssen korrekt behandelt werden; JVM und Native müssen dieselben Nutzlasten liefern.
- **Neue Eingabe:** Eigener stdlib-Probe `own-provenance-probe.py`; alternative XTF `src/test/resources/search/catalog.xml` (12 Ebenen) und `contract/with-qml.xml`; Notice
  `Prüfhinweis <img src=x onerror=alert(9001)> <script>alert(9001)</script> & "Doppel" 'einfach' </p><svg/onload=alert(9001)>`
  sowie realistische Colon-Notice `Datenhinweis: Der mitgelieferte Harvest ist unvollständig.` und explizit leere Notice.
- **Fälle:** Default-Harvest ohne Notice; alternative XTF ohne Notice; alternative XTF mit XSS-Notice; explizit leer; Colon-Notice; QML-XTF mit XSS-Notice; System-Property gegen absichtlich falsche Env-Quelle/Notice; fehlende Datei; fehlende classpath-Ressource; `https:`-Quelle. Jeder gute Start prüft alle vier Vollseiten; APIs werden kanonisch gehasht.
- **Ergebnis:** JVM **Exit 0**, Native **Exit 0**; Default **169 Themen / 886 Ebenen**; alternative XTF **12 Ebenen**, Ranking `atlas` zuerst, Filter-Facet `plan=3`; QML **62 Bytes** unverändert; alle drei Missing-/HTTP-Quellen **Exit 1 vor Ready**. `searchPlain == searchNotice` und JVM-Payloadhashes == Native-Payloadhashes (`payloadHashes equal JVM/native: True`). Die XSS-Seite enthält im `.provenance`-Block kein `<`/`>`; nach HTML-Unescape ergibt sich exakt die Notice; keine Inline-Scripts/Eventhandler/Angreifer-`img`.
- **Warum nicht trivial:** Die Probe vergleicht vollständige API-Payloads statt nur Zähler, prüft vier Vollseiten, eine echte alternative XTF, eine realistische Colon-Notice, explizit leere Konfiguration und Property-vs-Env-Präzedenz; sie hätte eine nur auf `print("PASS")` gestützte oder auf den Harvest beschränkte Implementierung nicht bestanden.
- **Rohbelege:** `/tmp/kartenkatalog-c3-review-f2d85d7/own-provenance-probe.py`, `own-probe-jvm/own-results-jvm.json`, `own-probe-native/own-results-native.json`, zugehörige `*.log`.

### 4.2 Eigene Classpath-Bindung in temporärer Kopie

- **Ziel:** Quarkus soll `catalog.xtf-location=classpath:search/catalog.xml` und eine Notice mit `<b>`/`&`/`"` real binden und escaped rendern.
- **Eingabe:** Temporäre Kopie des Kandidaten unter `/tmp/…/template-classpath-canary` (nicht der Kandidat), neue Testklasse `C3ReviewClasspathProbeTest` mit eigenem `QuarkusTestProfile`.
- **Ergebnis:** `./gradlew test --tests ch.so.agi.meta.C3ReviewClasspathProbeTest` **Exit 0**; 12 Ebenen; `config.xtfLocation()` = classpath-Wert; `config.provenanceNotice()` = `Optional.of(NOTICE)`; HTML enthält `&lt;b&gt;…` und nicht `<b>…`. XML: `own-classpath-canary-xml/TEST-ch.so.agi.meta.C3ReviewClasspathProbeTest.xml`.
- **Warum nicht trivial:** Andere Fixture und andere Notice als der Kandidatentest; prüft die tatsächliche SmallRye-/Quarkus-Bindung, nicht nur den String in `application.properties`.

### 4.3 Eigene `@CheckedTemplate`-Negativ-Canary

- **Ziel:** Der Build muss eine falsche Qute-Property in einem `@CheckedTemplate` tatsächlich rot machen.
- **Eingabe:** In derselben temporären Kopie `{page.doesNotExistC3Canary}` an `catalog.html` angehängt.
- **Ergebnis:** `./gradlew quarkusBuild` **Exit 1** (erwartet); Fehler:
  `catalog.html:10:1 - {page.doesNotExistC3Canary}: Property/method [doesNotExistC3Canary] not found on class [ch.so.agi.meta.web.CatalogViewModels$CatalogPage]`.
- **Warum nicht trivial:** Beweist die Source-Bindung/CheckedTemplate-Buildprüfung; ein blosses `grep` auf `@CheckedTemplate` hätte das nicht gezeigt.
- **Rohbeleg:** `/tmp/kartenkatalog-c3-review-f2d85d7/own-checked-template-canary.log`.

## 5. Pflichtgates und Rohbelege

Alle Kommandos mit `cwd=/Users/stefan/sources/kartenkatalog-cleanup-C3-review-f2d85d7`. Rohbelege unter `/tmp/kartenkatalog-c3-review-f2d85d7/`.

| Gate | Kommando | Exit | Tests / Errors / Failures / Skips | Report | SHA-Bindung |
|---|---|---|---|---|---|
| T0 Tree/Diff/Blob/Consumer | `git rev-parse`, `git cat-file`, `git diff --name-status`, Blob-/SHA256-Vergleich, `grep` | 0 | 13 Pfade; 128 unveränderte Blobs identisch; 33→33 Produktionsklassen; keine Löschung | `t0-identity.txt`, `t0-blobs.txt`, `t0-consumers-clean.txt`, `final-status.txt` | `f2d85d7…` |
| T1 JVM/Unit/API/Paket | `./gradlew clean test quarkusBuild quarkusIntTest --console=plain` | **0** | **66 / 0 / 0 / 0** + **3 / 0 / 0 / 0** | `t1-jvm.log`, `t1-xml/**`, `pre-native/test-results/**` | `f2d85d7…` |
| T2 Runtime | `python3 scripts/runtime-process-contract.py` | **0** | Gutstart ready 3.069 s; HTTP-Contract 0; **8** erwartete Fehlstarts je **Exit 1** vor Ready | `t2-runtime.log`, `pre-native/reports/runtime/process-summary.json` | `f2d85d7…` |
| T2 Dev/CORS | `python3 scripts/runtime-process-contract.py --dev` | **0** | Dev ready 12.728 s; erlaubte OPTIONS/GET **200**, fremde **403**; keine Credentials | `t2-runtime-dev.log`, `pre-native/reports/runtime/dev-summary.json` | `f2d85d7…` |
| T2 RAM-Index | `python3 scripts/search-index-contract.py` | **0** | Harvest 886; first/second/changed je 12; broken-index **Exit 1**; keine Indexdateien | `t2-index.log`, `pre-native/reports/search-index/summary.json` | `f2d85d7…` |
| T3 Browser | `./gradlew playwrightTest --console=plain` | **0** | **14 / 0 / 0 / 0** (Catalog 6, Filter 8); Chromium+Firefox; 1440×900/390×844; 26 Screenshot-/Logdateien | `t3-playwright.log`, `t3-xml/**`, `pre-native/reports/browser/**` | `f2d85d7…` |
| T4 Native macOS/arm64 | `GRAALVM_HOME=/Users/stefan/.sdkman/candidates/java/25.0.3-graal ./gradlew clean testNative -Dquarkus.native.enabled=true -Dquarkus.package.jar.enabled=false -Dquarkus.native.native-image-xmx=4g --console=plain` | **0** | **3 / 0 / 0 / 0** Native-Pakettests; Mach-O-arm64-Binary `8f333e5d21bb393b46968c45e83339a112502e1ed733f09cedd2e0bac6739dad` | `t4-native-build.log`, `native/application`, `native/test-results/`, `t4-xml/` | `f2d85d7…` |
| T4 lokale JVM/Native-Parität | `python3 scripts/distribution-contract.py --native-executable /tmp/…/native/application --report-dir build/reports/cleanup-review/distribution` | **0** | **20 reale Fälle** (10 je Runtime: 5 Gut-/Restart-/Cross-Fälle, 1 geänderte XTF, 4 erwartete Fehlstarts); identische Payload-Hashes JVM=Native | `t4-distribution.log`, `t4-distribution-reports/summary.json`, `*/{jvm,native}-*.log` | `f2d85d7…` |
| Eigene JVM-Probe | `python3 own-provenance-probe.py --runtime jvm --jar pre-native/quarkus-app/quarkus-run.jar` | **0** | 10 Fälle; Default 169/886, Alt-XTF 12; 3 erwartete Missing-/HTTP-Exits 1; Payloadparität | `own-probe-jvm.log`, `own-probe-jvm/own-results-jvm.json` | `f2d85d7…` |
| Eigene Native-Probe | `python3 own-provenance-probe.py --runtime native --executable native/application` | **0** | 10 Fälle; gleiche Hashes wie JVM | `own-probe-native.log`, `own-probe-native/own-results-native.json` | `f2d85d7…` |
| Eigene Classpath-Probe | `./gradlew test --tests ch.so.agi.meta.C3ReviewClasspathProbeTest` (temporäre Kopie) | **0** | 1 neuer Test; classpath-Bindung + Escaping | `own-classpath-canary.log`, `own-classpath-canary-xml/` | `f2d85d7…` |
| Eigene CheckedTemplate-Canary | `./gradlew quarkusBuild` mit absichtlich falscher Property (temporäre Kopie) | **1** (erwartet) | Qute-Templatefehler `doesNotExistC3Canary` | `own-checked-template-canary.log` | `f2d85d7…` |
| Dependencies | `./gradlew dependencies --configuration runtimeClasspath --console=plain` | **0** | Quarkus 3.40.1.1, Lucene 10.6.0, jsoup 1.23.2; kein Spring/JDBC/PostgreSQL/DuckDB/JTE | `dependencies.log` | `f2d85d7…` |
| T5 (nicht C3-fällig, zusätzlich) | `python3 scripts/versioning-contract.py`; `python3 scripts/release-contract.py` | **0 / 0** | 4 gültige Versionen, 5 ungültige Run-Nummern abgelehnt; 6 unittest, 0 Fehler/Skips | `t5-versioning.log`, `t5-release.log` | `f2d85d7…` |
| Hygiene | `git status --porcelain=v1`; `git diff --check BASE CANDIDATE`; `lsof`/`ps` | 0; Status leer | keine getrackte Änderung; keine App-Listener/Prozesse; keine `build/reports/p*` | `final-status.txt` | `f2d85d7…` |

- **Nicht ausgeführte fällige C3-Prüfungen:** **keine.**
- **Bewusst nicht fällig / nicht behauptet:** Linux-/Container-/linux-amd64-Gesamtgate (C4), T5-Pflicht (C3 ändert weder Scripts noch CI; die beiden T5-Harnesses liefen nur zusätzlich), C4-Dokumentations-/Archivgate. Der macOS-arm64-Native-Lauf wird **nicht** als Linux/amd64-Nachweis ausgegeben.
- **Native-/Linux-/Containerarchitektur:** Nur **macOS/arm64** (Mach-O 64-bit arm64, SHA256 `8f333e5d…`). Kein Docker-/Linux-Lauf in C3.
- **Release-/Tag-/Triggerprüfung ohne Push:** In C3 keine Workflow-/Script-Änderung; `versioning-contract.py`/`release-contract.py` read-only/fake-Registry grün. **Kein** `git push`, Merge, Tag, Workflow-Dispatch, Registry-Push oder Release.

## 6. Findings

- **Keine funktionalen, Test-, CI-, Fixture-, Provenienz-, XSS- oder Native-Findings.**
- **Keine verletzte Muss-AC.**
- **Blocker nach INFRA / MISSING_EVIDENCE / SCOPE_DECISION:** keine.
- **Nicht blockierende Beobachtung:** Die aktiven Dokumente `README.md` und `src/test/resources/contract/README.md` verlinken `P4.md`/`P0.md` ausdrücklich als historische Berichte. Das ist bis C4 gemäss Plan zulässig; die Archivierung ist C4, kein C3-Verstoss.

## 7. Urteil

**PASS**

- **Begründende Muss-Kriterien:** AC3-01 bis AC3-06 sind einzeln anhand eigener Git-/Blob-/Consumer-/Config-/Prozess-/Browser-/Native- und XSS-Belege auf exakt `f2d85d71422eca803b0a8367a2606d96642325cb` erfüllt. Der XTF-Default ist nur noch in `application.properties` definiert; file-/classpath-/Env-/Property-Bindung und Fail-fast bei fehlenden/falschen Quellen sind real geprüft. Die konfigurierte Notice erscheint ohne `RawString` escaped auf allen vier Vollseiten; ohne Notice entsteht kein Harvest-Absatz. Alternative XTF, API, Suche, Facets, Ranking, Kartenlink und QML bleiben unabhängig von der Notice und zwischen JVM und Native identisch. Es gab keine zusätzliche Klassenlöschung; alle DI-/Native-/Sanitizer-/DTO-/Fixture-Grenzen blieben erhalten. T1–T3 und das gezielte T4 liefen frisch; zwei eigene neue Canaries (XSS/Alternativ-XTF/Config sowie Classpath/CheckedTemplate) belegen die Wirksamkeit. Der Kandidat blieb tracked-sauber.
- **Einschränkung:** Der PASS gilt für **C3**, **nicht** als C4-Gesamtfreigabe und **nicht** als Linux/amd64-Nachweis. C4 wurde nicht begonnen.
- **Nächster zulässiger Schritt:** C3-Review und Status dürfen in einem rein dokumentarischen Commit archiviert werden; danach kann C4 gemäss Plan beginnen. **Kein Push nach `main`, kein Merge, kein Git-Tag, kein Workflow-Dispatch, keine Registry-Publikation und kein Release** ohne separate Nutzerentscheidung.

---

**Geprüfter Kandidat:** `f2d85d71422eca803b0a8367a2606d96642325cb`  
**Geprüfte Basis:** `17f1c25a6c60c563525cae63a7d3064d7597f119`  
**Review-Worktree:** `/Users/stefan/sources/kartenkatalog-cleanup-C3-review-f2d85d7` (tracked clean vor/nach; HEAD unverändert)  
**Rohbelege:** `/tmp/kartenkatalog-c3-review-f2d85d7/`  
**Reviewbericht:** `/Users/stefan/sources/kartenkatalog-C3-review-f2d85d7.md`
