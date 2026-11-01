# Unabhängiger Pre-Publication-Review: Version 0.2 und Docker-Hub-Release

**Repository:** `edigonzales/kartenkatalog`  
**BASE_SHA:** `8be37f0a4ceed9d8f6db357f177d81dd0cd7f61c`  
**CANDIDATE_SHA / geprüfter HEAD:** `fbf8e93d1f55cc5a4c4a0c7cbff3bf1ac9335e77`  
**Review-Worktree:** `/Users/stefan/sources/kartenkatalog-review-release`  
**Evidence:** `/Users/stefan/sources/kartenkatalog-release-review-evidence/fbf8e93d1f55cc5a4c4a0c7cbff3bf1ac9335e77/`  
**Datum:** 1. November 2026

## 0. Urteil

**FAIL – ausschliesslich R7 (Dokumentation). R1–R6 sind PASS; alle funktionalen Gates sind frisch grün.**

Es gibt **keinen** reproduzierten Code-, Test-, Build-, Container-, Native-, Versions-, Tag-, Trigger- oder Registry-Verifier-Fehler. Die einzige offene Muss-Abweichung sind zwei veraltete, jetzt sachlich falsche Sätze in `README.md` und `docs/quarkus-migration/OPERATIONS.md`, die weiterhin behaupten, es gebe keinen automatischen Push/Release. Das widerspricht dem neuen Release-Abschnitt und dem tatsächlichen Workflow. Details in Abschnitt 10. Die FAIL-Einstufung ist bewusst strikt an R7 gebunden und **kein** funktionaler Code-Blocker; die beiden Sätze müssen aber vor einer Veröffentlichung korrigiert werden.

Dieser Review hat **nichts veröffentlicht**: kein GitHub-Push, kein Docker-Hub-Login, kein `docker push`, kein Pull öffentlicher `sogis/kartenkatalog`-Tags. Die reale GitHub-amd64-CI, die Docker-Hub-Schreibvorgänge und der öffentliche 8-Tag-Digest-/Pull-Nachweis sind gemäss Auftrag **nach** diesem Review durch Codex auszuführen und werden hier nicht als bereits erfolgt behauptet. Diese bewusste Staging-Reihenfolge ist **nicht** der FAIL-Grund und auch kein BLOCKED.

## 1. Identität, Provider und direkte Verbindung

- **CLI:** `/Applications/OpenCode.app/Contents/Resources/opencode-cli`, `opencode v2.1.4`.
- **Prozessaufruf:** `opencode run --standalone --auto --model deepseek/deepseek-flash ...` (PID 49545).
- **Neue Sitzung:** `OPENCODE_SESSION_ID=ses_e69d9f482ffeFSP2l2NbXukhdw`; derselbe Wert steht in `opencode-run.jsonl`. Kein alter PASS wurde wiederverwendet.
- **Provider-/Modellmetadaten:** `models.dev` liefert Provider `deepseek`, API `https://api.deepseek.com`, Modell-ID `deepseek-flash` mit Anzeigename **„DeepSeek V4.1 Flash 1017“**. Das entspricht der geforderten Zuordnung `deepseek-flash = DeepSeek-V4.1-Flash-1017`.
- **Direkter Endpoint per TCP:** `dig +short api.deepseek.com` → `3.173.21.63` (CloudFront-CNAME). Der CLI-Kindprozess PID 49546 hatte während der Sitzung eine establierte TLS-Verbindung `172.20.10.5:<port> -> 3.173.21.63:443`. Damit ist der tatsächliche Verkehr zum aufgelösten DeepSeek-Endpoint belegt, nicht nur eine Modellselbstauskunft.
- **Kein OpenCode/Zen/OpenRouter-Hosted-Modell:** Der Modellselektor ist `deepseek/deepseek-flash`; die Verbindung zeigt auf den aufgelösten DeepSeek-Endpoint. Die vorhandenen Auth-Provider-Einträge (`deepseek`, `infomaniak`, `openrouter`) wurden nur als Schlüsselnamen geprüft; es wurden **keine** Credential-Werte gelesen oder ausgegeben.
- **Rohbelege:** `logs/identity.txt`, `probe/tcp-evidence.txt`, `opencode-run.jsonl`, `probe/tcp-live.log`.

## 2. Repository-Zustand und Unveränderlichkeit

- `git rev-parse HEAD` war vor und nach allen Prüfungen exakt `fbf8e93d1f55cc5a4c4a0c7cbff3bf1ac9335e77`.
- `git status --porcelain=v1` war vor Beginn und am Ende **leer**. Build-Artefakte liegen ausschliesslich unter dem ignorierten `build/`; der Kandidat wurde nicht verändert.
- `git diff --stat BASE..CANDIDATE` umfasst 12 Dateien (Workflow, README, OPERATIONS, STATUS, RELEASE, Versionierung, vier Skripte, zwei Dockerfiles). **Keine** Änderung an `src/main/java`, `src/test`, `data`, `build.gradle` oder `settings.gradle`.
- Der Harvest ist unverändert: SHA-256 `de40f025368ae6fc70f5858ab42592488bca837c6942cb6ff8759e6c8803de86`.
- `git diff --check` und `python3 -m py_compile scripts/*.py` waren erfolgreich.
- Es gibt kein `AGENTS.md` im Repository/Elternpfad. Gelesen wurden `implementation/RELEASE.md`, `README.md`, `OPERATIONS.md`, `STATUS.md`, `OPENCODE_REVIEW_PROMPT.md`, `EXECUTION_PLAN.md`, die Dockerfiles und die Release-/Prozessskripte.
- **Wichtig:** `GITHUB_RUN_NUMBER=731` und `0.2.731` sind ausschliesslich **meine Testwerte**. Sie reservieren keine echte GitHub-Laufnummer und werden nicht als reale Release-Version behauptet.

## 3. R1–R7 im Überblick

| Ziel | Ergebnis | Wesentlicher Beleg |
|---|---|---|
| **R1** Versionierung | **PASS** | `versioning.gradle`; 9/9 Vertragsfälle; 16/16 eigene Zusatzfälle; Manifest `Implementation-Version: 0.2.731`; Startup-Log `kartenkatalog 0.2.731`; Tags im Dry-Run `0.2.731` |
| **R2** Images/OpenShift/OCI/Laufzeit | **PASS** | Beide Images `USER 1001:0`; p4 20/20 inkl. UID `1000730000:0`, read-only RootFS/XTF, nur `/tmp`-tmpfs, kein persistierter Index; eigene Native-Probe mit UID `1000730042:0` und Default-UID; Java 21.0.13, Class-Major 65, Quarkus 3.40.1.1, Lucene 10.6.0/`ByteBuffersDirectory` |
| **R3** CI-Gates/Reihenfolge/amd64 | **PASS** (lokal; amd64-CI extern ausstehend) | `linux-native` `needs: jvm`; JVM-/Browser-/API-/CORS-/P2-Gates im `jvm`-Job; JVM-Image vor Native-Packaging; `testNative`/Native-Build; p4 vor Login/Push; `--platform linux/amd64`; gepinnter Mandrel-Digest unverändert; p4 nach Pull erneut |
| **R4** Trigger/Secrets/Concurrency | **PASS** | Bedingung `github.ref == 'refs/heads/main' && (push || workflow_dispatch)` für Login/Publish/Verify; PR-/Branch-/Schedule-Matrix alle negativ; `DOCKER_USERNAME`/`DOCKER_PASSWORD` nur im `docker/login-action@v4`-Schritt; branchbezogene Concurrency; keine Deploy-Kommandos; actionlint 1.7.12 Exit 0 |
| **R5** exakt 8 Tags | **PASS** | Eigener Fake-Docker-Dry-Run des echten Publish-Blocks: 4 Native- + 4 JVM-Pushes, exakt die geforderten 8 Tags, keine weiteren Docker-Kommandos |
| **R6** Registry-Verifier | **PASS** | Eigener Real-HTTP-Harness mit lokalem Registry-Server: 8/8 Tags, Digestgleichheit je Variante, Varianten verschieden, fehlender Tag 404, gesplitteter Alias, ARM-only/ARM+amd64/leerer Index abgewiesen, amd64+`unknown/unknown`-Provenienz akzeptiert; CI-Pull-/P4-Nachlauf statisch verifiziert |
| **R7** Doku/Regression/Reportertreue | **FAIL (Doku)** | Regression 62/3/14 grün, API/UI/Security-Quellen unverändert, RELEASE.md korrekt zu arm64 vs. geplantem amd64 und ohne Publikationsbehauptung; **aber** README:101 und OPERATIONS:10 behaupten weiterhin „kein automatischer Push/Release“ bzw. „CI … ohne Push/Release“ |

## 4. Code- und Verhaltensanalyse

### R1 – Versionierung

`gradle/versioning.gradle:2-6` liest ausschliesslich `providers.environmentVariable('GITHUB_RUN_NUMBER')`. Ein gesetzter Wert muss `[1-9][0-9]*` entsprechen, sonst wirft der Build `GITHUB_RUN_NUMBER must be a positive integer`. `BUILD_NUMBER` und `TRAVIS_BUILD_NUMBER` werden nicht mehr gelesen. Lokal ohne Variable gilt `0.2.LOCALBUILD`; mit GitHub-Laufnummer `0.2.<N>`.

Beobachtet:

- `python3 scripts/versioning-contract.py`: **Exit 0**, 4 echte Gradle-Versionen (`local` = `0.2.LOCALBUILD`, `github` = `0.2.73`, `legacy-only` = `0.2.LOCALBUILD`, `github-wins` = `0.2.74`) und 5 erwartete Ablehnungen (`''`, `abc`, `0`, `-1`, `73/suffix`).
- Eigener Zusatzlauf `probe/version-extra.py`: **16/16 PASS**, u. a. `731` + gesetzte Legacy-Variablen → `0.2.731`, Legacy-only → `0.2.LOCALBUILD`, und Ablehnung von ` 731`, `731 `, `0731`, `+731`, `1e3`, `731.0`, `731-1`, `/731`, `0`, `-731`, `abc`, `''`.
- Der mit `GITHUB_RUN_NUMBER=731` gebaute JVM-Jar trägt `Implementation-Version: 0.2.731`; der echte Startup-Log lautet `kartenkatalog 0.2.731 on JVM (powered by Quarkus 3.40.1.1)`.
- Der Publisher-Dry-Run mit `APP_VERSION=0.2.731` erzeugt exakt die Tags `0.2.731`, `latest`, `0`, `0.2` (Native) und `0.2.731-jvm`, `latest-jvm`, `0-jvm`, `0.2-jvm` (JVM). Damit bestimmt dieselbe CI-Nummer sowohl das gepackte App-Artefakt als auch die Tags; ein separater Workflow-Zähler existiert nicht.

### R2 – Images, OpenShift, OCI, Laufzeit

`src/main/docker/Dockerfile.jvm` und `Dockerfile.native` setzen beide `USER 1001:0`, `WORKDIR /catalog`/`/work` und OCI-Labels aus `VERSION`/`REVISION`; die Quell-URL ist fest `https://github.com/edigonzales/kartenkatalog`. Die Basisimages sind per Digest gepinnt. Das JVM-Image basiert auf Temurin 21.0.13 (Java 21); die Anwendungsklassen haben Major-Version 65 (Java 21). Das Native-Image nutzt den gepinnten Quarkus-UBI9-Micro-Runtime-Container; der Native-Compiler ist Mandrel 25.0.4.1 (absichtlich, siehe P4-Bericht).

Frische echte Containerprüfung mit `scripts/p4-distribution-contract.py`:

- `--jvm-image kartenkatalog:release-review-fbf8e93-jvm --native-image kartenkatalog:release-review-fbf8e93-native --expected-version 0.2.731 --expected-revision fbf8e93d1f55cc5a4c4a0c7cbff3bf1ac9335e77 --expected-platform linux/arm64`
- **Exit 0; 20/20 Fälle** (je 10 JVM und Native): Harvest, Suchfixture, unveränderter Neustart, QML, Cross-Layer, geänderte XTF, malformed XML, XXE, defekte Referenz, echter Indexaufbaufehler.
- JVM und Native erzeugten identische Payload-Hashes (z. B. Harvest `80a7d59f710dc9794fb3da72275c1653f19c0b229090165cc35abb23094642d9`, Search `1bb33a87…`, Cross `f2a1d6f3…`), inklusive Ranking/Facets/HTML/HTMX/QML.
- Beide Images liefen als UID `1000730000:0` mit `--read-only`, `--cap-drop=ALL`, `--security-opt=no-new-privileges`, 512 MiB, read-only XTF-Bindmount und ausschliesslich `/tmp` als tmpfs. `docker diff` war leer; in `/tmp` wurden keine Lucene-Segment-/Lockdateien gefunden.
- Die Startup-Logs enthielten `kartenkatalog 0.2.731`, `Quarkus 3.40.1.1` und `Lucene 10.6.0 ByteBuffersDirectory GermanAnalyzer`; die Native-Logs zeigten die Native-Runtime, keinen JVM-Fallback.
- Zusätzliche eigene Native-Probe (`probe/native-runtime-probe.py`) mit **UID `1000730042:0`** und separat mit **Default-UID** (ohne `--user`): beide **Exit 0**, 886 Ebenen/169 Themen, 135 Suchtreffer für `wasser`, Detail-/Facets-/HTML-/Ping-/QML-404-Prüfungen, read-only RootFS, `USER 1001:0`, nur `/tmp`-tmpfs, `docker diff` leer, keine Lucene-Dateien in `/tmp`, Version/Lucene/Quarkus im Log korrekt. Die JVM-Image-Java-Version ist `21.0.13`; der Native-Compiler ist `Mandrel-25.0.4.1-Final`.

### R3 – Workflow-Reihenfolge, Gates, amd64, gepinnter Builder

Der Workflow `JVM checks` hat `linux-native: needs: jvm`. Der `jvm`-Job führt Versions-/Registry-Vertrag, `clean test quarkusBuild quarkusIntTest`, Chromium-/Firefox-Playwright, `p1`, `p1 --dev`, `p2` aus. Der Native-Job baut und testet zuerst das JVM-Artefakt, sichert das JVM-Image (`kartenkatalog:ci-jvm`), baut danach Native/`testNative`, baut `kartenkatalog:ci-native`, führt p4 mit `--expected-version "$APP_VERSION"`, `--expected-revision "$GITHUB_SHA"`, `--expected-platform linux/amd64` aus und **erst danach** Login/Publish/Verify.

- Die JVM- und Native-Image-Builds verwenden explizit `--platform linux/amd64`; der Mandrel-Builder-Digest ist unverändert `quay.io/quarkus/ubi9-quarkus-mandrel-builder-image@sha256:29a5078c94489623ef7e554aad2cbff70a4ff872ac6d60a46f5538697ed82a30`.
- Der Publish-Schritt baut nichts neu: Er taggt/pusht ausschliesslich `kartenkatalog:ci-native` und `kartenkatalog:ci-jvm`, also genau die zuvor von p4 geprüften lokalen Images.
- Nach dem Push werden beide versionierten Tags gepullt und p4 mit denselben Erwartungen erneut ausgeführt; der Registry-Tag-Report wird ins Job-Summary geschrieben.
- **Lokale Architektur:** Docker Desktop ist Linux/arm64; die lokalen Images und p4-Nachweise sind ehrlich `linux/arm64`. Es wird **nicht** behauptet, dass amd64 lokal ausgeführt wurde. Die amd64-Durchsetzung und der echte GitHub-Lauf sind extern noch ausstehend.

Der zusätzlich versuchte CI-Task `./gradlew testNative` (exakt die Workflow-Variante) schlug **lokal auf macOS** fehl: JUnit 3 Tests, 1 Failure, 2 Skipped; der Fehler lautet `Unable to successfully launch process ... Exit code is '126'` und `.../build/kartenkatalog-0.2.731-runner: cannot execute binary file`. Ursache ist der in einem Linux-Container gebaute **Linux/arm64-ELF**, der auf dem macOS-Host nicht ausführbar ist. Das ist die im Test selbst genannte und in der Aufgabe ausdrücklich erlaubte lokale macOS-Grenze; der Linux-Container-T4 (p4 20/20 plus eigene Native-Probe) wurde stattdessen vollständig ausgeführt. Der CI-Task läuft auf `ubuntu-24.04` (Linux) und ist Teil des nach diesem PASS/FAIL extern auszuführenden GitHub-Laufs. **Dieser lokale Umgebungsfehler ist kein Kandidatenfehler und kein BLOCKED-Grund.** Rohbeleg: `logs/testNative.log`, `logs/testNative-junit.xml`.

### R4 – Veröffentlichungsbedingungen, Secrets, Concurrency

Die Login-, Publish- und Verify-Schritte tragen identisch:

```text
github.ref == 'refs/heads/main' && (github.event_name == 'push' || github.event_name == 'workflow_dispatch')
```

Eigene Bedingungsmatrix (6/6):

| Kontext | erwartet | beobachtet |
|---|---|---|
| `push` auf `refs/heads/main` | veröffentlichen | **true** |
| `workflow_dispatch` auf `refs/heads/main` | veröffentlichen | **true** |
| `pull_request` (`refs/pull/17/merge`) | nicht veröffentlichen | **false** |
| `push` auf `refs/heads/feature-x` | nicht veröffentlichen | **false** |
| `workflow_dispatch` auf `refs/heads/feature-x` | nicht veröffentlichen | **false** |
| `schedule` auf `refs/heads/main` | nicht veröffentlichen | **false** |

`DOCKER_USERNAME`/`DOCKER_PASSWORD` kommen ausschliesslich in `docker/login-action@v4` (Zeilen 83–84) vor; kein `echo`, kein `set -x`, kein Secret in einem Shell-Kommando. Es gibt keine `kubectl`-/`helm`-/`oc apply`-/Deployment-Kommandos im Release-Workflow. Die Concurrency-Gruppe ist `kartenkatalog-${{ github.workflow }}-${{ github.ref }}` mit `cancel-in-progress: true`. Der separate `mirror.yml`-Workflow spiegelt nur nach Codeberg und enthält keinen Docker-Login/-Push und kein App-Deployment.

### R5 – Exakt acht Tags

Der unveränderte Publish-Block wurde aus `.github/workflows/main.yml` extrahiert und mit einem **Fake-Docker** ausserhalb des Kandidaten ausgeführt (`probe/publisher-dryrun.py`). Beobachtete Aufrufe:

```text
docker tag kartenkatalog:ci-native sogis/kartenkatalog:0.2.731
docker push sogis/kartenkatalog:0.2.731
docker tag kartenkatalog:ci-native sogis/kartenkatalog:latest
docker push sogis/kartenkatalog:latest
docker tag kartenkatalog:ci-native sogis/kartenkatalog:0
docker push sogis/kartenkatalog:0
docker tag kartenkatalog:ci-native sogis/kartenkatalog:0.2
docker push sogis/kartenkatalog:0.2
docker tag kartenkatalog:ci-jvm sogis/kartenkatalog:0.2.731-jvm
docker push sogis/kartenkatalog:0.2.731-jvm
docker tag kartenkatalog:ci-jvm sogis/kartenkatalog:latest-jvm
docker push sogis/kartenkatalog:latest-jvm
docker tag kartenkatalog:ci-jvm sogis/kartenkatalog:0-jvm
docker push sogis/kartenkatalog:0-jvm
docker tag kartenkatalog:ci-jvm sogis/kartenkatalog:0.2-jvm
docker push sogis/kartenkatalog:0.2-jvm
```

**8 Tags, 8 Pushes**, exakt die geforderte Native-/JVM-Zuordnung; keine weiteren Docker-Kommandos, kein Login, kein Push. Der Dry-Run hat nichts veröffentlicht. Die vollständige Matrix liegt in `probe/publisher-dryrun.json`.

### R6 – Registry-Verifier

`scripts/verify-published-images.py` prüft für beide Suffixe (`''`, `-jvm`) die vier Tags, akzeptiert nur `0.2.<positive Zahl>`, holt ein öffentliches Pull-Token, liest `Docker-Content-Digest`, verlangt `schemaVersion == 2`, prüft bei Indizes die nicht-`unknown`-Plattformen exakt als `[('linux','amd64')]`, verlangt gleichen Digest je Variante und unterschiedliche Digests zwischen Native und JVM.

Eigener unabhängiger Harness (`probe/registry-harness.py`) mit einem **echten lokalen HTTP-Server** (Statuscodes, Header, Token/Auth, Manifest-JSON; keine Wiederverwendung der vorhandenen `unittest.mock`-Fälle):

| Fall | Erwartung | Beobachtung |
|---|---|---|
| alle 8 Tags, je Variante gleicher Digest | akzeptieren | **PASS** |
| `latest` zeigt auf anderen Native-Digest | ablehnen | `AssertionError: Variant aliases differ` |
| `0.2.731-jvm` fehlt | ablehnen | `HTTPError 404` |
| Index nur `linux/arm64` | ablehnen | AssertionError |
| Index `linux/amd64` + `linux/arm64` | ablehnen | AssertionError |
| Index ohne Laufzeit | ablehnen | AssertionError |
| Index `linux/amd64` + `unknown/unknown` | akzeptieren | **PASS** |
| Native- und JVM-Tag gleicher Digest | ablehnen | `Native and JVM tags must identify distinct images` |
| ungültiger/kurzer Digest | ablehnen | AssertionError |
| `schemaVersion: 1` | ablehnen | AssertionError |
| ungültige Versionen (`0.2.LOCALBUILD`, `0.2.0`, `0.0.731`, `0.2.731-jvm`, `''`, `0.2.073`, `0.2.-1`) | ablehnen, **vor** Token-Request | 7/7 `ValueError`, `requests == []` |

**11/11 Harness-Gruppen PASS.** Die CI-Pull-/P4-Nachprüfung nach dem Push ist statisch im Workflow vorhanden (beide versionierten Tags, vollständige read-only JVM-/Native-Matrix); die öffentlichen Manifeste selbst sind erwartungsgemäss noch nicht vorhanden, weil dieser Review nicht publiziert.

## 5. Frische Kommandos, Exitcodes und JUnit-Zahlen

Alle Kommandos liefen mit explizitem `cwd=/Users/stefan/sources/kartenkatalog-review-release`; die Native-/JVM-Artefakte wurden nicht parallel gebaut.

| Kommando | Exit | Ergebnis |
|---|---:|---|
| `python3 scripts/versioning-contract.py` | 0 | 9 Fälle: 4 Versionen PASS, 5 ungültige abgewiesen |
| `python3 scripts/release-contract.py` | 0 | 6/6 Tests OK |
| `./gradlew dependencies --configuration runtimeClasspath` | 0 | Lucene 10.6.0 core/analysis-common; 0 Treffer für Spring/JDBC/Postgres/Hikari/JOOQ/JPA |
| `GITHUB_RUN_NUMBER=731 ./gradlew clean test quarkusBuild quarkusIntTest playwrightTest --console=plain` | 0 | **62/0/0/0**, **3/0/0/0**, **14/0/0/0** (Tests/Failures/Errors/Skips); `BUILD SUCCESSFUL in 1m 7s` |
| `GITHUB_RUN_NUMBER=731 python3 scripts/p1-process-contract.py` | 0 | JVM-ready + HTTP + 8 harte Startfehler |
| `GITHUB_RUN_NUMBER=731 python3 scripts/p1-process-contract.py --dev` | 0 | `quarkusDev` ready; CORS `localhost:8081` 200, `localhost:9999` 403 |
| `GITHUB_RUN_NUMBER=731 python3 scripts/p2-process-contract.py` | 0 | 886/12-Dokument-RAM-Index, Neustartdeterminismus, geänderte XTF, Index-Startfehler, keine persistierten Dateien |
| `docker build ... Dockerfile.jvm` (VERSION=0.2.731, REVISION=CANDIDATE, linux/arm64) | 0 | `linux/arm64`, User `1001:0`, Labels korrekt |
| `GITHUB_RUN_NUMBER=731 ./gradlew quarkusBuild -Dquarkus.native.enabled=true ...` | 0 | Mandrel-25.0.4.1, Linux/aarch64-ELF, Native-Generierung 2m27s, Gesamt 2m39s |
| `docker build ... Dockerfile.native` (gleiche Args, linux/arm64) | 0 | `linux/arm64`, User `1001:0`, Labels korrekt |
| `python3 scripts/p4-distribution-contract.py ... --expected-platform linux/arm64` | 0 | **20/20** echte Containerfälle |
| Eigene `probe/native-runtime-probe.py` | 0 | 2 Läufe (UID `1000730042:0`, Default-UID), alle Checks PASS |
| Eigene `probe/image-counterprobe.py` | 0 | **7/7** Negativfälle korrekt abgewiesen |
| Eigene `probe/publisher-dryrun.py` | 0 | 8 Tags/8 Pushes, 6/6 Bedingungsmatrix |
| Eigene `probe/registry-harness.py` | 0 | 11/11 Gruppen PASS |
| Eigene `probe/workflow-audit.py` | 0 | 27/27 statische Workflow-Checks PASS |
| Eigene `probe/version-extra.py` | 0 | 16/16 PASS, Manifest `0.2.731` |
| `/tmp/kartenkatalog-release-actionlint/actionlint .github/workflows/main.yml` | 0 | 1.7.12, keine Befunde |
| `./gradlew testNative ...` (nur lokal/macOS) | 1 | 3 Tests, 1 Failure, 2 Skipped; `cannot execute binary file`/Exit 126; erwartete macOS-Grenze, siehe R3 |
| `git diff --check`; `python3 -m py_compile scripts/*.py` | 0 | unverändert/syntaktisch OK |

JUnit-Rohzahlen aus `build/test-results/` (nach dem Pflichtlauf):

- `test`: 11 XML-Dateien, **62 Tests, 0 Failures, 0 Errors, 0 Skipped**
- `quarkusIntTest`: 1 XML-Datei, **3/0/0/0**
- `playwrightTest`: 2 XML-Dateien, **14/0/0/0** (Chromium und Firefox, Desktop/Mobile)

## 6. Native-Laufzeit und OpenShift-Evidenz

- Native-Artefakt: `build/kartenkatalog-0.2.731-runner`, 70.257.592 Bytes, `ELF 64-bit LSB executable, ARM aarch64`, dynamisch gelinkt, gestrippt.
- Native-Build-Log: `Java version: 25.0.4.1+1-LTS, vendor version: Mandrel-25.0.4.1-Final`; `Finished generating 'kartenkatalog-0.2.731-runner' in 2m 27s`; Peak RSS 4.33 GB.
- p4-Native-Startup: `kartenkatalog 0.2.731 native (powered by Quarkus 3.40.1.1)`, `Lucene 10.6.0 ByteBuffersDirectory GermanAnalyzer`; Harvest-Index 62 ms, Search-Index 1–2 ms (kleine Fixture), RSS ca. 102 MiB (Harvest).
- Eigene Native-Probe: 886 Ebenen, 169 Themen, 135 Treffer für `wasser`, `qwcLayerName` korrekt, Facets-Organisation leer (Harvest), Detailseite 200, QML ohne QML 404, `/ping` = `kartenkatalog`, read-only RootFS, tmpfs nur `/tmp`, `docker diff` leer, keine Lucene-Dateien in `/tmp`. Beide UID-Varianten (`1000730042:0` und Default `1001:0`) bestanden.
- JVM und Native lieferten für alle p4-Fixtures identische Payload-Hashes; es gab keinen JVM-Fallback für Native.

## 7. Eigene Negativ-Gegenproben (real, ausserhalb des Kandidaten)

`probe/image-counterprobe.py` leitet Images aus den eigenen JVM-/Native-Images ab und ruft den echten p4-Gate mit falschen Erwartungen auf. Die abgeleiteten Dockerfiles/Reports liegen ausserhalb des Kandidaten; die Quelle blieb unverändert.

| Fall | Eingabe/Erwartung | Beobachtung |
|---|---|---|
| falsches Version-Label | JVM-Image mit Label `0.2.999`, erwartet `0.2.731` | Exit 1; `org.opencontainers.image.version`-Assertion; Inspect zeigt Label `0.2.999` |
| **Label vs. tatsächliche App** | JVM+Native Label `0.2.999`, erwartet `0.2.999`; tatsächliche App ist `0.2.731` | Exit 1; **echter Startup-Log `kartenkatalog 0.2.731 on JVM`**, danach `Application version differs from image label`; Label `0.2.999` |
| falsches Native-Version-Label | JVM korrekt, Native-Label `0.2.999`, erwartet `0.2.731` | Exit 1; Native-Label-Assertion; Inspect zeigt Native-Label `0.2.999` |
| falsche Revision | Label `deadbeef` | Exit 1; Revisions-Assertion |
| falsche Quelle | `https://example.invalid/kartenkatalog` | Exit 1; Quell-Assertion |
| Default-Root-User | `USER 0` | Exit 1; `Image must default to the non-root OpenShift user`; Inspect `User=0` |
| ARM64 als AMD64 | lokales `linux/arm64`-Image, erwartet `linux/amd64` | Exit 1; Inspect `Architecture=arm64` |

**7/7 PASS.** Damit ist nicht nur „Metadaten gegen Metadaten“, sondern auch der entscheidende Label-vs-Laufzeit-Fall mit echtem Containerstart und echtem App-Log belegt.

## 8. Befunde

### F1 (blockierend für R7) – README behauptet weiterhin „kein automatischer Push/Release“

`README.md:101` sagt:

> „… Kein automatischer Push/Release. …“

Das widerspricht `README.md:112-116` („Nur erfolgreiche Main-Builds veröffentlichen …“) und dem tatsächlichen Workflow (Publish-Schritt auf `main`). Es ist eine sachlich falsche Aussage in der aktuellen README, nicht nur eine historische P5-Notiz.

### F2 (blockierend für R7) – OPERATIONS behauptet „CI … ohne Push/Release“

`docs/quarkus-migration/OPERATIONS.md:10` sagt:

> „… CI baut auf der Architektur ihres Linux-Runners, ohne Push/Release.“

Das widerspricht dem neuen Abschnitt „Docker-Hub-Veröffentlichung“ (OPERATIONS:138-160) und dem Workflow. Auch dieser Satz ist eine aktuelle, falsche Release-Aussage.

**Erforderliche minimale Korrektur (nicht vom Reviewer ausgeführt):** Beide Sätze so fassen, dass lokale Builds nicht automatisch pushen, aber Main-Push/Workflow-Dispatch auf `main` nach den Gates veröffentlicht. Beispiel: „Lokale Builds pushen nicht automatisch; ein Push auf `main` bzw. `workflow_dispatch` auf `main` veröffentlicht erst nach den JVM-/Browser-/Native-/Container-Gates.“ Danach ist ein neuer Docs-SHA und die übliche erneute Prüfung erforderlich.

### F3 (nicht blockierend, keine Kandidatenabweichung) – lokaler `testNative`-Lauf auf macOS

`./gradlew testNative` scheitert lokal mit Exit 126 (`cannot execute binary file`), weil das in einem Linux-Container gebaute Linux/arm64-ELF nicht auf macOS ausführbar ist. Das ist die im Test selbst genannte und in der Aufgabe erlaubte lokale Grenze; der Linux-Container-T4 (p4 20/20 + eigene Native-Probe) wurde vollständig ausgeführt. Der CI-Task läuft auf Linux und ist Teil des extern noch auszuführenden GitHub-Laufs. Kein Produktfehler, kein BLOCKED.

### F4 (keine Abweichung) – externe Nachweise absichtlich ausstehend

GitHub-amd64-CI, Docker-Hub-Login/Push, öffentliche 8-Tag-Digests und der Post-Push-Pull-/HTTP-Nachweis wurden **nicht** ausgeführt. Das ist die vom Auftrag vorgegebene Staging-Reihenfolge und wird hier nicht als bereits erfolgt behauptet. Die Implementierung ist für diese Schritte vorbereitet und lokal geprüft; die tatsächliche externe Ausführung bleibt Codex vorbehalten.

## 9. Abschlusszustand

- HEAD am Ende: exakt `fbf8e93d1f55cc5a4c4a0c7cbff3bf1ac9335e77`.
- `git status --porcelain=v1`: leer.
- Keine eigenen Container mehr vorhanden (`docker ps -a` ohne `kartenkatalog-p4-*`/`kartenkatalog-review-*`).
- Kein `docker push`, kein `docker login`, kein GitHub-Push, kein öffentlicher Registry-Pull.
- Der TCP-Sampler wurde beendet; keine fremden Daemons/Container wurden gestoppt.

## 10. Gesamturteil

**FAIL – nur wegen R7/F1+F2 (Dokumentation).**

- **R1–R6: PASS** mit frischen, eigenen Nachweisen.
- **R7: FAIL**, weil die aktuelle README und das Runbook zwei falsche „kein Push/Release“-Aussagen enthalten, obwohl der Release-Abschnitt und der Workflow das Gegenteil belegen.
- Keine funktionale Muss-Abweichung, kein Test-/Build-/Container-/Native-/Registry-Fehler.
- Nach einer Docs-only-Korrektur ist der neue SHA erneut zu prüfen; R1–R6 müssen dafür nicht neu implementiert werden, aber der Reviewprozess verlangt einen neuen Kandidaten-SHA und eine erneute Abnahme.
- Die externe GitHub-amd64-/Docker-Hub-Veröffentlichung bleibt bis dahin unvollständig und darf nicht als bereits erfolgt dokumentiert werden.

## 11. Rohbelege

Unter `/Users/stefan/sources/kartenkatalog-release-review-evidence/fbf8e93d1f55cc5a4c4a0c7cbff3bf1ac9335e77/`:

- `logs/`: `versioning-contract.log`, `release-contract.log`, `dependencies.log`, `jvm-full-gate.log`, `p1-process.log`, `p1-dev-process.log`, `p2-process.log`, `jvm-image-build.log`, `native-build.log`, `native-image-build.log`, `p4-distribution.log`, `native-runtime-probe.log`, `image-counterprobe.log`, `publisher-dryrun.log`, `registry-harness.log`, `workflow-audit.log`, `testNative.log`, `testNative-junit.xml`, `junit-counts.json`, `identity.txt`, `tcp-evidence.txt`, `docs-finding.txt`, `final-git.txt`, `images-inspect.json`, `native-artifact-file.txt`, `class-version.txt`
- `distribution/`: p4-`summary.json` und Container-/Prozesslogs
- `probe/`: eigene Skripte und JSON-Reports (`publisher-dryrun.json`, `registry-harness.json`, `workflow-audit.json`, `version-extra.json`, `counterprobe-reports/`, `native-runtime/`, `tcp-live.log`, `tcp-evidence.txt`)
- `review.md`: dieser Bericht (ausserhalb des Kandidaten; keine Änderung am Kandidaten)
