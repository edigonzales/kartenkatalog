@import ch.so.agi.meta.CatalogText
@import java.util.List
@import ch.so.agi.meta.Thema

@param List<Thema> themen

<!DOCTYPE html>

<html lang="de">
<head>
    <meta charset="UTF-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge" />
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=5.0, user-scalable=0" />
    <meta name="mobile-web-app-capable" content="yes" />

    <title>Kartenportal • Kanton Solothurn</title>

    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/so-web-components@0.1.10/dist/styles/reset.css" />
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/so-web-components@0.1.10/dist/styles/fonts.css" />
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/so-web-components@0.1.10/dist/styles/tokens.css" />
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.13.2/font/bootstrap-icons.min.css" />
    
    <link rel="stylesheet" href="kartenkatalog2.css">
    <link href="https://fonts.googleapis.com/css?family=Material+Icons|Material+Icons+Outlined|Material+Icons+Two+Tone|Material+Icons+Round|Material+Icons+Sharp" rel="stylesheet">

    <script type="module" src="https://cdn.jsdelivr.net/npm/so-web-components@0.1.10/dist/index.js"></script>
    <script src="https://unpkg.com/htmx.org@2.0.11"></script>
</head>

<body class="so-page">

<header>
    <so-header
        active-section="Verwaltung"
        site-name="Kanton Solothurn">
    </so-header>

    <so-breadcrumb>
        <so-breadcrumb-item href="https://so.ch">so.ch</so-breadcrumb-item>
        <so-breadcrumb-item href="https://so.ch/verwaltung/">Verwaltung</so-breadcrumb-item>
        <so-breadcrumb-item href="https://so.ch/verwaltung/bau-und-justizdepartement/">Bau- und Justizdepartement</so-breadcrumb-item>
        <so-breadcrumb-item href="https://so.ch/verwaltung/bau-und-justizdepartement/amt-fuer-geoinformation/">Amt für Geoinformation</so-breadcrumb-item>
        <so-breadcrumb-item isCurrentPage>Kartenkatalog</so-breadcrumb-item>
    </so-breadcrumb>
</header>


<main class="so-main so-main--wide">
    <h1 class="so-page-title">Kartenkatalog</h1>
    <so-lead-text><p>Entdecken Sie Themen und Kartenebenen des Kantons Solothurn.</p></so-lead-text>
    
        <div style="margin-top:60px;">               
            @for (var thema : themen)
                <details
                    hx-get="themen/${thema.identifier()}/ebenen"
                    hx-trigger="toggle once"
                    hx-target="#theme-${thema.identifier().replaceAll("[^A-Za-z0-9_-]", "_")}-anchor"
                    hx-swap="innerHTML"
                >
                    <summary>
                        <span class="sum-grid">
                            <span class="acc-title">${thema.title()}</span>
                            @if(thema.ebenen_count() == 1)
                                <span class="badge">1 Ebene</span>                            
                            @else
                                <span class="badge">${thema.ebenen_count()} Ebenen</span>                            
                            @endif
                            
                            
                            <span class="acc-desc">
                                @if(thema.adescription() != null)
                                    $unsafe{CatalogText.html(thema.adescription())}
                                @endif                                                    
                            </span>
                            @if(thema.further_info_url() != null)
                                <a class="acc-info" href="${thema.further_info_url()}" aria-label="Weitere Informationen" target="_blank" rel="noopener noreferrer"><span class="material-icons-outlined" aria-hidden="true">info</span></a>
                            @endif
                        </span>      
                    </summary>
                    <div id="theme-${thema.identifier().replaceAll("[^A-Za-z0-9_-]", "_")}-anchor" class="acc-body">
                        <em>Loading…</em>.
                    </div>
                </details>
            @endfor                
        </div>
    
        
</main>    

</body>

<script>
  document.addEventListener("click", (e) => {
    if (e.target.closest("summary a")) e.stopPropagation();
  });
</script>

</html>



