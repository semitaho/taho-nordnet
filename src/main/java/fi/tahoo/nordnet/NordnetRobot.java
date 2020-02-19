package fi.tahoo.nordnet;

import com.gargoylesoftware.htmlunit.AjaxController;
import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.WebRequest;
import com.gargoylesoftware.htmlunit.WebResponse;
import com.gargoylesoftware.htmlunit.WebWindowEvent;
import com.gargoylesoftware.htmlunit.WebWindowListener;
import com.gargoylesoftware.htmlunit.html.DomElement;
import com.gargoylesoftware.htmlunit.html.DomNodeList;
import com.gargoylesoftware.htmlunit.html.Html;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlButton;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlInput;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlPasswordInput;
import com.gargoylesoftware.htmlunit.html.HtmlSelect;
import com.gargoylesoftware.htmlunit.html.HtmlSpan;
import com.gargoylesoftware.htmlunit.html.HtmlTable;
import com.gargoylesoftware.htmlunit.html.HtmlTableBody;
import com.gargoylesoftware.htmlunit.html.HtmlTableCell;
import com.gargoylesoftware.htmlunit.html.HtmlTableRow;
import com.gargoylesoftware.htmlunit.html.HtmlTextInput;
import com.gargoylesoftware.htmlunit.util.WebConnectionWrapper;
import fi.tahoo.nordnet.model.OmaOsake;
import fi.tahoo.nordnet.model.YleisnakymaResponse;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class NordnetRobot {

  private WebClient webClient;

  public NordnetRobot() {
    //java.util.logging.Logger.getLogger("com.gargoylesoftware").setLevel(Level.OFF);
    try (WebClient webClient = new WebClient(BrowserVersion.FIREFOX_60)) {
      this.webClient = webClient;
      this.webClient.setAjaxController(new AjaxController(){


        @Override
        public boolean processSynchron(HtmlPage page, WebRequest request, boolean async) {
          System.out.println("AJAX METHOD: "+ request.getHttpMethod()+ " "+  request.getUrl());

          return super.processSynchron(page, request, async);
        }
      });
      new WebConnectionWrapper(webClient) {

        @Override
        public WebResponse getResponse(final WebRequest request) throws IOException {
          System.out.println("METHOD: "+ request.getHttpMethod()+ " "+  request.getUrl());

          final WebResponse response = super.getResponse(request);
          return response;
        }
      };
      webClient.getOptions().setUseInsecureSSL(true);
      webClient.getOptions().setCssEnabled(true);
      webClient.getOptions().setRedirectEnabled(true);
      webClient.getOptions().setJavaScriptEnabled(true);
      webClient.getOptions().setThrowExceptionOnFailingStatusCode(false);
      webClient.getCookieManager().setCookiesEnabled(true);
      webClient.getOptions().setThrowExceptionOnScriptError(false);
      login();

    }
  }

  public YleisnakymaResponse checkYleisnakyma() {
    try {

      HtmlPage page = webClient.getPage(
              new URL("https://www.nordnet.fi/yleisnakyma/tili/3"));
      webClient.waitForBackgroundJavaScript(5000);

      BigDecimal saldo = findYleisnakymaSaldo(page);
      List<OmaOsake> osakkeet = findYleisnakymaOsakkeet(page);
      List<String> avoimetToimeksiannot = findAvoimetToimeksiannot();
      return new YleisnakymaResponse(saldo, osakkeet, avoimetToimeksiannot);
    } catch (IOException ioe) {
      throw new FunctionException(ioe);

    }
  }

  private BigDecimal findYleisnakymaSaldo(HtmlPage page) {
    List<Object> byXPath = page.getByXPath("//div[header[h2[contains(text(),'Käytettävissä')]]]/div//*[@value]");
    return byXPath.stream().map(o -> (HtmlSpan) o)
            .map((HtmlSpan span) -> span.getAttribute("value"))
            .map(BigDecimal::new)
            .findFirst()
            .orElse(null);

  }

  private List<String> findAvoimetToimeksiannot() throws FunctionException {
    try {
      HtmlPage page = webClient.getPage("https://www.nordnet.fi/toimeksiannot-kaupat");
      webClient.waitForBackgroundJavaScript(4000);
      HtmlTable table = page.getFirstByXPath("//div[header//*[contains(text(), 'Pörssilistatut')]]//table");
      if (table == null || table.getBodies() == null || table.getBodies().isEmpty()) {
        return Collections.emptyList();
      }
      return table.getBodies()
              .iterator()
              .next()
              .getRows()
              .stream()
              .map((HtmlTableRow row) -> {
                HtmlTableCell osakeCell = row.getCell(2);
                return osakeCell.asText().trim();
              }).collect(Collectors.toList());
    } catch (IOException e) {
      throw new FunctionException(e);
    }

  }

  private List<OmaOsake> findYleisnakymaOsakkeet(HtmlPage page) {
    DomNodeList<DomElement> elementsByTagName = page.getElementsByTagName("table");
    HtmlTable table = (HtmlTable) elementsByTagName.get(0);
    if (table == null) return Collections.emptyList();
    HtmlTableBody htmlTableBody = table.getBodies().iterator().next();

    List<OmaOsake> osakkeet = htmlTableBody.getRows()
            .stream()
            .map((HtmlTableRow row) -> {
              final String osakkeenNimi = getOsakkeenNimi(row);
              final Integer maara = Integer.valueOf(row.getCell(2).getTextContent());
              String ostohintaPerOsake = row.getCell(3).asText().replace("EUR", "");
              BigDecimal ostohinta = new BigDecimal((ostohintaPerOsake.trim()).replace(",", "."));
              return new OmaOsake(osakkeenNimi, maara, ostohinta);
            }).collect(Collectors.toList());
    return osakkeet;

  }

  private String getOsakkeenNimi(HtmlTableRow row) {
    HtmlTableCell cell = row.getCell(1);
    DomNodeList<HtmlElement> spanElements = cell.getElementsByTagName("span");
    final String osakeNimi = spanElements.get(spanElements.size() - 2).getTextContent().replace("Finland", "");
    return osakeNimi.trim();
  }

  private void login() throws FunctionException {
    final HtmlPage page;
    try {
      page = webClient.getPage("https://classic.nordnet.fi/mux/login/start.html?state=signin");
      webClient.waitForBackgroundJavaScript(4000);
      HtmlTextInput textInput = page.getHtmlElementById("username");
      HtmlPasswordInput passwordInput = page.getHtmlElementById("password");
      textInput.type(System.getenv("NORDNET_USERNAME"));
      passwordInput.type(System.getenv("NORDNET_PASSWORD"));

      HtmlForm htmlForm = page.getForms().get(0);
      HtmlButton button = (HtmlButton) htmlForm.getElementsByTagName("button").get(0);
      button.click();
      webClient.waitForBackgroundJavaScript(4000);

    } catch (IOException e) {
      throw new FunctionException(e);
    }

  }

  public void myy(OmaOsake omaOsake) throws FunctionException {
    HtmlPage page = null;
    try {
      page = webClient.getPage("https://www.nordnet.fi/yleisnakyma/tili/3");
      webClient.waitForBackgroundJavaScript(5300);
      HtmlTableRow row = page.getFirstByXPath("//tr[//*[contains(text(), '" + omaOsake.getNimi() + "')]]");
      HtmlAnchor link =  row.getFirstByXPath("//a[span[text()='Myy']]");
      String hrefAttribute = link.getHrefAttribute();
      this.doSell(omaOsake, hrefAttribute);

    } catch (IOException e) {
      throw new FunctionException(e);
    }
  }
  private void doSell(OmaOsake osake, String href) {
    try {
      HtmlPage page = webClient.getPage( new URL("https://www.nordnet.fi/markkinakatsaus/osakekurssit/16102550-neste-corporation/toimeksianto/myy?accid=3"));

      webClient.waitForBackgroundJavaScript(6000);

      List<HtmlForm> forms = page.getByXPath("//div[header[//*[contains(text(), 'Myy')]]]/form");
      forms.get(0).remove();
      forms.get(1).remove();
      HtmlForm form = forms.get(2);

      HtmlTextInput textInput =  form.getFirstByXPath("//input[@id='volume']");
      textInput.type("22");
      webClient.waitForBackgroundJavaScript(10_000);
      HtmlButton button = form.getFirstByXPath("//button[@type='submit']");
      button.removeAttribute("disabled");
      Page click = button.click();
      webClient.waitForBackgroundJavaScript(10_000);
      HtmlPage clickHtmlPage = (HtmlPage) click;
    } catch (IOException e) {
      throw new FunctionException(e);
    }


  }
}


