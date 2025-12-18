@import java.util.List
@import ch.so.agi.meta.Ebene

@param List<Ebene> ebenen
@param String domIdentifier

@for (var ebene : ebenen)

<div class="layers">
    <div class="layer">
        <span class="layer-text">${ebene.title()}</span>
        <span class="layer-subtext">
            @if(ebene.adescription() != null)
                $unsafe{ebene.adescription()}
            @elseif(ebene.description_override() != null)
                $unsafe{ebene.description_override()}
            @elseif(ebene.description_model() != null)
                $unsafe{ebene.description_model()}
            @endif        
        </span>
        
        @if(ebene.transparency() != null && !ebene.transparency().equals("0"))
            <a href="https://geo.so.ch/map?l=${ebene.ident_part()}[${ebene.transparency()}]" target="_blank"><i class="material-icons-outlined black-link">open_in_new</i></a>
        @else
            <a href="https://geo.so.ch/map?l=${ebene.ident_part()}" target="_blank"><i class="material-icons-outlined black-link">open_in_new</i></a>                
        @endif             
        
        <a href="ebene/${ebene.ident_part()}" target="_blank"><i class="material-icons-outlined layer-icon">info</i></a>
        
        
    </div>
</div>


@endfor
