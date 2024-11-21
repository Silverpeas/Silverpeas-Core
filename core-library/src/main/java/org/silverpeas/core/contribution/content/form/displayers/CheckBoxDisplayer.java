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
import org.silverpeas.core.contribution.content.form.Field;
import org.silverpeas.core.contribution.content.form.FieldDisplayer;
import org.silverpeas.core.contribution.content.form.FieldTemplate;
import org.silverpeas.core.contribution.content.form.Form;
import org.silverpeas.core.contribution.content.form.FormException;
import org.silverpeas.core.contribution.content.form.PagesContext;
import org.silverpeas.core.contribution.content.form.Util;
import org.silverpeas.core.contribution.content.form.field.TextField;
import org.silverpeas.kernel.util.StringUtil;
import org.silverpeas.kernel.logging.SilverLogger;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

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

  private static final String VALUES = "values";

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
    String selectedValues = "";

    List<String> valuesFromDB = new ArrayList<>();
    String keys = "";
    String values = "";
    StringBuilder html = new StringBuilder();
    int cols = 1;
    String language = pageContext.getLanguage();

    String fieldName = template.getFieldName();
    Map<String, String> parameters = template.getParameters(language);
    if (!field.isNull()) {
      selectedValues = field.getValue(language);
    }
    //noinspection StringTokenizerDelimiter
    StringTokenizer st = new StringTokenizer(selectedValues, "##");
    while (st.hasMoreTokens()) {
      valuesFromDB.add(st.nextToken());
    }
    if (parameters.containsKey("keys")) {
      keys = parameters.get("keys");
    }
    if (parameters.containsKey(VALUES)) {
      values = parameters.get(VALUES);
    }
    String cssClass = null;
    if (parameters.containsKey("class")) {
      cssClass = parameters.get("class");
      if (StringUtil.isDefined(cssClass)) {
        cssClass = "class=\"" + cssClass + "\"";
      }
    }

    try {
      if (parameters.containsKey("cols")) {
        cols = Integer.parseInt(parameters.get("cols"));
      }
    } catch (NumberFormatException nfe) {
      SilverLogger.getLogger(this).error("Illegal Parameter cols: {0}", parameters.get("cols"));
    }

    String defaultValue = getDefaultValue(template, pageContext);
    if (!StringUtil.isDefined(values)) {
      values = defaultValue;
    }

    // if either keys or values is not filled
    // take the same for keys and values
    if ("".equals(keys) && !"".equals(values)) {
      keys = values;
    }
    if ("".equals(values) && !"".equals(keys)) {
      values = keys;
    }

    GenerationParameters params = new GenerationParameters();
    params.keys = keys;
    params.values = values;
    params.fieldName = fieldName;
    params.cssClass = cssClass;
    params.valuesFromDB = valuesFromDB;
    params.defaultValue = defaultValue;
    params.cols = cols;
    generateHTML(html, template, pageContext, params);
    out.println(html);
  }

  private void generateHTML(StringBuilder html, FieldTemplate template, PagesContext pageContext,
      GenerationParameters params) {
    //noinspection StringTokenizerDelimiter
    StringTokenizer stKeys = new StringTokenizer(params.keys, "##");
    //noinspection StringTokenizerDelimiter
    StringTokenizer stValues = new StringTokenizer(params.values, "##");

    if (stKeys.countTokens() != stValues.countTokens()) {
      SilverLogger.getLogger(this)
          .error("Illegal Parameters. Key count = {0}, values count = {1}", stKeys.countTokens(),
              stValues.countTokens());
    } else {
      html.append("<table border=\"0\">");
      params.stKeys = stKeys;
      params.stValues = stValues;
      params.nbOfTokenToDisplay = getNbHtmlObjectsDisplayed(template, pageContext);
      int col = 0;
      for (int i = 0; i < params.nbOfTokenToDisplay; i++) {
        col = generateHTMLValue(html, template, pageContext, i, col, params);
      }
      if (col != 0) {
        html.append("</tr>");
      }
      html.append("</table>");
    }
  }

  private static int generateHTMLValue(StringBuilder html, FieldTemplate template,
      PagesContext pageContext, int rowIndex, int colIndex, GenerationParameters parameters) {
    if (colIndex == 0) {
      html.append("<tr>");
    }

    colIndex++;
    html.append("<td>");
    String optKey = parameters.stKeys.nextToken();
    String optValue = parameters.stValues.nextToken();
    html.append("<input type=\"checkbox\" id=\"").append(parameters.fieldName).append("_")
        .append(rowIndex);
    html.append("\" name=\"").append(parameters.fieldName).append("\" value=\"").append(optKey)
        .append("\" ");
    if (StringUtil.isDefined(parameters.cssClass)) {
      html.append(parameters.cssClass);
    }
    if (template.isDisabled() || template.isReadOnly()) {
      html.append(" disabled=\"disabled\" ");
    }
    boolean mustBeChecked =
        parameters.valuesFromDB.isEmpty() && !pageContext.isIgnoreDefaultValues() && (
        optKey.equals(parameters.defaultValue) || optValue.equals(parameters.defaultValue));

    if (parameters.valuesFromDB.contains(optKey)) {
      mustBeChecked = true;
    }

    if (mustBeChecked) {
      html.append(" checked=\"checked\" ");
    }

    html.append("/>&nbsp;").append(optValue);

    // last checkBox
    if (rowIndex == parameters.nbOfTokenToDisplay - 1 && template.isMandatory() && !template.isDisabled() &&
        !template.isReadOnly() && !template.isHidden() && pageContext.useMandatory()) {
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
    String keys = "";
    String values = "";
    Map<String, String> parameters = template.getParameters(pagesContext.getLanguage());
    if (parameters.containsKey("keys")) {
      keys = parameters.get("keys");
    }
    if (parameters.containsKey(VALUES)) {
      values = parameters.get(VALUES);
    }

    // if either keys or values is not filled
    // take the same for keys and values
    if (keys.isEmpty() && !values.isEmpty()) {
      keys = values;
    }

    // Calculate numbers of html elements
    //noinspection StringTokenizerDelimiter
    StringTokenizer stKeys = new StringTokenizer(keys, "##");
    return stKeys.countTokens();

  }

  private static class GenerationParameters {
    String keys;
    String values;
    String fieldName;
    String cssClass;
    List<String> valuesFromDB;
    String defaultValue;
    int cols;
    StringTokenizer stKeys;
    StringTokenizer stValues;
    int nbOfTokenToDisplay;
  }
}
