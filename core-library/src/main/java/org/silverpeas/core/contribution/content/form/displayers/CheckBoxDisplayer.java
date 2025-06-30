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

import org.apache.commons.fileupload.FileItem;
import org.silverpeas.core.contribution.content.form.*;
import org.silverpeas.core.contribution.content.form.field.TextField;
import org.silverpeas.core.contribution.content.form.record.Parameter;
import org.silverpeas.kernel.annotation.NonNull;
import org.silverpeas.kernel.annotation.Nullable;
import org.silverpeas.kernel.util.Mutable;
import org.silverpeas.kernel.util.StringUtil;
import org.silverpeas.kernel.logging.SilverLogger;

import java.io.PrintWriter;
import java.util.*;

/**
 * A CheckBoxDisplayer is an object which can display a checkbox in HTML the content of a checkbox
 * to a end user and can retrieve via HTTP any updated value.
 * <p>
 *
 * @see Field
 * @see FieldTemplate
 * @see Form
 * @see FieldDisplayer
 */
public class CheckBoxDisplayer extends AbstractFieldDisplayer<TextField> {

  /**
   * Returns the name of the managed types.
   */
  public String[] getManagedTypes() {
    return new String[]{TextField.TYPE};
  }

  @Override
  public void displayScripts(PrintWriter out, FieldTemplate template, PagesContext pagesContext) {
    String language = pagesContext.getLanguage();
    String fieldName = template.getFieldName();
    if (template.isMandatory() && pagesContext.useMandatory()) {
      out.println(" var checked = false;\n");
      out.println(" for (var i = 0; i < " + getNbHtmlObjectsDisplayed(template, pagesContext) +
          "; i++) {\n");
      out.println("   if (document.getElementsByName('" + fieldName + "')[i].checked) {\n");
      out.println("     checked = true;\n");
      out.println("   }\n");
      out.println(" }\n");
      out.println(" if(!ignoreMandatory && checked == false) {\n");
      out.println("   errorMsg+=\"  - '" + template.getLabel(language) + "' " +
          Util.getString("GML.MustBeFilled", language) + "\\n \";");
      out.println("   errorNb++;");
      out.println(" }");
    }
    Util.getJavascriptChecker(template.getFieldName(), pagesContext, out);
  }

  @Override
  public void display(PrintWriter out, TextField field, FieldTemplate template,
      PagesContext pageContext) throws FormException {
    String language = pageContext.getLanguage();

    List<String> valuesFromDB = getSelectedValues(field, language);

    StringBuilder html = new StringBuilder();

     Map<String, String> parameters = template.getParameters(language);
    String cssClass = getStyleClasses(parameters);
    int cols = getColumnSize(parameters);

    FieldValuesTemplate valuesTemplate = template.getFieldValuesTemplate(language);
    String defaultValue = getDefaultValue(template, pageContext);
    if (valuesTemplate.isEmpty()) {
      valuesTemplate.withAsValue(defaultValue, defaultValue);
    }

    GenerationParameters params = new GenerationParameters();
    params.fieldTemplate = template;
    params.fieldValuesTemplate = valuesTemplate;
    params.cssClass = cssClass;
    params.valuesFromDB = valuesFromDB;
    params.defaultValue = defaultValue;
    params.cols = cols;
    generateHTML(html, params, pageContext);
    out.println(html);
  }

  private int getColumnSize(Map<String, String> parameters) {
    int cols = -1;
    try {
      if (parameters.containsKey("cols")) {
        cols = Integer.parseInt(parameters.get("cols"));
      }
    } catch (NumberFormatException nfe) {
      SilverLogger.getLogger(this).error("Illegal Parameter cols: {0}", parameters.get("cols"));
    }
    return cols;
  }

  @Nullable
  private static String getStyleClasses(Map<String, String> parameters) {
    String cssClass = null;
    if (parameters.containsKey("class")) {
      cssClass = parameters.get("class");
      if (StringUtil.isDefined(cssClass)) {
        cssClass = "class=\"" + cssClass + "\"";
      }
    }
    return cssClass;
  }

  @NonNull
  private static List<String> getSelectedValues(TextField field, String language) {
    if (!field.isNull()) {
      String selectedValues = field.getValue(language);
      return Parameter.decode(selectedValues);
    }
    return List.of();
  }

  private void generateHTML(StringBuilder html, GenerationParameters params, PagesContext pageContext) {
      html.append("<table border=\"0\">");
      params.nbOfTokenToDisplay = getNbHtmlObjectsDisplayed(params.fieldTemplate, pageContext);
      final Mutable<Integer> col = Mutable.of(0);
      final Mutable<Integer> idx = Mutable.of(0);
      params.fieldValuesTemplate
          .apply(v -> col.set(generateHTMLValue(html, v, params, pageContext, col.get(),
              idx.get())));
      if (col.get() != 0) {
        html.append("</tr>");
      }
      html.append("</table>");
  }

  private static int generateHTMLValue(StringBuilder html, FieldValue fieldValue,
      GenerationParameters parameters, PagesContext pageContext, int rowIndex, int colIndex) {
    if (colIndex == 0) {
      html.append("<tr>");
    }

    String fieldName = parameters.fieldTemplate.getFieldName();
    colIndex++;
    html.append("<td>");
    html.append("<input type=\"checkbox\" id=\"")
        .append(fieldName)
        .append("_")
        .append(rowIndex);
    html.append("\" name=\"")
        .append(fieldName)
        .append("\" value=\"")
        .append(fieldValue.getKey())
        .append("\" ");
    if (StringUtil.isDefined(parameters.cssClass)) {
      html.append(parameters.cssClass);
    }
    if (parameters.fieldTemplate.isDisabled() || parameters.fieldTemplate.isReadOnly()) {
      html.append(" disabled=\"disabled\" ");
    }
    boolean mustBeChecked =
        parameters.valuesFromDB.isEmpty() && !pageContext.isIgnoreDefaultValues()
            && (fieldValue.getKey().equals(parameters.defaultValue)
            || fieldValue.getLabel().equals(parameters.defaultValue));

    if (parameters.valuesFromDB.contains(fieldValue.getKey())) {
      mustBeChecked = true;
    }

    if (mustBeChecked) {
      html.append(" checked=\"checked\" ");
    }

    html.append("/>&nbsp;")
        .append(fieldValue.getLabel());

    // last checkBox
    if (rowIndex == parameters.nbOfTokenToDisplay - 1 && parameters.fieldTemplate.isMandatory()
        && !parameters.fieldTemplate.isDisabled() && !parameters.fieldTemplate.isReadOnly()
        && !parameters.fieldTemplate.isHidden() && pageContext.useMandatory()) {
      html.append(Util.getMandatorySnippet());
    }

    html.append("</td>");
    html.append("\n");
    if (colIndex == parameters.cols) {
      html.append("</tr>");
      colIndex = 0;
    }
    return colIndex;
  }

  @Override
  public List<String> update(List<FileItem> items, TextField field, FieldTemplate template,
      PagesContext pageContext) throws FormException {
    StringBuilder value = new StringBuilder();
    Iterator<FileItem> iter = items.iterator();
    String parameterName = template.getFieldName();
    while (iter.hasNext()) {
      FileItem item = iter.next();
      if (parameterName.equals(item.getFieldName())) {
        if (value.length() > 0) {
          value.append("##");
        }
        value.append(item.getString());
      }
    }
    if (pageContext.getUpdatePolicy() == PagesContext.ON_UPDATE_IGNORE_EMPTY_VALUES &&
        value.length() == 0) {
      return new ArrayList<>();
    }
    return update(value.toString(), field, template, pageContext);
  }

  @Override
  public List<String> update(String valuesToInsert, TextField field, FieldTemplate template,
      PagesContext pagesContext) throws FormException {
    setFieldValue(valuesToInsert, field, pagesContext);
    return new ArrayList<>();
  }

  static void setFieldValue(String valuesToInsert, TextField field, PagesContext pagesContext) throws FormException {
    if (!TextField.TYPE.equals(field.getTypeName())) {
      throw new FormException("Incorrect field type '{0}', expected; {0}", TextField.TYPE);
    }

    if (field.acceptValue(valuesToInsert, pagesContext.getLanguage())) {
      field.setValue(valuesToInsert, pagesContext.getLanguage());
    } else {
      throw new FormException("Incorrect field value type. Expected {0}", TextField.TYPE);
    }
  }

  @Override
  public boolean isDisplayedMandatory() {
    return true;
  }

  @Override
  public int getNbHtmlObjectsDisplayed(FieldTemplate template, PagesContext pagesContext) {
    return template.getFieldValuesTemplate(pagesContext.getLanguage()).size();
  }

  private static class GenerationParameters {
    FieldTemplate fieldTemplate;
    FieldValuesTemplate fieldValuesTemplate;
    String cssClass;
    List<String> valuesFromDB;
    String defaultValue;
    int cols;
    int nbOfTokenToDisplay;
  }
}
