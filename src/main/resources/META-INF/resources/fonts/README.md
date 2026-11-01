# Frutiger Webfonts

Aus den am 2026-11-01 vom Projektbetreiber bereitgestellten TTF-Dateien konvertiert.
Regular/Italic verwenden Gewicht 400, Bold/Bold Italic Gewicht 700.

| Quelle | Webfont | SHA-256 der Quelle |
| --- | --- | --- |
| `FTR_____.TTF` | `frutiger-regular.woff2` | `7ebcebac41a2f8abddc7a0979bae7339ae6d891005f2f46526718be57aa32eef` |
| `FTI_____.TTF` | `frutiger-italic.woff2` | `4d7856bd64c60b626625817cb10df1f9d29913e83629fe6c5faa04efe3b040bb` |
| `FTBL____.TTF` | `frutiger-bold.woff2` | `5f33eca3a580d08723762028d97c12d142debfc6c12136f5450dbff7a3d27a20` |
| `FTBLI___.TTF` | `frutiger-bold-italic.woff2` | `a1693f910e41f311bd75ee9c2b1a18abd6829871d6cd816548e3e9e2664b3ae9` |

Konvertierung mit FontTools 4.68.0 und Brotli (`uv run --with 'fonttools[woff]==4.68.0' python …`):

```python
from fontTools.ttLib import TTFont
font = TTFont(source, recalcTimestamp=False)
font['cmap'].ensureDecompiled()
font.flavor = 'woff2'
font.save(destination)
```

Die Zeichentabelle muss vor dem Speichern dekompiliert werden: Die alten TTFs
enthalten einen ungültigen terminalen Bereichsverweis, den Browser ablehnen.
FontTools schreibt die Tabelle aus den bestehenden Zuordnungen korrekt neu.
Es erfolgt kein Subsetting. Unicode-Zuordnung, horizontale Zeichenmetriken und
sämtliche Glyphenkonturen wurden nach dem WOFF2-Roundtrip mit den Quellen verglichen.
Die ursprünglichen TTFs werden für den Betrieb nicht benötigt.
