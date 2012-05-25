/**
 * Copyright (C) 2000 - 2011 Silverpeas
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
import java.util.Map;

import com.silverpeas.util.EncodeHelper;
import com.stratelia.silverpeas.peasCore.URLManager;
import com.stratelia.webactiv.util.FileServerUtils;
import com.stratelia.webactiv.util.GeneralPropertiesManager;
import com.stratelia.webactiv.util.ResourceLocator;

public class Util {

  private static final ResourceLocator formIcons = new ResourceLocator(
      "com.silverpeas.form.settings.formIcons", "");
  private static final ResourceLocator settings = new ResourceLocator(
      "com.silverpeas.form.settings.form", "");
  private static final String path = URLManager.getApplicationURL();
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
    setLanguage(language);
    if (msg.startsWith("GML.")) {
      return generalMessage.getString(msg);
    }
    return message.getString(msg);
  }

  public static String getJavascriptIncludes(String language) {
    StringBuilder includes = new StringBuilder();
    addSilverpeasScript(includes, "/wysiwyg/jsp/ckeditor/ckeditor.js");
    addSilverpeasScript(includes, "/util/javaScript/dateUtils.js");
    addSilverpeasScript(includes, "/util/javaScript/checkForm.js");
    addSilverpeasScript(includes, "/util/javaScript/animation.js");

    // includes external scripts once because
    // including several times the same script (once per field) can provide
    // dysfunction on this fields
    String webContext = path;
    addExternalStyleSheet(includes, webContext, "/util/yui/fonts/fonts-min.css");
    addExternalStyleSheet(includes, webContext,
        "/util/yui/autocomplete/assets/skins/sam/autocomplete.css");
    addExternalScript(includes, webContext, "/util/yui/yahoo-dom-event/yahoo-dom-event.js");
    addExternalScript(includes, webContext, "/util/yui/animation/animation-min.js");
    addExternalScript(includes, webContext, "/util/yui/datasource/datasource-min.js");
    addExternalScript(includes, webContext, "/util/yui/autocomplete/autocomplete-min.js");
    addExternalScript(includes, webContext, "/util/javaScript/jquery/jquery.ui.datepicker-"+language+".js");
    addExternalScript(includes, webContext, "/util/javaScript/silverpeas-defaultDatePicker.js");
    return includes.toString();

  }

  private static void addSilverpeasScript(StringBuilder includes, String script) {
    includes.append("<script type=\"text/javascript\" src=\"").append(path);
    includes.append(script).append("\"></script>\n");
  }

  private static void addExternalStyleSheet(StringBuilder includes, String webContext,
      String styleSheet) {
    includes.append("<link rel=\"stylesheet\" type=\"text/css\" href=\"").append(webContext);
    includes.append(styleSheet).append("\" />\n");
  }

  private static void addExternalScript(StringBuilder includes, String webContext, String script) {
    includes.append("<script type=\"text/javascript\" src=\"").append(webContext);
    includes.append(script).append("\"></script>\n");
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

  public static void includeFileNameLengthChecker(FieldTemplate template, PagesContext pageContext,
      PrintWriter out) {
    out.println(" //check length of filename ");
    out.println("   var lastIndexOfPathSeparator = field.value.lastIndexOf('/');");
    out.println("   if (lastIndexOfPathSeparator == -1) {");
    out.println("   //check Windows path");
    out.println("     lastIndexOfPathSeparator = field.value.lastIndexOf('\\\\');");
    out.println("   } ");
    out.println("   if (lastIndexOfPathSeparator == -1) { ");
    out.println("     lastIndexOfPathSeparator = 0;");
    out.println("   } ");
    out.println("   var filename = field.value.substring(lastIndexOfPathSeparator); ");
    out.println("   if (filename.length > 100) { ");
    out.println("       errorMsg+=\"  - '" +
        EncodeHelper.javaStringToJsString(template.getLabel(pageContext.getLanguage())) + "' " +
        Util.getString("form.field.file.toolong", language) + "\\n \";");
    out.println("       errorNb++;");
    out.println("   } ");
  }

  private synchronized static void setLanguage(String lg) {
    if ((language == null) || (!language.trim().toLowerCase().equals(lg.trim().toLowerCase()))) {
      language = lg;
      generalMessage = GeneralPropertiesManager.getGeneralMultilang(language);
      message = new ResourceLocator("com.silverpeas.form.multilang.formBundle", language);
    }
  }

  /**
   * @return the mandatory icon to print after a mandatory field
   */
  public static String getMandatorySnippet() {
    return "&nbsp;<img src=\"" + getIcon("mandatoryField")
        + "\" width=\"5\" height=\"5\" alt=\""
        + Util.getString("GML.requiredField", language) + "\"/>";
  }

  public static boolean getBooleanValue(Map<String, String> parameters, String parameter) {
    String paramValue = parameters.containsKey(parameter) ? parameters.get(parameter) : "false";
    return Boolean.parseBoolean(paramValue);
  }
}
