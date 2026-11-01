# Quarkus-Kartenkatalog betreiben und prüfen

## Artefakte und Voraussetzungen

Anwendung: **Java 21**, Quarkus **3.40.1.1**, Gradle **9.8.2**, Lucene **10.6.0** (`lucene-core`, `lucene-analysis-common`). Native-Kompilierung für diese Quarkus-Version benötigt **GraalVM/Mandrel 25**; GraalVM 21 wird von Quarkus ausdrücklich zurückgewiesen. Das ändert weder Java-21-Toolchain noch Bytecode-Target. Linux-Mandrel-Builder und Runtime-Basisimages sind in CI/Dockerfiles mit Digest gepinnt. Ein macOS-Mach-O-Binary kann nicht in einem Linux-Container laufen.

- JDK21 und bei lokalem Native-Build zusätzlich GraalVM25 mit `native-image` und C-Toolchain.
- Für Linux-Build/Prüfung Docker oder ein entsprechend konfiguriertes Podman. Die folgenden tatsächlich genutzten Befehle verwenden Docker.
- Native-Build benötigt mindestens ca. 4GiB Compilerheap plus Build-/Container-Overhead; keine parallelen Native-Builds in einer kleinen VM. Im lokalen Bericht stehen echte Build-/Runtime-Messwerte.
- Die getestete lokale Linux-Architektur und unabhängige Nachweise stehen in `implementation/P5.md`/`reviews/P5-review.md`. Für eine andere Zielarchitektur erneut bauen **und dort ausführen**, keinen macOS-/arm64-Test als amd64-Nachweis ausgeben. Der Release-Workflow baut und testet Linux/amd64. Lokale Builds pushen nicht automatisch; Main-Push/Workflow-Dispatch auf Main veröffentlicht nach den JVM-/Browser-/Native-/Container-Gates die unten beschriebenen Docker-Hub-Tags.

Offizielle Grundlagen: [Quarkus Native](https://quarkus.io/guides/building-native-image/), [Gradle/Integrationstests](https://quarkus.io/guides/gradle-tooling/). Die tatsächliche Version 3.40.1.1 verlangt getrennte Paketwahl: Native mit `-Dquarkus.native.enabled=true -Dquarkus.package.jar.enabled=false`; JVM ohne diese beiden Optionen. Ein gleichzeitiges Native-/JAR-Paket lehnt das Plugin ab.

## Daten und Start

Die XTF ist die einzige persistente Katalogquelle. Kein DB-Secret, kein PostgreSQL und **kein Index-Volume**. File-Mount bevorzugt; `classpath:` nur für bewusst gebündelte Ressourcen. Kein HTTP-Abruf/Reload. Die mitgelieferte Harvest-XTF ist unvollständig, die 169/886-Counts gelten nur für diese Fixture; Organisationen, Keywords, Synonyme, Permissions und QML nicht erfinden.

```sh
./gradlew quarkusDev
./gradlew test quarkusBuild quarkusIntTest
CATALOG_XTF_LOCATION=file:/absoluter/pfad/catalog.xtf \
  java -jar build/quarkus-app/quarkus-run.jar
```

Native lokal (Beispielpfad durch eigene Installation ersetzen, JDK21 bleibt Gradle-/Compiler-Toolchain):

```sh
GRAALVM_HOME=/pfad/zu/graalvm-25 ./gradlew testNative \
  -Dquarkus.native.enabled=true -Dquarkus.package.jar.enabled=false \
  -Dquarkus.native.native-image-xmx=4g
CATALOG_XTF_LOCATION=file:/absoluter/pfad/catalog.xtf \
  ./build/kartenkatalog-0.2.LOCALBUILD-runner
```

`gradle/versioning.gradle` setzt lokal `0.2.LOCALBUILD` und in GitHub Actions ausschliesslich `0.2.<GITHUB_RUN_NUMBER>`; alte Jenkins-/Travis-Buildnummern werden ignoriert; den tatsächlich gebauten `build/*-runner` verwenden. `testNative` führt dieselben drei `PackagedCatalogIT`-HTTP-Verträge wie `quarkusIntTest` am echten gebauten Artefakt aus, nicht am In-Process-Testcontainer. Zusätzlich ist der separate Paritäts-/Startfehlertest verbindlich:

```sh
# Native-Binary vor einem weiteren Build ausdrücklich an eigenem Artefaktpfad sichern:
mkdir -p build/native-local
cp build/*-runner build/native-local/application
# Aktuelles JVM-JAR erzeugen:
./gradlew quarkusBuild quarkusIntTest
python3 scripts/distribution-contract.py \
  --native-executable "$PWD/build/native-local/application"
# Optional auch die gesamte Browsermatrix am echten lokalen Native-Prozess:
./gradlew playwrightTest -PcatalogExecutable="$PWD/build/native-local/application"
```

`clean` entfernt beide Artefakte und Rohberichte. Nach **jeder** Produkt-/Buildänderung die nativen Artefakte neu bauen; ein altes Binary ist kein Nachweis für neue Quellen. Die Browser-Option ist explizit: ohne `catalogExecutable` testen dieselben 14 Fälle den selbst gestarteten JVM-Prozess, mit Option das angegebene echte Native-Binary. Kein automatischer Fallback.

## Linux-Container und JVM-/Native-Parität

Zuerst aktuelles JVM-Artefakt samt Pflichtgates, dann JVM-Image sichern, **danach** natives Paket bauen:

```sh
./gradlew clean test quarkusBuild quarkusIntTest playwrightTest
# Auf Linux bei Bedarf: -PplaywrightWithDeps=true

docker build -f src/main/docker/Dockerfile.jvm -t kartenkatalog:local-jvm .
./gradlew quarkusBuild \
  -Dquarkus.native.enabled=true -Dquarkus.package.jar.enabled=false \
  -Dquarkus.native.container-build=true -Dquarkus.native.container-runtime=docker \
  -Dquarkus.native.builder-image=quay.io/quarkus/ubi9-quarkus-mandrel-builder-image@sha256:29a5078c94489623ef7e554aad2cbff70a4ff872ac6d60a46f5538697ed82a30 \
  -Dquarkus.native.native-image-xmx=4g
# Auf einem passenden Linux-Host stattdessen testNative für zusätzliche Integrationstests.
docker build -f src/main/docker/Dockerfile.native -t kartenkatalog:local-native .
python3 scripts/distribution-contract.py \
  --jvm-image kartenkatalog:local-jvm --native-image kartenkatalog:local-native
```

Der Test startet **echte getrennte Container** als UID `1000730000:0`, mit read-only RootFS und read-only Daten-Bindmount, ohne Capabilities/Privilege-Escalation oder Indexvolume. Ein auf 64MiB begrenztes flüchtiges `/tmp`-tmpfs dient dem Vert.x-Ressourcencache; sein Inhalt wird separat kopiert und auf Lucene-Dateien geprüft. Er prüft Health, alle fünf API-Formen, HTML-/HTMX-Fragmente, QML/404, gleiche JSON-Payloads, exakte QML-Bodies und Ranking/Facets für Harvest und Kleinfixtures, Neustartdeterminismus und geänderte XTF. Eine separate Abschlussfixture prüft XTF → Suchtext über Titel/Beschreibung/Synonym → Mehrfachfilter → JSON/Facets → Vollseite/HTMX/Metadatendetail, auch mit fehlender Organisation/QML und unsicherem Beschreibungs-HTML. Defekt-XML, XXE, defekte Referenz und echter Lucene-Indexaufbaufehler müssen beide Laufzeiten vor Ready beenden. Er prüft Docker-Mount-/User-/RootFS-Inspektion und `docker diff`, native Logs mit Lucene-Version/Speicherklasse, RSS/Start-/Index-/Suchzeit und räumt eigene Container auf. Rohbelege: `build/reports/distribution/`. Keine vorhandenen Container/Volumes werden gelöscht.

Einzelner Betrieb (XTF-Pfad absolut; bewusst Linux-native Image):

```sh
docker run --rm --name kartenkatalog -p 127.0.0.1:8080:8080 \
  --read-only --user 1000730000:0 --cap-drop=ALL \
  --security-opt=no-new-privileges --memory=512m \
  --tmpfs /tmp:rw,noexec,nosuid,size=64m,mode=1777 \
  --mount type=bind,source=/absoluter/pfad/catalog.xtf,target=/catalog/catalog.xtf,readonly \
  -e CATALOG_XTF_LOCATION=file:/catalog/catalog.xtf \
  kartenkatalog:local-native
```

Daten müssen für die effektive UID/Gruppe lesbar und Elternverzeichnisse durchsuchbar sein. Read-only Mount verhindert Änderungen durch die Anwendung; Quelldateien ausserhalb des Prozesses aktualisieren und Deployment neu starten. Kein administrativer Reload-Endpunkt und keine Index-Synchronisation.

## OpenShift

`deploy/openshift.yaml` ist eine **Vorlage**, keine Behauptung eines durchgeführten Clusterdeployments. Vor Anwendung Image auf das geprüfte Linux-Image der Nodearchitektur setzen und vorhandene PVC `kartenkatalog-xtf` mit `catalog.xtf` bereitstellen. Die PVC enthält nur die Quelldatei, keinen Lucene-Index. `/tmp` verwendet ein begrenztes `emptyDir` im RAM für Vert.x, kein persistentes Volume. Keine produktiven Clusteränderungen werden durch Tests oder CI ausgeführt.

OpenShift vergibt die UID; im Manifest keine feste `runAsUser` erzwingen. Die Images sind ohne Root ausgelegt und mit beliebiger UID/Gruppe0 getestet. `runAsNonRoot`, `readOnlyRootFilesystem`, `allowPrivilegeEscalation:false`, Capabilities `ALL` entfernt, Seccomp `RuntimeDefault`. Startup-Probe wartet auf `/q/health/ready`, Readiness auf vollständigen Snapshot/offenen Index, Liveness auf `/q/health/live`. Serviceport80 → Container8080. Route/TLS/Ingress und Ressourcenlimits an Clusteranforderungen anpassen.

Neustartablauf: neue valide XTF bereitstellen → Deployment-Rollout → Startup/Ready abwarten → reale Suche prüfen. Fehlerhafte XTF verhindert Startup/Ready und darf nicht durch Mock-/DB-Fallback verschleiert werden. Bei Replikaten Quelle/Version je Rollout konsistent bereitstellen; kein gemeinsam geschriebener Index nötig.

## Health, Logs und Fehlerdiagnose

- `/ping` bleibt kompatibel. `/q/health/live` meldet den lebenden Prozess; `/q/health/ready` nur bei fertig geladenem Snapshot und verfügbarem Lucene-Index. Defekte Quelle/Index beendet den Start, statt `UP` oder leere Treffer vorzutäuschen.
- Startup-Log nennt Themen-/Dokumentzahl, Quelle, Lucene-Version, `ByteBuffersDirectory`/`GermanAnalyzer`, Index-/Katalogzeit und belegten Heap. Kein XTF-Payload oder Secret erforderlich. Keine DB-Konfiguration/Secrets.
- Read-only RootFS braucht ein flüchtiges `/tmp` für Vert.x (`Unable to create folder …/vertx-cache` wurde im echten Container beobachtet). Das ist kein Lucene-Indexpfad; der Test inventarisiert die Scratch-Dateien und verwirft Lucene-Segment-/Lockdateien.
- `Cannot load catalog XTF`: Pfad/Dateirechte, Namespace/Modell/Referenzen/duplizierte IDs/XXE prüfen. `Cannot build catalog RAM index`: Indexursache im Fehlerstack prüfen, nicht auf FSDirectory/MMapDirectory umstellen.
- Beobachteter Native-Fehler `ClassNotFoundException: …KeywordAttributeImpl`: gezielte `LuceneNativeConfiguration` registriert den von Lucene dynamisch aufgelösten Konstruktor. Keine pauschale Reflection aller Lucene-Klassen, keine unbewiesene Runtime-Initialisierungsflagge.
- Native-Parität fand zunächst fehlende HTTPS-Links in `descriptionHtml`: Jsoup verwendet intern `java.net.URL`. `quarkus.native.enable-https-url-handler=true` erhält diese Links auch im Native Image; ein expliziter Pakettest schützt einen realen Harvest-Link. [Quarkus-Dokumentation](https://quarkus.io/guides/building-native-image/) beschreibt den standardmässig deaktivierten HTTPS-URL-Handler. Keine Netzwerkabfrage der XTF oder Linkziele.
- `quarkus.locales=de-CH,en-US` nimmt die vertragliche de-CH-Sortierung auch ins Native Image auf.
- `Out of date version …23.1… supports 25.0.0`: GraalVM/Mandrel25 als Native-Compiler wählen; Java-21-Anwendung nicht unnötig umstellen.
- `Outputting both native and JAR packages …`: beim Native-Befehl `quarkus.package.jar.enabled=false`, JVM separat bauen.
- Plattformfehler/`Exec format error`: Binaryformat/Architektur (`file`, `docker image inspect`) prüfen; macOS-Mach-O nicht im Linux-Image verwenden.
- HTTP `400/404/503` bleibt Problem-JSON; Suchindexausfall ist 503, kein `[]`. CORS bevorzugt same-origin Reverse Proxy; lokale QWC2-Freigaben gezielt konfigurieren, nicht pauschal Produktion öffnen.

CI `.github/workflows/main.yml` prüft JVM/T1–T3 und einen separaten Linux-Native-Job mit echtem `testNative` und read-only Containerparität. Lokal ausgeführte Ergebnisse und unabhängig geprüfter SHA werden ausschliesslich in Implementierungs-/Reviewbericht behauptet; ein konfigurierter CI-Job allein ist kein ausgeführter Nachweis.


## QWC2-Dev-CORS

Produktion bevorzugt denselben Origin über Reverse Proxy. Für einen lokalen QWC2-Client auf `http://localhost:8081` kann Quarkus Dev gezielt freigegeben werden ([offizielle CORS-Konfiguration](https://quarkus.io/guides/security-cors/)):

```sh
./gradlew quarkusDev -Dquarkus.http.port=8083 \
  -Dquarkus.http.cors.enabled=true \
  -Dquarkus.http.cors.origins=http://localhost:8081 \
  -Dquarkus.http.cors.methods=GET,HEAD,OPTIONS \
  -Dquarkus.http.cors.headers=Accept,Content-Type \
  -Dquarkus.http.cors.access-control-allow-credentials=false
```

Origin einschliesslich Schema/Hostname/Port an den echten Dev-Client anpassen (`127.0.0.1` ist ein anderer Origin). Diese Optionen gelten nur für diesen Aufruf; die ausgelieferte Konfiguration aktiviert CORS nicht pauschal. Kein `*` und keine Cookie-Freigabe erforderlich. QWC2 sendet normale JSON-GETs, keine HTMX-Header.

```sh
curl -i -X OPTIONS http://localhost:8083/api/v1/ebenen \
  -H 'Origin: http://localhost:8081' \
  -H 'Access-Control-Request-Method: GET' \
  -H 'Access-Control-Request-Headers: Content-Type'
curl -i http://localhost:8083/api/v1/themen -H 'Origin: http://localhost:8081'
curl -i http://localhost:8083/api/v1/themen -H 'Origin: http://localhost:9999'
```

Erwartung: erlaubter Preflight/GET ist 200 mit exakt `Access-Control-Allow-Origin: http://localhost:8081`; fremder Origin wird mit 403 und ohne Freigabe abgewiesen. `Access-Control-Allow-Credentials: true` darf fehlen. Bei Problemen Origin und Browser-Preflight mit den freigegebenen Methoden/Headern vergleichen; nicht den API-Vertrag ändern.


## Docker-Hub-Veröffentlichung

Der Workflow `JVM checks` vergibt **0.2.N**, wobei N seine GitHub-Laufnummer ist (kein Git-Tag und keine Jenkins-/Travis-Nummer). Ein erneuter Versuch desselben GitHub-Laufs behält N. Ohne `GITHUB_RUN_NUMBER` ist die lokale Version `0.2.LOCALBUILD`; ungültige gesetzte Laufnummern werden abgewiesen.

Repository: `docker.io/sogis/kartenkatalog`, Plattform **linux/amd64**. Native: `0.2.N`, `latest`, `0`, `0.2`; JVM: `0.2.N-jvm`, `latest-jvm`, `0-jvm`, `0.2-jvm`. Alle vier Tags einer Variante müssen denselben Registry-Digest haben. Für reproduzierbare Deployments konkrete Version oder Digest verwenden; Aliase werden bei erfolgreichen Veröffentlichungen aktualisiert.

Der Native-Job wartet auf die JVM-/API-/Browser-/CORS-/Prozessgates. Er baut und prüft beide Images samt Non-Root-Standardbenutzer, Plattform, OCI-Version/Revision/Quelle und tatsächlicher Startversion, bevor genau diese Images getaggt/gepusht werden. Docker-Hub-Login nutzt ausschliesslich vorhandene Actions-Secrets `DOCKER_USERNAME`/`DOCKER_PASSWORD`. Nur Push auf `main` und manuelles `workflow_dispatch` auf `main` veröffentlichen; Pull Requests nicht. Branchbezogene Concurrency bricht ältere Workflowausführungen zugunsten neuer ab.

Nach dem Push prüft `scripts/verify-published-images.py --version 0.2.N` sämtliche acht öffentlichen Registry-Manifeste und schreibt `build/reports/release/registry-tags.json`. Beide versionierten Images werden erneut gepullt und durch die vollständige read-only JVM-/Native-Prozessmatrix geprüft. Registry-Digests und Reports werden als CI-Artefakte/Job-Summary gesichert. Bei Fehlern ist der Lauf rot; bereits erfolgte Pushes werden nicht als vollständige Freigabe ausgegeben.

Beispielstart mit veröffentlichtem Native-Alias (für feste Version `0.2` durch `0.2.N` ersetzen):

```sh
docker pull --platform linux/amd64 sogis/kartenkatalog:0.2
docker run --rm -p 127.0.0.1:8080:8080 --read-only \
  --user 1000730000:0 --cap-drop=ALL --security-opt=no-new-privileges \
  --tmpfs /tmp:rw,noexec,nosuid,size=64m,mode=1777 \
  --mount type=bind,source="$PWD/data/kartenkatalog-harvest-ohne-qml.xtf",target=/catalog/catalog.xtf,readonly \
  sogis/kartenkatalog:0.2
```

JVM analog mit `0.2-jvm`. Die Images enthalten keine XTF; Quelle muss eingebunden werden. Beide verwenden `USER 1001:0` (kein Root-Benutzer), unterstützen OpenShift-UIDs und benötigen nur flüchtiges Scratch für Vert.x, kein Indexvolume. Auf ARM-Hosts emuliert Docker diese **amd64**-Images; es wird keine zusätzliche ARM-Laufzeit veröffentlicht. Ein OCI-Index kann neben der amd64-Laufzeit BuildKit-Provenienz enthalten. Ein echtes Clusterdeployment bleibt separat.

Manuelle Image-Builds erhalten Labels über `--build-arg VERSION=<Gradle-Version> --build-arg REVISION=$(git rev-parse HEAD)`; vor einem JVM-Image `quarkusBuild` ausführen, vor einem nativen Image das passende Linux-Native-Artefakt bauen. Die Label-Prüfung allein ersetzt nicht den Startversions-/HTTP-Test.
