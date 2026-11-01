# Unabhängiger Review P4

## Identität und Kandidat

- **Lieferabschnitt:** P4 – Produktionsbetrieb, Native Image, Linux-Container, reale JVM-/Native-Prozesse.
- **AC-IDs:** AC4-01, AC4-02, AC4-03, AC4-04.
- **Rolle:** Agent B, unabhängiger Prüfer. Keine Produktimplementierung, keine Folgephase, keine Änderung des Kandidaten.
- **OpenCode-Version (tatsächlich geprüft):** gebündelte CLI **`opencode v2.1.4`**
  (`/Applications/OpenCode.app/Contents/Resources/opencode-cli --version`, Exit 0).
  Der Nutzer hat die frühere GUI-Vorgabe ausdrücklich durch autonome CLI-Ausführung ersetzt. Es wird **keine 2.2.x-Version** behauptet.
- **API-Provider + exakte Modellkennung (lokale OpenCode-Sitzungs-/Modelldaten, Konfiguration und Verbindung; nicht Modellselbstauskunft):**
  - **Session-ID `ses_e6c21a51cffeH502X7Rv6TAAQY`**, Titel „P4 independent native Linux review 1af5736c“, Version **2.1.4**.
    Die `session_v2`-Zeile enthält `model = {"id":"deepseek-flash","providerID":"deepseek"}`
    (`identity/session-row.json`).
  - Alle **80 Assistant-Nachrichten** dieser Sitzung zum Zeitpunkt der Evidenzsicherung tragen
    `providerID=deepseek`, `modelID=deepseek-flash`, `variant=default`
    (`identity/session-message-model-counts.json`).
  - Lokaler OpenCode-Modelldatensatz (`~/.cache/opencode/models.json`, Provider `deepseek`): API
    **`https://api.deepseek.com`**, npm `@ai-sdk/openai-compatible`, Modell **`deepseek-flash`**,
    Anzeigename „DeepSeek V4.1 Flash“, kanonisch `deepseek/deepseek-v4.1-flash`
    (`identity/models-provider-deepseek.json`). Die globale `~/.config/opencode/opencode.jsonc` enthält
    keinen DeepSeek-Override und keinen Zen/Go/OpenRouter-Eintrag.
  - **Direkte Verbindung:** Der CLI-Serverprozess **PID 31024** hatte eine ESTABLISHED-Verbindung zu
    **`3.173.21.63:443`**; `dig +short api.deepseek.com` liefert dieselbe CloudFront-IP
    (`identity/connection-and-version.txt`, `identity/api-deepseek-dns.txt`).
  - **Offizielle DeepSeek-Preisseite** (`identity/deepseek-pricing.html`, abgerufen 31.10.2026):
    `deepseek-flash` → **`DeepSeek-V4.1-Flash-1017`**; OpenAI-Basis-URL `https://api.deepseek.com`
    (`identity/deepseek-pricing-excerpt.txt`). Das deckt sich mit den lokalen Modelldaten.
  - **Metadaten-Anomalie, kein Identitätsblocker:** Die `session_v2`-Zeile nennt als Projektverzeichnis
    `/Users/stefan/sources/kartenkatalog`; der Serverprozess 31024 hat `cwd=/Users/stefan/sources/kartenkatalog-review-P4`,
    und **alle** Prüfkommandos liefen explizit im Review-Worktree (`identity/session-process-cwd.txt`).
  - Es wurden **keine** Credential-Dateien, Auth-Dateien, Service-Tokens oder Key-Umgebungsvariablen gelesen oder ausgegeben.
  - `execution-identity.json` wurde nur als Startmetadatum zur Kenntnis genommen, **nicht** als Identitätsbeleg verwendet.
- **BASE_SHA:** `6c810d962875b2fa0f9c6810bf5fe7d236291e0a`
- **CANDIDATE_SHA; tatsächliches `git rev-parse HEAD`:** `1af5736c548181746d80688c150e6fefc5c28d5f`
- **Review-Worktree:** `/Users/stefan/sources/kartenkatalog-review-P4` (detached HEAD exakt auf dem Kandidaten)
  - `git status --porcelain=v1` vor und nach dem Review: **leer** (`logs/final-git-state.txt`).
  - Keine Commits seit dem Kandidaten (`git rev-list --count CANDIDATE..HEAD` = **0**), keine Pushes.
  - Kandidatendiff: **16 Dateien, +706/−17**; vollständig als `raw/candidate.diff` (860 Zeilen) gelesen.
  - Eigene Skripte/Fixtures/Mutationen liegen ausschließlich extern unter
    `/Users/stefan/sources/kartenkatalog-P4-review-evidence/1af5736c548181746d80688c150e6fefc5c28d5f/`.
- **Prüfumgebung:** macOS **15.8.1**/arm64, Temurin **JDK 21.0.7+6**, Gradle **9.8.2**, Quarkus **3.40.1.1**,
  Lucene **10.6.0**, Playwright **1.60.0**, Chromium + Firefox. Docker **29.8.2**, Linux **arm64**,
  4 CPUs / ca. 8 GB VM. Native-Compiler: Mandrel **25.0.4.1** im gepinnten UBI9-Builder.
  JVM-Container: Temurin **21.0.13**.
- **Letzter unabhängiger PASS-SHA aus voriger Lieferung:** `c030009c361995baeab0d83ef76fd763ec131a84` (P3b).
- **AGENTS.md:** im Repository und dessen Elternpfad nicht vorhanden.
- **Externes Evidenzverzeichnis:** `/Users/stefan/sources/kartenkatalog-P4-review-evidence/1af5736c548181746d80688c150e6fefc5c28d5f`

## Fachliche Kriterien

| AC-ID | Erfüllt / verletzt / nicht prüfbar | Eigener Code-/HTTP-/Test-/Browser-/Native-Beleg | Finding |
|---|---|---|---|
| **AC4-01** | **Erfüllt** | `build.gradle`: Java-Toolchain **21**, Quarkus **3.40.1.1**, Lucene **10.6.0**. Eigenes JVM-Artefakt gebaut und als `kartenkatalog:p4-review-1af5736-jvm` gesichert; `javap` des JVM-App-Classes: **major version 65** (`logs/class-check/major-version.txt`). Eigener Linux-Native-Build mit **Mandrel 25.0.4.1-Final**, GCC 11.5/aarch64 und den verlangten Flags `quarkus.native.enabled=true` **und** `quarkus.package.jar.enabled=false`; `file` des Artefakts: **ELF 64-bit LSB executable, ARM aarch64, GNU/Linux**, SHA-256 `ba18085b…` (`logs/linux-native-build.log`, `logs/native-artifact-metadata.txt`). Der native Harvest-Start loggt **`Lucene 10.6.0 ByteBuffersDirectory GermanAnalyzer; index 88 ms`** und **`native (powered by Quarkus 3.40.1.1)`** (`distribution/native-harvest.log`). Quellcode verwendet ausschließlich `ByteBuffersDirectory`, `GermanAnalyzer`, `DirectoryReader`, `IndexWriter`; **kein** `FSDirectory`/`MMapDirectory`; Reader/Analyzer/Directory werden auf Erfolgs- und Fehlerpfaden geschlossen (`CatalogSearchIndexBuilder`, `LuceneCatalogSearchIndex`). Die Native-Konfiguration registriert **gezielt** nur `KeywordAttributeImpl` (`methods=false, fields=false`), keine pauschale Reflection/Runtime-Initialisierung; `quarkus.native.enable-https-url-handler=true` und `quarkus.locales=de-CH,en-US` sind gesetzt. | Kein Finding. |
| **AC4-02** | **Erfüllt** | Eigener **18-Prozess-Lauf** mit getrennten Linux/arm64-Containern: 9 JVM + 9 native, je Harvest, Suchfixture, unveränderter Neustart, QML, geänderte XTF sowie vier Startfehler. `scripts/p4-distribution-contract.py` **Exit 0**; vollständige Payloads (Listen/Details/Catalog/Suche/Facets), Ranking, HTML, HTMX und Fehlercodes stimmen zwischen JVM und Native überein (`distribution/summary.json`, `distribution/*.log`). Der QML-Vertrag (XML, `<qgis`, 404 bei fehlender QML) wird je Laufzeit geprüft; die eigene XTF-Gegenprobe vergleicht zusätzlich den **QML-Body** sowie HTML/JSON exakt (nur JSON-Objektmitgliedsreihenfolge von `problem+json` wird RFC-konform normalisiert; siehe OBS-01). Harvest-Payload-Hash in beiden Laufzeiten **`94c32af95ac9afe60545814a0c164d1eedc97bcb02682ee91ac1737d01e7df36`**; Such-Fixture **`b4d258ed…`**. Kein persistierter Index: alle `*-tmp-files.json` leer, eigener `docker diff` leer (`logs/native-docker-diff.txt`), `/tmp`-Inventar leer (`logs/native-tmp-inventory.txt`). Start mit malformed XTF, XXE, defekter Referenz und Indexfehler endet in beiden Laufzeiten vor Ready mit Exit 1 (`failureExit=1`; z. B. `distribution/native-reference.log`). Native-Log weist echte Laufzeit aus, kein JVM-Fallback. | Kein Finding. OBS-01: `problem+json`-Objektmitgliedsreihenfolge kann selbst zwischen JVM-Prozessen variieren; semantisch identisch, JSON-Objektordnung ist kein Vertrag. |
| **AC4-03** | **Erfüllt** | `/q/health/live` und `/q/health/ready` liefern **200/UP**; Startlogs nennen **169 Themen / 886 Ebenen**, Quelle, Lucene-Version, `ByteBuffersDirectory`/`GermanAnalyzer`, Index-/Katalogdauer und Heap (`distribution/*-harvest.log`). Linux-JVM- und Native-Images laufen als **UID 1000730000:0**, mit **read-only RootFS**, read-only XTF-Bindmount, `--cap-drop=ALL`, `no-new-privileges`, 512 MiB Memory und ausschließlich **64-MiB-RAM-`/tmp`**; keine Indexvolume, keine DB-/Secret-Konfiguration (`probe/run/result.json`, `logs/*identity*`). JVM-Image enthält Temurin 21.0.13, Native-Image **kein Java** (`logs/jvm-image-runtime-identity.txt`, `logs/native-image-runtime-identity.txt`). Runbook/OPERATIONS, Dockerfiles, OpenShift-Vorlage und CI geprüft; `actionlint` 1.7.7 **Exit 0**. **Kein Clusterdeployment und kein amd64-Test behauptet**; CI nur gelintet. | Kein Finding. |
| **AC4-04** | **Erfüllt** | Frische T1–T4 am Kandidaten: **62 Unit/HTTP**, **3 JVM-Paket-Integrationstests**, **14 echte Chromium-/Firefox-Browsertests**, **18 echte Linux-Containerprozesse**; jeweils **0 Failures/0 Errors/0 Skips**. Eigene neue Native-/Startgegenprobe: **186/186 Checks, Exit 0**, mit neuer externer XTF, echter JVM-/Native-Parität, Ranking/Cross-Field-AND/Umlauten/Mehrfachfiltern/Facets, sicheren HTTPS-/XSS-Metadaten, QML/404, Native-Neustart und zwei nativen Negativstarts (Referenz/XXE, jeweils Exit 1). Runtime-Graph enthält nur Lucene 10.6.0 core/analysis-common; keine Spring/JTE/JDBC/DB/Queryparser/Playwright-Runtime. Sicherheits-/Dependency-Prüfung und eigener containerisierter Native-Lauf durchgeführt. | Kein Finding. |

## Eigene neue Gegenprobe

### Warum gerade dieser Gegenfall die Phase kritisch testet

Die P4-Risiken liegen nicht in einem bloß gestarteten Binary, sondern in (a) nativen Abweichungen bei Jsoup-/HTTPS-Metadaten, (b) Ranking und AND über getrennte Felder, (c) dynamischen Facets und OR/AND-Filtern, (d) Null-/Listenvertrag, (e) QML/404 und sicherer Ausgabe, (f) echtem Neustart-Heapindex und (g) hartem Startfehler bei defekter XTF. Deshalb wurde eine **neue eigene** XTF mit 3 Themen und 5 Ebenen gebaut – keine Umbenennung eines Codex-Tests und keine Codex-Fixture.

### Eigene Eingabe (reproduzierbar)

- `probe/own-probe.xml` (SHA-256 `2e7d0aaa…`):
  - **Natur & Landschaft (`natur`)**: „Alpenblick Gipfel“ (datasetview, Org „Amt für Natur“, Keywords „Gletscher, Aussicht“, Synonym „Berggipfel“, Beschreibung „Hochtal im Süden“, Transparenz 85, echtes QML, `service_download=false`), „Alpenblick Route“ (extwms, Org Natur, Beschreibung mit echtem **HTTPS-Link**, `javascript:`-Link, `<script>` und `onerror`).
  - **Wasser (`wasser`)**: „Blausee“ (layergroup, Org „Amt für Wasser“, keine QML).
  - **Planung (`planung`)**: „Waldstück Nord“ (datasetview, Keywords „Bäume“, Org „Amt für Wald“), „Leere Karte“ (facadelayer, **keine** Organisation/Beschreibung/Listenwerte).
- `probe/own_p4_probe.py` (SHA-256 `ea34e7fe…`) startet beide Images selbst, mit beliebiger UID, read-only RootFS/XTF und 64-MiB-`/tmp`, und beendet sie wieder.

### Hypothese / Erwartung

JVM und Native liefern identische API-/HTML-/HTMX-/QML-Payloads; `q=Alpenblick` ergibt die beiden Titel mit deterministischem A-Z-Tie-Break; `q=Gletscher Hochtal` findet über Keyword **und** Beschreibung; `q=Bäume` und `q=Waldstück` behandeln Umlaute; Mehrfachfilter sind OR innerhalb/AND zwischen Gruppen; Facetcounts berücksichtigen die jeweils anderen Filter; fehlende Werte bleiben `null`/`[]`; HTTPS bleibt erhalten, `javascript:`/`script`/`onerror` verschwinden; echte QML liefert XML, fehlende QML 404; ein Referenz- oder XXE-Defekt verhindert den nativen Start vor Ready.

### Exakte Schritte/Befehle und Exit

```sh
cd /Users/stefan/sources/kartenkatalog-review-P4
EV=/Users/stefan/sources/kartenkatalog-P4-review-evidence/1af5736c548181746d80688c150e6fefc5c28d5f
xmllint --noout "$EV/probe/own-probe.xml"                         # Exit 0
python3 "$EV/probe/own_p4_probe.py" \
  --jvm-image kartenkatalog:p4-review-1af5736-jvm \
  --native-image kartenkatalog:p4-review-1af5736-native \
  --fixture "$EV/probe/own-probe.xml" \
  --out "$EV/probe/run"                                          # Exit 0
```

### Tatsächliche Ausgabe / Rohreport

```text
READY jvm seconds=1.465
READY native seconds=0.475
READY native-restart seconds=0.475
...
PASS native: relevance tie follows title A-Z actual=['probe.gipfel', 'probe.route']
PASS native: AND across keyword and description fields actual=['probe.gipfel']
PASS native: umlaut keyword Bäume actual=['probe.wald']
PASS native: umlaut title Waldstück actual=['probe.wald']
PASS native: OR within and AND between filter groups actual=['probe.gipfel', 'probe.see']
PASS native: dynamic theme facet counts actual={'natur': 1, 'planung': 1, 'wasser': 1}
PASS native: dynamic type facet counts actual={'datasetview': 1, 'extwms': 1, 'facadelayer': 0, 'layergroup': 1}
PASS native: dynamic organization facet counts actual={'Amt für Natur': 1, 'Amt für Wald': 0, 'Amt für Wasser': 1}
PASS native: HTTPS description link survives in detail HTML
PASS native: real QML returned as XML
PASS native: missing QML is 404
PASS native-bad-reference: non-zero startup exit actual=1
PASS native-bad-reference: expected startup error actual='Cannot load catalog XTF'
PASS native-bad-xxe: non-zero startup exit actual=1
PASS native-bad-xxe: expected startup error actual='Cannot load catalog XTF'
OWN_PROBE PASS checks=186
```

- Vollständiger Rohlog: `logs/own-probe.log`
- Maschinenlesbares Ergebnis: `probe/run/result.json` (`checks=186`, `Platform=linux`, `ConfigUser=1000730000:0`, `ReadonlyRootfs=true`, `Tmpfs=/tmp:...size=64m`)
- Native-/JVM-Payloads: `probe/run/jvm-payload.json`, `probe/run/native-payload.json`
- Native-Neustart: `probe/run/native-restart-payload.json`; beide Payloads identisch.
- Native Negativlogs: `probe/run/logs/native-bad-reference.log`, `probe/run/logs/native-bad-xxe.log` (jeweils `Unresolved theme reference` bzw. DOCTYPE-Ablehnung; Exit 1, kein erfolgreicher Startlog).

### Schlussfolgerung

Die eigene Gegenprobe bestätigt den vollständigen P4-Kern unabhängig: echte JVM-/Native-Parität für Suche/Ranking/Filter/Facets/HTML/HTMX/QML, frische Heapindexierung bei beiden Native-Starts, sichere Metadaten inklusive HTTPS-Erhalt, korrekte Null-/Listenwerte, QML/404 und harte native Startfehler bei defekter Referenz/XXE. Es entstand **kein Produktfinding**.

**OBS-01 (kein Finding):** `problem+json` wird aus `Map.of` erzeugt; die JSON-Objektmitgliedsreihenfolge ist nicht garantiert und variierte zwischen Prozessen. Die geparsten Fehlerobjekte sind identisch. RFC 8259 macht die Mitgliedsreihenfolge nicht vertraglich; die eigene Paritätsprüfung normalisiert nur diese Reihenfolge.

## Pflichtprüfungen

Alle Kommandos im Review-Worktree `/Users/stefan/sources/kartenkatalog-review-P4`.
`EV` = `/Users/stefan/sources/kartenkatalog-P4-review-evidence/1af5736c548181746d80688c150e6fefc5c28d5f`.

| Testfamilie | Ausgeführter Befehl | Exit | Tests/Failures/Errors/Skips | Rohreport | SHA-bezogen |
|---|---|---:|---|---|---|
| **T1/T2 + T3 Pflichtlauf** | `./gradlew clean test quarkusBuild quarkusIntTest playwrightTest --console=plain` | **0** | **62/0/0/0** Unit/HTTP; **3/0/0/0** JVM-Paket; **14/0/0/0** Browser (7 Chromium + 7 Firefox) | `logs/gradle-jvm.log`, `build-reports/jvm/TEST-*.xml`, `build-reports/playwright/TEST-*.xml`, `build-reports/html-*` | Ja |
| T3 Browser/Viewports | im Pflichtlauf enthalten (`installPlaywrightBrowsers`, `playwrightTest`) | **0** | 14 echte Tests, beide Engines; Screenshots **1440×900** und **390×844** | `build-reports/p3a/screenshots/`, `build-reports/p3b/screenshots/` (24 PNG; Dimensionen mit `sips` geprüft) | Ja |
| **T4 Linux JVM/Native** | `python3 scripts/p4-distribution-contract.py --jvm-image kartenkatalog:p4-review-1af5736-jvm --native-image kartenkatalog:p4-review-1af5736-native --report-dir "$EV/distribution"` | **0** | **18** echte Prozesse (9 JVM + 9 Native), alle Paritäts-/Negativfälle | `logs/distribution-contract.log`, `distribution/summary.json`, `distribution/*.log` | Ja |
| Fehlgrün-Canary | gleicher Befehl, aber `--native-image kartenkatalog:p4-review-1af5736-jvm` | **1** (erwartet) | Native-Identitätsprüfung lehnt echtes JVM-Startlog ab | `logs/runtime-canary-wrong-jvm.log`, `distribution-canary/native-harvest.log` | Ja |
| Leeres Native-Image | `--native-image ''` | **2** (erwartet) | CLI verweigert leeres Image, kein Fallback | `logs/runtime-canary-empty-image.log` | Ja |
| Eigene Native-/Startprobe | `python3 "$EV/probe/own_p4_probe.py" …` | **0** | **186/186 Checks**, 3 Starts + 2 negative Starts | `logs/own-probe.log`, `probe/run/` | Ja |
| Runtime-Dependency-Graph | `./gradlew dependencies --configuration runtimeClasspath --console=plain` | **0** | nur `lucene-core:10.6.0`, `lucene-analysis-common:10.6.0`; kein Queryparser/Spring/JTE/JDBC/DB/Playwright | `logs/dependencies-runtimeClasspath.log` | Ja |
| P1 JVM-/Startfehler | `python3 scripts/p1-process-contract.py` | **0** | JVM ready 2.376 s; 8 defekte Quellen (malformed, wrong-model, dangling-reference, duplicate-layer, XXE, missing-file, missing-classpath, http-source) je Exit 1, nie ready | `logs/p1-process.log`, `build-reports/p1/` | Ja |
| P2 RAM-Index/Neustart/read-only | `python3 scripts/p2-process-contract.py` | **0** | 886 Dokumente; 12-Dokument-Fixture; drei Starts; XTF-Änderung; Index-Startfehler Exit 1; keine persistierten Indexdateien | `logs/p2-process.log`, `build-reports/p2/` | Ja |
| Native-Build | `./gradlew quarkusBuild -Dquarkus.native.enabled=true -Dquarkus.package.jar.enabled=false -Dquarkus.native.container-build=true -Dquarkus.native.container-runtime=docker -Dquarkus.native.builder-image=quay.io/quarkus/ubi9-quarkus-mandrel-builder-image@sha256:29a5078c94489623ef7e554aad2cbff70a4ff872ac6d60a46f5538697ed82a30 -Dquarkus.native.native-image-xmx=4g --console=plain` | **0** | Mandrel **25.0.4.1-Final**, GCC 11.5/aarch64; Native Image 74 MB, Peak RSS 4.31 GB | `logs/linux-native-build.log`, `logs/native-artifact-metadata.txt` | Ja |
| Container-Images | `docker build -f src/main/docker/Dockerfile.jvm -t kartenkatalog:p4-review-1af5736-jvm .`; danach `docker build -f src/main/docker/Dockerfile.native -t kartenkatalog:p4-review-1af5736-native .` | **0 / 0** | Linux/arm64; JVM `sha256:995ed9d2…`, Native `sha256:17fe8134…` | `logs/jvm-image-build.log`, `logs/native-image-build.log`, `logs/*image-inspect.txt` | Ja |
| Runtime-Identität | `docker run --entrypoint java …-jvm -version`; `docker run --entrypoint /bin/sh …-native -c 'command -v java; ldd /work/application; uname -m'` | **0** | JVM Temurin **21.0.13**, aarch64; Native **kein Java**, glibc 2.34, aarch64 | `logs/jvm-image-runtime-identity.txt`, `logs/native-image-runtime-identity.txt` | Ja |
| Scratch/RootFS | eigener nativer Container; `docker diff`; `docker cp /tmp/.` | **0** | `docker diff` **leer**; `/tmp`-Inventar **leer**; keine Lucene-Segment-/Lockdateien | `logs/native-docker-diff.txt`, `logs/native-tmp-inventory.txt` | Ja |
| Fixture-Integrität | `git show BASE:… \| shasum -a 256` vs. Kandidat | **0** | `baseline.json` **`cd1167f8…`**, Harvest **`de40f025…`** byte-identisch | `logs/fixture-hashes-base-candidate.txt` | Ja |
| CI-Syntax | Docker `rhysd/actionlint:1.7.7 .github/workflows/main.yml` | **0** | keine Ausgabe | `logs/actionlint.log` | Ja |
| Git-/Kandidatenhygiene | `git rev-parse HEAD`, `git status --porcelain=v1`, `git rev-list --count`, `git diff --check` | **0** | HEAD = Kandidat; Status leer; 0 Commits seit Kandidat; Diff-Check sauber | `logs/final-git-state.txt`, `raw/git-status-before.txt`, `raw/candidate.diff` | Ja |

- **Fällige, aber nicht ausgeführte Checks:** Ein lokales macOS-`testNative` wurde **nicht zusätzlich** ausgeführt und **nicht als bestanden behauptet**; es wäre mit `GRAALVM_HOME` möglich, ist aber nicht das verlangte Linux-Native-Gate. Eine native Browsermatrix war optional und wurde nicht ausgeführt. Ein Linux-ELF kann auf macOS nicht direkt über `testNative` laufen; die verlangten T4-Nachweise wurden deshalb mit echten getrennten **Linux/arm64-Containern** erbracht. Die JVM-Browsermatrix (beide Engines, beide Viewports) wurde vollständig ausgeführt. Kein Testtask wurde als `NO-SOURCE`/Skip als grün ausgegeben; `NO-SOURCE` betraf nur generierte Quell-/Ressourcen-Tasks, nicht Testausführung.
- **Kumulatives Phasengate:** Für P4 nicht gefordert; P3a/P3b-UI-Verhalten wurde durch die frischen 14 Browsertests und den JVM-/Native-HTTP-Vertrag nicht regressiv verändert.
- **P5:** nicht begonnen, nicht ausgeführt, nicht freigegeben.

## Findings

**FINDING-01: Keine offene Muss-Abweichung.** AC4-01 bis AC4-04 sind nach unabhängiger Quell-, Build-, ELF-, Container-, HTTP-, Paritäts-, Negativ- und Sicherheitsprüfung erfüllt. Alle P4-Pflichtläufe und die eigene Gegenprobe sind erfolgreich.

**Beobachtungen ohne Abnahmewirkung:**

- **OBS-01:** JSON-Objektmitgliedsreihenfolge in `application/problem+json` ist wegen `Map.of` nicht stabil; geparste Payloads sind identisch. Kein Vertragsverstoß (RFC 8259).
- **OBS-02:** Der verteilte Contract-Runner prüft QML-MIME/Inhalt je Laufzeit, vergleicht den QML-Body aber nicht explizit zwischen JVM und Native. Die eigene Gegenprobe schließt diese Lücke mit exaktem QML-Body-Vergleich.
- **OBS-03:** Native Browsermatrix und macOS-`testNative` waren optional und wurden nicht ausgeführt; die verlangten Linux-Native-/HTTP-/Paritätstests liefen tatsächlich.

## Unabhängiges Urteil

**PASS**

- **Begründung:** Der Kandidat `1af5736c548181746d80688c150e6fefc5c28d5f` erfüllt **AC4-01, AC4-02, AC4-03 und AC4-04**. Es gibt einen eigenen frischen **Linux/arm64-Native-Build** mit Mandrel 25.0.4.1 und Java-21-App-Bytecode, ein eigenes JVM-Image und echte getrennte Containerprozesse. Beide Laufzeiten bauen beim Start einen Lucene-10.6.0-`ByteBuffersDirectory`-Heapindex aus der XTF auf, liefern identische Such-/Ranking-/Facet-/HTML-/HTMX-/QML-Verträge und scheitern bei defekter XTF/Index vor Ready. Read-only RootFS/XTF, beliebige UID, 64-MiB-RAM-`/tmp`, kein Indexvolume, kein persistierter Index, kein DB-/Secret-/Mock-Fallback wurden geprüft. Die frische T1–T4-Matrix ist **62/0/0/0 + 3/0/0/0 + 14/0/0/0 + 18 echte Containerprozesse**; die eigene neue Gegenprobe ist **186/186, Exit 0**. Es gibt kein FAIL und kein BLOCKED.
- **P4-Ziel:** bestätigt.
- **Nächster Schritt:** **Vor P5 stoppen.** P5 ist mit diesem Review **nicht** begonnen und **nicht** freigegeben.
- **Geprüfter SHA:** `1af5736c548181746d80688c150e6fefc5c28d5f`
- **Bericht (absolut):** `/Users/stefan/sources/kartenkatalog-P4-review-evidence/1af5736c548181746d80688c150e6fefc5c28d5f/review.md`
- **Rohbelege (absolut):** `/Users/stefan/sources/kartenkatalog-P4-review-evidence/1af5736c548181746d80688c150e6fefc5c28d5f/`
  - Diff/HEAD/Status: `raw/candidate.diff`, `raw/git-head-before.txt`, `logs/final-git-state.txt`
  - Tests: `logs/gradle-jvm.log`, `build-reports/jvm/`, `build-reports/playwright/`, `build-reports/html-test/`, `build-reports/html-playwright/`
  - Linux-Native: `logs/linux-native-build.log`, `logs/native-artifact-metadata.txt`, `logs/native-image-build.log`, `logs/jvm-image-build.log`
  - T4: `logs/distribution-contract.log`, `distribution/summary.json`, `distribution/`
  - Canaries: `logs/runtime-canary-wrong-jvm.log`, `logs/runtime-canary-empty-image.log`
  - Eigene Gegenprobe: `probe/own-probe.xml`, `probe/own_p4_probe.py`, `logs/own-probe.log`, `probe/run/`
  - Identität: `identity/session-row.json`, `identity/session-message-model-counts.json`, `identity/models-provider-deepseek.json`, `identity/connection-and-version.txt`, `identity/deepseek-pricing-excerpt.txt`
