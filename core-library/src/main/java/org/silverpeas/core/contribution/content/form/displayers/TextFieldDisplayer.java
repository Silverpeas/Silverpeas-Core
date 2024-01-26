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
import org.silverpeas.core.util.WebEncodeHelper;
import org.silverpeas.kernel.util.StringUtil;

import java.io.PrintWriter;
import java.util.List;
import java.util.Map;

/**
 * A TextFieldDisplayer is an object which can display a TextField in HTML the content of a
 * TextField to a end user and can retrieve via HTTP any updated value.
 * @see Field
 * @see FieldTemplate
 * @see Form
 * @see FieldDisplayer
 */
public class TextFieldDisplayer extends AbstractTextFieldDisplayer {

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
    if (field == null) {
      return;
    }

    String fieldName = Util.getFieldOccurrenceName(template.getFieldName(), field.getOccurrence());
    Map<String, String> parameters = template.getParameters(pageContext.getLanguage());

    // Suggestions used ?
    String paramSuggestions = parameters.getOrDefault("suggestions", "false");
    boolean useSuggestions = Boolean.parseBoolean(paramSuggestions);
    List<String> suggestions =
        useSuggestions((TextFieldImpl) field, template, pageContext, fieldName, useSuggestions);

    String cssClass = getCssClass(parameters);
    String defaultValue = getDefaultValue(template, pageContext);
    String value = getValue(field, pageContext, defaultValue);

    input textInput = getTextInput(template, fieldName, parameters, cssClass, value);
    img image = getImage(template, pageContext);

    if (suggestions != null && !suggestions.isEmpty()) {
      TextFieldImpl.printSuggestionsIncludes(fieldName, out);
      out.println("<div id=\"listAutocomplete" + fieldName + "\">\n");

      out.println(textInput);

      out.println("<div id=\"container" + fieldName + "\"/>\n");
      out.println("</div>\n");

      if (image != null) {
        out.println(image);
      }

      TextFieldImpl.printSuggestionsScripts(fieldName, suggestions, out);
    } else {
      if (image != null) {
        ElementContainer container = new ElementContainer();
        container.addElement(textInput);
        container.addElement("&nbsp;");
        container.addElement(image);
        out.println(container);
      } else {
        out.println(textInput);
      }
    }
  }

  private static img getImage(final FieldTemplate template, final PagesContext pageContext) {
    img image = null;
    if (template.isMandatory() && !template.isDisabled() && !template.isReadOnly() && !template.
        isHidden() && pageContext.useMandatory()) {
      image = new img();
      image.setSrc(Util.getIcon("mandatoryField"));
      image.setWidth(5);
      image.setHeight(5);
      image.setBorder(0);
    }
    return image;
  }

  private static input getTextInput(final FieldTemplate template, final String fieldName,
      final Map<String, String> parameters, final String cssClass, final String value) {
    input textInput = new input();
    textInput.setName(fieldName);
    textInput.setID(fieldName);
    textInput.setValue(WebEncodeHelper.javaStringToHtmlString(value));
    textInput.setType(template.isHidden() ? input.hidden : input.text);
    textInput.setMaxlength(parameters.getOrDefault(TextField.PARAM_MAXLENGTH, "1000"));
    textInput.setSize(parameters.getOrDefault("size", "50"));
    if (parameters.containsKey("border")) {
      textInput.setBorder(Integer.parseInt(parameters.get("border")));
    }
    if (template.isDisabled()) {
      textInput.setDisabled(true);
    } else if (template.isReadOnly()) {
      textInput.setReadOnly(true);
    }
    if (StringUtil.isDefined(cssClass)) {
      textInput.setClass(cssClass);
    }
    return textInput;
  }

  private static String getValue(final TextField field, final PagesContext pageContext,
      final String defaultValue) {
    String value = (!field.isNull() ? field.getValue(pageContext.getLanguage()) : defaultValue);

    if (pageContext.isBlankFieldsUse()) {
      value = "";
    }
    return value;
  }

  private static String getCssClass(final Map<String, String> parameters) {
    String cssClass = null;
    if (parameters.containsKey("class")) {
      cssClass = parameters.get("class");
      if (cssClass != null) {
        cssClass = "class=\"" + cssClass + "\"";
      }
    }
    return cssClass;
  }

  private static List<String> useSuggestions(final TextFieldImpl field,
      final FieldTemplate template,
      final PagesContext pageContext, final String fieldName, final boolean useSuggestions) {
    List<String> suggestions = null;
    if (useSuggestions) {
      suggestions = field.getSuggestions(fieldName, template.getTemplateName(),
          pageContext.getComponentId());
    }
    return suggestions;
  }
}