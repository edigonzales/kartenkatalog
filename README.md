# Kartenkatalog

Quarkus **3.40.1.1**, Java **21**, Gradle Wrapper **9.8.2**. Der Katalog wird beim Start aus einer lokalen INTERLIS-2.4-XTF-Datei gelesen, validiert und als unveränderlicher In-Memory-Snapshot bereitgestellt. Keine Datenbank, kein Mock-Fallback, kein Reload-Endpunkt.

Quarkus REST/Jackson, typsichere Qute-Seiten im SODATA-Stil, SmallRye Health und **Apache Lucene 10.6.0**. Der Index wird bei jedem Start aus XTF im Java-Heap (`ByteBuffersDirectory`, `GermanAnalyzer`) neu aufgebaut. Kein persistierter Suchindex oder Index-Volume. Die Startseite bietet aufklappbare Themen mit lazy geladenen Kartenebenen und No-JS-Themenlinks; Detailseiten zeigen nur vorhandene Metadaten. Suche und Mehrfachfilter arbeiten als normale GET-Form und mit HTMX-Fragmenten, URL-/Back-/Reload-Zustand, Chips, Desktop-Popovern und mobilem Filtersheet. JVM- und Native-Artefakte werden getrennt gebaut und mit echten HTTP-Prozessen auf Parität geprüft; die unabhängige Abnahme gilt ausschliesslich für den im Migrationsstatus genannten SHA.

## Start und Build

JDK 21 muss verfügbar sein; der Gradle-Wrapper bleibt unverändert.

```sh
./gradlew quarkusDev
./gradlew test quarkusBuild
java -jar build/quarkus-app/quarkus-run.jar
```

Standardport: 8080. Andere Datei/Port, z.B.:

```sh
CATALOG_XTF_LOCATION=file:/absoluter/pfad/katalog.xtf \
  java -Dquarkus.http.port=8083 -jar build/quarkus-app/quarkus-run.jar
```

`catalog.xtf-location` / `CATALOG_XTF_LOCATION` akzeptiert ausschliesslich `file:` oder `classpath:`. Default: `file:./data/kartenkatalog-harvest-ohne-qml.xtf`, relativ zum Arbeitsverzeichnis. Keine HTTP-Quelle und keine Datenbank-/Spring-Profile mehr. Für eine Änderung der XTF Anwendung neu starten. Fehlerhafte/fehlende Dateien, doppelte Kennungen/TIDs/UUIDs, ungültige Pflichtfelder, Modell-/Referenzfehler und DTD/XXE verhindern den Start. Der DOM-Leser ist modellbezogen und kein vollständiger INTERLIS-Schemavalidator.

**Datenherkunft:** Der mitgelieferte Harvest hat 169 Themen und 886 Ebenen; dies ist eine Fixture-Eigenschaft, keine Produktionsgrenze. Er ist `INCOMPLETE`, enthält synthetische UUIDs/`harvest-*`-Themen und keine Keywords, Synonyme, Organisationen, Permissions oder QML. Fehlende Metadaten werden nicht erfunden. Siehe [data/README.md](data/README.md).

## HTTP-Vertrag

| GET | Antwort |
|---|---|
| `/api/v1/catalog` | `{ "themes": [{ "theme": {…}, "layers": […] }] }` |
| `/api/v1/themen` | Themen mit Beschreibung und Ebenenanzahl |
| `/api/v1/themen/{identifier}/ebenen` | Ebenen des Themas |
| `/api/v1/ebenen` | alle Ebenen; `q` und optional `thema` |
| `/api/v1/ebenen/{id}` | einzelne Ebene per `ident_part` |
| `/api/v1/facets` | dynamische Facets; gleiche Query-/Filterparameter wie `/api/v1/ebenen` |
| `/` | vollständige Katalog-Startseite, `200 text/html` |
| `/thema` | kompatibler Alias zur Katalog-Startseite |
| `/themen/{identifier}/ebenen` | vollständige Themenseite; mit `HX-Request: true` nur Ebenenfragment |
| `/ebene/{id}` | Qute-Detailseite mit Metadaten, Themen- und sicherem Kartenlink |
| `/ebene/{id}/style` | XML/QML-Attachment, nur bei tatsächlich vorhandenem QML |
| `/ping` | `200 text/plain`, `kartenkatalog` |
| `/q/health/live`, `/q/health/ready` | SmallRye Health; Readiness setzt vollständigen Snapshot und offenen Suchindex voraus |

Die API bleibt für das separate [QWC2-Plugin](https://github.com/edigonzales/qwc-kartenkatalog) kompatibel. `id` und `qwcLayerName` sind der publizierte `ident_part`; `sourceId` ist die separate Quell-UUID. Optionale Skalare werden als `null`, Keywords/Synonyme als Arrays ausgegeben. Öffentliche Theme-/Layer-Felder sind durch `src/test/resources/contract/baseline.json` geschützt. Beschreibungen verwenden weiterhin Beschreibung → Override → Modellbeschreibung; Klartext und mit Jsoup bereinigtes HTML sind getrennt. QML, Remarks und Berechtigungsrohwerte gehören nicht zum DTO.

Die Listen sind standardmässig nach de-CH-Titel und Kennung sortiert. `sort=title-desc` kehrt nur die Titelrichtung um; Kennungen lösen Gleichstände weiterhin A–Z auf. `sort=relevance` ist nur mit Suchtext erlaubt. Die Suche verwendet AND über Wörter und OR über Felder (Kennung, Titel, Keywords, Synonyme, Thema, Organisation, Beschreibung). Kennung/Titel/Keywords/Synonyme unterstützen Teilstrings; Beschreibungen deutsche Wortstämme. Gross-/Kleinschreibung, Akzente, Umlaute und ß/ss werden normalisiert. Interpunktion trennt Wörter; Lucene-Querysyntax wird nicht interpretiert. Mindestens ein Wort muss zwei Zeichen haben; alleinige Einzeichen-/Sonderzeichensuche ist 400. `q` bleibt auf 200 Zeichen begrenzt, auch bei Leerzeichen. Der GermanAnalyzer verwendet eine leere Stopwortliste, damit AND-Wörter nicht still verschwinden. Rangstufen: exakte Kennung > exakter Titel > Titelpräfix > Titelteilstring > Keyword/Synonym > Thema/Organisation > Beschreibung; bei Gleichstand Titel/Kennung.

`thema`, `typ`, `organisation` dürfen mehrfach vorkommen: OR innerhalb, AND zwischen Gruppen. Werte sind echte, unveränderte Quellkennungen/-werte. Unbekanntes Thema bleibt aus P0-Kompatibilität **404**; unbekannter Typ/Organisation und ungültige Sortierung sind **400**. Kein Treffer ist `200 []`; unbekannter Layer/fehlendes QML **404**; ausgefallener Suchindex **503**, niemals eine leere Erfolgsliste. Detail-/Metadatenabfragen lesen Maps unabhängig vom Index.

`GET /api/v1/facets` liefert `{thema: […], typ: […], organisation: […]}`. Eintrag: `{value, label, count, selected}`. Jede Gruppe zählt unter Suchtext und allen **anderen** Filtergruppen; der eigene Gruppenfilter wird beim Zählen ausgelassen. Auch 0-Treffer-Optionen behalten ihren Auswahlstatus. Organisationswerte fehlen im Harvest, deshalb ist dessen `organisation`-Array leer. Die Suchfixture mit Organisationen/Keywords/Synonymen liegt getrennt unter `src/test/resources/search/`; im Harvest wird nichts erfunden. Beispiel:

```text
/api/v1/ebenen?q=wasser&thema=harvest-hoheitsgrenzen&typ=datasetview&sort=relevance
/api/v1/facets?q=wasser&typ=datasetview
```

Fehler verwenden weiterhin `application/problem+json` mit `status`, `title`, `detail`, `instance`, ohne Stacktraces. CORS ist nicht pauschal freigeschaltet; für QWC bevorzugt same-origin Reverse Proxy. Eine gezielte, nachprüfbare lokale QWC2-Freigabe steht im [Betriebshandbuch](docs/quarkus-migration/OPERATIONS.md#qwc2-dev-cors).

## Verifikation

```sh
./gradlew clean test quarkusBuild quarkusIntTest
./gradlew playwrightTest
./gradlew dependencies --configuration runtimeClasspath
python3 scripts/p1-process-contract.py
python3 scripts/p1-process-contract.py --dev
python3 scripts/p2-process-contract.py
```

Der P2-Prozesstest prüft Neuaufbau/Determinismus nach Neustart, XTF-Änderungen, Suche/Ranking/Filter/Facets, read-only Quelle/Arbeitsverzeichnis ohne Indexdateien und einen echten Index-Startupfehler. Er protokolliert lokale Startup-/RSS-/Suchzeitwerte unter `build/reports/p2/`. Der P1-JVM-Prozesstest startet das gebaute JAR selbst, prüft die P0-HTTP-Verträge, Health und Snapshot-Lebensdauer sowie echte Startabbrüche für ungültige Quellen. `--dev` prüft einen separaten `quarkusDev`-Prozess und die dokumentierte QWC2-CORS-Freigabe samt erlaubtem/abgewiesenem Preflight und GET. Beide räumen ihre Prozessgruppen auf; Rohlogs liegen unter `build/reports/p1/`. Der weiterhin nutzbare `scripts/p0-http-contract.py <Basis-URL>` prüft einen bereits laufenden Prozess. JUnit-Rohreports: `build/test-results/test/`; HTML: `build/reports/tests/test/`.

Die synthetische `with-qml.xml` dient nur Tests für QML und Sanitization; sie wird nicht in den Harvest gemischt. CI prüft JVM/T1–T3 sowie einen separaten Linux-Native-Build mit realen Integrationstests und read-only Containerparität. Die alten Boot-Native-/Docker-Publishing-Tasks wurden entfernt. Kein Push/Release als versteckter Buildschritt.

Migrationsplan, Status und unabhängige Abnahmen: [docs/quarkus-migration](docs/quarkus-migration/STATUS.md). Frühere Notizen: [docs/legacy-notes.md](docs/legacy-notes.md).

## Oberfläche und Browserprüfung

`/` und `/thema` liefern dieselbe Startseite. Native `details`-Themenzeilen laden ihre Ebenen einmalig als HTMX-Fragment; ohne JavaScript bleibt der normale Themenlink nutzbar. Die Themenübersicht bleibt A–Z; die Ebenen folgen der ausgewählten Titel-/Relevanzsortierung.

Die GET-Form bietet Mehrfachwahl für Thema, Produkttyp und bei echten Quellwerten Organisation. Facetzahlen berücksichtigen Suchtext und jeweils andere Kategorien. Desktop-Popover öffnen per Klick/Enter, schliessen mit Escape/Aussenklick; mobil öffnet derselbe Formularinhalt als natives modales Filtersheet mit Fokusbegrenzung und Rückkehr zum Auslöser. Ohne JS bleiben native Checkboxgruppen und Submit nutzbar. Chips entfernen genau einen Wert und erhalten andere Filter; Suchtext löschen erhält die Filter, Zurücksetzen entfernt die gesamte Auswahl. Keine Datums-/Dummyfilter.

HTMX ersetzt bei Suche/Filter/Sortierung nur `#catalog-state`, aktualisiert die URL und lädt weiterhin passende geöffnete Themen neu. `Vary: HX-Request, HX-History-Restore-Request` unterscheidet das Fragment von der Vollseite. Der HTMX-DOM-Verlaufscache ist deaktiviert: Browser-Back lädt den gültigen Zustand aus der jeweiligen URL, statt noch nicht angewendete Formfelder aus einem Snapshot wiederherzustellen. Reload und kopierte URLs bleiben serverseitig vollständig nutzbar. Bei HTTP-/Netzfehlern bleiben bisherige Ergebnisse und ein sichtbarer Fehlerhinweis erhalten.

Metadatenlinks sind auf absolute HTTP(S)-URLs ohne Zugangsdaten begrenzt; Beschreibungs-HTML wird vor Qute-RawString mit Jsoup saniert. QML und optionale Felder werden nur mit echtem Quellinhalt angezeigt.

`./gradlew playwrightTest` installiert bei Bedarf die gepinnten Chromium- und Firefox-Versionen für Playwright **1.60.0** (Task `installPlaywrightBrowsers`). Beide Engines prüfen reale, selbst gestartete JVM-Prozesse: Harvest sowie getrennte Such-/Organisationsfixture, 1440×900 und 390×844, lokale Assets, Tastatur/Skip-Link, Aufklappen, No-JS, Mehrfachfilter/Facets/JSON-Parität, Back/Reload/Deep-Link, Reset/Chips, Popover/Sheet/Fokus/Overflow, Fehler und sichere HTML-Ausgabe. Fehlende Browser verursachen einen Fehler, keinen Skip.

Browserberichte: `build/reports/tests/playwrightTest/`; Screenshots und JVM-Logs unter `build/reports/p3a/` (kumulative Basis) und `build/reports/p3b/` (Filtermatrix). Der normale `test`-Task prüft alle Unit-/HTTP-Tests; CI ruft **beide** Pflichttasks auf und installiert Linux-Systembibliotheken mit `-PplaywrightWithDeps=true`. `clean` ersetzt diese Rohreports. Herkunft und Reproduktion der tatsächlichen Referenzbilder: [docs/quarkus-migration/reference](docs/quarkus-migration/reference/README.md).


## Native Image und Betrieb

Java-21-Toolchain bleibt unverändert. **GraalVM/Mandrel25** ist als Native-Compiler für Quarkus3.40.1.1 erforderlich; GraalVM21 wird vom Plugin abgewiesen. `quarkusIntTest` prüft das tatsächliche JVM-Paket, `testNative` dieselben HTTP-Verträge an einem echten Native-Binary. Native-Paketwahl: `-Dquarkus.native.enabled=true -Dquarkus.package.jar.enabled=false`; JVM separat ohne diese Optionen bauen.

Vollständige reproduzierbare Befehle, Compiler-/Runtime-Digests, Linux-/Architekturgrenzen, read-only XTF-Mount, OpenShift-Vorlage, Health/Restart und Fehlerdiagnose: [Betriebshandbuch](docs/quarkus-migration/OPERATIONS.md). Das Handbuch erläutert auch den durch einen echten Native-Startfehler begründeten gezielten Lucene-Reflection-Hint; kein Versionsdowngrade, kein Dateisystemindex.

`scripts/p4-distribution-contract.py --native-executable /abs/pfad/application` prüft getrennte lokale JVM-/Native-Prozesse. `--jvm-image <image> --native-image <image>` prüft echte Linux-Container mit beliebiger UID, read-only RootFS/Daten und ohne Indexvolume. Beide prüfen identische JSON-Payloads, Treffer-/Ranking-/Facet-Parität, Neustarts, geänderte XTF, HTML/HTMX/exakte QML-Bodies, einen Cross-Layer-Fall mit fehlenden Metadaten und Mehrfachfiltern sowie fehlschlagenden Start bei XML-/XXE-/Referenz-/Indexfehlern. Rohbelege: `build/reports/p4/`.

Die Images unter `src/main/docker/` verwenden UBI9 und laufen ohne Root; `deploy/openshift.yaml` ist eine anzupassende Vorlage, kein bereits ausgeführtes Clusterdeployment. Kein automatischer Push/Release. Native-/Linux-Nachweise und tatsächlich geprüfte Architektur stehen im [P4-Implementierungsbericht](docs/quarkus-migration/implementation/P4.md) und im unabhängigen Reviewbericht.
