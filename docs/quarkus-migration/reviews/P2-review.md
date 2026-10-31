# Unabhängiger Review P2

## Identität und Kandidat

- **Lieferabschnitt:** P2 – In-Memory-Lucene, Suche und fachliche Facets.
- **AC-IDs:** AC2-01, AC2-02, AC2-03, AC2-04. **PHASENGATE:** nein.
- **Rolle:** Agent B, unabhängiger Prüfer; keine Implementierung, keine Folgephase (P3a nicht begonnen).
- **OpenCode-Version (tatsächlich geprüft):** gebündelte CLI **`opencode v2.1.4`**
  (`/Applications/OpenCode.app/Contents/Resources/opencode-cli --version`, Exit 0). Es wird **keine
  2.2.x-Version** behauptet. Start erfolgte durch den Nutzer über
  `opencode-cli run --standalone --auto --model deepseek/deepseek-flash --format json`
  (Prozess-Kommandozeile in `identity/launch-and-auth.txt`).
- **API-Provider + exakte Modellkennung (aus OpenCode-Metadaten/Konfiguration, nicht Modellselbstauskunft):**
  - Selektor **`deepseek/deepseek-flash`**, Provider-ID **`deepseek`**, Anzeigename
    **„DeepSeek V4.1 Flash 1017“**, Status `active`
    (lokale OpenCode-Modelldaten, `tools.opencode.models`, Provider `deepseek` →
    `identity/models-api.json`).
  - **Session-Metadaten des tatsächlich gelaufenen Reviews:** In der lokalen OpenCode-Sessionablage
    (`session_message`, Session `ses_e6c9af4bcffeG9QcjpjBpqdPsw`) tragen **alle 72 Assistant-Nachrichten**
    exakt `"model":{"id":"deepseek-flash","providerID":"deepseek"}` (`identity/session-model-metadata.txt`).
    Das ist Provider-/Modellmetadatum der Sitzung, keine Selbstauskunft des Modells.
  - **Direkter DeepSeek-Endpunkt:** Die gebündelte CLI definiert für den Provider `deepseek` die Base-URL
    **`https://api.deepseek.com/v1`** (Binärstring, `identity/provider-connection.txt`). Der
    Inferenz-Kindprozess (`opencode-cli serve --stdio`, PID 87110) hatte als **einzige ausgehende
    öffentliche TCP-Verbindung** `192.168.10.232:49162 -> 3.173.21.63:443`; `dig +short api.deepseek.com`
    liefert genau `3.173.21.63` (CloudFront). Kein OpenCode-Zen-, Go- oder OpenRouter-Endpunkt war verbunden.
    Zusätzlich bestand eine lokale Verbindung zu `127.0.0.1:11434` (lokaler Ollama-Dienst); der gewählte
    Provider-/Modellpfad ist laut Session-Metadaten und CLI-Aufruf `deepseek/deepseek-flash`, und es gab keine
    öffentliche Verbindung zu einem anderen Inferenzendpunkt.
  - **Offizielle DeepSeek-Quelle:** `https://api-docs.deepseek.com/quick_start/pricing/` (abgerufen und
    archiviert als `identity/deepseek-pricing.html`, Auszug `identity/deepseek-pricing-excerpt.txt`) nennt
    **`deepseek-flash` = Modellversion `DeepSeek-V4.1-Flash-1017`**, Base URL `https://api.deepseek.com`,
    Preise $0.003 / $0.15 / $0.6 pro 1M Tokens (Cache-Hit-Input / Input / Output, Off-Peak). Das deckt sich
    mit den lokalen OpenCode-Modelldaten.
  - **Credentials:** Es wurde **keine** Key-Variable gesetzt, keine neue Installation vorgenommen und
    **keine** Credential-Datei gelesen oder ausgegeben. Der Auth-Store enthält die Provider-IDs
    `deepseek`, `infomaniak`, `openrouter` (nur Namensliste in `identity/launch-and-auth.txt`).
    `execution-identity.json` wurde nur als Startmetadatum gelesen und **nicht** als Identitätsbeleg verwendet.
- **BASE_SHA:** `99143256de729a26f137bef2b5865f0b8ffe3597`
- **CANDIDATE_SHA; `git rev-parse HEAD`:** `a9dde956b4937916cec862c6c672cf26dd30636f`
- **Review-Worktree:** `/Users/stefan/sources/kartenkatalog-review-P2` (detached HEAD auf exakt dem Kandidaten).
  - `git status --porcelain=v1` vor und nach dem Review: **leer** (`logs/00-git-before.txt`,
    `logs/99-git-after.txt`).
  - Keine Commits, kein Push, keine Änderung an getrackten Source-/Test-/Build-/Daten-/Kriteriendateien.
    Eigene Fixtures/Skripte/Mutationen liegen ausschließlich im externen Evidenzverzeichnis; `build/` enthält
    nur erlaubte Build-Artefakte. Kein Codex-Implementierungscheckout wurde verwendet.
- **Kandidatendiff:** 29 Dateien, **+844/−54**; vollständig gelesen und als `candidate.diff` (1202 Zeilen)
  archiviert.
- **Prüfumgebung:** macOS 15.8.1/arm64, Temurin **OpenJDK 21.0.7+6**, Gradle **9.8.2**, Quarkus
  **3.40.1.1**. GraalVM/Native und Browser sind für P2 **nicht fällig** (Native/Linux = P4,
  Browser/UI-Gate = P3a/P3b) und werden hier nicht als Kriterium bewertet.
- **Letzter unabhängiger PASS-SHA aus voriger Lieferung:** `2014f6035638730739820d391a61423a1c967bf3` (P1).
- **Externes Evidenzverzeichnis:**
  `/Users/stefan/sources/kartenkatalog-P2-review-evidence/a9dde956b4937916cec862c6c672cf26dd30636f`

## Fachliche Kriterien

| AC-ID | Erfüllt / verletzt / nicht prüfbar | Eigener Code-/HTTP-/Test-/Prozess-Beleg | Finding |
|---|---|---|---|
| **AC2-01** | **Erfüllt** | `build.gradle` pinnt **genau** `lucene-core:10.6.0` und `lucene-analysis-common:10.6.0`. Frischer `./gradlew dependencies --configuration runtimeClasspath` → **Exit 0**, Graph enthält **nur** diese beiden Lucene-Artefakte (beide 10.6.0), **keinen** Queryparser, kein Spring/JDBC/Postgres/Hikari/JTE (`logs/02-dependencies-runtimeClasspath.log`); im gebauten `build/quarkus-app/lib/main/` liegen exakt `lucene-core-10.6.0.jar` und `lucene-analysis-common-10.6.0.jar`. Quellcode: `CatalogSearchIndexBuilder` erzeugt ausschließlich `new ByteBuffersDirectory()`, `GermanAnalyzer` (leere Stopwortliste), `IndexWriter`, `DirectoryReader`, prüft `numDocs == layers.size()`, Duplikat-IDs und fehlende Themen. Keine `FSDirectory`/`MMapDirectory`/`NIOFS`/Pfad-/Datei-Index-API im Produktionscode (Grep leer). Der Index liefert nur `SearchHit(id, score)`; die Fachebene wird über die Snapshot-Map aufgelöst. Eigener Live-JVM-Beleg: `jcmd <pid> GC.class_histogram` enthält `org.apache.lucene.store.ByteBuffersDirectory`, aber **keine** `MMapDirectory`/`FSDirectory`-Instanz; `lsof` zeigt keine `.cfs`/`segments_`/`write.lck`-Dateien und keine Dateihandles im (schreibgeschützten) Arbeitsverzeichnis; Verzeichnisinhalt vor/nach identisch. Dokumentzahl/IDs: 14 eigene Ebenen → 14 Indexdokumente, ID-Menge == Ebenen-Identifier; Harvest 886 → 886. Neustart/Determinismus: zwei Prozesse mit derselben XTF liefern identische IDs, Rangfolge und Facets; geänderte XTF ändert die neu aufgebaute Rangfolge. Startup/Ready/503: absichtlich nicht indizierbarer Titel → Prozess-Exit 1, nie ready, Port geschlossen, Log enthält `Cannot build catalog RAM index`; nach `snapshot.close()` liefern Suche/Facets (auch leerer Suchtext) `SearchUnavailableException` → HTTP 503 problem+json, Readiness DOWN, Detailabfrage bleibt 200. Close ist idempotent; Builder-Fehler (Duplikat-ID, unaufgelöstes Thema) schließen ohne Fallback. | Kein Finding. |
| **AC2-02** | **Erfüllt** | Eigene XTF-Fixture (14 Ebenen, 3 Themen, außerhalb des Kandidaten) und eigene Java-/HTTP-Probe: Ranking-Tiers exakt **exakte Kennung > exakter Titel > Titelpräfix > Titelteilstring > Keyword/Synonym > Thema/Organisation > Beschreibung** inkl. Score-Vergleich und Identifier-Tie-Break bei gleichem Titel; Cross-Field-AND (`gemeinsam wald`: Titel-Token + Keyword/Synonym-Token) liefert genau die zwei erwarteten IDs, UND schlägt fehl, wenn ein Token nirgends vorkommt. Alle Felder suchbar: Identifier, Titel (exakt/Präfix/Substring), Keywords, Synonyme, Thema-Titel, Organisation, Klartextbeschreibung (HTML-Tags nicht indexiert). Normalisierung: Gross-/Kleinschreibung, Akzente/Umlaute (`grunflache` == `Grünfläche`), `ß`/`ss` (`strasse` == `Straße`), Interpunktion/Mehrfachleerzeichen; `AV` (2 Zeichen) findet die AV-Ebene; 1-Zeichen-/Sonderzeichen-Queries werden kontrolliert mit 400 abgelehnt. Leere/missing Metadaten werden weder erfunden noch als Treffer/Facet-Wert ausgegeben (`probe.null`). Doppelprodukte: ein Dokument je `ident_part`. Codex-Tests decken dieselben Punkte zusätzlich auf separater Fixture ab; die eigene Probe ist nicht aus diesen Tests abgeleitet. | Kein Finding. |
| **AC2-03** | **Erfüllt** | Eigene HTTP-/Java-Probe: `thema`, `typ`, `organisation` mehrfach; **OR innerhalb** einer Gruppe (`typ=layergroup&typ=extwms`), **AND zwischen** Gruppen (`thema=gewaesser&typ=extwms`), Suchtext AND Filter. Unbekanntes Thema → **404** (P0-Vertrag), unbekannter Typ/Organisation → **400** problem+json, ungültige Sortierung → 400, `relevance` ohne Text → 400, `q` > 200 Zeichen (auch 201 Leerzeichen) → 400, 1 Zeichen/Sonderzeichen → 400. Leerer Suchtext liefert die vollständige sortierte Liste, kein Indexfehler; echte Filterkombination ohne Treffer liefert `200 []`; geschlossener Index liefert **503**, nie eine leere Erfolgsliste. Dynamische Facets: eigene Gruppe wird beim Zählen ausgelassen, alle anderen Filter und der Suchtext zählen; exakte eigene Zahlen geprüft (z. B. `q=wald`: Thema 8/1/0, Typ 7/1/1/0, Organisation 3/2; mit `thema+typ`: Thema zählt trotz eigenem Filter 8/1/1, Typ ignoriert eigenen Filter 8/1/1/0, Organisation 3/3; ausgewählte 0-Treffer-Optionen bleiben `selected=true`). Nur echte Organisationswerte werden gelistet; Harvest-`organisation` ist leer (keine Dummy-Organisation), Typen nur tatsächlich vorkommende `dtype`-Werte. Stabile Auswahl-IDs sind die unveränderten Quellwerte. | Kein Finding. |
| **AC2-04** | **Erfüllt** | P0/P1-Verträge frisch: `BaselineContractTests` (P0-`baseline.json`: Feldnamen, `null`/Listen, `qwcLayerName`, 400/404/problem+json, 169/886, fünf API-Formen, HTML-Deep-Links) und `scripts/p1-process-contract.py` (echter 886er-JVM, Health, Snapshot-Lebensdauer, 8 Startfehler) sind im frischen Lauf grün. Sortierung kontrolliert: Standard `titel-asc` de-CH mit Identifier-Tie-Break, `title-desc` dreht nur die Titelrichtung, `relevance` nur mit Suchtext; eigener Test mit gleichen/ähnlichen Titeln bestätigt Identifier-Tie-Break auch absteigend. Separater JVM-Prozess baut den RAM-Index bei jedem Start frisch aus XTF (siehe AC2-01). Frische JUnit-Reports auf dem Kandidaten (55 Tests), keine historischen Artefakte. Eigene neue Gegenprobe (siehe unten) deckt Cross-Field-AND, Ranking-Tiers, Filter, Facet-Selbstausschluss, Nullwerte, 503/Close, Startup-Fehler und den Heap-/Nicht-Dateisystem-Pfad ab. | Kein Finding. |

## Eigene neue Gegenprobe

### Warum diese Gegenfälle die Phase kritisch testen

Die P2-Risiken liegen nicht in der blossen Existenz von Lucene, sondern in (a) einer falschen Rang- oder
AND-Semantik, die ein grüner Test mit derselben Fixture nicht bemerkt, (b) Facet-Zahlen, die den eigenen
Filter mitzählen, (c) einem stillen Dateisystem-Index oder einem leeren Erfolg bei Indexausfall und (d) einem
Start, der bei Indexfehlern trotzdem „ready“ wird. Deshalb wurden **eigene**, vom Kandidaten unabhängige
Fixtures und Programme verwendet; keine Codex-Testdatei wurde kopiert oder umbenannt.

### Gegenprobe 1 – eigener Java-Kern-Test (81 Checks, Exit 0)

- **Eigene Eingabe:** `probe/probe-catalog.xml` – 14 Ebenen, 3 Themen (`Gewässer`, `Waldthema`, `Planung`),
  bewusst kontrollierte Kollisionen: `ident_part=wald`, Titel `Wald`, `Waldgebiet`, `Urwald`, zwei Ebenen mit
  gleichem Titel `Gemeinsam` (Keyword vs. Synonym → Identifier-Tie-Break), Organisations-Treffer
  `Amt Wald`, Themen-Treffer `Waldthema`, Beschreibungs-Treffer `Wald im Winter`, Ebenen ohne
  Keywords/Synonyme/Organisation/Beschreibung, `Straße`, `Grünfläche`, `AV Daten`, `facadelayer`.
- **Hypothese:** exakte Kennung > exakter Titel > Präfix > Teilstring > Keyword/Synonym > Thema/Organisation >
  Beschreibung; Titel-/Identifier-Tie-Break; Cross-Field-AND; OR innerhalb/AND zwischen Filtern; Facets ohne
  eigenen Filter; Null-Organisationen tauchen nirgends als Filter-/Facet-Wert auf; geschlossener Index → harte
  Ausnahme statt leerer Liste.
- **Kommando/Exit:**
  ```sh
  javac -encoding UTF-8 -cp "build/quarkus-app/app/kartenkatalog-0.0.LOCALBUILD.jar:build/quarkus-app/lib/main/*" \
    -d "$EV/probe/classes" "$EV/probe/Probe.java"          # Exit 0
  java -cp "$EV/probe/classes:build/quarkus-app/app/kartenkatalog-0.0.LOCALBUILD.jar:build/quarkus-app/lib/main/*" \
    Probe "$EV/probe/probe-catalog.xml"                     # Exit 0, 81 Checks
  ```
- **Tatsächliche Ausgabe (Auszug, vollständig `probe/probe-run.log`):**
  ```text
  PASS Lucene version expected=10.6.0 actual=10.6.0
  PASS Directory is ByteBuffersDirectory ...
  PASS Analyzer is GermanAnalyzer ...
  PASS index document count equals layer count expected=14 actual=14
  PASS index IDs equal layer identifiers ...
  PASS ranking tiers ... expected=[wald, probe.b, probe.c, probe.d, probe.a.key, probe.z.syn, probe.h, probe.theme, probe.i]
  PASS cross-field AND: title token + keyword/synonym token expected=[probe.a.key, probe.z.syn]
  PASS AND fails when one token is absent expected=[] actual=[]
  PASS facet theme gewaesser under q=wald expected=8 actual=8
  PASS theme facet ignores its own group filter expected=8 actual=8
  PASS type facet ignores its own group filter expected=8 actual=8
  PASS selected theme kept at count 0 expected=0 actual=0
  PASS blank search after close fails, no empty success threw SearchUnavailableException
  PASS duplicate index id rejected without fallback threw IllegalStateException
  PASS unresolved index theme rejected without fallback threw IllegalStateException
  PROBE OK: 81 checks, 14-document own fixture, ByteBuffersDirectory/GermanAnalyzer, ranking/facets/close/error paths
  ```
- **Schlussfolgerung:** Die fachliche Such-, Rang-, Filter- und Facet-Semantik stimmt auch mit einer
  unabhängigen, anders aufgebauten Fixture; Indexausfall und Fehlerpfade liefern keine falsch-leeren Treffer.

### Gegenprobe 2 – echter JVM-/HTTP-Prozess, read-only, Heap-Index (Exit 0)

- **Eigene Eingabe:** dieselbe Fixture, gestartet als gebautes `quarkus-run.jar` mit
  `CATALOG_XTF_LOCATION=file:<eigene Fixture>`, Arbeitsverzeichnis `probe/runtime` auf **0555** (read-only),
  Fixture 0444.
- **Hypothese:** Ranking/Filter/Facets funktionieren über echtes HTTP; der Live-JVM hält den Index im Heap;
  es entstehen keine Lucene-Dateien; defekte XTF und nicht indizierbarer Titel verhindern Start/Ready.
- **Kommando/Exit:** `python3 "$EV/probe/http_probe.py"` → **Exit 0** (Log `probe/http-probe-console.log`,
  `probe/http-probe.log`, `probe/summary-http.json`).
- **Tatsächliche Ausgabe:**
  ```text
  HTTP ranking tiers: wald > probe.b > probe.c > probe.d > probe.a.key > probe.z.syn > probe.h > probe.theme > probe.i
  JVM heap holds org.apache.lucene.store.ByteBuffersDirectory; no MMap/FS Directory instance
  lsof: no Lucene segment/lock files and no working-directory file handles
  invalid-reference: exit=1, not ready, port closed, log contains "Cannot load catalog XTF"
  broken-index: exit=1, not ready, port closed, log contains "Cannot build catalog RAM index"
  ALL OWN HTTP/PROCESS PROBES PASSED
  {"ok": {"documents": 14, "ids": 14, "rssKiB": 179904, "medianSearchMs": 5.883, "indexLog": "133"}, ...}
  ```
  Zusätzlich geprüft: 14 eindeutige IDs; `gemeinsam wald` → 2 Cross-Field-Treffer; `AV` → AV-Ebene;
  `Straße`/`grunflache` → Normalisierung; `organisation=Amt Wald` → 3 Treffer; Facet-Selbstausschluss inkl.
  ausgewählter 0-Option; `q=201 Zeichen`, `sort=relevance` ohne Text, unbekannter Typ/Organisation → 400
  problem+json; unbekanntes Thema → 404 problem+json.
- **Schlussfolgerung:** Der RAM-Pfad ist real (nicht nur behauptet), es gibt keinen Dateisystem-Index und
  keine falsch-positive Readiness bei defekter XTF/Indexerstellung.

### Gegenprobe 3 – Neustart-Determinismus und geänderte XTF (Exit 0)

- **Kommando/Exit:** `python3 "$EV/probe/restart_probe.py"` → **Exit 0** (`probe/restart-probe.log`).
- **Ergebnis:** Zwei Prozesse mit identischer XTF liefern identische IDs (14), identische Rangfolge
  `[wald, probe.b, probe.c, probe.d, probe.a.key, probe.z.syn, probe.h, probe.theme, probe.i]` und identische
  Facets. Nach Änderung eines Titels in einer Kopie der XTF ändert sich die Rangfolge
  `[wald, probe.c, probe.b, …]` – der neue Prozess hat den Index frisch aus der geänderten XTF gebaut, kein
  Stale-Index.
- **Schlussfolgerung:** Deterministischer Neuaufbau aus der XTF bei jedem Start ist real belegt.

## Pflichtprüfungen

Alle Kommandos im Review-Worktree `/Users/stefan/sources/kartenkatalog-review-P2`. `EV` =
`/Users/stefan/sources/kartenkatalog-P2-review-evidence/a9dde956b4937916cec862c6c672cf26dd30636f`.

| Testfamilie | Ausgeführter Befehl | Exit | Tests/Failures/Errors/Skips | Rohreport | SHA-bezogen |
|---|---|---:|---|---|---|
| JUnit/Quarkus + Build (frisch) | `./gradlew clean test quarkusBuild` | **0** | **55 / 0 / 0 / 0** (8 Testklassen) | `EV/logs/01-clean-test-quarkusBuild.log`, `EV/build-reports/junit/TEST-*.xml`, `EV/build-reports/junit-html/` | Ja, `a9dde956…` |
| Runtime-Dependency-Graph | `./gradlew dependencies --configuration runtimeClasspath` | **0** | nur `lucene-core:10.6.0`, `lucene-analysis-common:10.6.0`; kein Queryparser/Spring/JDBC/Postgres/Hikari/JTE | `EV/logs/02-dependencies-runtimeClasspath.log` | Ja, `a9dde956…` |
| Echter JVM + P0-HTTP + Health + Startfehler | `python3 scripts/p1-process-contract.py` | **0** | JVM ready 1.09 s; 8 Startfehler je Exit 1, nie ready | `EV/logs/03-p1-process.log`, `EV/build-reports/p1-raw/` | Ja, `a9dde956…` |
| Echter `quarkusDev` | `python3 scripts/p1-process-contract.py --dev` | **0** | Dev ready 4.777 s; P0-HTTP grün | `EV/logs/04-p1-process-dev.log`, `EV/build-reports/p1-raw/dev*.log` | Ja, `a9dde956…` |
| P2-RAM-Index/Neustart/read-only/Startfail | `python3 scripts/p2-process-contract.py` | **0** | Harvest: 886 Dokumente, ready 1.023 s, Index 334 ms, RSS 256 336 KiB, Median 16.759 ms; Fixture: 12 Dok., ready 0.691/0.694 s, Index 134/136 ms; zwei identische Starts; geänderte XTF ändert Ranking; Index-Startfehler Exit 1; keine Dateien im read-only cwd | `EV/logs/05-p2-process.log`, `EV/build-reports/p2/{harvest,first,second,changed,broken-index}.log`, `EV/build-reports/p2/summary.json` | Ja, `a9dde956…` |
| Eigener Java-Kern-Test | `javac … Probe.java`; `java … Probe <eigene XTF>` | **0** | **81 Checks**, keine Failures | `EV/probe/Probe.java`, `EV/probe/probe-run.log`, `EV/probe/probe-catalog.xml` | Ja, gegen Kandidatenklassen/JAR |
| Eigener Real-JVM-HTTP-/Heap-/Startfehler-Test | `python3 "$EV/probe/http_probe.py"` | **0** | Ranking/Cross-Field/Filter/Facets/400/404; `jcmd` ByteBuffersDirectory, kein FS-Dir; `lsof` keine Indexdateien; 2 Startfehler Exit 1 | `EV/probe/http-probe.log`, `EV/probe/summary-http.json`, `EV/probe/http-ok.log`, `EV/probe/http-*.log` | Ja, gegen Kandidaten-JAR |
| Eigener Neustart-/Änderungs-Test | `python3 "$EV/probe/restart_probe.py"` | **0** | 2× identische IDs/Ranking/Facets; geänderte XTF → andere Rangfolge | `EV/probe/restart-probe.log` | Ja, gegen Kandidaten-JAR |
| Git-/Fixture-Hygiene | `git rev-parse HEAD`, `git status --porcelain=v1`, `git diff --check`, `shasum -a 256` | **0** | HEAD = Kandidat; Status leer; Diff-Check sauber; Harvest-SHA `de40f025…` unverändert | `EV/logs/00-git-before.txt`, `EV/logs/99-git-after.txt` | Ja, `a9dde956…` |

- **Fällige, aber nicht ausgeführte Checks und genaue Ursache:** Keine P2-Pflichtprüfung wurde ausgelassen.
  Browser-/Playwright-Gates (T3) sind erst P3a/P3b, native/Linux-/Container-Gates (T4) erst P4; sie wurden
  **nicht** als P2-Lücke gewertet und nicht als Erfolg behauptet.
- **Kumulatives Phasengate:** nicht anwendbar (`PHASENGATE: nein`). Die P0-/P1-Verträge wurden dennoch frisch
  gegen den Kandidaten geprüft (Baseline-Tests und echter 886er-JVM-Prozess).
- **Prozesshygiene:** Die Skripte starten und beenden ihre eigenen JVMs; nach den Läufen waren keine
  `quarkus-run.jar`-/`quarkusDev`-Prozesse mehr vorhanden (`EV/logs/99-git-after.txt`). Der Gradle-Daemon wurde
  nach Abschluss gestoppt (`EV/logs/zz-gradle-stop.log`, Exit 0).

## Findings

- **FINDING-01:** **Keine offene Muss-Abweichung.** AC2-01 bis AC2-04 sind nach unabhängiger Quell-,
  Test-, HTTP-, Prozess-, Speicher- und Fehlerpfadprüfung erfüllt; alle Pflichtprüfungen und alle eigenen
  Gegenproben sind erfolgreich.

**Beobachtungen ohne Abnahmewirkung (kein Finding):**

- **OBS-01:** Ein Fehler beim Indexaufbau wird vom Parser-Catch als `Invalid catalog XTF: Cannot build catalog
  RAM index` verpackt (die Ursache steht vollständig im Log). Der Start bricht korrekt ab; die Meldung ist
  sprachlich etwas unscharf, aber kein Vertrags-/Verhaltensfehler.
- **OBS-02:** `CatalogReadiness` ruft `store.current().searchIndex().available()` ohne Null-Guard auf. Das ist
  nach erfolgreichem `@Startup` nicht erreichbar (Start bricht bei Fehlern ab); eine defensive Prüfung wäre
  trotzdem sinnvoll.
- **OBS-03:** Einzelzeichen-/Sonderzeichen-Queries liefern jetzt **400**, während die P1-Teilstringsuche
  derartige Eingaben noch als (leere) Suche verarbeitete. Das ist durch den P2-Plan („keine unbegrenzte Suche
  durch leeres oder 1-Zeichen-Fragment“) gedeckt, dokumentiert und bricht keinen P0-Baseline-Fall.
- **OBS-04:** `QueryExceptionMapper` übersetzt **jede** `IllegalArgumentException` in 400. Das ist für
  Parameterfehler gewollt und leakt keinen Stacktrace; theoretische Programmierfehler würden aber ebenfalls als
  400 erscheinen. Kein beobachteter Fehlfall.
- **OBS-05:** `GET /api/v1/facets` akzeptiert `sort` (gleiche Query-/Filterparameter wie `/ebenen`), verwendet
  ihn aber nicht. Additiv und ohne Auswirkung auf P2-Verträge; für P3b dokumentieren.

## Unabhängiges Urteil

**PASS**

- **Begründung:** Der Kandidat `a9dde956b4937916cec862c6c672cf26dd30636f` erfüllt AC2-01, AC2-02, AC2-03
  und AC2-04. Frischer Build/Testlauf **55/0/0/0**; der aufgelöste Runtime-Graph enthält exakt Lucene
  **10.6.0** (`core` + `analysis-common`), keinen Queryparser und keine Alt-Stack-Reste. Der Index wird
  nachweislich als `ByteBuffersDirectory` im Java-Heap mit `GermanAnalyzer` gebaut, enthält genau ein Dokument
  je fachlicher Ebene, persistiert keine Dateien, wird bei jedem Prozessstart deterministisch aus der XTF neu
  aufgebaut und liefert bei Ausfall 503/Readiness DOWN statt falsch-leerer Treffer. Die eigene 14-Ebenen-Fixture
  bestätigt Ranking-Tiers, Identifier-Tie-Break, Cross-Field-AND, Umlaut-/ß-/AV-/Substring-Verhalten sowie
  OR-innerhalb/AND-zwischen-Filter und dynamische Facet-Zahlen inklusive Selbstausschluss und Nullwerten über
  echtes HTTP im separaten JVM-Prozess. P0-/P1-Verträge und alle Pflichtprozesse sind frisch grün; keine
  offene Muss-Abweichung.
- **Nächster freigebbarer Lieferabschnitt:** **P3a** – erst nach diesem PASS. Der aktuelle Auftrag endet
  jedoch hier: **P3a wurde nicht begonnen**, und Browser-/Native-Prüfungen wurden nicht vorgezogen.
- **Geprüfter SHA:** `a9dde956b4937916cec862c6c672cf26dd30636f`

### Rohbelege (absolut)

- Review: `/Users/stefan/sources/kartenkatalog-P2-review-evidence/a9dde956b4937916cec862c6c672cf26dd30636f/review.md`
- Kandidatendiff: `…/candidate.diff`
- Build/Tests: `…/logs/01-clean-test-quarkusBuild.log`, `…/build-reports/junit/TEST-*.xml`,
  `…/build-reports/junit-html/`
- Dependency-Graph: `…/logs/02-dependencies-runtimeClasspath.log`
- Prozesse: `…/logs/03-p1-process.log`, `…/logs/04-p1-process-dev.log`, `…/logs/05-p2-process.log`,
  `…/build-reports/p1-raw/`, `…/build-reports/p2/`
- Eigene Gegenproben: `…/probe/Probe.java`, `…/probe/probe-catalog.xml`, `…/probe/probe-run.log`,
  `…/probe/http_probe.py`, `…/probe/http-probe.log`, `…/probe/summary-http.json`,
  `…/probe/restart_probe.py`, `…/probe/restart-probe.log`
- Identität: `…/identity/models-api.json`, `…/identity/session-model-metadata.txt`,
  `…/identity/provider-connection.txt`, `…/identity/connection-evidence.txt`,
  `…/identity/deepseek-pricing.html`, `…/identity/deepseek-pricing-excerpt.txt`,
  `…/identity/launch-and-auth.txt`
- Git: `…/logs/00-git-before.txt`, `…/logs/99-git-after.txt`
