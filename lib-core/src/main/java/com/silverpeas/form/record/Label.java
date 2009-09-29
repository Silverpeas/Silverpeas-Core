package com.silverpeas.form.record;

public class Label {
  private String label = "";
  private String language = "";

  public Label() {
  }

  public Label(String label, String language) {
    this.label = label;
    this.language = language;
  }

  public String getLabel() {
    return this.label;
  }

  public String getLanguage() {
    return this.language;
  }

  public void setLabel(String label) {
    this.label = label;
  }

  public void setLanguage(String language) {
    this.language = language;
  }

}
