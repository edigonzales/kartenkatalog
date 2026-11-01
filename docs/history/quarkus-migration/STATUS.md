# Migrationsstatus

- Aktuelle Lieferung: **P5 – Migration abgeschlossen**, kumulative Gesamtabnahme **P0–P5**.
- Zustand: **PASS**, unabhängig durch OpenCode/DeepSeek API bestätigt; alle **28 Kriterien** erfüllt, keine offene Muss-Abweichung.
- Finaler unabhängig bestätigter Produkt-PASS-SHA: **`a6ded992019cedce3d448f996518cadeeb3defbc`**.
- Implementierungsbasis P5: `bdb22973557d785c7c46d90595a924b91db44a43`; vorheriger Produkt-PASS P4: `1af5736c548181746d80688c150e6fefc5c28d5f`.
- [Implementierungsbericht P5](implementation/P5.md), [unveränderter unabhängiger Prüfbericht](reviews/P5-review.md), [Betriebshandbuch](OPERATIONS.md).
- Reviewer: OpenCode CLI **2.1.4**, eigene neue Sitzung `ses_e6b2668b0ffe9s5ipx1xIbv4vg`; tatsächlicher Provider `deepseek`, Modell `deepseek-flash` = **DeepSeek-V4.1-Flash-1017**, direkt via DeepSeek API. Identität über Session-/Provider-Metadaten und direkte TLS-Verbindung verifiziert; CLI ausdrücklich vom Nutzer erlaubt. Reviewprozess beendet mit Exit0.
- Unabhängig frisch auf dem finalen SHA: **62/0/0/0** Unit/HTTP, **3/0/0/0** JVM-Paket, **14/0/0/0** Chromium-/Firefox-Browser, eigener neuer Linux/arm64-Mandrel25-Native-Build, **20/20 echte JVM-/Native-Containerfälle**, **67/67 eigene Native-Cross-Layer-Checks**, **47/47 eigene Browserchecks**, Vertrags-/404-/Filter-/XSS-/Themenreferenz-/Qute-Negativcanaries. Keine Fehler, Failures oder unzulässigen Skips.
- Java21, Quarkus3.40.1.1, Lucene10.6.0 mit `ByteBuffersDirectory`/`GermanAnalyzer`; kein Spring/JTE/JDBC/DB-Betrieb. Snapshot und Heap-Index bei jedem Start aus XTF, keine persistierten Indexdateien/Indexvolumes. P0-API-/QWC2-Vertrag und unvollständiger Harvest unverändert.
- Zusätzlich durch Agent A: 3 macOS-Native-Pakettests, 14 Native-Browsertests und 20 lokale JVM-/Native-Prozessfälle; diese nicht als unabhängige Ausführung behauptet. Dev-CORS mit erlaubtem und abgewiesenem Origin tatsächlich geprüft.
- Tatsächlich geprüfte Linux-Architektur **arm64**; kein amd64-, Clusterdeployment- oder Remote-CI-Erfolg behauptet. OpenShift-Manifest bleibt Vorlage. Kein Push/Merge/Release.
- Rohbelege: `/Users/stefan/sources/kartenkatalog-P5-review-evidence/a6ded992019cedce3d448f996518cadeeb3defbc/`; sauberer separater Review-Worktree: `/Users/stefan/sources/kartenkatalog-review-P5`.
- Dieser nachgelagerte Commit archiviert ausschliesslich Dokumentation; seit Produkt-PASS keine Code-/Test-/Buildänderung. **Kein weiterer Lieferabschnitt offen.**


## Nachgelagerter Auftrag: Version 0.2 / Docker Hub

- Zustand: **PASS – abgeschlossen und tatsächlich veröffentlicht**.
- Version **0.2.73**, echte GitHub-Laufnummer73, Plattform **linux/amd64**, Repository **sogis/kartenkatalog**.
- Unabhängige Pre-Publication-Freigabe: **96e266d332a91456ff80e885dea03cac9b074ef6**, OpenCode2.1.4 / `deepseek/deepseek-flash` direkt via DeepSeek API, Sitzung `ses_e69c92d6cffeUa5eTY3BSJ0HfL`, R1–R7 PASS. Zwei anfängliche Doku-Findings behoben; historische P5-Abnahme oben unverändert SHA-bezogen.
- Tatsächlich veröffentlichter Commit: **19300afaa36546fa1972b0cbd5d450a7f57a7467** (nur Doku-Archivierung nach der Freigabe, Produktbaum identisch).
- [Release-CI73](https://github.com/edigonzales/kartenkatalog/actions/runs/41289361002) und [Mirror34](https://github.com/edigonzales/kartenkatalog/actions/runs/41289360988) vollständig grün. Frische JVM-/Browser-/Native-Tests ohne Fehler/Skips; echter Linux-amd64-testNative **3/0/0/0**; **20 Containerfälle vor Push und 20 nach erneutem Pull** erfolgreich.
- Native-Tags **0.2.73, latest, 0, 0.2** → `sha256:e9af3500af679393b01ae06920193c783e834f9f52aaec637ebb09c8be34c753`.
- JVM-Tags **0.2.73-jvm, latest-jvm, 0-jvm, 0.2-jvm** → `sha256:e86dfd5c197c6f9d35530620a71cb8459cd3f67c98b373ad994c9ead4b40d37e`.
- Alle acht öffentlichen Registry-Manifeste zusätzlich ausserhalb CI geprüft; Digestgleichheit je Variante bestätigt. OCI-Version/Revision/Quelle, Default-USER1001:0, beliebige UID, read-only RootFS/XTF und Heap-Index tatsächlich geprüft.
- [Implementierung, Tests und Veröffentlichungsnachweis](implementation/RELEASE.md), [finaler unabhängiger Review](reviews/RELEASE-review.md), [erster Review mit behobenen Doku-Findings](reviews/RELEASE-initial-review.md).
- Kein Clusterdeployment und keine ARM-Veröffentlichung. Keine offene Pflichtprüfung. Dieser Nachweiscommit enthält nur Dokumentation; Imageversion/-Digests bleiben unverändert.
