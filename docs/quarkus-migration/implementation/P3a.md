# P3a – Qute-/SODATA-Oberfläche und sichere Metadaten

31.10.2026 · Agent A · AC3A-01 bis AC3A-04 · **AWAITING_REVIEW**.
BASE_SHA: `047da2ce91a29bfabf0ba7f6138154ca3bd959d9` (sauberer `main`). Voriges unabhängiges PASS: P2 `a9dde956b4937916cec862c6c672cf26dd30636f`, Bericht `reviews/P2-review.md`. Keine AGENTS.md im Kartenkatalog vorhanden. P3b wird nicht begonnen.

## Umsetzung und öffentliche Verträge

- Geteiltes Qute-Layout, `@CheckedTemplate` und typisierte `CatalogViewModels`, Aufbereitung in `CatalogPageVmFactory`. Lokale `so-web-components` **0.1.10** (Header/Breadcrumb mit No-JS-Fallback), vorhandene Kantonsfonts/Logo, Skip-Link mit fokussierbarem Hauptinhalt, Footer und Provenienzhinweis. Ruhige Zeilen-/Typografiesprache, 62px-Suchfeld und Farbtokens an der konkret inspizierten SODATA-Referenz orientiert. Alle Laufzeitassets same-origin; keine CDN-Abhängigkeit, keine neue SPA-/Node-Build-Infrastruktur.
- **`/` liefert jetzt 200 und die vollständige Katalog-Startseite** statt des historischen Redirects, wie im Plan vorgesehen. `/thema` bleibt funktionierender Alias. Native `details`-Themenzeilen zeigen Beschreibung und echte Ebenenzahl, darunter bei Expansion serverseitig gerenderte Ebenen mit Produktart, Metadaten-/Kartenlink. Anfangs werden keine 886 Detailtexte ins DOM gerendert. Lokal vendortes HTMX **2.0.11** lädt einmalig beim Öffnen ein Fragment; ein normaler Themenlink bleibt ohne JS nutzbar. Native Tastaturbedienung, `aria-controls` und mit JS aktualisiertes `aria-expanded`; ohne JS bleibt die native Expanded-Semantik unüberschrieben. Ladefehler behalten den Themenlink und zeigen einen zugänglichen Fehlerhinweis.
- Normale GET-Suche und einfache Thema-/Produkttyp-Selects, Organisation nur bei tatsächlichen Quellwerten. P2-Suche und Filter bestimmen angezeigte Themen/Anzahlen; leere Themen bei aktiver Query verschwinden. Expansion/Themenseite erhalten Such-/Typ-/Org-Parameter. `Vary: HX-Request` trennt Vollseite/Fragment. Keine Filter-Popover, Chips, History- oder Mobile-Sheet-Implementierung vor P3b.
- Detailseite gruppiert Beschreibung, Schlagwörter, Synonyme und Karteninformationen. Organisation, Verfügbarkeit/Download-Angabe und QML nur mit echten Quellwerten; keine leeren Abschnitte/Badges oder erfundene Berechtigungen. Das Thema stammt weiterhin aus der echten Referenz. Kein Kartenpreview als neue Pflicht.
- Nur Jsoup-saniertes Beschreibungs-HTML wird als `RawString` ausgegeben; Titel, Keywords, Organisation usw. bleiben escaped. `CatalogUrlFactory` baut kodierte interne/QWC2-Links und erlaubt für `further_info_url` nur absolute HTTP(S)-URLs mit Host ohne Zugangsdaten. `javascript:`, `data:`, relative/protokollrelative URLs und Header-Steuerzeichen werden abgewehrt. QML bleibt XML-Attachment mit kodiertem Dateinamen; kein Roh-QML im HTML.
- JSON-DTOs und **`src/test/resources/contract/baseline.json` unverändert**. Nur die explizit vorgesehene HTML-Root-Änderung wurde in Baseline-HTML-Test und unabhängigem Python-Smoke aktualisiert; 400/404/503, MIME, Null-/Listen-/Kennungsverträge bleiben erhalten. HTML-Suche/Fragmente ergeben bei Indexausfall ebenfalls 503 statt leerem Inhalt.

## Tatsächliche Verifikation

macOS 15.8.1/arm64, Java 21.0.7, Gradle 9.8.2, Quarkus 3.40.1.1; Lucene bleibt 10.6.0.

| Befehl / Gegenfall | Exit / Ergebnis | Rohbeleg |
|---|---|---|
| **`./gradlew clean test quarkusBuild playwrightTest -PreferenceUrl=http://127.0.0.1:18090`** nach allen Java-/Template-/Teständerungen | **0; Unit/HTTP 59/0/0/0; Chromium 3/0/0/0** | `build/reports/p3a/clean-test-browser.log`, `build/test-results/{test,playwrightTest}/TEST-*.xml`, `build/reports/tests/` |
| `python3 scripts/p1-process-contract.py` | **0**, echte JVM, aktualisierter P0-HTTP-Vertrag, Health, Snapshot-Lifetime und alle acht Startfehler | `build/reports/p3a/p1-process.log`, `build/reports/p1/` |
| `python3 scripts/p2-process-contract.py` | **0**, echter RAM-Index, read-only/Neustarts/XTF-Änderung/Index-Startfehler | `build/reports/p3a/p2-process.log`, `build/reports/p2/` |
| `./gradlew dependencies --configuration runtimeClasspath` | **0**, kein Spring/JTE/JDBC; Browserbibliothek nur testseitig | `build/reports/p3a/dependencies.log` |
| Eigenständige Wegwerfkopie des aktuellen Arbeitsstands, `./gradlew quarkusBuild --console=plain`, danach `product.missingFieldForCanary` im Detailtemplate und nochmals Build | **0 Basis / 1 Mutation erwartet**, Qute meldet Property not found. Implementierungscheckout unverändert | `build/reports/p3a/qute-canary-{baseline,mutated}.log` |
| SODATA-Referenzcheckout am Commit `9db0920e…`, `JAVA_HOME=/Users/stefan/.sdkman/candidates/java/25.0.3-tem SPRING_PROFILES_ACTIVE=local ./gradlew bootRun --args='--server.address=127.0.0.1 --server.port=18090'` | echter Start, 62er-Referenzkatalog, HTTP/Chromium-Aufnahmen; nach Aufnahme gezielt beendet (Gradle Exit 1 wegen Stop) | `/tmp/kartenkatalog-p3a-reference.log`; versioniertes Referenzbild und Herkunft unter `reference/` |
| `git diff --check`, `git diff --cached --check` | **0** | lokaler Commit-Check |

Neue Unit-/HTTP-Tests prüfen Schema-/URL-Angriffe, reflektierten Suchtext sowie `<script>`, `onerror`, `javascript:` in einer separaten echten XTF-Fixture, fehlende Metadaten/QML, sichere Kartenlinks/85-%-Transparenz, Vollseite/Fragment/Vary, No-JS-Links und HTML-503. Der alte Root-302-Test scheiterte erwartungsgemäss zunächst und wurde auf den im Plan geforderten 200-HTML-Vertrag portiert; die JSON-Fixture blieb unangetastet.

Die **echten Chromium-Tests** starten das gebaute JVM-JAR selbst und prüfen: 169 initiale Themen/0 initiale Ebenenzeilen, lokale Webcomponents/Assets ohne externe Requests oder JS-Fehler, Skip-Link/Fokus, Enter-Expansion mit korrekten Unterzeilen, genau einen Fragmentrequest auch nach erneutem Öffnen, Details/Kartenlink/fehlendes QML, ausgeschaltetes JavaScript mit vollständiger Themenseite und GET-Suche sowie 390px-Layout ohne horizontales Overflow. Playwright **1.60.0**, Browserinstallation als explizite Task-Abhängigkeit; `test` und `playwrightTest` sind getrennte tatsächlich ausgeführte Pflichtfamilien, CI ruft beide auf (Linux-Systempakete via `-PplaywrightWithDeps=true`, CI nicht remote ausgeführt). Keine deaktivierten Tests/Assumptions oder stillen Browser-Skips.

## Visueller Abgleich (Bilder tatsächlich angesehen)

Referenz: echte laufende SODATA-Anwendung, `/datasets?view=list`, identischer Commit wie P0. Vergleich bei **1440×900**, ergänzende Aufnahmen bei **390×844**. Gepinntes Desktop-Referenzbild: `reference/sodata-desktop.png` (112 KB), Herkunft/Reproduktion/Lizenzen: `reference/README.md`. Neue Rohbilder unter `build/reports/p3a/screenshots/`: `catalog-desktop`, `catalog-expanded-desktop`, `detail-desktop`, `catalog-mobile`, `detail-mobile` sowie Referenzaufnahmen `sodata-1440`/`sodata-390` (jeweils `.png`).

| Merkmal | Sichtprüfung / fachliche Anpassung |
|---|---|
| Header/Breadcrumb | dieselben lokalen Solothurn-Komponenten, Logo, Navigation, rote aktive Markierung; Kartenkatalog-/Themenpfad statt Datenportalpfad |
| Intro/Typografie/Suche | Frutiger aus vorhandenem Repoasset, 36px-H1, grosszügiger Lead, 62px-Suchfläche/max.1024px; sichtbarer Submit für normale GET-Suche |
| Liste/Expansion | ruhige Trennlinien, Plus/Minus, klare Titel und Kurztexte; Themen mit Kindebenen statt Datensatzeditionen; native Fokusmarkierung sichtbar |
| Informationen/Details | Ebenenzahl und Produktart, Details und Karte statt unbelegter Publikationsdaten/Dateiformate; lesbarer Metadatenblock und Sidebar, keine leeren Org-/QML-Werte |
| Leerzustand/schmale Ansicht | klare Rücksetz-/Kein-Treffer-Meldung; Header/Breadcrumb umbrechend, Details einspaltig; kein Overflow. Einfacher GET-Filter statt vorgezogenem P3b-Sheet |

MIT-Herkunftshinweise für SODATA-Gestaltung und Webcomponents sowie HTMX-Lizenz lokal erhalten. Fonts/Logo sind unveränderte bereits vorhandene Kartenkatalog-Assets; keine Fontdateien erfunden. Die Referenzanwendung blieb getrackt unverändert und wurde nach der Aufnahme gestoppt.

## Kriterien und Review

**AC3A-01:** typsichere Qute-Verträge mit echter Build-Canary, gemeinsames lokales Layout und Herkunft belegt. **AC3A-02:** native Themenliste, echte Unterzeilen/Counts, stabile Deep Links und No-JS-Fallback über HTTP/Browser geprüft. **AC3A-03:** bedingte Metadaten, QML/Map-/externe Links und XSS-Proben erfolgreich. **AC3A-04:** echter Chromium-Lauf und visueller Referenzvergleich vorhanden; unabhängige neue Accessibility-/XSS-/Deep-Link-Probe bleibt OpenCode-Aufgabe.

Keine offene Implementierungs-Pflichtprüfung. Firefox-/Filter-/History-/Mobile-Sheet-Gesamtgate bleibt P3b, Native/Linux P4; keine solche Freigabe behauptet. Handoff nach lokalem Commit mit SHA, sauberem separatem Worktree, `OPENCODE_REVIEW_PROMPT.md`, OpenCode `deepseek/deepseek-flash` direkt via DeepSeek API. Kein eigenes PASS; **vor P3b stoppen**.
