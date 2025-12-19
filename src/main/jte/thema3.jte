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

    <script src="https://unpkg.com/htmx.org@2.0.4"></script>

    <!--
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/@materializecss/materialize@2.2.1/dist/css/materialize.min.css">
    -->
    <script src="https://cdn.jsdelivr.net/npm/@materializecss/materialize@2.2.1/dist/js/materialize.min.js"></script>
    <link href="https://fonts.googleapis.com/css?family=Material+Icons|Material+Icons+Outlined|Material+Icons+Two+Tone|Material+Icons+Round|Material+Icons+Sharp" rel="stylesheet">

    <link rel="stylesheet" href="fonts.css">
    <link rel="stylesheet" href="kartenkatalog-debug.css">

</head>

<body>
    <div class="container">
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
                @template.search-results(themen = themen)
            </tbody>
        </table>
    </div>
</body>
</html>
