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

import org.apache.ecs.xhtml.img;
import org.apache.ecs.xhtml.input;
import org.silverpeas.core.contribution.content.form.*;
import org.silverpeas.core.contribution.content.form.field.TextField;
import org.silverpeas.core.contribution.content.form.field.TextFieldImpl;

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

  @Override
  public void display(PrintWriter out, TextField field, FieldTemplate template,
      PagesContext pageContext) throws FormException {
    if (field == null) {
      return;
    }

    var fieldProps = getFieldProperties(template, field, pageContext);
    var fieldName = fieldProps.getFieldName();

    // Suggestions used ?
    String paramSuggestions = fieldProps.getParameters().getOrDefault("suggestions", "false");
    boolean useSuggestions = Boolean.parseBoolean(paramSuggestions);
    List<String> suggestions =
        useSuggestions((TextFieldImpl) field, template, pageContext, fieldName, useSuggestions);
    String cssClass = getCssClass(fieldProps.getParameters());

    input textInput = makeTextInput(fieldProps, cssClass);
    img image = getMandatoryIcon(template, pageContext);

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
      var elt = setImage(textInput, image);
      out.println(elt);
    }
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