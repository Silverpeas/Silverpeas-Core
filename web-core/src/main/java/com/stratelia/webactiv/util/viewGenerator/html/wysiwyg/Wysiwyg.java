package com.stratelia.webactiv.util.viewGenerator.html.wysiwyg;

import com.silverpeas.util.StringUtil;
import com.stratelia.silverpeas.peasCore.URLManager;
import com.stratelia.webactiv.util.ResourceLocator;

public class Wysiwyg {

  private String replace;
  private String height = "500";
  private String width = "100%";
  private String language = "en";
  private String toolbar = "Default";
  private boolean toolbarStartExpanded = true;
  private String imageBrowserURL;
  private String serverURL;

  ResourceLocator wysiwygSettings = new ResourceLocator("org.silverpeas.wysiwyg.settings.wysiwygSettings", "");

  public Wysiwyg() {

  }

  public String print() {
    String configFile = wysiwygSettings.getString("configFile", URLManager.getApplicationURL() +"/wysiwyg/jsp/ckeditor/silverconfig.js");
    StringBuilder builder = new StringBuilder(100);

    builder.append("CKEDITOR.replace('").append(getReplace()).append("', {\n");
    builder.append("width : '").append(getWidth()).append("',\n");
    builder.append("height : ").append(getHeight()).append(",\n");
    builder.append("language : '").append(getLanguage()).append("',\n");
    String basehref = wysiwygSettings.getString("baseHref", getServerURL());
    builder.append("baseHref : '").append(basehref).append("',\n");
    if (StringUtil.isDefined(getImageBrowserURL())) {
      builder.append("filebrowserImageBrowseUrl : '").append(getImageBrowserURL()).append("',\n");
    }
    builder.append("toolbarStartupExpanded : ").append(isToolbarStartExpanded()).append(",\n");
    builder.append("customConfig : '").append(configFile).append("',\n");
    builder.append("toolbar : '").append(getToolbar()).append("'\n");
    builder.append("});");

    return builder.toString();
  }

  public String getReplace() {
    return replace;
  }

  public void setReplace(String replace) {
    this.replace = replace;
  }

  public String getHeight() {
    return height;
  }

  public void setHeight(String height) {
    this.height = height;
  }

  public String getWidth() {
    return width;
  }

  public void setWidth(String width) {
    this.width = width;
  }

  public String getLanguage() {
    return language;
  }

  public void setLanguage(String language) {
    this.language = language;
  }

  public String getToolbar() {
    return toolbar;
  }

  public void setToolbar(String toolbar) {
    this.toolbar = toolbar;
  }

  public boolean isToolbarStartExpanded() {
    return toolbarStartExpanded;
  }

  public void setToolbarStartExpanded(boolean toolbarStartExpanded) {
    this.toolbarStartExpanded = toolbarStartExpanded;
  }

  public String getImageBrowserURL() {
    return imageBrowserURL;
  }

  public void setImageBrowserURL(String imageBrowserURL) {
    this.imageBrowserURL = imageBrowserURL;
  }

  public void setServerURL(String serverURL) {
    this.serverURL = serverURL;
  }

  public String getServerURL() {
    return serverURL;
  }

}
