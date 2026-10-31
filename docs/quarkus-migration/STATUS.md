# Migrationsstatus

- Aktuelle Lieferung: **P2** (AC2-01 bis AC2-04)
- Zustand: **PASS** – unabhängig durch OpenCode / DeepSeek geprüft.
- Letzter unabhängig bestätigter PASS-SHA: **`a9dde956b4937916cec862c6c672cf26dd30636f`**
- Implementierungsbasis P2: `99143256de729a26f137bef2b5865f0b8ffe3597`
- Vorige Freigabe P1: `2014f6035638730739820d391a61423a1c967bf3`
- Implementierungsbericht: [implementation/P2.md](implementation/P2.md)
- Unabhängiger Bericht: [reviews/P2-review.md](reviews/P2-review.md), unverändert aus dem externen Review übernommen.
- Ausführung: vorhandene gebündelte OpenCode-CLI **2.1.4**, vom Nutzer autorisiert; separate Sitzung `ses_e6c9af4bcffeG9QcjpjBpqdPsw`, sauberer Review-Worktree auf genau dem Kandidaten.
- Provider/Modell: **`deepseek/deepseek-flash` direkt via DeepSeek API**, vorhandene gespeicherte Authentisierung. Session-/Verbindungsmetadaten und offizielle Quellen bestätigen **DeepSeek-V4.1-Flash-1017**; keine Zen-/Go-/OpenRouter-Inferenz.
- Unabhängige Nachweise: frischer Clean-Build **55/0/0/0**, exakt Lucene **10.6.0** (`core` + `analysis-common`), JVM-/Dev-/P0-P1-Verträge, RAM-Index-Neuaufbau/read-only/Startfail, eigene 14-Layer-Fixture mit **81 Checks** und eigenen HTTP-/Neustart-Gegenproben. Live-Heap und offene Dateien unabhängig geprüft: ByteBuffersDirectory, kein FS-/MMap-Index, keine persistierten Indexdateien. **Keine offenen Muss-Findings.**
- Beobachtungen ohne Abnahmewirkung sind im Review dokumentiert, insbesondere Fehlerklassifizierung/Fehlermeldungen und Facet-Sortparameter; keine zusätzliche Produktänderung nach dem geprüften SHA.
- Originalbericht/Rohbelege: `/Users/stefan/sources/kartenkatalog-P2-review-evidence/a9dde956b4937916cec862c6c672cf26dd30636f/`; OpenCode-Prozess Exit 0.
- Dieser nachträgliche Commit archiviert ausschliesslich Review und Status; geprüfter Produktkandidat bleibt der oben genannte PASS-SHA.
- **P3a wurde nicht begonnen.** Der aktuelle Auftrag endet mit P2-PASS und Dokumentation.
