@import java.util.List
@import ch.so.agi.meta.Product

@param List<Product> productList

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge" />
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=5.0, user-scalable=0" />
    <meta name="mobile-web-app-capable" content="yes" />

    <title>Kartenportal • Kanton Solothurn</title>
    
    <script src="https://unpkg.com/htmx.org@2.0.4"></script>

    <!--
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/@materializecss/materialize@2.2.1/dist/css/materialize.min.css">
    -->
    <script src="https://cdn.jsdelivr.net/npm/@materializecss/materialize@2.2.1/dist/js/materialize.min.js"></script>
    <link href="https://fonts.googleapis.com/css?family=Material+Icons|Material+Icons+Outlined|Material+Icons+Two+Tone|Material+Icons+Round|Material+Icons+Sharp" rel="stylesheet">

    <link rel="stylesheet" href="fonts.css">
    <link rel="stylesheet" href="kartenkatalog-debug2.css">

</head>

<body>

@for (var product : productList) 

<div class="card-container">
  <div class="card">
    <div class="card-header">
      <h2>${product.title()}</h2>
      <p>
            @if(product.description() != null) 
                $unsafe{product.description()}
            @elseif(product.description_override() != null) 
                $unsafe{product.description_override()}
            @elseif(product.description_model() != null) 
                $unsafe{product.description_model()}            
            @endif      
      </p>
      <button 
        hx-get="/kartenebenen/1/kinder"
        hx-target="#children-1"
        hx-swap="innerHTML toggle"
        class="expand-btn"
      >
        ▼ Kinder anzeigen
      </button>
    </div>
    <div id="children-1" class="children"></div>
  </div>
</div>

@endfor

</body>
</html>
