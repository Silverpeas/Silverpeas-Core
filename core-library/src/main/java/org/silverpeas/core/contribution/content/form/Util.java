/*
 * Copyright (C) 2000 - 2014 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception. You should have recieved a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.silverpeas.core.contribution.content.form;

import org.silverpeas.core.util.URLUtil;
import org.silverpeas.core.util.EncodeHelper;
import org.silverpeas.core.util.file.FileServerUtils;
import org.silverpeas.core.util.LocalizationBundle;
import org.silverpeas.core.util.ResourceLocator;
import org.silverpeas.core.util.SettingBundle;
import org.silverpeas.core.util.StringUtil;

import java.io.PrintWriter;
import java.util.Map;

import static org.silverpeas.core.util.URLUtil.appendVersion;

public class Util {

  private static final SettingBundle formIcons = ResourceLocator.getSettingBundle(
      "org.silverpeas.form.settings.formIcons");
  private static final SettingBundle settings = ResourceLocator.getSettingBundle(
      "org.silverpeas.form.settings.form");
  private static final String path = URLUtil.getApplicationURL();
  private static LocalizationBundle message;
  private static String language;

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
    return message.getString(msg);
  }

  public static String getJavascriptIncludes(String language) {
    StringBuilder includes = new StringBuilder();
    addSilverpeasScript(includes, "/wysiwyg/jsp/ckeditor/ckeditor.js");
    addSilverpeasScript(includes, "/util/javaScript/dateUtils.js");
    addSilverpeasScript(includes, "/util/javaScript/checkForm.js");

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
    addExternalScript(includes, webContext, "/util/javaScript/jquery/jquery.ui.datepicker-" +
        language + ".js");
    addExternalScript(includes, webContext, "/util/javaScript/silverpeas-defaultDatePicker.js");
    return includes.toString();

  }

  private static void addSilverpeasScript(StringBuilder includes, String script) {
    includes.append("<script type=\"text/javascript\" src=\"").append(appendVersion(path + script))
        .append("\"></script>\n");
  }

  private static void addExternalStyleSheet(StringBuilder includes, String webContext,
      String styleSheet) {
    includes.append("<link rel=\"stylesheet\" type=\"text/css\" href=\"")
        .append(appendVersion(webContext + styleSheet)).append("\" />\n");
  }

  private static void addExternalScript(StringBuilder includes, String webContext, String script) {
    includes.append("<script type=\"text/javascript\" src=\"")
        .append(appendVersion(webContext + script)).append("\"></script>\n");
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
      message = ResourceLocator.getLocalizationBundle("org.silverpeas.form.multilang.formBundle",
          language);
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

  public static void printOneMoreInputSnippet(String fieldName, PagesContext pageContext,
      PrintWriter out) {
    out.println("<a href=\"#\" id=\"moreField-" + fieldName + "\" onclick=\"showOneMoreField('" +
        fieldName + "');return false;\">");
    out.println("<img src=\"" + Util.getIcon("add") + "\" width=\"14px\"> ");
    out.println(Util.getString("field.multivaluable.add", pageContext.getLanguage()));
    out.println("</a>");
  }

  public static String getFieldOccurrenceName(String fieldName, int occurrence) {
    if (occurrence == 0) {
      return fieldName;
    }
    return fieldName + AbstractForm.REPEATED_FIELD_SEPARATOR + occurrence;
  }

  public static boolean isEmptyFieldsDisplayed() {
    return StringUtil.getBooleanValue(getSetting("form.view.emptyFields.displayed"));
  }
}