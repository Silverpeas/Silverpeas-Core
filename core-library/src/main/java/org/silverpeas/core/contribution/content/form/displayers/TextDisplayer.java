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
import org.silverpeas.core.contribution.content.form.field.DateField;
import org.silverpeas.core.contribution.content.form.field.TextField;
import org.silverpeas.core.contribution.content.form.record.GenericFieldTemplate;
import org.silverpeas.core.silvertrace.SilverTrace;
import org.silverpeas.core.util.DateUtil;
import org.silverpeas.core.util.EncodeHelper;
import org.silverpeas.core.util.StringUtil;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

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

  /**
   * Constructeur
   */
  public TextDisplayer() {
  }

  /**
   * Returns the name of the managed types.
   */
  public String[] getManagedTypes() {
    return new String[]{TextField.TYPE, DateField.TYPE};
  }

  /**
   * Prints the javascripts which will be used to control the new value given to the named field.
   * The error messages may be adapted to a local language. The FieldTemplate gives the field type
   * and constraints. The FieldTemplate gives the local labeld too. Never throws an Exception but
   * log a silvertrace and writes an empty string when :
   * <ul>
   * <li>the fieldName is unknown by the template.</li>
   * <li>the field type is not a managed type.</li>
   * </ul>
   * @throws java.io.IOException
   */
  @Override
  public void displayScripts(PrintWriter out, FieldTemplate template, PagesContext PagesContext)
      throws java.io.IOException {
  }

  /**
   * Prints the HTML value of the field. The displayed value must be updatable by the end user. The
   * value format may be adapted to a local language. The fieldName must be used to name the html
   * form input. Never throws an Exception but log a silvertrace and writes an empty string when :
   * <ul>
   * <li>the field type is not a managed type.</li>
   * </ul>
   * @throws FormException
   */
  @Override
  public void display(PrintWriter out, Field field, FieldTemplate template,
      PagesContext pagesContext) throws FormException {
    StringBuilder html = new StringBuilder(10000);
    String language = pagesContext.getLanguage();
    Map<String, String> parameters = template.getParameters(language);
    String value = "";
    if (!field.isNull()) {
      if (field.getTypeName().equals(DateField.TYPE)) {
        try {
          value = DateUtil.getOutputDate(field.getValue(), pagesContext.getLanguage());
        } catch (Exception e) {
          SilverTrace.error("form", "TextDisplayer.display", "form.INFO_NOT_CORRECT_TYPE",
              "value = " + field.getValue(), e);
        }
      } else {
        value = EncodeHelper.convertWhiteSpacesForHTMLDisplay(field.getValue(language));
      }
    }

    String classe = null;
    if (parameters.containsKey("class")) {
      classe = parameters.get("class");
      if (classe != null) {
        classe = "class=\"" + classe + "\"";
      }
    }

    if (parameters.containsKey("values") || parameters.containsKey("keys")) {
      Map<String, String> keyValuePairs = ((GenericFieldTemplate) template).getKeyValuePairs(
          language);
      String newValue = "";
      if (StringUtil.isDefined(value)) {
        if (value.contains("##")) {
          // Try to display a checkbox list
          StringTokenizer tokenizer = new StringTokenizer(value, "##");
          String t;
          while (tokenizer.hasMoreTokens()) {
            t = tokenizer.nextToken();
            t = keyValuePairs.get(t);
            newValue += t;

            if (tokenizer.hasMoreTokens()) {
              newValue += ", ";
            }
          }
        } else {
          newValue = keyValuePairs.get(value);
        }
      }
      value = newValue;
    }
    if (StringUtil.isDefined(classe)) {
      html.append("<span ").append(classe).append(">");
    }

    if (parameters.containsKey("fontSize") || parameters.containsKey("fontColor")
        || parameters.containsKey("fontFace")) {
      html.append("<font");
    }

    String size = "";
    if (parameters.containsKey("fontSize")) {
      size = parameters.get("fontSize");
      html.append(" size=\"").append(size).append("\"");
    }

    String color = "";
    if (parameters.containsKey("fontColor")) {
      color = parameters.get("fontColor");
      html.append(" color=\"").append(color).append("\"");
    }

    String face = "";
    if (parameters.containsKey("fontFace")) {
      face = parameters.get("fontFace");
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
    if (StringUtil.isDefined(classe)) {
      html.append("</span>");
    }
    out.println(html.toString());
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
      PagesContext PagesContext) throws FormException {
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
