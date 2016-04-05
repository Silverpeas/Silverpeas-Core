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
import org.apache.ecs.ElementContainer;
import org.apache.ecs.xhtml.a;
import org.apache.ecs.xhtml.img;
import org.apache.ecs.xhtml.input;
import org.silverpeas.core.util.EncodeHelper;
import org.silverpeas.core.util.StringUtil;

import java.io.PrintWriter;
import java.util.Map;

/**
 * A TextFieldDisplayer is an object which can display a TextField in HTML the content of a
 * TextField to a end user and can retrieve via HTTP any updated value.
 *
 * @see Field
 * @see FieldTemplate
 * @see Form
 * @see FieldDisplayer
 */
public class EmailFieldDisplayer extends AbstractTextFieldDisplayer {

  public final static String PARAM_MAILTO = "mailto";
  public final static String PARAM_SIZE = "size";

  public EmailFieldDisplayer() {
  }

  @Override
  public void addSpecificScript(PrintWriter out, FieldTemplate template, PagesContext pageContext) {
    String language = pageContext.getLanguage();
    String label = template.getLabel(language);

    if (!template.getTypeName().equals(TextField.TYPE)) {

    }
    if (template.isMandatory() && pageContext.useMandatory()) {
      StringBuilder script = new StringBuilder(10000);

      script.append("   if (!checkemail(field.value)) {\n");
      script.append("     errorMsg+=\"  - '").append(label).append("' ").
          append(Util.getString("GML.MustContainsEmail", language)).append("\\n\";\n");
      script.append("     errorNb++;\n");
      script.append("   }\n");

      out.print(script.toString());
    }
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
      PagesContext pageContext) throws FormException {
    String fieldName = Util.getFieldOccurrenceName(template.getFieldName(), field.getOccurrence());
    Map<String, String> parameters = template.getParameters(pageContext.getLanguage());

    String defaultValue = (parameters.containsKey("default") ? parameters.get("default") : "");
    if (pageContext.isIgnoreDefaultValues()) {
      defaultValue = "";
    }
    String value = (!field.isNull() ? field.getValue(pageContext.getLanguage()) : defaultValue);
    if (pageContext.isBlankFieldsUse()) {
      value = "";
    }

    if (template.isReadOnly() && !template.isHidden()) {
      if (StringUtil.isDefined(value)) {
        if (StringUtil.getBooleanValue(parameters.get(PARAM_MAILTO))) {
          a mailto = new a();
          mailto.setHref("mailto:"+value);
          mailto.addElement(value);
          out.println(mailto.toString());
        } else {
          out.println(value);
        }
      }
    } else {
      input inputField = new input();
      inputField.setName(fieldName);
      inputField.setID(fieldName);
      inputField.setValue(EncodeHelper.javaStringToHtmlString(value));
      inputField.setType(template.isHidden() ? input.hidden : input.text);
      inputField.setMaxlength(100);
      inputField.setSize(parameters.containsKey(PARAM_SIZE) ? parameters.get(PARAM_SIZE) : "50");
      if (parameters.containsKey("border")) {
        inputField.setBorder(Integer.parseInt(parameters.get("border")));
      }
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
  }
}