# Tests und Fehlerdiagnose

Alle Befehle im Repository mit JDK 21 und dem Gradle-Wrapper ausführen. Versionen bleiben Quarkus 3.40.1.1, Lucene 10.6.0, Playwright 1.60.0 und Gradle 9.8.2. Tests benötigen keine Datenbank oder Secrets. Fixturezahlen 169/886 gelten nur für den mitgelieferten unvollständigen Harvest; kleine Search-/Security-/QML-/Integrationfixtures schützen davon unabhängige Verträge.

## JVM, HTTP und Prozesse

```sh
./gradlew clean test quarkusBuild quarkusIntTest --console=plain
./gradlew playwrightTest --console=plain
python3 scripts/runtime-process-contract.py
python3 scripts/runtime-process-contract.py --dev
python3 scripts/search-index-contract.py
python3 scripts/versioning-contract.py
python3 scripts/release-contract.py
./gradlew dependencies --configuration runtimeClasspath --console=plain
```

| Gate | Tatsächlicher Umfang | Rohreports |
|---|---|---|
| `test` | 66 Unit-/Quarkus-HTTP-Fälle: Parser/XXE/IDs/Referenzen, JSON/null/Problem-JSON, Suche/Ranking/Facets, URL/Sanitization, Qute/HTMX, QML und optionale Provenienz | `build/test-results/test/`, `build/reports/tests/test/` |
| `quarkusBuild` | Paket und typsicheres Qute-`@CheckedTemplate` | Gradle-Konsole, `build/quarkus-app/` |
| `quarkusIntTest` | 3 `PackagedCatalogIT`-HTTP-Fälle am echten JVM-Paket | `build/test-results/quarkusIntTest/`, `build/reports/tests/quarkusIntTest/` |
| `playwrightTest` | 14 echte E2E-Fälle, Chromium **und** Firefox, 1440×900/390×844; No-JS, Assets, Tastatur, Filter, Facets, History/Deep Links, Chips, Fokus/Overflow, Fehler/XSS | `build/test-results/playwrightTest/`, `build/reports/tests/playwrightTest/`, Screenshots/JVM-Logs unter `build/reports/browser/catalog/` und `browser/filters/` |
| Runtime | gestartetes JVM-JAR, HTTP-Golden-Vertrag, Health, Snapshotlebensdauer; 8 falsche Quellen/XML-Fälle müssen vor Ready abbrechen | `build/reports/runtime/` |
| Runtime `--dev` | eigener QuarkusDev-Prozess; gezielte QWC2-CORS-Freigabe, erlaubte OPTIONS/GET und abgewiesener Origin ohne Credentials | `build/reports/runtime/` |
| Search-Index | tatsächliche Neustarts, geänderte XTF, Suche/Ranking/Facets, read-only Quelle/Arbeitsverzeichnis ohne Indexdateien, echter Indexfehlstart | `build/reports/search-index/` |
| Versionierung/Release | gültige/ungültige Laufnummern; Registryprüfung mit Fake-Antworten inkl. falschen Tags/Digests/Plattformen | `build/reports/release/` und Konsole |

`test` schliesst ausschliesslich die mit `playwright` markierten Browsertests aus; `playwrightTest` führt sie separat aus. Beide Tasks sind CI-Pflicht. `installPlaywrightBrowsers` installiert gepinnt Chromium und Firefox; auf Linux bei Bedarf `./gradlew playwrightTest -PplaywrightWithDeps=true`. Fehlende Browser sind Fehler, keine Skips. Die [historischen SODATA-Bilder](history/quarkus-migration/reference/README.md) sind Vergleichsbelege, keine aktuelle Erfolgsaussage.

Der [öffentliche Golden-Vertrag](../src/test/resources/contract/README.md) bleibt bytegleich. Der direkte HTTP-Harness **benötigt einen bereits laufenden Server** mit der Default-Harvest-Fixture:

```sh
# Erst in einem Terminal:
./gradlew quarkusDev -Dquarkus.http.port=8083
# Dann in einem zweiten Terminal:
python3 scripts/api-http-contract.py http://127.0.0.1:8083
```

Runtime-, Dev-, Index-, Browser- und Distributionsharness starten ihre Prozesse selbst und räumen sie auf. Nicht parallel auf denselben Ports/Buildverzeichnissen ausführen. Python-Assertions müssen aktiv bleiben: **kein `python -O`, `PYTHONOPTIMIZE=1`, `-x test` oder ignorierte Returncodes**. Ein negativer Source-/Indexfall ist nur bestanden, wenn der reale Prozess vor Ready mit Fehler endet; der übergeordnete Harness muss dann Exit 0 liefern. Abgeschwächte Assertions, still geschluckte Fehler oder leere Testtasks sind kein Nachweis.

## Native Image und Linux-Distribution

[Reproduzierbare Builds und Containerbefehle](operations.md#linux-container-und-jvm-native-parität). GraalVM/Mandrel 25 kompiliert Native Image; Java-21-Toolchain bleibt unverändert. `testNative` führt dieselben drei Paketverträge an einem echten Binary aus. Native/JAR getrennt bauen, getestete Artefakte und XML vor `clean` sichern. Ein macOS-Mach-O-Binary läuft nicht im Linux-Container.

```sh
GRAALVM_HOME=/pfad/zu/graalvm-25 ./gradlew testNative \
  -Dquarkus.native.enabled=true -Dquarkus.package.jar.enabled=false \
  -Dquarkus.native.native-image-xmx=4g --console=plain
mkdir -p build/native-local
cp build/*-runner build/native-local/application
./gradlew quarkusBuild quarkusIntTest
python3 scripts/distribution-contract.py \
  --native-executable "$PWD/build/native-local/application"
```

Der lokale Distributionsvertrag startet 20 Fälle (10 je Runtime), darunter geänderte XTF, Harvest, Search/QML/Cross-Layer, Neustart und vier erwartete Fehler je Runtime. Linux benötigt **separat frisch gebaute** JVM-/Native-Images des getesteten Quellstands:

```sh
python3 scripts/distribution-contract.py \
  --jvm-image kartenkatalog:local-jvm --native-image kartenkatalog:local-native \
  --expected-version 0.2.LOCALBUILD --expected-revision "$(git rev-parse HEAD)" \
  --expected-platform linux/arm64 --report-dir build/reports/distribution/linux-arm64
```

`--expected-platform` muss die tatsächlich gebaute und ausgeführte Zielarchitektur prüfen. CI verwendet Linux/amd64; ein Linux/arm64- oder macOS-Lauf beweist keinen amd64-Erfolg. Labels müssen zum echten Startup-/HTTP-Test passen. Containerprüfungen erzwingen UID1000730000:0, read-only RootFS/XTF, flüchtiges `/tmp`, keine DB-/Indexvolumes; sie inspizieren Image, Mounts, `docker diff` und Scratchdateien. Die vollständige API-/Such-/Facet-/QML-/HTML-/HTMX-Parität und Fehlstarts müssen real durchlaufen. Rohreports: `build/reports/distribution/` oder explizites `--report-dir`. Optional prüft dieselbe Browsermatrix ein lokales Native-Binary mit `-PcatalogExecutable=/abs/pfad/application`.

## Diagnose und Nachweis

Bei Fehlern zuerst Gradle-Exit und JUnit-XML prüfen: `tests`, `failures`, `errors`, `skipped` sowie Suiteidentität. Aktuell 66 Unit/HTTP, 3 JVM-Paket, 14 Browser, 3 Native-Paket; bei Änderungen tatsächliche Fälle/Assertions vergleichen statt Zahlen anzupassen. Screenshots zeigen den DOM-Zustand; Prozesslogs erklären Startup-/HTTP-/Indexfehler. Fehlendes QML ist 404, ungültige Filter 400, unbekanntes Thema 404, Suchindexausfall 503. [Betriebsdiagnose](operations.md#health-logs-und-fehlerdiagnose).

`clean` entfernt Rohreports. Für eine Abnahme SHA, cwd, Toolchain, exakte Kommandos/Exitcodes, XML und Bilder ausserhalb des Buildbaums sichern. Eigene Gegenproben verwenden temporäre Kopien: falscher JSON-Feldwert oder kaputter aktiver Dokument-/CI-Pfad muss erkannt werden; alternative XTF mit gefährlicher Notice muss escaped bleiben und API-/Native-Parität erhalten. Produktquellen und Golden-Dateien nicht für ein grünes Resultat verändern.

CI wartet mit Native auf das komplette JVM-Gate. Versionierungs-/Fake-Registrytests publizieren nichts; `verify-published-images.py` ist read-only und benötigt reale veröffentlichte Tags. [Main-Push-/Dispatch-/Mirrorwirkung](operations.md#docker-hub-veröffentlichung) vor jeder späteren Nutzerentscheidung beachten. Ein konfigurierter Job oder historischer Release ist kein frisch ausgeführter Kandidatentest.
