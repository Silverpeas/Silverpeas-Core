package com.silverpeas.form.record;

import java.io.Serializable;

public class ParameterValue implements Serializable {
  private String lang = "fr";
  private String value = "";

  public ParameterValue() {
  }

  public ParameterValue(String lang, String value) {
    this.lang = lang;
    this.value = value;
  }

  public String getLang() {
    return this.lang;
  }

  public String getValue() {
    return this.value;
  }

  public void setLang(String lang) {
    if (lang == null || lang.equals(""))
      lang = "fr";
    this.lang = lang;
  }

  public void setValue(String value) {
    this.value = value;
  }
}
