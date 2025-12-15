@import java.util.List
@import ch.so.agi.meta.Ebene

@param List<Ebene> ebenen
@param String domIdentifier

<tr class="theme-${domIdentifier}-child child-header">
    <td></td>
    <td></td>
    <td colspan="4"><strong>Ebenen</strong></td>
</tr>
@for (var ebene : ebenen)
<tr class="theme-${domIdentifier}-child">
    <td></td>
    <td style="padding-right:0px;"><i class="material-icons-outlined">map</i></td>
    <td>${ebene.title()}</td>
    <td>
        @if(ebene.adescription() != null)
            $unsafe{ebene.adescription()}
        @elseif(ebene.description_override() != null)
            $unsafe{ebene.description_override()}
        @elseif(ebene.description_model() != null)
            $unsafe{ebene.description_model()}
        @endif
    </td>
    <td>
        @if(ebene.transparency() != null && ebene.transparency() != 0)
            <a href="https://geo.so.ch/map?l=${ebene.ident_part()}[${ebene.transparency()}]" target="_blank"><i class="material-icons-outlined black-link">open_in_new</i></a>
        @else
            <a href="https://geo.so.ch/map?l=${ebene.ident_part()}" target="_blank"><i class="material-icons-outlined black-link">open_in_new</i></a>
        @endif
    </td>
    <td><a href="/ebene/${ebene.ident_part()}" target="_blank"><i class="material-icons-outlined black-link">info</i></a></td>
</tr>
@endfor
