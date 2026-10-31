# Kartenkatalog

Quarkus **3.40.1.1**, Java **21**, Gradle Wrapper **9.8.2**. Der Katalog wird beim Start aus einer lokalen INTERLIS-2.4-XTF-Datei gelesen, validiert und als unveränderlicher In-Memory-Snapshot bereitgestellt. Keine Datenbank, kein Mock-Fallback, kein Reload-Endpunkt.

Aktueller Migrationsabschnitt **P1**: Quarkus REST/Jackson, minimale typsichere Qute-Seiten und SmallRye Health. Die bisherige lineare Teilstringsuche bleibt vorerst erhalten. Lucene, die neue SODATA-Oberfläche und Native-/Linux-Abnahme folgen in den separat geprüften Lieferungen P2–P4. Der aktuelle Stand behauptet keine Native-Freigabe.

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
| `/` | Weiterleitung nach `/thema` |
| `/thema` | vollständige Themenliste |
| `/themen/{identifier}/ebenen` | vollständige Themenseite; mit `HX-Request: true` nur Ebenenfragment |
| `/ebene/{id}` | minimale Qute-Detailseite mit sicherem Kartenlink |
| `/ebene/{id}/style` | XML/QML-Attachment, nur bei tatsächlich vorhandenem QML |
| `/ping` | `200 text/plain`, `kartenkatalog` |
| `/q/health/live`, `/q/health/ready` | SmallRye Health; Readiness setzt vollständigen Snapshot voraus |

Die API bleibt für das separate [QWC2-Plugin](https://github.com/edigonzales/qwc-kartenkatalog) kompatibel. `id` und `qwcLayerName` sind der publizierte `ident_part`; `sourceId` ist die separate Quell-UUID. Optionale Skalare werden als `null`, Keywords/Synonyme als Arrays ausgegeben. Öffentliche Theme-/Layer-Felder sind durch `src/test/resources/contract/baseline.json` geschützt. Beschreibungen verwenden weiterhin Beschreibung → Override → Modellbeschreibung; Klartext und mit Jsoup bereinigtes HTML sind getrennt. QML, Remarks und Berechtigungsrohwerte gehören nicht zum DTO.

Die Listen sind nach de-CH-Titel und Kennung sortiert. Die P1-Suche trimmt Leerzeichen, ignoriert Gross-/Kleinschreibung und Akzente und sucht einen Teilstring über Kennung, Titel, Beschreibung, Keywords und Synonyme. `q` hat maximal 200 Zeichen. Kein Treffer: `200 []`; unbekanntes Thema/Layer oder fehlendes QML: **404**; zu langer Suchtext: **400**. Fehler verwenden `application/problem+json` mit `status`, `title`, `detail`, `instance`; keine Stacktraces. CORS ist nicht pauschal freigeschaltet; für QWC bevorzugt same-origin Reverse Proxy.

## Verifikation

```sh
./gradlew clean test quarkusBuild
./gradlew dependencies --configuration runtimeClasspath
python3 scripts/p1-process-contract.py
python3 scripts/p1-process-contract.py --dev
```

Der JVM-Prozesstest startet das gebaute JAR selbst, prüft die P0-HTTP-Verträge, Health und Snapshot-Lebensdauer sowie echte Startabbrüche für ungültige Quellen. `--dev` prüft einen separaten `quarkusDev`-Prozess. Beide räumen ihre Prozessgruppen auf; Rohlogs liegen unter `build/reports/p1/`. Der weiterhin nutzbare `scripts/p0-http-contract.py <Basis-URL>` prüft einen bereits laufenden Prozess. JUnit-Rohreports: `build/test-results/test/`; HTML: `build/reports/tests/test/`.

Die synthetische `with-qml.xml` dient nur Tests für QML und Sanitization; sie wird nicht in den Harvest gemischt. CI prüft aktuell JVM/Tests; Linux-Native und Container werden in P4 eingeführt. Die alten Boot-Native-/Docker-Publishing-Tasks wurden entfernt. Kein Push/Release als versteckter Buildschritt.

Migrationsplan, Status und unabhängige Abnahmen: [docs/quarkus-migration](docs/quarkus-migration/STATUS.md). Frühere Notizen: [docs/legacy-notes.md](docs/legacy-notes.md).
