package ch.so.agi.meta.web;

import ch.so.agi.meta.*;
import ch.so.agi.meta.api.CatalogProblem;
import ch.so.agi.meta.search.*;
import io.quarkus.qute.RawString;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.*;
import static ch.so.agi.meta.web.CatalogViewModels.*;

@ApplicationScoped
public class CatalogPageVmFactory {
    private final CatalogService catalog;
    private final CatalogSearchService search;
    public CatalogPageVmFactory(CatalogService catalog, CatalogSearchService search) { this.catalog=catalog; this.search=search; }

    public CatalogPage catalog(CatalogQuery query) {
        var result=search.search(query);
        var counts=new HashMap<String,Integer>(); result.layers().forEach(e->counts.merge(e.thema_r(),1,Integer::sum));
        boolean filtered=!query.q().isEmpty() || !query.thema().isEmpty() || !query.typ().isEmpty() || !query.organisation().isEmpty();
        var themes=catalog.findAllThemen();
        var rows=themes.stream().filter(t->!filtered || counts.getOrDefault(t.t_id(),0)>0)
            .map(t->new ThemeRow(t.identifier(),t.title(),summary(t.adescription()),counts.getOrDefault(t.t_id(),0),
                CatalogUrlFactory.themeQuery(t.identifier(),query),"theme-"+CatalogUrlFactory.encoded(t.identifier()))).toList();
        var facets=search.facets(query);
        return new CatalogPage(chrome(null,null),rows,result.total(),query.q(),
            facets.thema().stream().map(f->new Option(f.value(),f.label(),f.selected())).toList(),
            facets.typ().stream().map(f->new Option(f.value(),type(f.label()),f.selected())).toList(),
            facets.organisation().stream().map(f->new Option(f.value(),f.label(),f.selected())).toList());
    }
    public ThemePage theme(String identifier, CatalogQuery query) {
        var t=catalog.findThema(identifier).orElseThrow(()->new CatalogProblem(404,"Thema nicht gefunden"));
        var scoped=new CatalogQuery(query.q(),Set.of(identifier),query.typ(),query.organisation(),query.sort());
        var rows=search.search(scoped).layers().stream().map(e->new LayerRow(e.title(),CatalogUrlFactory.layer(e.ident_part()),
            CatalogUrlFactory.mapUrl(e),type(e.dtype()),summary(CatalogText.description(e)))).toList();
        return new ThemePage(chrome(t.title(),null),t.title(),safe(t.adescription()),CatalogUrlFactory.external(t.further_info_url()),rows);
    }
    public Detail detail(String identifier) {
        var e=catalog.findEbeneByIdentPart(identifier).orElseThrow(()->new CatalogProblem(404,"Kartenebene nicht gefunden"));
        var t=catalog.findAllThemen().stream().filter(theme->theme.t_id().equals(e.thema_r())).findFirst().orElseThrow();
        String themeHref=CatalogUrlFactory.theme(t.identifier());
        return new Detail(chrome(e.title(),new Crumb(t.title(),themeHref)),e.title(),e.ident_part(),type(e.dtype()),
            CatalogUrlFactory.mapUrl(e),e.style_server()==null||e.style_server().isBlank()?null:CatalogUrlFactory.layer(e.ident_part())+"/style",
            safe(CatalogText.description(e)),CatalogText.terms(e.keywords()),CatalogText.terms(e.synonyms()),e.org_name(),e.display_text(),
            e.service_download()==null?null:(e.service_download()?"Download verfügbar":"Kein Download verfügbar"),t.title(),themeHref,
            CatalogUrlFactory.external(t.further_info_url()));
    }
    private static Chrome chrome(String current, Crumb theme) {
        var crumbs=new ArrayList<Crumb>();
        crumbs.add(new Crumb("so.ch","https://so.ch")); crumbs.add(new Crumb("Verwaltung","https://so.ch/verwaltung/"));
        crumbs.add(new Crumb("Amt für Geoinformation","https://so.ch/verwaltung/bau-und-justizdepartement/amt-fuer-geoinformation/"));
        crumbs.add(new Crumb("Kartenkatalog",current==null?null:"/"));
        if(theme!=null)crumbs.add(theme);
        if(current!=null)crumbs.add(new Crumb(current,null));
        return new Chrome(List.copyOf(crumbs));
    }
    private static RawString safe(String source) {
        String clean=CatalogText.html(source);
        return clean==null||CatalogText.plain(clean).isBlank()?null:new RawString(clean);
    }
    private static String summary(String source) {
        String text=CatalogText.plain(source);
        if(text==null||text.isBlank())return null;
        return text.codePointCount(0,text.length())>230?text.substring(0,text.offsetByCodePoints(0,230))+" …":text;
    }
    private static String type(String raw) {
        return switch(raw) {
            case "datasetview" -> "Kartenebene";
            case "layergroup" -> "Layergruppe";
            case "facadelayer" -> "Fassadenlayer";
            case "extwms" -> "Externer WMS";
            default -> raw;
        };
    }
}
