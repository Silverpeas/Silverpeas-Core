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

import org.owasp.encoder.Encode;
import org.silverpeas.core.contribution.attachment.AttachmentServiceProvider;
import org.silverpeas.core.contribution.attachment.model.SimpleDocument;
import org.silverpeas.core.contribution.attachment.model.SimpleDocumentPK;
import org.silverpeas.core.contribution.content.form.*;
import org.silverpeas.core.contribution.content.form.field.DateField;
import org.silverpeas.core.contribution.content.form.field.FileField;
import org.silverpeas.core.contribution.content.form.field.TextField;
import org.silverpeas.core.contribution.content.form.record.Parameter;
import org.silverpeas.core.util.DateUtil;
import org.silverpeas.core.util.WebEncodeHelper;
import org.silverpeas.kernel.exception.NotFoundException;
import org.silverpeas.kernel.logging.SilverLogger;
import org.silverpeas.kernel.util.StringUtil;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * A TextFieldDisplayer is an object which can display a TextField in HTML the content of a
 * TextField to a end user and can retrieve via HTTP any updated value.
 *
 * @see Field
 * @see FieldTemplate
 * @see Form
 * @see FieldDisplayer
 */
public class TextDisplayer extends AbstractFieldDisplayer<Field> {

  private static final String FONT_SIZE = "fontSize";
  private static final String FONT_COLOR = "fontColor";
  private static final String FONT_FACE = "fontFace";

  /**
   * Returns the name of the managed types.
   */
  public String[] getManagedTypes() {
    return new String[]{TextField.TYPE, DateField.TYPE};
  }

  @Override
  public void displayScripts(PrintWriter out, FieldTemplate template, PagesContext pagesContext)
      throws java.io.IOException {
    // no script to print out
  }

  @Override
  public void display(PrintWriter out, Field field, FieldTemplate template,
      PagesContext pagesContext) throws FormException {
    StringBuilder html = new StringBuilder(10000);
    Map<String, String> parameters = template.getParameters(pagesContext.getLanguage());

    String value = getFieldValue(template, parameters, field, pagesContext);
    String cssClass = getCssClass(parameters);

    if (StringUtil.isDefined(cssClass)) {
      html.append("<span ").append(cssClass).append(">");
    }

    if (parameters.containsKey(FONT_SIZE) || parameters.containsKey(FONT_COLOR)
        || parameters.containsKey(FONT_FACE)) {
      html.append("<font");
    }

    String size = "";
    if (parameters.containsKey(FONT_SIZE)) {
      size = parameters.get(FONT_SIZE);
      html.append(" size=\"").append(size).append("\"");
    }

    String color = "";
    if (parameters.containsKey(FONT_COLOR)) {
      color = parameters.get(FONT_COLOR);
      html.append(" color=\"").append(color).append("\"");
    }

    String face = "";
    if (parameters.containsKey(FONT_FACE)) {
      face = parameters.get(FONT_FACE);
      html.append(" face=\"").append(face).append("\"");
    }

    if (StringUtil.isDefined(size) || StringUtil.isDefined(color) || StringUtil.isDefined(face)) {
      html.append(">");
    }
    String bold = "";
    if (parameters.containsKey("bold")) {
      bold = parameters.get("bold");
      if ("true".equals(bold)) {
        html.append("<b>");
      }
    }
    html.append(value);

    if (StringUtil.isDefined(bold)) {
      html.append("</b>");
    }
    if (StringUtil.isDefined(size) || StringUtil.isDefined(color) || StringUtil.isDefined(face)) {
      html.append("</font>");
    }
    if (StringUtil.isDefined(cssClass)) {
      html.append("</span>");
    }
    out.print(html);
  }

  private static String getCssClass(Map<String, String> parameters) {
    String cssClass = null;
    if (parameters.containsKey("class")) {
      cssClass = parameters.get("class");
      if (cssClass != null) {
        cssClass = "class=\"" + cssClass + "\"";
      }
    }
    return cssClass;
  }

  private String getFieldValue(FieldTemplate template, Map<String, String> parameters, Field field,
      PagesContext pagesContext) {
    String language = pagesContext.getLanguage();
    String value = "";
    if (!field.isNull()) {
      if (field.getTypeName().equals(DateField.TYPE)) {
        value = getDateValue(field, pagesContext);
      } else if (field.getTypeName().equals(FileField.TYPE)) {
        value = getFilenameValue(field, pagesContext);
      } else {
        value = WebEncodeHelper.convertBlanksForHtml(Encode.forHtml(field.getValue(language)));
      }
    }

    if (parameters.containsKey("values") || parameters.containsKey("keys")) {
      var valuesTemplate = template.getFieldValuesTemplate(language);
      var emptyValue = FieldValue.emptyFor(language);
      StringBuilder newValue = new StringBuilder();
      if (StringUtil.isDefined(value)) {
        if (value.contains("##")) {
          // Try to display a checkbox list
          buildValuesList(value, valuesTemplate, newValue);
        } else {
          newValue.append(valuesTemplate.get(value).orElse(emptyValue).getLabel());
        }
      }
      value = newValue.toString();
    }
    return value;
  }

  private static void buildValuesList(String value, FieldValuesTemplate valuesTemplate,
      StringBuilder valuesList) {
    var values = Parameter.decode(value).stream()
        .map(v -> valuesTemplate.get(v)
            .orElseThrow(() -> new NotFoundException("No such value for " + v)))
        .map(FieldValue::getLabel)
        .collect(Collectors.joining(", "));
    valuesList.append(values);
  }

  private static String getFilenameValue(Field field, PagesContext pagesContext) {
    SimpleDocument doc = AttachmentServiceProvider.getAttachmentService().searchDocumentById(
        new SimpleDocumentPK(field.getValue(), pagesContext.getComponentId()), null);
    if (doc != null) {
      return doc.getFilename();
    }
    return "";
  }

  private String getDateValue(Field field, PagesContext pagesContext) {
    try {
      return DateUtil.getOutputDate(field.getValue(), pagesContext.getLanguage());
    } catch (Exception e) {
      SilverLogger.getLogger(this).error("Incorrect type for value " + field.getValue(), e);
    }
    return "";
  }

  /**
   * Updates the value of the field. The fieldName must be used to retrieve the HTTP parameter from
   * the request.
   *
   * @throws FormException if the field type is not a managed type or if the field doesn't accept
   * the new value.
   */
  @Override
  public List<String> update(String newValue, Field field, FieldTemplate template,
      PagesContext pagesContext) throws FormException {
    return new ArrayList<>();
  }

  @Override
  public boolean isDisplayedMandatory() {
    return false;
  }

  @Override
  public int getNbHtmlObjectsDisplayed(FieldTemplate template, PagesContext pagesContext) {
    return 0;
  }
}
