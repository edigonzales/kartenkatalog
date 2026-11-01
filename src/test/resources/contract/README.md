# Öffentlicher API-Vertrag

`public-api.json` schützt die öffentlichen JSON-Feldnamen, Typen, expliziten
null-Werte, leeren Arrays, Klartext-/HTML-Beschreibungen und die publizierte
QWC-Kennung gegenüber der Quell-UUID. Vier fokussierte Produkte decken die
Produkttypen der mitgelieferten unvollständigen Harvest-Fixture ab; fünf
Fehlerfälle prüfen Status, Titel, Detail, Instanzpfad und Problem-JSON.
Der SHA256 und die 169 Themen/886 Ebenen gelten ausschliesslich für diese Fixture.

`CatalogApiContractTests` prüft echte HTTP-Antworten eines Quarkus-Servers,
Katalogmitgliedschaft, de-CH-Sortierung, API-Formen, Suchgrenzen und HTML-Links.
`CatalogQmlContractTests` lädt `with-qml.xml` mit eigenem Quarkus-Testprofil und
prüft exakte QML-Bytes, MIME, Attachmentnamen und HTML-Sanitization. Die kleine
synthetische Fixture verändert den Harvest nicht.

```sh
./gradlew test
./gradlew quarkusDev -Dquarkus.http.host=127.0.0.1 -Dquarkus.http.port=8083
# In einem zweiten Terminal; braucht den bereits laufenden Server:
python3 scripts/api-http-contract.py http://127.0.0.1:8083
```

Der eigenständige HTTP-Harness verwendet dieselben unveränderten Golden-Bytes.
`runtime-process-contract.py` startet seine JVM-/Dev-Prozesse selbst und ruft
ihn über den tatsächlich laufenden Python-Interpreter auf. Python-Assertions
müssen aktiviert bleiben; kein `python -O` oder `PYTHONOPTIMIZE=1`.

Ein Mutationscanary in einer temporären Kopie muss etwa ein absichtlich falsches
`qwcLayerName` oder eine falsche Feldmenge mit Exit ungleich 0 erkennen.
Kandidatenquellen und Golden-Bytes bleiben dabei unverändert.
Die [historische Entstehung](../../../../docs/quarkus-migration/implementation/P0.md)
ist separat dokumentiert; frühere Ergebnisse gelten nur für ihre damaligen SHAs.
