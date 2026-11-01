# Unabhängiger Pre-Publication-Re-Review: Docs-only-Korrektur

**Repository:** `edigonzales/kartenkatalog`  
**BASE_SHA:** `8be37f0a4ceed9d8f6db357f177d81dd0cd7f61c`  
**Vorheriger geprüfter Kandidat:** `fbf8e93d1f55cc5a4c4a0c7cbff3bf1ac9335e77`  
**CANDIDATE_SHA / geprüfter HEAD:** `96e266d332a91456ff80e885dea03cac9b074ef6`  
**Review-Worktree:** `/Users/stefan/sources/kartenkatalog-review-release-docs`  
**Evidence:** `/Users/stefan/sources/kartenkatalog-release-review-evidence/96e266d332a91456ff80e885dea03cac9b074ef6/`  
**Datum:** 1. November 2026

## 0. Urteil

**PASS (Pre-Publication) auf `96e266d332a91456ff80e885dea03cac9b074ef6` — R1–R7 PASS.**

Der frühere R7-FAIL ist geschlossen. Die beiden konkret beanstandeten Sätze in `README.md:101` und `docs/quarkus-migration/OPERATIONS.md:10` sind korrigiert und stimmen jetzt mit dem tatsächlichen Main-Workflow überein. Es gibt keine neue blockierende Doku-Abweichung und keine Produkt-, Build-, Test-, CI- oder Dockerfile-Änderung gegenüber dem bereits vollständig geprüften Kandidaten.

Dieser PASS ist **nur die Pre-Publication-Freigabe der Implementierung**. Er behauptet **keine** bereits erfolgte externe Veröffentlichung. Der echte GitHub-amd64-Lauf, ein erfolgreicher Linux-`testNative`, die Docker-Hub-Schreibvorgänge, alle acht öffentlichen Tag-Digests sowie Pull-/HTTP-Nachweise folgen auftragsgemäss **nach** diesem PASS. Diese Reihenfolge ist kein BLOCKED.

## 1. Identität, Provider und direkter Endpoint

- **CLI:** `/Applications/OpenCode.app/Contents/Resources/opencode-cli`, `opencode v2.1.4`.
- **Aufruf:** `opencode run --standalone --auto --model deepseek/deepseek-flash --format json --title "Independent release docs re-review 96e266d" …` (PID 66989, Kind-Server PID 66991).
- **Neue Sitzung:** `ses_e69c92d6cffeUa5eTY3BSJ0HfL`; derselbe Wert steht in `OPENCODE_SESSION_ID` und im laufenden `opencode-run.jsonl`. Es wurde kein alter PASS wiederverwendet.
- **Provider/Modell:** `deepseek/deepseek-flash`, Anzeigename **„DeepSeek V4.1 Flash 1017“**, Provider `deepseek`, API `https://api.deepseek.com`. Das entspricht der geforderten offiziellen Zuordnung `deepseek-flash = DeepSeek-V4.1-Flash-1017`.
- **Direkter Endpoint per TCP:** `dig +short api.deepseek.com` → `d3bbv8sr76az5s.cloudfront.net.` / `3.173.21.63`. Der CLI-Kindprozess PID 66991 hatte während der Sitzung eine etablierte TLS-Verbindung `172.20.10.5:50568 -> 3.173.21.63:443`. Damit ist der tatsächliche Verkehr zum aufgelösten DeepSeek-Endpoint belegt.
- **Kein OpenCode/Zen/OpenRouter-Hosted-Modell:** Der Modellselektor ist `deepseek/deepseek-flash`; die Verbindung zeigt auf den DeepSeek-Endpoint. Die Auth-Provider-Liste enthält unter anderem DeepSeek, Infomaniak und OpenRouter, aber es wurde kein `openrouter/...`-Selektor verwendet. Credential-Werte wurden nicht gelesen oder ausgegeben; die Auth-Provider-Ausgabe wurde ohne Credential-Werte erfasst (nur Anbieter-/Statusangaben).
- **CLI-cwd-Metadatum:** Der laufende CLI-Prozess zeigt als cwd `/Users/stefan/sources/kartenkatalog` (Main-Checkout), obwohl der Launcher den Review-Worktree als cwd setzte. Deshalb wurde **jeder** Repository-/Build-/Testbefehl mit explizitem `cd /Users/stefan/sources/kartenkatalog-review-release-docs` bzw. explizitem `git -C` ausgeführt. Das ist die im Handoff angekündigte CLI-Metadaten-Eigenheit, keine Ausführung im falschen Checkout.
- **Rohbelege:** `logs/identity-current.txt`, `probe/models-dev-api.json`, `opencode-run.jsonl`, `logs/candidate-manifest.txt`.

## 2. Kandidat, Base, Worktree und Unveränderlichkeit

- `96e266d332a91456ff80e885dea03cac9b074ef6` ist der direkte Kind-Commit von `fbf8e93d1f55cc5a4c4a0c7cbff3bf1ac9335e77`; die Basis `8be37f0a4ceed9d8f6db357f177d81dd0cd7f61c` ist Vorfahre.
- Der Review-Worktree ist detached auf exakt `96e266d332a91456ff80e885dea03cac9b074ef6`, `git status --porcelain=v1` war vor Beginn und am Ende leer.
- Der Main-Checkout steht ebenfalls auf exakt `96e266d332a91456ff80e885dea03cac9b074ef6`; `git status --porcelain=v1` ist leer. `main` ist lokal `ahead 2` gegenüber `origin/main` (die zwei noch nicht publizierten Commits `fbf8e93` und `96e266d`).
- Der vollständige Diff `fbf8e93..96e266d` umfasst **3 Dateien, 11 Insertions, 2 Deletions**: `README.md`, `docs/quarkus-migration/OPERATIONS.md` und `docs/quarkus-migration/implementation/RELEASE.md`. Es gibt keine weiteren Änderungen.
- **Produktbaum-Nachweis:** Die gefilterte `git ls-tree -r`-Ausgabe ohne `README.md` und `docs/` ergibt für beide SHAs exakt denselben SHA-256:
  `914b65b8c60ccb0a6065d5baac4e54e979542eee95c1ea7fd3086c10f8846cbf`.
  Zusätzlich sind die Git-Subtrees `.github` (`9e6765ff…`), `src` (`5c7f92c0…`), `gradle` (`f0af6e42…`), `scripts` (`9eda9bc2…`), `data` (`b5199025…`), `deploy` (`596aa72a…`), `build.gradle` (`9251bec0…`) und `settings.gradle` (`8d388a10…`) zwischen beiden SHAs identisch.
- Wichtige Einzelblobs sind ebenfalls identisch, u. a. `.github/workflows/main.yml` (`d5c10c15…`), `gradle/versioning.gradle` (`52ccf610…`), `scripts/versioning-contract.py` (`a7071295…`), `scripts/release-contract.py` (`26714caf…`), `scripts/verify-published-images.py` (`580b766e…`), `Dockerfile.jvm` (`09bafbe2…`) und `Dockerfile.native` (`29563299…`).
- `git diff --check 8be37f0..96e266d` und `git show --check 96e266d` waren Exit 0.
- Der unveränderte Harvest hat weiterhin SHA-256 `de40f025368ae6fc70f5858ab42592488bca837c6942cb6ff8759e6c8803de86`.
- Der vorherige unabhängige Reviewbericht wurde nicht verändert: SHA-256 `d54b9d86e49a7c1dfa5f5877bf2882410dc5c187073f3ed00d8324360969c720` am Ende, nachdem er nur gelesen wurde.
- `GITHUB_RUN_NUMBER=731` ist ausschliesslich mein Testwert; er reserviert keine echte GitHub-Laufnummer und wird nicht als reale Release-Version behauptet.

## 3. Verifizierter Docs-only-Diff

Die beiden ursprünglichen Widersprüche sind wörtlich entfernt:

- **README.md:101 alt:** „… Kein automatischer Push/Release. …“  
  **neu:** „… Lokale Builds pushen nicht automatisch. Main-Push/Workflow-Dispatch auf Main veröffentlicht nach den Testgates die unten beschriebenen Docker-Hub-Tags. …“
- **OPERATIONS.md:10 alt:** „… CI baut auf der Architektur ihres Linux-Runners, ohne Push/Release.“  
  **neu:** „… Der Release-Workflow baut und testet Linux/amd64. Lokale Builds pushen nicht automatisch; Main-Push/Workflow-Dispatch auf Main veröffentlicht nach den JVM-/Browser-/Native-/Container-Gates die unten beschriebenen Docker-Hub-Tags.“

`docs/quarkus-migration/implementation/RELEASE.md` erhält einen neuen Abschnitt „Nacharbeit aus dem ersten unabhängigen Review“ (Zeilen 40–46). Er dokumentiert Sitzung und SHA des Vorreviews, R1–R6 PASS / R7 FAIL, die zwei Korrekturen, die Docs-only-Natur, die bereits attribuierten Funktionsnachweise und den weiterhin ausstehenden externen Publikationsnachweis. Die Aussagen decken sich mit dem Vorreview und den Rohbelegen.

Es wurden **keine** Produkt-, Test-, Build-, Workflow-, Dockerfile-, Daten- oder Skriptdateien geändert. Die drei geänderten Dateien sind ausschliesslich Dokumentation.

## 4. R1–R7 kumulativ

Legende: **Frisch** = in dieser Sitzung auf `96e266d` ausgeführt. **Attribuiert** = unabhängig auf `fbf8e93` ausgeführt; für `96e266d` gilt es nur wegen des nachgewiesen identischen Produktbaums, nicht als frischer Lauf auf dem neuen SHA.

| Ziel | Ergebnis | Beleg und Attribution |
|---|---|---|
| **R1** Versionierung | **PASS** | Frisch auf `96e266d`: `versioning-contract.py` 9/9 (4 echte Versionen, 5 erwartete Ablehnungen), `printVersion` lokal `0.2.LOCALBUILD` und mit `GITHUB_RUN_NUMBER=731` `0.2.731`. `gradle/versioning.gradle` ist blob-identisch. Attribuiert auf `fbf8e93`: 16/16 Zusatzfälle, Manifest `0.2.731`, Startup-Log `kartenkatalog 0.2.731`, Tag-Dry-Run `0.2.731`. |
| **R2** Images/OpenShift/OCI/Laufzeit | **PASS** | Attribuiert auf `fbf8e93`: p4 20/20 echte JVM-/Native-Containerfälle, identische Payload-Hashes, UID `1000730000:0`, read-only RootFS/XTF, nur `/tmp`-tmpfs, kein persistierter Index, Java 21.0.13, Quarkus 3.40.1.1, Lucene 10.6.0; eigene Native-Probe mit UID `1000730042:0` und Default-UID; 7/7 Negativ-Gegenproben. Beide Dockerfiles und `deploy/openshift.yaml` sind blob-/tree-identisch. |
| **R3** CI-Gates/Reihenfolge/amd64 | **PASS** | Attribuiert auf `fbf8e93`: Workflow-Audit 27/27, `linux-native needs: jvm`, JVM-/Browser-/API-/CORS-/P2-Gates vor Native, `testNative` und p4 vor Login/Push, beide Image-Builds `--platform linux/amd64`, gepinnter Mandrel-Digest. Frisch auf `96e266d`: `main.yml`-Actionlint Exit 0 und 23/23 Doku-/Workflow-Konsistenzchecks. Der lokale macOS-`testNative` war **kein** Erfolg (Exit 126, 1 Failure/2 Skips); der echte Linux-CI-`testNative` bleibt Pflicht vor dem Image-Push. |
| **R4** Trigger/Secrets/Concurrency | **PASS** | Attribuiert auf `fbf8e93`: 6/6 Trigger-Matrix (nur Main-Push und Main-`workflow_dispatch` positiv; PR, Fremdbranch, Schedule negativ). Frisch auf `96e266d`: identische Publish-Condition exakt dreimal (Login/Publish/Verify), `DOCKER_USERNAME`/`DOCKER_PASSWORD` nur im Login-Schritt, branchbezogene Concurrency mit `cancel-in-progress: true`, keine Deploy-Kommandos. |
| **R5** exakt 8 Tags | **PASS** | Attribuiert auf `fbf8e93`: echter Fake-Docker-Dry-Run des Publish-Blocks mit 4 Native- + 4 JVM-Pushes, exakt acht Tags, keine weiteren Docker-Kommandos, 6/6 Triggerfälle. `main.yml` ist auf `96e266d` blob-identisch; die frische statische Prüfung bestätigt die zwei Tag-Schleifen. |
| **R6** Registry-Verifier | **PASS** | Frisch auf `96e266d`: `release-contract.py` 6/6 Exit 0. Attribuiert auf `fbf8e93`: eigener Real-HTTP-Harness 11/11 (8 Tags, Digestgleichheit je Variante, Varianten verschieden, fehlender Tag 404, gesplitteter Alias, ARM-only/ARM+amd64/leerer Index abgewiesen, amd64+`unknown/unknown`-Provenienz akzeptiert, ungültige Versionen vor Token-Request abgewiesen). `verify-published-images.py` ist blob-identisch; die öffentlichen Manifeste selbst sind erwartungsgemäss noch nicht vorhanden. |
| **R7** Doku/Regression/Reportertreue | **PASS** | Frisch auf `96e266d`: beide alten Widersprüche fehlen (Grep-Count 0), beide neuen Sätze vorhanden, 23/23 Doku-/Workflow-Konsistenzchecks PASS. Keine aktive Doku-Aussage behauptet mehr, Main-Builds würden nicht veröffentlichen. Historische P5-No-Publish-Sätze sind als P5-historie eingeordnet; der README-Satz „kein Push/Release als versteckter Buildschritt“ ist korrekt auf den lokalen Buildschritt bezogen. Regression/API/UI/Security bleiben über den identischen Produktbaum attribuiert (62/3/14 auf `fbf8e93`). |

## 5. Schliessung der ursprünglichen Findings

### F1 (früher blockierend für R7) – README „kein automatischer Push/Release“: **geschlossen**

`README.md:101` enthält den alten Satz nicht mehr (`grep -c` = 0). Der neue Satz trennt lokalen Build und explizite Main-Veröffentlichung und verweist auf die acht Docker-Hub-Tags weiter unten. Das entspricht `README.md:106-115` und dem Workflow.

### F2 (früher blockierend für R7) – OPERATIONS „CI … ohne Push/Release“: **geschlossen**

`OPERATIONS.md:10` enthält den alten Satz nicht mehr (`grep -c` = 0). Der neue Satz nennt den Linux/amd64-Release-Workflow, die lokale Nicht-Push-Semantik und die Main-Push-/`workflow_dispatch`-Veröffentlichung nach den Gates; er verweist auf den Abschnitt „Docker-Hub-Veröffentlichung“ (Zeilen 139–162), der die acht Tags und den Verifier beschreibt.

### F3 (früher nicht blockierend) – lokaler `testNative` auf macOS: **korrekt eingeordnet**

Der direkte `./gradlew testNative`-Versuch auf macOS scheiterte mit Exit 126 (`cannot execute binary file`), 1 Failure und 2 Skips. Das war ein Linux/arm64-ELF auf einem macOS-Host, kein erfolgreicher Lauf und kein Produktfehler. Die Linux-Container-T4-Alternative bestand mit p4 20/20. Die neue RELEASE-Dokumentation behauptet diesen Fehlversuch nicht als Erfolg und nennt den echten Linux-CI-`testNative` weiterhin als Pflicht vor dem Push. Rohbeleg (Vorreview): `/Users/stefan/sources/kartenkatalog-release-review-evidence/fbf8e93d1f55cc5a4c4a0c7cbff3bf1ac9335e77/logs/testNative.log`.

### F4 (früher keine Abweichung) – externe Nachweise absichtlich ausstehend: **unverändert korrekt**

GitHub-amd64-CI, Docker-Hub-Login/Push, die acht öffentlichen Tag-Digests und die Post-Push-Pull-/HTTP-Tests wurden in diesem Review **nicht** ausgeführt. Die neue RELEASE-Dokumentation sagt das ausdrücklich. Es gibt keine falsche Behauptung, die Veröffentlichung sei bereits erfolgt.

## 6. Frische Kommandos, Exitcodes und Ergebnisse auf `96e266d`

Alle Kommandos liefen mit explizitem cwd `/Users/stefan/sources/kartenkatalog-review-release-docs`.

| Kommando | Exit | Ergebnis |
|---|---:|---|
| `python3 scripts/versioning-contract.py` | 0 | 4 Versionen PASS: `0.2.LOCALBUILD`, `0.2.73`, Legacy-only `0.2.LOCALBUILD`, Precedence `0.2.74`; 5 ungültige Werte (`''`, `abc`, `0`, `-1`, `73/suffix`) abgewiesen |
| `./gradlew -q printVersion` / `GITHUB_RUN_NUMBER=731 ./gradlew -q printVersion` | 0 | `0.2.LOCALBUILD` / `0.2.731`; 731 ist Testwert |
| `python3 scripts/release-contract.py` | 0 | 6/6 Tests OK (acht Tags, gesplitteter Alias, fehlender Tag, Provenienz, unerwünschte Plattformen, ungültige Versionen) |
| `actionlint .github/workflows/main.yml` (1.7.12) | 0 | keine Befunde im Release-Workflow |
| `git diff --check 8be37f0..96e266d` | 0 | keine Whitespace-Fehler |
| `git show --check 96e266d` | 0 | keine Whitespace-Fehler im neuen Commit |
| `PYTHONPYCACHEPREFIX=… python3 -m py_compile scripts/*.py` | 0 | alle Skripte syntaktisch OK |
| eigener `probe/docs-consistency.py` | 0 | **23/23 PASS** (alte Sätze weg, neue Sätze da, Workflow-/Tag-/Secret-/Concurrency-/Versionierungsaussagen konsistent) |
| `git status --porcelain=v1` in Main und Review-Worktree | 0/leer | beide Checkouts tracked clean |

**Zusatzbeobachtung ausserhalb des Release-Scope:** `actionlint .github/workflows/main.yml .github/workflows/mirror.yml` endet mit Exit 1, weil das unveränderte `mirror.yml` noch `actions/checkout@v3` verwendet („runner … too old“). `mirror.yml` ist zwischen Basis und Kandidat blob-identisch (`560a5d14…`) und enthält nur den Codeberg-Spiegel, keinen Docker-Login/-Push und kein App-Deployment. Der Release-Workflow `main.yml` ist sauber. Das ist kein Kandidatenfinding und kein Release-Blocker, sollte aber bei Gelegenheit separat aktualisiert werden.

## 7. Exakt attribuierte frühere Funktionsnachweise (auf `fbf8e93`, nicht auf `96e266d`)

Diese Nachweise wurden **nicht** in dieser Sitzung auf `96e266d` wiederholt. Sie gelten für den neuen SHA nur, weil der Produktbaum nachweislich byte-identisch ist. Die relativen Rohbeleg-Pfade in der folgenden Tabelle liegen unter `/Users/stefan/sources/kartenkatalog-release-review-evidence/fbf8e93d1f55cc5a4c4a0c7cbff3bf1ac9335e77/`.

| Nachweis auf `fbf8e93` | Ergebnis | Rohbeleg |
|---|---|---|
| JVM-/Paket-/Browsertests | **62/0/0/0**, **3/0/0/0**, **14/0/0/0** (Tests/Failures/Errors/Skips) | `logs/junit-counts.json`, `logs/jvm-full-gate.log` |
| P1/P1-dev/P2-Prozessverträge | alle Exit 0 | `logs/process-exits.txt`, `logs/p1-process.log`, `logs/p1-dev-process.log`, `logs/p2-process.log` |
| Linux/arm64-Native-Build | Mandrel 25.0.4.1, Linux/aarch64-ELF | `logs/native-build.log`, `logs/native-artifact-file.txt` |
| p4-Containerparität | **20/20** (10 JVM + 10 Native), identische Payload-Hashes | `distribution/summary.json`, `logs/p4-distribution.log` |
| Native-Runtime-Probe | 2 Läufe (UID `1000730042:0`, Default-UID), read-only, kein Index | `logs/native-runtime-probe.log` |
| Eigene Image-Gegenproben | **7/7** korrekt abgewiesen (falsches Label, Label-vs-Runtime, Revision, Quelle, Root, ARM64) | `logs/image-counterprobe.log` |
| Publisher-Dry-Run | **8 Tags / 8 Pushes**, 6/6 Triggerfälle | `probe/publisher-dryrun.json` |
| Registry-HTTP-Harness | **11/11** Gruppen PASS | `probe/registry-harness.json` |
| Workflow-Audit | **27/27** Checks PASS | `probe/workflow-audit.json` |
| Versions-Zusatzfälle | **16/16** PASS, Manifest `0.2.731` | `probe/version-extra.json` |
| Registry-Vertrag | **6/6** Tests OK | `logs/release-contract.log` |
| Lokaler macOS-`testNative` | Exit 126, 3 Tests / 1 Failure / 2 Skipped — **kein Erfolg** | `logs/testNative.log`, `logs/testNative-junit.xml` |

Wichtig für die Staging-Aussage: Der lokale macOS-`testNative`-Versuch ist **nicht** als erfolgreicher Linux-Lauf zu werten. Die verpflichtende Linux-Container-T4-Alternative bestand mit 20/20; der echte Linux-CI-`testNative` auf dem GitHub-amd64-Runner bleibt vor jedem Image-Push zwingend.

## 8. Doku gegen tatsächliches Verhalten

- `README.md:101` und `OPERATIONS.md:10` beschreiben jetzt denselben Publish-Vertrag wie `.github/workflows/main.yml`: Publish-Condition `github.ref == 'refs/heads/main' && (push || workflow_dispatch)`, Login/Publish/Verify erst nach JVM-, Browser-, Native- und Container-Gates, danach acht Tags und Verifier.
- `README.md:115` und `OPERATIONS.md:143-147` nennen exakt die vier Native- und vier JVM-Tags, die Digestgleichheit je Variante, den amd64-Zwang und den Post-Push-Pull/HTTP-Nachlauf. Das deckt sich mit dem Workflow und `verify-published-images.py`.
- `README.md:74` („Kein Push/Release als versteckter Buildschritt“) ist keine Gegenaussage: Der lokale Gradle-Build enthält keine Push-/Publish-Tasks; die Veröffentlichung ist ein expliziter, bedingter Workflow-Schritt. `build.gradle`, `settings.gradle`, `gradle/` und die Dockerfiles enthalten keine Push-/Publish-Aufrufe.
- `STATUS.md:12` („Kein Push/Merge/Release“) und `implementation/P5.md:34` („kein Push/Deployment“) sind historische P5-Aussagen. `STATUS.md:17-22` trennt den nachgelagerten Version-0.2-/Docker-Hub-Auftrag ausdrücklich von der historischen P5-Abnahme; diese Sätze sind keine aktiven Release-Behauptungen.
- `CODEX_STARTPROMPT.md` und `OPENCODE_REVIEW_PROMPT.md` enthalten Prozessanweisungen („kein Push ohne Auftrag“ bzw. „Reviewer publiziert nicht“), keine Aussage über das Release-Verhalten des Main-Workflows.
- **Keine verbleibende aktive Doku-Aussage** wurde gefunden, die behauptet, Main-Builds würden generell nicht veröffentlichen.

**Nicht blockierende Präzisierungen für den nächsten Schritt:**

1. `main.yml` hat `push.paths-ignore: '**.md'`. Ein reiner Docs-only-Push dieses Commits löst deshalb **keinen** Workflow aus. Für die tatsächliche Veröffentlichung nach diesem PASS ist `workflow_dispatch` auf `main` (oder ein Main-Push mit einer Nicht-Doku-Änderung) zu verwenden. Die Doku nennt `workflow_dispatch` bereits; die Pfadfilterung ist eine Betriebsnotiz, kein Widerspruch.
2. `RELEASE.md:46` sagt noch „Neuer Docs-Kandidat folgt …“. Das war zum Commit-Zeitpunkt korrekt; nach diesem PASS sollte der Archivierungscommit den neuen SHA und das Ergebnis nachtragen.
3. `STATUS.md:22` sagt noch „Review … noch ausstehend“. Das ist vor diesem PASS korrekt und sollte nach PASS/Publikation aktualisiert werden.

## 9. Verbleibende externe Verifikation und Staging

Folgendes ist **nicht** erfolgt und wird hier **nicht** als Nachweis behauptet:

- tatsächlicher GitHub-Actions-Lauf auf dem amd64-Runner,
- erfolgreicher Linux-`testNative` in CI,
- Docker-Hub-Login und die acht Tag-Schreibvorgänge,
- öffentliche Manifest-/Digest-Prüfung der acht Tags,
- Pull beider versionierter Images und erneute HTTP-/Index-/Read-only-Tests,
- Nachweis der tatsächlichen CI-Version `0.2.<echte Run-Nummer>`.

Diese Schritte folgen bewusst nach diesem PASS. Der PASS ist eine Implementierungsfreigabe, keine Publikationsbehauptung. Die Reihenfolge ist kein BLOCKED.

## 10. Abschlusszustand

- Main-Checkout: HEAD exakt `96e266d332a91456ff80e885dea03cac9b074ef6`; `git status --porcelain=v1` leer.
- Review-Worktree: HEAD exakt `96e266d332a91456ff80e885dea03cac9b074ef6`; `git status --porcelain=v1` leer.
- Keine getrackten Dateien geändert, kein Commit, kein Git-Push, kein Docker-Login, kein `docker push`, keine Registry-Pulls durch diesen Reviewer.
- Der vorherige Reviewbericht blieb unverändert (SHA-256 `d54b9d86…`).
- Die frischen Läufe erzeugten nur ignorierte Gradle-Build-Artefakte; diese wurden nach der Prüfung entfernt. Der getrackte Stand blieb clean, `git status --porcelain=v1 --ignored` ist leer.
- Alle eigenen Rohlogs/Proben liegen ausserhalb des Kandidaten unter dem oben genannten Evidence-Verzeichnis.

## 11. Gesamturteil

**PASS (Pre-Publication) auf `96e266d332a91456ff80e885dea03cac9b074ef6`.**

- **R1–R6: PASS** (im Vorreview frisch auf `fbf8e93` belegt; für `96e266d` über den nachgewiesenen identischen Produktbaum und frische Vertrags-/Doku-Checks bestätigt).
- **R7: PASS** — F1 und F2 sind geschlossen; die aktiven README-/OPERATIONS-Aussagen stimmen mit dem Main-Workflow überein; keine verbleibende aktive Gegenaussage.
- Kein FAIL, kein BLOCKED.
- Die externe GitHub-amd64-/Docker-Hub-Veröffentlichung bleibt ausstehend und darf erst nach den tatsächlichen externen Gates als erfolgt dokumentiert werden.

## 12. Rohbelege

Unter `/Users/stefan/sources/kartenkatalog-release-review-evidence/96e266d332a91456ff80e885dea03cac9b074ef6/`:

- `logs/identity-current.txt`, `probe/models-dev-api.json`, `opencode-run.jsonl`
- `logs/candidate-manifest.txt`, `logs/doc-only.patch`, `logs/doc-findings-closure.txt`
- `logs/versioning-contract.log`, `logs/print-version.log`, `logs/release-contract.log`
- `logs/actionlint-main.log`, `logs/actionlint.log`, `logs/git-diff-check.log`, `logs/git-show-check.log`, `logs/py-compile.log`
- `logs/docs-consistency.log`, `probe/docs-consistency.py`
- `logs/final-git.txt`, `logs/final-main-check.txt`, `logs/final-worktree-check.txt`, `logs/final-worktree-check-after-clean.txt`, `logs/final-post-report.txt`, `logs/final-verification.txt`
- `review.md` (dieser Bericht, ausserhalb des Kandidaten)

Frühere unveränderte Funktionsbelege: `/Users/stefan/sources/kartenkatalog-release-review-evidence/fbf8e93d1f55cc5a4c4a0c7cbff3bf1ac9335e77/` (insbesondere `review.md`, `logs/`, `distribution/`, `probe/`).
