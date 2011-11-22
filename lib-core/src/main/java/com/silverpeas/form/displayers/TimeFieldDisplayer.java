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
package com.silverpeas.form.displayers;

import java.io.PrintWriter;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.ecs.ElementContainer;
import org.apache.ecs.xhtml.img;
import org.apache.ecs.xhtml.input;

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
import java.util.Collections;

/**
 * A TimeFieldDisplayer is an object which can display a time
 * @see Field
 * @see FieldTemplate
 * @see Form
 * @see FieldDisplayer
 */
public class TimeFieldDisplayer extends AbstractFieldDisplayer<TextField> {

  public TimeFieldDisplayer() {
  }

  /**
   * Returns the name of the managed types.
   * @return 
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
   * @param out 
   * @param template
   * @param PagesContext 
   * @throws java.io.IOException  
   */
  @Override
  public void displayScripts(PrintWriter out, FieldTemplate template, PagesContext PagesContext)
      throws java.io.IOException {
    String language = PagesContext.getLanguage();
    if (!TextField.TYPE.equals(template.getTypeName())) {
      SilverTrace.info("form", "TimeFieldDisplayer.displayScripts", "form.INFO_NOT_CORRECT_TYPE",
          TextField.TYPE);
    }
    
    out.println("var "+template.getFieldName()+"Empty = isWhitespace(stripInitialWhitespace(field.value));");

    if (template.isMandatory() && PagesContext.useMandatory()) {
      out.println("	if ("+template.getFieldName()+"Empty) {");
      out.println("		errorMsg+=\"  - '" + template.getLabel(language) + "' "
          + Util.getString("GML.MustBeFilled", language) + "\\n \";");
      out.println("		errorNb++;");
      out.println("	}");
    }

    out.println(" if (!"+template.getFieldName()+"Empty) {");
    out.println("var reg=new RegExp(\"^([01][0-9]|2[0-3]):([0-5][0-9])$\",\"g\");");
    out.println("if (!reg.test(field.value)) {");
    out.println("		errorMsg+=\"  - '" + template.getLabel(language) + "' " + Util.getString(
        "GML.MustContainsCorrectHour", language) + "\\n \";");
    out.println("		errorNb++;");
    out.println("}");
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
   * @param out 
   * @param field 
   * @param template 
   * @param pageContext 
   * @throws FormException 
   */
  @Override
  public void display(PrintWriter out, TextField field, FieldTemplate template, PagesContext pageContext)
      throws FormException {
    String value = "";
    String html = "";

    String fieldName = template.getFieldName();
    SilverTrace.info("form", "TimeFieldDisplayer.display", "root.MSG_GEN_PARAM_VALUE",
        "fieldName=" + fieldName);
    Map<String, String> parameters = template.getParameters(pageContext.getLanguage());
    if (field == null) {
      return;
    }
    if (!field.getTypeName().equals(TextField.TYPE)) {
      SilverTrace.info("form", "TimeFieldDisplayer.display", "form.INFO_NOT_CORRECT_TYPE",
          TextField.TYPE);
    }

    String defaultParam = (parameters.containsKey("default") ? parameters.get("default") : "");
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
      input inputField = new input();
      inputField.setName(template.getFieldName());
      inputField.setID(template.getFieldName());
      inputField.setValue(EncodeHelper.javaStringToHtmlString(value));
      inputField.setType(template.isHidden() ? input.hidden : input.text);
      inputField.setMaxlength("5");
      inputField.setSize("10");
      if (template.isDisabled()) {
        inputField.setDisabled(true);
      } else if (template.isReadOnly()) {
        inputField.setReadOnly(true);
      }

      img image = null;
      if (template.isMandatory() && !template.isDisabled() && !template.isReadOnly()
          && !template.isHidden() && pageContext.useMandatory()) {
        image = new img();
        image.setSrc(Util.getIcon("mandatoryField"));
        image.setWidth(5);
        image.setHeight(5);
        image.setBorder(0);
      }

      // print field
      if (image != null) {
        ElementContainer container = new ElementContainer();
        container.addElement(inputField);
        container.addElement("&nbsp;");
        container.addElement(image);
        out.println(container.toString());
      } else {
        out.println(inputField.toString());
      }
    }
    out.println(html);
  }

  public List<String> update(String newValue, TextField field, FieldTemplate template,
      PagesContext PagesContext) throws FormException {
    if (!TextField.TYPE.equals(field.getTypeName())) {
      throw new FormException("TimeFieldDisplayer.update", "form.EX_NOT_CORRECT_TYPE",
          TextField.TYPE);
    }
    if (field.acceptValue(newValue, PagesContext.getLanguage())) {
      field.setValue(newValue, PagesContext.getLanguage());
    } else {
      throw new FormException("TimeFieldDisplayer.update", "form.EX_NOT_CORRECT_VALUE",
          TextField.TYPE);
    }
    return Collections.emptyList();
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
