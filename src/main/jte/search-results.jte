@import java.util.List
@import java.time.LocalDate
@import ch.so.agi.maps.Product

@param List<Product> productList
@param String baseUrl = "https://sos-ch-dk-2.exo.io/ch.so.agi.fubar1/"

@for (var product : productList) 
    <tr>
        <td>
            @if(product.dtype().equals("simiProduct_LayerGroup"))
                <i class="material-icons">add</i>
            @endif
        </td>
        <td>${product.id()}</td>
        <td>${product.title()}</td>
        <td><!--${product.ident_part()}--></td>
        <td><!--${product.description()}--></td>
    </tr>
@endfor
    