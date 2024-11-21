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
import org.silverpeas.kernel.util.StringUtil;
import org.silverpeas.kernel.logging.SilverLogger;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

/**
 * A ListBoxFieldDisplayer is an object which can display a listbox in HTML the content of a listbox
 * to a end user and can retrieve via HTTP any updated value.
 *
 * @see Field
 * @see FieldTemplate
 * @see Form
 * @see FieldDisplayer
 */
public class ListBoxFieldDisplayer extends AbstractFieldDisplayer<TextField> {


  /**
   * Returns the name of the managed types.
   */
  public String[] getManagedTypes() {
    return new String[]{TextField.TYPE};
  }

  @Override
  public void displayScripts(PrintWriter out, FieldTemplate template, PagesContext pagesContext) {
    produceMandatoryCheck(out, template, pagesContext);
    Util.getJavascriptChecker(template.getFieldName(), pagesContext, out);
  }

  @Override
  public void display(PrintWriter out, TextField field, FieldTemplate template,
      PagesContext pageContext) throws FormException {
    String keys = "";
    String values;
    StringBuilder html = new StringBuilder();
    String language = pageContext.getLanguage();

    String fieldName = template.getFieldName();
    Map<String, String> parameters = template.getParameters(language);

    String value = getDefaultValue(template, pageContext);
    if (field != null && !field.isNull()) {
      value = field.getValue(language);
    }

    generateCssClass(html, fieldName, parameters);

    if (template.isDisabled() || template.isReadOnly()) {
      html.append(" disabled");
    }

    html.append(" >\n").append("<option value=\"\"></option>\n");
    if (parameters.containsKey("keys")) {
      keys = parameters.get("keys");
    }

    values = parameters.getOrDefault("values", keys);
    //noinspection StringTokenizerDelimiter
    StringTokenizer stKeys = new StringTokenizer(keys, "##");
    //noinspection StringTokenizerDelimiter
    StringTokenizer stValues = new StringTokenizer(values, "##");
    String optKey;
    String optValue;
    int nbTokens = stKeys.countTokens();

    if (stKeys.countTokens() != stValues.countTokens()) {
      SilverLogger.getLogger(this)
          .error("Illegal Parameters. Key count = {0}, values count = {1}", stKeys.countTokens(),
              stValues.countTokens());
    } else {
      for (int i = 0; i < nbTokens; i++) {
        optKey = stKeys.nextToken();
        optValue = stValues.nextToken();

        html.append("<option ");
        if (optKey.equals(value) || optValue.equals(value)) {
          html.append(" selected ");
        }
        html.append("value=\"").append(optKey).append("\">").append(optValue).append("</option>\n");
      }
    }

    html.append("</select>\n");

    if (template.isMandatory() && !template.isDisabled() && !template.isReadOnly() && !template.
        isHidden() && pageContext.useMandatory()) {
      html.append(Util.getMandatorySnippet());
    }

    out.println(html);
  }

  private static void generateCssClass(StringBuilder html, String fieldName, Map<String, String> parameters) {
    String cssClass = null;
    if (parameters.containsKey("class")) {
      cssClass = parameters.get("class");
      if (StringUtil.isDefined(cssClass)) {
        cssClass = "class=\"" + cssClass + "\"";
      }
    }
    if (StringUtil.isDefined(cssClass)) {
      html.append("<select ")
          .append(cssClass)
          .append(" id=\"")
          .append(fieldName)
          .append("\" name=\"")
          .append(fieldName)
          .append("\"");
    } else {
      html.append("<select id=\"")
          .append(fieldName)
          .append("\" name=\"")
          .append(fieldName)
          .append("\"");
    }
  }

  @Override
  public List<String> update(String newValue, TextField field, FieldTemplate template,
      PagesContext pagesContext) throws FormException {

    CheckBoxDisplayer.setFieldValue(newValue, field, pagesContext);
    return new ArrayList<>();
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
