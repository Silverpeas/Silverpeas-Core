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

import org.silverpeas.core.contribution.content.form.*;
import org.silverpeas.core.contribution.content.form.field.TextField;
import org.silverpeas.kernel.util.StringUtil;

import java.io.PrintWriter;

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

  public static final String PARAM_ROWS = "rows";
  public static final String PARAM_COLS = "cols";

  @Override
  public void display(PrintWriter out, TextField field, FieldTemplate template,
      PagesContext pageContext) throws FormException {
    String rows = "8";
    String cols = "100";
    String html = "";
    String cssClass = null;

    FieldProperties fieldProps = getFieldProperties(template, field, pageContext);

    if (fieldProps.getParameters().containsKey("class")) {
      cssClass = fieldProps.getParameters().get("class");
      if (StringUtil.isDefined(cssClass)) {
        cssClass = "class=\"" + cssClass + "\"";
      }
    }

    if (StringUtil.isDefined(cssClass)) {
      html += "<span " + cssClass + ">";
    }

    html += "<textarea id=\"" + fieldProps.getFieldName() + "\" name=\"" +
        fieldProps.getFieldName() + "\"";

    if (fieldProps.getParameters().containsKey(TextAreaFieldDisplayer.PARAM_ROWS)) {
      rows = fieldProps.getParameters().get(TextAreaFieldDisplayer.PARAM_ROWS);
    }
    html += " rows=\"" + rows + "\"";

    if (fieldProps.getParameters().containsKey(TextAreaFieldDisplayer.PARAM_COLS)) {
      cols = fieldProps.getParameters().get(TextAreaFieldDisplayer.PARAM_COLS);
    }
    html += " cols=\"" + cols + "\"";

    if (template.isDisabled()) {
      html += " disabled=\"disabled\"";
    } else if (template.isReadOnly()) {
      html += " readonly=\"readonly\"";
    }

    html += " >" + fieldProps.getValue() + "</textarea>";

    if (StringUtil.isDefined(cssClass)) {
      html += "</span>";
    }

    if (template.isMandatory() && !template.isDisabled() && !template.isReadOnly()
        && !template.isHidden() && pageContext.useMandatory()) {
      html += Util.getMandatorySnippet();
    }

    out.println(html);
  }
}