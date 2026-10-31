# Migrationsstatus

- Aktuelle Lieferung: **P1** (AC1-01 bis AC1-04)
- Zustand: **PASS** – unabhängig durch OpenCode / DeepSeek geprüft.
- Letzter unabhängig bestätigter PASS-SHA: **`2014f6035638730739820d391a61423a1c967bf3`**
- Implementierungsbasis P1: `49cf211d2b1c223cf05a9a4e2118233c180179ed`
- Vorige Freigabe P0: `cf43a66599ea38d63286ee5d95f093ad89bb08a6`
- Implementierungsbericht: [implementation/P1.md](implementation/P1.md)
- Unabhängiger Bericht: [reviews/P1-review.md](reviews/P1-review.md), unverändert aus dem externen Review übernommen.
- Ausführung: vorhandene gebündelte OpenCode-CLI **2.1.4**, vom Nutzer autorisiert; separate Sitzung `ses_e6cb243e1ffemR42fKk9cmnVAs` und sauberer Review-Worktree auf genau dem Kandidaten.
- Modell/Provider: **`deepseek/deepseek-flash` direkt via `https://api.deepseek.com`**, bestehende DeepSeek-Authentisierung; unabhängige Konfigurations-/Verbindungsprüfung und offizielle Quellen bestätigen **DeepSeek-V4.1-Flash-1017**. Keine OpenCode-Zen-/OpenRouter-Inferenz.
- Unabhängige Nachweise: frischer `clean test quarkusBuild`, **41/0/0/0**, Runtime ohne Spring/JDBC/Postgres/Hikari/JTE, JVM-/Dev-Prozesse, Startabbrüche, eigene Parser-/XXE-/Immutability- (49), JVM/API- (106) und XSS/QML-Gegenproben (32), wirksame Qute-Build-Canary. **Keine offenen Muss-Findings.**
- Beobachtungen ohne Abnahmewirkung im Review festgehalten: Dev-Kindprozess endet nach ca. einer Sekunde Shutdown-Race; Link-Schemaprüfung vor späterer UI-Ausgabe, gezieltes CORS und weitergehendes UI/Native gehören in spätere Lieferungen.
- Originalbericht/Rohbelege: `/Users/stefan/sources/kartenkatalog-P1-review-evidence/2014f6035638730739820d391a61423a1c967bf3/`; OpenCode-Prozess Exit 0.
- Dieser nachträgliche Commit archiviert ausschliesslich Review und Status; geprüfter Produktkandidat bleibt der oben genannte PASS-SHA.
- **P2 wurde nicht begonnen.** Der aktuelle Auftrag endet mit P1-PASS und Dokumentation.
