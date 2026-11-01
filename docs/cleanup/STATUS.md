# Cleanup-Status

- Lieferung: **C2 – fachliche Test-, Harness- und Reportnamen**.
- Zustand: **AWAITING_REVIEW**, noch keine unabhängige C2-Abnahme.
- BASE_SHA: `ca7d629c65d3b856d877bf3d468e946dc752d473` (reine Archivierung nach C1-PASS).
- Letzter unabhängiger Cleanup-PASS: **C1, `b3df50894a40e672612b213de01094685062db76`**, [Originalreview](reviews/C1-review.md). Vorheriger [C0-Review](reviews/C0-review.md) unverändert erhalten.
- [C2-Implementierungsbericht](implementation/C2.md), [verbindlicher Reviewprompt](OPENCODE_REVIEW_PROMPT.md).
- Acht git-mv-Umbenennungen gemäss Plan umgesetzt; aktive Script-/CI-/Doku-/Test-/Reportreferenzen angepasst. Runtime ruft den HTTP-Harness mit `sys.executable` auf. JSON-/XML-Fixtures bytegleich; keine Testassertion/Option entfernt. Alle 33 Produktions-Java-Klassen, Config/Qute/JS, Dependencies und Releasegates unverändert; CSS nur Kommentarpräfix bereinigt.
- Frisch grün: **62/0/0/0** Unit/HTTP, **3/0/0/0** JVM-Paket, **14/0/0/0** Chromium-/Firefox-Browser, **3/0/0/0** echte macOS/arm64-Native-Pakettests; Runtime-/Dev-CORS-/Index-/Versions-/Fake-Registry-Verträge. **20 lokale JVM-/Native-Prozessfälle plus 20 echte linux/arm64-Containerfälle** mit neuen Distributions-/Fixturepfaden. Kein linux/amd64- oder C4-Gesamterfolg behauptet.
- Tatsächliche negative Gegenprobe: unveränderter HTTP-Harness direkt an echter JVM Exit0; Proxy mit falschem 200 statt erwartetem 404 führt zu Exit1. Zwei Distributions-CLI-Missbrauchsfälle werden mit Exit2 verworfen. AST-/Klassen-/Fixtureparität, neue JUnit-Suiteidentitäten, 24 Screenshots und unveränderte CI-Semantik belegt.
- Rohbelege: `/Users/stefan/sources/kartenkatalog-C2-evidence/ca7d629c65d3b856d877bf3d468e946dc752d473/`.
- Offen: unabhängiger OpenCode-2.x-/DeepSeek-4.1-Flash-1017-Review des nach diesem Bericht erzeugten lokalen Kandidaten, AC2-01..AC2-07 mit eigenen Tests und eigener neuer Gegenprobe. Codex startet selbständig und wartet auf PASS/FAIL/BLOCKED; bei FAIL nacharbeiten/erneut prüfen, bei BLOCKED stoppen.
- **C3 nicht begonnen**. Kein Git-Push, Merge, Git-Tag, Workflow-Dispatch, Registry-Push oder Release. Nur lokale Docker-Testimages gebaut; die automatische Main-Push-/Codeberg-Publikationswirkung bleibt unverändert und wurde nicht ausgelöst.
