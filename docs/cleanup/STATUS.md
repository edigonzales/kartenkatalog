# Cleanup-Status

- Lieferung: **C1 – Root-Artefakte und projektspezifische Ignore-Regeln**.
- Zustand: **AWAITING_REVIEW**, noch keine unabhängige C1-Abnahme.
- BASE_SHA: `dd0e1a871db7e124487d02c1a4742b2653f0c51c` (rein dokumentarische Archivierung nach C0-PASS).
- Letzter unabhängig bestätigter Cleanup-PASS: **C0, `1378d3f5b2bb7a210efa5f704c94a4e43ba23643`**, [originalgetreuer C0-Review](reviews/C0-review.md).
- [C1-Implementierungsbericht](implementation/C1.md), [verbindlicher Reviewprompt](OPENCODE_REVIEW_PROMPT.md).
- C1 implementiert: drei nach erneuter Consumerprüfung unbenutzte Root-ZIP/PNG-Artefakte entfernt; `.gitignore` projektspezifisch reduziert und um lokale Environment-/Credential-/Python-Regeln ergänzt. Aktive XTF, Daten-README, sämtliche Testfixtures/Lizenzen, Source, Scripts, Build, CI und `.dockerignore` unverändert.
- Frisch erfolgreich: **62/0/0/0** Unit/HTTP, **3/0/0/0** JVM-Paket; bestehende JVM-/Dev-CORS-/RAM-Indexprozesse samt XML-/Source-/Indexfehlstarts; 32 Ignorefälle, Audit aller 135 verbliebenen getrackten Dateien und reale temporäre XTF/XML-Gegenprobe.
- Rohbelege: `/Users/stefan/sources/kartenkatalog-C1-evidence/dd0e1a871db7e124487d02c1a4742b2653f0c51c/`.
- Offen: unabhängiger OpenCode-2.x-/DeepSeek-4.1-Flash-1017-Review des nach diesem Bericht erzeugten lokalen Kandidatencommits, AC1-01..AC1-05 mit eigenen Tests und eigener neuer Gegenprobe. Codex startet den Review selbständig und wartet auf PASS/FAIL/BLOCKED; bei FAIL nacharbeiten/erneut prüfen, bei BLOCKED stoppen.
- **C2 nicht begonnen**. Kein Push, Merge, Tag, Workflow-Dispatch, Registry-Upload oder Release. Native-/Container-Gesamtgate bleibt für C4 fällig; für C1 nicht als ausgeführt behauptet.
