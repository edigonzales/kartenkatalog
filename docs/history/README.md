# Historische Projektbelege

Die abgeschlossene Quarkus-Migration und frühere Recherche liegen hier als Archiv. Aktuelle Anleitungen: [Architektur](../architecture.md), [Betrieb](../operations.md), [Tests](../testing.md). Historische Berichte sind Aussagen über ihre damaligen SHAs und Umgebungen, keine Freigabe des heutigen Produkts oder einer neuen Veröffentlichung.

## Herkunft und Inhalt

Die Archivierung verschiebt den vollständigen bisherigen Baum `docs/quarkus-migration/` nach `docs/history/quarkus-migration/` sowie `docs/legacy-notes.md` nach `docs/history/legacy-notes.md`. Ausgangscommit: `ec57d04b8688617ddfe3a36070f23d15459fce30`. Alle ursprünglichen Dateien bleiben bytegleich, einschliesslich der damaligen Betriebsanleitung, unabhängiger Erst-/Fehlreviews, Referenzbilder und Statusdatei. Die neue aktive Betriebsanleitung ist separat in `docs/operations.md`.

- [Originalplan](quarkus-migration/EXECUTION_PLAN.md), [Implementierungsprompt](quarkus-migration/CODEX_STARTPROMPT.md), [Reviewprompt](quarkus-migration/OPENCODE_REVIEW_PROMPT.md), [Status](quarkus-migration/STATUS.md), [damaliger Betrieb](quarkus-migration/OPERATIONS.md).
- Vollständige [Implementierungen](quarkus-migration/implementation/) und [unabhängige Reviews](quarkus-migration/reviews/), einschliesslich [erster Releaseprüfung](quarkus-migration/reviews/RELEASE-initial-review.md) und [erneuter Releaseprüfung](quarkus-migration/reviews/RELEASE-review.md). Früheres FAIL wird nicht entfernt oder umgeschrieben.
- [SODATA-Referenzbilder und Herkunft](quarkus-migration/reference/README.md); diese Bilder sind keine entbehrlichen Root-Mockups.
- [Frühere Recherche](legacy-notes.md).

Git bewahrt den Inhalt, nicht bloss den neuen Dateinamen. Beispiel für einen reproduzierbaren Identitätsvergleich (beide Ausgaben müssen gleich sein):

```sh
git rev-parse ec57d04b8688617ddfe3a36070f23d15459fce30:docs/quarkus-migration/reviews/RELEASE-initial-review.md
git rev-parse HEAD:docs/history/quarkus-migration/reviews/RELEASE-initial-review.md
```

Die vollständige Blob-/SHA256-Tabelle und der Vergleich aller Archivdateien gehören zum [Cleanup-Implementierungsbericht](../cleanup/implementation/C4.md). Die Cleanup-Pläne und früheren Cleanup-Reviews bleiben unter `docs/cleanup/` als nachvollziehbare Prozessbelege erhalten.

## Pfadzuordnung und Grenzen alter Aussagen

| Historisch genannter Ort | Heutiger Ort / Einordnung |
|---|---|
| `docs/quarkus-migration/**` | `docs/history/quarkus-migration/**`, gleicher Inhalt |
| `docs/legacy-notes.md` | `docs/history/legacy-notes.md`, gleicher Inhalt |
| `OPERATIONS.md` innerhalb des alten Baums | Archivkopie bleibt dort; aktive Anleitung unter `docs/operations.md` |
| Relative Links innerhalb `implementation/`, `reviews/`, `reference/` zum selben Archivbaum | gemeinsame Verschiebung erhält die Beziehung |
| Relative Links aus dem Archiv zu damaligen Root-/Sourcepfaden | zusätzliche Ebene `history/` berücksichtigen; Repositorypfade am jeweils genannten historischen Commit auflösen |
| Alte Script-/Test-/Fixture-/Reportnamen in Berichten | damalige Namen, keine ausführbare heutige Anleitung; aktuelle Namen stehen in `docs/testing.md` |
| `/tmp`, lokale absolute Evidenzpfade, damalige Screenshots/Buildreports | externe historische Belege, nicht vollständig Teil des Gitarchivs; kein heutiger Laufnachweis |
| Öffentliche Releases/Registry-Digests, insbesondere `0.2.73` | historische Publikation für den damaligen SHA; keine aktuelle Kandidatenprüfung, kein neuer Releaseauftrag |

Ein alter Link wird im Originalbericht nicht korrigiert. Für die frühere Datei kann beispielsweise `git show <damaliger-SHA>:<damaliger-Pfad>` verwendet werden; ein damals extern abgelegtes Artefakt wird dadurch nicht nachträglich Gitinhalt. Neue aktive Dokumente verlinken ausschliesslich gültige heutige Pfade. Historische P5-/Release-PASS ersetzen keine frischen JVM-/Browser-/Native-/Linux-/Releasechecks des neuen Kandidaten.
