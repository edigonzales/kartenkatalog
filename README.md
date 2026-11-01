# Kartenkatalog

Der Kartenkatalog liest beim Start eine lokale INTERLIS-2.4-XTF und bietet eine such- und filterbare SODATA-Oberfläche sowie die JSON-API für das separate [QWC2-Plugin](https://github.com/edigonzales/qwc-kartenkatalog).

Java **21**, Quarkus **3.40.1.1**, Gradle Wrapper **9.8.2**, Lucene **10.6.0**, Qute und HTMX. Ein unveränderlicher Snapshot mit `ByteBuffersDirectory`/`GermanAnalyzer` liegt im RAM. Keine Datenbank, kein persistierter Suchindex, kein Mock-Fallback und kein Reload-Endpunkt.

## Lokal starten

Mit JDK 21 im Repository:

```sh
./gradlew quarkusDev
```

Oberfläche: [http://localhost:8080/](http://localhost:8080/). JVM-Paket alternativ:

```sh
./gradlew test quarkusBuild quarkusIntTest
CATALOG_XTF_LOCATION=file:/absoluter/pfad/catalog.xtf \
  java -jar build/quarkus-app/quarkus-run.jar
```

Ohne Override gilt `file:./data/kartenkatalog-harvest-ohne-qml.xtf`, relativ zum Arbeitsverzeichnis. Nur `file:` und `classpath:` sind erlaubt. Fehlende/ungültige Quellen verhindern den Start; Quelländerungen erfordern Neustart.

**Die mitgelieferte Fixture ist INCOMPLETE.** Ihre 169 Themen/886 Ebenen sind keine Produktionsgrenze. Synthetische Kennungen sind keine originalen SIMI-Schlüssel; Metadaten werden nicht erfunden. [Datenherkunft und Hash](data/README.md).

`CATALOG_PROVENANCE_NOTICE` / `catalog.provenance-notice` ist optional und standardmässig leer. Ohne explizite Konfiguration kein Provenienzabsatz; gesetzter Text erscheint auf allen Vollseiten HTML-escaped. Beispiel: `CATALOG_PROVENANCE_NOTICE='Datenhinweis: Der mitgelieferte Harvest ist unvollständig.'`. Der XTF-Dateiname begründet keine Vollständigkeitsannahme.

## Oberfläche und API

`/` und `/thema` zeigen Themen, lazy geladene Ebenen, Suche, Mehrfachfilter, dynamische Facets und Chips. GET-Formulare/Themenlinks funktionieren ohne JavaScript. Browser-History, Reload und kopierte URLs erhalten den Zustand; Desktop-Popover und mobiles Filtersheet unterstützen Tastatur/Fokus. Die Live-Suche startet ab drei Zeichen nach 300 ms Tipp-Pause; kürzere Eingaben wirken wie eine leere Suche. Leeren aktualisiert sofort. Sortierung wird direkt bei Auswahl angewendet, Checkbox-Filter weiterhin mit „Anwenden“. Ohne JavaScript stehen GET-Submit-Buttons bereit. Frutiger wird in vier Schnitten lokal als WOFF2 geladen; UI-Icons sind eingebettete Bootstrap Icons (MIT). Details unter `/ebene/{id}`, QML bei vorhandenem Inhalt unter `/ebene/{id}/style`.

Öffentliche GET-Endpunkte: `/api/v1/catalog`, `/api/v1/themen`, `/api/v1/themen/{identifier}/ebenen`, `/api/v1/ebenen`, `/api/v1/ebenen/{id}`, `/api/v1/facets`. `id == qwcLayerName` ist der publizierte `ident_part`, `sourceId` die Quell-UUID. JSON-Feldnamen, null/Arrays, Suche/Filter/Sortierung und Problem-JSON sind geschützt. [HTTP-Vertrag und Architektur](docs/architecture.md#http-vertrag).

## Tests, Betrieb und Veröffentlichung

```sh
./gradlew clean test quarkusBuild quarkusIntTest
./gradlew playwrightTest
python3 scripts/runtime-process-contract.py
python3 scripts/runtime-process-contract.py --dev
python3 scripts/search-index-contract.py
python3 scripts/versioning-contract.py
python3 scripts/release-contract.py
```

[Testmatrix und Rohreports](docs/testing.md), [JVM-/Native-/Linux-/OpenShift-Betrieb](docs/operations.md), [Architektur und Sicherheitsgrenzen](docs/architecture.md). GraalVM/Mandrel **25** kompiliert Native Image bei unveränderter Java-21-Toolchain. JVM- und Native-Artefakte separat bauen und wirklich starten; Architektur eines getesteten Binaries/Image ausdrücklich prüfen.

Die Releasepipeline baut Linux/amd64 und veröffentlicht nach JVM-, Browser-, Prozess-, Native- und Container-Gates **acht Tags** unter `sogis/kartenkatalog`: Native `0.2.N`, `latest`, `0`, `0.2`; JVM jeweils mit `-jvm`. N ist die GitHub-Laufnummer, lokal gilt `0.2.LOCALBUILD`. Ein erfolgreicher **Push auf main oder manueller Dispatch auf main publiziert automatisch**; Pull Requests nicht. Reine Markdown-Pushes sind im Hauptworkflow ausgenommen. Der Mirror-Workflow spiegelt Pushes nach Codeberg. Lokale Builds/Commits veröffentlichen nichts. [Releasebedingungen und Pull-/Startbefehle](docs/operations.md#docker-hub-veröffentlichung).

## Dokumentation und Nachweise

- [Architektur](docs/architecture.md), [Betrieb](docs/operations.md), [Tests](docs/testing.md).
- [Historisches Archiv mit Herkunft und Pfadzuordnung](docs/history/README.md): frühere Implementierungen, unabhängige Reviews und SODATA-Referenzbilder; deren Aussagen gelten ausschliesslich für ihre damaligen SHAs.
- [Cleanup-Status und unabhängige Abnahmen](docs/cleanup/STATUS.md).
