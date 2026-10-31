# Migrationsstatus

- Aktuelle Lieferung: **P1** (AC1-01 bis AC1-04)
- Zustand: **AWAITING_REVIEW**
- Letzter unabhängig bestätigter PASS-SHA: **`cf43a66599ea38d63286ee5d95f093ad89bb08a6`** (P0)
- Implementierungsbasis P1: `49cf211d2b1c223cf05a9a4e2118233c180179ed`
- Bericht: [implementation/P1.md](implementation/P1.md)
- Nachweise: frischer Clean-Build, 41 Tests ohne Failures/Errors/Skips, JVM-/Dev-Smokes, acht Startfehlerfälle, Qute-Canary und Runtime-Dependency-Graph.
- Nächster Schritt: unabhängiger OpenCode-Review mit `deepseek/deepseek-flash` direkt via DeepSeek API im separaten sauberen Review-Worktree auf dem Kandidaten; CLI vom Nutzer autorisiert. Bei Findings P1 nacharbeiten und neuen SHA erneut prüfen.
- **P2 gesperrt** bis unabhängigem PASS für P1; auch nach P1-PASS endet der aktuelle Auftrag vor P2.
