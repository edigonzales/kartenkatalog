# Migrationsstatus

- Aktuelle Lieferung: **P3a** (AC3A-01 bis AC3A-04)
- Zustand: **PASS** – unabhängig geprüft durch OpenCode mit DeepSeek API.
- Letzter unabhängig bestätigter PASS-SHA: **`9dfde0ffb2ad6f94bb71bdd278beb174eb3f9ee6`** (P3a)
- Implementierungsbasis P3a: `047da2ce91a29bfabf0ba7f6138154ca3bd959d9`
- Implementierungscommits: `0a0427a090938c3c28cbafb35fcb3651fcef9d9f`, `9dfde0ffb2ad6f94bb71bdd278beb174eb3f9ee6`.
- Bericht: [implementation/P3a.md](implementation/P3a.md)
- Unveränderter unabhängiger Prüfbericht: [reviews/P3a-review.md](reviews/P3a-review.md)
- Reviewer: OpenCode CLI **2.1.4** aus der installierten App, separate Sitzung `ses_e6c7c0516ffes4UOI8kmQ6Cszb`, Modell `deepseek/deepseek-flash` = DeepSeek-V4.1-Flash-1017 direkt über `https://api.deepseek.com/v1`. Provider-/Modell-/Verbindungsidentität vom Reviewer verifiziert.
- Unabhängige Nachweise: frischer Clean-Build, **59 Unit-/HTTP-Tests und 3 echte Chromium-Tests** ohne Failures/Errors/Skips, eigene Qute-Build-Canary (Basis 0/Mutation 1), neue externe XTF mit **59/59 HTTP-/Prozess- und 35/35 Chromium-Gegenprüfungen**, tatsächliche Screenshot-/SODATA-Sichtprüfung, JVM-/Dev-/RAM-Index-Verträge und Runtime-Graph erneut grün.
- Keine offene Muss-Abweichung. Beobachtungen vollständig im Reviewbericht: einmaliger Navigationstimeout im ersten Browserlauf, danach sechs erfolgreiche Wiederholungen einschliesslich zweier Clean-Läufe; kleinere Hinweise zu HTML-Charset, Metadatenlabel, No-JS-Headernavigation und flüchtigen Build-Reportpfaden. Keine Produkt-/Teständerung nach dem abgenommenen SHA.
- Externe Rohbelege: `/Users/stefan/sources/kartenkatalog-P3a-review-evidence/9dfde0ffb2ad6f94bb71bdd278beb174eb3f9ee6/`; sauberer separater Review-Worktree: `/Users/stefan/sources/kartenkatalog-review-P3a`.
- Dieser nachfolgende Commit archiviert ausschliesslich Dokumentation; Produktfreigabe gilt für den oben genannten Kandidaten-SHA.
- **P3b nicht begonnen.** Der aktuelle Auftrag endet hier. Firefox-/Filter-/History-/Mobile-Sheet-Gesamtgate bleibt P3b; Native/Linux bleibt P4.
