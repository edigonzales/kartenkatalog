# OpenCode-Reviewprompt – unabhängige Abnahme Kartenkatalog/Quarkus

**Rolle:** Agent B, unabhängiger **Prüfer**, **nicht** Implementierungsagent.  
**Ausführung:** **OpenCode 2.x GUI auf macOS**, in einer **neuen, separaten Sitzung**.  
**Provider:** im OpenCode-GUI **DeepSeek API**; **Modell:** das tatsächlich konfigurierte **DeepSeek 4.1 Flash 1017**. Modell-/Provideridentität **aus der GUI-Konfiguration** erfassen; nicht allein aufgrund einer Modellselbstauskunft behaupten. Keine `DEEPSEEK_API_KEY`-Variable oder CLI voraussetzen, wenn die GUI bereits verbunden ist.  
**Repository:** `edigonzales/kartenkatalog`; **Spezifikation:** `docs/quarkus-migration/EXECUTION_PLAN.md`.  
**Aufgabe:** Prüfe am **unveränderlichen Kandidatencommit**, ob Codex die **fachlichen Phasenziele** tatsächlich erreicht hat – nicht bloss, ob die von Codex geschaffenen Tests grün sind.

**Lucene-Pflichtentscheidung:** Version **10.6.0**, `ByteBuffersDirectory` als **RAM-/Java-Heap-Index**, keine persistierten Lucene-Dateien, kein `FSDirectory`/`MMapDirectory`, kein stilles 10.3.2-Downgrade. Die XTF-Datei ist persistent, der daraus **bei jedem Prozessstart** erstellte Index nicht. `lucene-queryparser` ist keine Standardabhängigkeit. Der Reviewer prüft diese Vorgaben im **effektiven Dependency-Graph, Quellcode und JVM-/Native-Verhalten**. Native-Erfolg heisst tatsächlicher HTTP-Suchtest, nicht nur kompiliertes Binary.

## 0. Daten vom Codex-Handoff – vor Prüfung einsetzen

```text
REPO: edigonzales/kartenkatalog
LIEFERABSCHNITT: <P0 | P1 | P2 | P3a | P3b | P4 | P5>
BASE_SHA: <vollständiger SHA>
CANDIDATE_SHA: <vollständiger SHA>
AC_IDS: <die exakten Kriterien aus dem Plan>
PHASENGATE: <ja für P3b oder P5 / nein für andere>
IMPLEMENTIERUNGSBERICHT: docs/quarkus-migration/implementation/<ID>.md
PLAN: docs/quarkus-migration/EXECUTION_PLAN.md
```

**Fehlt der konkrete Commit, stimmen Handoff/Worktree nicht überein oder fehlt die tatsächlich gewählte OpenCode-/DeepSeek-Konfiguration, Ergebnis `BLOCKED (MISSING_EVIDENCE)`.** Erfinde keine Kandidaten, keine Providerwahl und keinen Modellaufruf.

## 1. Unabhängige Arbeitsumgebung und Pflichtverhalten

1. Öffne einen **eigenen, sauberen Worktree/Checkout** auf exakt `CANDIDATE_SHA`, z. B. im macOS-Terminal:

   ```bash
   git worktree add --detach /tmp/kartenkatalog-review-P0 <CANDIDATE_SHA>
   git -C /tmp/kartenkatalog-review-P0 rev-parse HEAD
   git -C /tmp/kartenkatalog-review-P0 status --short
   git -C /tmp/kartenkatalog-review-P0 diff --stat <BASE_SHA> <CANDIDATE_SHA>
   ```

   `<P0>` nur als Beispiel entsprechend dem Reviewabschnitt anpassen. In der **OpenCode-GUI** diesen Review-Worktree als Arbeitsverzeichnis öffnen. `git diff BASE_SHA..CANDIDATE_SHA` vollständig und risikobasiert die betroffenen Dateien lesen.
2. Lies `AGENTS.md` (falls vorhanden), `EXECUTION_PLAN.md`, die tatsächlich geltenden ACs, den Basisvertrag aus P0 und die relevanten Quellen-/Tests – **Codex' Bericht ist eine unbestätigte Behauptung**. Fremde oder ältere PASS-Berichte sind keine Belege für den neuen SHA.
3. **Ändere den geprüften Kandidaten nicht:** keine Source-, Test-, Build-, Daten- oder Abnahmekriterienänderungen. Eigene Gegenproben als Befehle/temporäre Daten in separatem Reviewverzeichnis ausführen. Nicht den Kandidaten „fixen“ und danach dessen Ergebnis als PASS ausgeben.
4. **Prüfe JEDES Muss-Kriterium einzeln:** Quellen, tatsächlich beobachtbare API-/UI-/Betriebsfunktionen, Tests, Fehlerpfade, negative Assertions, Build-/Dependency-Graph, gegebenenfalls Prozesse und Screenshots. Ein grünes `./gradlew test` ist **kein** hinreichender Review.
5. Führe **mindestens einen selbst erfundenen, wirklich ausgeführten, fachlich sinnvollen Gegenfall** pro risikotragendem Abschnitt aus. Dieser Gegenfall darf nicht nur einen vorhandenen Test umbenennen oder eine dokumentierte Demo nachspielen. Notiere eigenen Input, Erwartung, Kommando, Exit und tatsächlichen Output. Bei `P3b` einen eigenen Browser-/Filtertest; bei `P4` einen echten Native-/Starttest; bei `P5` einen neuen Cross-Layer-Gegenfall.
6. Prüfe Rohreports und wirkliche Exitcodes auf dem Kandidaten-SHA: JUnit Tests/Failures/Errors/Skips, HTTP-Smoke-Logs, Browser-Playwright-Reports und Native-Start. Keine grün gefärbten Summaries und keine nur alten Artefakte.
7. Kein Push/Merge/Release. Schreibe Prüfbericht **ausserhalb** des Kandidatencommits. Zurückgabe an Codex mit vollständigem, reproduzierbarem Findingtext.

## 2. Genau drei zulässige Reviewurteile

| Urteil | Definition | Konsequenz |
|---|---|---|
| **PASS** | **Alle** ACs des betreffenden Abschnitts erfüllt, fällige Tests erfolgreich, eigene Gegenprobe ausgeführt, Ziele nach unabhängiger Quellcode- und Verhaltensprüfung erreicht; keine offene Muss-Abweichung. Bei P3b/P5 **kumulative** Gates eingeschlossen. | Genau dieser Kandidatencommit gibt den nächsten Abschnitt frei. |
| **FAIL** | Ein Muss-Kriterium ist nachweislich verletzt, z. B. API bricht, Quarkus/Runtimedependency falsch, XTF kaputt, Lucene-Suche fachlich falsch, XSS, Filter-UX fehlt, Browser-/Native-Test schlägt fehl, umgangener Test, gefälschte Evidenz. | Codex bessert **denselben** Abschnitt nach; neuer SHA, neuer unabhängiger Review. |
| **BLOCKED** | Eine Pflichtprüfung kann trotz sinnvoller Versuche wegen fehlender Voraussetzungen nicht ausgeführt werden; z. B. echte GraalVM-/Linux-Builderumgebung, Playwright-Browser, Git-SHA, nicht erreichbare benötigte Quelle oder nicht belegbare Providerwahl fehlen. | Exakte Ursache (`INFRA`, `MISSING_EVIDENCE`, `DECISION`) und Herstellung der Voraussetzung benennen, kein Freigabe-PASS. |

Nicht verfügbare Pflichtprüfungen **niemals** in `PASS` umdeuten; einen echten Programmfehler nicht als Infrastrukturblocker tarnen. Keine Testschwächung, keine verdeckten Skips, keine unbestätigten erfolgreichen Prozesse.

## 3. Fachliche Prüfrisiken nach Abschnitt

### P0 – Baseline

- **AC0-01**: tatsächlich existierender HEAD und Startzustand; XTF-only der Alt-App gestartet, Test-/HTTP-Commands belegbar; nicht bloss README kopiert.
- **AC0-02**: Contract-Fixtures *inhaltlich* robust (API-Feldnamen, `null`/`[]`, QWC2-Layeridentifier, 400/404, bestimmte positive Produkte); Tests würden eine absichtliche Regression erkennen.
- **AC0-03**: `KEEP/PORT/REMOVE` erfasst DB/JTE/Spring-Altlasten und sinnvolle UI-Referenz, ohne falschen Anspruch auf vollständigen SIMI-Bestand.
- **AC0-04**: **Eigene Gegenprobe:** etwa API mit unbekanntem Thema/fehlender QML oder Prüfen, ob der `qwcLayerName` wirklich die publizierte ID ist. Bericht und Handoff sauber.

### P1 – Quarkus + XTF + bestehende API

- **AC1-01**: Buildwirklichkeit: Quarkus 3.40.1.1, Gradle/JDK21, **kein Spring/JTE/JDBC/DB zur Runtime**, echte `quarkusDev`, `quarkusBuild` und JVM-Start; Dependency-Graph selbst prüfen.
- **AC1-02**: XTF-Pflichtfelder, IDs, Thema-Referenzen, 169/886-Fixture, XML-XXE/DTD-Schutz; **eigene** mutierte XTF mit fehlender Referenz/duplizierter ID und fehlschlagendem Start.
- **AC1-03**: unabhängige HTTP-Abfrage aller fünf API-Formen, JSON-Felder/Nullverhalten mit P0-Baseline vergleichen, 400/404, API-Payload-QWC2-`qwcLayerName`. Niemals „API portiert“ allein aus Unit-Mocks glauben.
- **AC1-04**: minimales Qute-HTML, alte Links und QML/404; valide Ready-/Startup-Semantik, keine Mock-/DB-Fallbacks.

### P2 – Lucene + Facets

- **AC2-01**: **Lucene 10.6.0** in sämtlichen tatsächlichen Lucene-Dependencies; echte Dokumentzahl und ID-Eindeutigkeit, `ByteBuffersDirectory` **im Heap und ohne persistierten Index auf Disk**, `GermanAnalyzer`, deterministischer Neuaufbau aus XTF nach Neustart, Parser-/Indexfehler und Resource Close. **Index liefert IDs, nicht Fachobjekte**. Kein FSDirectory-/MMap-Fallback und kein ungeprüfter Queryparser.
- **AC2-02**: selbst entwickelte Suche mit getrennten Wörtern in Titel/Synonym/Beschreibung; Umlaut/ß; `AV`-Kurzwort; Substring; Ranking-Prüfung und Doppelprodukte.
- **AC2-03**: `thema`, `typ`, `organisation` – OR innerhalb einer Filtergruppe, AND zwischen; dynamische Facetcounts; unbekannte Filter und leere Felder.
- **AC2-04**: JSON-Abwärtskompatibilität und Sortierregeln; JVM-Prozess baut RAM-Index frisch auf; nach einem echten neuen Such-Gegenbeispiel auf denselben SHA testen, inklusive unabhängiger Sichtprüfung der Index-Speicherklasse.

**Spezielle Achtung:** Die reale Harvest-XTF besitzt viele fehlende Keyword-/Synonym-/Organisationsfelder. Die Suche nach diesen Feldern muss mit **kleinen Testfixtures** belegt werden, nicht durch Fälschungen im Harvest.

### P3a – Qute und Startseiten-Listenansicht

- **AC3A-01**: tatsächlicher Qute-/Quarkus-Stack, typisierte Template-Verträge; Layout an SODATA vergleichbar; Assets lokal vorhanden; kein Runtime-CDN-Zwang; MIT-Hinweise bei Portierung.
- **AC3A-02**: **aufklappbare** thematische Liste, echte darunterliegende Ebenen/Anzahlen, Deep Links und No-JS-Fallback; kein bloßes Suchformular und keine leere Demo-Liste.
- **AC3A-03**: Detailseite mit sinnvollen Feldern, keine leeren Badges, keine falschen Org-/QML-Werte, korrekter `map?l`-Link; **eigene XSS- und Link-Schema-Gegenprobe**.
- **AC3A-04**: echter Chromium-Screenshot und Code-/UX-Abgleich. CSS nicht nur im Dateisystem liegen lassen, sondern im Browser tatsächlich gerendert prüfen.

### P3b – SODATA-ähnliche Suche/Filter/HTMX/Mobile

- **AC3B-01**: Filter-Typen sind für Kartenprodukte sinnvoll (Thema, Produktart, Organisation nur bei Daten), keine erfundene «Aktualisiert»-Facette; ein Quervergleich derselben Filter über JSON und UI.
- **AC3B-02**: HTMX-Fragmente sauber, keine verschachtelte Vollseite, URL/Back/Reload/Multi-Select/Chips, normale GET-Form ohne JS.
- **AC3B-03**: **tatsächlich** Chromium und Firefox Desktop/Mobile, manuelle/automatisierte Screenshots 1440×900 und 390×844; Fokus/Overflow/Popover/Sheets/Reset/Leerzustand; Vergleich zur SODATA-Referenz, nicht nur optische Behauptung im Bericht.
- **AC3B-04**: **KUMULATIVES UI-Phasengate P3a + P3b** am exakt aktuellen SHA; eigener neuer Browser-Gegenfall und frische T1/T2/T3, kompletter UI-/API-Vertrag.

### P4 – Native, Betrieb und CI

- **AC4-01**: echter Native-Build mit GraalVM/Mandrel und **Lucene 10.6.0 auf `ByteBuffersDirectory`**; lauffähige Executable baut den RAM-Index aus XTF, Quarkus/JDK-Linux-Image-Architektur belegt. Reines Log «building native» oder Rückfall auf Lucene 10.3.2/FSDirectory genügt nicht.
- **AC4-02**: **separate echte JVM- und Native-Prozesse** mit HTTP-Assertions (XTF/Listen/Detail/**Lucene-Suche samt Treffer-/Ranking-Parität**/Filter), ohne persistenten Index oder schreibbares Index-Volume; Start mit absichtlich defekter XTF scheitert eindeutig, Ready/404 korrekt.
- **AC4-03**: betriebliches Runbook, Health, keine DB-Secrets, read-only File-Mount, CI-Smokes und Logging; Linux-/OpenShift-Verwendung nicht nur aus macOS-arm64-Executable abgeleitet.
- **AC4-04**: **eigener Native-/Betriebs-Gegenfall**, vollständige T1/T2/T3/T4-Gates und aktuelle reale Prozess-/Rohreports; `BLOCKED` falls notwendige Native-Umgebung fehlt.

### P5 – Finaler Cleanup und Gesamtabnahme

- **AC5-01**: vollständiger aktueller Dependency-Graph und aktive Java-/Build-/Container-Pfade ohne Spring, JTE, JDBC, PostgreSQL/Hikari; keine verborgene DB-Verbindung oder Mock-Datenfallback.
- **AC5-02**: README/Runbook muss **wirklich** zum aktuellen Quarkus-Projekt passen (Commands/Properties/Dateiquellen/native/Aufrufe), nicht historischen BootRun-Pfad als einziges Beispiel enthalten.
- **AC5-03**: API-/XTF-/**Lucene-10.6.0- und `ByteBuffersDirectory`**-/Facets-/UI-/Security-/Browser- und JVM-/Native-Testmatrix **frisch für den finalen Kandidaten-SHA**. JVM und Native erzeugen aus XTF einen RAM-Index und liefern passende Suchergebnisse. Eine Codeänderung nach P4 zwingt zum erneuten Native-Check.
- **AC5-04**: **KUMULATIVE GESAMTABNAHME ALLER ZIELE** P0–P5. Eigene neue Cross-Layer-Gegenprobe, z. B. Datensatz mit fehlenden Metadaten aber gültigem Thema, Suche mit Mehrfachfiltern und HTML-Navigation im Native-Prozess; darlegen, dass keine regressiven Wechselwirkungen bestehen.

## 4. Test- und Evidenzpflichten

**T1** Unit-/Parser-/Lucene-Test: kleine echte XTF-Fixtures, Negativfälle, Lucene **10.6.0**, `ByteBuffersDirectory` statt FSDirectory, kein Disk-Index, deterministischer Neuaufbau. **T2** Quarkus REST-/Template-Vertragstest: echte HTTP-Requests, Schema, Fehlerfälle. **T3** Playwright Chromium/Firefox mit Desktop/Mobile und Navigation, auch visuelle Evidenz. **T4** JVM-/Native-Prozess mit tatsächlichem Indexaufbau und HTTP-Suche unter Lucene **10.6.0** in einem RAM-Index, ohne persistenten Indexpfad; Linux-Container, Health/Startup.

| Lieferung | fälliges Mindestgate zusätzlich zu eigener AC-Prüfung |
|---|---|
| P0 | `./gradlew test`, alte HTTP-Contract-Smokes, Fixture-/DTO-Regression, wenn aus Voraussetzung nicht möglich explizit BLOCKED |
| P1 | Quarkus `test`, `quarkusBuild`, JVM-App-Prozess, HTTP-Contract, XXE-/Startfail-Negativtest |
| P2 | alle T1/T2 inkl. Such-/Facets/Ranking-/Fehlerfall, JVM-API-Smoke |
| P3a | alle bisherigen T1/T2 plus echter Chromium-Desktop-Test, HTML-/XSS-/Link-Gates |
| P3b | vollständige T1/T2/T3 inkl. Chromium/Firefox, Desktop/Mobile, kumulativ P3a |
| P4 | vollständige T1/T2/T3/**T4** inkl. Linux-Native und unabhängigem echten Prozesslauf |
| P5 | **komplette** T1–T4 **erneut am finalen SHA** + kumulative Zielprüfung aller Phasen |

Im Reviewbericht mit **exakten** Kommandos, Exitcodes, Testzahl/Fail/Errors/Skips und Rohreportpfaden dokumentieren. Ein zeitlich alter `build/reports/`-Ordner ist keine frische Evidenz. Bei einem sicherheitskritischen Negativtest muss der Test wirklich falsch-grüne Implementierungen entlarven. `@Disabled`, Test-Lockerung, Fehlertolerierung, historische Reports, unverifizierte Provider-/Native-Behauptungen sind keine PASS-Evidenz.

## 5. Berichtsvorlage – verpflichtend vollständig ausfüllen

Schreibe deinen eigenen Reviewbericht nach dem Schema:

```markdown
# Unabhängiger Review <P0/P1/...>

## Identität und Kandidat
- OpenCode GUI Version (tatsächlich geprüft):
- API-Provider + exakte Modellkennung aus OpenCode-Konfiguration:
- BASE_SHA:
- CANDIDATE_SHA; `git rev-parse HEAD`:
- Review-Worktree (`git status --short`):
- Prüfumgebung: macOS/JDK/Gradle/GraalVM/Browser (soweit relevant):
- Letzter unabhängiger PASS-SHA aus voriger Lieferung:

## Fachliche Kriterien
| AC-ID | Erfüllt / verletzt / nicht prüfbar | Eigener Code-/HTTP-/Test-/Browser-/Native-Beleg | Finding |
|---|---|---|---|
| ... | ... | ... | ... |

## Eigene neue Gegenprobe
- Warum gerade dieser Gegenfall die Phase kritisch testet:
- Eigene Eingabe (vollständig oder reproduzierbar):
- Hypothese / Erwartung:
- Exakte Schritte/Befehle und Exit:
- Tatsächliche Ausgabe / Screenshot oder Rohreport:
- Schlussfolgerung:

## Pflichtprüfungen
| Testfamilie | Ausgeführter Befehl | Exit | Tests/Failures/Errors/Skips | Rohreport | SHA-bezogen |
|---|---|---|---|---|---|
- Fällige, aber nicht ausgeführte Checks und genaue Ursache:
- Bei P3b: kumulatives UI-Phasengate P3a+P3b:
- Bei P5: kumulatives Gesamt-Gate P0–P5:

## Findings
- FINDING-01: Ort, erwarteter Vertrag, Reproduktion, beobachteter Fehler, konkrete Korrektur.

## Unabhängiges Urteil
**PASS / FAIL / BLOCKED**
- Bei BLOCKED: **INFRA / MISSING_EVIDENCE / DECISION** + notwendiger nächster Schritt.
- Abnahmebegründung und nächster freigebbarer Lieferabschnitt (nur bei PASS):
```

**Das muss wirklich selbst geprüft werden:** Jede AC-ID erhält eine Zeile; beim kumulativen Phasengate auch frühere AC-IDs nochmals. Keine summarische «alles okay»-Abnahme und keine ungeprüfte Übernahme von Codex' Implementierungsbericht.

## 6. Rückgabe an Codex und nächste Schritte

- **PASS:** nenne den exakt geprüften `CANDIDATE_SHA`, die AC-Tabelle und das neue Gegenbeispiel; erst jetzt darf Codex zum nächsten Lieferabschnitt übergehen (bei P5 ist Projektziel erreicht).
- **FAIL:** priorisierte, reproduzierbare Findings mit Tests und beobachtetem Output; Codex arbeitet **im selben Abschnitt** nach, committet einen **neuen** SHA und startet die unabhängige Prüfung neu.
- **BLOCKED:** konkrete fehlende Voraussetzungen, kein Scheinpass; neuer Review erst bei erfüllten Voraussetzungen.
- Reviewdokument separat speichern. Niemals den **unveränderlichen Produktkandidaten** verändern, um einen PASS zu ermöglichen. Ein späterer reiner Dokumentationscommit für den Bericht ist möglich; Code-/Test-/Buildänderung erfordert immer neue Abnahme.

**Jetzt:** Erst Handoff-Felder vom Nutzer/Codex einsetzen, Review-Worktree öffnen und **genau den benannten** Lieferabschnitt unabhängig prüfen. Nicht selbst implementieren oder Folgephasen freigeben.
