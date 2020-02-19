package fi.tahoo.nordnet.model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class YleisnakymaResponse {


  private BigDecimal saldo;
  private List<OmaOsake> osakkeet;
  private List<String> avoimetToimeksiannot;

  public YleisnakymaResponse(BigDecimal saldo, List<OmaOsake> osakkeet, List<String> avoimetToimeksiannot) {
    this.saldo = saldo;
    this.osakkeet = osakkeet;
    this.avoimetToimeksiannot = avoimetToimeksiannot;
  }

  public BigDecimal getSaldo() {
    return saldo;
  }

  public void setSaldo(BigDecimal saldo) {
    this.saldo = saldo;
  }

  public List<OmaOsake> getOsakkeet() {
    return osakkeet;
  }

  public void setOsakkeet(List<OmaOsake> osakkeet) {
    this.osakkeet = osakkeet;
  }

  public List<String> getAvoimetToimeksiannot() {
    return avoimetToimeksiannot;
  }

  public void setAvoimetToimeksiannot(List<String> avoimetToimeksiannot) {
    this.avoimetToimeksiannot = avoimetToimeksiannot;
  }
}
