/**
 * Copyright (C) 2000 - 2009 Silverpeas
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
 * "http://repository.silverpeas.com/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.silverpeas.form.fieldDisplayer;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.ecs.AlignType;
import org.apache.ecs.ElementContainer;
import org.apache.ecs.xhtml.a;
import org.apache.ecs.xhtml.img;
import org.apache.ecs.xhtml.input;
import org.apache.ecs.xhtml.span;

import com.silverpeas.form.Field;
import com.silverpeas.form.FieldDisplayer;
import com.silverpeas.form.FieldTemplate;
import com.silverpeas.form.Form;
import com.silverpeas.form.FormException;
import com.silverpeas.form.PagesContext;
import com.silverpeas.form.Util;
import com.silverpeas.form.fieldType.DateField;
import com.silverpeas.util.EncodeHelper;
import com.silverpeas.util.StringUtil;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.util.DateUtil;

/**
 * A DateFieldDisplayer is an object which can display a TextFiel in HTML the content of a TextFiel
 * to a end user and can retrieve via HTTP any updated value.
 * @see Field
 * @see FieldTemplate
 * @see Form
 * @see FieldDisplayer
 */
public class DateFieldDisplayer extends AbstractFieldDisplayer {

  /**
   * Constructeur
   */
  public DateFieldDisplayer() {
  }

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
  public void displayScripts(PrintWriter out, FieldTemplate template, PagesContext pagesContext)
      throws IOException {
    String language = pagesContext.getLanguage();

    if (!template.getTypeName().equals(DateField.TYPE)) {
      SilverTrace.info("form", "DateFieldDisplayer.displayScripts", "form.INFO_NOT_CORRECT_TYPE",
          DateField.TYPE);
    }

    if (template.isMandatory() && pagesContext.useMandatory()) {
      out.println("		if (isWhitespace(stripInitialWhitespace(field.value))) {");
      out.println("			errorMsg+=\"  - '" +
          EncodeHelper.javaStringToJsString(template.getLabel(language)) + "' " +
          Util.getString("GML.MustBeFilled", language) + "\\n \";");
      out.println("			errorNb++;");
      out.println("		}");
    }
    out.println("		if (! isWhitespace(stripInitialWhitespace(field.value))) {");
    out.println("			if (! isCorrectDate(extractYear(field.value, '" + language +
        "'), extractMonth(field.value, '" + language + "'), extractDay(field.value, '" + language +
        "'))) {");
    out.println("				errorMsg+=\"  - '" +
        EncodeHelper.javaStringToJsString(template.getLabel(language)) + "' " +
        Util.getString("GML.MustContainsCorrectDate", language) + "\\n \";");
    out.println("				errorNb++;");
    out.println("		}}");

    out.println("		if (! isValidText(field, " + Util.getSetting("nbMaxCar") + ")) {");
    out.println("			errorMsg+=\"  - '" + template.getLabel(language) + "' " +
        Util.getString("ContainsTooLargeText", language) + Util.getSetting("nbMaxCar") + " " +
        Util.getString("Characters", language) + "\\n \";");
    out.println("			errorNb++;");
    out.println("		}");

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
  public void display(PrintWriter out, Field field, FieldTemplate template,
      PagesContext pagesContext) throws FormException {
    if (!field.getTypeName().equals(DateField.TYPE)) {
      SilverTrace.info("form", "DateFieldDisplayer.display", "form.INFO_NOT_CORRECT_TYPE",
          DateField.TYPE);
    }

    String language = pagesContext.getLanguage();
    Map<String, String> parameters = template.getParameters(language);
    String fieldName = template.getFieldName();
    String cssClass = null;

    if (parameters.containsKey("class")) {
      cssClass = (String) parameters.get("class");
      if (StringUtil.isDefined(cssClass))
        cssClass = "class=\"" + cssClass + "\"";
    }

    String defaultParam = (parameters.containsKey("default") ? parameters.get("default") : "");
    String defaultValue = "";
    if ("now".equalsIgnoreCase(defaultParam) && !pagesContext.isIgnoreDefaultValues())
      defaultValue = DateUtil.dateToString(new Date(), pagesContext.getLanguage());

    String value = (!field.isNull() ? field.getValue(language) : defaultValue);
    if (pagesContext.isBlankFieldsUse())
      value = "";

    input inputField = new input();
    inputField.setID(fieldName);
    inputField.setName(fieldName);
    inputField.setValue(EncodeHelper.javaStringToHtmlString(value));
    inputField.setType(template.isHidden() ? input.hidden : input.text);
    inputField.setMaxlength(parameters.containsKey("maxLength") ? parameters.get("maxLength") : "10");
    inputField.setSize(parameters.containsKey("size") ? parameters.get("size") : "13");
    if (parameters.containsKey("border")) {
      inputField.setBorder(Integer.parseInt(parameters.get("border")));
    }
    if (template.isDisabled()) {
      inputField.setDisabled(true);
    } else if (template.isReadOnly()) {
      inputField.setReadOnly(true);
    }

    if (!template.isHidden() && !template.isDisabled() && !template.isReadOnly()) {
      ElementContainer container = new ElementContainer();
      container.addElement(inputField);

      container.addElement("&nbsp;");

      a link = new a();
      link.setHref("javascript:calendar('" + fieldName + "');");
      img calendarImg = new img();
      calendarImg.setSrc(Util.getIcon("calendar"));
      calendarImg.setWidth(15);
      calendarImg.setHeight(15);
      calendarImg.setBorder(0);
      String calendarLab = Util.getString("GML.viewCalendar", language);
      calendarImg.setAlt(calendarLab);
      calendarImg.setTitle(calendarLab);
      calendarImg.setAlign(AlignType.top);
      link.addElement(calendarImg);
      container.addElement(link);

      container.addElement("&nbsp;");
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

  /**
   * Updates the value of the field. The fieldName must be used to retrieve the HTTP parameter from
   * the request.
   * @throw FormException if the field type is not a managed type.
   * @throw FormException if the field doesn't accept the new value.
   */
  public List<String> update(String newValue, Field field, FieldTemplate template,
      PagesContext pagesContext) throws FormException {
    if (field.acceptValue(newValue, pagesContext.getLanguage())) {
      field.setValue(newValue, pagesContext.getLanguage());
    } else {
      throw new FormException("DateFieldDisplayer.update", "form.EX_NOT_CORRECT_VALUE",
          DateField.TYPE);
    }
    return new ArrayList<String>();
  }

  public boolean isDisplayedMandatory() {
    return true;
  }

  public int getNbHtmlObjectsDisplayed(FieldTemplate template, PagesContext pagesContext) {
    return 1;
  }

}
