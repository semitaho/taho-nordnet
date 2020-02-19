package fi.tahoo.nordnet.model;

import java.math.BigDecimal;

public class OmaOsake {

  private String nimi;

  private Integer maara;

  private BigDecimal ostohinta;

  public OmaOsake() {

  }
  public OmaOsake(String nimi, Integer maara, BigDecimal ostohinta) {
    this.nimi = nimi;
    this.maara = maara;
    this.ostohinta = ostohinta;
  }

  public String getNimi() {
    return nimi;
  }

  public void setNimi(String nimi) {
    this.nimi = nimi;
  }

  public Integer getMaara() {
    return maara;
  }

  public void setMaara(Integer maara) {
    this.maara = maara;
  }

  public BigDecimal getOstohinta() {
    return ostohinta;
  }

  public void setOstohinta(BigDecimal ostohinta) {
    this.ostohinta = ostohinta;
  }
}


