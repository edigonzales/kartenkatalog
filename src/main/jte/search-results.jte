@import java.util.List
@import ch.so.agi.meta.Thema

@param List<Thema> themen

@for (var thema : themen)
    <tr id="theme-${thema.identifier().replaceAll("[^A-Za-z0-9_-]", "_")}-anchor" class="theme-row">
        <td style="padding-right:0px;">
            <i
                style="cursor: pointer;"
                class="material-icons"
                hx-get="/themen/${thema.identifier()}/ebenen"
                hx-target="#theme-${thema.identifier().replaceAll("[^A-Za-z0-9_-]", "_")}-anchor"
                hx-swap="afterend"
                hx-trigger="click[!this.dataset.loaded || this.dataset.loaded === 'false']"
                hx-on:click="if(this.dataset.loaded==='true'){document.querySelectorAll('.theme-${thema.identifier().replaceAll("[^A-Za-z0-9_-]", "_")}-child').forEach(el => el.remove()); this.dataset.loaded='false';}"
                hx-on:htmx:after-request="this.dataset.loaded='true';"
            >add</i>
        </td>
        <td colspan="2">${thema.title()}</td>
        <td>
            @if(thema.adescription() != null)
                $unsafe{thema.adescription()}
            @endif
        </td>
        <td></td>
        <td></td>
    </tr>
@endfor
