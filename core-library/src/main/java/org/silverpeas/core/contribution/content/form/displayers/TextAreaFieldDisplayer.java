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
import org.silverpeas.core.util.EncodeHelper;
import org.silverpeas.core.util.StringUtil;

import java.io.PrintWriter;
import java.util.Map;

/**
 * A TextAreaFieldDisplayer is an object which can display a TextField in HTML the content of a
 * TextField to a end user and can retrieve via HTTP any updated value.
 *
 * @see Field
 * @see FieldTemplate
 * @see Form
 * @see FieldDisplayer
 */
public class TextAreaFieldDisplayer extends AbstractTextFieldDisplayer {

  static public final String PARAM_ROWS = "rows";
  static public final String PARAM_COLS = "cols";

  /**
   * Constructor
   */
  public TextAreaFieldDisplayer() {
  }

  /**
   * Prints the HTML value of the field. The displayed value must be updatable by the end user. The
   * value format may be adapted to a local language. The fieldName must be used to name the html
   * form input. Never throws an Exception but log a silvertrace and writes an empty string when :
   * <ul></li>
   * <li>the field type is not a managed type.</li>
   * </ul></li>
   */
  @Override
  public void display(PrintWriter out, TextField field, FieldTemplate template,
      PagesContext PagesContext) throws FormException {
    String value = "";
    String rows = "8";
    String cols = "100";
    String html = "";
    String cssClass = null;

    String fieldName = Util.getFieldOccurrenceName(template.getFieldName(), field.getOccurrence());
    Map<String, String> parameters = template.getParameters(PagesContext.getLanguage());

    if (!field.getTypeName().equals(TextField.TYPE)) {

    }

    if (!field.isNull()) {
      value = field.getValue(PagesContext.getLanguage());
    }

    if (parameters.containsKey("class")) {
      cssClass = parameters.get("class");
      if (StringUtil.isDefined(cssClass)) {
        cssClass = "class=\"" + cssClass + "\"";
      }
    }

    if (StringUtil.isDefined(cssClass)) {
      html += "<span " + cssClass + ">";
    }

    html += "<textarea id=\"" + fieldName + "\" name=\"" + fieldName + "\"";

    if (parameters.containsKey(TextAreaFieldDisplayer.PARAM_ROWS)) {
      rows = parameters.get(TextAreaFieldDisplayer.PARAM_ROWS);
    }
    html += " rows=\"" + rows + "\"";

    if (parameters.containsKey(TextAreaFieldDisplayer.PARAM_COLS)) {
      cols = parameters.get(TextAreaFieldDisplayer.PARAM_COLS);
    }
    html += " cols=\"" + cols + "\"";

    if (template.isDisabled()) {
      html += " disabled=\"disabled\"";
    } else if (template.isReadOnly()) {
      html += " readonly=\"readonly\"";
    }

    html += " >" + EncodeHelper.javaStringToHtmlString(value) + "</textarea>";

    if (StringUtil.isDefined(cssClass)) {
      html += "</span>";
    }

    if (template.isMandatory() && !template.isDisabled() && !template.isReadOnly()
        && !template.isHidden() && PagesContext.useMandatory()) {
      html += Util.getMandatorySnippet();
    }

    out.println(html);
  }
}