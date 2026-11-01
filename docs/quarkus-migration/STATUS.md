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

- Zustand: **IN_PROGRESS** – Implementierung unabhängig mit **PASS (Pre-Publication)** auf `96e266d332a91456ff80e885dea03cac9b074ef6` abgenommen; tatsächliche Veröffentlichung steht noch aus. Die historische P5-Abnahme oben bleibt unverändert SHA-bezogen.
- Basis: `8be37f0a4ceed9d8f6db357f177d81dd0cd7f61c`.
- Ziel: Linux/amd64 Native unter `sogis/kartenkatalog:{0.2.N,latest,0,0.2}`, JVM mit entsprechenden `-jvm`-Tags; N ist die GitHub-Laufnummer. Vorhandene Docker-Secrets, kein Clusterdeployment.
- Versionierung/OCI-Labels/Tests/Workflow umgesetzt und eigene frische Tests erfolgreich (62 Unit/HTTP, 3 Paket, 14 Browser, 20 Containerfälle, 9 Versionsfälle, 6 Registrytests). [Implementierungsbericht](implementation/RELEASE.md). Unabhängiger OpenCode-/DeepSeek-Review abgeschlossen, R1–R7 PASS. [Finaler Bericht](reviews/RELEASE-review.md), [erster Review mit behobenen Doku-Findings](reviews/RELEASE-initial-review.md). Noch ausstehend: echte amd64-CI, Docker-Hub-Tags/Digests und Post-Pull-Prozessmatrix.
