package com.silverpeas.look;

public class Shortcut {

  private String iconURL;
  private String target;
  private String url;
  private String altText;

  public Shortcut(String iconURL, String target, String url, String altText) {
    setIconURL(iconURL);
    setTarget(target);
    setUrl(url);
    setAltText(altText);
  }

  public String getIconURL() {
    return iconURL;
  }

  public void setIconURL(String iconURL) {
    this.iconURL = iconURL;
  }

  public String getTarget() {
    return target;
  }

  public void setTarget(String target) {
    this.target = target;
  }

  public String getUrl() {
    return url;
  }

  public void setUrl(String url) {
    this.url = url;
  }

  public String getAltText() {
    return altText;
  }

  public void setAltText(String altText) {
    this.altText = altText;
  }

}
