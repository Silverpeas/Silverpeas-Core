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
package org.silverpeas.core.contribution.content.form.displayers;

import org.silverpeas.core.contribution.content.form.Field;
import org.silverpeas.core.contribution.content.form.FieldDisplayer;
import org.silverpeas.core.contribution.content.form.FieldTemplate;
import org.silverpeas.core.contribution.content.form.Form;
import org.silverpeas.core.contribution.content.form.FormException;
import org.silverpeas.core.contribution.content.form.PagesContext;
import org.silverpeas.core.contribution.content.form.Util;
import org.silverpeas.core.contribution.content.form.field.TextField;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.silvertrace.SilverTrace;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

/**
 * A ListBoxFieldDisplayer is an object which can display a listbox in HTML the content of a listbox
 * to a end user and can retrieve via HTTP any updated value.
 *
 * @see Field
 * @see FieldTemplate
 * @see Form
 * @see FieldDisplayer
 */
public class ListBoxFieldDisplayer extends AbstractFieldDisplayer<TextField> {

  /**
   * Constructeur
   */
  public ListBoxFieldDisplayer() {
  }

  /**
   * Returns the name of the managed types.
   */
  public String[] getManagedTypes() {
    return new String[]{TextField.TYPE};
  }

  /**
   * Prints the javascripts which will be used to control the new value given to the named field.
   * The error messages may be adapted to a local language. The FieldTemplate gives the field type
   * and constraints. The FieldTemplate gives the local labeld too. Never throws an Exception but
   * log a silvertrace and writes an empty string when :
   * <UL>
   * <LI>the fieldName is unknown by the template.
   * <LI>the field type is not a managed type.
   * </UL>
   */
  @Override
  public void displayScripts(PrintWriter out, FieldTemplate template, PagesContext PagesContext)
      throws java.io.IOException {

    String language = PagesContext.getLanguage();

    if (!TextField.TYPE.equals(template.getTypeName())) {

    }

    if (template.isMandatory() && PagesContext.useMandatory()) {
      out.println("	if (isWhitespace(stripInitialWhitespace(field.value))) {");
      out.println("		errorMsg+=\"  - '" + template.getLabel(language) + "' " + Util.getString(
          "GML.MustBeFilled", language) + "\\n\";");
      out.println("		errorNb++;");
      out.println("	}");
    }

    Util.getJavascriptChecker(template.getFieldName(), PagesContext, out);
  }

  /**
   * Prints the HTML value of the field. The displayed value must be updatable by the end user. The
   * value format may be adapted to a local language. The fieldName must be used to name the html
   * form input. Never throws an Exception but log a silvertrace and writes an empty string when :
   * <UL>
   * <LI>the field type is not a managed type.
   * </UL>
   */
  @Override
  public void display(PrintWriter out, TextField field, FieldTemplate template,
      PagesContext PagesContext) throws FormException {
    String value = "";
    String keys = "";
    String values;
    String html = "";
    String language = PagesContext.getLanguage();
    String cssClass = null;

    String fieldName = template.getFieldName();
    Map<String, String> parameters = template.getParameters(language);

    if (!TextField.TYPE.equals(field.getTypeName())) {

    }

    if (!field.isNull()) {
      value = field.getValue(language);
    }

    if (parameters.containsKey("class")) {
      cssClass = parameters.get("class");
      if (StringUtil.isDefined(cssClass)) {
        cssClass = "class=\"" + cssClass + "\"";
      }
    }
    if (StringUtil.isDefined(cssClass)) {
      html += "<select " + cssClass + " id=\"" + fieldName + "\" name=\"" + fieldName + "\"";
    } else {
      html += "<select id=\"" + fieldName + "\" name=\"" + fieldName + "\"";
    }

    if (template.isDisabled() || template.isReadOnly()) {
      html += " disabled";
    }

    html += " >\n";
    html += "<option value=\"\"></option>\n";
    if (parameters.containsKey("keys")) {
      keys = parameters.get("keys");
    }

    if (parameters.containsKey("values")) {
      values = parameters.get("values");
    } else {
      values = keys;
    }

    StringTokenizer stKeys = new StringTokenizer(keys, "##");
    StringTokenizer stValues = new StringTokenizer(values, "##");
    String optKey;
    String optValue;
    int nbTokens = stKeys.countTokens();

    if (stKeys.countTokens() != stValues.countTokens()) {
      SilverTrace.error("form", "ListBoxFieldDisplayer.display", "form.EX_ERR_ILLEGAL_PARAMETERS",
          "Nb keys=" + stKeys.countTokens() + " & Nb values=" + stValues.countTokens());
    } else {
      for (int i = 0; i < nbTokens; i++) {
        optKey = stKeys.nextToken();
        optValue = stValues.nextToken();

        html += "<option ";
        if (optKey.equals(value)) {
          html += "selected=\"selected\" ";
        }

        html += "value=\"" + optKey + "\">" + optValue + "</option>\n";
      }
    }

    html += "</select>\n";

    if (template.isMandatory() && !template.isDisabled() && !template.isReadOnly() && !template.
        isHidden() && PagesContext.useMandatory()) {
      html += Util.getMandatorySnippet();
    }

    out.println(html);
  }

  @Override
  public List<String> update(String newValue, TextField field, FieldTemplate template,
      PagesContext PagesContext) throws FormException {

    if (!TextField.TYPE.equals(field.getTypeName())) {
      throw new FormException("TextAreaFieldDisplayer.update", "form.EX_NOT_CORRECT_TYPE",
          TextField.TYPE);
    }

    if (field.acceptValue(newValue, PagesContext.getLanguage())) {
      field.setValue(newValue, PagesContext.getLanguage());
    } else {
      throw new FormException("TextAreaFieldDisplayer.update", "form.EX_NOT_CORRECT_VALUE",
          TextField.TYPE);
    }
    return new ArrayList<>();
  }

  @Override
  public boolean isDisplayedMandatory() {
    return true;
  }

  @Override
  public int getNbHtmlObjectsDisplayed(FieldTemplate template, PagesContext pagesContext) {
    return 1;
  }
}
