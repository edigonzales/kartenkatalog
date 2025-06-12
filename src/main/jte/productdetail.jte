@import ch.so.agi.meta.Product

@param Product product
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
            <h1>${product.title()} <a href="https://geo.so.ch/map?l=${product.ident_part()}[${product.transparency()}]" target="_blank"><i class="material-icons-outlined black-link">open_in_new</i></a></h1>
        </div>
        
        <!--<div>${product.service_download()}</div>-->
                                        
        <div class="textedit-container">
            <label class="textedit-label">Beschreibung</label>
            <div class="textedit-display textedit-display-area">
                @if(product.description() != null)
                    $unsafe{product.description()}
                @elseif(product.description_override() != null)
                    $unsafe{product.description_override()}
                @elseif(product.description_model() != null)
                    $unsafe{product.description_model()}
                @endif            
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
            <label class="textedit-label">Layergruppe</label>
            <div class="textedit-display textedit-display-line"> 
                @if(product.parent_ident_part() != null) 
                    <a href="../product/${product.parent_ident_part()}" target="_blank">${product.parent_title()} / ${product.parent_ident_part()}</a>                
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
            <label class="textedit-label">Typ</label>
            <div class="textedit-display textedit-display-line">
                @if(product.children().length > 0)
                    Layergruppe
                @else
                    Einzellayer / Fassadelayer
                @endif       
            </div>
        </div>

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
                    <a href="../product/${product.ident_part()}/style" target="_blank">${product.ident_part()}.qml</a>                
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
        
        <!--
        <mapml-viewer style="border:0px green solid;" height="600" projection="OSMTILE" zoom="10" lat="47.1943" lon="7.5411" controls> 
            <map-layer label="Hintergrundkarte" opacity="0.66" checked>
            
  

          <map-extent label="Landeskarte" units="OSMTILE" checked="checked" hidden="hidden">
            <map-input name="z" type="zoom" min="0" max="16" step="1" />
            <map-input name="x" type="location" units="tilematrix" axis="column" min="0" max="262144" />
            <map-input name="y" type="location" units="tilematrix" axis="row" min="0" max="262144" />
            <map-link
              rel="tile"
              tref="https://wmts.geo.admin.ch/1.0.0/ch.swisstopo.pixelkarte-grau/default/current/3857/{z}/{x}/{y}.jpeg"
            />
          </map-extent>
            
            
            </map-layer>   
        </mapml-viewer>
-->

<mapml-viewer style="border:0px green solid;" height="600" projection="OSMTILE" lat="47.2943" lon="7.6511" zoom="10" controls>
  <map-layer label="Hintergrundkarte" checked>
  
    <map-extent label="Plan für das Grundbuch" units="OSMTILE" checked hidden>
      <map-input name="z" type="zoom" value="24" min="17" max="24" step="1" />
      <map-input name="xmin" type="location" rel="map" position="top-left" axis="easting"
        units="pcrs" min="783975" max="926967" />
      <map-input name="ymin" type="location" rel="map" position="bottom-left" axis="northing"
        units="pcrs" min="5945675" max="6034311" />
      <map-input name="xmax" type="location" rel="map" position="top-right" axis="easting"
        units="pcrs" min="783975" max="926967" />
      <map-input name="ymax" type="location" rel="map" position="top-left" axis="northing"
        units="pcrs" min="5945675" max="6034311" />
      <map-input name="w" type="width" min="1" max="6000" />
      <map-input name="h" type="height" min="1" max="6000" />
      <map-link rel="image" tref="https://geo.so.ch/wms?request=GetMap&amp;crs=EPSG:3857&amp;service=WMS&amp;bbox={xmin},{ymin},{xmax},{ymax}&amp;layers=ch.so.agi.hintergrundkarte_sw&amp;format=image/png&amp;width={w}&amp;styles=population&amp;language=en&amp;version=1.3.0&amp;transparent=true&amp;height={h}"></map-link> 
    </map-extent>
  
    <map-extent label="Landeskarte" units="OSMTILE" opacity="0.66" checked hidden>
      <map-input name="z" type="zoom" min="0" max="16"></map-input>
      <map-input name="x" type="location" units="tilematrix" axis="column" min="0" max="262144"></map-input>
      <map-input name="y" type="location" units="tilematrix" axis="row" min="0" max="262144"></map-input>
      <map-link rel="tile" tref="https://wmts.geo.admin.ch/1.0.0/ch.swisstopo.pixelkarte-grau/default/current/3857/{z}/{x}/{y}.jpeg"></map-link>
    </map-extent>
  </map-layer>
</mapml-viewer>



        
        <!--
        <table class="responsive-table">
            <colgroup>
                <col style="width: 2%" />
                <col style="width: 2%" />
                <col style="width: 22%" />
                <col style="width: 58%" />
                <col style="width: 8%" />
                <col style="width: 8%" />
            </colgroup>

            <thead>
            <tr>
                <th style="padding-right:0px;"></th>
                <th colspan="2">Titel</th>
                <th>Beschreibung</th>
                <th>Karte</th>
                <th>Details</th>
            </tr>
            </thead>
            <tbody id="search-results">
                @template.search-results(productList = productList)
            </tbody>
        </table>    
        -->
    </div>    
</body>
</html>
