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

import org.apache.ecs.xhtml.input;
import org.owasp.encoder.Encode;
import org.silverpeas.core.contribution.content.form.*;
import org.silverpeas.core.contribution.content.form.field.TextField;
import org.silverpeas.core.util.DateUtil;

import java.io.PrintWriter;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * A TimeFieldDisplayer is an object which can display a time
 *
 * @see Field
 * @see FieldTemplate
 * @see Form
 * @see FieldDisplayer
 */
public class TimeFieldDisplayer extends AbstractFieldDisplayer<TextField> {

  /**
   * Returns the name of the managed types.
   */
  public String[] getManagedTypes() {
    return new String[]{TextField.TYPE};
  }

  @Override
  public void displayScripts(PrintWriter out, FieldTemplate template, PagesContext pagesContext) {
    String language = pagesContext.getLanguage();
    String label = Encode.forHtml(template.getLabel(language));

    out.println("var " + template.getFieldName()
        + "Empty = isWhitespace(stripInitialWhitespace(field.value));");

    produceMandatoryCheck(out, template, pagesContext);

    out.println(" if (!" + template.getFieldName() + "Empty) {");
    out.println("var reg=new RegExp(\"^([01][0-9]|2[0-3]):([0-5][0-9])$\",\"g\");");
    out.println("if (!reg.test(field.value)) {");
    out.println("\t\terrorMsg+=\"  - '" + label + "' " + Util.getString(
        "GML.MustContainsCorrectHour", language) + "\\n \";");
    out.println("\t\terrorNb++;");
    out.println("}");
    out.println("}");

    Util.getJavascriptChecker(template.getFieldName(), pagesContext, out);
  }

  @Override
  public void display(PrintWriter out, TextField field, FieldTemplate template,
      PagesContext pageContext) throws FormException {
    if (field == null) {
      return;
    }
    Map<String, String> parameters = template.getParameters(pageContext.getLanguage());
    String value = getValue(field, pageContext, parameters);

    String html;
    if (template.isReadOnly() && !template.isHidden()) {
      html = value;
    } else {
      html = "";
      input inputField = new input();
      inputField.setName(template.getFieldName());
      inputField.setID(template.getFieldName());
      inputField.setValue(Encode.forHtml(value));
      inputField.setType(template.isHidden() ? input.hidden : input.text);
      inputField.setMaxlength("5");
      inputField.setSize("10");
      var elt = initInputField(template, inputField, pageContext);
      out.println(elt);
    }
    out.println(html);
  }

  private String getValue(final TextField field, final PagesContext pageContext,
      final Map<String, String> parameters) {
    String defaultParam = parameters.getOrDefault("default", "");
    String defaultValue = "";
    if ((pageContext.isCreation() || pageContext.isDesignMode()) &&
        !pageContext.isIgnoreDefaultValues() && !defaultParam.isEmpty()) {
      if ("now".equalsIgnoreCase(defaultParam)) {
        defaultValue = DateUtil.formatTime(new Date());
      } else {
        defaultValue = defaultParam;
      }
    }
    String value = (!field.isNull() ? field.getValue(pageContext.getLanguage()) : defaultValue);
    if (pageContext.isBlankFieldsUse()) {
      value = "";
    }
    return value;
  }

  public List<String> update(String newValue, TextField field, FieldTemplate template,
      PagesContext pagesContext) throws FormException {
    CheckBoxDisplayer.setFieldValue(newValue, field, pagesContext);
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
