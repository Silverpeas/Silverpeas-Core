/**
 * Copyright (C) 2000 - 2012 Silverpeas
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

package com.silverpeas.form.displayers;

import com.silverpeas.form.Field;
import com.silverpeas.form.FieldDisplayer;
import com.silverpeas.form.FieldTemplate;
import com.silverpeas.form.Form;
import com.silverpeas.form.FormException;
import com.silverpeas.form.PagesContext;
import com.silverpeas.form.Util;
import com.silverpeas.form.fieldType.TextField;
import com.silverpeas.util.EncodeHelper;
import com.silverpeas.util.StringUtil;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import java.io.IOException;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * A TextAreaFieldDisplayer is an object which can display a TextFiel in HTML the content of a
 * TextFiel to a end user and can retrieve via HTTP any updated value.
 * @see Field
 * @see FieldTemplate
 * @see Form
 * @see FieldDisplayer
 */
public class TextAreaFieldDisplayer extends AbstractFieldDisplayer<TextField> {

  /**
   * Constructeur
   */
  public TextAreaFieldDisplayer() {
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
  public void displayScripts(PrintWriter out, FieldTemplate template, PagesContext PagesContext)
      throws IOException {
    String language = PagesContext.getLanguage();

    if (!TextField.TYPE.equals(template.getTypeName())) {
      SilverTrace.info("form", "TextAreaFieldDisplayer.displayScripts",
          "form.INFO_NOT_CORRECT_TYPE", TextField.TYPE);
    }

    if (template.isMandatory() && PagesContext.useMandatory()) {
      out.println("	if (isWhitespace(stripInitialWhitespace(field.value))) {");
      out.println("		errorMsg+=\"  - '" + template.getLabel(language) + "' "
          + Util.getString("GML.MustBeFilled", language) + "\\n \";");
      out.println("		errorNb++;");
      out.println("	}");
    }

    out.println("	if (! isValidText(field, " + Util.getSetting("nbMaxCar") + ")) {");
    out.println("		errorMsg+=\"  - '" + template.getLabel(language) + "' "
        + Util.getString("ContainsTooLargeText", language) + Util.getSetting("nbMaxCar") + " "
        + Util.getString("Characters", language) + "\\n \";");
    out.println("		errorNb++;");
    out.println("	}");

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
  public void display(PrintWriter out, TextField field, FieldTemplate template,
      PagesContext PagesContext) throws FormException {
    String value = "";
    String rows = "6";
    String cols = "48";
    String html = "";
    String cssClass = null;

    String fieldName = template.getFieldName();
    Map<String, String> parameters = template.getParameters(PagesContext.getLanguage());

    if (!field.getTypeName().equals(TextField.TYPE)) {
      SilverTrace.info("form", "TextAreaFieldDisplayer.display", "form.INFO_NOT_CORRECT_TYPE",
          TextField.TYPE);
    }

    if (!field.isNull()) {
      value = field.getValue(PagesContext.getLanguage());
    }

    if (parameters.containsKey("class")) {
      cssClass = parameters.get("class");
      if (StringUtil.isDefined(cssClass)) {
        cssClass = "class=\"" + cssClass + "\"";
      }
    }

    if (StringUtil.isDefined(cssClass)) {
      html += "<span " + cssClass + ">";
    }

    html += "<textarea id=\"" + fieldName + "\" name=\"" + fieldName + "\"";

    if (parameters.containsKey("rows")) {
      rows = parameters.get("rows");
    }
    html += " rows=\"" + rows + "\"";

    if (parameters.containsKey("cols")) {
      cols = parameters.get("cols");
    }
    html += " cols=\"" + cols + "\"";

    if (template.isDisabled()) {
      html += " disabled=\"disabled\"";
    } else if (template.isReadOnly()) {
      html += " readonly=\"readonly\"";
    }

    html += " >" + EncodeHelper.javaStringToHtmlString(value) + "</textarea>";

    if (StringUtil.isDefined(cssClass)) {
      html += "</span>";
    }

    if (template.isMandatory() && !template.isDisabled() && !template.isReadOnly()
        && !template.isHidden() && PagesContext.useMandatory()) {
      html += Util.getMandatorySnippet();
    }

    out.println(html);
  }

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
    return new ArrayList<String>();
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
