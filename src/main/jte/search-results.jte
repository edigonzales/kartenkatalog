@import java.util.List
@import java.time.LocalDate
@import ch.so.agi.maps.Product

@param List<Product> productList
@param String baseUrl = "https://sos-ch-dk-2.exo.io/ch.so.agi.fubar1/"

@for (var product : productList) 
    <tr>
        <td>
            @if(product.dtype().equals("simiProduct_LayerGroup"))
                <i style="cursor: pointer;" class="material-icons" hx-on:click="this.closest('tbody').querySelectorAll('.child-row-${product.id()}').forEach(el => el.classList.toggle('expanded'))">add</i>
            @else
                <i class="material-icons-two-tone">layers</i>
            @endif
        </td>
        <td>&nbsp;</td>
        <td>${product.id()}</td>
        <td>${product.title()}</td>
        <td>${product.dtype()}</td>
        <td>$unsafe{product.description()}</td>
    </tr>

    @if(product.children().length > 0)

    @for (var child : product.children())
    <tr class="child-row-${product.id()} hidden">
        <td></td>
        <td><i class="material-icons-two-tone">layers</i></td>
        <td>${child.id()}</td>
        <td>${child.title()}</td>
        <td>${child.dtype()}</td>
        <td></td>
    </tr>
@endfor


    @endif
@endfor
    