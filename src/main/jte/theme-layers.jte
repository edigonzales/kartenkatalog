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
        <span class="layer-icon material-icons-outlined" aria-hidden="true">check</span>        
        <span class="layer-icon material-icons-outlined" aria-hidden="true">info</span>
    </div>
</div>


@endfor
