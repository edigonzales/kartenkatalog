# Unabhängiger Review C4 (Wiederholung) – Kartenkatalog Cleanup

**Datum:** 1. November 2026  
**Rolle:** unabhängiger Reviewer, keine Implementierung, keine Kandidatenänderung  
**Repository:** `edigonzales/kartenkatalog`

## 1. Identität und Umgebung

### 1.1 Lieferung und SHAs

- **Lieferabschnitt / Gate:** C4 – aktive Dokumentation, Archiv und kumulative Gesamtabnahme. **PHASENGATE: ja.**
- **BASE_SHA:** `ec57d04b8688617ddfe3a36070f23d15459fce30` (`git cat-file -t` = commit; nach C3-PASS-Archivierung).
- **CANDIDATE_SHA:** `fd7164d413fde495465d8c295d645cb5e453b233` (`git rev-parse HEAD`).
- **Ancestry:** `git merge-base --is-ancestor BASE CANDIDATE` = 0; `git rev-list --count BASE..CANDIDATE` = 2. Der erste C4-Kandidat `0b1704c72113a9872a10cf68110f506c6592a547` ist direkter Elternknoten des neuen Kandidaten; BASE ist dessen Elternknoten.
- **Erster C4-Kandidat (FAIL):** `0b1704c72113a9872a10cf68110f506c6592a547`.
- **Erster unabhängiger Bericht:** `/Users/stefan/sources/kartenkatalog-C4-review-0b1704c.md`, SHA256 `78f0e9e2d40b1d14dc906877538d357fb47795d962fbc90ee436998b542be0f2`; Rohbelege dauerhaft unter `/Users/stefan/sources/kartenkatalog-C4-review-evidence/0b1704c72113a9872a10cf68110f506c6592a547/`. Beide wurden nicht verändert.
- **Review-Worktree:** `/Users/stefan/sources/kartenkatalog-cleanup-C4-review-fd7164d` (detached HEAD). `git status --porcelain=v1` war **vor und nach** allen Prüfungen leer; HEAD blieb unverändert.
- **Kein `AGENTS.md`** im Repository oder in dessen Elternpfaden bis `/` gefunden.

### 1.2 Provider-/Modellidentität (nicht aus Modellselbstauskunft)

- **OpenCode-GUI:** Version **2.1.4** auf macOS. Belege: `/Applications/OpenCode.app/Contents/Info.plist` (`CFBundleShortVersionString = 2.1.4`), laufender Dienst `/Users/stefan/Library/Application Support/ai.opencode.desktop/cli/2.1.4-arm64/opencode-cli serve --service`.
- **Aktuelle Session:** `ses_e68dd951cfferTEMDQ63CPh2nX`, Titel „C4-Wiederholungsreview fd7164d“, `version = 2.1.4`, `agent = build`, `model = {"id":"deepseek-flash","providerID":"deepseek","variant":"high"}`. Quelle: lokale OpenCode-Session-Metadaten (`session_v2`), nicht Modellausgabe.
- **Provider:** `deepseek` (Anzeige „DeepSeek“), `api = https://api.deepseek.com`, `npm = @ai-sdk/openai-compatible`, Env-Name `DEEPSEEK_API_KEY` (kein Credentialwert gelesen oder ausgegeben).
- **Modell:** `deepseek/deepseek-flash`, Anzeigename **„DeepSeek V4.1 Flash 1017“**, Release `2026-10-17`, `canonical_model_id = deepseek/deepseek-v4.1-flash-1017`, Session-Variante `high`.
- **Kein OpenRouter-/Zen-Fallback:** Die Session nennt direkt `providerID = deepseek`; der Modellkatalog bestätigt den direkten Endpunkt `https://api.deepseek.com`. Eine TCP-Verbindung des laufenden Desktop-Prozesses zu `3.173.21.63:443` (api.deepseek.com) war etabliert. Es wurde **kein OpenCode-CLI-Aufruf** und kein CLI-Fallback verwendet; `auth.json`/Credentials wurden nicht gelesen.
- **Hinweis:** Der lokale Modellkatalog enthält auch andere Provider (u. a. OpenRouter/Zen), die Session-Auswahl ist jedoch ausschließlich `deepseek`; daraus wird keine Fallback-Nutzung abgeleitet.
- **Rohbelege:** `/tmp/kartenkatalog-c4-review-fd7164d/evidence/identity/identity.txt`, `.../session.json`, `.../catalog.json`.

### 1.3 Toolchain und Zielarchitektur

- macOS 15.8.1, **arm64**.
- Temurin **Java 21.0.7+6-LTS**; Gradle Wrapper **9.8.2**; Python **3.9.6** mit `sys.flags.optimize == 0`.
- Playwright **1.60.0**, Chromium **und** Firefox; Viewports **1440×900** und **390×844**.
- Docker **29.8.2**, Server **linux/arm64**.
- GraalVM **25.0.3** unter `/Users/stefan/.sdkman/candidates/java/25.0.3-graal` für den lokalen Native-Build.
- Quarkus **3.40.1.1**, Lucene **10.6.0**, Jsoup **1.23.2** aus unverändertem `build.gradle`.
- **Architekturen:** `testNative` lokal = **macOS/arm64 (Mach-O)**; Linux-Native/Container = **linux/arm64 (ELF/OCI)**. **Kein linux/amd64-Erfolg** wurde ausgeführt oder behauptet. Der Release-Workflow ist statisch als amd64-Ziel geprüft, nicht als lokaler Kandidatenlauf.

## 2. Eigene Source-, Git- und Consumeranalyse

### 2.1 Umfang des Kandidaten

`git diff --name-status BASE..CANDIDATE` enthält ausschließlich Dokumentation und Archivverschiebungen:

- `M README.md`
- `A docs/architecture.md`
- `M docs/cleanup/STATUS.md`
- `A docs/cleanup/implementation/C4.md`
- `A docs/history/README.md`
- 26 × `R100` aus `docs/quarkus-migration/**` nach `docs/history/quarkus-migration/**`
- `R100 docs/legacy-notes.md` → `docs/history/legacy-notes.md`
- `A docs/operations.md`
- `A docs/testing.md`
- `M src/test/resources/contract/README.md`

**Keine** Änderung an Java-/Python-/JS-/CSS-/Template-/Build-/CI-/Deploy-/Daten-/Testdateien, keine Versionsänderung. `git diff --stat` = 35 Dateien, 496 Insertions, 99 Deletions (davon überwiegend neue Dokumente und R100-Renames).

### 2.2 Änderung seit dem ersten C4-Kandidaten

`git diff --stat 0b1704c... fd7164d...` = **3 Dateien, 11 Insertions, 2 Deletions**:

- `docs/architecture.md`: genau eine zusätzliche EOF-Leerzeile entfernt; Datei endet jetzt mit genau einem `\n`, 0 Zeilen mit Trailing Whitespace.
- `docs/cleanup/STATUS.md`: Nacharbeitsstatus und Verweis auf den ersten FAIL.
- `docs/cleanup/implementation/C4.md`: dokumentierte Nacharbeit/Verweis auf den ersten Bericht.

Damit ist die Behauptung „seit Erstkandidat ausschließlich EOF-Leerzeile plus Status/Bericht“ **selbst bestätigt**. Die erste FAIL-Ursache F1 (`git diff --check` Exit 2) ist behoben; `git diff --check BASE CANDIDATE` = **Exit 0**.

### 2.3 Archivblobidentität aller 27 Originale gegen BASE

Eigener Vergleich über `git ls-tree -r BASE -- docs/quarkus-migration docs/legacy-notes.md` und `git rev-parse CANDIDATE:<neuer Pfad>`. **27/27 OK**, alle `R100`, keine fehlende oder geänderte Datei:

| Original (BASE) | Neuer Pfad (CANDIDATE) | Git-Blob |
|---|---|---|
| `docs/legacy-notes.md` | `docs/history/legacy-notes.md` | `5d721f9adb2c9f64d2172d5a742604de438f8c46` |
| `quarkus-migration/CODEX_STARTPROMPT.md` | `history/quarkus-migration/CODEX_STARTPROMPT.md` | `57d194c428d394538b5f5b353f401eb09eaedea5` |
| `quarkus-migration/EXECUTION_PLAN.md` | `history/quarkus-migration/EXECUTION_PLAN.md` | `5d6ac4d4b3cd9274a89d0a4e62dd7bc5254eca44` |
| `quarkus-migration/OPENCODE_REVIEW_PROMPT.md` | `history/quarkus-migration/OPENCODE_REVIEW_PROMPT.md` | `80f6043f28ab54f76df28fb7fb23c91e48cc6486` |
| `quarkus-migration/OPERATIONS.md` | `history/quarkus-migration/OPERATIONS.md` | `26f735609dbff39d554efe9202f896a8b1b488e3` |
| `quarkus-migration/STATUS.md` | `history/quarkus-migration/STATUS.md` | `c57bc4d7b2e09a49a983e496ff1cdf09e9ffab90` |
| `implementation/P0.md` | `history/quarkus-migration/implementation/P0.md` | `0ff40ab344fe23ebaed888cb2326aecbd9690452` |
| `implementation/P1.md` | `history/quarkus-migration/implementation/P1.md` | `b2a297a1901d603f88aea0248ce86e34a2a814cf` |
| `implementation/P2.md` | `history/quarkus-migration/implementation/P2.md` | `158f8917e50cb93a56201098e51725b9d301cac7` |
| `implementation/P3a.md` | `history/quarkus-migration/implementation/P3a.md` | `35f31b4ac529d353b0eae620769753d547391328` |
| `implementation/P3b.md` | `history/quarkus-migration/implementation/P3b.md` | `62615082f7efa8ae8149c6f1a78db8d4ece82de7` |
| `implementation/P4.md` | `history/quarkus-migration/implementation/P4.md` | `11a1a8856c8704003c76a955ad9876c9bb157fb2` |
| `implementation/P5.md` | `history/quarkus-migration/implementation/P5.md` | `c437d5d45831b23e5ab740b2e1272c0f4455b89a` |
| `implementation/RELEASE.md` | `history/quarkus-migration/implementation/RELEASE.md` | `6edd6fdff767e6c598ee200c6ef6e233bf04d994` |
| `reference/README.md` | `history/quarkus-migration/reference/README.md` | `f9ab83e2e7621fb353136dda3a499f1549119f75` |
| `reference/sodata-desktop.png` | `history/quarkus-migration/reference/sodata-desktop.png` | `fcdd45980986b51ce2309d1e6677cba9977b6849` |
| `reference/sodata-filter-desktop.png` | `history/quarkus-migration/reference/sodata-filter-desktop.png` | `ba4a2af776f2626c6a593d633aba73e394855fa0` |
| `reference/sodata-filter-mobile.png` | `history/quarkus-migration/reference/sodata-filter-mobile.png` | `b4af31ac85a8232dfa301881188782e7c3f91ca1` |
| `reviews/P0-review.md` | `history/quarkus-migration/reviews/P0-review.md` | `568f300c0fa6fbd6e88b86da50bad72c214df681` |
| `reviews/P1-review.md` | `history/quarkus-migration/reviews/P1-review.md` | `dcad170a48f67dcdf878f018cbbce1e90bca5bca` |
| `reviews/P2-review.md` | `history/quarkus-migration/reviews/P2-review.md` | `8d37e7c6c48f19c0b7b803567af7a102d397cdb0` |
| `reviews/P3a-review.md` | `history/quarkus-migration/reviews/P3a-review.md` | `51ee723d43e0388a65f0d546d042c5b9d6a6e078` |
| `reviews/P3b-review.md` | `history/quarkus-migration/reviews/P3b-review.md` | `93f0d522c79b0ce95090f7f8d22a7e46846594b4` |
| `reviews/P4-review.md` | `history/quarkus-migration/reviews/P4-review.md` | `47fa4bd5d9174c2fa6e26824b15b3aed9c51c8a9` |
| `reviews/P5-review.md` | `history/quarkus-migration/reviews/P5-review.md` | `b5e6ad5f813315e9ad2875eb4e3f0a2ffbfb6d27` |
| `reviews/RELEASE-initial-review.md` | `history/quarkus-migration/reviews/RELEASE-initial-review.md` | `5554db5f0deaf9319861eec1712678f35edf692c` |
| `reviews/RELEASE-review.md` | `history/quarkus-migration/reviews/RELEASE-review.md` | `e1a9cbc0e2f46de90bcbd9e32795890b40af7a12` |

Zusätzlich geprüft: Der frühere Release-`FAIL` in `RELEASE-initial-review.md` ist bytegleich erhalten; die drei SODATA-PNGs sind unverändert (1440×900, 1440×900, 390×844); `OPERATIONS.md`, Prompts, Status und alle P-Implementierungen/Reviews sind unverändert. **Keine historische Datei wurde editiert.**

Rohbeleg: `/tmp/kartenkatalog-c4-review-fd7164d/evidence/archive-identity.txt`.

### 2.4 Frühere C0–C3-Reviews und kumulative Ausgangslage

Die tatsächlichen früheren Cleanup-Reviews sind unverändert im Kandidaten und waren echte unabhängige PASS-Berichte:

| Review | Kandidat | Git-Blob in BASE/CANDIDATE |
|---|---|---|
| C0 | `1378d3f5b2bb7a210efa5f704c94a4e43ba23643` | `db0c32f98b8ae3ea2af89721324cd00782fa9cdb` |
| C1 | `b3df50894a40e672612b213de01094685062db76` | `b55d915fbca64cee05f2395424c1fdd30aa21d9a` |
| C2 | `c93714d2e08238e7549da62487c87ef5291a2df0` | `7828c3fef58a19028d6c4fe105fe625514f678af` |
| C3 | `f2d85d71422eca803b0a8367a2606d96642325cb` | `2b1a30edf0db568281803d0cd3bf5682d6a948a5` |

- Die C0-Inventur am Baseline-Commit `1378d3f` enthält die drei Root-Artefakte `kartenkatalog.duckdb.zip`, `kartenkatalog.xtf.zip`, `mockup_C_tree_clean.png`; im heutigen Tree/Dateisystem sind sie **abwesend**. Das ist konsistent mit der C1-Entfernung, nicht mit einem nachträglich umgedeuteten C0.
- C1 entfernte die drei Artefakte und bereinigte `.gitignore`.
- C2 benannte die vier Harnesse, zwei Testklassen und zwei Fixtures um; die Fixture-Blobs sind bytegleich:
  - `baseline.json` → `public-api.json`: Blob `f193170aabc9082f8ba72e81675e0ca142bb8841`, SHA256 `cd1167f8870b5dae021b913c08b7643427683ed406e3018e915cbab4feff219b`.
  - `final/catalog.xml` → `integration/catalog.xml`: Blob `1772f1544bb7ef3e4ca2c5b16c0a97fda72ac24d`, SHA256 `4f2e54495e0b852b06e5a35e76763216afa04e6da5b1d4383437c9de732ec510`.
  - `with-qml.xml` unverändert; `MockCatalogRepository` erhalten.
- C3 reduzierte den XTF-Default auf `application.properties`, führte die optionale escaped Provenienz ein und ließ DI-/Native-/Sanitizer-/DTO-/Fixture-Grenzen unverändert. Die C3-Review belegt dies mit 66/3/0/0 + 3/0/0/0, Browser 14/0/0/0 und gezieltem Native-Test.

### 2.5 Daten, Fixtures und öffentliche Verträge

Eigene Hashes/SHA256:

- `data/kartenkatalog-harvest-ohne-qml.xtf`: `de40f025368ae6fc70f5858ab42592488bca837c6942cb6ff8759e6c8803de86`; Modell `SO_AGI_Kartenkatalog_Publikation_20251210`; **169 `<k:Thema>` / 886 `<k:Ebene>`**; nur diese Fixture, ausdrücklich INCOMPLETE.
- `data/README.md`: `3744512640d0e2a6ec0ab13fec2929bcc541bdad35ee12a87fce7ce1a6e4736c`.
- `src/test/resources/contract/public-api.json`: `cd1167f8870b5dae021b913c08b7643427683ed406e3018e915cbab4feff219b`.
- `src/test/resources/integration/catalog.xml`: `4f2e54495e0b852b06e5a35e76763216afa04e6da5b1d4383437c9de732ec510`.
- Nur `data/kartenkatalog-harvest-ohne-qml.xtf` ist als XTF getrackt; keine Root-ZIPs/PNG mehr.

### 2.6 Aktive Namen, Ignore, Abhängigkeiten und Sicherheitsgrenzen

- **Aktive Phasennamen:** `git grep` über aktive Java-/Python-/Gradle-/YAML-/Properties-/HTML-/JS-/XML-Dateien (ohne `docs/**`) findet **keine** `P0/P1/P2/P3a/P3b/P4/P5`, `P3ITTest`, `BaselineContractTests`, `BaselineQmlTests`, `baseline.json`, `final/catalog.xml` oder `build/reports/p*`. Die vier alten Scriptnamen und die Root-Artefaktnamen haben außerhalb `docs/history/**`/`docs/cleanup/**` **keine Treffer**. Historische Belege sind ausgenommen.
- **Keine Legacy-Runtime:** `git grep` findet keine Spring-/JTE-/JDBC-/PostgreSQL-/DuckDB-Referenzen in aktiven Quellen; `./gradlew dependencies --configuration runtimeClasspath` (Exit 0) enthält Quarkus 3.40.1.1, Lucene 10.6.0, Jsoup 1.23.2 und **keine** DB-/Spring-/JTE-Abhängigkeit.
- **RAM-Index:** `CatalogSearchIndexBuilder` nutzt ausschließlich `ByteBuffersDirectory`; `GermanAnalyzer` mit `CharArraySet.EMPTY_SET`; keine `FSDirectory`/`MMapDirectory`/Dateipersistenz. `LuceneNativeConfiguration` registriert `KeywordAttributeImpl` weiterhin.
- **XTF-Sicherheit:** `XtfCatalogParser` blockiert DTD/XXE/externe Entities, prüft Modell, Pflichtfelder, IDs, UUIDs und Themenreferenzen; Fehler verhindern Start/Ready.
- **Ignore-Canaries** (`git check-ignore -q --no-index`):
  - ignoriert: `data/anderes.xtf`, `.env`, `build/x`, `.gradle/x`, `.idea/x`, `__pycache__/x.pyc`;
  - **nicht** ignoriert: die erlaubte Harvest-XTF, `src/main/resources/application.properties`, `gradlew`, `gradle/wrapper/gradle-wrapper.jar`.
  - `.dockerignore` ist eine Whitelist für `build/quarkus-app/**` und `build/*-runner`; beide Images bauten erfolgreich aus diesem Kontext.
- **Doku-Links:** eigener Link-/Ankercheck über **45 Markdown-Dateien, 59 lokale Links, 0 Fehler** (inklusive aktiver und archivierter Dokumente). Der Checker prüft Dateiexistenz und Heading-Anker; historische relative Links bleiben nach der gemeinsamen Verschiebung gültig.

## 3. Jede Muss-AC einzeln

| AC-ID | erfüllt / verletzt / nicht prüfbar | Eigener Code-/Test-/Prozessbeleg | Finding-ID |
|---|---|---|---|
| **AC0-01** | **erfüllt** | BASE/CANDIDATE per `cat-file`/`rev-parse`; separater Worktree; `git status` vor/nach leer; `git ls-files` = 147; C0-Review `1378d3f` dokumentiert 135-Dateien-Inventur, Toolchain und Main-Push-/Docker-Hub-/Mirror-Wirkung. | — |
| **AC0-02** | **erfüllt** | C0-Baum `1378d3f` enthält die drei Root-Artefakte; heutiger Tree/Dateisystem enthält sie nicht; C0-Bericht unterscheidet DuckDB-ZIP, altes XTF-2.3-ZIP und PNG, aktive Harvest separat. Eigene Hashes/169/886 und Contract-Bytes reproduziert; kein Löschbeweis nur über GitHub-Suche. | — |
| **AC0-03** | **erfüllt** | C0-Review dokumentiert frische T1 62/0/0/0 + 3/0/0/0, T2/T3/T5 und ausdrücklich **keinen** Native-Erfolg. Aktueller Kandidat frisch: 66/3/14/3-Paket sowie T2/T4/T5; keine vorgetäuschte Native-Abnahme. | — |
| **AC0-04** | **erfüllt** | `P3ITTest` ist im realen Baum nicht vorhanden; aktuelle Testklassen sind fachlich benannt; aktiver Phasenscan ohne Treffer; C0-Review bestätigt den Negativbefund und die echten Baseline-/Phasenreste. | — |
| **AC0-05** | **erfüllt** | `docs/cleanup/reviews/C0-review.md` ist ein echter unabhängiger PASS auf `1378d3f`, Blob `db0c32f…` in BASE/CANDIDATE identisch; der Bericht enthält eine eigene Golden-Contract-Mutation/Referenzkette. | — |
| **AC1-01** | **erfüllt** | C1-Kandidat `b3df508` entfernt die drei Root-Artefakte; aktueller Tree/Dateisystem ohne sie; `git grep` findet außerhalb der Historie keine aktiven Consumer; Git-Historie bleibt; kein History-Rewrite/Force-Push. | — |
| **AC1-02** | **erfüllt** | Nur die Harvest-XTF ist getrackt; SHA256 `de40f025…`, Modell `SO_AGI_Kartenkatalog_Publikation_20251210`, 169/886; `data/README.md` und Testfixtures unverändert. | — |
| **AC1-03** | **erfüllt** | `.gitignore` ist projektspezifisch; positive/negative `check-ignore`-Canaries (siehe §2.6); `.dockerignore`-Whitelist durch erfolgreiche JVM-/Native-Image-Builds bestätigt. | — |
| **AC1-04** | **erfüllt** | T1 **66/0/0/0 + 3/0/0/0**, T2 Runtime/Dev/Index Exit 0; keine `@Disabled`/`assumeTrue`/Testselektion; `git diff --check BASE CANDIDATE` **Exit 0**; keine Testabschwächung (C3 erhöhte 62 auf 66). | — |
| **AC1-05** | **erfüllt** | C1-Review ist unverändert (Blob `b55d915…`) und dokumentiert eigene Ignore-/Fixture-Gegenprobe; eigener Ignore-Canary in §2.6. | — |
| **AC2-01** | **erfüllt** | Nur `scripts/{api-http,runtime-process,search-index,distribution,versioning,release}-contract.py` und `verify-published-images.py`; alte `p0/p1/p2/p4`-Dateien fehlen; CI/Doku verwenden neue Pfade; erzeugte Reports liegen unter `runtime`, `search-index`, `browser/catalog`, `browser/filters`, nicht `build/reports/p*`. | — |
| **AC2-02** | **erfüllt** | `runtime-process-contract.py` ruft `[sys.executable, 'scripts/api-http-contract.py', …]` auf; Distribution nutzt `integration/catalog.xml`; CLI-Optionen `--native-executable`, `--jvm-image`, `--native-image`, `--expected-version`, `--expected-revision`, `--expected-platform`, `--report-dir` sind vorhanden; echte Negativfälle 8/1 erwartete Exits. | — |
| **AC2-03** | **erfüllt** | `CatalogApiContractTests`/`CatalogQmlContractTests` ersetzen die Baseline-Klassen; Fixture-Blobs `f193170…` und `1772f15…` bytegleich; T1 66/3, 0 Failures/Errors/Skips; `MockCatalogRepository` vorhanden und genutzt. | — |
| **AC2-04** | **erfüllt** | `playwrightTest` **14/0/0/0**: `CatalogBrowserTests` 6, `CatalogFilterBrowserTests` 8; Chromium **und** Firefox, 1440×900/390×844; Screenshots/JVM-Logs unter `build/reports/browser/catalog` und `browser/filters`; keine Assertion-/Viewport-Änderung. | — |
| **AC2-05** | **erfüllt** | Struktureller CI-Audit: `linux-native needs: jvm`; alle JVM-/Browser-/Runtime-/Dev-/Index-Gates vor Login/Publish; gepinnter Mandrel-Builder; `--expected-platform linux/amd64`/`--expected-revision "$GITHUB_SHA"`; Login/Publish/Verify nur Main-Push/Dispatch; zwei Vierer-Tagloops = **acht Tags**; Mirror auf Push nach Codeberg. Kein Push ausgelöst. | — |
| **AC2-06** | **erfüllt** | Frische T1/T2/T3/T5; echte Exitcodes und XML-Zahlen; Runtime 8 erwartete Fehlstarts, Indexfehlstart Exit 1; mindestens eine absichtlich fehlschlagende Probe (Probe 1: API-Golden-Mutation Exit 1); keine Publikation. | — |
| **AC2-07** | **erfüllt** | C2-Review ist echter PASS auf `c93714d` (Blob `7828c3f…`); eigener Probe-1-Scan/Mutationen (fehlendes Script, PR-Publish, fehlende Plattform) und API-Golden-Mutation bestätigen die Empfindlichkeit. | — |
| **AC3-01** | **erfüllt** | Nur `application.properties` definiert `catalog.xtf-location=${CATALOG_XTF_LOCATION:file:./data/…}`; `CatalogConfig` ohne `@WithDefault`; `file:`/`classpath:`/Env-Override und fehlende/`https:`-Quelle real geprüft; Fehlstart vor Ready. | — |
| **AC3-02** | **erfüllt** | Ohne Notice kein `.provenance`-Absatz/Harvest-Satz; gesetzte Notice auf `/`, `/thema`, Themenseite, Detail sichtbar; `<script>`/`<img onerror>`/`<svg onload>` escaped, kein ausführbarer Knoten; `CatalogProvenanceTests` 3 Fälle und Probe 2. | — |
| **AC3-03** | **erfüllt** | Alternative XTF (Probe 2: 2 Themen/4 Ebenen) ohne falsche Harvest-Behauptung; Suche/Filter/Facets/API/Detail unabhängig; JVM und Native liefern identische Seiten-/API-Hashes und exakte QML-Bytes. | — |
| **AC3-04** | **erfüllt** | Keine Klassenlöschung/‑zusammenlegung im kumulativen Diff; `CatalogStore`/`CatalogRepository`/`CatalogSnapshot`, `MockCatalogRepository`, `LuceneNativeConfiguration`, Sanitizer, DTOs, Fixtures, Lizenzen und Native-Hints erhalten; keine neue Persistenz. | — |
| **AC3-05** | **erfüllt** | Frische T1 66/3, T2 Runtime/Dev/Index, T3 14 Browser, T4 3 Native-Paket + 20 lokale + 20 Linux-Distributionsfälle; XSS-/Alternativ-XTF-/Missing-Source-/Indexfehler-Fälle; keine Versions-/DTO-/Rankingänderung. | — |
| **AC3-06** | **erfüllt** | C3-Review auf `f2d85d7` (Blob `2b1a30e…`) mit eigener XSS-/Alternativ-XTF-/Classpath-/`@CheckedTemplate`-Probe; zusätzlich eigene neue Probe 2 auf dem aktuellen SHA mit alternativer XTF, gefährlicher Notice und JVM/Native-Parität. | — |
| **AC4-01** | **erfüllt** | `README.md`, `docs/architecture.md`, `docs/operations.md`, `docs/testing.md`, `docs/history/README.md`, `data/README.md`, Contract-README gelesen und gegen Source/Build/CI/Dockerfiles geprüft: Java 21, Quarkus 3.40.1.1, Lucene 10.6.0, Gradle 9.8.2, XTF-only, QWC2-API, null/Arrays, Notice, Native-/Releasegrenzen, acht Tags, Main-Push-/Mirrorwirkung. Keine alten ausführbaren Namen/Spring-Anleitungen; 59/59 lokale Links/Anker gültig. | — |
| **AC4-02** | **erfüllt** | **27/27** historische Originale bytegleich gegen BASE (Tabelle §2.3); früherer Release-FAIL, alle Reviews, Implementierungen, Prompts, STATUS, OPERATIONS, drei Referenzbilder und `legacy-notes.md` erhalten; keine historische Datei editiert. | — |
| **AC4-03** | **erfüllt** | Eigener Linkcheck: 45 Markdown-Dateien, 59 lokale Links, **0 bad**; Report-/Fixture-/Scriptpfade existieren; alter `docs/quarkus-migration/`-Pfad ist in aktiven Anleitungen nicht verwendet; History-Index kartiert den alten Pfad; Probe 1 erkennt einen absichtlich alten README-Pfad mit Exit 1. | — |
| **AC4-04** | **erfüllt** | Root-Artefakte weg; aktive Java-/Script-/Test-/CI-Namen phasenfrei; keine Spring-/JTE-/JDBC-/PostgreSQL-/DuckDB-Abhängigkeit/neue Persistenz; historische Belege ausgenommen; `git ls-files` 147. | — |
| **AC4-05** | **erfüllt** | T1 66/0/0/0 + 3/0/0/0; T2 Runtime/Dev/Index; T3 14/0/0/0 (Chromium+Firefox, beide Viewports); negative Parser-/Index-/XSS-/API-/CORS-/HTMX-/History-Fälle; XML-Zahlen und Rohbelege gesichert; keine Skips. | — |
| **AC4-06** | **erfüllt** | Frischer macOS/arm64-`testNative` **3/0/0/0** (Mach-O, SHA256 `95e2bc3d…`); frischer Linux/arm64-Mandrel-Build (ELF aarch64, SHA256 `6f77a437…`); neue JVM-/Native-Images aus diesem Worktree mit Revision `fd7164d…`, Version `0.2.LOCALBUILD`; **20 lokale + 20 Linux-Distributionsfälle**, eigene 2-Container-Cross-Layer-Probe; Readiness, Read-only, UID `1000730000:0`, tmpfs, keine Index-/DB-Volumes; **kein amd64-Erfolg behauptet**. | — |
| **AC4-07** | **erfüllt** | `versioning-contract.py` 4 gültige/5 abgewiesene Run-Nummern; `release-contract.py` 6/0/0/0 Fake-Registry inkl. falscher Tags/Digests/Plattformen; CI-Struktur-/Trigger-/Tag-/Mirror-Audit; Probe-1-Mutationen Exit 1; keine Docker-Hub-/Codeberg-Publikation; Main-Push-Wirkung in README/Operations dokumentiert. | — |
| **AC4-08** | **erfüllt (Review durchgeführt; Gesamtfreigabe erteilt)** | Dieser kumulative Review aller 31 ACs auf exakt `fd7164d…`; zwei eigene neue, tatsächlich ausgeführte Gegenproben (Probe 1 und 2); Native-/Linux-/Container- und CI-Belege; keine offenen Muss-Abweichungen. | — |

## 4. Selbst entwickelte Gegenproben

### 4.1 Probe 1 – CI-/Testharness-/Dokupfad-Falle (eigene Probe)

**Ziel/Hypothese:** Der aktive CI-/Doku-/Harness-Pfad muss den unveränderten Kandidaten grün lassen, aber eine absichtlich gebrochene Kopie in `/tmp` rot machen. Geprüft werden Struktur, Gate-Vollständigkeit, Publish-Grenzen, Doku-Links und ein echter HTTP-Golden-Vertrag.

**Eingaben/Herkunft:**

- Original: `.github/workflows/main.yml`, `.github/workflows/mirror.yml` und aktive Dokumente des Kandidaten.
- Temporäre Mutationen (nur `/tmp/.../probes/probe1/…`):
  1. `scripts/search-index-contract.py` → `scripts/nonexistent-search-contract.py`;
  2. Entfernen von `needs: jvm`;
  3. Publish-Bedingung zusätzlich auf `pull_request` erweitern;
  4. Entfernen von `--expected-platform linux/amd64`;
  5. README-Link `docs/testing.md` → `docs/quarkus-migration/OPERATIONS.md`;
  6. echte Kopie von `scripts/api-http-contract.py` + `public-api.json`, Produktfeld `products[0].title` auf `MUTIERTER-C4RR-PRODUKT-TITEL`.
- Echter JVM-Prozess aus `build/quarkus-app/quarkus-run.jar` (Default-Harvest) für den HTTP-Harness.

**Kommandos/Erwartung:** `python3 /tmp/kartenkatalog-c4-review-fd7164d/probes/probe1.py`; Original jeweils Exit 0, Mutationen jeweils Exit ≠ 0.

**Tatsächlich (Exit 0 der Gesamtprobe):**

| Fall | Exit | Beobachtung |
|---|---|---|
| CI original | 0 | keine Fehler; alle referenzierten Scripts existieren |
| CI fehlendes Script | 1 | `scripts/nonexistent-search-contract.py` fehlt, Gate fehlt |
| CI `needs: jvm` entfernt | 1 | `linux-native needs jvm missing` |
| CI PR-Publish | 1 | Publish-Condition nicht mehr main-only |
| CI Plattform entfernt | 1 | `--expected-platform linux/amd64` fehlt |
| Doku original | 0 | `files 45 local-links 59 bad 0` |
| Doku alter Pfad | 1 | `BAD ('README.md', 'docs/quarkus-migration/OPERATIONS.md', 'missing-file')` |
| API original | 0 | `All API HTTP contract assertions succeeded.` |
| API Produktfeld mutiert | 1 | `AssertionError` in `api-http-contract.py` |

**Rohbelege:** `/tmp/kartenkatalog-c4-review-fd7164d/evidence/probe1-results.json`, `logs/probe1.log`, `logs/probe1-jvm.log`, `probes/probe1.py`.

### 4.2 Probe 2 – neue alternative XTF, gefährliche Notice, JVM/Native-Parität

**Ziel/Hypothese:** Eine völlig andere lokale XTF darf keine Harvest-Provenienz vortäuschen; eine gefährliche Notice muss auf allen Vollseiten escaped bleiben; Suche, Ranking, Filter, Facets, API, HTML/HTMX und QML müssen zwischen echten JVM- und Native-Linux/arm64-Containern identisch sein.

**Neue Eingabe:** `/tmp/kartenkatalog-c4-review-fd7164d/probes/probe2/catalog.xtf`, unabhängig aus dem XTF-2.4-Modell aufgebaut (nicht Kopie der Kandidatenfixture):

- 2 Themen `probe-wasser`, `probe-bau`; 4 Ebenen `ch.c4rr.bach`, `ch.c4rr.see`, `ch.c4rr.bau`, `ch.c4rr.plan`.
- Gefährliche Notice: `<img src=x onerror=alert(777)> & <script>alert(778)</script> "Quelle" </p><svg onload=alert(779)>`.
- QML exakt 104 Bytes; Beschreibung mit `<img onerror>`/`<script>` sowie Suchtext „Sondenmarke“; Keywords/Synonyme/Organisation.

**Fälle:** JVM- und Native-Container `--read-only --user 1000730000:0 --cap-drop=ALL --security-opt=no-new-privileges --tmpfs /tmp… --mount source=<XTF-Dir>,target=/catalog,readonly`; beide Images aus diesem Worktree mit Revision `fd7164d…`; 4 Vollseiten, HTMX-Fragment, 8 API-Antworten, QML, Readiness, `docker inspect`/`docker diff`.

**Erwartung:** beide Exit 0; Notice genau einmal pro Vollseite und unescaped exakt gleich; keine ausführbaren Angreifer-Knoten; keine Harvest-Behauptung; Suche 2 Treffer mit Keyword-Rang vor Beschreibung; Filter 1; Facets korrekt; QML exakt; JVM/Native-Hashes gleich; Container read-only/UID/tmpfs/kein Volume/keine Indexdatei.

**Tatsächlich:** Gesamtprobe **Exit 0**. Beide Container `UP`; JVM und Native:

- Notice auf `/`, `/thema`, `/themen/probe-wasser/ebenen`, `/ebene/ch.c4rr.bach` je genau **1×**; `html.unescape` ergibt exakt die konfigurierte Notice; keine rohen `<img onerror>`-/`<script>`-/`<svg onload>`-Vektoren; kein Harvest-Satz.
- `/api/v1/ebenen?q=Sondenmarke` → 2 Treffer, Reihenfolge `[ch.c4rr.bau, ch.c4rr.bach]` (Keyword vor Beschreibung).
- `thema=probe-wasser&typ=datasetview` → `[ch.c4rr.bach]`.
- Facets: `thema` je 1 für `probe-bau`/`probe-wasser`; `typ` `datasetview=2`, `extwms=0`, `layergroup=0`.
- QML **104 Bytes**, exakt, `200 application/xml`, Attachment-Dateiname `ch.c4rr.bach.qml`.
- **5 Seiten + 8 API-Antworten JVM == Native** (SHA256-Vergleich), QML-Hash gleich.
- Container: `User=1000730000:0`, `ReadonlyRootfs=true`, nur read-only Bind-Mount; `docker diff` leer; Quellverzeichnis unverändert nur `catalog.xtf`; keine Volumes.
- Beide eigenen Container wurden entfernt.

**Rohbelege:** `/tmp/kartenkatalog-c4-review-fd7164d/evidence/probe2-results.json`, `logs/probe2.log`, `logs/probe2-jvm-container.log`, `logs/probe2-native-container.log`, `probes/probe2.py`, `probes/probe2/catalog.xtf`.

## 5. Pflichtgates und Rohbelege

Alle Kommandos mit explizitem cwd `/Users/stefan/sources/kartenkatalog-cleanup-C4-review-fd7164d`; Builds/Serverprozesse sequenziell.

| Gate | Kommando | Exit | Tests / Failures / Errors / Skips | Report / SHA-Bindung |
|---|---|---|---|---|
| T0 Git/Archiv/Links | `git rev-parse`, `git ls-tree`, `git diff --name-status`, Blob-/SHA256-/Linkcheck | 0 | 27/27 Archivblobs identisch; 45 MD/59 Links/0 bad; `git diff --check` 0 | `evidence/archive-identity.txt`, `linkcheck-all.txt`, `final-status.txt`; `fd7164d…` |
| T0 Root/Ignore/Names | `git ls-files`, `git check-ignore -q --no-index`, `git grep` | 0 | Root-Artefakte absent; 6 positive/4 negative Ignore-Canaries; keine aktiven Phasennamen | `evidence/t0-*.txt`; `fd7164d…` |
| T1 JVM/API/Paket | `./gradlew clean test quarkusBuild quarkusIntTest --console=plain` | **0** | **66 / 0 / 0 / 0** + **3 / 0 / 0 / 0** | `logs/t1-jvm.log`, `evidence/t1/**`; `fd7164d…` |
| T2 Runtime | `python3 scripts/runtime-process-contract.py` | **0** | 1 Gutstart; **8** erwartete Fehlstarts Exit 1 vor Ready | `logs/t2-runtime.log`, `build/reports/runtime`; `fd7164d…` |
| T2 Dev/CORS | `python3 scripts/runtime-process-contract.py --dev` | **0** | OPTIONS/GET erlaubter Origin 200/200; fremder 403/403; keine Credentials | `logs/t2-runtime-dev.log`; `fd7164d…` |
| T2 Index | `python3 scripts/search-index-contract.py` | **0** | Harvest 886; first/second/changed je 12; broken-index Exit 1; keine Indexdateien | `logs/t2-search-index.log`, `build/reports/search-index`; `fd7164d…` |
| T3 Browser | `./gradlew playwrightTest --console=plain` | **0** | **14 / 0 / 0 / 0**; Chromium+Firefox; 1440×900/390×844 | `logs/t3-browser.log`, `evidence/t3/**`; `fd7164d…` |
| T4 macOS Native | `GRAALVM_HOME=…/25.0.3-graal ./gradlew clean testNative -Dquarkus.native.enabled=true -Dquarkus.package.jar.enabled=false -Dquarkus.native.native-image-xmx=4g --console=plain` | **0** | **3 / 0 / 0 / 0**; Mach-O arm64, SHA256 `95e2bc3d…` | `logs/t4-native-macos.log`, `evidence/t4-macos/**`; `fd7164d…` |
| T4 lokale Distribution | `python3 scripts/distribution-contract.py --native-executable …/evidence/t4-macos/application --report-dir …/evidence/local-distribution` | **0** | **20** Fälle; JVM/Native-Payloads, Ranking/Facets/QML/HTML/HTMX/Readiness, Fehlstarts | `logs/t4-local-distribution.log`, `evidence/local-distribution/summary.json`; `fd7164d…` |
| T4 Linux Native-Build | `./gradlew quarkusBuild -Dquarkus.native.enabled=true -Dquarkus.package.jar.enabled=false -Dquarkus.native.container-build=true -Dquarkus.native.container-runtime=docker -Dquarkus.native.builder-image=quay.io/…@sha256:29a5… -Dquarkus.native.native-image-xmx=4g --console=plain` | **0** | ELF 64-bit ARM aarch64, SHA256 `6f77a437…` | `logs/t4-linux-native-build.log`, `evidence/t4-linux/**`; `fd7164d…` |
| T4 JVM-/Native-Images | `docker build --platform linux/arm64 --build-arg VERSION=0.2.LOCALBUILD --build-arg REVISION=fd7164d… -f src/main/docker/Dockerfile.jvm|native …` | **0** | `linux/arm64`, Revision `fd7164d…`, Version `0.2.LOCALBUILD`, `USER 1001:0`; JVM-Digest `sha256:612e9bb…`, Native-Digest `sha256:6af803a…` | `logs/t4-docker-jvm.log`, `logs/t4-docker-native.log`; `fd7164d…` |
| T4 Linux Distribution | `python3 scripts/distribution-contract.py --jvm-image kartenkatalog:c4rr-fd7164d-jvm --native-image kartenkatalog:c4rr-fd7164d-native --expected-version 0.2.LOCALBUILD --expected-revision fd7164d… --expected-platform linux/arm64 --report-dir …/evidence/linux-distribution` | **0** | **20** Fälle; UID `1000730000:0`, read-only RootFS/XTF, tmpfs, keine Index-/DB-Volumes; API/Suche/Ranking/Facets/QML/HTML/HTMX/Readiness | `logs/t4-linux-distribution.log`, `evidence/linux-distribution/summary.json`; `fd7164d…` |
| T5 Versionierung | `python3 scripts/versioning-contract.py` | **0** | 4 gültige, 5 ungültige Run-Nummern abgewiesen | `logs/t5-versioning.log`; `fd7164d…` |
| T5 Release/Fake-Registry | `python3 scripts/release-contract.py` | **0** | **6/0/0/0**; acht Tags, falsche Tags/Digests/Plattformen abgewiesen | `logs/t5-release.log`; `fd7164d…` |
| T5 CI-Struktur | eigener Probe-1-Audit + Mutationen | 0 (Original), 1 (4 Mutationen) | `needs: jvm`, Gates vor Login/Publish, Main-only, 8 Tags, Mirror; keine Publikation | `evidence/probe1-results.json`; `fd7164d…` |
| Abhängigkeiten | `./gradlew dependencies --configuration runtimeClasspath --console=plain` | **0** | Quarkus 3.40.1.1, Lucene 10.6.0, Jsoup 1.23.2; keine DB/Spring/JTE | `logs/t5-dependencies.log`; `fd7164d…` |
| Eigene Probe 1 | `python3 /tmp/kartenkatalog-c4-review-fd7164d/probes/probe1.py` | **0** | Original 0; vier CI-Mutationen 1; Doku-Mutation 1; API-Mutation 1 | `evidence/probe1-results.json`; `fd7164d…` |
| Eigene Probe 2 | `python3 /tmp/kartenkatalog-c4-review-fd7164d/probes/probe2.py` | **0** | 2 echte Container; 5 Seiten/8 APIs JVM==Native; QML 104 B; Notice escaped | `evidence/probe2-results.json`; `fd7164d…` |
| Hygiene | `git status --porcelain=v1`, `git diff --check BASE CANDIDATE` | 0 | tracked Tree leer; Diffcheck 0 | `evidence/final-status.txt`; `fd7164d…` |

**Nicht ausgeführte, fällige C4-Prüfungen:** keine. **Bewusst nicht lokal ausgeführt und nicht als Erfolg behauptet:** `linux/amd64`-Native-/Containerlauf (lokale Engine ist linux/arm64) und ein echter externer Registry-/CI-Release; der Releaseworkflow wurde nur strukturell/semantisch geprüft. Die lokale Linux-Prüfung ist ausdrücklich **linux/arm64**; macOS/arm64 ist kein amd64-Nachweis.

## 6. Findings

### F1 – erledigt

- **Ursache:** Zusätzliche EOF-Leerzeile in `docs/architecture.md` im ersten C4-Kandidaten `0b1704c…`; `git diff --check ec57d04… 0b1704c…` Exit 2.
- **Nacharbeit:** Der neue Kandidat `fd7164d…` entfernt ausschließlich diese Leerzeile; `docs/architecture.md` endet mit genau einem Newline, 0 Trailing-Whitespace-Zeilen.
- **Verifikation:** `git diff --check ec57d04… fd7164d…` = **Exit 0**. Die Datei-/Diff-Prüfung ist damit sauber; AC1-04 ist erfüllt.
- **Abgrenzung:** Der erste Bericht bleibt unverändert ausserhalb des Kandidaten; Hash `78f0e9e2…` unverändert.

### Neue Findings

**Keine** funktionalen, Test-, CI-, Fixture-, Provenienz-, XSS-, Native-, Container- oder Release-Findings.

### Blocker

**Keine** Blocker nach `INFRA`, `MISSING_EVIDENCE` oder `SCOPE_DECISION`.

**Nicht blockierende, ausdrücklich eingeordnete Beobachtungen:**

- Aktive Doku verlinkt historische P0/P5-Berichte unter `docs/history/**` als ausdrücklich historische Belege; das ist gemäß Plan C4 zulässig und keine aktive Phasenanleitung.
- Die lokale Docker-Engine und alle eigenen Linux-Läufe sind **linux/arm64**; der Releaseworkflow zielt **linux/amd64**. Es wurde kein amd64-Erfolg behauptet.
- Die eigenen Review-Images wurden nach der Belegsicherung entfernt; keine eigenen Container/Prozesse blieben zurück.

## 7. Urteil

# PASS

**Begründung:** Alle **31 Muss-Kriterien** AC0-01..05, AC1-01..05, AC2-01..07, AC3-01..06 und AC4-01..08 sind auf dem exakten Kandidaten `fd7164d413fde495465d8c295d645cb5e453b233` anhand eigener Git-/Source-/Doku-/Test-/Prozess-/Browser-/Native-/Container- und CI-Belege erfüllt. Die frühere F1-Ursache ist behoben; der kumulative Diff ist whitespace-sauber. Die historischen 27 Originale sind bytegleich archiviert, die aktiven Dokumente sind aktuell und linksauflösbar, die Phasennamen-/Root-Artefakt-Bereinigung ist wirksam, und C2/C3-Verträge bleiben erhalten. T1–T5 liefen frisch; zwei eigene neue Gegenproben (CI-/Doku-/HTTP-Harness-Mutation sowie alternative XTF mit gefährlicher Notice und JVM/Native-Parität) haben die behaupteten Schutzmechanismen tatsächlich geprüft. Es gibt keine offene Muss-Abweichung und keinen Blocker.

**Nächster zulässiger Schritt:** Der unveränderte Bericht darf in einem rein dokumentarischen Commit (z. B. `docs/cleanup/reviews/C4-review.md` und Status) archiviert werden. **Dieses PASS ist ausschließlich die fachliche Cleanup-Freigabe des Kandidaten-SHA; es ist keine Freigabe für Push nach `main`, Merge, Git-Tag, Workflow-Dispatch, Docker-Hub-/Codeberg-Publikation oder Release.** Diese Entscheidung liegt beim Nutzer.

---

**Geprüfter Kandidat:** `fd7164d413fde495465d8c295d645cb5e453b233`  
**Geprüfte Basis:** `ec57d04b8688617ddfe3a36070f23d15459fce30`  
**Review-Worktree:** `/Users/stefan/sources/kartenkatalog-cleanup-C4-review-fd7164d` (tracked clean vor/nach; HEAD unverändert)  
**Architektur:** macOS/arm64 (testNative Mach-O) und Linux/arm64 (ELF/OCI); kein linux/amd64-Erfolg behauptet.  
**Rohbelege:** `/tmp/kartenkatalog-c4-review-fd7164d/`  
**Bericht:** `/Users/stefan/sources/kartenkatalog-C4-review-fd7164d.md`
