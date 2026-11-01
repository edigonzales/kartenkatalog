# Unabhängiger Review P0

## Identität und Kandidat

- **Lieferabschnitt:** P0, AC0-01 bis AC0-04; Phasengate: nein.
- **OpenCode-Version (tatsächlich geprüft):**
  - GUI-Bundle: **2.1.4** (`/Applications/OpenCode.app/Contents/Info.plist`, `CFBundleShortVersionString`).
  - Gebündelte CLI: **2.1.4** (`/Applications/OpenCode.app/Contents/Resources/opencode-cli --version`).
  - Separate PATH-CLI: **2.0.28** (`/Users/stefan/.opencode/bin/opencode`), für diesen Review **nicht** verwendet.
- **Ausführungsänderung (transparent):** Der Review wurde – abweichend von der ursprünglichen GUI-Vorgabe – über die gebündelte OpenCode-CLI 2.1.4 ausgeführt. Der Nutzer hat diese CLI-Ausführung im Handoff ausdrücklich freigegeben; deshalb ist dies **kein** BLOCKED-Grund. Es wurde keine Version 2.2.x behauptet.
- **API-Provider + exakte Modellkennung aus OpenCode-Konfiguration:**
  - Selektor: `deepseek/deepseek-flash`, Provider `deepseek`, Modell-ID `deepseek-flash`, Anzeigename **DeepSeek V4.1 Flash 1017**, Base-URL `https://api.deepseek.com`.
  - Verifiziert über die OpenCode-Models-API: Provider `DeepSeek`, Modell `deepseek/deepseek-flash`, Name `DeepSeek V4.1 Flash 1017`, Status `active` (Beleg: `identity/models-api.json`).
  - Offizielle DeepSeek-Quellen: Die Preistabelle `https://api-docs.deepseek.com/quick_start/pricing/` nennt für `deepseek-flash` die Modellversion **DeepSeek-V4.1-Flash-1017**; der Changelog-Eintrag vom 2026-10-17 bestätigt das Upgrade auf DeepSeek-V4.1-Flash-1017. Belege: `identity/deepseek-pricing.html`, `identity/deepseek-updates.html`.
  - Die Identität wurde **nicht** aus einer Modellselbstauskunft abgeleitet.
- **BASE_SHA:** `eabbb24ab73aad21ed108c13ed8893e6cb8848a5`
- **CANDIDATE_SHA; `git rev-parse HEAD`:** `cf43a66599ea38d63286ee5d95f093ad89bb08a6`
- **Review-Worktree:** `/Users/stefan/sources/kartenkatalog-review-P0`
  - `git status --porcelain=v1` am Anfang und am Ende: leer.
  - `BASE_SHA` ist Vorfahre von `CANDIDATE_SHA`; Kandidat ist detached auf `cf43a66599ea38d63286ee5d95f093ad89bb08a6`.
- **Kandidatendiff:** 8 neue Dateien, +471 Zeilen; ausschließlich Tests, Contract-Fixtures, Python-Smoke, Implementierungsbericht und Statusdokument. **Keine** Änderungen an `src/main`, Build, Daten, Kriterien oder Quellen (Beleg: `candidate.diff`, `logs/final-git.txt`).
- **Prüfumgebung:** macOS 15.8.1/arm64, Temurin OpenJDK **21.0.7+6**, Gradle **9.8.2**, Spring Boot **4.1.2**, Tomcat **11.0.27**; GraalVM **25.0.3** vorhanden, für P0 nicht erforderlich. Java/Gradle/Boot entsprechen dem Implementierungsbericht.
- **Letzter unabhängiger PASS-SHA aus voriger Lieferung:** `none` (P0 ist die erste Lieferung; `STATUS.md` führt ebenfalls `none`).
- **Evidenzverzeichnis:** `/Users/stefan/sources/kartenkatalog-P0-review-evidence/cf43a66599ea38d63286ee5d95f093ad89bb08a6`

## Fachliche Kriterien

| AC-ID | Erfüllt / verletzt / nicht prüfbar | Eigener Code-/HTTP-/Test-/Browser-/Native-Beleg | Finding |
|---|---|---|---|
| **AC0-01** | **Erfüllt** | Implementierungsbericht `implementation/P0.md` dokumentiert HEAD/Basis, Quellstruktur, Java 21.0.7, Gradle 9.8.2, Boot 4.1.2, JTE 3.2.4, jsoup 1.23.2 und den XTF-Start. Unabhängig nachvollzogen: `./gradlew test --rerun-tasks` Exit 0, **24 Tests / 0 Failures / 0 Errors / 0 Skips**; echter `bootRun` mit Profil `xtf` auf freiem Port 18083, Tomcat-Start nach 1,162 s; `GET /api/v1/catalog`, `/api/v1/ebenen`, `/api/v1/themen`, `/thema`, `/ebene/ch.so.agi.gemeindegrenzen` alle mit erwartetem Status ausgeführt. | Kein Finding. |
| **AC0-02** | **Erfüllt** | `src/test/resources/contract/baseline.json` (127 Zeilen) enthält einen vollständigen Layer, ein vollständiges Thema, vier repräsentative Produkte (datasetview, layergroup, facadelayer, extwms), explizite `null`-Werte, leere `keywords`/`synonyms`-Listen sowie 400/404-Fälle. `BaselineContractTests` (5 Tests) prüft echte HTTP-Antworten, DTO-Feldmengen, Mitgliedschaft/Counts, de-CH-Sortierung, Suchgrenzen, Problem-JSON und HTML-Deep-Links; `BaselineQmlTests` (2 Tests) prüft QML/XML-MIME/Attachment und Sanitization über eine kleine synthetische XTF. Die Fixture ist keine Vollkopie sensibler Live-Daten. Eigene Wegwerfkopie-Mutation (`qwcLayerName` → interne UUID) ließ den Contract-Test mit Exit 1 und 1 Failure scheitern (siehe Gegenprobe). | Kein Finding. |
| **AC0-03** | **Erfüllt** | `implementation/P0.md`, Abschnitt „Refactoring-Matrix und konkrete Referenz“ (Zeilen 35–45), enthält `KEEP/PORT/REMOVE` für XTF, Datenrecords, Services, REST, HTML, Tests, JTE, JDBC, Spring und Container/CI. Die SODATA-UI-Referenz ist mit Commit `9db0920e58613ece3bccf5e024199c016705d5de` und konkreten Dateien dokumentiert; der Commit und die genannten Pfade wurden über die GitHub-API unabhängig bestätigt (HTTP 200, Beleg `identity/sodata-commit.json`). Kein unnötiger Gesamtcodeabdruck; der Bericht bleibt bei 51 Zeilen. | Kein Finding. |
| **AC0-04** | **Erfüllt** | Eigene, unabhängige XTF-/HTTP-Gegenprobe: XTF direkt geparst (169 Themen, 886 Ebenen), danach alle 886 API-Layer verglichen. `qwcLayerName` ist für alle Layer der publizierte XTF-`ident_part` und **nicht** die interne XTF-UUID `sourceId`; der HTML-Kartenlink verwendet den publizierten Namen. Unbekanntes Thema und fehlendes QML liefern **404 `application/problem+json`**, `q > 200` liefert **400**, ein echter Nicht-Treffer liefert **200 `[]`**. Handoff korrekt: BASE/CANDIDATE stimmen, Worktree sauber, nur additiver P0-Commit, keine Produktänderung. | Kein Finding. |

## Eigene neue Gegenprobe

### Gegenprobe 1: Publizierte QWC-Layer-ID gegen interne XTF-UUID (positiv, unabhängig)

- **Warum gerade dieser Gegenfall die Phase kritisch testet:** Eine blosses `200`-Prüfung würde auch dann bestehen, wenn die API statt des publizierten Layeridentifiers die interne Harvest-UUID (`sourceId`) als `qwcLayerName` zurückgäbe. Genau dieser Vertrag ist für das QWC2-Plugin und den Kartenlink entscheidend. Die Probe leitet den Sollwert unabhängig aus der XTF ab und vergleicht ihn mit der laufenden API.
- **Eigene Eingabe:** `data/kartenkatalog-harvest-ohne-qml.xtf` (SHA-256 `de40f025368ae6fc70f5858ab42592488bca837c6942cb6ff8759e6c8803de86`), bekanntes Produkt `ch.so.agi.gemeindegrenzen`, XTF-UUID `e01871cf-fb42-5a9c-a7e6-dd8b5d100275`, Thema `harvest-hoheitsgrenzen`.
- **Hypothese/Erwartung:** `id == qwcLayerName == XTF-ident_part`; `sourceId == XTF-id` (UUID) und `sourceId != qwcLayerName`; der HTML-Kartenlink enthält `l=ch.so.agi.gemeindegrenzen`; unbekanntes Thema/fehlendes QML sind 404-Problem-JSON, kein leeres 200.
- **Exakte Schritte/Befehle und Exit:**
  ```sh
  python3 /Users/stefan/sources/kartenkatalog-P0-review-evidence/cf43a66599ea38d63286ee5d95f093ad89bb08a6/counterprobe/independent_contract_probe.py http://127.0.0.1:18083
  # Exit 0
  ```
- **Tatsächliche Ausgabe (Auszug, vollständig in `counterprobe/counterprobe.log`):**
  ```text
  XTF: 169 themes, 886 layers, qml present=0
  API layers: id=qwcLayerName=published XTF ident_part; sourceId=XTF UUID and differs for all 886 entries
  HTML detail uses published qwcLayerName in map link and offers no fake QML link
  Error contracts: unknown theme=404 problem+json, missing QML=404, >200 chars=400, genuine no-match=200 []
  baseline.json layer/theme/products match live payload exactly; all product examples prove qwcLayerName != sourceId
  AGENT-B COUNTER-PROBE PASS
  ```
  Zusätzlich belegt die rohe HTTP-Antwort für `ch.so.agi.gemeindegrenzen`: `id = qwcLayerName = ch.so.agi.gemeindegrenzen`, `sourceId = e01871cf-fb42-5a9c-a7e6-dd8b5d100275`, `themeIdentifier = harvest-hoheitsgrenzen`, `keywords = []`, `synonyms = []`, `organization = null`.
- **Schlussfolgerung:** Der P0-Vertrag ist nicht nur „HTTP 200“, sondern semantisch korrekt: publizierte ID und interne UUID sind getrennt, Fehlerfälle sind unterscheidbar, und die Fixture stimmt mit den Live-Antworten überein.

### Gegenprobe 2: Absichtliche Regression im Wegwerf-Checkout (negativ, mutationsfähig)

- **Warum:** Der positive Lauf allein zeigt nicht, ob die Contract-Tests eine stille Vertragsverletzung tatsächlich entlarven. Deshalb wurde die produktive DTO-Abbildung in einer **separaten Wegwerfkopie** (nicht im Kandidaten) so verändert, dass `qwcLayerName` die interne UUID zurückgibt.
- **Mutation in der Wegwerfkopie:**
  ```diff
  - CatalogText.terms(e.synonyms()), e.ident_part(), e.derived_identifier()
  + CatalogText.terms(e.synonyms()), e.id(), e.derived_identifier()
  ```
- **Befehl/Exit:**
  ```sh
  ./gradlew test --tests '*BaselineContractTests.publicPayloadsRetainNamesTypesNullsListsAndPublishedQwcIdentifier' --rerun-tasks
  # Exit 1 (erwartet)
  ```
- **Tatsächliche Ausgabe:** `BaselineContractTests > publicPayloadsRetainNamesTypesNullsListsAndPublishedQwcIdentifier() FAILED`, `AssertionFailedError at BaselineContractTests.java:42`; JUnit `1 Test / 1 Failure / 0 Errors / 0 Skips` (Beleg: `counterprobe/mutation-test-focused.log`, `junit`-Kopie der Wegwerfkopie nicht im Kandidaten). Die Wegwerfkopie wurde anschließend vollständig entfernt.
- **Schlussfolgerung:** Die P0-Contract-Tests erkennen die fachlich gefährlichste Verwechslung (publizierte ID vs. interne UUID) und sind keine blossen „happy path“-Tests. Der Kandidat selbst wurde dabei nicht verändert.

## Pflichtprüfungen

| Testfamilie | Ausgeführter Befehl | Exit | Tests/Failures/Errors/Skips | Rohreport | SHA-bezogen |
|---|---|---:|---|---|---|
| JUnit/Contract (frisch) | `./gradlew test --rerun-tasks --console=plain` | **0** | **24 / 0 / 0 / 0** | `logs/gradle-test.log`, `logs/gradle-test.exit.txt`, `junit/TEST-*.xml` | Ja, `cf43a665…` |
| Echter XTF-Prozess (bootRun) | `./gradlew bootRun --console=plain --args="--spring.profiles.active=xtf --server.address=127.0.0.1 --server.port=18083"` | Langlaufprozess nach Tests absichtlich beendet; Start erfolgreich (Tomcat 18083, „Started … in 1.162 seconds“) | n/a | `logs/bootrun.log` | Ja, `cf43a665…` |
| Bestehender HTTP-Contract-Smoke | `python3 scripts/p0-http-contract.py http://127.0.0.1:18083` | **0** | Alle Assertions erfolgreich | `smoke/codex-http-contract.log`, `smoke/codex-http-contract.exit.txt` | Ja, `cf43a665…` |
| Eigene unabhängige Gegenprobe | `python3 …/counterprobe/independent_contract_probe.py http://127.0.0.1:18083` | **0** | 169/886, alle 886 IDs/UUIDs/Fehlerfälle geprüft | `counterprobe/counterprobe.log`, `counterprobe/counterprobe.exit.txt` | Ja, `cf43a665…` |
| Mutations-Canary (Wegwerfkopie) | `./gradlew test --tests '*BaselineContractTests.publicPayloadsRetainNamesTypesNullsListsAndPublishedQwcIdentifier' --rerun-tasks` | **1 (erwartet)** | 1 / 1 / 0 / 0 | `counterprobe/mutation-test-focused.log`, `counterprobe/mutation-test.exit.txt` | Wegwerfkopie von `cf43a665…`, nicht Kandidat |
| Fixture-Integrität | `shasum -a 256 data/kartenkatalog-harvest-ohne-qml.xtf` | **0** | SHA `de40f025…` | `logs/fixture-sha256.txt` | Ja, `cf43a665…` |
| Diff-Hygiene | `git diff --check eabbb24… cf43a665…` | **0** | keine Whitespace-Fehler | `logs/diff-check.log`, `logs/diff-check.exit.txt` | Ja, `cf43a665…` |

- **Fällige, aber nicht ausgeführte Checks und genaue Ursache:** Keine. P0 verlangt weder Browser- noch Native-Gate; `bootRun` ist ein Langlaufprozess und wurde nach den HTTP-Smokes absichtlich beendet (Port 18083 danach geschlossen, keine Restprozesse). Aus dem absichtlichen Beenden wird **kein** erfolgreicher Exitcode abgeleitet.
- **Bei P3b:** nicht anwendbar (P0).
- **Bei P5:** nicht anwendbar (P0).
- **Kandidat unverändert:** `HEAD` am Ende weiterhin `cf43a66599ea38d63286ee5d95f093ad89bb08a6`; `git status --porcelain=v1` leer; keine Commits, kein Push; keine Änderung an getrackten Dateien, Kriterien, Quellen, Tests, Daten oder Builddateien. Eigene Dateien liegen ausschließlich im externen Evidenzverzeichnis. Build-Ausgaben unter `build/` sind erlaubt und nicht getrackt.

## Findings

- **FINDING-01:** Keine offene Muss-Abweichung. Alle vier P0-Kriterien sind erfüllt; die Pflichtprüfungen und die eigene Gegenprobe sind erfolgreich.

**Beobachtungen ohne Abnahmewirkung:**

- **OBS-01:** `BaselineContractTests` prüft zusätzlich `id == qwcLayerName`. Da beide im DTO aus `ident_part` stammen, ist diese Assertion allein eine Vertrags-Pinnung und keine unabhängige Herleitung. Die unabhängige XTF-Ableitung und die Wegwerfkopie-Mutation schließen diese Lücke für AC0-04; kein Finding.
- **OBS-02:** Die im Implementierungsbericht dokumentierten Altmängel (Root-Redirect, fragment-only `/thema`, relative Detail-Links, fehlender No-JS-Fallback, CDN-Abhängigkeiten, leere Detailfelder, `[0]`-Transparenzsuffix) sind ausdrücklich **nicht** als P0-Sollnorm eingefroren und werden P1/P3 zugeordnet. Das ist mit dem P0-Auftrag „keine Produktfunktionalität verändern“ vereinbar; kein Finding.
- **OBS-03:** Der `bootRun`-Prozess wurde nach den HTTP-Prüfungen beendet. Der fehlende „normale“ Exitcode eines Langlaufprozesses ist kein Testfehler; die erfolgreiche Initialisierung und alle HTTP-Antworten sind separat belegt.

## Unabhängiges Urteil

**PASS**

- **Begründung:** Der Kandidat `cf43a66599ea38d63286ee5d95f093ad89bb08a6` erfüllt AC0-01, AC0-02, AC0-03 und AC0-04 nach unabhängiger Quell-, Test-, HTTP- und XTF-Prüfung. Der frische Testlauf ergibt **24/0/0/0**; der echte XTF-Prozess liefert die erwarteten API-/HTML-Antworten; die eigene Gegenprobe bestätigt die publizierte QWC-ID, die UUID-Trennung und die 400/404-Semantik; die Wegwerfkopie-Mutation zeigt, dass die Contract-Tests eine echte Regression erkennen. Es gibt keine offene Muss-Abweichung.
- **Nächster freigebbarer Lieferabschnitt:** **P1** – erst nach diesem PASS. P1 wurde in diesem Review nicht begonnen.
- **Geprüfter SHA:** `cf43a66599ea38d63286ee5d95f093ad89bb08a6`

### Rohbelege (absolut)

- Review: `/Users/stefan/sources/kartenkatalog-P0-review-evidence/cf43a66599ea38d63286ee5d95f093ad89bb08a6/review.md`
- Testlauf: `…/logs/gradle-test.log`, `…/logs/gradle-test.exit.txt`, `…/junit/TEST-*.xml`
- Prozess: `…/logs/bootrun.log`; HTTP: `…/smoke/codex-http-contract.log`, `…/smoke/raw/`
- Gegenprobe: `…/counterprobe/independent_contract_probe.py`, `…/counterprobe/counterprobe.log`, `…/counterprobe/mutation-test-focused.log`
- Identität: `…/identity/models-api.json`, `…/identity/deepseek-pricing.html`, `…/identity/deepseek-updates.html`, `…/identity/sodata-commit.json`
- Git: `…/logs/final-git.txt`, `…/candidate.diff`, `…/logs/diff-check.log`
