# Migrationsstatus

- Aktuelle Lieferung: **P4** (AC4-01 bis AC4-04)
- Zustand: **AWAITING_REVIEW**
- Letzter unabhängig bestätigter PASS-SHA: **`c030009c361995baeab0d83ef76fd763ec131a84`** (P3b, kumulatives UI-Gate)
- Implementierungsbasis P4: `6c810d962875b2fa0f9c6810bf5fe7d236291e0a`
- Bericht: [implementation/P4.md](implementation/P4.md); [Betriebshandbuch](OPERATIONS.md)
- Eigene Nachweise: **62/0/0/0** Unit/HTTP, **3/0/0/0** JVM-Paket, **3/0/0/0** Native-Paket, jeweils **14/0/0/0** Browserfälle an JVM und Native. Echter macOS/arm64-GraalVM25-Build und separater Linux/arm64-Mandrel25-Build; Java21/Quarkus3.40.1.1/Lucene10.6.0 unverändert. Je 18 echte JVM-/Native-Prozessfälle lokal und in read-only Linux-Containern, Payload-/Such-/Facet-/QML-Parität und harte Startfehler erfolgreich, kein persistierter Index. Negative Laufzeit-Canary erkennt JVM statt Native, leeres Image wird abgelehnt.
- Betrieb: beliebige Non-Root-UID, read-only XTF/RootFS, begrenztes RAM-Scratch mit Dateiinventar, UBI9-Images, Health/Probes, Runbook und CI. Kein tatsächliches Clusterdeployment/amd64-Ergebnis behauptet, CI nur lokal gelintet.
- Nächster Schritt: unabhängiger OpenCode-Review mit `deepseek/deepseek-flash` direkt via DeepSeek API im separaten sauberen Kandidaten-Worktree. Frische T1–T4 inkl. eigenem Linux-Native-Build, echter JVM-/Native-Parität und eigenem neuen Native-/Startgegenfall; keine Übernahme alter Rohberichte als neue Abnahme.
- **P5 nicht begonnen.** Der Auftrag endet nach unabhängigem P4-PASS und Dokumentation.
