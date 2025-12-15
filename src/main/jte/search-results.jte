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
                hx-on:click="if(this.dataset.loaded==='true'){document.querySelectorAll('.theme-${thema.identifier().replaceAll("[^A-Za-z0-9_-]", "_")}-child').forEach(el => el.classList.toggle('hidden')); event.preventDefault();}"
                hx-on::after-request="this.dataset.loaded='true'; document.querySelectorAll('.theme-${thema.identifier().replaceAll("[^A-Za-z0-9_-]", "_")}-child').forEach(el => el.classList.remove('hidden'));"
            >add</i>
        </td>
        <td style="padding-right:0px;"><i class="material-icons-outlined">folder</i></td>
        <td>${thema.title()}</td>
        <td>
            @if(thema.adescription() != null)
                $unsafe{thema.adescription()}
            @endif
        </td>
        <td></td>
        <td></td>
    </tr>
@endfor
