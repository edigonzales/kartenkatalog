# P3b – Filter-/Such-UX und kumulatives UI-Gate

31.10.2026 · Agent A · **AWAITING_REVIEW**. Kriterien **AC3B-01–04**, kumulativ **AC3A-01–04**.
BASE_SHA: `82ff569acad954e8a65212fef7a9ec25f0e4b5e8` (sauberes `main`); voriger unabhängiger Produkt-PASS `9dfde0ffb2ad6f94bb71bdd278beb174eb3f9ee6` (P3a). Keine AGENTS.md im Repository/Elternpfad. **P4 nicht begonnen.**

## Umsetzung und Verträge

- Ein typisiertes `catalogState.html` rendert Suchformular, Filter, Chips, Ergebniszusammenfassung und bestehende native Themenliste. `/` und `/thema` geben mit `HX-Request: true` nur diesen Zustand zurück; normale Anfragen/History-Restore liefern das vollständige Layout. `Vary: HX-Request, HX-History-Restore-Request`, explizites UTF-8 für Vollseite und Fragmente. Qute-`@CheckedTemplate` bleibt aktiv; Rendern der Response als String verhindert, dass die Qute-Response-Behandlung den expliziten Charset wieder durch `text/html` ersetzt (P3a-OBS-02).
- Themen/Produkttyp/Organisation verwenden dieselben P2-Queries/Facets wie JSON. Checkbox-Mehrfachwahl, OR innerhalb/AND zwischen Gruppen, dynamische Counts unter jeweils anderen Filtern. Kategorien und Labels aus echten Daten; Organisation fehlt im Harvest-UI weiterhin. 0-Optionen bleiben wählbar/entfernbar. Keine Datums-/Dummyfilter, keine Harvest-Änderung.
- Desktop: native Details-Popover mit Checkboxen und Apply, nur eines offen, Escape/Aussenklick schliessen ohne Layoutverschiebung. Mobil: dieselben Formfelder in nativem modalen `dialog`, Schliessen/Escape, Browser-Fokusbegrenzung und Rückkehr zum Auslöser, scrollbare Kategorien und sticky Apply. Ohne JS bleiben die Checkboxgruppen/GET-Submits und normalen Themenlinks nutzbar; kein zweiter Formular-/State-Baum.
- HTMX ersetzt ausschliesslich `#catalog-state`, aktualisiert die URL und synchronisiert konkurrierende Form-/Chip-Requests. Passende offene Themen werden mit neuen Queryparametern wieder geöffnet/neu geladen, entfernte Themen verschwinden. Neue Zeilen werden vor der Wiederherstellung mit `htmx.process` initialisiert. Fehler erhalten die bisherigen Ergebnisse und zeigen einen zugänglichen Hinweis; Themenfragmente behalten ihren Fallback-Link.
- Der HTMX-DOM-Verlaufscache ist bewusst **deaktiviert** (`historyCacheSize: 0`, `historyRestoreAsHxRequest: false`): geänderte, noch nicht angewendete Formfelder dürfen nicht unter der vorherigen URL gespeichert werden. Back stellt den Zustand frisch aus der betreffenden URL her; Reload und kopierte Deep Links funktionieren auch ohne JS. Kein eigener History-/State-Store.
- `CatalogUrlFactory.listUrl/withoutFilter` kodieren wiederholte Werte, erhalten andere Gruppen und entfernen exakt einen Chip. Suchtext löschen erhält Filter und fällt bei Relevanz auf Titel A–Z zurück. Globaler Reset leert die Auswahl. Themen bleiben A–Z; Ebenen werden innerhalb des Themas nach ausgewähltem Titel A–Z/Z–A oder Relevanz bei Suchtext sortiert, entsprechend beschriftet.
- JSON-DTOs, API-, Snapshot-/Lucene-/Parserquellen, P0-JSON-Fixture und Harvest-XTF unverändert. P3a-Sanitization, sichere Karten-/Metadaten-/QML-Links, lokale Assets/Layout/Lizenzen und fehlende Metadaten bleiben erhalten. Keine Runtime-Dependencies hinzugefügt. CI ruft beide Pflichtfamilien auf; Browserinstallation umfasst jetzt **Chromium und Firefox**, Task `installPlaywrightBrowsers`, ohne stille Skips.

## Tatsächliche Verifikation

macOS 15.8.1 arm64, Java 21.0.7, Gradle 9.8.2, Quarkus 3.40.1.1, Lucene 10.6.0, Playwright 1.60.0.

| Prüfung / exakter Befehl | Exit / Ergebnis | Frische Rohbelege |
|---|---|---|
| `./gradlew clean test quarkusBuild playwrightTest -PreferenceUrl=http://127.0.0.1:18090 --console=plain` | **0**, 1m04s; **62/0/0/0** Unit/HTTP und **14/0/0/0** Browser (Tests/Failures/Errors/Skips) | `build/reports/p3b/clean-ui.log`, `build/test-results/{test,playwrightTest}/TEST-*.xml`, `build/reports/tests/` |
| `python3 scripts/p1-process-contract.py` | **0**, HTTP/Health/Snapshot und acht echte Startabbrüche | `build/reports/p3b/p1-process.log`, `build/reports/p1/` |
| `python3 scripts/p1-process-contract.py --dev` | **0**, echter Dev-Prozess und HTTP-Verträge | `build/reports/p3b/dev.log` |
| `python3 scripts/p2-process-contract.py` | **0**, frischer RAM-Index/Neustarts/read-only/XTF-Änderung/Indexfehler; Harvest ready 1.010s, Index 313ms, RSS 201712KiB, mediane Suche 17.719ms | `build/reports/p3b/p2-process.log`, `build/reports/p2/` |
| `./gradlew dependencies --configuration runtimeClasspath --console=plain` | **0**, weiterhin nur Lucene core/analysis-common 10.6.0, kein Spring/JTE/JDBC/Playwright zur Runtime | `build/reports/p3b/dependencies.log` |
| Externe Wegwerfkopie aller aktuellen Quellen: `./gradlew quarkusBuild --console=plain`, danach `{option.count}` → `{option.missingFacetCount}` in `catalogState.html` und erneut Build | **0 Basis / 1 Mutation erwartet**, Qute meldet nicht existierendes Feld; Kopie entfernt, Produktquelle unverändert | `build/reports/p3b/qute-{baseline,mutated}.log` |
| `git diff --check`, SHA-Vergleich von JSON-Fixture/Harvest gegen BASE | **0**, beide byte-identisch | lokaler Commit-Check; unabhängiger Reviewer wiederholt |

Die Browsermatrix besteht aus **6 kumulativen P3a-Tests** (3 Fälle × beide Engines: Assets/Skip-Link/Keyboard/Expansion/Details, No-JS-Suche/Links, schmales Layout) und **8 P3b-Tests** (4 Fälle × beide Engines). P3b startet ein separates gebautes JVM-JAR mit der bestehenden kleinen Such-XTF, ohne Harvest-Metadaten zu erfinden:

1. Desktop 1440×900: Suche; Popover per Enter/Escape und Fokus; unveränderte Ergebnis-Y-Koordinate bei Öffnung; Mehrfachthemen/-typen plus Organisation, Chips/Count/URL; echte expandierte Layer-IDs/-Reihenfolge gleich JSON; Sortierwechsel lädt offenes Thema neu; einzelner Chip, Back, Reload, kopierter Deep Link, Suchtext-Clear und Gesamtreset; keine JS-Fehler/verschachtelte Vollseite.
2. Mobil 390×844: modales Sheet, Fokus bleibt enthalten, Escape stellt Auslöserfokus her; Kategorie-AND mit leerem Trefferbild; Chip entfernt nur Typ, Thema bleibt; Expansion, Detail-/Kartenlink und kein Overflow.
3. Ohne JS 390×844: Checkbox-Mehrfachthemen über normalen GET-Submit, URL/Chips, echter Themenlink und Entfernen eines Chips; kein Overflow.
4. Negativ: HTTP-503 für eigenes Suchfragment wird gezielt injiziert, Ergebnisse bleiben bestehen/URL bleibt unverändert/Fehler sichtbar; anschliessend reflektierter HTML-Angriff in Suchfeld/Chip ohne ausführbares Markup und Reset aus Leerzustand.

Neue HTTP-Tests vergleichen **denselben** Multi-Query über JSON, Vollseite und HTMX samt Ebenen-IDs/-Reihenfolge, allen Facetcounts, ausgewählten Optionen/Chips; History-Restore liefert Vollseite, Nulltreffer behalten entfernbare Auswahl, ungültige Filter/zu lange Query bleiben 400/404. Zusätzlicher URL-Test schützt Encoding/Mehrfachwerte und selektives Entfernen. Alle bisherigen T1/T2 inklusive 503/XSS/QML laufen frisch mit.

### Während der Umsetzung entdeckte Fehler

Erste Läufe scheiterten nachvollziehbar: neuer Jsoup-Test benutzte zunächst nicht unterstütztes `:checked`, RestAssured kodierte den vorbereiteten Query erneut; Testharness korrigiert (Attributselektor/Encoding und explizite Merge-Konfiguration für wiederholte Parameter). Die neue Charset-Assertion deckte die Qute-Response-Übersteuerung auf; explizite Response-Ausgabe korrigiert. Browsertests fanden fehlende Neuinitialisierung offener Themen nach Sortierung sowie einen im History-Snapshot erhaltenen Busy-/Formzustand; durch Initialisierung und serverseitige History-Wiederherstellung behoben. Ein No-JS-Firefox-Lauf hing in der **zusätzlichen Screenshot-Hilfe** an `document.fonts.ready`: Lauf gezielt beendet (Exit 1), diesen Font-Warte-/Screenshotaufruf im No-JS-Fall entfernt; alle No-JS-Funktionsassertions bleiben aktiv, Pflichtscreenshots entstehen in beiden Engines mit JS. Eine Testannahme zur ersten de-CH-Titelzeile war falsch (`Atlas` steht vor `AV Vermessung`) und wurde auf tatsächliche fachliche Sortierung korrigiert. Danach vollständiger frischer Clean-Lauf mit allen 76 Tests grün. Keine Abschwächung des Produktvertrags/keine Skips.

## Visueller Vergleich – Bilder tatsächlich geöffnet

Die echte SODATA-App am unveränderten Commit `9db0920e58613ece3bccf5e024199c016705d5de` lief erneut mit Java25/Profil `local` auf Port18090 und ihrem eigenen 62er-Katalog. In **beiden Engines** Startseite und geöffnete Desktop-/Mobile-Filter aufgenommen, Referenz danach gezielt beendet (Referenz-Gradle Exit1 wegen Stop). Gepinnte exemplarische Aufnahmen: `reference/sodata-filter-desktop.png`, `reference/sodata-filter-mobile.png`; Herkunft/Lizenz/Reproduktion in `reference/README.md`. Kein Laufzeitzwang auf den Referenzserver.

| Merkmal | Sichtprüfung und begründete Anpassung |
|---|---|
| Header/Breadcrumb/Typografie | dieselben lokalen Solothurn-Assets/Komponenten, Rot-/Grau-/Frutiger-Sprache, grosses H1 und 62px-Suchfeld; Kartenkatalogpfad statt Datenportal |
| Desktopband/Popover | ruhige gerahmte Kategorien, Auswahlhinweis/Chevron, Checkboxen mit Count rechts, schwebendes Panel ohne Verschiebung; sichtbarer GET-Submit statt JS-Zwang |
| Kategorien/Sortierung | Thema/Produkttyp, Organisation nur in separater Fixture; kein Publikationsdatum oder künstliche Neueste-Sortierung; klare Ebenensortierung bei A–Z-Themen |
| Chips/Leerzustand | einzelne graue entfernbare Chips, erhaltene Auswahl/Count; klare Kein-Treffer-Meldung und Reset; reflektierter Angriffsstring erscheint nur als Text |
| Liste/Expansion/Details | ursprüngliche Plus/Minus-Zeilen, echte Ebenenzahl und Unterzeilen; sichere Detail-/Kartenlinks, auf Mobile eine Spalte, keine erfundenen Zusatzfelder |
| Mobile-Sheet | wie Referenz Overlay, Titel/Schliessen, scrollbare Gruppen und Apply; native Dialog-Modalsemantik statt eigener Fokusfalle, max.90dvh statt voller Referenzhöhe; kein Overflow, ausreichende Checkboxabstände |

Frische eigene Screenshots: `build/reports/p3a/screenshots/{chromium,firefox}-{catalog-desktop,catalog-expanded-desktop,detail-desktop,catalog-mobile,detail-mobile}.png`; `build/reports/p3b/screenshots/{chromium,firefox}-{desktop-popover,desktop-selected-expanded,desktop-empty,mobile-sheet,mobile-empty,mobile-expanded,mobile-detail}.png`. `build/` ist flüchtig und wird bei `clean` ersetzt; Reviewer soll Rohbelege extern archivieren und selbst Bilder ansehen.

## Kriterien und Reviewauftrag

**AC3B-01:** fachliche Mehrfachfilter/Counts/Chips/Reset und JSON-/HTML-/HTMX-Parität belegt. **AC3B-02:** Fragment-, URL-/Back-/Reload-/Deep-Link-/Expansion- und No-JS-Verträge belegt. **AC3B-03:** beide Engines, beide Viewports, echte Popover/Sheet-/Fokus-/Layout-/Leerzustandsbilder und Referenzvergleich ausgeführt. **AC3B-04:** technische Eigenprüfungen frisch grün; unabhängige kumulative Zielabnahme **aller AC3A-01–04 und AC3B-01–04 bleibt OpenCode-Aufgabe**, keine Selbstfreigabe.

Keine offene Pflichtprüfung in der eigenen P3b-Implementierung. Firefox nicht nur installiert, sondern alle sieben Browserfälle je Engine ausgeführt. Native/Linux/Container ausschliesslich P4, nicht vorgezogen oder als bestanden behauptet. Handoff nach Commit mit vollem SHA an neue OpenCode-Sitzung/sauberen Review-Worktree, `deepseek/deepseek-flash` direkt via DeepSeek API, eigener neuer Filter-/Browser-Gegenfall und konkrete Bildsichtprüfung. **Vor P4 stoppen.**
