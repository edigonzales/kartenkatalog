# Migrationsstatus

- Aktuelle Lieferung: **P3b** (AC3B-01 bis AC3B-04, kumulativ AC3A-01 bis AC3A-04)
- Zustand: **PASS** – unabhängiges kumulatives UI-Gate P3a+P3b bestätigt.
- Letzter unabhängig bestätigter PASS-SHA: **`c030009c361995baeab0d83ef76fd763ec131a84`** (P3b)
- Implementierungsbasis P3b: `82ff569acad954e8a65212fef7a9ec25f0e4b5e8`
- Bericht: [implementation/P3b.md](implementation/P3b.md)
- Unveränderter unabhängiger Prüfbericht: [reviews/P3b-review.md](reviews/P3b-review.md)
- Reviewer: OpenCode CLI **2.1.4** aus der installierten App, neue Sitzung `ses_e6c582dc0ffemS1G4yvXXFtWCv`; `deepseek/deepseek-flash` = DeepSeek-V4.1-Flash-1017 direkt via DeepSeek API, vom Reviewer über reale Sitzungs-/Modell-/Verbindungsdaten verifiziert.
- Unabhängige Nachweise: frischer Clean-Build, **62 Unit-/HTTP- und 14 echte Browsertests** (Chromium und Firefox, Desktop/Mobile) ohne Failures/Errors/Skips; eigene Qute-Mutationscanary (Basis 0/Mutation 1); **212/212 eigene HTTP-/JSON-/Sicherheits-/Browser-Gegenprüfungen**, echte Bildsichtprüfung gegen SODATA; frische JVM-/Dev-/RAM-Index-Prozesse und Runtime-Graph. JSON-Baseline und Harvest byte-identisch.
- Keine offene Muss-Abweichung. Beobachtungen vollständig im unabhängigen Bericht: Font-Wartehänger im eigenen No-JS-Probeharness (final 212/212 grün), kurz asynchrones `aria-expanded` nach Dialogschliessen und dokumentierte visuelle Adaptationen. Keine Produktänderung nach PASS.
- Externe Rohbelege: `/Users/stefan/sources/kartenkatalog-P3b-review-evidence/c030009c361995baeab0d83ef76fd763ec131a84/`; sauberer separater Review-Worktree: `/Users/stefan/sources/kartenkatalog-review-P3b`.
- Dieser nachfolgende Commit archiviert ausschliesslich Dokumentation; Produktfreigabe gilt für den genannten Kandidaten-SHA.
- **P4 nicht begonnen.** Der aktuelle Auftrag endet hier; Native/Linux/Container bleiben P4 und sind nicht als bestanden behauptet.
