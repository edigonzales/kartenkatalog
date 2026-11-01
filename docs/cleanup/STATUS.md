# Cleanup-Status

- Lieferung: **C3 – explizite Provenienz und eine XTF-Defaultquelle**.
- Zustand: **AWAITING_REVIEW**, noch keine unabhängige C3-Abnahme.
- BASE_SHA: `17f1c25a6c60c563525cae63a7d3064d7597f119` (reine Archivierung nach C2-PASS).
- Letzter unabhängiger Cleanup-PASS: **C2, `c93714d2e08238e7549da62487c87ef5291a2df0`**, [Originalreview](reviews/C2-review.md); vorherige [C1](reviews/C1-review.md)-/[C0](reviews/C0-review.md)-Reviews unverändert erhalten.
- [C3-Implementierungsbericht](implementation/C3.md), [verbindlicher Reviewprompt](OPENCODE_REVIEW_PROMPT.md).
- C3 implementiert: redundantes `@WithDefault` entfernt; Properties bleiben alleinige XTF-Defaultquelle. Optionale `catalog.provenance-notice` / `CATALOG_PROVENANCE_NOTICE` mit leerem Default, normales String-Feld im gemeinsamen Chrome und konditionaler, HTML-escaped Qute-Ausgabe. Keine Vollständigkeitsableitung aus der XTF. Keine weitere Klassenlöschung und kein DI-Redesign; Daten, Fixtures, Native-Hints, Build/CI/Scripts und Lizenzen unverändert.
- Frisch grün: **66/0/0/0** Unit/HTTP (+4 neue Fälle), **3/0/0/0** JVM-Paket, **14/0/0/0** Chromium-/Firefox-Browser, **3/0/0/0** echtes macOS/arm64-Native-Paket; Runtime-/Dev-CORS-/Indexprozesse, **20 lokale JVM-/Native-Distributionsfälle** und **14 eigene Provenienz-/Configprozesse**. Neue Checks für Default/leer/Plaintext/XSS/alternative XTF/Property-vs-Env/missing file; komplette APIs unabhängig von Notice und JVM/Native gleich.
- Rohbelege: `/Users/stefan/sources/kartenkatalog-C3-evidence/17f1c25a6c60c563525cae63a7d3064d7597f119/`. JVM-/Browserreports vor Native-clean gesichert. Kein Linux-/Container-/amd64-/C4-Gesamterfolg behauptet; C4 behält das volle T4-Gate.
- Offen: unabhängiger OpenCode-2.x-/DeepSeek-4.1-Flash-1017-Review des nach diesem Bericht erzeugten lokalen Kandidaten, AC3-01..AC3-06 mit eigenen Gates und eigener neuer Provenienz-/XSS-/Alternativ-XTF-Gegenprobe. Codex startet selbständig, arbeitet bei FAIL nach/prüft erneut und stoppt bei BLOCKED.
- **C4 nicht begonnen**. Keine Archivverschiebung oder neue C4-Dokumentstruktur. Kein Push, Merge, Git-Tag, Workflow-Dispatch, Registry-Push oder Release.
