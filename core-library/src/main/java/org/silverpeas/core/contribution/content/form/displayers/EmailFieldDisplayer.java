/*
 * Copyright (C) 2000 - 2024 Silverpeas
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
 * "https://www.silverpeas.org/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.contribution.content.form.displayers;

import org.apache.ecs.xhtml.a;
import org.apache.ecs.xhtml.input;
import org.silverpeas.core.contribution.content.form.*;
import org.silverpeas.core.contribution.content.form.field.TextField;
import org.silverpeas.kernel.util.StringUtil;

import java.io.PrintWriter;

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

  public static final String PARAM_MAILTO = "mailto";
  public static final String PARAM_SIZE = "size";

  @Override
  public void addSpecificScript(PrintWriter out, FieldTemplate template, PagesContext pageContext) {
    String language = pageContext.getLanguage();
    String label = template.getLabel(language);

    if (template.isMandatory() && pageContext.useMandatory()) {

      String script = "   if (!checkemail(field.value)) {\n" +
          "     errorMsg+=\"  - '" + label + "' " +
          Util.getString("GML.MustContainsEmail", language) + "\\n\";\n" +
          "     errorNb++;\n" +
          "   }\n";

      out.print(script);
    }
  }

  @Override
  public void display(PrintWriter out, TextField field, FieldTemplate template,
      PagesContext pageContext) throws FormException {
    FieldProperties fieldProps = getFieldProperties(template, field, pageContext);

    if (template.isReadOnly() && !template.isHidden()) {
      if (StringUtil.isDefined(fieldProps.getValue())) {
        if (StringUtil.getBooleanValue(fieldProps.getParameters().get(PARAM_MAILTO))) {
          a mailto = new a();
          mailto.setHref("mailto:"+fieldProps.getValue());
          mailto.addElement(fieldProps.getValue());
          out.println(mailto);
        } else {
          out.println(fieldProps.getValue());
        }
      }
    } else {
      input inputField = new input();
      inputField.setName(fieldProps.getFieldName());
      inputField.setID(fieldProps.getFieldName());
      inputField.setValue(fieldProps.getValue());
      inputField.setType(template.isHidden() ? input.hidden : input.text);
      inputField.setMaxlength(100);
      inputField.setSize(fieldProps.getParameters().getOrDefault(PARAM_SIZE, "50"));
      if (fieldProps.getParameters().containsKey("border")) {
        inputField.setBorder(Integer.parseInt(fieldProps.getParameters().get("border")));
      }
      var elt = initInputField(template, inputField, pageContext);
      out.println(elt);
    }
  }
}