# Migrationsstatus

- Aktuelle Lieferung: **P4** (AC4-01 bis AC4-04)
- Zustand: **PASS** – unabhängig durch OpenCode/DeepSeek API bestätigt.
- Letzter unabhängig bestätigter PASS-SHA: **`1af5736c548181746d80688c150e6fefc5c28d5f`** (P4)
- Implementierungsbasis P4: `6c810d962875b2fa0f9c6810bf5fe7d236291e0a`
- Bericht: [implementation/P4.md](implementation/P4.md); [Betriebshandbuch](OPERATIONS.md)
- Unveränderter unabhängiger Prüfbericht: [reviews/P4-review.md](reviews/P4-review.md)
- Reviewer: OpenCode CLI **2.1.4**, neue Sitzung `ses_e6c21a51cffeH502X7Rv6TAAQY`; `deepseek/deepseek-flash` = DeepSeek-V4.1-Flash-1017 direkt via DeepSeek API, über echte Sitzungs-/Modell-/Verbindungsdaten verifiziert.
- Unabhängige Nachweise: **62/0/0/0** Unit/HTTP, **3/0/0/0** JVM-Paket und **14/0/0/0** Browsertests; eigener frischer **Linux/arm64-Native-Build** mit Mandrel25, eigene Runtime-Images, **18 echte JVM-/Native-Container-Prozessfälle**, volle Payload-/Ranking-/Facet-Parität und harte Startfehler. Eigene neue XTF-/Native-/Betriebs-Gegenprobe des Reviewers: **186/186 Checks, Exit0**. Kein persistierter Lucene-Index; read-only RootFS/XTF, beliebige UID, begrenztes RAM-Scratch überprüft. Runtime-Canaries/Dependency-/Security-/CI-Syntaxprüfung erfolgreich, kein unzulässiger Skip.
- Java21/Quarkus3.40.1.1/Lucene10.6.0 unverändert. GraalVM/Mandrel25 ist der vom Plugin verlangte Native-Compiler; App-Bytecode weiterhin Major65. Zusätzliche eigene macOS/arm64-Native-Tests (3 Paket-/14 Browsertests) dokumentiert, vom Reviewer nicht als eigene Tests behauptet.
- Keine offene Muss-Abweichung. Kleinere Beobachtungen im Reviewbericht: JSON-Objektmitgliedsreihenfolge und dessen eigene zusätzliche exakte QML-Body-Paritätsprobe. Kein tatsächliches Clusterdeployment/amd64-/Remote-CI-Ergebnis behauptet.
- Externe Rohbelege: `/Users/stefan/sources/kartenkatalog-P4-review-evidence/1af5736c548181746d80688c150e6fefc5c28d5f/`; sauberer separater Review-Worktree: `/Users/stefan/sources/kartenkatalog-review-P4`.
- Dieser nachfolgende Commit archiviert ausschliesslich Dokumentation; Produktfreigabe gilt für den genannten Kandidaten-SHA, keine Produktänderung nach PASS.
- **P5 nicht begonnen.** Der aktuelle Auftrag endet hier.
