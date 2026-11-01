# Unabhängiger OpenCode-Review – Cleanup des Quarkus-Kartenkatalogs

**Rolle:** Agent B / unabhängiger Prüfer, **nicht** Implementierungsagent.  
**Umgebung:** OpenCode **2.x GUI auf macOS**, direkt verbundene **DeepSeek API**.  
**Gewünschtes Modell:** **DeepSeek 4.1 Flash 1017** (das im GUI tatsächlich konfigurierte Provider-/Modellpaar im Bericht festhalten).  
**Repository:** `edigonzales/kartenkatalog`.  
**Verbindlicher Plan:** `docs/cleanup/EXECUTION_PLAN.md`.

Deine Aufgabe ist ausdrücklich die **unabhängige Überprüfung der Ziele und Testwirksamkeit**, **nicht** die bloße Wiederholung grüner Testbefehle und **nicht** die Fortsetzung der Implementierung. Der umzusetzende Code ist auf einem konkreten Kandidatencommit eingefroren. Ein Agentenbericht ist eine Behauptung, niemals selbst der Beweis.

## 0. Handoff-Daten – vom Nutzer/Codex ausfüllen

```text
REPO: edigonzales/kartenkatalog
LIEFERUNG: <C0 | C1 | C2 | C3 | C4>
BASE_SHA: <vollständiger 40-Zeichen-SHA>
CANDIDATE_SHA: <vollständiger 40-Zeichen-SHA>
AC_IDS: <exakte Muss-Kriterien dieser Lieferung>
PHASENGATE: <ja nur bei C4; sonst nein>
IMPLEMENTIERUNGSBERICHT: docs/cleanup/implementation/C<N>.md
PLAN: docs/cleanup/EXECUTION_PLAN.md
LETZTER UNABHÄNGIGER CLEANUP-PASS: <SHA / none>
```

**Ohne eindeutige reale SHAs, richtige Lieferung, zugehörige Spezifikation oder überprüfbare Provider-/Modellidentität lautet das Ergebnis `BLOCKED (MISSING_EVIDENCE)`.** Keine positive Annahme aus einem bloßen Dateinamen, Chattext, einem im Bericht zitierten `PASS` oder der Selbstbeschreibung des Modells ableiten.

## 1. Unabhängigkeit und sichere Arbeitsumgebung

1. Prüfe `BASE_SHA`, `CANDIDATE_SHA`, `git cat-file -t`, `git rev-parse HEAD`, `git status --porcelain=v1`, `git diff --stat BASE_SHA..CANDIDATE_SHA` und lies den gesamten **relevanten Diff**. Für den Review einen **eigenen** Worktree auf `CANDIDATE_SHA` verwenden, z. B.:

   ```bash
   REVIEW_DIR="$(mktemp -d /tmp/kartenkatalog-cleanup-review-XXXXXX)"
   git worktree add --detach "$REVIEW_DIR" <CANDIDATE_SHA>
   git -C "$REVIEW_DIR" rev-parse HEAD
   git -C "$REVIEW_DIR" status --porcelain=v1
   git -C "$REVIEW_DIR" diff --check <BASE_SHA> <CANDIDATE_SHA>
   ```

2. Öffne diesen Worktree in der OpenCode-GUI. Alle **getrackten** Dateien bleiben beim Review unverändert. Eigene Counterprobes unter `/tmp` oder in ignorierten `build/reports/cleanup-review/` schreiben. Keine Änderungen an Produktquellen/Tests/Build/ACs des Kandidaten und **kein** Commit, Push, Merge, Tag oder Release.
3. Lies `AGENTS.md` (falls vorhanden), `EXECUTION_PLAN.md`, die für den Abschnitt geltenden ACs, den vorherigen tatsächlichen Cleanup-PASS, die betroffenen Produktionsdateien/CI-/Testscripts und die historischen Ausgangsbelege selbst. Code und Referenzen **selbst** untersuchen. Den Implementierungsbericht niemals als Beweis für real ausgeführte Tests annehmen.
4. Prüfe sämtliche Pflichtkriterien **einzeln** anhand exakter Codestellen, eigenständig ausgeführter Tests/negativer Gegenproben, Rohreportzahlen und SHA-Bezug. Jede Muss-AC benötigt eine Tabellenzeile `erfüllt / verletzt / nicht prüfbar`.
5. **Mindestens eine eigene neue, wirklich ausgeführte Gegenprobe pro Lieferung.** Bei C4 mindestens **zwei**, darunter eine Cross-Layer-/Native-Probe und eine CI-/Testharness-/Pfadprobe. Sie darf nicht nur einen vom Implementierungsagenten schon benannten Test erneut ausführen. Eine nur vorgeschlagene Probe ist kein Nachweis.
6. Wenn ein Check **tatsächlich scheitert**, ist das `FAIL` (mit Kommando, Exitcode, Output, Erwartung). Wenn ein Pflichtcheck wegen fehlender Hardware/Umgebung/Netzzugang nicht ausführbar ist, ist das `BLOCKED (INFRA)` – nicht `PASS`. Fehlt eine verbindliche fachliche Nutzerentscheidung, `BLOCKED (SCOPE_DECISION)`. Tests dürfen nicht nachträglich als optional erklärt werden.
7. Insbesondere **keine** Action auslösen, die einen Main-Push oder Docker-Hub-Push bewirkt. Die bestehende CI publiziert auf `main` nach grünen Gates acht Tags und spiegelt bei Push nach Codeberg. Teste Trigger/Gating in isolierten Checks, ggf. auf einem nach Nutzerfreigabe erstellten **nicht veröffentlichenden** PR-/Branch-CI-Lauf, aber nie implizit per Publish.
8. Nach dem Review einen eigenständigen Bericht schreiben **ausserhalb** des Produktcommits, der unveränderte Kandidat muss nach allen Tests weiterhin denselben HEAD und einen sauberen tracked Tree haben. Bei `FAIL` nur Findings an Codex zurückgeben; selbst keine Korrekturen am Kandidaten einspielen.

## 2. Genau drei zulässige Urteile

| Urteil | Vergaberegel und Folge |
|---|---|
| **`PASS`** | Alle Muss-ACs des Lieferabschnitts anhand **eigener** Belege erfüllt; alle fälligen Tests und Gegenproben ausgeführt und erfolgreich; keine offen gebliebene Muss-Abweichung. Bei C4 gilt dies **für sämtliche 31 ACs** und T1–T5 auf dem aktuellen Kandidaten-SHA. |
| **`FAIL`** | Mindestens eine Muss-AC nachweislich verletzt: aktive Consumer übersehen, Fixture beschädigt, Test aus CI verschwunden, HTML-/API-Bruch, Native-Regression, irreführende Provenienz, falsche Publish-Gates, Schein-PASS usw. Mit minimalem reproduzierbarem Finding. |
| **`BLOCKED`** | Relevante Pflichtprüfung objektiv nicht möglich oder erforderliche Entscheidung/Evidenz fehlt. Konkreten Grund `INFRA`, `MISSING_EVIDENCE` oder `SCOPE_DECISION` und einen ausführbaren Weg zur Entblockung nennen. Kein nächster Abschnitt vor PASS. |

**Ebenfalls wichtig:** Ein älterer P5-Review-/Release-`PASS` bezieht sich auf eine **andere** Revision. Er kann die Baseline illustrieren, ersetzt aber keine aktuelle Prüfung. Eine unvollständige Harvest-Datei darf niemals als vollwertiger SIMI-Export freigegeben werden.

## 3. Eigene technische Prüfungen nach Lieferung

### C0 – Tatsächliche Inventur statt angenommener Altlasten

**Zu prüfen: AC0-01 bis AC0-05.**

- Stimmen `git ls-files` und die Inventur tatsächlich überein? Sind alle aktiven Python-Scripts, Java-Klassen, Templates, CI-/Docker-/Deployment-Verbraucher berücksichtigt? Auch die Scripte, die andere Scripte über `subprocess` aufrufen?
- Sind die drei Root-Artefakte `kartenkatalog.duckdb.zip`, `kartenkatalog.xtf.zip`, `mockup_C_tree_clean.png` vorhanden? Sind Inhalte/Verwendungen überprüft, nicht bloss auf Grund von Dateinamen als redundant bezeichnet? Wird die echte aktive `data/kartenkatalog-harvest-ohne-qml.xtf` separat behandelt?
- Zeigt ein unabhängiger Hashcheck den Ausgangsstand der XTF und des API-Golden-Contracts? Sind 169/886 ausdrücklich nur als Fixturewerte bezeichnet?
- Ist `P3ITTest` nach realem Dateibaum tatsächlich abwesend? Sind derzeitige aktive Phasenbenennungen, `Baseline*`-Tests und `src/test/resources/final/` korrekt dokumentiert?
- Sind die tatsächlichen Quarkus-/JVM-/Browser-/Prozess-/Release-Testgates frisch erfasst? Wurde kein Main-Push erzeugt?
- **Eigene neue Probe:** Wähle eine übersehbar scheinende Referenz (z. B. `scripts/p1-process-contract.py` → `p0-http-contract.py` oder `p4-distribution-contract.py` → Cross-Layer-Fixture) und prüfe, ob die komplette Kette im Inventar existiert; alternativ ein positives/negatives `git check-ignore`-Beispiel.

**C0 ist ein Baseline-Pilot:** Native-Gesamtgate ist noch nicht obligatorisch, falsche Behauptungen über Native-Erfolg sind dennoch ein Finding. Der Bericht darf keine späteren Löschungen als bereits ausgeführt darstellen.

### C1 – Artefakte entfernen, ohne den echten Katalog zu zerstören

**Zu prüfen: AC1-01 bis AC1-05.**

- Sind **alle drei** Altartefakte aus dem **aktiven Git-Tree** verschwunden? Sind noch aktive Aufrufer vorhanden? Wurden keine Quellen in eine neue `old/`-Schattenablage kopiert? Keine history rewrite?
- Liegt `data/kartenkatalog-harvest-ohne-qml.xtf` weiterhin unverändert vor und ist via Default-Konfiguration geladen? Prüfe Hash und Objektzahlen an **realem** API/Quarkus-Startup.
- Ist `.gitignore` projektspezifisch? `git check-ignore -v --no-index` für `.env`, `build/`, `.gradle/`, neue XTF, erlaubte Fixture, Java-/Configquellen, Gradle-Wrapper. Ein einzelnes `grep` auf den Namen reicht nicht.
- `.dockerignore` sollte weiter die Quarkus-JVM-/Native-Artefakte einschliessen. Prüfe bei Bedarf die tatsächliche Docker-Build-Context-Wirkung, nicht den Inhalt allein.
- **Eigene Gegenprobe:** Lege in `/tmp` oder im separaten Worktree eine untracked `data/anderes.xtf` und eine bewusst erlaubte Test-XTF an; überprüfe das unterschiedliche Ignore-Verhalten (ohne die echte getrackte Fixture zu ersetzen). Oder zeige einen versteckten aktiven Consumer einer gelöschten Datei als `FAIL`.
- JVM/HTTP-Frischlauf und negativen XML-/Source-Test prüfen, kein Abrutschen auf Mock-/DB-Daten.

### C2 – Rename der Pflicht-Harnesses und Tests: Parität, nicht Kosmetik

**Zu prüfen: AC2-01 bis AC2-07.**

- Alle vier fachlichen Scriptnamen und alle **aktiven** Referenzen prüfen, besonders subprocess-Aufruf `runtime-process-contract.py` → `api-http-contract.py` sowie `distribution-contract.py` → HTTP-Contract + Integrationstest-Fixture.
- Stimmt der Interpreteraufruf mit dem tatsächlich laufenden Python überein und bleiben Parameter/Exit-/Fehlermeldungen unverändert? Kein `-O`, keine verlorenen `assert`, kein `except: pass` für Fehler, keine ausgeschlossenen Negativfälle.
- Haben die umbenannten JUnit-/QML-Testklassen mit neuen Namen **dieselben** Testmethoden, Assertions und Profile wie vorher? Im Test-XML die reale Suite-/Klassenidentität und Testzahl prüfen; nicht allein `./gradlew test` Exit 0.
- Sind `contract/public-api.json` und `integration/catalog.xml` gegenüber den alten Dateien bytegleich? Funktioniert der Cross-Layer-Fall in echten JVM-/Native-Contracts? `MockCatalogRepository` muss als echtes Testdouble erhalten bleiben.
- Browser: Chromium und Firefox, Desktop und Mobil – gleiche Szenarien, echte Screenshots nun unter `build/reports/browser/catalog` und `browser/filters`. Keine Tests verloren oder durch Pattern/Tag aus `playwrightTest` ausgeschlossen.
- CI: Prüfe `.github/workflows/main.yml` vor/nach semantisch (JVM `needs`, Browser/Prozess/Dev-/Native-/Docker-Pull, acht Tags, Main-only publish), die geänderten Script- und Reportpfade, den Mirror-Workflow und den Dependencygraph. **Kein** echter Push/Release.
- **Eigene negative Canary:** In einer **temporären** Kopie der CI-Definition einen neuen Scriptpfad absichtlich verstümmeln oder im Proxy eines HTTP-Contracts `qwcLayerName`/404 manipulieren. Zeige, dass der Prüfprozess den Fehler erkennt. Ein Probe-Script, das trotz falschem Aufruf nicht scheitert, ist ein `FAIL`.
- Bei Process-/Browser-Gates stdout, Exitcode und tatsächliche `Tests/Failures/Errors/Skipped` protokollieren. Keine reine Prüfung des Scriptstrings.

### C3 – Korrigierte Provenienz und technische Restaltlasten

**Zu prüfen: AC3-01 bis AC3-06.**

- Ist der doppelte XTF-Default tatsächlich auf eine Quelle reduziert, und löst Quarkus die Property bei JVM, Test und (wo fällig) Native weiterhin korrekt auf? `file:`/`classpath:` und `CATALOG_XTF_LOCATION` prüfen. Ungültige/unbekannte Quellen verhindern weiterhin einen erfolgreichen Start.
- Wird der harte Harvest-Hinweis **nicht mehr** auf jeder Qute-Seite pauschal angezeigt? Wie wird die optionale `catalog.provenance-notice`/`CATALOG_PROVENANCE_NOTICE` angebunden? Ist die Vorgabe **leer**, und wird die Angabe **ohne `RawString`** escaped? Ist sie auf Start-/Themen-/Detailseiten konsistent?
- Wird keine Vollständigkeit allein aus dem Dateipfad erraten? Auf einer **anderen** lokalen XTF (kleine Such-/QML-Fixture) darf bei leerer Notice keine sachlich falsche Harvest-Aussage erscheinen. `data/README.md` bleibt bezüglich seiner Fixture korrekt.
- Hat der Agent weitere Produktivklassen gelöscht/zusammengelegt? Für **jede** solche Änderung unabhängigen Consumer-/Test-/Native- oder DI-Nachweis verlangen. `CatalogStore`/`CatalogRepository`/`CatalogSnapshot` sind nicht allein deshalb redundant, weil sie delegieren.
- **Eigene neue XSS-/Provenienzprobe:** Starte eine echte JVM-App mit einer XTF-Testquelle und absichtlich gefährlichem Text in `CATALOG_PROVENANCE_NOTICE`, etwa `<img src=x onerror=alert(1)>`. Die Ausgabe muss sichtbaren escaped Text bzw. sichere Kodierung, aber **keinen ausführbaren HTML-Knoten** enthalten. Dieselbe API muss unverändert und die falsche Default-Warnung absent sein.
- Prüfe Quarkus-`@CheckedTemplate`-Build und `LuceneNativeConfiguration` unverändert, HTML/HTMX-/CORS-/API-Verträge, graziöse negative Starts, echte Browser-Ergebnisse.

### C4 – Dokumentation, Archividentität, kumulatives Produktgate

**Zu prüfen: AC4-01 bis AC4-08 und **alle AC0–AC3**.**

1. **Ist-Doku prüfen:** `README.md`, `docs/architecture.md`, `docs/operations.md`, `docs/testing.md`, `data/README.md`, Contract-README mit aktueller Anwendung und tatsächlichem Code/CI vergleichen. Keine historisch noch im Einsatz stehenden `p0-*`-/`P3ITTest`-Anweisungen in aktiven Bereichen. HTTP-/Quarkus-/QWC2-/GraalVM-/Docker-Befehle wirklich reproduzierbar.
2. **Archiv-Herkunft beweisen:** `docs/history/quarkus-migration/**` vs `BASE_SHA:docs/quarkus-migration/**` per `git rev-parse <SHA>:<pfad>` oder Hash identischer Git-Blobs prüfen; sowohl `implementation/`, `reviews/`, `reference/` als auch Prompts, altes `STATUS.md` und die alte `OPERATIONS.md`. `docs/history/legacy-notes.md` mit vorherigem Inhalt vergleichen. Die Reviewberichte inklusive früherem `FAIL` dürfen **nicht** umgeschrieben sein. Ein `git mv` kann als Rename erscheinen; **Inhalt** muss gleich bleiben.
3. **Live-Links prüfen:** neuen aktiven Linkgraphen (README → Architektur/Operations/Testing/History; Test-README → neuer JSON-/Scriptname; Ops → CI-/Deployment-/Scriptpfade; historische externe Evidenz) vor allem nach den Verzeichnisverschiebungen kontrollieren. Ein bewusst defekter aktiver Link in einer temporären Kopie soll von der Referenzprüfung erkannt werden.
4. **Klassennamen/Abhängigkeiten:** `git ls-files` und `git grep` in `src`, `scripts`, `.github`, `deploy`, `build.gradle`, aktive `README.md` und aktive `docs/*.md` – **nicht** historisches Archiv/Plan – sollen keine P0/P1/P2/P3a/P3b/P4/P5-/Baseline-Überbleibsel enthalten. Prüfe echte Runtime-Abhängigkeiten: keine Spring-/JTE-/JDBC-/PostgreSQL-/DuckDB-Klassen oder DB-Secrets, Lucene10.6.0 im RAM, Quarkus3.40.1.1.
5. **JVM/HTTP:** Vollständige Testmatrix auf exaktem C4-Candidate-SHA frisch mit Kommandos, Roh-JUnit/XML, Status/Fehler/Skips, API-DTO-Feld-/Null-/Problem-JSON-, XTF-, Index-/Facet-/Ranking-/Security-/Qute-/HTMX-Prüfungen. Keine stillen Skips.
6. **Browser:** volle Chromium- und Firefox-E2E-Matrix, Desktop/Mobile, No-JS, Filter, History/Deep Links, Fokus, CSS/Overflows und eigene neue Gegenprobe; Screenshots/Daten eindeutig dem Candidate-SHA zuordnen.
7. **Native/Container echt:** Linux-/Native-Build und `testNative`/echte Container-Durchläufe **für diesen SHA**. Prüfe Native-Lucenesuche, reale JVM-/Native-Parität, QML, read-only RootFS/XTF, Arbitrary-UID, kein DB-/Indexvolume, tatsächliche Zielarchitektur. Eine macOS-ARM-Probe **nicht** als linux/amd64-Erfolg ausgeben. Ein älteres GitHub-amd64-Release **nicht** als frischen Kandidatenbeweis verwenden. Wenn diese Pflicht nicht ausführbar ist und kein aktuelles nicht-publizierendes CI-Artefakt vorliegt: `BLOCKED (INFRA)`.
8. **Release ohne Release:** Prüfe T5-`versioning-contract.py`, `release-contract.py`, alle acht Tagmuster, Main-only-Push, PR-No-Push, `linux-native needs: jvm`, Tags/Digests/Pull-/HTTP-Gates, Secrets-/Triggergrenzen und den Codeberg-Mirror. Keine echte Publikation. Etwaige historische `0.2.73`-Digests korrekt ausschliesslich als historischen Beleg zitieren.
9. **Zwei wirklich neue Gegenproben:** (i) ein manipuliertes Script/CI-/Referenzpfad-Szenario muss den neuen Testsuite-Check fehlschlagen lassen und (ii) ein übergreifender fachlicher Fall mit alternativer XTF + konfigurierter XSS-Provenienz, Lucene-Suche und Native-/JVM-Antwort muss die Sicherheits-/Paritätsbehauptung ernsthaft versuchen zu falsifizieren. Auf dem sauberen Kandidaten selbst keine Quellen ändern.
10. **Kumulative Prüfung:** AC0-01–05, AC1-01–05, AC2-01–07, AC3-01–06, AC4-01–08 **je mit eigener Tabellenzeile** bewerten. Selbst bei vollständigem `test`-PASS kann ein fehlendes Archiv, ein falscher CI-Pfad oder eine noch in HTML hart codierte Provenienz den Gesamt-PASS verhindern.

## 4. Testdurchführung und negative Assertions

Alle ausgeführten Kommandos mit tatsächlichem `cwd`, Version, Exit, Ergebnis und Reportpfad festhalten. Üblicher Testkatalog – **nur wenn die jeweilige Lieferung ihn fällig macht; erst nach C2 existieren die neuen Scriptnamen**:

```bash
./gradlew clean test quarkusBuild quarkusIntTest --console=plain
./gradlew playwrightTest --console=plain
python3 scripts/runtime-process-contract.py
python3 scripts/runtime-process-contract.py --dev
python3 scripts/search-index-contract.py
python3 scripts/versioning-contract.py
python3 scripts/release-contract.py
./gradlew dependencies --configuration runtimeClasspath
# Testverteilung und echtes Native-Image nach docs/operations.md.
python3 scripts/distribution-contract.py --jvm-image <jvm> --native-image <native> \
  --expected-platform linux/amd64 --report-dir build/reports/cleanup-review/distribution
```

Bei Testausgaben die **Assertions**, Testfallzahlen und JUnit XML betrachten; eine nur auf `print("PASS")` gestützte Aussage ist kein Test. Skripte mit `assert` dürfen nicht unter optimiertem Python (`python -O`, `PYTHONOPTIMIZE=1`) laufen. Gefälschte Exitcodes durch `|| true`, `try/except: pass`, ignorierte `subprocess`-Returncodes, kleinere Testselektionen und wegkommentierte CI-Steps sind Findings.

**Schutz gegen „nur Grün“:** Die Gegenprobe muss ein vorher behauptetes Verhalten nachweislich widerlegen können, etwa `qwcLayerName`-Feldumbenennung, kaputter 404-Status, nicht geöffneter Index, bösartige XTF, fehlendes Script im CI oder durch Quarkus unescaped dargestellte Konfiguration. Erwarteter Fehlschlag eines mutierten Inputs ist **positiver Harness-Nachweis**; sein tatsächlicher Exit muss aber separat als **erwartet ungleich 0** dokumentiert werden.

## 5. Reviewbericht – zwingendes Format

Schreibe nach eigenem Review einen vollständigen, reproduzierbaren Markdownbericht **ausserhalb des unveränderlichen Produktkandidaten**. Codex wird ihn nach dem Review im Repository archivieren.

```markdown
# Unabhängiger Review C<N> – Kartenkatalog Cleanup

## 1. Identität und Umgebung
- C<N>, PHASENGATE ja/nein:
- OpenCode-GUI-Version, tatsächlich gewählter DeepSeek-Provider/Modellkennung und deren geprüfte Herkunft:
- BASE_SHA:
- CANDIDATE_SHA (im Worktree verifiziert):
- Review-Worktree, git status vorher/nachher:
- Java, Gradle, Python, Browser, Docker, GraalVM/Mandrel / Zielarchitektur:

## 2. Eigene Source-/Git-/Consumeranalyse
- Ist-/Soll-Abgleich der betroffenen Dateien:
- Echte Consumers / Releaseschutz / vorhandene und entfernte Klassen:
- Git-Blob-Identität historischer Evidenz (falls betroffen):

## 3. Jede Muss-AC einzeln
| AC-ID | erfüllt / verletzt / nicht prüfbar | Eigener Code-/Test-/Prozessbeleg | Finding-ID |
|---|---|---|---|
| ... |

## 4. Selbst entwickelte Gegenprobe(n)
- Ziel/Hypothese / Warum dieser Gegenfall ein echtes Risiko adressiert:
- Genaue neue Eingabe und Datenherkunft:
- Kommando, erwartete Beobachtung, tatsächlicher Output, Exit:
- Wie sie über einen trivialen grünen Test hinausgeht:

## 5. Pflichtgates und Rohbelege
| Gate | Kommando | Exit | Tests / Errors / Failures / Skips | Report | SHA-Bindung |
|---|---|---|---|---|---|
| ... |
- Nicht ausgeführte fällige Prüfungen und konkreter Grund:
- Native-/Linux-/Containerarchitektur und echte Ausführung:
- Release-/Tag-/Triggerprüfung ohne Push:

## 6. Findings
- ID, Schwere, Code-/Dokupfad, Reproduktion, erwartet vs. beobachtet, notwendige Nacharbeit:
- Blocker nach INFRA / MISSING_EVIDENCE / SCOPE_DECISION:

## 7. Urteil
**PASS / FAIL / BLOCKED**
- Welche Muss-Kriterien dieses Urteil begründen:
- Nächster zulässiger Schritt:
```

**C4-Urteil nur als kumulative Entscheidung:** Falls für eine einzige aktuelle Muss-AC kein wirklicher Beleg vorliegt, ist keine Gesamtfreigabe möglich. Falls C4 `PASS` erreicht: Es ist die **Cleanup-Freigabe des Kandidatencommits**, **nicht** eine Freigabe zum Push nach `main` oder zur automatischen Docker-Hub-Veröffentlichung. Darüber entscheidet der Nutzer separat.

## 6. Arbeitsauftrag an dich jetzt

Lies den vollständigen verbindlichen Plan und den konkreten Handoff. Prüfe im sauberen Worktree den **gesamten für diese Lieferung geltenden** Umfang, nicht nur Codex' Bericht. Führe die fälligen Tests **selbst** aus, ergänze mindestens eine echte eigene negative Gegenprobe, bewerte **jede** AC und gib eines von `PASS`, `FAIL`, `BLOCKED` mit belastbaren Belegen zurück. Keine Kandidatenänderung, kein Push, kein Merge und kein Release.
