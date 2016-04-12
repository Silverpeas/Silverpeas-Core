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
import org.silverpeas.core.silvertrace.SilverTrace;
import org.apache.commons.fileupload.FileItem;
import org.silverpeas.core.util.StringUtil;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

/**
 * A CheckBoxDisplayer is an object which can display a checkbox in HTML the content of a checkbox
 * to a end user and can retrieve via HTTP any updated value.
 * <p/>
 * @see Field
 * @see FieldTemplate
 * @see Form
 * @see FieldDisplayer
 */
public class CheckBoxDisplayer extends AbstractFieldDisplayer<TextField> {

  /**
   * Constructeur
   */
  public CheckBoxDisplayer() {
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
   * <ul>
   * <li>the fieldName is unknown by the template.
   * <li>the field type is not a managed type.
   * </ul>
   * @param out
   * @param template
   * @param pagesContext
   * @throws IOException
   */
  @Override
  public void displayScripts(PrintWriter out, FieldTemplate template, PagesContext pagesContext)
      throws IOException {
    String language = pagesContext.getLanguage();
    String fieldName = template.getFieldName();
    if (template.isMandatory() && pagesContext.useMandatory()) {
      out.println(" var checked = false;\n");
      out.println(" for (var i = 0; i < " + getNbHtmlObjectsDisplayed(template, pagesContext) +
          "; i++) {\n");
      out.println("   if (document.getElementsByName('" + fieldName + "')[i].checked) {\n");
      out.println("     checked = true;\n");
      out.println("   }\n");
      out.println(" }\n");
      out.println(" if(checked == false) {\n");
      out.println("   errorMsg+=\"  - '" + template.getLabel(language) + "' " +
          Util.getString("GML.MustBeFilled", language) + "\\n \";");
      out.println("   errorNb++;");
      out.println(" }");
    }
    Util.getJavascriptChecker(template.getFieldName(), pagesContext, out);
  }

  /**
   * Prints the HTML value of the field. The displayed value must be updatable by the end user. The
   * value format may be adapted to a local language. The fieldName must be used to name the html
   * form input. Never throws an Exception but log a silvertrace and writes an empty string when :
   * <ul>
   * <li>the field type is not a managed type.</li>
   * </ul>
   * @param out
   * @param field
   * @param template
   * @param PagesContext
   * @throws FormException
   */
  @Override
  public void display(PrintWriter out, TextField field, FieldTemplate template,
      PagesContext PagesContext) throws FormException {
    String selectedValues = "";
    List<String> valuesFromDB = new ArrayList<>();
    String keys = "";
    String values = "";
    StringBuilder html = new StringBuilder();
    int cols = 1;
    String language = PagesContext.getLanguage();

    String fieldName = template.getFieldName();
    Map<String, String> parameters = template.getParameters(language);
    if (!field.isNull()) {
      selectedValues = field.getValue(language);
    }
    StringTokenizer st = new StringTokenizer(selectedValues, "##");
    while (st.hasMoreTokens()) {
      valuesFromDB.add(st.nextToken());
    }
    if (parameters.containsKey("keys")) {
      keys = parameters.get("keys");
    }
    if (parameters.containsKey("values")) {
      values = parameters.get("values");
    }
    String cssClass = null;
    if (parameters.containsKey("class")) {
      cssClass = parameters.get("class");
      if (StringUtil.isDefined(cssClass)) {
        cssClass = "class=\"" + cssClass + "\"";
      }
    }

    try {
      if (parameters.containsKey("cols")) {
        cols = Integer.valueOf(parameters.get("cols"));
      }
    } catch (NumberFormatException nfe) {
      SilverTrace.error("form", "CheckBoxDisplayer.display", "form.EX_ERR_ILLEGAL_PARAMETER_COL",
          parameters.get("cols"));
      cols = 1;
    }

    // if either keys or values is not filled
    // take the same for keys and values
    if ("".equals(keys) && !"".equals(values)) {
      keys = values;
    }
    if ("".equals(values) && !"".equals(keys)) {
      values = keys;
    }

    StringTokenizer stKeys = new StringTokenizer(keys, "##");
    StringTokenizer stValues = new StringTokenizer(values, "##");

    int nbTokens = getNbHtmlObjectsDisplayed(template, PagesContext);

    if (stKeys.countTokens() != stValues.countTokens()) {
      SilverTrace.error("form", "CheckBoxDisplayer.display", "form.EX_ERR_ILLEGAL_PARAMETERS",
          "Nb keys=" + stKeys.countTokens() + " & Nb values=" + stValues.countTokens());
    } else {
      html.append("<table border=\"0\">");
      int col = 0;
      for (int i = 0; i < nbTokens; i++) {
        if (col == 0) {
          html.append("<tr>");
        }

        col++;
        html.append("<td>");
        String optKey = stKeys.nextToken();
        String optValue = stValues.nextToken();
        html.append("<input type=\"checkbox\" id=\"").append(fieldName).append("_").append(i);
        html.append("\" name=\"").append(fieldName).append("\" value=\"").append(optKey)
            .append("\" ");
        if (StringUtil.isDefined(cssClass)) {
          html.append(cssClass);
        }
        if (template.isDisabled() || template.isReadOnly()) {
          html.append(" disabled=\"disabled\" ");
        }
        if (valuesFromDB.contains(optKey)) {
          html.append(" checked=\"checked\" ");
        }
        html.append("/>&nbsp;").append(optValue);

        // last checkBox
        if (i == nbTokens - 1) {
          if (template.isMandatory() && !template.isDisabled() && !template.isReadOnly() &&
              !template.isHidden() && PagesContext.useMandatory()) {
            html.append(Util.getMandatorySnippet());
          }
        }
        html.append("</td>");
        html.append("\n");
        if (col == cols) {
          html.append("</tr>");
          col = 0;
        }
      }
      if (col != 0) {
        html.append("</tr>");
      }
      html.append("</table>");
    }
    out.println(html);
  }

  @Override
  public List<String> update(List<FileItem> items, TextField field, FieldTemplate template,
      PagesContext pageContext) throws FormException {
    String value = "";
    Iterator<FileItem> iter = items.iterator();
    String parameterName = template.getFieldName();
    while (iter.hasNext()) {
      FileItem item = iter.next();
      if (parameterName.equals(item.getFieldName())) {
        if (StringUtil.isDefined(value)) {
          value += "##";
        }
        value += item.getString();
      }
    }
    if (pageContext.getUpdatePolicy() == PagesContext.ON_UPDATE_IGNORE_EMPTY_VALUES &&
        !StringUtil.isDefined(value)) {
      return new ArrayList<>();
    }
    return update(value, field, template, pageContext);
  }

  @Override
  public List<String> update(String valuesToInsert, TextField field, FieldTemplate template,
      PagesContext PagesContext) throws FormException {
    if (!TextField.TYPE.equals(field.getTypeName())) {
      throw new FormException("CheckBoxDisplayer.update", "form.EX_NOT_CORRECT_TYPE",
          TextField.TYPE);
    }

    if (field.acceptValue(valuesToInsert, PagesContext.getLanguage())) {
      field.setValue(valuesToInsert, PagesContext.getLanguage());
    } else {
      throw new FormException("CheckBoxDisplayer.update", "form.EX_NOT_CORRECT_VALUE",
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
    String keys = "";
    String values = "";
    Map<String, String> parameters = template.getParameters(pagesContext.getLanguage());
    if (parameters.containsKey("keys")) {
      keys = parameters.get("keys");
    }
    if (parameters.containsKey("values")) {
      values = parameters.get("values");
    }

    // if either keys or values is not filled
    // take the same for keys and values
    if (keys.equals("") && !values.equals("")) {
      keys = values;
    }

    // Calculate numbers of html elements
    StringTokenizer stKeys = new StringTokenizer(keys, "##");
    return stKeys.countTokens();

  }
}
