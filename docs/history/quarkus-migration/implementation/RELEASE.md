# Version 0.2 und Docker-Hub-Veröffentlichung

Basis: `8be37f0a4ceed9d8f6db357f177d81dd0cd7f61c`, sauberer Main-Checkout. Auftrag nach abgeschlossener P5-Migration, keine Änderung der öffentlichen API oder fachlichen Such-/UI-Logik.

## Umsetzung

- Gradle verwendet ausschliesslich `GITHUB_RUN_NUMBER` für **0.2.N**, lokal **0.2.LOCALBUILD**. Jenkins-/Travis-Variablen ignoriert; ungültige gesetzte Laufnummer abgewiesen. `printVersion` bietet dieselbe echte Buildversion für Prüfung/Diagnose.
- Beide Dockerfiles behalten **USER1001:0**, read-only/XTF-/OpenShift-Eignung und gepinnte Basisimages. OCI-Labels für Version, Revision und Quellrepository über Buildargumente.
- Bestehender Workflow behält seine GitHub-Laufnummern. Native-Job wartet auf erfolgreichen JVM-/Browser-/API-/CORS-Job. Images werden einmal gebaut und vor dem Push mit Versions-/Revisions-/Plattform-/User-Prüfung sowie vollständiger Prozessmatrix getestet. Erst danach Login via vorhandene `DOCKER_USERNAME`/`DOCKER_PASSWORD` und Push derselben Images.
- Veröffentlichungsziel **sogis/kartenkatalog, linux/amd64**. Native-Tags `0.2.N`, `latest`, `0`, `0.2`; JVM-Tags `0.2.N-jvm`, `latest-jvm`, `0-jvm`, `0.2-jvm`. Nur Main-Push/Workflow-Dispatch auf Main veröffentlichen; PR/andere Branches nicht. Branchbezogene Concurrency bricht ältere Läufe ab.
- Nach Veröffentlichung prüft `verify-published-images.py` alle acht öffentlichen Registry-Manifeste: Digestgleichheit je Variante, Varianten verschieden, kein fehlender Tag. OCI-Index mit BuildKit-Provenienz akzeptiert nur eine amd64-Laufzeit. Danach erneuter Pull beider Versionstags und volle Prozessmatrix mit erwarteter Version/Revision/amd64. Rohreport/Job-Summary nennt Digests. README/Runbook aktualisiert.

## Tatsächlich ausgeführte Vorabtests

| Prüfung | Befehl | Ergebnis |
|---|---|---|
| Versionsvertrag | `python3 scripts/versioning-contract.py` | Exit0; 4 echte Gradle-Versionen (lokal/GitHub/Legacy/Precedence), 5 erwartete Fehler bei ungültigem N |
| Registryvertrag | `python3 scripts/release-contract.py` | Exit0; **6 Tests**, auch gesplitteter Alias, fehlender Tag, ungültige Releaseversion, erlaubter Provenienzindex und unerwünschte Plattformen |
| Workflow | `actionlint .github/workflows/main.yml` (1.7.12) | Exit0 |
| JVM/T1–T3 | `GITHUB_RUN_NUMBER=73 ./gradlew clean test quarkusBuild quarkusIntTest playwrightTest --console=plain` | Exit0; **62/0/0/0**, **3/0/0/0**, **14/0/0/0** (Tests/Failures/Errors/Skips) |
| Linux-Native | `GITHUB_RUN_NUMBER=73 ./gradlew quarkusBuild -Dquarkus.native.enabled=true -Dquarkus.package.jar.enabled=false -Dquarkus.native.container-build=true -Dquarkus.native.container-runtime=docker -Dquarkus.native.builder-image=quay.io/quarkus/ubi9-quarkus-mandrel-builder-image@sha256:29a5078c94489623ef7e554aad2cbff70a4ff872ac6d60a46f5538697ed82a30 -Dquarkus.native.native-image-xmx=4g --console=plain` | Exit0; Mandrel25.0.4.1, Linux/arm64, 5m05s Native-Kompilierung |
| Image-Metadaten/HTTP | `python3 scripts/p4-distribution-contract.py --jvm-image kartenkatalog:release-jvm --native-image kartenkatalog:release-native --expected-version 0.2.73 --expected-revision 8be37f0a4ceed9d8f6db357f177d81dd0cd7f61c --expected-platform linux/arm64 --report-dir build/reports/release/local` | Exit0; **20/20** echte Containerfälle, tatsächliche Version passt zu Label; beliebige UID/read-only/kein Diskindex/volle Payload-/Ranking-/Facet-/QML-Parität/Startfehler |
| Negative Metadaten | echte Image-Inspect-Aufrufe über `Runner` mit falscher Version/Revision/Plattform | alle 3 vor Prozessstart korrekt abgewiesen |
| JVM erneut paketieren | `GITHUB_RUN_NUMBER=73 ./gradlew quarkusBuild quarkusIntTest --console=plain` | Exit0, Pakettests3/0/0/0 |
| Prozessverträge | `python3 scripts/p1-process-contract.py`, `python3 scripts/p2-process-contract.py`, `GITHUB_RUN_NUMBER=73 python3 scripts/p1-process-contract.py --dev` | alle Exit0; P0-HTTP-Vertrag, Snapshot, harte Startfehler, RAM-Rebuild und erlaubtes/verbotenes CORS |

Native- und JVM-Images wurden jeweils mit `--build-arg VERSION=0.2.73 --build-arg REVISION=<Basis-SHA>` gebaut (beide Docker-Builds Exit0); das sind Vorabtests des noch uncommitteten Produktstands, kein falscher Anspruch auf bereits veröffentlichte Images. **73 wurde lokal als Testlaufnummer gesetzt**, nicht als tatsächlicher GitHub-Release reserviert. Der unabhängige Reviewer baut auf dem exakten Kandidaten-SHA; CI verwendet anschliessend automatisch seine echte Laufnummer und seinen tatsächlichen Commit-SHA.

Ein erster P2-Prozesslauf kollidierte mit parallel laufendem Native-Packaging, das das JVM-Paket entfernt. Dieser Lauf schlug mit fehlendem JAR fehl (Rohlog `*-p2-initial.log`); danach JVM neu gebaut und sämtliche P1/P2/Dev-Proben sequenziell erfolgreich wiederholt. Keine Tests geschwächt/übersprungen und kein Produktfehler verdeckt.

Rohbelege: `/tmp/kartenkatalog-release-*.log`, `build/reports/release/`, JUnit `build/test-results/{test,quarkusIntTest,playwrightTest}`. Python-Syntaxprüfung und `git diff --check` erfolgreich. Lokaler Docker-Host **arm64**; keine lokale amd64-Ausführung behauptet. Der Workflow erzwingt amd64 und prüft diese Plattform vor und nach Veröffentlichung auf dem echten GitHub-amd64-Runner.

## Übergabe vor Veröffentlichung

Status **AWAITING_REVIEW**: unabhängiger neuer OpenCode-/DeepSeek-Review des konkreten lokalen Commits folgt vor jedem GitHub-/Docker-Push. Der Reviewer muss Versions-/Tag-/Trigger-/Dependency-/Metadaten-/OpenShift-Verhalten eigenständig prüfen, T1–T4 frisch ausführen und eigene Negativproben entwickeln. Keine Registry-Zugangsdaten lesen/ausgeben, keine Veröffentlichung durch den Reviewer.

Noch ausstehend, bewusst nach dem Review: tatsächlicher GitHub-amd64-Lauf, Docker-Hub-Push und Nachweis aller acht öffentlichen Tags/Digests plus erneuter Pull-/HTTP-Tests. Diese externen Nachweise werden erst nach wirklicher Ausführung dokumentiert. P5-Prüfbericht bleibt historisch unverändert; keine neue Produktfreigabe aus altem PASS abgeleitet.


## Nacharbeit aus dem ersten unabhängigen Review

OpenCode/DeepSeek-Sitzung `ses_e69d9f482ffeFSP2l2NbXukhdw` prüfte `fbf8e93d1f55cc5a4c4a0c7cbff3bf1ac9335e77`: **R1–R6 PASS**, **R7 FAIL** wegen zwei überholter „kein automatischer Push/Release“-Sätze in README/Runbook. Beide Sätze auf die neue Trennung lokaler Build / Veröffentlichung auf Main korrigiert. Ausschliesslich Dokumentation geändert; kein Code-/Test-/Build-/Workflow-/Dockerfile-Diff gegenüber dem bereits vollständig geprüften Kandidaten.

Unabhängig frisch auf dem ersten Kandidaten: 9 Versionsfälle, 6 Registrytests, 62/3/14 JVM-/Paket-/Browsertests, eigener Mandrel25-Linux/arm64-Build, 20/20 Containerfälle, eigene andere/default-UID-Läufe, 7 negative Image-Gegenfälle, 11 HTTP-Registry-Harnessgruppen, 16 zusätzliche Versionsfälle, 27 Workflowchecks und Publisher-Trockenlauf mit acht Tags plus sechs Triggerfällen. Der zusätzlich versuchte direkte `testNative`-Start eines Linux-ELF auf macOS scheiterte erwartungsgemäss (Exit126, ein Failure/zwei Skips); die verpflichtende Linux-Container-T4-Alternative bestand vollständig. Echte amd64-Pakettests bleiben vor dem Push der Images in CI verbindlich. Dieser optionale lokale Fehlversuch wird nicht als erfolgreicher Lauf behauptet.

Neuer Docs-Kandidat folgt und benötigt ausdrückliches unabhängiges PASS. Die funktionsbezogenen Belege bleiben exakt als Belege für den ursprünglichen SHA gekennzeichnet; keine vorgetäuschte frische Ausführung auf einem anderen SHA. Tatsächliche GitHub-/Docker-Hub-Veröffentlichung weiterhin ausstehend.


## Unabhängige Freigabe vor Veröffentlichung

Die neue OpenCode-/DeepSeek-Sitzung `ses_e69c92d6cffeUa5eTY3BSJ0HfL` hat **`96e266d332a91456ff80e885dea03cac9b074ef6` mit PASS (Pre-Publication), R1–R7**, abgenommen. Tatsächlich `deepseek/deepseek-flash` = DeepSeek-V4.1-Flash-1017 direkt via DeepSeek API, Provider-/Sitzungs-/TLS-Identität verifiziert, Reviewprozess beendet Exit0. Beide Muss-Findings F1/F2 geschlossen, keine offene Implementierungsabweichung.

Der Reviewer bestätigt den byteidentischen Produktbaum gegenüber `fbf8e93` (gefilterter Tree-SHA256 `914b65b8c60ccb0a6065d5baac4e54e979542eee95c1ea7fd3086c10f8846cbf`). Frühere vollständige Funktions-/Native-Belege bleiben ausdrücklich dem ursprünglichen SHA zugeordnet. Frisch auf `96e266d`: Versions-/Registryvertrag (9 Fälle/6 Tests), Actionlint/Python-Syntax/Git-Checks, 23 eigene Dokumentations-/Workflow-Konsistenzchecks. Keine neue Native-Ausführung für die beiden reinen Doku-Sätze behauptet. Echter Linux-amd64-`testNative`/Publish/Pull steht noch aus.

Beide unabhängigen Berichte **byteidentisch** archiviert: `../reviews/RELEASE-initial-review.md` (ursprüngliches R7-FAIL) und `../reviews/RELEASE-review.md` (abschliessendes Implementierungs-PASS). Rohbelege unter `/Users/stefan/sources/kartenkatalog-release-review-evidence/<jeweiliger SHA>/`. Dieser Archivierungscommit enthält nur Dokumentation, keine Produkt-/Build-/Teständerung nach PASS.

Nächster Schritt gemäss Auftrag: gesamte Lieferung nach Main pushen, echten GitHub-amd64-Lauf bis erfolgreich begleiten, alle acht Tags/Registry-Digests und erneute Pull-/HTTP-Tests tatsächlich prüfen. Der erste kombinierte Push enthält seit `8be37f0` auch Nicht-Markdown-Dateien und löst den bestehenden Workflow aus; ein späterer ausschliesslicher Doku-Push allein würde wegen `paths-ignore` keinen Release-Lauf starten. Veröffentlichung weiterhin **nicht** vorgetäuscht; endgültiger Nachweis folgt erst nach realer CI/Registry-Ausführung.


## Tatsächliche Veröffentlichung abgeschlossen: 0.2.73

**GitHub-Lauf73**, Workflow `JVM checks`, Push-Commit **`19300afaa36546fa1972b0cbd5d450a7f57a7467`**: [vollständig erfolgreich](https://github.com/edigonzales/kartenkatalog/actions/runs/41289361002). Kein geplanter, sondern tatsächlich ausgeführter Release. Dieser Commit enthält gegenüber dem unabhängig freigegebenen `96e266d` ausschliesslich unveränderte Reviewberichte und Status-/Implementierungsdokumentation; Produktbaum identisch.

Veröffentlichte Plattform **linux/amd64**, Repository **docker.io/sogis/kartenkatalog**:

| Variante | Tatsächlich veröffentlichte Tags | Registry-Digest (alle vier Tags identisch) |
|---|---|---|
| Native | `0.2.73`, `latest`, `0`, `0.2` | `sha256:e9af3500af679393b01ae06920193c783e834f9f52aaec637ebb09c8be34c753` |
| JVM | `0.2.73-jvm`, `latest-jvm`, `0-jvm`, `0.2-jvm` | `sha256:e86dfd5c197c6f9d35530620a71cb8459cd3f67c98b373ad994c9ead4b40d37e` |

Die Version **0.2.73** stammt aus der echten GitHub-Laufnummer73. OCI-Version **0.2.73**, Revision **19300afaa36546fa1972b0cbd5d450a7f57a7467**, Quelle GitHub-Repository und Defaultbenutzer **1001:0** wurden an beiden Images vor dem Push und nach erneutem Registry-Pull tatsächlich geprüft. Die Registry-Digests sind Manifest-/Indexdigests, nicht mit den abweichenden Docker-Config-Image-IDs zu verwechseln.

### Frische echte GitHub-amd64-Nachweise

Aus heruntergeladenen `TEST-*.xml` selbst gezählt (jeweils Tests/Failures/Errors/Skips):

- JVM-Job: **62/0/0/0** Unit/HTTP, **3/0/0/0** JVM-Paket, **14/0/0/0** Chromium/Firefox; Version-/Registryvertrag, P1/P1-dev-CORS/P2 ebenfalls erfolgreich.
- Native-Job: **62/0/0/0** Unit/HTTP, **3/0/0/0** JVM-Paket und nun auch der echte Linux-amd64-**testNative 3/0/0/0**, frischer Mandrel25-Build.
- **20/20 echte JVM-/Native-Containerfälle vor Veröffentlichung**; daraufhin Login und alle acht `docker push` erfolgreich.
- **20/20 echte Containerfälle nach erneutem `docker pull --platform linux/amd64` beider Versionstags**, einschliesslich Health, XTF-/Lucene10.6.0-Heap-Indexaufbau, P0-API, Liste/Detail/Suche/Ranking/Facets, HTML/HTMX/QML, Neustart/Quelländerung und harten Startfehlern.
- Read-only RootFS/XTF, beliebige UID **1000730000:0**, nur begrenztes RAM-Scratch, keine Indexdateien; Versions-/Revisions-/Plattform-/Default-User-Gates erfolgreich. Beide Laufzeiten liefern Harvest-Payloadhash `80a7d59f710dc9794fb3da72275c1653f19c0b229090165cc35abb23094642d9` vor und nach Veröffentlichung.
- Separater Mirror-Lauf [34](https://github.com/edigonzales/kartenkatalog/actions/runs/41289360988) ebenfalls grün.

Zusätzlich **ausserhalb GitHub Actions** die öffentlichen acht Manifeste erneut gelesen: `python3 scripts/verify-published-images.py --version 0.2.73 --report build/reports/release/public-registry-tags.json`, **Exit0**; alle Aliasdigests je Variante identisch, Varianten verschieden. Das Ergebnis ist byteinhaltlich/JSON-semantisch mit dem CI-Registryreport gleich. Keine Publikationsbehauptung aus einem Trockenlauf abgeleitet.

CI-Rohartefakte: `jvm-reports` (ID13076771100, Digest `sha256:8288390d468da809da26212a699b96e269b0b8653c5e348cc12359908fb6331a`), `linux-native-reports` (ID13077370366, Digest `sha256:9394a4d2ad0320466c526a13c65027a986fd4bb7c48a192b27bdf27c539a14fc`). Lokal heruntergeladen nach `build/reports/release/github-73/`; dort `test-results/`, `reports/p4/distribution/summary.json`, `reports/release/pulled/summary.json`, `reports/release/registry-tags.json`. Die Digests oben sind dauerhaft hier dokumentiert; CI-Artefakte unterliegen GitHubs Aufbewahrungsfrist.

Pull-Beispiele: `docker pull --platform linux/amd64 sogis/kartenkatalog:0.2.73` und `docker pull --platform linux/amd64 sogis/kartenkatalog:0.2.73-jvm`. XTF weiterhin extern read-only einbinden und `/tmp` flüchtig bereitstellen, siehe Betriebshandbuch. Kein OpenShift-Clusterdeployment und keine ARM-Image-Veröffentlichung.

**Auftrag vollständig abgeschlossen:** unabhängig abgenommene Implementierung, Findings behoben, echte amd64-CI grün, beide Images mit allen acht Tags veröffentlicht, Digests und Post-Pull-HTTP-/Index-/Suchverhalten verifiziert. Dieser abschliessende Nachweiscommit ist reine Dokumentation und löst wegen `paths-ignore` keine erneute Image-Veröffentlichung aus.
