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
class CatalogBrowserTests {
    Process app;
    String base;
    Playwright playwright;
    Browser browser;
    Browser firefox;
    final Path reports=Path.of("build/reports/browser/catalog/screenshots");
    @BeforeAll void start() throws Exception {
        Files.createDirectories(reports);
        int port;
        try(var socket=new ServerSocket(0)){port=socket.getLocalPort();}
        base="http://127.0.0.1:"+port;
        String executable=System.getProperty("catalogExecutable","");
        var command=executable.isBlank()
            ? new ProcessBuilder("java","-Dquarkus.http.host=127.0.0.1","-Dquarkus.http.port="+port,"-jar","build/quarkus-app/quarkus-run.jar")
            : new ProcessBuilder(executable,"-Dquarkus.http.host=127.0.0.1","-Dquarkus.http.port="+port);
        command.environment().put("CATALOG_XTF_LOCATION","file:"+Path.of("data/kartenkatalog-harvest-ohne-qml.xtf").toAbsolutePath());
        app=command.redirectErrorStream(true).redirectOutput(Path.of("build/reports/browser/catalog/browser-jvm.log").toFile()).start();
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
    @ParameterizedTest @ValueSource(strings={"chromium","firefox"}) void desktopLoadsLocalAssetsAndAccessibleDisclosuresWithoutDuplicateFetches(String engine) {
        try(var context=(engine.equals("firefox")?firefox:browser).newContext(new Browser.NewContextOptions().setViewportSize(1440,900))) {
            var external=new ArrayList<String>();var errors=new ArrayList<String>();var fragments=new ArrayList<String>();
            context.route("**/*",route->{if(!route.request().url().startsWith(base+"/")){external.add(route.request().url());route.abort();}else route.resume();});
            context.onRequest(request->{if(request.url().contains("/themen/")&&request.url().contains("/ebenen"))fragments.add(request.url());});
            Page page=context.newPage();page.onPageError(errors::add);
            page.navigate(base+"/", new Page.NavigateOptions().setWaitUntil(com.microsoft.playwright.options.WaitUntilState.DOMCONTENTLOADED));page.waitForFunction("() => customElements.get('so-header') !== undefined");
            page.evaluate("() => document.fonts.ready");
            assertEquals(169,page.locator("details.theme-row").count());
            assertEquals(0,page.locator(".provenance").count());
            assertEquals(0,page.locator(".layer-row").count());
            assertTrue(page.locator("so-header").isVisible());assertTrue(page.locator("so-breadcrumb").isVisible());
            page.screenshot(new Page.ScreenshotOptions().setPath(reports.resolve(engine+"-catalog-desktop.png")));
            page.keyboard().press("Tab");assertEquals("Zum Inhalt springen",page.locator(":focus").innerText());
            page.keyboard().press("Enter");assertEquals("main-content",page.locator(":focus").getAttribute("id"));
            var row=page.locator("details.theme-row").first();var summary=row.locator("summary");
            int expected=Integer.parseInt(row.locator(".theme-count").innerText().split(" ")[0]);
            summary.focus();summary.press("Enter");
            page.waitForFunction("n => document.querySelector('details.theme-row .theme-layers').querySelectorAll('.layer-row').length === n",expected);
            assertEquals("true",summary.getAttribute("aria-expanded"));
            assertEquals(expected,row.locator(".layer-row").count());
            page.screenshot(new Page.ScreenshotOptions().setPath(reports.resolve(engine+"-catalog-expanded-desktop.png")));
            summary.press("Enter");page.waitForFunction("() => !document.querySelector('details.theme-row').open");
            summary.press("Enter");page.waitForFunction("() => document.querySelector('details.theme-row').open");
            assertEquals(1,fragments.size());assertEquals(expected,row.locator(".layer-row").count());
            page.navigate(base+"/ebene/ch.so.agi.gemeindegrenzen");page.evaluate("() => document.fonts.ready");
            assertEquals("Gemeindegrenzen",page.locator("h1").innerText());
            assertEquals("https://geo.so.ch/map?l=ch.so.agi.gemeindegrenzen",page.locator("a.primary-action").getAttribute("href"));
            assertEquals(0,page.locator("a[download]").count());
            page.screenshot(new Page.ScreenshotOptions().setPath(reports.resolve(engine+"-detail-desktop.png")));
            assertTrue(external.isEmpty(),external.toString());assertTrue(errors.isEmpty(),errors.toString());
        }
    }
    @ParameterizedTest @ValueSource(strings={"chromium","firefox"}) void noJavascriptFallbackAndSimpleGetSearchRemainUsable(String engine) {
        try(var context=(engine.equals("firefox")?firefox:browser).newContext(new Browser.NewContextOptions().setJavaScriptEnabled(false).setViewportSize(1440,900))) {
            Page page=context.newPage();page.navigate(base+"/", new Page.NavigateOptions().setWaitUntil(com.microsoft.playwright.options.WaitUntilState.DOMCONTENTLOADED));
            assertTrue(page.locator(".header-fallback").isVisible());assertTrue(page.locator(".breadcrumb-fallback").isVisible());
            var row=page.locator("details.theme-row").first();row.locator("summary").click();row.locator(".fallback-link a").click();
            assertTrue(page.url().contains("/themen/"));assertTrue(page.locator(".layer-row").count()>0);
            page.navigate(base+"/", new Page.NavigateOptions().setWaitUntil(com.microsoft.playwright.options.WaitUntilState.DOMCONTENTLOADED));page.locator("#catalog-search").fill("Gemeindegrenzen");page.locator(".search-field button").click();
            assertTrue(page.url().contains("q=Gemeindegrenzen"));assertTrue(page.locator("details.theme-row").count()>0);
            page.locator("details.theme-row").first().locator("summary").click();page.locator(".fallback-link a").first().click();
            assertTrue(page.locator(".layer-row").count()>0);assertTrue(page.locator(".layer-list").innerText().contains("Gemeindegrenzen"));
            page.navigate(base+"/?q=nonexistentqxyz");assertEquals(0,page.locator("details.theme-row").count());assertTrue(page.locator(".empty-state").innerText().contains("Keine Kartenebenen"));
        }
    }
    @ParameterizedTest @ValueSource(strings={"chromium","firefox"}) void narrowLayoutAndReferenceCapture(String engine) {
        try(var context=(engine.equals("firefox")?firefox:browser).newContext(new Browser.NewContextOptions().setViewportSize(390,844))) {
            Page page=context.newPage();page.navigate(base+"/", new Page.NavigateOptions().setWaitUntil(com.microsoft.playwright.options.WaitUntilState.DOMCONTENTLOADED));page.waitForFunction("() => customElements.get('so-header') !== undefined");page.evaluate("() => document.fonts.ready");
            assertTrue((Boolean)page.evaluate("() => document.documentElement.scrollWidth <= window.innerWidth"));
            page.screenshot(new Page.ScreenshotOptions().setPath(reports.resolve(engine+"-catalog-mobile.png")));
            page.navigate(base+"/ebene/ch.so.agi.gemeindegrenzen");page.evaluate("() => document.fonts.ready");
            assertTrue((Boolean)page.evaluate("() => document.documentElement.scrollWidth <= window.innerWidth"));
            page.screenshot(new Page.ScreenshotOptions().setPath(reports.resolve(engine+"-detail-mobile.png")));
        }
        String reference=System.getProperty("referenceUrl","");
        if(!reference.isBlank())for(int[] size:new int[][]{{1440,900},{390,844}}) {
            try(var context=(engine.equals("firefox")?firefox:browser).newContext(new Browser.NewContextOptions().setViewportSize(size[0],size[1]))) {
                Page page=context.newPage();page.navigate(reference+"/datasets?view=list");page.waitForFunction("() => customElements.get('so-header') !== undefined");page.evaluate("() => document.fonts.ready");
                page.screenshot(new Page.ScreenshotOptions().setPath(reports.resolve(engine+"-sodata-"+size[0]+".png")));
                page.locator(size[0]>700?"[data-filter-trigger]":"[data-mobile-filter-trigger]").first().click();
                page.locator("[data-filter-panel]").waitFor();
                page.screenshot(new Page.ScreenshotOptions().setPath(reports.resolve(engine+"-sodata-filter-"+size[0]+".png")));
            }
        }
    }
}
