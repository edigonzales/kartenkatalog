# Quarkus-Kartenkatalog betreiben und prüfen

## Artefakte und Voraussetzungen

Anwendung: **Java 21**, Quarkus **3.40.1.1**, Gradle **9.8.2**, Lucene **10.6.0** (`lucene-core`, `lucene-analysis-common`). Native-Kompilierung für diese Quarkus-Version benötigt **GraalVM/Mandrel 25**; GraalVM 21 wird von Quarkus ausdrücklich zurückgewiesen. Das ändert weder Java-21-Toolchain noch Bytecode-Target. Linux-Mandrel-Builder und Runtime-Basisimages sind in CI/Dockerfiles mit Digest gepinnt. Ein macOS-Mach-O-Binary kann nicht in einem Linux-Container laufen.

- JDK21 und bei lokalem Native-Build zusätzlich GraalVM25 mit `native-image` und C-Toolchain.
- Für Linux-Build/Prüfung Docker oder ein entsprechend konfiguriertes Podman. Die folgenden tatsächlich genutzten Befehle verwenden Docker.
- Native-Build benötigt mindestens ca. 4GiB Compilerheap plus Build-/Container-Overhead; keine parallelen Native-Builds in einer kleinen VM. Im lokalen Bericht stehen echte Build-/Runtime-Messwerte.
- Die getestete lokale Linux-Architektur und unabhängige Nachweise stehen in `implementation/P4.md`/`reviews/P4-review.md`. Für eine andere Zielarchitektur erneut bauen **und dort ausführen**, keinen macOS-/arm64-Test als amd64-Nachweis ausgeben. CI baut auf der Architektur ihres Linux-Runners, ohne Push/Release.

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
  ./build/kartenkatalog-0.0.LOCALBUILD-runner
```

Bei Git-Tags kann der Dateiname die von `gradle/versioning.gradle` ermittelte Version enthalten; den tatsächlich gebauten `build/*-runner` verwenden. `testNative` führt dieselben drei `PackagedCatalogIT`-HTTP-Verträge wie `quarkusIntTest` am echten gebauten Artefakt aus, nicht am In-Process-Testcontainer. Zusätzlich ist der separate Paritäts-/Startfehlertest verbindlich:

```sh
# Native-Binary vor einem weiteren Build ausdrücklich an eigenem Artefaktpfad sichern:
mkdir -p build/native-local
cp build/*-runner build/native-local/application
# Aktuelles JVM-JAR erzeugen:
./gradlew quarkusBuild quarkusIntTest
python3 scripts/p4-distribution-contract.py \
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

docker build -f src/main/docker/Dockerfile.jvm -t kartenkatalog:p4-jvm .
./gradlew quarkusBuild \
  -Dquarkus.native.enabled=true -Dquarkus.package.jar.enabled=false \
  -Dquarkus.native.container-build=true -Dquarkus.native.container-runtime=docker \
  -Dquarkus.native.builder-image=quay.io/quarkus/ubi9-quarkus-mandrel-builder-image@sha256:29a5078c94489623ef7e554aad2cbff70a4ff872ac6d60a46f5538697ed82a30 \
  -Dquarkus.native.native-image-xmx=4g
# Auf einem passenden Linux-Host stattdessen testNative für zusätzliche Integrationstests.
docker build -f src/main/docker/Dockerfile.native -t kartenkatalog:p4-native .
python3 scripts/p4-distribution-contract.py \
  --jvm-image kartenkatalog:p4-jvm --native-image kartenkatalog:p4-native
```

Der Test startet **echte getrennte Container** als UID `1000730000:0`, mit read-only RootFS und read-only Daten-Bindmount, ohne Capabilities/Privilege-Escalation oder Indexvolume. Ein auf 64MiB begrenztes flüchtiges `/tmp`-tmpfs dient dem Vert.x-Ressourcencache; sein Inhalt wird separat kopiert und auf Lucene-Dateien geprüft. Er prüft Health, alle fünf API-Formen, HTML-/HTMX-Fragmente, QML/404, gleiche JSON-Payloads und Ranking/Facets für Harvest und Kleinfixtures, Neustartdeterminismus und geänderte XTF. Defekt-XML, XXE, defekte Referenz und echter Lucene-Indexaufbaufehler müssen beide Laufzeiten vor Ready beenden. Er prüft Docker-Mount-/User-/RootFS-Inspektion und `docker diff`, native Logs mit Lucene-Version/Speicherklasse, RSS/Start-/Index-/Suchzeit und räumt eigene Container auf. Rohbelege: `build/reports/p4/distribution/`. Keine vorhandenen Container/Volumes werden gelöscht.

Einzelner Betrieb (XTF-Pfad absolut; bewusst Linux-native Image):

```sh
docker run --rm --name kartenkatalog -p 127.0.0.1:8080:8080 \
  --read-only --user 1000730000:0 --cap-drop=ALL \
  --security-opt=no-new-privileges --memory=512m \
  --tmpfs /tmp:rw,noexec,nosuid,size=64m,mode=1777 \
  --mount type=bind,source=/absoluter/pfad/catalog.xtf,target=/catalog/catalog.xtf,readonly \
  -e CATALOG_XTF_LOCATION=file:/catalog/catalog.xtf \
  kartenkatalog:p4-native
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
