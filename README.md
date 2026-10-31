# Kartenkatalog

Spring-Boot-Anwendung für Themen und Kartenebenen. Die bestehende HTML-/HTMX-Oberfläche und die JSON-API nutzen denselben `CatalogService`. Die QWC-App und ihr künftiges Plugin liegen separat in [qwc-kartenkatalog](https://github.com/edigonzales/qwc-kartenkatalog).

## Lokal mit dem gelieferten XTF starten

Voraussetzungen: **JDK 21** für die Anwendung und Gradle-Wrapper. Der Wrapper unterstützt auch JDK 25 zum Ausführen des Builds; die Java-21-Toolchain muss dann ebenfalls installiert sein. Keine Datenbank erforderlich:

```sh
./gradlew bootRun --args='--spring.profiles.active=xtf --server.address=127.0.0.1 --server.port=8083'
```

- Oberfläche: [Themen](http://127.0.0.1:8083/thema)
- API: [vollständiger Katalog](http://127.0.0.1:8083/api/v1/catalog)
- Beispiel: [Gemeindegrenzen](http://127.0.0.1:8083/api/v1/ebenen/ch.so.agi.gemeindegrenzen)

Das Profil `xtf` liest `data/kartenkatalog-harvest-ohne-qml.xtf` beim Start als unveränderlichen Snapshot. Eine andere Datei kann mit `CATALOG_XTF_LOCATION=file:/absoluter/pfad/datei.xtf` gewählt werden. Nach Austausch der Datei neu starten. Ungültige Dateien, doppelte fachliche Kennungen und defekte Themenreferenzen lassen den Start mit einer Fehlermeldung abbrechen; es gibt keinen stillen Mock-Fallback. Das ist ein gezielter Leser für das angehängte INTERLIS-2.4-Publikationsmodell, kein allgemeiner INTERLIS-Validator.

**Datenstand:** 169 Themen, 886 Kartenebenen. Der Transfer ist ausdrücklich eine unvollständige Rekonstruktion aus öffentlichen QWC-Diensten. `harvest-*`-Themenkennungen und UUIDs sind teilweise synthetisch; QML, Schlagwörter, Synonyme und Berechtigungsangaben fehlen in diesem Datenstand. Fehlende Angaben werden nicht erfunden. Siehe [Datenherkunft](data/README.md).

## JSON-API v1

| GET-Endpunkt | Antwort |
| --- | --- |
| `/api/v1/catalog` | `{ "themes": [{ "theme": {…}, "layers": […] }] }` |
| `/api/v1/themen` | Themen mit Beschreibung und Anzahl Kartenebenen |
| `/api/v1/themen/{identifier}/ebenen` | Kartenebenen eines fachlichen Themas |
| `/api/v1/ebenen` | Alle Kartenebenen |
| `/api/v1/ebenen?q=wasser&thema=harvest-hoheitsgrenzen` | Suche und optionaler Themenfilter |
| `/api/v1/ebenen/{identPart}` | Metadaten einer Kartenebene |

Die Suche ignoriert Gross-/Kleinschreibung und Akzente, entfernt äussere Leerzeichen und durchsucht Kennung, Titel, Beschreibung, Schlagwörter und Synonyme als Teilzeichenfolge. `q` darf maximal 200 Zeichen enthalten. Leere Suchresultate sind `[]`; unbekannte Themen/Kartenebenen und nicht vorhandene QML-Dateien ergeben **404**, ungültige Suchparameter **400**. API-Fehler werden als `application/problem+json` ausgegeben. Listen sind deterministisch nach deutschen Titeln (de-CH), bei Gleichstand nach Kennung sortiert. Der vollständige Katalog benötigt zwei Repository-Abfragen statt einer SQL-Abfrage je Thema.

### Felder

Themen: `identifier`, `title`, `description`, `descriptionHtml`, `keywords`, `synonyms`, `furtherInfoUrl`, `dataOwner`, `layerCount`.

Kartenebenen: `id`, `sourceId`, `type`, `title`, `description`, `descriptionHtml`, `keywords`, `synonyms`, `qwcLayerName`, `derivedIdentifier`, `transparency`, `themeIdentifier`, `themeTitle`, `organization`, `publicationScope`, `downloadAvailable`.

- `id` und `qwcLayerName` sind die vorhandene fachlich-technische Kennung `ident_part`, z.B. `ch.so.agi.gemeindegrenzen`. `sourceId` ist die separate Quell-ID; beim Harvest ist sie synthetisch.
- `type` erhält den Quellwert (`datasetview`, `layergroup`, `facadelayer`, `extwms` im gelieferten XTF). `transparency` ist der vorhandene Prozentwert 0–100; fehlende Werte bleiben `null`.
- `themeIdentifier` kommt aus der tatsächlichen Themenreferenz, nicht aus einem eventuell fehlenden oder abweichenden redundanten Quellfeld.
- Listen für Schlagwörter/Synonyme sind nie `null`; optionale skalare Felder dürfen `null` sein. Beschreibungen verwenden die bestehende Reihenfolge: explizite Beschreibung, Override, Modellbeschreibung; leere Werte werden übersprungen.
- `description` ist Text; `descriptionHtml` ist mit jsoup auf erlaubte Formatierung und Links bereinigt. Interne Bemerkungen, Berechtigungs-Rohdaten und QML werden nicht als öffentliche DTO-Felder serialisiert.

Die API liefert Metadaten, keine WMS-Endpunkte und keine Zusicherung der aktuellen QWC-Verfügbarkeit. Das QWC-Plugin löst Kennungen über seine vorhandene Layerverwaltung und den Datenprodukt-Service auf. Die effektive Geodienst-Berechtigung bleibt beim QWC-/GIS-Backend. CORS ist nicht pauschal freigeschaltet; die spätere QWC-Anbindung verwendet einen gezielten Proxy oder eine passende Deployment-Konfiguration.

## PostgreSQL und Profile

Ohne `xtf` oder `mock` verwendet die App weiterhin `JdbcCatalogRepository` mit dem bestehenden Schema `agi_kartenkatalog_v2` und den Tabellen `kartenkatalog_thema`/`kartenkatalog_ebene`. Es gibt keine automatische Migration oder Änderung bestehender Daten.

- `DBURL`: Standard `jdbc:postgresql://localhost:54321/edit`
- `DBUSR`, `DBPWD`: lokale Standardwerte `ddluser`
- `HIKARI_MAX_POOL_SIZE`: Standard `2`
- `mock`: kleine synthetische Beispieldaten für isolierte Tests.
- `prod`: bestehendes Deployment-Profil; verwendet JDBC. Alle Profile nutzen vorab kompilierte jte-Templates.

`xtf` und `mock` sind alternative Datenquellen und dürfen nicht kombiniert werden. Das XTF wird nicht automatisch in die produktive Datenbank importiert und ist kein vollständiger Ersatz eines SIMI-Exports.

## Build und Tests

```sh
./gradlew clean check bootJar
java -jar build/libs/kartenkatalog-0.0.LOCALBUILD.jar --spring.profiles.active=xtf --server.port=8083
```

Aktualisiert auf Spring Boot **4.1.2**, jte **3.2.4** mit Boot-4-Starter, Gradle **9.8.2**, Native Build Tools **1.1.18** und jsoup **1.23.2**. PostgreSQL, H2 und Spring-Abhängigkeiten werden durch das Spring-Boot-BOM abgestimmt. Browser-Bibliotheken sind ebenfalls aktualisiert und auf konkrete Versionen festgelegt.

Die Tests prüfen die vollständige XTF-Datei, JSON-Verträge, Suche und Fehlerfälle, Themenreferenzen und Zählwerte, HTML-Rendering, HTML-Bereinigung sowie JDBC-Record-Mapping und parametrisierte SQL-Abfragen mit H2 im PostgreSQL-Modus. H2 ersetzt keinen Integrationstest gegen das produktive PostgreSQL-Schema.

### Native Image

Boot 4 benötigt **GraalVM 25+**. Das Laufzeitprofil muss beim AOT-Build feststehen:

```sh
JAVA_HOME=/pfad/zu/graalvm25 ./gradlew nativeCompile -PaotProfiles=xtf
./build/native/nativeCompile/kartenkatalog --spring.profiles.active=xtf --server.port=8083
```

Native-Builds sind standardmässig auf 4 GiB Heap und zwei Threads begrenzt. `-PnativeMaxHeap=8g -PnativeParallelism=4` erhöht die Grenzen; `-PnativeQuickBuild` verwendet die schnellere Entwicklungsoptimierung `-Ob`.

Ohne `-PaotProfiles` baut die bestehende Pipeline das JDBC-Profil (`default`). Ein so erzeugtes Native Image kann nicht nachträglich auf die XTF-Datenquelle umgeschaltet werden. Die XTF-Datei bleibt extern und wird relativ zum Arbeitsverzeichnis oder über `CATALOG_XTF_LOCATION` gefunden. CI führt die JVM-Tests vor der Native-Kompilation aus. `buildNativeDockerImage` baut lokal; nur `publishNativeDockerImage` veröffentlicht ein Image. Beide Tasks hängen von einer aktuellen Native-Kompilation ab. Das bestehende Dockerfile erwartet ein Linux-amd64-Binary; lokale macOS-Binaries dürfen dafür nicht verwendet werden.

Frühere Arbeitsnotizen sind unter [Legacy-Notizen](docs/legacy-notes.md) erhalten; sie beschreiben teilweise ältere Modelle und gelten nicht als aktuelle Startanleitung.

## Verifikation am 30.10.2026

17 Tests bestehen. Das ausführbare JVM-JAR läuft im XTF-Profil auf Java 25 (die Anwendung und Tests werden mit der Java-21-Toolchain kompiliert/ausgeführt). Zusätzlich wurde ein Native Image mit GraalVM 25 und `-PnativeQuickBuild` gebaut und gestartet: Der vollständige Katalog ist einschliesslich Sortierung und aller Metadaten identisch zur JVM-Antwort; Detailabruf und beide jte-Seiten liefern HTTP 200. Die HTML-Themenansicht und das Nachladen eines Themas wurden ebenfalls im Browser geprüft. Der Docker-Registry-Push und das produktive PostgreSQL wurden nicht ausgeführt.
