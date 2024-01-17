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
import org.silverpeas.core.contribution.content.form.field.DateField;
import org.apache.ecs.ElementContainer;
import org.apache.ecs.xhtml.img;
import org.apache.ecs.xhtml.input;
import org.apache.ecs.xhtml.span;
import org.silverpeas.core.util.DateUtil;
import org.silverpeas.core.util.WebEncodeHelper;
import org.silverpeas.core.util.StringUtil;

import javax.annotation.Nullable;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * A DateFieldDisplayer is an object which can display a TextField in HTML the content of a
 * TextField to a end user and can retrieve via HTTP any updated value.
 * <p>
 * @see Field
 * @see FieldTemplate
 * @see Form
 * @see FieldDisplayer
 */
public class DateFieldDisplayer extends AbstractFieldDisplayer<DateField> {

  /**
   * Returns the name of the managed types.
   */
  public String[] getManagedTypes() {
    return new String[] { DateField.TYPE };
  }

  /**
   * Prints the javascripts which will be used to control the new value given to the named field.
   * The error messages may be adapted to a local language. The FieldTemplate gives the field type
   * and constraints. The FieldTemplate gives the local labeld too. Never throws an Exception but
   * log a silvertrace and writes an empty string when :
   * <UL>
   * <LI>the fieldName is unknown by the template.
   * <LI>the field type is not a managed type.
   * </UL>
   */
  @Override
  public void displayScripts(PrintWriter out, FieldTemplate template, PagesContext pagesContext) {
    String language = pagesContext.getLanguage();
    String label = WebEncodeHelper.javaStringToJsString(template.getLabel(language));
    produceMandatoryCheck(out, template, pagesContext);
    out.println("\t\tif (! isWhitespace(stripInitialWhitespace(field.value))) {");
    out.println("\t\t\tif (! isCorrectDate(extractYear(field.value, '" + language
        + "'), extractMonth(field.value, '" + language + "'), extractDay(field.value, '" + language
        + "'))) {");
    out.println("\t\t\t\terrorMsg+=\"  - '"
        + label + "' "
        + Util.getString("GML.MustContainsCorrectDate", language) + "\\n\";");
    out.println("\t\t\t\terrorNb++;");
    out.println("\t\t}}");

    out.println("\t\tif (! isValidText(field, " + Util.getSetting("nbMaxCar") + ")) {");
    out.println("\t\t\terrorMsg+=\"  - '" + label + "' "
        + Util.getString("ContainsTooLargeText", language) + Util.getSetting("nbMaxCar") + " "
        + Util.getString("Characters", language) + "\\n\";");
    out.println("\t\t\terrorNb++;");
    out.println("\t\t}");

    Util.getJavascriptChecker(template.getFieldName(), pagesContext, out);
  }

  /**
   * Prints the HTML value of the field. The displayed value must be updatable by the end user. The
   * value format may be adapted to a local language. The fieldName must be used to name the html
   * form input. Never throws an Exception but log a silvertrace and writes an empty string when :
   * <UL>
   * <LI>the field type is not a managed type.
   * </UL>
   */
  @Override
  public void display(PrintWriter out, DateField field, FieldTemplate template,
      PagesContext pagesContext) throws FormException {
    String language = pagesContext.getLanguage();
    Map<String, String> parameters = template.getParameters(language);
    String fieldName = template.getFieldName();
    String cssClass = getCssClass(parameters);
    String value = getValue(field, pagesContext, language, parameters);

    input inputField = new input();
    inputField.setID(fieldName);
    inputField.setName(fieldName);
    inputField.setValue(WebEncodeHelper.javaStringToHtmlString(value));
    inputField.setType(template.isHidden() ? input.hidden : input.text);
    inputField.setMaxlength(parameters.getOrDefault("maxLength", "10"));
    inputField.setSize(parameters.getOrDefault("size", "13"));
    if (parameters.containsKey("border")) {
      inputField.setBorder(Integer.parseInt(parameters.get("border")));
    }
    if (template.isDisabled()) {
      inputField.setDisabled(true);
    } else if (template.isReadOnly()) {
      inputField.setReadOnly(true);
    }

    if (!template.isHidden() && !template.isDisabled() && !template.isReadOnly()) {
      inputField.setClass("dateToPick");
      ElementContainer container = new ElementContainer();
      container.addElement(inputField);

      span spanCSS = new span();
      if (StringUtil.isDefined(cssClass)) {
        spanCSS.setClass(cssClass);
      } else {
        spanCSS.setClass("txtnote");
      }
      spanCSS.addElement("(" + Util.getString("GML.dateFormatExemple", language) + ")");
      container.addElement(spanCSS);

      if (template.isMandatory() && pagesContext.useMandatory()) {
        container.addElement("&nbsp;");

        img image = new img();
        image.setSrc(Util.getIcon("mandatoryField"));
        image.setWidth(5);
        image.setHeight(5);
        image.setBorder(0);
        image.setAlt("");
        container.addElement(image);
      }

      out.println(container.toString());
    } else {
      out.println(inputField.toString());
    }
  }

  private String getValue(final DateField field, final PagesContext pagesContext,
      final String language, final Map<String, String> parameters) {
    String defaultParam = (parameters.containsKey("default") ? parameters.get("default") : "");
    String defaultValue = "";
    if (!pagesContext.isIgnoreDefaultValues() && (pagesContext.isCreation() || pagesContext.isDesignMode()))
    {
      if ("now".equalsIgnoreCase(defaultParam)) {
        defaultValue = DateUtil.dateToString(new Date(), pagesContext.getLanguage());
      }
      else
        defaultValue = defaultParam;
    }

    String value = (!field.isNull() ? field.getValue(language) : defaultValue);
    if (pagesContext.isBlankFieldsUse()) {
      value = "";
    }
    return value;
  }

  @Nullable
  private String getCssClass(final Map<String, String> parameters) {
    String cssClass = null;

    if (parameters.containsKey("class")) {
      cssClass = parameters.get("class");
      if (StringUtil.isDefined(cssClass)) {
        cssClass = "class=\"" + cssClass + "\"";
      }
    }
    return cssClass;
  }

  /**
   * Updates the value of the field. The fieldName must be used to retrieve the HTTP parameter from
   * the request.
   * @throws FormException if the field type is not a managed type or  if the field doesn't accept
   * the new value.
   */
  @Override
  public List<String> update(String newValue, DateField field, FieldTemplate template,
      PagesContext pagesContext) throws FormException {
    if (field.acceptValue(newValue, pagesContext.getLanguage())) {
      field.setValue(newValue, pagesContext.getLanguage());
    } else {
      throw new FormException("DateFieldDisplayer.update", "form.EX_NOT_CORRECT_VALUE",
          DateField.TYPE);
    }
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
