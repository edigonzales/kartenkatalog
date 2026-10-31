# P3a: konkrete SODATA-Referenz

Quelle: `sogis/datenportal-sodata`, Commit
`9db0920e58613ece3bccf5e024199c016705d5de` (MIT, ©2026 edigonzales).
`/datasets?view=list` aus der echten lokal gestarteten Anwendung, Profil `local`
(mit deren 62er-Testkatalog), am 31.10.2026 in Chromium/Playwright 1.60.0 bei
**1440×900** aufgenommen: `sodata-desktop.png`. Keine nachgebaute Referenzseite.

Reproduktion bei vorhandenem Referenzcheckout:

```sh
# Im Referenzcheckout, mit verfügbarer Java-25-Toolchain:
SPRING_PROFILES_ACTIVE=local ./gradlew bootRun --args='--server.address=127.0.0.1 --server.port=18090'
# Im Kartenkatalog, Java 21:
./gradlew playwrightTest -PreferenceUrl=http://127.0.0.1:18090
```

Der Kartenkatalog benötigt den Referenzserver nicht zur Laufzeit oder für seine
Browser-Pflichtprüfungen. Ohne `referenceUrl` prüft der Task die neue Oberfläche;
das hier versionierte Referenzbild bleibt für den visuellen Abgleich verfügbar.
Die optional zusätzliche Referenzaufnahme ist kein übersprungenes Browser-Gate.
Rohscreenshots der neuen Oberfläche (mit `referenceUrl` zusätzlich mobile Referenzaufnahme):
`build/reports/p3a/screenshots/`.

Gezielt adaptierte Quellen: `pages/catalog.jte`, `layouts/main.jte`,
`components/{entryTable,entryRow,searchForm,filterBar,resultsShell}.jte`,
`static/css/{design-tokens,catalog}.css`, `CatalogFiltersPlaywrightTest.java`.
Verwendet werden die gemeinsame Farb-/Typografiesprache, Kopfbereich, Breadcrumb,
Suchband, Ergebniszahl, ruhige Zeilen und Plus/Minus-Expansion. Kartenkatalog-
Abweichungen: Themen mit Kartenebenen statt Datenreihen mit Editionen, keine
Datums-/Dateiformatspalten, Karten-/Metadatenlink statt Downloadpillen. P3a nutzt
normale GET-Selects; Desktop-Popover/Mobile-Sheet/Filter-Chips folgen erst P3b.

Lokale Assets/Herkunft:

- `vendor/so-web-components/0.1.10/`: benötigte JS-/Reset-/Token-
  Dateien aus dem Referenzcheckout (dessen npm-Vendorcopy, MIT ©2026 Stefan
  Ziegler; mit LICENSE und Herkunfts-README). Nur benötigte Module übernommen;
  lediglich Whitespace an Zeilenenden normalisiert.
- `vendor/htmx/2.0.11/`: offizielle Dateien aus
  `https://github.com/bigskysoftware/htmx/tree/v2.0.11`, inklusive LICENSE.
- `fonts.css`, `kantons-logo.svg`: bereits im Kartenkatalog enthaltene Assets,
  jetzt unter Quarkus' lokalem Ressourcenpfad verfügbar (Whitespace an
  Zeilenenden normalisiert). Keine Fontdateien erfunden.
- `catalog.css`: eigene Kartenkatalog-Stile unter Übernahme der SODATA-
  Gestaltungskonzepte; Quelle und MIT-Hinweis in CSS, vollständige Lizenz unter
  `vendor/SODATA-LICENSE`.
