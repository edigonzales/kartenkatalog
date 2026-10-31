@import ch.so.agi.meta.CatalogText
@import java.util.List
@import ch.so.agi.meta.Ebene

@param List<Ebene> ebenen

@for (var ebene : ebenen)

<div class="layers">
    <div class="layer">
        <span class="layer-text">${ebene.title()}</span>
        <span class="layer-subtext">
            $unsafe{CatalogText.html(CatalogText.description(ebene))}
        </span>

        @if(ebene.transparency() != null && ebene.transparency() != 0)
            <a href="https://geo.so.ch/map?l=${ebene.ident_part()}[${ebene.transparency()}]" target="_blank" rel="noopener noreferrer"><i class="material-icons-outlined black-link">open_in_new</i></a>
        @else
            <a href="https://geo.so.ch/map?l=${ebene.ident_part()}" target="_blank" rel="noopener noreferrer"><i class="material-icons-outlined black-link">open_in_new</i></a>
        @endif

        <a href="ebene/${ebene.ident_part()}" target="_blank" rel="noopener noreferrer"><i class="material-icons-outlined layer-icon">info</i></a>


    </div>
</div>


@endfor
