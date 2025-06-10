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

    <script src="https://cdn.jsdelivr.net/npm/@materializecss/materialize@2.2.1/dist/js/materialize.min.js"></script>
    <link href="https://fonts.googleapis.com/css?family=Material+Icons|Material+Icons+Outlined|Material+Icons+Two+Tone|Material+Icons+Round|Material+Icons+Sharp" rel="stylesheet">

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
                                        
        <div class="textedit-container">
            <label class="textedit-label">Beschreibung</label>
            <div class="textedit-display" style="min-height:100px;">
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
            <div class="textedit-display" style="min-height:30px;">
                @if(product.keywords() != null) 
                    @for (var keyword : product.keywords().split(","))
                        <span class="badge">${keyword}</span>          
                    @endfor
                @endif
            </div>
        </div>

        <div class="textedit-container">
            <label class="textedit-label">Synonyme</label>
            <div class="textedit-display" style="min-height:30px;">
                @if(product.synonyms() != null) 
                    @for (var synonym : product.synonyms().split(","))
                        <span class="badge">${synonym}</span>          
                    @endfor
                @endif
            </div>
        </div>

               
        <div class="textedit-container">
            <label class="textedit-label">Organisation</label>
            <div class="textedit-display">
                ${product.org_name()}          
            </div>
        </div>
        
        <div class="textedit-container">
            <label class="textedit-label">Layergruppe</label>
            <div class="textedit-display" style="min-height:20px;">  <!-- TODO: class machen und anwenden, dito badge-->
                ${product.parent_ident_part()}          
            </div>
        </div>


        <p>&nbsp;</p>

        <div class="textedit-container">
            <label class="textedit-label">Thema</label>
            <div class="textedit-display">
                ${product.theme_title()}          
            </div>
        </div>

        <p>&nbsp;</p>

        <div class="textedit-container">
            <label class="textedit-label">Typ</label>
            <div class="textedit-display">
                @if(product.children().length > 0)
                    Layergruppe
                @else
                    Einzellayer / Fassadelayer
                @endif       
            </div>
        </div>

        <div class="textedit-container">
            <label class="textedit-label">Verfügbarkeit</label>
            <div class="textedit-display">
                ${product.display_text()}          
            </div>
        </div>
        
        <div class="textedit-container">
            <label class="textedit-label">QML</label>
            <div class="textedit-display" style="vertical-align:center;">
                <a href="../product/${product.ident_part()}/style" target="_blank">${product.ident_part()}.qml</a>
            </div>
        </div>

        <div class="textedit-container">
            <label class="textedit-label">Interne Bemerkungen</label>
            <div class="textedit-display">
                ${product.remarks()}          
            </div>
        </div>

        <div class="textedit-container">
            <label class="textedit-label">Permissions</label>
            <div class="textedit-display">
                ${product.permissions()}          
            </div>
        </div>
        
        
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
