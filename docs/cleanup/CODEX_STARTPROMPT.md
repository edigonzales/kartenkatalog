# Codex-Startprompt – Altlastenbereinigung nach dem Quarkus-Refactoring

**Du bist Agent A (Implementierung) im bestehenden Repository `edigonzales/kartenkatalog`.**

**Verbindlicher Auftrag:** `docs/cleanup/EXECUTION_PLAN.md` vollständig lesen und ausführen.  
**Unabhängiger Review:** `docs/cleanup/OPENCODE_REVIEW_PROMPT.md` in OpenCode 2.x GUI auf macOS mit DeepSeek API / DeepSeek 4.1 Flash 1017.  
**Jetzt nur Lieferung `C0` beginnen.** Nicht bereits `C1`–`C4` implementieren. Eine neue Gesamt-Spezifikation ist **nicht** dein Auftrag: Die Spezifikation liegt vor.

## 1. Zielbild

Die bereits funktionierende **Quarkus-3.40.1.1-/Java-21-/Lucene-10.6.0-/Qute-/HTMX-Anwendung** soll nach einer erfolgreichen Spring-Boot-Migration konsequent von historischen Dateien, Phasennamen, unzutreffenden Laufzeittexten und überholter Dokumentation bereinigt werden. Sie bleibt **im selben Repository**. Erhalte ihre XTF-only-Laufzeit, den unveränderlichen RAM-Snapshot, den Lucene-`ByteBuffersDirectory`-Index, die JSON-API für das separate QWC2-Plugin, JVM/Native/Browser-Qualität und die Docker-Hub-Releasepipeline.

Verbindliche Arbeitspakete gemäss Plan:

- **C0:** aktueller Git-/Build-/Consumer-/Fixture-/Test-Baseline-Nachweis, ohne Produktivänderung;
- **C1:** drei belegbar unbenutzte Root-Artefakte und projektfremde `.gitignore`-Regeln entfernen;
- **C2:** phasenbasierte Python-Harness-/Testklassen-/Fixture-/Reportnamen vollständig und ohne Testverlust umbenennen; CI/Release-Aufrufer aktualisieren;
- **C3:** doppelte XTF-Defaultkonfiguration reduzieren und den statischen, bei fremder XTF irreführenden Harvest-Footer als optionales sicher escaped Konfigurationsfeld lösen; übrige Codealtlasten nach Beweis prüfen;
- **C4:** kurze aktuelle Architektur-/Betriebs-/Testdokumentation schaffen, alte Migration samt unabhängiger Reports und Screenshots **unverändert archivieren**, volle JVM-/Browser-/Native-/Container-/Release-Regression und unabhängige Gesamtabnahme.

Die genauen **31 Muss-Abnahmekriterien** stehen in `EXECUTION_PLAN.md`. Keine eigenmächtige Änderung der ACs oder der geschützten API-/Fixture-/Releaseverträge.

## 2. Arbeitsprinzipien: besonders wichtig

1. **Bestand statt Behauptung:** Der bei der Recherche gesehene Commit war `f37b19598d806294121f681a65c2dd93079ce520`. Der tatsächliche aktuelle HEAD ist massgeblich. Vorhandene lokale Änderungen anderer Personen **nicht** verwerfen, zurücksetzen, überschreiben oder ungefragt committieren.
2. **Nichts löschen, nur weil es alt aussieht:** `MockCatalogRepository` ist ein aktives Testdouble, `CatalogRepository` eine reale DI-/Testabstraktion, `LuceneNativeConfiguration` ein nötiger GraalVM-Hint, `data/kartenkatalog-harvest-ohne-qml.xtf` der aktive XTF-Fixturebestand. Die Quarkus-SODATA-Assets sowie Lizenzen bleiben.
3. **Kein fiktiver P3ITTest:** Diese Klasse war im recherchierten `main` nicht mehr vorhanden. Kontrolliere erneut `git ls-files`/`git grep` und schreibe das **tatsächliche** Ergebnis auf; erfinde keine zu löschende Klasse.
4. **Historische Review- und Implementierungsberichte sind nachgewiesene Evidenz:** Sie dürfen ihre alten P0–P5-Namen behalten und werden später gemeinsam archiviert. **Niemals ihren Inhalt umschreiben**, um einen Grep über das ganze Repository künstlich phasenfrei zu machen.
5. **Tests nicht verlieren:** Bestehende `scripts/p0-http-contract.py`, `p1-process-contract.py`, `p2-process-contract.py`, `p4-distribution-contract.py` sind **aktiver Teil der CI**. Sie werden erst C2 mit `git mv` und vollständigen CI-/Caller-/Dokureferenzen ersetzt. Nicht löschen, nicht still abschwächen, keine erwarteten Fehler in Erfolgsfälle umdeuten.
6. **Release-Sicherheit:** `.github/workflows/main.yml` veröffentlicht auf erfolgreichem Main-Push nach Gates automatisch native und JVM-Images (`sogis/kartenkatalog`); ein anderer Workflow spiegelt nach Codeberg. **Weder du noch der Reviewer darfst ohne ausdrücklichen Nutzerauftrag `git push`, Merge, Tag, `workflow_dispatch`, Registry-Push oder Release ausführen.** Alle Aufträge enden zunächst mit lokalen Commits. Änderungen an Workflow/CI dürfen nicht die Tests oder die Main-Only-Publish-Bedingung verwässern.
7. **Quarkus/Lucene/Native bleiben fest:** Java21, Quarkus3.40.1.1, Lucene10.6.0, `ByteBuffersDirectory`, `GermanAnalyzer`, Qute, HTMX, `testNative`, read-only XTF und No-DB. Keine Dependency-Updates als Nebeneffekt.
8. **Keine künstlichen Erfolge:** Führe echte Kommandos aus. Notiere Exitcodes, Zahlen (Tests/Failures/Errors/Skips) und Rohreports. `-x test`, `@Disabled`, veränderte Golden-Assertions und unbelegte CI-/Native-Erfolgsaussagen sind unzulässig.
9. **Knappe Agentenartefakte:** Genau ein Implementierungsbericht pro Lieferung und eine aktive `docs/cleanup/STATUS.md`. Kein neues generisches Ticket-/Audit-/Reviewframework.
10. **Review wirklich unabhängig:** Codex darf kein `PASS` erteilen. OpenCode prüft den **exakten unveränderlichen Kandidaten-SHA** im separaten Kontext/Worktree, führt eigene Tests und Gegenfälle aus. Einem Selbst-Review gleichwertige Modellantwort genügt nicht.

## 3. Jetzt ausführen: C0 – Baseline und echte Nutzung klären

### 3.1 Umgebung

Führe die folgenden Schritte ohne globale Änderung an der Nutzerumgebung aus:

```bash
pwd
git status --porcelain=v1
git branch --show-current
git rev-parse HEAD
git ls-files
java -version
./gradlew --version
python3 --version
# Docker/GraalVM/Native/Playwright nur soweit verfügbar und im Bericht präzisieren.
```

Lies `AGENTS.md` (sofern vorhanden), `docs/cleanup/EXECUTION_PLAN.md` vollständig, diesen Prompt und `OPENCODE_REVIEW_PROMPT.md`. Historische Umsetzungs-Prompts unter `docs/quarkus-migration/` sind **keine** neuen Implementierungsaufträge. Erfasse `BASE_SHA` vor der C0-Änderung.

### 3.2 Analyse des aktiven Bestands (nur C0)

Lies gezielt:

- `README.md`, `data/README.md`, `build.gradle`, `.gitignore`, `.dockerignore`, `gradle/versioning.gradle`, `.github/workflows/{main,mirror}.yml`, `deploy/openshift.yaml`;
- alle `scripts/*.py`, insbesondere ihre `subprocess`-Ketten, `build/reports/p*`-Pfade und Docker-Hub-Verifikationsoptionen;
- `src/main/java/ch/so/agi/meta/{CatalogConfig,CatalogStore,CatalogSnapshot,CatalogRepository,CatalogService,CatalogXtfSource,XtfCatalogParser}.java` und `search/`, `web/`, `api/`;
- `src/main/resources/application.properties`, `templates/layout.html`, `META-INF/resources/catalog.js`, CSS und echte lokale Vendor-Assets;
- alle `src/test/java`, `src/integrationTest/java`, `src/test/resources/contract`, `search`, `web`, `final`;
- `docs/quarkus-migration/{STATUS,OPERATIONS,EXECUTION_PLAN}.md`, `implementation/`, `reviews/`, `reference/` und `docs/legacy-notes.md` nur zur Provenienz-/Link-/Consumerprüfung.

Führe reproduzierbare Suchen aus, nicht allein GitHub-UI-Suchen:

```bash
git grep -n 'kartenkatalog.duckdb.zip\|kartenkatalog.xtf.zip\|mockup_C_tree_clean.png' -- . ':!docs/quarkus-migration' || true
git grep -n -E 'P3ITTest|p0-|p1-|p2-|p4-|BaselineContractTests|BaselineQmlTests|build/reports/p[0-5]' -- \
  src scripts .github README.md build.gradle gradle deploy docs/quarkus-migration/OPERATIONS.md || true
unzip -l kartenkatalog.duckdb.zip
unzip -l kartenkatalog.xtf.zip
sha256sum data/kartenkatalog-harvest-ohne-qml.xtf src/test/resources/contract/baseline.json
```

Falls macOS kein `sha256sum` anbietet, nutze `shasum -a 256`. Beachte: historische `.md`-Berichte dürfen alte Namen enthalten. Prüfe, ob Dateien inzwischen bereits umbenannt sind: dann die Spezifikation nicht blind auf den alten Baum anwenden, sondern Ist-/Soll-Differenz dokumentieren.

Erstelle `docs/cleanup/implementation/C0.md` mit **KEEP/DELETE/RENAME/ARCHIVE/OPTIONAL_REFACTOR**-Matrix, dokumentiertem Caller und dem jeweils fälligen Negativ-/Verhaltensnachweis. Prüfe ausdrücklich, dass die drei ZIP/PNG-Root-Artefakte nicht zur Laufzeit gebraucht werden und das `data/`-XTF die tatsächliche Quelle ist.

### 3.3 Baseline-Prüfungen

Prüfe die tatsächlich vorhandenen Tasks und führe die aktuelle JVM-/API-/Browser-/Prozess-/Release-Suite soweit fällig aus:

```bash
./gradlew clean test quarkusBuild quarkusIntTest --console=plain
./gradlew playwrightTest --console=plain
python3 scripts/p1-process-contract.py
python3 scripts/p1-process-contract.py --dev
python3 scripts/p2-process-contract.py
python3 scripts/versioning-contract.py
python3 scripts/release-contract.py
./gradlew dependencies --configuration runtimeClasspath
```

**C0 verwendet noch die alten Scriptnamen**. `scripts/p0-http-contract.py` braucht einen bereits gestarteten Server oder wird über `p1-process-contract.py` aufgerufen. Beobachte und dokumentiere die tatsächlichen Testzahlen, gebauten Artefakte, Resultate, Negativfälle, Fehlermeldungen und mögliche Tool-/Infrastrukturblocker. **Keine** Release-/Push-CLI. Das echte Native-Gesamtgate wird in C4 erneut ausgeführt; C0 ist nur Baseline.

Die Fixturewerte 169 Themen / 886 Ebenen gelten nur für `data/kartenkatalog-harvest-ohne-qml.xtf`; keine hardcoded Produktionsannahme. Der historische P5-Review und die Docker-Hub-Veröffentlichung 0.2.73 belegen **nicht** den neuen Kandidaten-SHA.

### 3.4 C0-Status und Abnahme

Erstelle `docs/cleanup/STATUS.md` mit letzter unabhängiger Cleanup-PASS `none`, Lieferung `C0`, tatsächlichen offenen Punkten, nächstem Schritt. In `docs/cleanup/implementation/C0.md` alle Kriterien **AC0-01 bis AC0-05** jeweils mit konkreten Belegen behandeln. Im ersten Commit **keine** Root-Artefakte löschen, Scripts umbenennen oder Produktivcode ändern.

Dann:

1. `git diff --check` und `git status --short` prüfen.
2. C0-Report, `STATUS.md` und ggf. minimale C0-Test-/Dokuevidenz **lokal committen**.
3. Erst **nach** diesem Commit den vollständigen `CANDIDATE_SHA` bestimmen.
4. Einen manuellen OpenCode-Handoff ausgeben; wenn OpenCode-GUI von deinem Kontext nicht bedienbar ist, **STOPP**.

Beispielausgabe (Werte wirklich ermitteln):

```text
Repository: edigonzales/kartenkatalog
Lieferung: C0
BASE_SHA: <vollständiger SHA>
CANDIDATE_SHA: <vollständiger SHA>
Kriterien: AC0-01..AC0-05
Implementierungsbericht: docs/cleanup/implementation/C0.md
Reviewprompt: docs/cleanup/OPENCODE_REVIEW_PROMPT.md
Letzter Cleanup-PASS: none
Fällige aber nicht ausgeführte Prüfungen: <exakt/keine>
Status: AWAITING_REVIEW
```

**Nach C0 STOPPEN.** Nicht zu C1 springen, bevor der Nutzer ein echtes OpenCode-/DeepSeek-`PASS` auf den **exakten** Kandidaten-SHA zurückgemeldet hat. Bei `FAIL`: Befunde in C0 korrigieren, neu committen, neu prüfen. Bei `BLOCKED`: konkrete Voraussetzung herstellen; kein PASS fingieren.

## 4. Fortsetzung nach Freigaben

Bei C0-PASS nacheinander **nur** die vollständig fälligen Lieferungen C1, C2, C3 und C4 durchführen, jede einzeln mit `BASE_SHA`, `CANDIDATE_SHA`, Kurzbericht, konkreten Tests und **echtem unabhängigen PASS**. Nach C1-PASS folgt C2, nach C2-PASS C3, nach C3-PASS C4. Ein `FAIL` wiederholt den **gleichen** Abschnitt, nicht einen neuen beliebigen Unterabschnitt. C4 benötigt sämtliche C0–C4-Kriterien und vollständige frische T1–T5-Prüfung auf dem endgültigen Produkt-SHA, einschliesslich echtem Native-/Container-Gate und Releaseschutz.

**Besondere Regel:** Ohne einen echten aktuellen Linux-/Native-Beleg für C4 darfst du weder selbst noch per berichtender Zusammenfassung eine komplette Abnahme behaupten. Die bestehende Main-Push-Pipeline veröffentlicht automatisch; ein nicht veröffentlichendes PR-/Branch-CI-Gate ist nur nach ausdrücklicher Nutzerfreigabe zum entsprechenden Push erlaubt.

**Jetzt keine neue Planung schreiben. Beginne C0, sichere die Baseline und liefere den unabhängigen Handoff.**
