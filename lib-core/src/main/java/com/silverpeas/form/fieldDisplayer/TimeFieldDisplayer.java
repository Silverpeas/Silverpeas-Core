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
import java.util.Date;
import java.util.Map;

import org.apache.ecs.ElementContainer;
import org.apache.ecs.html.IMG;
import org.apache.ecs.html.Input;

import com.silverpeas.form.Field;
import com.silverpeas.form.FieldDisplayer;
import com.silverpeas.form.FieldTemplate;
import com.silverpeas.form.Form;
import com.silverpeas.form.FormException;
import com.silverpeas.form.PagesContext;
import com.silverpeas.form.Util;
import com.silverpeas.form.fieldType.TextField;
import com.silverpeas.util.EncodeHelper;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.util.DateUtil;
import java.util.ArrayList;
import java.util.List;

/**
 * A TimeFieldDisplayer is an object which can display a time
 * @see Field
 * @see FieldTemplate
 * @see Form
 * @see FieldDisplayer
 */
public class TimeFieldDisplayer extends AbstractFieldDisplayer {

  public TimeFieldDisplayer() {
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
      SilverTrace.info("form", "TimeFieldDisplayer.displayScripts", "form.INFO_NOT_CORRECT_TYPE",
          TextField.TYPE);
    }

    if (template.isMandatory() && PagesContext.useMandatory()) {
      out.println("	if (isWhitespace(stripInitialWhitespace(field.value))) {");
      out.println("		errorMsg+=\"  - '" + template.getLabel(language) + "' " +
          Util.getString("GML.MustBeFilled",
          language) + "\\n \";");
      out.println("		errorNb++;");
      out.println("	}");
    }

    out.println("var reg=new RegExp(\"^([01][0-9]|2[0-3]):([0-5][0-9])$\",\"g\");");
    out.println("if (!reg.test(field.value))");
    out.println("{");
    out.println("		errorMsg+=\"  - '" + template.getLabel(language) + "' " + Util.getString(
        "GML.MustContainsCorrectHour", language) + "\\n \";");
    out.println("		errorNb++;");
    out.println("}");

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
    String html = "";

    String fieldName = template.getFieldName();
    SilverTrace.info("form", "TimeFieldDisplayer.display", "root.MSG_GEN_PARAM_VALUE",
        "fieldName=" + fieldName);
    Map parameters = template.getParameters(pageContext.getLanguage());

    if (field == null) {
      return;
    }

    if (!field.getTypeName().equals(TextField.TYPE)) {
      SilverTrace.info("form", "TimeFieldDisplayer.display", "form.INFO_NOT_CORRECT_TYPE",
          TextField.TYPE);
    }

    String defaultParam =
        (parameters.containsKey("default") ? (String) parameters.get("default") : "");
    String defaultValue = "";
    if ("now".equalsIgnoreCase(defaultParam) && !pageContext.isIgnoreDefaultValues()) {
      defaultValue = DateUtil.formatTime(new Date());
    }

    value = (!field.isNull() ? field.getValue(pageContext.getLanguage()) : defaultValue);
    if (pageContext.isBlankFieldsUse()) {
      value = "";
    }

    if (template.isReadOnly() && !template.isHidden()) {
      html = value;
    } else {
      Input input = new Input();
      input.setName(template.getFieldName());
      input.setID(template.getFieldName());
      input.setValue(EncodeHelper.javaStringToHtmlString(value));
      input.setType(template.isHidden() ? Input.hidden : Input.text);
      input.setMaxlength("5");
      input.setSize("10");
      if (template.isDisabled()) {
        input.setDisabled(true);
      } else if (template.isReadOnly()) {
        input.setReadOnly(true);
      }

      IMG img = null;
      if (template.isMandatory() && !template.isDisabled() && !template.isReadOnly() &&
          !template.isHidden() && pageContext.
          useMandatory()) {
        img = new IMG();
        img.setSrc(Util.getIcon("mandatoryField"));
        img.setWidth(5);
        img.setHeight(5);
        img.setBorder(0);
      }

      // print field
      if (img != null) {
        ElementContainer container = new ElementContainer();
        container.addElement(input);
        container.addElement("&nbsp;");
        container.addElement(img);
        out.println(container.toString());
      } else {
        out.println(input.toString());
      }
    }
    out.println(html);
  }

  public List<String> update(String newValue,
      Field field,
      FieldTemplate template,
      PagesContext PagesContext)
      throws FormException {

    if (!field.getTypeName().equals(TextField.TYPE)) {
      throw new FormException("TimeFieldDisplayer.update", "form.EX_NOT_CORRECT_TYPE",
          TextField.TYPE);
    }

    if (field.acceptValue(newValue, PagesContext.getLanguage())) {
      field.setValue(newValue, PagesContext.getLanguage());
    } else {
      throw new FormException("TimeFieldDisplayer.update", "form.EX_NOT_CORRECT_VALUE",
          TextField.TYPE);
    }
    return new ArrayList<String>();
  }

  public boolean isDisplayedMandatory() {
    return true;
  }

  public int getNbHtmlObjectsDisplayed(FieldTemplate template, PagesContext pagesContext) {
    return 1;
  }
}
