# Codex-Startprompt – Kartenkatalog von Spring Boot auf Quarkus / Lucene / Qute migrieren

**Rolle:** Agent A / Implementierungsagent **Codex**.  
**Repository:** `https://github.com/edigonzales/kartenkatalog` (bestehendes Repository, kein neues Produktrepo).  
**Verbindlicher Plan:** `docs/quarkus-migration/EXECUTION_PLAN.md`.  
**Unabhängige Abnahme:** `docs/quarkus-migration/OPENCODE_REVIEW_PROMPT.md`, ausgeführt in **OpenCode 2.x GUI auf macOS** mit dem in der GUI konfigurierten **DeepSeek-API-Provider** und **DeepSeek 4.1 Flash 1017**.  
**Jetzt ausschließlich beginnen mit Lieferung `P0`.** Nicht eigenmächtig zu `P1` oder darüber hinaus springen.

## 1. Gesamtauftrag (zur Orientierung)

Refactoriere **in diesem Repository** den Kartenkatalog auf **Quarkus 3.40.1.1 LTS + JDK 21 + Gradle + XTF-only + in-memory Lucene 10.6.0 (`ByteBuffersDirectory`) + Qute/HTMX**. Entferne Spring Boot, JDBC/PostgreSQL und JTE vollständig aus der finalen Laufzeit. Erhalte die `/api/v1`-Verträge, damit das separate QWC2-Plugin weiterhin funktioniert. Gestalte die Startseite optisch und funktional analog zum Kartenkatalog von `sogis/datenportal-sodata`: aufklappbare Listenansicht, Suche, fachlich passende Filter, Filter-Chips, Responsive-Ansicht und Metadatendetails. Eine JVM- **und** native Quarkus-Distribution muss am Ende wirklich laufen.

**Index-Entscheidung:** Version **10.6.0** aller Lucene-Artefakte, nicht die ältere 10.3.2-Referenz aus `datenportal-sodata`; `ByteBuffersDirectory` im **Java-Heap**, `GermanAnalyzer`, `lucene-core` und `lucene-analysis-common`. Die **XTF liegt persistent**, der **Lucene-Index nicht**: keine `FSDirectory`/`MMapDirectory`, keine persistierten Indexdateien und kein schreibbares Index-Volume. Bei jedem Neustart aus XTF neu aufbauen; `lucene-queryparser` nur mit konkret belegtem Bedarf. Erforderliche JVM- und **echte GraalVM-Native-Tests** müssen den Indexaufbau und die Suche mit genau dieser Version zeigen. Keine stille Herabstufung auf 10.3.2 oder ein anderes Speicherformat bei Native-Schwierigkeiten.

Die verbindlichen Klassen-/Methodenverträge, API-Pfade, UI-Referenzen, Teststufen und **Akzeptanzkriterien** stehen vollständig in `EXECUTION_PLAN.md`. Dieser Startprompt ersetzt die Spezifikation nicht.

**Bestandswissen, vor dem Arbeiten verifizieren:** Es existieren bereits `XtfCatalogRepository`, `CatalogService`, `CatalogDtos`, `CatalogApiController`, das `xtf`-Profil und die Test-XTF mit 169 Themen/886 Kartenebenen. Keine unnötige Neubauarchitektur, keine Datenbank, kein separates neues Repository. Die XTF ist ein **unvollständiger Harvest**; synthetische OIDs/`harvest-*`-Themen nicht als echte SIMI-IDs ausgeben.

## 2. Unverrückbarer Agentenprozess

1. Implementiere nur den nächsten **freigegebenen** Lieferabschnitt (`P0`, `P1`, `P2`, `P3a`, `P3b`, `P4`, `P5`).
2. Führe **die fälligen** Tests und unabhängigen Prozess-/Browser-/Native-Nachweise **selbst** aus; notiere die echten Kommandos, Exitcodes, Testzahlen, Fehler und Skips.
3. Schreibe je Lieferung **einen kurzen** Implementierungsbericht unter `docs/quarkus-migration/implementation/<ID>.md`; committe **Code, Tests und Bericht lokal**.
4. Ermittle danach den **vollständigen `CANDIDATE_SHA`** und `BASE_SHA` und übergib diese beiden zusammen mit AC-IDs und `OPENCODE_REVIEW_PROMPT.md` **separat** an OpenCode.
5. **Einzig OpenCode + DeepSeek API/Flash 1017 in einer getrennten Sitzung und einem sauberen Review-Worktree** kann `PASS`, `FAIL` oder `BLOCKED` aussprechen. Es prüft **Tests UND Ziele** (Code, fachliches Verhalten, API, Security, UX, native Betrieb) und führt einen eigenen Gegenfall aus.
6. **Kein nächster Lieferabschnitt vor `PASS` des aktuellen Produktcommits.** `FAIL` ⇒ im gleichen Abschnitt nacharbeiten, neu committen, neuer unabhängiger Review. `BLOCKED` ⇒ die notwendige Voraussetzung beseitigen/Entscheidung einholen, **nicht** überspringen.
7. Wenn du OpenCode **GUI** nicht tatsächlich bedienen/aufrufen kannst, ist das normal: **stoppe nach Commit und liefere einen kopierbaren manuellen Handoff**; Status `AWAITING_REVIEW`. Behaupte keine Reviewausführung, keine DeepSeek-Konfiguration, keinen selbst vergebenen PASS. Verlange nicht unbegründet eine `DEEPSEEK_API_KEY`-Umgebungsvariable für Codex oder die Bedienung einer OpenCode-CLI; der Nutzer hat den Provider im GUI eingerichtet.
8. Kein Push, Merge oder Release ohne ausdrücklichen Auftrag. Keine Änderungen am unabhängigen Reviewbericht während der Prüfung. Ein Reviewbericht kann nach `PASS` separat als reiner Dokumentationscommit gespeichert werden; produktive Änderungen erfordern einen neuen Review.

Die einzige operative Statusübersicht ist `docs/quarkus-migration/STATUS.md`. Keine parallele Ticket-/Phasen-/Audit-Plattform erfinden.

## 3. Technischer Einstieg – jetzt durchführen

1. Prüfe `pwd`, `git status --short`, `git rev-parse HEAD`, `git branch --show-current`, `java -version`, `./gradlew --version`, vorhandene JDKs/Gradle-Tasks und GraalVM-Verfügbarkeit; dabei **keine** uncommitteten Nutzeränderungen verwerfen. Der bei der Spezerstellung untersuchte SHA `c273b6e9f42e6d04803965fe1e3b06e2ba823d34` ist nur historische Referenz – der aktuelle Checkout ist massgeblich.
2. Lies vollständig `docs/quarkus-migration/EXECUTION_PLAN.md`, dieses Dokument, `docs/quarkus-migration/OPENCODE_REVIEW_PROMPT.md` und vorhandenes `AGENTS.md` (falls vorhanden). **Nicht** die alten INTERLIS-G2-Phasen aus den angehängten Verfahrensbeispielen übernehmen.
3. Lies im Kartenkatalog nur die relevanten Bestandsdateien: `README.md`, `build.gradle`, `XtfCatalogRepository`, `CatalogService`, `CatalogDtos`, `CatalogApiController`, `CatalogText`, JTE-Seiten, `application-xtf.properties`, `data/README.md`, XTF-Vertragstests.
4. Für UI-Referenz gezielt `sogis/datenportal-sodata` inspizieren: `src/main/jte/pages/catalog.jte`, `components/{entryTable,searchForm,filterBar,resultsShell}.jte`, `src/main/resources/static/css/catalog.css`, `CatalogFiltersPlaywrightTest.java`. Nicht dessen Businessmodell und DuckDB-/Explore-Frontend kopieren.
5. Prüfe die Quarkus-3.40.1.1-Dokumentation, insbesondere Quarkus/Gradle, Qute `@CheckedTemplate` und Native Testing. Prüfe auch die Lucene-10.6.0-APIs für `ByteBuffersDirectory` und `GermanAnalyzer`. Quarkus-/Lucene-Versionsabweichungen oder ein anderer Indexspeicher sind **nur mit reproduzierbarer Evidenz und expliziter Nutzerfreigabe**, nicht zur Umgehung eines Native-Gates, zulässig.
6. Lege die benötigten Ordner `docs/quarkus-migration/implementation/`, `docs/quarkus-migration/reviews/` an und die **einzige** Fortschrittsdatei `STATUS.md`. Den letzten nachweislich bestätigten PASS-SHA nur aus einem echten unabhängigen Review übernehmen; initial `none`.

## 4. JETZT NUR `P0`: Baseline sichern und Migration nachvollziehbar abgrenzen

P0 hat **keine** Quarkus-Produktimplementierung. Das ist Absicht: Wir brauchen zunächst einen **nachprüfbaren Altvertrag**, an dem der Reviewer später erkennen kann, ob die Migration wirklich gleichwertig ist.

### P0.1 Bestands- und Laufzeitprüfung

- Aktuelles Repository verstehen, sowohl PostgreSQL-Standardpfad als auch vorhandenes `xtf`-Profil, aber **keine DB starten**, wenn der `xtf`-Modus es nicht benötigt.
- Führe möglichst frisch `./gradlew test` und die Anwendung über:

  ```bash
  ./gradlew bootRun --args='--spring.profiles.active=xtf --server.address=127.0.0.1 --server.port=8083'
  ```

  Starte HTTP-Smokes nur **gegen den tatsächlich laufenden Prozess**. Wenn das Projekt inzwischen anders konfiguriert ist, dokumentiere die realen Startbefehle.
- Beispiele für HTTP-Verträge: `GET /api/v1/catalog`, `/api/v1/themen`, `/api/v1/ebenen`, `/api/v1/ebenen?q=wasser`, `/api/v1/ebenen/ch.so.agi.gemeindegrenzen`, `/thema`, `/ebene/ch.so.agi.gemeindegrenzen`, unbekannte IDs `404`, QML vorhanden/nicht vorhanden.
- Die mitgelieferte Fixture ist für den Test auf **169 Themen und 886 Ebenen** zu prüfen; **kein** zukünftiger Produktions-Code darf diese Werte hart verdrahten.

### P0.2 API-Verträge sichern

- Erzeuge **kleine fokussierte** Contract-Fixtures/Tests unter `src/test/resources/contract/` und dazu passende JUnit-Vertragstests: JSON-Struktur von `Theme`, `Layer`, `Catalog`, `null` vs `[]`, `qwcLayerName`, Identifier, Beschreibung/HTML, 400/404, JSON-Fehlerstruktur und wesentliche HTML-Deep-Links.
- Verifiziere, welche Altverhalten als Soll-Vertrag sinnvoll sind und welche echten Bugs es gibt. Ein fehlerhaftes Altverhalten nicht als neue fachliche Norm ohne Begründung einfrieren; als `KNOWN_ISSUE` dokumentieren.
- Baue mindestens einen **Negativtest**, der beim absichtlichen Austausch eines öffentlichen DTO-Feldnamens oder einer 404-Antwort sicher fehlschlagen würde.

### P0.3 Migrationsinventur und Designreferenz

- In maximal 1–2 Seiten `docs/quarkus-migration/implementation/P0.md`: Matrix **KEEP**, **PORT**, **REMOVE** für vorhandene Quellen/Assets/Build/Tests. Vermerke die konkreten Quarkus-Zielkomponenten.
- Dokumentiere die visuelle UI-Referenz SODATA konkret: grosszügiger Header/Breadcrumb, Such-/Filterband, Trefferzählung, aufklappbare Listenzeilen, Mobile-Filter. Definiere Unterschiede aus fachlichen Gründen: Themen/Produkttyp/Organisation statt «Aktualisiert»; keine künstliche Sortierung nach Datum. Optional kleine Screenshots referenzieren, aber nicht massenhaft Bilddateien ins Git committen.
- Dokumentiere Quell-Fakten: unvollständige Harvest-XTF, leere `keywords/synonyms` im gelieferter Datei, teilweise fehlende `organisation`, künstliche `harvest-*`-Kennungen. Keine Daten erfinden, um neue Filter zu demonstrieren: Testfixtures separat.

### P0-Abnahme – alle Muss-Kriterien

Erfülle `AC0-01`, `AC0-02`, `AC0-03`, `AC0-04` aus `EXECUTION_PLAN.md`. Beachte insbesondere: Der unabhängige Reviewer soll **eigenständig** einen API-/Fehlervertrag prüfen, den bloße `200`-Tests nicht nachweisen.

### P0-Abgabe

1. `docs/quarkus-migration/implementation/P0.md` mit tatsächlich ausgeführten Befehlen/Exitcodes/Reports und allen ACs.
2. `git diff --check`; keine fremden uncommitteten Änderungen versehentlich committen.
3. Lokal committen. **Danach** `git rev-parse HEAD` lesen.
4. Ausgabe des manuellen Handoffs:

   ```text
   Repository: edigonzales/kartenkatalog
   Lieferung: P0
   BASE_SHA: <vollständiger SHA vor P0>
   CANDIDATE_SHA: <vollständiger SHA nach P0-Commit>
   Kriterien: AC0-01..AC0-04
   Implementierungsbericht: docs/quarkus-migration/implementation/P0.md
   Reviewprompt: docs/quarkus-migration/OPENCODE_REVIEW_PROMPT.md
   Phasengate: nein (Baseline-Pilot)
   Noch fehlende Pflichtprüfungen: <keine / exakt>
   Status: AWAITING_REVIEW
   ```

**Nach P0 HIER STOPPEN.** Nicht bereits Quarkus-Dateien erzeugen, bevor der unabhängige P0-Review tatsächlich PASS ist. Nach einem echten PASS folgt P1 gemäß `EXECUTION_PLAN.md`, dann die übrigen Abschnitte in vorgeschriebener Reihenfolge.

## 5. Fortsetzungsregeln nach P0

- **P1:** Quarkus 3.40.1.1 + XTF-only, sichere Parser- und Snapshot-Schicht, verhaltensgleiche API, minimale Qute-Seiten, Spring/JDBC-JTE-Laufzeit entfernen.
- **P2:** **Lucene 10.6.0** auf `ByteBuffersDirectory` (heapbasiert, kein persistierter Index), `GermanAnalyzer`, echter Indexaufbau aus XTF und deterministischer Rebuild bei Neustart; Such-Qualitätsassertions, Thema/Typ/Organisation-Facets und bestehende API.
- **P3a:** Qute-Seiten, SODATA-artige Liste/Akkordeon/Details und sichere Metadatenausgabe.
- **P3b:** HTMX, Desktop-/Mobile-Filter, Chips, URL-Back/Reload, echte Chromium-/Firefox-E2E-Tests; **kumulativer UI-Phasenreview**.
- **P4:** GraalVM Native + realer JVM-/Native-Prozess + OpenShift/CI/Health; **Lucene 10.6.0 und `ByteBuffersDirectory` müssen im Native-Prozess aus XTF indexieren und per HTTP suchen**, ohne schreibbares Lucene-Index-Volume; Pflicht-Native-Gate.
- **P5:** Restbereinigung, Betriebsdokumentation, alle Tests **frisch**, **unabhängige kumulative Gesamtfreigabe**.

Pro Abschnitt nur dessen vollständige Kriterien implementieren; technische Findings desselben Abschnitts nacharbeiten. SODATA-Code selektiv nutzen, Lizenzhinweise bei kopierten wesentlichen Teilen mitnehmen. Tests und Reviewer dürfen niemals auf grüne Erwartungen zurechtgebogen werden.

**Jetzt:** P0 ausführen und den unabhängigen OpenCode-Handoff liefern. Kein eigenes PASS vergeben.
