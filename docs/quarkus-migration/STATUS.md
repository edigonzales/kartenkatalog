# Migrationsstatus

- Aktuelle Lieferung: **P5**, kumulativer Abschluss P0–P5.
- Zustand: **AWAITING_REVIEW**.
- Implementierungsbasis: `bdb22973557d785c7c46d90595a924b91db44a43`.
- Letzter unabhängig bestätigter PASS-SHA: `1af5736c548181746d80688c150e6fefc5c28d5f` (P4), [Prüfbericht](reviews/P4-review.md).
- Cleanup und finale Betriebsdokumentation umgesetzt. Eigene frische Gates: 62 Unit/HTTP, 3 JVM-Paket, 3 macOS-Native-Paket, je 14 JVM-/Native-Browsertests, je 20 macOS-/Linux-JVM-/Native-Prozessfälle; keine Fehler/Skips. Neue Cross-Layer-/exakte QML-Parität und gezielte Dev-CORS-Prüfung erfolgreich. Bericht: [implementation/P5.md](implementation/P5.md).
- Nächster Schritt: lokaler P5-Kandidatencommit, neuer separater OpenCode-Review mit `deepseek/deepseek-flash` direkt via DeepSeek API; Gesamtabnahme aller 28 Kriterien und eigener neuer Cross-Layer-Gegenprobe. Noch keine P5-Freigabe.
