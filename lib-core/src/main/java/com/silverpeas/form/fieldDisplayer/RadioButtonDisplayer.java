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
package com.silverpeas.form.fieldDisplayer;

import java.io.PrintWriter;
import java.util.Map;
import java.util.StringTokenizer;

import com.silverpeas.form.Field;
import com.silverpeas.form.FieldDisplayer;
import com.silverpeas.form.FieldTemplate;
import com.silverpeas.form.Form;
import com.silverpeas.form.FormException;
import com.silverpeas.form.PagesContext;
import com.silverpeas.form.Util;
import com.silverpeas.form.fieldType.TextField;
import com.silverpeas.util.StringUtil;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import java.util.ArrayList;
import java.util.List;

/**
 * A RadioButtonDisplayer is an object which can display a radio button in HTML the content of a
 * radio button to a end user and can retrieve via HTTP any updated value.
 * @see Field
 * @see FieldTemplate
 * @see Form
 * @see FieldDisplayer
 */
public class RadioButtonDisplayer extends AbstractFieldDisplayer {

  /**
   * Constructeur
   */
  public RadioButtonDisplayer() {
  }

  /**
   * Returns the name of the managed types.
   */
  public String[] getManagedTypes() {
    String[] s = new String[0];
    s[0] = TextField.TYPE;
    return s;
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
  public void displayScripts(PrintWriter out,
      FieldTemplate template,
      PagesContext PagesContext) throws java.io.IOException {

    String language = PagesContext.getLanguage();

    if (!template.getTypeName().equals(TextField.TYPE)) {
      SilverTrace.info("form", "RadioButtonDisplayer.displayScripts", "form.INFO_NOT_CORRECT_TYPE",
          TextField.TYPE);
    }

    if (template.isMandatory() && PagesContext.useMandatory()) {
      int currentIndex = new Integer(PagesContext.getCurrentFieldIndex()).intValue();
      int fin = currentIndex + getNbHtmlObjectsDisplayed(template, PagesContext);
      out.println("	var checked = false;\n");
      out.println("	for (var i = " + currentIndex + "; i < " + fin + "; i++) {\n");
      out.println("		if (document.forms[" + PagesContext.getFormIndex() +
          "].elements[i].checked) {\n");
      out.println("			checked = true;\n");
      out.println("		}\n");
      out.println("	}\n");
      out.println("	if(checked == false) {\n");
      out.println("		errorMsg+=\"  - '" + template.getLabel(language) + "' " +
          Util.getString("GML.MustBeFilled",
          language) + "\\n \";");
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
  public void display(PrintWriter out,
      Field field,
      FieldTemplate template,
      PagesContext pageContext) throws FormException {
    String value = "";
    String keys = "";
    String values = "";
    String html = "";
    int cols = 1;
    String defaultValue = "";
    String language = pageContext.getLanguage();

    String mandatoryImg = Util.getIcon("mandatoryField");

    String fieldName = template.getFieldName();
    Map<String, String> parameters = template.getParameters(language);

    if (!field.getTypeName().equals(TextField.TYPE)) {
      SilverTrace.info("form", "RadioButtonDisplayer.display", "form.INFO_NOT_CORRECT_TYPE",
          TextField.TYPE);
    }

    if (!field.isNull()) {
      value = field.getValue(language);
    }

    if (parameters.containsKey("keys")) {
      keys = parameters.get("keys");
    }

    if (parameters.containsKey("values")) {
      values = parameters.get("values");
    }

    try {
      if (parameters.containsKey("cols")) {
        cols = (Integer.valueOf(parameters.get("cols"))).intValue();
      }
    } catch (NumberFormatException nfe) {
      SilverTrace.error("form", "RadioButtonDisplayer.display",
          "form.EX_ERR_ILLEGAL_PARAMETER_COL",
          parameters.get("cols"));
      cols = 1;
    }

    if (parameters.containsKey("default") && !pageContext.isIgnoreDefaultValues()) {
      defaultValue = parameters.get("default");
    }

    if (!StringUtil.isDefined(value)) {
      value = defaultValue;
    }

    if (pageContext.isBlankFieldsUse()) {
      value = "";
    }

    // if either keys or values is not filled
    // take the same for keys and values
    if (keys.equals("") && !values.equals("")) {
      keys = values;
    }
    if (values.equals("") && !keys.equals("")) {
      values = keys;
    }

    StringTokenizer stKeys = new StringTokenizer(keys, "##");
    StringTokenizer stValues = new StringTokenizer(values, "##");
    String optKey = "";
    String optValue = "";
    int nbTokens = getNbHtmlObjectsDisplayed(template, pageContext);

    if (stKeys.countTokens() != stValues.countTokens()) {
      SilverTrace.error("form",
          "RadioButtonDisplayer.display",
          "form.EX_ERR_ILLEGAL_PARAMETERS",
          "Nb keys=" + stKeys.countTokens() + " & Nb values=" + stValues.countTokens());
    } else {
      html += "<table border=0>";
      int col = 0;
      for (int i = 0; i < nbTokens; i++) {
        if (col == 0) {
          html += "<tr>";
        }

        col++;
        html += "<td>";
        optKey = stKeys.nextToken();
        optValue = stValues.nextToken();

        html +=
            "<INPUT type=\"radio\" id=\"" + fieldName + "\" name=\"" + fieldName + "\" value=\"" +
            optKey + "\" ";

        if (template.isDisabled() || template.isReadOnly()) {
          html += " disabled ";
        }

        if (optKey.equals(value)) {
          html += " checked ";
        }

        // last radio button must be checked if others are not !
        /*
         * if (!checked && i==nbTokens-1 && template.isMandatory()) { html += " checked "; }
         */

        html += ">&nbsp;" + optValue;

        if (i == nbTokens - 1) {
          if (template.isMandatory() && !template.isDisabled() && !template.isReadOnly() &&
              !template.isHidden() && pageContext.
              useMandatory()) {
            html +=
                "&nbsp;<img src=\"" + mandatoryImg + "\" width=\"5\" height=\"5\" border=\"0\">";
          }
        }

        // html += "\n";
        html += "</td>";

        if (col == cols) {
          html += "</tr>";
          col = 0;
        }
      }

      if (col != 0) {
        html += "</tr>";
      }

      html += "</table>";
    }
    out.println(html);
  }

  /**
   * Updates the value of the field. The fieldName must be used to retrieve the HTTP parameter from
   * the request.
   * @throw FormException if the field type is not a managed type.
   * @throw FormException if the field doesn't accept the new value.
   */
  /*
   * public void update(HttpServletRequest request, Field field, FieldTemplate template,
   * PagesContext PagesContext) throws FormException { if (!
   * field.getTypeName().equals(TextField.TYPE)) throw new
   * FormException("TextAreaFieldDisplayer.update","form.EX_NOT_CORRECT_TYPE",TextField.TYPE);
   * String newValue = request.getParameter(template.getFieldName()); if
   * (field.acceptValue(newValue, PagesContext.getLanguage())) field.setValue(newValue,
   * PagesContext.getLanguage()); else throw new
   * FormException("TextAreaFieldDisplayer.update","form.EX_NOT_CORRECT_VALUE",TextField.TYPE); }
   */
  public List<String> update(String newValue,
      Field field,
      FieldTemplate template,
      PagesContext PagesContext)
      throws FormException {

    if (!field.getTypeName().equals(TextField.TYPE)) {
      throw new FormException("TextAreaFieldDisplayer.update", "form.EX_NOT_CORRECT_TYPE",
          TextField.TYPE);
    }
    if (field.acceptValue(newValue, PagesContext.getLanguage())) {
      field.setValue(newValue, PagesContext.getLanguage());
    } else {
      throw new FormException("TextAreaFieldDisplayer.update", "form.EX_NOT_CORRECT_VALUE",
          TextField.TYPE);
    }
    return new ArrayList<String>();
  }

  public boolean isDisplayedMandatory() {
    return true;
  }

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
    if (values.equals("") && !keys.equals("")) {
      values = keys;
    }

    // Calculate numbers of html elements
    StringTokenizer stKeys = new StringTokenizer(keys, "##");
    return stKeys.countTokens();
  }
}
