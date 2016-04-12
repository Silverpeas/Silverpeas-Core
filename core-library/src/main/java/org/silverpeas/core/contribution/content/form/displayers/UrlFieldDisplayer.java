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
import org.silverpeas.core.contribution.content.form.field.TextFieldImpl;
import org.apache.ecs.ElementContainer;
import org.apache.ecs.xhtml.img;
import org.apache.ecs.xhtml.input;
import org.silverpeas.core.util.EncodeHelper;
import org.silverpeas.core.util.StringUtil;

import java.io.PrintWriter;
import java.util.List;
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
public class UrlFieldDisplayer extends AbstractTextFieldDisplayer {

  public UrlFieldDisplayer() {
  }

  /**
   * Prints the HTML value of the field. The displayed value must be updatable by the end user. The
   * value format may be adapted to a local language. The fieldName must be used to name the html
   * form input. Never throws an Exception but log a silvertrace and writes an empty string when :
   * <ul>
   * <li>the field type is not a managed type.</li>
   * </ul>
   */
  @Override
  public void display(PrintWriter out, TextField field, FieldTemplate template,
      PagesContext pageContext) throws FormException {
    String value;
    String html = "";

    String fieldName = Util.getFieldOccurrenceName(template.getFieldName(), field.getOccurrence());

    Map<String, String> parameters = template.getParameters(pageContext.getLanguage());

    if (!field.getTypeName().equals(TextField.TYPE)) {

    }

    String defaultValue =
        (parameters.containsKey("default") ? parameters.get("default") : "");
    if (pageContext.isIgnoreDefaultValues()) {
      defaultValue = "";
    }
    value = (!field.isNull() ? field.getValue(pageContext.getLanguage()) : defaultValue);
    if (pageContext.isBlankFieldsUse()) {
      value = "";
    }

    if (template.isReadOnly() && !template.isHidden()) {
      if (StringUtil.isDefined(value)) {
        if (!value.startsWith("http") && !value.startsWith("ftp:") && !value.startsWith("/")) {
          value = "http://" + value;
        }
        html =
            "<a target=\"_blank\" href=\"" + value + "\">"
            + EncodeHelper.javaStringToHtmlString(value) + "</a>";
      }
    } else {
      // Suggestions used ?
      String paramSuggestions =
          parameters.containsKey("suggestions") ? parameters.get("suggestions") : "false";
      boolean useSuggestions = Boolean.valueOf(paramSuggestions);
      List<String> suggestions = null;
      if (useSuggestions) {
        TextFieldImpl textField = (TextFieldImpl) field;
        suggestions =
            textField.getSuggestions(fieldName, template.getTemplateName(), pageContext.
            getComponentId());
      }

      input inputField = new input();
      inputField.setName(fieldName);
      inputField.setID(fieldName);
      inputField.setValue(EncodeHelper.javaStringToHtmlString(value));
      inputField.setType(template.isHidden() ? input.hidden : input.text);
      inputField.setMaxlength(parameters.containsKey("maxLength") ? parameters.get("maxLength")
          : "1000");
      inputField.setSize(parameters.containsKey("size") ? parameters.get("size") : "50");
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

      if (suggestions != null && suggestions.size() > 0) {
        TextFieldImpl.printSuggestionsIncludes(pageContext, fieldName, out);
        out.println("<div id=\"listAutocomplete" + fieldName + "\">\n");

        out.println(inputField.toString());

        out.println("<div id=\"container" + fieldName + "\"/>\n");
        out.println("</div>\n");

        if (image != null) {
          image.setStyle("position:absolute;left:16em;top:5px");
          out.println(image.toString());
        }

        TextFieldImpl.printSuggestionsScripts(fieldName, suggestions, out);
      } else {
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
    out.println(html);
  }
}