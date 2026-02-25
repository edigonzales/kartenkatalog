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

    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/so-web-components@0.1.8/dist/styles/reset.css" />
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/so-web-components@0.1.8/dist/styles/fonts.css" />
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/so-web-components@0.1.8/dist/styles/tokens.css" />
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css" />
    
    <link rel="stylesheet" href="kartenkatalog2.css">
    <link rel="stylesheet" href="fonts.css">
    <link href="https://fonts.googleapis.com/css?family=Material+Icons|Material+Icons+Outlined|Material+Icons+Two+Tone|Material+Icons+Round|Material+Icons+Sharp" rel="stylesheet">

    <script type="module" src="https://cdn.jsdelivr.net/npm/so-web-components@0.1.8/dist/index.js"></script>
    <script src="https://unpkg.com/htmx.org@2.0.8"></script>
</head>

<body class="so-page">

<header class="so-page-header">
    <so-header
        active-section="Verwaltung"
        site-name="Kanton Solothurn">
    </so-header>

    <so-breadcrumb class="so-page-breadcrumb">
        <so-breadcrumb-item href="https://so.ch">so.ch</so-breadcrumb-item>
        <so-breadcrumb-item href="https://so.ch/verwaltung/">Verwaltung</so-breadcrumb-item>
        <so-breadcrumb-item href="https://so.ch/verwaltung/bau-und-justizdepartement/">Bau- und Justizdepartement</so-breadcrumb-item>
        <so-breadcrumb-item href="https://so.ch/verwaltung/bau-und-justizdepartement/amt-fuer-geoinformation/">Amt für Geoinformation</so-breadcrumb-item>
        <so-breadcrumb-item isCurrentPage>Kartenkatalog</so-breadcrumb-item>
    </so-breadcrumb>
</header>


    <div style="margin-top:20px;margin-left:20px;text-align:left;">
        <img src="kantons-logo.svg" alt="Logo Kanton Solothurn">
    </div>

    <div class="container">

        <div class="left-align" style="margin-top:120px;">
            <h1>Kartenkatalog</h1>
        </div>

        <div class="left-align" style="margin-top:30px; line-height: 1.42857143;">
            Lorem ipsum odor amet, consectetuer adipiscing elit. Nisi donec vulputate posuere auctor dictum ante. Eu convallis mi consequat per sollicitudin vestibulum odio ut maximus. Maecenas tellus sodales eget nunc volutpat nam integer? Efficitur lobortis posuere aptent id aliquam nec nibh litora. Accumsan enim feugiat morbi, integer pharetra ad. Nec aenean rhoncus ridiculus arcu sapien est vitae. Natoque nisl arcu habitasse morbi netus egestas justo. Rhoncus tellus pellentesque vivamus purus phasellus varius suscipit.
            <br/><br/>Ad diam tristique netus conubia dui maecenas pretium aenean. Ridiculus dictum ligula pretium, ridiculus maecenas tortor. Velit pulvinar lectus hac pretium inceptos duis? Tristique facilisi neque odio; dapibus praesent lobortis. Proin ex gravida magnis scelerisque tristique venenatis. Ligula ex facilisis montes vestibulum vel etiam non?         
        </div>
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
                                    $unsafe{thema.adescription()}
                                @endif                                                    
                            </span>
                            <a class="acc-info" href="/more-info" aria-label="More information">
                                <span class="material-icons-outlined" aria-hidden="true">info</span>
                            </a>
                        </span>      
                    </summary>
                    <div id="theme-${thema.identifier().replaceAll("[^A-Za-z0-9_-]", "_")}-anchor" class="acc-body">
                        <em>Loading…</em>.
                    </div>
                </details>
            @endfor                
        </div>
    </div>
</body>

<script>
  document.addEventListener("click", (e) => {
    if (e.target.closest("summary a")) e.stopPropagation();
  });
</script>

</html>



