package ch.so.agi.meta;

import com.microsoft.playwright.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import java.net.*;
import java.net.http.*;
import java.nio.file.*;
import java.time.Duration;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

/** Actual packaged JVM and actual Chromium and Firefox; intentionally separate from the Quarkus test container. */
@Tag("playwright")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class CatalogFilterBrowserTests {
    Process app;
    String base;
    Playwright playwright;
    Browser browser;
    Browser firefox;
    final Path reports=Path.of("build/reports/browser/filters/screenshots");
    @BeforeAll void start() throws Exception {
        Files.createDirectories(reports);
        int port;
        try(var socket=new ServerSocket(0)){port=socket.getLocalPort();}
        base="http://127.0.0.1:"+port;
        String executable=System.getProperty("catalogExecutable","");
        var command=executable.isBlank()
            ? new ProcessBuilder("java","-Dquarkus.http.host=127.0.0.1","-Dquarkus.http.port="+port,"-jar","build/quarkus-app/quarkus-run.jar")
            : new ProcessBuilder(executable,"-Dquarkus.http.host=127.0.0.1","-Dquarkus.http.port="+port);
        command.environment().put("CATALOG_XTF_LOCATION","file:"+Path.of("src/test/resources/search/catalog.xml").toAbsolutePath());
        app=command.redirectErrorStream(true).redirectOutput(Path.of("build/reports/browser/filters/browser-jvm.log").toFile()).start();
        var client=HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(2)).build();
        long deadline=System.nanoTime()+Duration.ofSeconds(30).toNanos();
        boolean ready=false;
        while(System.nanoTime()<deadline) {
            if(!app.isAlive())throw new AssertionError("Browser JVM failed; see browser-jvm.log");
            try { if(client.send(HttpRequest.newBuilder(URI.create(base+"/q/health/ready")).timeout(Duration.ofSeconds(2)).build(),HttpResponse.BodyHandlers.ofString()).statusCode()==200){ready=true;break;} }
            catch(java.io.IOException ignored){}
            Thread.sleep(100);
        }
        assertTrue(ready,"Browser JVM must become ready");
        playwright=Playwright.create(); browser=playwright.chromium().launch(); firefox=playwright.firefox().launch();
    }
    @AfterAll void stop() throws Exception {
        if(firefox!=null)firefox.close();if(browser!=null)browser.close();if(playwright!=null)playwright.close();
        if(app!=null){app.destroy();if(!app.waitFor(10,java.util.concurrent.TimeUnit.SECONDS)){app.destroyForcibly();app.waitFor();}}
    }
    private Browser engine(String name) {return name.equals("firefox")?firefox:browser;}
    private void navigate(Page page,String path) {page.navigate(base+path,new Page.NavigateOptions().setWaitUntil(com.microsoft.playwright.options.WaitUntilState.DOMCONTENTLOADED));}
    private void settle(Page page) {page.waitForFunction("() => !document.querySelector('#catalog-state').hasAttribute('aria-busy') && !document.querySelector('.htmx-swapping')");}
    private void apply(Page page,String kind,String... values) {
        var group=page.locator(".filter-group[data-kind='"+kind+"']");group.locator("summary").click();
        for(String value:values)group.locator("input[value='"+value+"']").check();
        group.locator(".apply-filter").click();settle(page);
    }
    private void snapshot(Page page,String file) {page.evaluate("() => document.fonts.ready");page.screenshot(new Page.ScreenshotOptions().setPath(reports.resolve(file+".png")));}
    private void noOverflow(Page page) {assertTrue((Boolean)page.evaluate("() => document.documentElement.scrollWidth <= innerWidth"));}
    @ParameterizedTest @ValueSource(strings={"chromium","firefox"})
    void desktopFiltersMatchJsonAndHistoryAndExpansionRemainConsistent(String name) {
        try(var context=engine(name).newContext(new Browser.NewContextOptions().setViewportSize(1440,900))) {
            var page=context.newPage();var errors=new ArrayList<String>();page.onPageError(errors::add);navigate(page,"/");
            page.locator("#catalog-search").fill("Atlas");page.locator(".search-field button").click();settle(page);
            assertTrue(page.url().contains("q=Atlas"));
            var group=page.locator(".filter-group[data-kind=thema]");
            double before=page.locator("#results-heading").boundingBox().y;
            group.locator("summary").focus();page.keyboard().press("Enter");
            com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat(group).hasAttribute("open","");
            assertEquals(before,page.locator("#results-heading").boundingBox().y,0.5);
            snapshot(page,name+"-desktop-popover");
            page.keyboard().press("Escape");assertEquals(group.locator("summary").getAttribute("aria-controls"),page.locator(":focus").getAttribute("aria-controls"));
            apply(page,"thema","hydro","plan");apply(page,"typ","datasetview","extwms");apply(page,"organisation","Amt A");
            String selectedUrl=page.url();assertTrue(selectedUrl.contains("thema=hydro"));assertTrue(selectedUrl.contains("thema=plan"));
            assertEquals(5,page.locator("input:checked").count());assertEquals(6,page.locator(".filter-chip").count());
            assertTrue(page.locator("#results-heading").innerText().startsWith("2 Kartenebenen"));
            assertEquals(1,page.locator("#catalog-state").count());assertEquals(1,page.locator("html").count());assertEquals(1,page.locator("h1").count());
            assertEquals("results-heading",page.locator(":focus").getAttribute("id"));
            var theme=page.locator(".theme-row");theme.locator("summary").click();
            page.waitForFunction("() => document.querySelectorAll('.layer-row').length === 2");
            var expected=io.restassured.path.json.JsonPath.from(page.request().get(base+"/api/v1/ebenen?"+URI.create(page.url()).getRawQuery()).text()).getList("id",String.class);
            assertEquals(expected.stream().map(id->"/ebene/"+id).toList(),page.locator(".layer-summary h3 a").evaluateAll("els => els.map(e => e.getAttribute('href'))"));
            snapshot(page,name+"-desktop-selected-expanded");
            page.locator("#catalog-sort").selectOption("title-desc");page.locator(".sort-control button").click();settle(page);
            page.waitForFunction("() => document.querySelectorAll('.layer-row').length === 2");
            assertTrue(page.locator(".theme-row").evaluate("e => e.open").equals(true));
            assertEquals("Zulu",page.locator(".layer-summary h3 a").first().innerText());
            page.locator(".filter-chip").filter(new Locator.FilterOptions().setHasText("Organisation:")).click();settle(page);
            assertFalse(page.url().contains("organisation="));assertEquals(4,page.locator("input:checked").count());
            page.goBack();settle(page);com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat(page.locator("input[name=organisation][value='Amt A']")).isChecked();
            page.reload();assertEquals("title-desc",page.locator("#catalog-sort").inputValue());assertEquals(5,page.locator("input:checked").count());
            navigate(page,selectedUrl.substring(base.length()));assertEquals(5,page.locator("input:checked").count());
            page.locator(".search-clear").click();settle(page);assertEquals("",page.locator("#catalog-search").inputValue());assertEquals(5,page.locator("input:checked").count());
            page.locator(".reset-link").click();settle(page);assertEquals(0,page.locator("input:checked").count());assertEquals(0,page.locator(".filter-chip").count());
            assertTrue(errors.isEmpty(),errors.toString());noOverflow(page);
        }
    }
    @ParameterizedTest @ValueSource(strings={"chromium","firefox"})
    void mobileSheetTrapsFocusRestoresItAndHandlesEmptyResetAndLinks(String name) {
        try(var context=engine(name).newContext(new Browser.NewContextOptions().setViewportSize(390,844))) {
            var page=context.newPage();navigate(page,"/");
            var trigger=page.locator(".mobile-filter-trigger");trigger.click();
            assertTrue((Boolean)page.locator("#filters-dialog").evaluate("e => e.matches(':modal')"));
            assertEquals("Filter schliessen",page.locator(":focus").getAttribute("aria-label"));
            page.keyboard().press("Shift+Tab");assertTrue((Boolean)page.locator(":focus").evaluate("e => !!e.closest('#filters-dialog')"));
            snapshot(page,name+"-mobile-sheet");noOverflow(page);
            page.keyboard().press("Escape");assertEquals("mobile-filter-trigger",page.locator(":focus").getAttribute("class"));
            trigger.click();page.locator("input[name=thema][value=hydro]").check();page.locator("input[name=typ][value=extwms]").check();
            page.locator(".sheet-actions button").click();settle(page);
            assertTrue(page.locator(".empty-state").isVisible());assertEquals(2,page.locator(".filter-chip").count());
            snapshot(page,name+"-mobile-empty");noOverflow(page);
            page.locator(".filter-chip").filter(new Locator.FilterOptions().setHasText("Produkttyp:")).click();settle(page);
            assertEquals(1,page.locator(".theme-row").count());page.locator(".theme-row summary").click();page.waitForFunction("() => document.querySelectorAll('.layer-row').length === 5");
            snapshot(page,name+"-mobile-expanded");noOverflow(page);
            page.locator(".layer-summary h3 a").first().click();assertEquals("Atlas",page.locator("h1").innerText());
            assertTrue(page.locator(".primary-action").getAttribute("href").startsWith("https://geo.so.ch/map?l="));
            snapshot(page,name+"-mobile-detail");noOverflow(page);
        }
    }
    @ParameterizedTest @ValueSource(strings={"chromium","firefox"})
    void withoutJavascriptRepeatedCheckboxGetAndChipsWork(String name) {
        try(var context=engine(name).newContext(new Browser.NewContextOptions().setJavaScriptEnabled(false).setViewportSize(390,844))) {
            var page=context.newPage();navigate(page,"/");page.locator("#catalog-search").fill("Atlas");
            var themes=page.locator(".filter-group[data-kind=thema]");themes.locator("summary").click();
            themes.locator("[value=hydro]").check();themes.locator("[value=plan]").check();
            themes.locator(".apply-filter").click();
            assertTrue(page.url().contains("thema=hydro"));assertTrue(page.url().contains("thema=plan"));assertEquals(2,page.locator("input:checked").count());
            assertEquals(3,page.locator(".filter-chip").count());
            page.locator(".theme-row").first().locator("summary").click();page.locator(".fallback-link a").first().click();assertTrue(page.locator(".layer-row").count()>0);
            page.goBack();page.locator(".filter-chip").filter(new Locator.FilterOptions().setHasText("Thema: Hydrologie")).click();assertEquals(1,page.locator("input:checked").count());
            noOverflow(page);
        }
    }
    @ParameterizedTest @ValueSource(strings={"chromium","firefox"})
    void failedFragmentPreservesResultsAndEscapedSearchCannotInjectMarkup(String name) {
        try(var context=engine(name).newContext(new Browser.NewContextOptions().setViewportSize(1440,900))) {
            var page=context.newPage();navigate(page,"/");
            context.route("**/*", route -> {
                if("true".equals(route.request().headerValue("HX-Request")) && URI.create(route.request().url()).getPath().equals("/"))
                    route.fulfill(new Route.FulfillOptions().setStatus(503).setContentType("application/problem+json").setBody("{\"status\":503}"));
                else route.resume();
            });
            page.locator("#catalog-search").fill("Atlas");page.locator(".search-field button").click();settle(page);
            com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat(page.locator(".catalog-error")).isVisible();
            assertEquals(3,page.locator(".theme-row").count());assertFalse(page.url().contains("q="));
            context.unroute("**/*");
            String payload="\"><img src=x onerror=alert(7)>";
            page.locator("#catalog-search").fill(payload);page.locator(".search-field button").click();settle(page);
            assertEquals(payload,page.locator("#catalog-search").inputValue());assertEquals(0,page.locator("img[src=x],[onerror]").count());
            assertTrue(page.locator(".empty-state").isVisible());
            snapshot(page,name+"-desktop-empty");
            page.locator(".search-clear").click();settle(page);assertEquals(3,page.locator(".theme-row").count());
        }
    }

}
