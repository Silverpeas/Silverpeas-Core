package com.silverpeas.util.i18n;

public class I18NLanguage {

  private int translationId = -1;
  private String code = null;
  private String label = null;

  public I18NLanguage(String code) {
    this.code = code;
  }

  public I18NLanguage(String code, String label) {
    this.code = code;
    this.label = label;
  }

  public String getCode() {
    return code;
  }

  public String getLabel() {
    return label;
  }

  public boolean equals(Object o) {
    I18NLanguage other = (I18NLanguage) o;
    return other.getCode().equals(code);
  }

  public int getTranslationId() {
    return translationId;
  }

  public void setTranslationId(int translationId) {
    this.translationId = translationId;
  }

}
