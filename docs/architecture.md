# Architektur des Kartenkatalogs

## Datenfluss und Lebensdauer

Die Java-21-Anwendung verwendet Quarkus 3.40.1.1, Quarkus REST/Jackson, Qute, SmallRye Health und Lucene 10.6.0. Die XTF ist die einzige persistente Katalogquelle. Es gibt keine Datenbank, keinen Mock-Fallback, Suchserver, Dateiindex oder Reload-Endpunkt.

`CatalogStore` wird mit `@Startup` eager aufgebaut: `CatalogXtfSource.open()` öffnet ausschliesslich `file:` oder `classpath:`, `XtfCatalogParser.parse()` liest das Modell `SO_AGI_Kartenkatalog_Publikation_20251210` aus XTF 2.4. Der Parser erstellt einen `CatalogSnapshot`; dessen Konstruktor kopiert Listen und Maps unveränderlich und ruft `CatalogSearchIndexBuilder.build()` auf. Der Store hält genau diesen vollständig aufgebauten Snapshot. Ein Source-, Parser- oder Indexfehler verhindert erfolgreichen Startup/Ready. Bei `@PreDestroy` schliesst er den Snapshot und dessen Reader, Analyzer und Directory.

Der modellbezogene DOM-Parser blockiert DTD/XXE und prüft Pflichtwerte, IDs, UUIDs sowie Themenreferenzen. Er ist kein vollständiger INTERLIS-Schemavalidator. Die [mitgelieferte XTF](../data/README.md) ist eine unvollständige Entwicklungsfixture; Mengen und fehlende Metadaten sind keine Produktionsannahmen.

`CatalogSnapshot` hält Mapzugriffe nach Themenidentifier und publiziertem `ident_part`. `CatalogService` liefert de-CH-sortierte Metadaten/Details über `CatalogRepository`. Das Interface bleibt für `MockCatalogRepository` in isolierten Tests erforderlich. `CatalogSearchService` verwendet für Suchtext den Lucene-Index und löst Treffer-IDs über die Snapshot-Map auf; ohne Suchtext dienen die Snapshotlisten als Kandidaten. Filter, Sortierung und `CatalogFacetService` arbeiten auf demselben Snapshot. Detailzugriffe brauchen den Suchindex nicht; Such-/Facetpfade melden bei Indexausfall 503.

## RAM-Suche

`CatalogSearchIndexBuilder` verwendet ausschliesslich `ByteBuffersDirectory` im Heap und `GermanAnalyzer` mit leerer Stopwortliste. `CatalogDocumentMapper` erzeugt Dokumente aus validierten Ebenen/Themen. Der Index wird pro Prozessstart neu aufgebaut; kein Indexpfad, Volume, laufender Schreibzugriff oder automatisches Nachladen. Quelländerungen werden erst nach Neustart sichtbar.

Die Suche verbindet Wörter mit AND und Felder mit OR. Kennung/Titel/Keywords/Synonyme unterstützen Teilstrings, Beschreibungen deutsche Wortstämme. Normalisierung behandelt Akzente/Umlaute/ß, Interpunktion trennt Tokens. Lucene-Querysyntax wird nicht ausgeführt. Relevanzpriorität: exakte Kennung, exakter Titel, Titelpräfix, Titelteilstring, Keyword/Synonym, Thema/Organisation, Beschreibung. Gleichstände werden durch Titel/Kennung aufgelöst. Filter verwenden OR innerhalb einer Gruppe und AND zwischen Gruppen. Facets zählen unter Suchtext und jeweils den anderen Gruppenfiltern.

## REST, Qute und Sicherheitsgrenzen

`CatalogApiController` und `CatalogDtos` bilden den öffentlichen JSON-Vertrag ab. `CatalogPages` verwendet `@CheckedTemplate` und `CatalogPageVmFactory` für Qute-Seiten; Browserinteraktion verwendet lokal eingebettetes HTMX und `catalog.js`. Start-/Alias-/Themen-/Detailseiten teilen `Chrome`. Dessen optionale Provenienz ist ein gewöhnlicher String, standardmässig leer, mit Qute-Escaping. Keine Herkunfts- oder Vollständigkeitsableitung aus Dateinamen.

Beschreibungen werden durch `CatalogText`/Jsoup bereinigt, bevor sie als `RawString` in Templates gelangen. Metadaten-/Kartenlinks sind sichere HTTP(S)-URLs ohne Zugangsdaten; QML wird nur bei vorhandenem Inhalt als Attachment angeboten. `JsonProblem` und Exceptionmapper liefern 400/404/503 ohne Stacktraces. Jackson behält explizite null-Werte. CORS ist standardmässig nicht pauschal aktiviert; [gezielte lokale Freigabe](operations.md#qwc2-dev-cors).

Native Themen-`details`, lazy HTMX-Fragmente und GET-Formulare erhalten den No-JS-Fallback. Desktop-Popover und mobiles modales Filtersheet unterstützen Tastatur/Fokus; Chips, Suchtext und Mehrfachfilter bleiben im URL-Zustand. HTMX ersetzt `#catalog-state`; `Vary: HX-Request, HX-History-Restore-Request` trennt Fragmente/Vollseiten. Der DOM-Verlaufscache ist deaktiviert, Back/Reload/Deep Links laden den serverseitigen URL-Zustand. Fehler lassen bisherige Ergebnisse sichtbar.

`CatalogReadiness` prüft den vollständig geladenen Snapshot und offenen Suchindex. Liveness kommt von SmallRye Health. Native Image behält `LuceneNativeConfiguration` für dynamisch geladene Lucene-Attribute, `de-CH,en-US` für Sortierung und den HTTPS-URL-Handler für Jsoup-Linkvalidierung. Keiner dieser Hinweise bedeutet Netzwerkabruf von XTF oder Metadatenlinks. Java-21-Toolchain und GraalVM/Mandrel-25-Compiler bleiben getrennt.

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

Die API bleibt für das separate [QWC2-Plugin](https://github.com/edigonzales/qwc-kartenkatalog) kompatibel. `id` und `qwcLayerName` sind der publizierte `ident_part`; `sourceId` ist die separate Quell-UUID. Optionale Skalare werden als `null`, Keywords/Synonyme als Arrays ausgegeben. Öffentliche Theme-/Layer-Felder sind durch `src/test/resources/contract/public-api.json` geschützt. Beschreibungen verwenden weiterhin Beschreibung → Override → Modellbeschreibung; Klartext und mit Jsoup bereinigtes HTML sind getrennt. QML, Remarks und Berechtigungsrohwerte gehören nicht zum DTO.

Themenobjekte liefern zusätzlich `contact` als optionalen einzelnen `mailto:`-URI (beispielsweise `mailto:agi@bd.so.ch`). Fehlende/blanke Werte bleiben `null`; andere Schemes, mehrere Empfänger, Header und Queryparameter verhindern das Laden der Quelle. Das ist eine lokale XTF-Erweiterung; das externe INTERLIS-Modell wird nicht mitgepflegt. Die Kontaktadresse wird in den Oberflächen nicht zusätzlich angezeigt. Organisationen und Themenverantwortung stammen aus der dokumentierten [Anreicherung](../data/README.md).

Die Listen sind standardmässig nach de-CH-Titel und Kennung sortiert. `sort=title-desc` kehrt nur die Titelrichtung um; Kennungen lösen Gleichstände weiterhin A–Z auf. `sort=relevance` ist nur mit Suchtext erlaubt. Die Suche verwendet AND über Wörter und OR über Felder (Kennung, Titel, Keywords, Synonyme, Thema, Organisation, Beschreibung). Kennung/Titel/Keywords/Synonyme unterstützen Teilstrings; Beschreibungen deutsche Wortstämme. Gross-/Kleinschreibung, Akzente, Umlaute und ß/ss werden normalisiert. Interpunktion trennt Wörter; Lucene-Querysyntax wird nicht interpretiert. Mindestens ein Wort muss zwei Zeichen haben; alleinige Einzeichen-/Sonderzeichensuche ist 400. `q` bleibt auf 200 Zeichen begrenzt, auch bei Leerzeichen. Der GermanAnalyzer verwendet eine leere Stopwortliste, damit AND-Wörter nicht still verschwinden. Rangstufen: exakte Kennung > exakter Titel > Titelpräfix > Titelteilstring > Keyword/Synonym > Thema/Organisation > Beschreibung; bei Gleichstand Titel/Kennung.

`thema`, `typ`, `organisation` dürfen mehrfach vorkommen: OR innerhalb, AND zwischen Gruppen. Werte sind echte, unveränderte Quellkennungen/-werte. Unbekanntes Thema bleibt aus Kompatibilitätsgründen **404**; unbekannter Typ/Organisation und ungültige Sortierung sind **400**. Kein Treffer ist `200 []`; unbekannter Layer/fehlendes QML **404**; ausgefallener Suchindex **503**, niemals eine leere Erfolgsliste. Detail-/Metadatenabfragen lesen Maps unabhängig vom Index.

`GET /api/v1/facets` liefert `{thema: […], typ: […], organisation: […]}`. Eintrag: `{value, label, count, selected}`. Jede Gruppe zählt unter Suchtext und allen **anderen** Filtergruppen; der eigene Gruppenfilter wird beim Zählen ausgelassen. Auch 0-Treffer-Optionen behalten ihren Auswahlstatus. Organisationswerte fehlen im Harvest, deshalb ist dessen `organisation`-Array leer. Die Suchfixture mit Organisationen/Keywords/Synonymen liegt getrennt unter `src/test/resources/search/`; im Harvest wird nichts erfunden. Beispiel:

```text
/api/v1/ebenen?q=wasser&thema=harvest-hoheitsgrenzen&typ=datasetview&sort=relevance
/api/v1/facets?q=wasser&typ=datasetview
```

Fehler verwenden weiterhin `application/problem+json` mit `status`, `title`, `detail`, `instance`, ohne Stacktraces. CORS ist nicht pauschal freigeschaltet; für QWC bevorzugt same-origin Reverse Proxy. Eine gezielte, nachprüfbare lokale QWC2-Freigabe steht im [Betriebshandbuch](operations.md#qwc2-dev-cors).
