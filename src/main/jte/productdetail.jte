@import ch.so.agi.meta.Ebene

@param Ebene product
@param String finalDescription = null

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge" />
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=5.0, user-scalable=0" />
    <meta name="mobile-web-app-capable" content="yes" />

    <title>${product.title()} • Kartenportal • Kanton Solothurn</title>

    <script src="https://unpkg.com/htmx.org@2.0.4"></script>
    <script type="module" src="https://cdn.jsdelivr.net/npm/@maps4html/mapml@latest/dist/mapml.js"></script>
    <script src="https://cdn.jsdelivr.net/npm/@materializecss/materialize@2.2.1/dist/js/materialize.min.js"></script>

    <link rel="stylesheet" href="https://fonts.googleapis.com/css?family=Material+Icons|Material+Icons+Outlined|Material+Icons+Two+Tone|Material+Icons+Round|Material+Icons+Sharp">
    <link rel="stylesheet" href="../fonts.css">
    <link rel="stylesheet" href="../kartenkatalog-debug.css">
</head>

<body>
    <div id="product_detail" class="container">
        <div style="margin-top:10px; text-align: right;">
            <img src="../Logo.png" alt="Logo Kanton Solothurn">
        </div>

        <div class="left-align" style="margin-top:60px;">
            @if (product.transparency() != null)
                <h1>${product.title()} <a href="https://geo.so.ch/map?l=${product.ident_part()}[${product.transparency()}]" target="_blank"><i class="material-icons-outlined black-link">open_in_new</i></a></h1>
            @else
                <h1>${product.title()} <a href="https://geo.so.ch/map?l=${product.ident_part()}" target="_blank"><i class="material-icons-outlined black-link">open_in_new</i></a></h1>
            @endif
        </div>

        <div class="textedit-container">
            <label class="textedit-label">Beschreibung</label>
            <div class="textedit-display textedit-display-area">
                @if(product.adescription() != null)
                    $unsafe{product.adescription()}
                @elseif(product.description_override() != null)
                    $unsafe{product.description_override()}
                @elseif(product.description_model() != null)
                    $unsafe{product.description_model()}
                @endif
            </div>
        </div>

        <div class="textedit-container">
            <label class="textedit-label">Layername</label>
            <div class="textedit-display textedit-display-line">
                ${product.ident_part()}
            </div>
        </div>

        <div class="textedit-container">
            <label class="textedit-label">Schlagwörter</label>
            <div class="textedit-display textedit-display-badge">
                @if(product.keywords() != null)
                    @for (var keyword : product.keywords().split(","))
                        <span class="badge">${keyword}</span>
                    @endfor
                @endif
            </div>
        </div>

        <div class="textedit-container">
            <label class="textedit-label">Synonyme</label>
            <div class="textedit-display textedit-display-badge">
                @if(product.synonyms() != null)
                    @for (var synonym : product.synonyms().split(","))
                        <span class="badge">${synonym}</span>
                    @endfor
                @endif
            </div>
        </div>

        <div class="textedit-container">
            <label class="textedit-label">Organisation</label>
            <div class="textedit-display textedit-display-line">
                ${product.org_name()}
            </div>
        </div>

        <p>&nbsp;</p>

        <div class="textedit-container">
            <label class="textedit-label">Thema</label>
            <div class="textedit-display textedit-display-line">
                ${product.theme_title()}
            </div>
        </div>

        <p>&nbsp;</p>

        <div class="textedit-container">
            <label class="textedit-label">Verfügbarkeit</label>
            <div class="textedit-display textedit-display-line">
                ${product.display_text()}
            </div>
        </div>

        <div class="textedit-container">
            <label class="textedit-label">QML</label>
            <div class="textedit-display textedit-display-line">
                @if(product.style_server() != null)
                    <a href="../ebene/${product.ident_part()}/style" target="_blank">${product.ident_part()}.qml</a>

                @endif
            </div>
        </div>

        <div class="textedit-container">
            <label class="textedit-label">Interne Bemerkungen</label>
            <div class="textedit-display textedit-display-line">
                $unsafe{product.remarks()}
            </div>
        </div>

        <div class="textedit-container">
            <label class="textedit-label">Permissions</label>
            <div class="textedit-display textedit-display-line">
                ${product.permissions()}
            </div>
        </div>

        <p>&nbsp;</p>

<mapml-viewer style="border:0px green solid;" height="600" projection="OSMTILE" lat="47.2943" lon="7.6511" zoom="10" controls>
  <map-layer label="Hintergrundkarte" checked>
    <map-extent label="Plan für das Grundbuch" units="OSMTILE" opacity="0.66" checked hidden>
        <map-input name="w" type="width"></map-input>
        <map-input name="h" type="height"></map-input>
        <map-input name="z" type="zoom" value="24" min="17" max="24" step="1"></map-input>
        <map-input name="xmin" type="location" units="pcrs" position="top-left" axis="easting" min="783975" max="926967" ></map-input>
        <map-input name="ymin" type="location" units="pcrs" position="bottom-left" axis="northing" min="5945675" max="6034311"></map-input>
        <map-input name="xmax" type="location" units="pcrs" position="top-right" axis="easting" min="783975" max="926967"></map-input>
        <map-input name="ymax" type="location" units="pcrs" position="top-left" axis="northing" min="5945675" max="6034311"></map-input>
        <map-link rel="image" tref="https://geo.so.ch/wms?REQUEST=GetMap&crs=EPSG:3857&SERVICE=WMS&BBOX={xmin},{ymin},{xmax},{ymax}&LAYERS=ch.so.agi.hintergrundkarte_sw&FORMAT=image/png&WIDTH={w}&HEIGHT={h}&STYLES=&TRANSPARENT=FALSE&version=1.3.0"></map-link>
    </map-extent>
    <map-extent label="Landeskarte" units="OSMTILE" opacity="0.66" checked hidden>
      <map-input name="z" type="zoom" min="0" max="16"></map-input>
      <map-input name="x" type="location" units="tilematrix" axis="column" min="0" max="262144"></map-input>
      <map-input name="y" type="location" units="tilematrix" axis="row" min="0" max="262144"></map-input>
      <map-link rel="tile" tref="https://wmts.geo.admin.ch/1.0.0/ch.swisstopo.pixelkarte-grau/default/current/3857/{z}/{x}/{y}.jpeg"></map-link>
    </map-extent>
  </map-layer>
  <map-layer label="${product.title()}" checked>
    <map-extent units="OSMTILE" checked hidden>
        <map-input name="w" type="width"></map-input>
        <map-input name="h" type="height"></map-input>
        <map-input name="z" type="zoom" value="24" min="1" max="24" step="1"></map-input>
        <map-input name="xmin" type="location" units="pcrs" position="top-left" axis="easting" min="783975" max="926967" ></map-input>
        <map-input name="ymin" type="location" units="pcrs" position="bottom-left" axis="northing" min="5945675" max="6034311"></map-input>
        <map-input name="xmax" type="location" units="pcrs" position="top-right" axis="easting" min="783975" max="926967"></map-input>
        <map-input name="ymax" type="location" units="pcrs" position="top-left" axis="northing" min="5945675" max="6034311"></map-input>
        <map-link rel="image" tref="https://geo.so.ch/wms?REQUEST=GetMap&crs=EPSG:3857&SERVICE=WMS&BBOX={xmin},{ymin},{xmax},{ymax}&LAYERS=${product.ident_part()}&FORMAT=image/png&WIDTH={w}&HEIGHT={h}&STYLES=&TRANSPARENT=TRUE&version=1.3.0"></map-link>
    </map-extent>
  </map-layer>

</mapml-viewer>

    </div>
</body>
</html>
