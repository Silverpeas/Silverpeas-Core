/*
 * Copyright (C) 2000 - 2017 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.web.util.viewgenerator.html.wysiwyg;

import org.silverpeas.core.util.URLUtil;
import org.silverpeas.core.util.ResourceLocator;
import org.silverpeas.core.util.SettingBundle;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.web.util.viewgenerator.html.GraphicElementFactory;

public class Wysiwyg {

  private String replace;
  private String height = "500";
  private String width = "100%";
  private String language = "en";
  private String toolbar = "Default";
  private boolean toolbarStartExpanded = true;
  private String serverURL;
  private String css;
  private boolean displayFileBrowser = true;

  SettingBundle wysiwygSettings =
      ResourceLocator.getSettingBundle("org.silverpeas.wysiwyg.settings.wysiwygSettings");

  private static final String INFO_LETTER_APPLICATION = "infoLetter";

  public Wysiwyg() {

  }

  public String print() {
    String baseDir = wysiwygSettings.getString("baseDir", "ckeditor");
    String configFile = wysiwygSettings.getString("configFile",
        URLUtil.getApplicationURL() + "/wysiwyg/jsp/" + baseDir + "/silverconfig.js");

    //special case of infoLetter application : special configFile
    if(INFO_LETTER_APPLICATION.equals(getToolbar())) {
      configFile = wysiwygSettings.getString("infoLetterConfigFile",
          URLUtil.getApplicationURL() + "/wysiwyg/jsp/" + baseDir + "/infoLetterConfig.js");
    }

    StringBuilder builder = new StringBuilder(100);

    builder.append("CKEDITOR.replace('").append(getReplace()).append("', {\n");
    builder.append("width : '").append(getWidth()).append("',\n");
    builder.append("height : ").append(getHeight()).append(",\n");
    builder.append("language : '").append(getLanguage()).append("',\n");
    String basehref = wysiwygSettings.getString("baseHref", getServerURL());
    builder.append("baseHref : '").append(basehref).append("',\n");
    if (! getDisplayFileBrowser()) {
      builder.append("filebrowserImageBrowseUrl : '',\n");
      builder.append("filebrowserFlashBrowseUrl : '',\n");
      builder.append("filebrowserBrowseUrl : '',\n");
    }
    builder.append("toolbarStartupExpanded : ").append(isToolbarStartExpanded()).append(",\n");
    builder.append("customConfig : '").append(configFile).append("',\n");
    builder.append("toolbar : '").append(getToolbar()).append("',\n");

    String skin = wysiwygSettings.getString("skin");
    if (StringUtil.isDefined(skin)) {
      builder.append("skin : '").append(skin).append("'");
    }

    //special case of infoLetter application : no CSS
    if(!INFO_LETTER_APPLICATION.equals(getToolbar())) {
      builder.append(",\n");
      String standardCSS = URLUtil.getApplicationURL() + GraphicElementFactory.STANDARD_CSS;
      if (StringUtil.isDefined(css)) {
        builder.append("contentsCss : ['").append(standardCSS).append("', '").append(css)
            .append("']\n");
      } else {
        builder.append("contentsCss : '").append(standardCSS).append("'\n");
      }
    }

    builder.append("});\n");

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

  public void setServerURL(String serverURL) {
    this.serverURL = serverURL;
  }

  public String getServerURL() {
    return serverURL;
  }

  public void setCustomCSS(String css) {
    this.css = css;
  }

  public boolean getDisplayFileBrowser() {
    return displayFileBrowser;
  }

  public void setDisplayFileBrowser(boolean displayFileBrowser) {
    this.displayFileBrowser = displayFileBrowser;
  }
}
