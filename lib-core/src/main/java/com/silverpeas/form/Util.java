/**
 * Copyright (C) 2000 - 2009 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have recieved a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://repository.silverpeas.com/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.silverpeas.form;

import java.io.PrintWriter;

import com.stratelia.silverpeas.peasCore.URLManager;
import com.stratelia.webactiv.util.FileServerUtils;
import com.stratelia.webactiv.util.GeneralPropertiesManager;
import com.stratelia.webactiv.util.ResourceLocator;

public class Util {

  private static final ResourceLocator formIcons = new ResourceLocator(
      "com.silverpeas.form.settings.formIcons", "");
  private static final ResourceLocator settings = new ResourceLocator(
      "com.silverpeas.form.settings.form", "");
  private static final String path = GeneralPropertiesManager
      .getGeneralResourceLocator().getString("ApplicationURL");
  private static ResourceLocator generalMessage;
  private static ResourceLocator message;
  private static String language = null;

  public static String getPath() {
    return path;
  }

  public static String getSetting(String setting) {
    return settings.getString(setting);
  }

  public static String getIcon(String icon) {
    return path + formIcons.getString(icon);
  }

  public static String getString(String msg, String language) {
    String s = "";
    setLanguage(language);
    if (msg.startsWith("GML."))
      s = generalMessage.getString(msg);
    else
      s = message.getString(msg);
    return s;
  }

  public static String getJavascriptIncludes() {
    String includes = "";
    includes += "<script type=\"text/javascript\" src=\"" + path
        + "/util/javaScript/dateUtils.js" + "\"></script>\n";
    includes += "<script type=\"text/javascript\" src=\"" + path
        + "/util/javaScript/checkForm.js" + "\"></script>\n";
    includes += "<script type=\"text/javascript\" src=\"" + path
        + "/util/javaScript/animation.js" + "\"></script>\n";
    includes += "<script language=\"JavaScript\">";
    includes += "function calendar(idField) {";
    includes += "	SP_openWindow('" + path
        + URLManager.getURL(URLManager.CMP_AGENDA)
        + "calendar.jsp?idElem='+idField,'Calendrier',180,200,'');";
    includes += "}";
    includes += "</script>";
    return includes;

  }

  public static void getJavascriptChecker(String fieldName,
      PagesContext pageContext, PrintWriter out) {
    String jsFunction = "check"
        + FileServerUtils.replaceAccentChars(fieldName.replace(' ', '_'));
    out.println(" try { ");
    out.println("if (typeof(" + jsFunction + ") == 'function')");
    out.println(" 	" + jsFunction + "('" + pageContext.getLanguage() + "');");
    out.println(" } catch (e) { ");
    out.println(" 	//catch all exceptions");
    out.println(" } ");
  }

  private static void setLanguage(String lg) {
    if ((language == null)
        || (!language.trim().toLowerCase().equals(lg.trim().toLowerCase()))) {
      language = lg;
      generalMessage = GeneralPropertiesManager.getGeneralMultilang(language);
      message = new ResourceLocator("com.silverpeas.form.multilang.formBundle",
          language);
    }
  }

  /**
   * @return the mandatory icon to print after a mandatory field
   */
  public static String getMandatorySnippet() {
    return "&nbsp;<img src=\"" + getIcon("mandatoryField")
        + "\" width=\"5\" height=\"5\" border=\"0\">";
  }
}