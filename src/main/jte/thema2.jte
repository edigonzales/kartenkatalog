@import java.util.List
@import ch.so.agi.meta.Thema

@param List<Thema> themen

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge" />
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=5.0, user-scalable=0" />
    <meta name="mobile-web-app-capable" content="yes" />

    <title>Kartenportal • Kanton Solothurn</title>

    <script src="https://unpkg.com/htmx.org@2.0.8"></script>
    
    <link href="https://fonts.googleapis.com/css?family=Material+Icons|Material+Icons+Outlined|Material+Icons+Two+Tone|Material+Icons+Round|Material+Icons+Sharp" rel="stylesheet">

    <link rel="stylesheet" href="fonts.css">
    <link rel="stylesheet" href="kartenkatalog2.css">

</head>

<body>

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
                <details>
                    <summary>
                        <span class="sum-grid">
                            <span class="acc-title">${thema.title()}</span>
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
                    <div class="acc-body">
                        ...details content...
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



