# Unabhängiger Review C1 – Kartenkatalog Cleanup

## 1. Identität und Umgebung

- **Lieferabschnitt:** C1 – Historische Root-Artefakte und `.gitignore` säubern. **PHASENGATE: nein** (C1; kein C4-Gesamtgate).
- **Repository:** `edigonzales/kartenkatalog`.
- **BASE_SHA:** `dd0e1a871db7e124487d02c1a4742b2653f0c51c` (Commit, direkt Elternknoten des Kandidaten; verifiziert per `git rev-parse`, `git cat-file -t`, `git merge-base --is-ancestor`).
- **CANDIDATE_SHA (im Review-Worktree verifiziert):** `b3df50894a40e672612b213de01094685062db76`.
- **Review-Worktree:** `/Users/stefan/sources/kartenkatalog-cleanup-C1-review-b3df508`, detached HEAD auf `b3df508…`. Alle Kommandos/Tests/Proben liefen mit explizitem `cwd` in diesem Worktree. `git status --porcelain=v1` war vor und nach dem Review leer; der tracked Tree blieb unverändert. Der Haupt-Checkout `/Users/stefan/sources/kartenkatalog` stand ebenfalls auf `b3df508…` und war tracked-sauber; die GUI-Session zeigt als `directory` den Haupt-Checkout, die Prüfkommandos liefen jedoch ausschliesslich im separaten Worktree.
- **OpenCode-/Provider-/Modellidentität (aus überprüfbaren Metadaten, nicht aus Modell-Selbstauskunft):**
  - Laufende GUI-App: **OpenCode 2.1.4** auf macOS. Belege: Prozesspfad `/Users/stefan/Library/Application Support/ai.opencode.desktop/cli/2.1.4-arm64/opencode-cli serve --service`; Crashpad-Annotation `_version=2.1.4`; Session-Datensatz `session_v2.version = 2.1.4`.
  - Aktuelle Session: `ses_e697d58c4ffeRRBBRq0fObUy6s`, Titel „C1-Review für kartenkatalog“, `agent=build`, `model={"id":"deepseek-flash","providerID":"deepseek","variant":"high"}`.
  - Direkter Provider: **`deepseek`**. Der von der laufenden 2.1.4-Instanz verwendete Katalog (`kv['models-dev:catalog']`) nennt für Provider `deepseek`: `api = https://api.deepseek.com`, `npm = @ai-sdk/openai-compatible`, `env = DEEPSEEK_API_KEY`. Das CLI-Binary enthält ausserdem den direkten Endpunkt `api.deepseek.com/v1`. `auth.json` enthält einen `deepseek`-Eintrag; **Credentialwerte wurden nicht gelesen oder ausgegeben**.
  - Modell: `deepseek/deepseek-flash`, Anzeigename im aktuellen Katalog **„DeepSeek V4.1 Flash 1017“**, `release_date 2026-10-17`, `canonical_model_id deepseek/deepseek-v4.1-flash-1017`; Session-Variante `high`. Das entspricht der vom Nutzer bestätigten GUI-Auswahl. Es wurde **kein OpenRouter-/Zen-Fallback** verwendet: Session-Provider ist `deepseek`, der Provider-Katalog zeigt den direkten DeepSeek-Endpunkt. Der ältere Cache `~/.cache/opencode/models.json` führt denselben Modellschlüssel noch als „DeepSeek V4.1 Flash“ (Release 2026-09-10); für die laufende Sitzung ist der aktuellere `models-dev`-Katalog der 2.1.4-Instanz maßgeblich.
- **Toolchain:** macOS 15.8.1/arm64; Temurin Java 21.0.7+6-LTS; Gradle Wrapper 9.8.2; Python 3.9.6 mit `sys.flags.optimize == 0`; Docker Client/Engine 29.8.2; Quarkus 3.40.1.1, Lucene 10.6.0, Playwright-Java 1.60.0 (Builddefinition unverändert).
- **Zielarchitektur:** C1 ist eine Artefakt-/Ignore-Lieferung ohne Produkt-, Test-, Build- oder CI-Änderung. Es wurde **kein** Native-/Linux-Container- oder Browser-Gesamtgate behauptet; die Fälligkeit wurde mit dem Plan abgeglichen (siehe §5).

## 2. Eigene Source-/Git-/Consumeranalyse

### 2.1 Kandidatenumfang und Blob-Identität

`git diff --name-status BASE..CANDIDATE` und `git diff --raw` zeigen ausschliesslich:

| Status | Pfad | Blob vorher → nachher |
|---|---|---|
| M | `.gitignore` | `61b3a09…` → `6584715…` |
| M | `docs/cleanup/STATUS.md` | `c02ecaf…` → `58eb7e7…` |
| A | `docs/cleanup/implementation/C1.md` | `0000000…` → `345eca0…` |
| D | `kartenkatalog.duckdb.zip` | `17581ad…` → gelöscht |
| D | `kartenkatalog.xtf.zip` | `f61e488…` → gelöscht |
| D | `mockup_C_tree_clean.png` | `9d49ec7…` → gelöscht |

Eigener vollständiger Tree-Vergleich: BASE 138 getrackte Dateien, Kandidat 136; **3 gelöscht, 1 neu, 2 modifiziert, 133 unverändert**. Alle unveränderten 133 Dateien haben in beiden Trees dieselbe Git-Blob-ID, darunter sämtliche `src/main`, `src/test`, `src/integrationTest`, Scripts, `.github`, `build.gradle`, `gradle/`, `deploy/`, Vendor-Lizenzen, Testfixtures und `.dockerignore`. Es gibt keine versteckte Produkt-/Test-/Build-/CI-Änderung.

**Beobachtung zur Berichtsarithmetik (O1, nicht AC-relevant):** Der C1-Bericht formuliert „alle übrigen 134 ursprünglichen Dateien bleiben bytegleich“. Tatsächlich sind es **133** bytegleiche Bestandsdateien, weil neben `.gitignore` auch `docs/cleanup/STATUS.md` modifiziert ist. Die Evidenzdatei `unchanged-files-check.json` listet als geändert nur `.gitignore` und die drei Löschungen und lässt `STATUS.md` aus. Die Substanz (keine Änderung an Produkt-/Test-/Build-/Datenpfaden) ist durch den unabhängigen Blob-Vergleich dennoch belegt.

### 2.2 Gelöschte Root-Artefakte: Inhalt, Consumer, Datenverlust

- `kartenkatalog.duckdb.zip` (1'226'128 Byte): enthält ausschliesslich `kartenkatalog.duckdb` (21'245'952 Byte, Zeitstempel 14.03.2025), DuckDB-v1.1.3-Header. Keine DuckDB-Laufzeit, kein Java-/Script-/Build-/CI-/Docker-/Deploy-/Template-/README-Consumer.
- `kartenkatalog.xtf.zip` (1'500'028 Byte): enthält ausschliesslich `kartenkatalog.xtf` (18'579'582 Byte), XTF **2.3**, Modell `SO_AGI_Kartenkatalog_20250311`, 689 `Produkt_Mutter_Kind`-Datensätze. Das ist **nicht** die aktive XTF 2.4/`SO_AGI_Kartenkatalog_Publikation_20251210`.
- `mockup_C_tree_clean.png` (90'652 Byte, PNG 1500×950): kein Vorkommen in Qute-Templates, CSS, JavaScript, aktiven Dokumenten oder Build-/Deploypfaden. Die historischen SODATA-Referenzbilder unter `docs/quarkus-migration/reference/` sind separat und unverändert.

Eigene Suche über den Kandidaten:

```text
git grep -n -i -E 'kartenkatalog\.duckdb|kartenkatalog\.xtf|mockup_C_tree_clean' -- . \
  ':!docs/quarkus-migration' ':!docs/cleanup' ':!docs/legacy-notes.md'
→ Exit 1, keine Treffer.
```

Die 19 Treffer der ungefilterten Suche liegen ausschliesslich in `docs/cleanup/**` (Plan, Prompt, C0/C1-Bericht) und damit in Prozessdokumenten, nicht in aktiven Consumern. `git grep` über `.github`, `scripts`, `deploy`, `src`, `build.gradle`, `gradle` findet keinen Treffer.

Zusätzliche Independent-Checks:

- `git archive CANDIDATE | tar -tf - | grep -E 'kartenkatalog\.(duckdb|xtf)\.zip|mockup_C_tree_clean'` → **keine Treffer**; die Dateien sind auch im Export nicht vorhanden.
- Die drei BASE-Blobs existieren weiterhin als Git-Objekte (`17581ad…`, `f61e488…`, `9d49ec7…`); die Inhalte sind über die Historie wiederherstellbar. Keine `old/`-Schattenablage, kein `filter-repo`, kein Force-Push. BASE ist direkter Parent des Kandidaten; `git log BASE..CANDIDATE` zeigt genau einen Commit.
- Die aktive App startet ohne die gelöschten Dateien und lädt die Harvest-Fixture (eigener Lauf, §4.3).

### 2.3 Aktive Fixture, Testressourcen und `.dockerignore`

| Prüfung | Ergebnis |
|---|---|
| `git ls-files '*.xtf'` | ausschliesslich `data/kartenkatalog-harvest-ohne-qml.xtf` |
| SHA256 Harvest-XTF vor/nach | `de40f025368ae6fc70f5858ab42592488bca837c6942cb6ff8759e6c8803de86` (identisch) |
| Modell/Namespace | XTF 2.4, `SO_AGI_Kartenkatalog_Publikation_20251210`, Namespace `http://www.interlis.ch/xtf/2.4/INTERLIS` |
| Objektzahlen | 169 `<Thema>`, 886 `<Ebene>` in der Fixture; realer JVM-Start meldet „169 themes, 886 layers“ |
| `data/README.md` SHA256 | `3744512640d0e2a6ec0ab13fec2929bcc541bdad35ee12a87fce7ce1a6e4736c` (unverändert) |
| `src/test/resources/contract/baseline.json` SHA256 | `cd1167f8870b5dae021b913c08b7643427683ed406e3018e915cbab4feff219b` (unverändert) |
| `src/test/resources/final/catalog.xml` SHA256 | `4f2e54495e0b852b06e5a35e76763216afa04e6da5b1d4383437c9de732ec510` (unverändert) |
| kleine Fixtures `search/`, `web/`, `contract/with-qml.xml` | SHA256 unverändert (`1e13d09…`, `19eadaa…`, `f42e614…`) |
| `.dockerignore` SHA256 | `0e94c74128cdd4ae9b8504e693b40b82d734bbe55dce42ed97fc19cc2db6299a`, bytegleich zu BASE |

`.dockerignore`-Kontrolle: Das Whitelist-Modell (`**` mit Freigaben für `build/`, `build/quarkus-app/**`, `build/*-runner`, `src/main/docker/**`) passt unverändert zu `Dockerfile.jvm` (`COPY build/quarkus-app/`) und `Dockerfile.native` (`COPY build/*-runner`). Die Root-ZIPs/PNG waren ohnehin nicht Teil des Docker-Contexts. Es war kein Docker-Build für C1 fällig; die Kontrolle erfolgte per Inhalt, Hash und Dockerfile-Abgleich.

### 2.4 Vorheriger PASS und Archividentität

- Der letzte unabhängige Cleanup-PASS ist C0 auf `1378d3f5b2bb7a210efa5f704c94a4e43ba23643`. Dieser Commit ist Vorfahre von BASE (`merge-base --is-ancestor` Exit 0); zwischen ihm und BASE liegt nur der dokumentarische Archivierungscommit `dd0e1a8`.
- `docs/cleanup/reviews/C0-review.md` hat in BASE und Kandidat dieselbe Git-Blob-ID `db0c32f98b8ae3ea2af89721324cd00782fa9cdb`; der PASS-Bericht ist unverändert.
- `docs/cleanup/STATUS.md` verweist korrekt auf C0-PASS und C1 `AWAITING_REVIEW`; der historische P5-PASS wird nicht als Cleanup-PASS ausgegeben.

## 3. Jede Muss-AC einzeln

| AC-ID | erfüllt / verletzt / nicht prüfbar | Eigener Code-/Test-/Prozessbeleg | Finding-ID |
|---|---|---|---|
| **AC1-01** | **erfüllt** | Alle drei Root-Artefakte sind aus `git ls-files`/Tree entfernt (`D` im Diff; `git archive` ohne Treffer), kein aktiver Consumer (`git grep` Exit 1; nur Prozess-/HistorienDocs), keine Schattenablage, keine History-Rewrite (BASE direkter Parent, genau ein Commit), BASE-Blobs weiterhin vorhanden. | — |
| **AC1-02** | **erfüllt** | `git ls-files '*.xtf'` = nur `data/kartenkatalog-harvest-ohne-qml.xtf`; SHA256 `de40f025…` vor/nach identisch; XTF 2.4/`SO_AGI_Kartenkatalog_Publikation_20251210`; 169/886 in XML und an realem API-Start; alle Testfixtures/READMEs/Lizenzen unter den 133 bytegleichen Bestandsdateien. | — |
| **AC1-03** | **erfüllt** | Eigene `git check-ignore -v --no-index`-Matrix: `.env`, `.env.production`, `.secrets/`, `secrets/`, `.gradle/`, `build/`, `out/`, `*.class`, fremdes `*.jar`, Logs, IDE/macOS/Python/Venv, beliebige neue XTF ignoriert; `gradlew`, `gradle/wrapper/gradle-wrapper.jar`, `build.gradle`, `settings.gradle`, `src/main/resources/application.properties`, Java-Quellen, Scripts, Test-XML und die Harvest-XTF nicht ignoriert. `git ls-files -ci --exclude-standard` = 0. Reale neue XTF/XML-Canaries und Differentialprobe in §4.1/4.2. `.dockerignore` bytegleich und passend zu beiden Dockerfiles. | — |
| **AC1-04** | **erfüllt** | Frischer T1-Lauf `./gradlew clean test quarkusBuild quarkusIntTest`: Exit 0, **62/0/0/0** Unit/HTTP + **3/0/0/0** Paket-IT; p1 Exit 0 mit 1 Gutstart + 8 erwarteten Fehlstarts (je Exit 1), p1 `--dev` Exit 0 mit CORS 200/403, p2 Exit 0 mit 886/12 Dokumenten und erwartetem Broken-Index-Exit 1. `git diff --check BASE CANDIDATE` Exit 0. Keine Test-/Assertion-/Selektionsänderung (alle Testpfade bytegleich). | — |
| **AC1-05** | **erfüllt** | Unabhängiger Review im separaten Worktree auf exakt `b3df508…`; eigene Nichtverwendungsprüfung mindestens eines gelöschten Artefakts (Archive-/Grep-/App-Start-Nachweis); eigene neue Ignore-/Fixture-Gegenprobe mit realen Dateien und Differentialprobe BASE vs. Kandidat; korrekte SHAs/Handoff. | — |

## 4. Selbst entwickelte Gegenprobe(n)

### 4.1 Eigene Ignore-/Fixture-Gegenprobe mit realen Dateien

- **Ziel/Hypothese:** Eine neu angelegte, nicht versionierte XTF muss ignoriert werden; eine neu angelegte XML-Testressource und die bestehende Harvest-XTF müssen weiterhin trackbar sein. Damit wird nicht nur Codex' Fallliste wiederholt, sondern das tatsächliche Dateisystem-/Statusverhalten geprüft.
- **Neue Eingabe:** Im Review-Worktree wurden temporär `data/reviewer-canary.xtf` (`<canary/>`) und `src/test/resources/contract/reviewer-canary.xml` (`<canary/>`) angelegt. Die echte Fixture wurde nicht ersetzt oder verändert.
- **Kommandos/Ergebnisse:**
  - `git status --porcelain=v1 --ignored` → `!! data/reviewer-canary.xtf`, `?? src/test/resources/contract/reviewer-canary.xml`.
  - `git check-ignore -q --no-index -- data/reviewer-canary.xtf` → **Exit 0** (ignoriert); Regel `.gitignore:18:*.xtf`.
  - `git check-ignore -q --no-index -- src/test/resources/contract/reviewer-canary.xml` → **Exit 1** (nicht ignoriert); erwarteter negativer Exit.
  - `git check-ignore -q --no-index -- data/kartenkatalog-harvest-ohne-qml.xtf` → **Exit 1** (nicht ignoriert); Regel `.gitignore:19:!/data/kartenkatalog-harvest-ohne-qml.xtf`.
  - Nach `rm` der beiden Canaries: `git status --porcelain=v1` leer, Exit 0.
- **Warum nicht trivial:** Die Probe nutzt echte untracked Dateien und prüft die unterschiedliche Behandlung von XTF vs. XML sowie die Ausnahme für genau die eine Fixture. Ein pauschales `*.xtf` ohne Ausnahme oder eine versehentliche XML-Ignore-Regel würde auffallen.

### 4.2 Differentialprobe: altes vs. neues `.gitignore` in isolierten `/tmp`-Repos

- **Ziel/Hypothese:** Die C1-Änderung soll `.env` neu schützen, ohne die XTF-/Source-/Wrapper-Ausnahmen zu verlieren.
- **Neue Eingabe:** Zwei frische Git-Repos unter `/tmp/kartenkatalog-c1-review-b3df508/ignore-diff/{base,candidate}` mit der jeweiligen `.gitignore` aus BASE bzw. Kandidat.
- **Ergebnisse (`git check-ignore -q --no-index`, 0 = ignoriert, 1 = nicht ignoriert):**

| Pfad | BASE | Kandidat |
|---|---:|---:|
| `.env` | 1 (nicht ignoriert) | **0 (ignoriert)** |
| `data/anderes.xtf` | 0 | 0 |
| `data/kartenkatalog-harvest-ohne-qml.xtf` | 1 | 1 |
| `src/test/resources/contract/baseline.json` | 1 | 1 |
| `gradle/wrapper/gradle-wrapper.jar` | 1 | 1 |

- **Warum nicht trivial:** Die Probe zeigt die tatsächliche Verhaltensdifferenz der Ignore-Regeln und bestätigt gleichzeitig, dass die Source-/Wrapper-/Fixture-Ausnahmen nicht durch die Kürzung verloren gingen.

### 4.3 Eigener realer JVM-/API-Start mit Default-Konfiguration

- **Ziel/Hypothese:** Die aktive Fixture wird über den Default `file:./data/kartenkatalog-harvest-ohne-qml.xtf` geladen; die reale API liefert 169 Themen/886 Ebenen; die gelöschten Root-Artefakte werden nicht benötigt.
- **Kommando:** Im Review-Worktree `env -u CATALOG_XTF_LOCATION java -Dquarkus.http.port=54611 -jar build/quarkus-app/quarkus-run.jar`, danach HTTP-Abfragen auf Loopback.
- **Beobachtung:** `/q/health/ready` erfolgreich; `/` HTTP 200; `/api/v1/themen` = **169**; `/api/v1/ebenen` = **886**; `/api/v1/facets` mit `organisation`, `thema`, `typ`; für alle Ebenen `qwcLayerName == id`. App-Log: „Catalog loaded: 169 themes, 886 layers; Lucene 10.6.0 ByteBuffersDirectory GermanAnalyzer … from file:./data/kartenkatalog-harvest-ohne-qml.xtf“. SHA256 der Fixture nach dem Lauf unverändert `de40f025…`.
- **Negativ-/Erwartungsfehler:** Der App-Prozess wurde per SIGTERM beendet (`exit 143`, erwartet); die p1-Fehlstartfälle und der p2-Broken-Index endeten jeweils mit tatsächlichem Exit 1 (erwartet). Kein nicht erwarteter Fehlschlag.

## 5. Pflichtgates und Rohbelege

Alle Kommandos mit `cwd=/Users/stefan/sources/kartenkatalog-cleanup-C1-review-b3df508`.

| Gate | Kommando | Exit | Tests / Errors / Failures / Skips | Report | SHA-Bindung |
|---|---|---|---:|---|---|
| T0 Tree/Diff/Hash/Consumer | `git diff --raw`, Blob-Vergleich, `git grep`, `git archive`, SHA256 | 0 bzw. 1 (erwartet bei „keine Treffer“) | 138 → 136 Dateien; 133 bytegleich; 3 D; 1 A; 2 M | `/tmp/kartenkatalog-c1-review-b3df508/{diffstat,hashes,candidate-tree}.txt` | `b3df508…` |
| T1 JVM/Unit/API/Paket | `./gradlew clean test quarkusBuild quarkusIntTest --console=plain` | **0** | **62 / 0 / 0 / 0** + **3 / 0 / 0 / 0** | `/tmp/kartenkatalog-c1-review-b3df508/t1.log`, `reports/test-results/**` | `b3df508…` |
| T1 Kernprozess JVM/HTTP | `python3 scripts/p1-process-contract.py` | **0** | 1 Gutstart; HTTP-Contract Exit 0; 8 Fehlstarts je Exit 1 | `/tmp/…/p1.log`, `reports/p1/process-summary.json` | `b3df508…` |
| T1 Dev/CORS | `python3 scripts/p1-process-contract.py --dev` | **0** | Dev-Start 5.584 s; HTTP-Contract Exit 0; CORS erlaubt 200, fremd 403 | `/tmp/…/p1-dev.log`, `reports/p1/dev-summary.json` | `b3df508…` |
| T1 Index/Prozess | `python3 scripts/p2-process-contract.py` | **0** | Harvest 886 Docs; first/second/changed je 12; Broken-Index erwarteter Child-Exit 1; keine Indexdateien | `/tmp/…/p2.log`, `reports/p2/summary.json` | `b3df508…` |
| Diff-Hygiene | `git diff --check BASE CANDIDATE` | **0** | — | `/tmp/…/diffstat.txt` | `b3df508…` |
| Eigene Ignore-Probe | `git check-ignore -q --no-index …` + reale Canaries + `git status --ignored` | 0 / 1 wie erwartet | 3 Kernfälle; Canaries danach entfernt | `/tmp/…/ignore-diff/`, `status-final.txt` | `b3df508…` |
| Eigene JVM-/API-Probe | `java -jar build/quarkus-app/quarkus-run.jar` + curl | 0 (App), 143 (SIGTERM erwartet) | 169 Themes / 886 Layers; Root 200; Facets OK | `/tmp/…/own-jvm.log`, `own-*.json` | `b3df508…` |

- **Nicht ausgeführte fällige Prüfungen:** **keine.** C1 verlangt gemäss Plan T0/T1 und die bestehenden Kernprozesschecks; diese wurden frisch ausgeführt.
- **Bewusst nicht fällig:** T3-Browser (C1 ist im Plan nicht als Browser-Gate gelistet; keine UI-/Template-/CSS-/JS-Änderung), T4 Native/Container (C4-Gesamtgate; keine distributionsrelevante Semantikänderung), T5 Release (C2/C4; Workflows/Build unverändert). Es wird nichts als ausgeführt behauptet.
- **Native-/Linux-/Containerarchitektur:** nicht ausgeführt; C1 hat keine Native-/Containeränderung. Keine macOS-ARM-Probe wird als linux/amd64-Erfolg ausgegeben.
- **Release-/Tag-/Triggerprüfung ohne Push:** `.github/workflows/**` ist gegenüber BASE bytegleich; keine Workflow-/Trigger-/Tag-Änderung, kein Dispatch, kein Push, kein Registry-Zugriff, kein Release. Der bestehende Main-Push-Releaseeffekt bleibt unverändert und wurde nicht ausgelöst.

## 6. Findings

- **Keine blockierenden Findings. Keine verletzte Muss-AC.**
- **O1 (niedrig, Dokumentation):** Der C1-Bericht nennt „alle übrigen 134 ursprünglichen Dateien“ bytegleich; tatsächlich sind es 133, weil `docs/cleanup/STATUS.md` zusätzlich geändert ist. `unchanged-files-check.json` führt `STATUS.md` nicht in `changed`. Die unabhängige Blob-Prüfung zeigt keine unerwartete Änderung; die Aussage sollte in einem rein dokumentarischen Follow-up korrigiert werden. Kein AC-Verstoss, kein Einfluss auf die Produkt-/Test-/Datenintegrität.
- **Nicht blockierende Beobachtung:** `*.xtf` gilt global; eine **neue** XTF-Testfixture unter `src/test/resources/` müsste bewusst mit `git add -f` versioniert werden. Das entspricht der Planvorgabe (`*.xtf` + Ausnahme genau für die Harvest-Fixture) und ist kein Finding.
- **Blocker nach INFRA / MISSING_EVIDENCE / SCOPE_DECISION:** keine.

## 7. Urteil

**PASS**

- **Begründende Muss-Kriterien:** AC1-01 bis AC1-05 sind einzeln anhand eigener Git-/Blob-/Consumer-/Ignore-/Laufzeitbelege erfüllt. Die drei Root-Artefakte sind aus dem aktiven Tree entfernt und nicht mehr konsumiert; die aktive XTF und alle Testressourcen sind bytegleich; `.gitignore` ist projektspezifisch und durch positive/negative Canaries belegt; T1 und die Kernprozesschecks sind frisch erfolgreich; der Reviewer hat eine eigene Ignore-/Fixture-Gegenprobe und eine eigene reale JVM-/API-Probe ausgeführt.
- **Nicht geprüfte, aber auch nicht fällige Gates:** Browser-, Native-/Container- und Release-Gesamtmatrix sind für C1 gemäss Plan nicht fällig und wurden nicht als bestanden ausgegeben.
- **Nächster zulässiger Schritt:** C1-Review und Status dürfen in einem rein dokumentarischen Commit archiviert werden; danach kann C2 gemäss Plan beginnen. **Kein Push nach `main`, kein Merge, kein Tag, kein Workflow-Dispatch, keine Registry-Publikation und kein Release** ohne separate Nutzerentscheidung.

---

**Geprüfter Kandidat:** `b3df50894a40e672612b213de01094685062db76`  
**Geprüfte Basis:** `dd0e1a871db7e124487d02c1a4742b2653f0c51c`  
**Review-Worktree:** `/Users/stefan/sources/kartenkatalog-cleanup-C1-review-b3df508` (tracked clean vor/nach)  
**Rohbelege:** `/tmp/kartenkatalog-c1-review-b3df508/`
