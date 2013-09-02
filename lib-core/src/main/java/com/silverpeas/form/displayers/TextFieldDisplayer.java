/**
 * Copyright (C) 2000 - 2012 Silverpeas
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

package com.silverpeas.form.displayers;

import com.silverpeas.form.Field;
import com.silverpeas.form.FieldDisplayer;
import com.silverpeas.form.FieldTemplate;
import com.silverpeas.form.Form;
import com.silverpeas.form.FormException;
import com.silverpeas.form.PagesContext;
import com.silverpeas.form.Util;
import com.silverpeas.form.fieldType.TextField;
import com.silverpeas.form.fieldType.TextFieldImpl;
import com.silverpeas.util.EncodeHelper;
import com.silverpeas.util.StringUtil;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.ecs.ElementContainer;
import org.apache.ecs.xhtml.img;
import org.apache.ecs.xhtml.input;

/**
 * A TextFieldDisplayer is an object which can display a TextFiel in HTML the content of a TextFiel
 * to a end user and can retrieve via HTTP any updated value.
 * @see Field
 * @see FieldTemplate
 * @see Form
 * @see FieldDisplayer
 */
public class TextFieldDisplayer extends AbstractFieldDisplayer<TextField> {

  private final static String[] MANAGED_TYPES = new String[] { TextField.TYPE };

  /**
   * Constructeur
   */
  public TextFieldDisplayer() {
  }

  /**
   * Returns the name of the managed types.
   * @return
   */
  public String[] getManagedTypes() {
    return MANAGED_TYPES;
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
  public void displayScripts(PrintWriter out, FieldTemplate template, PagesContext pagesContext)
      throws IOException {
    String language = pagesContext.getLanguage();
    String label = template.getLabel(language);

    if (!template.getTypeName().equals(TextField.TYPE)) {
      SilverTrace.info("form", "TextFieldDisplayer.displayScripts", "form.INFO_NOT_CORRECT_TYPE",
          TextField.TYPE);
    }
    StringBuilder script = new StringBuilder(10000);

    if (template.isMandatory() && pagesContext.useMandatory()) {
      script.append("		if (isWhitespace(stripInitialWhitespace(field.value))) {\n");
      script.append("			errorMsg+=\"  - '").append(label).append("' ").
          append(Util.getString("GML.MustBeFilled", language)).append("\\n\";\n");
      script.append("			errorNb++;\n");
      script.append("		}\n");
    }

    Map<String, String> parameters = template.getParameters(pagesContext.getLanguage());
    String contentType = parameters.get(TextField.CONTENT_TYPE);
    if (contentType != null) {
      if (contentType.equals(TextField.CONTENT_TYPE_INT)) {
        script.append("		if (field.value != \"\" && !(/^-?\\d+$/.test(field.value))) {\n");
        script.append("			errorMsg+=\"  - '").append(label).append("' ").
            append(Util.getString("GML.MustContainsNumber", language)).append(
            "\\n\";\n");
        script.append("			errorNb++;\n");
        script.append("		}\n");
      } else if (contentType.equals(TextField.CONTENT_TYPE_FLOAT)) {
        script.append("		field.value = field.value.replace(\",\", \".\")\n");
        script.append("		if (field.value != \"\" && !(/^([+-]?(((\\d+(\\.)?)|(\\d*\\.\\d+))");
        script.append("([eE][+-]?\\d+)?))$/.test(field.value))) {\n");
        script.append("			errorMsg+=\"  - '").append(label).append("' ");
        script.append(Util.getString("GML.MustContainsFloat", language)).append("\\n\";\n");
        script.append("			errorNb++;\n");
        script.append("		}\n");
      }
    }

    String nbMaxCar = (parameters.containsKey("maxLength") ?
        parameters.get("maxLength") : Util.getSetting("nbMaxCar"));
    script.append("		if (! isValidText(field, ").append(nbMaxCar).append(")) {\n");
    script.append("			errorMsg+=\"  - '").append(label).append("' ").
        append(Util.getString("ContainsTooLargeText", language)).append(nbMaxCar).append(" ").
        append(Util.getString("Characters", language)).append("\\n\";\n");
    script.append("			errorNb++;\n");
    script.append("		}\n");
    out.print(script.toString());

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
  public void display(PrintWriter out, TextField field, FieldTemplate template,
      PagesContext pageContext)
      throws FormException {
    if (field == null) {
      return;
    }

    if (!field.getTypeName().equals(TextField.TYPE)) {
      SilverTrace.info("form", "TextFieldDisplayer.display", "form.INFO_NOT_CORRECT_TYPE",
          TextField.TYPE);
    }

    String fieldName = template.getFieldName();
    Map<String, String> parameters = template.getParameters(pageContext.getLanguage());

    // Suggestions used ?
    String paramSuggestions =
        parameters.containsKey("suggestions") ? parameters.get("suggestions") : "false";
    boolean useSuggestions = Boolean.valueOf(paramSuggestions);
    List<String> suggestions = null;
    if (useSuggestions) {
      TextFieldImpl textField = (TextFieldImpl) field;
      suggestions = textField.getSuggestions(fieldName, template.getTemplateName(),
          pageContext.getComponentId());
    }

    String cssClass = null;
    if (parameters.containsKey("class")) {
      cssClass = parameters.get("class");
      if (cssClass != null) {
        cssClass = "class=\"" + cssClass + "\"";
      }
    }

    String defaultValue =
        (parameters.containsKey("default") ? parameters.get("default") : "");
    if (pageContext.isIgnoreDefaultValues()) {
      defaultValue = "";
    }
    String value = (!field.isNull() ? field.getValue(pageContext.getLanguage()) : defaultValue);
    if (pageContext.isBlankFieldsUse()) {
      value = "";
    }

    input textInput = new input();
    textInput.setName(template.getFieldName());
    textInput.setID(template.getFieldName());
    textInput.setValue(EncodeHelper.javaStringToHtmlString(value));
    textInput.setType(template.isHidden() ? input.hidden : input.text);
    textInput.setMaxlength(parameters.containsKey("maxLength") ? parameters.get("maxLength") :
        "1000");
    textInput.setSize(parameters.containsKey("size") ? parameters.get("size") : "50");
    if (parameters.containsKey("border")) {
      textInput.setBorder(Integer.parseInt(parameters.get("border")));
    }
    if (template.isDisabled()) {
      textInput.setDisabled(true);
    } else if (template.isReadOnly()) {
      textInput.setReadOnly(true);
    }
    if (StringUtil.isDefined(cssClass)) {
      textInput.setClass(cssClass);
    }

    img image = null;
    if (template.isMandatory() && !template.isDisabled() && !template.isReadOnly() &&
        !template.isHidden() && pageContext.useMandatory()) {
      image = new img();
      image.setSrc(Util.getIcon("mandatoryField"));
      image.setWidth(5);
      image.setHeight(5);
      image.setBorder(0);
    }

    if (suggestions != null && suggestions.size() > 0) {
      TextFieldImpl.printSuggestionsIncludes(pageContext, fieldName, out);
      out.println("<div id=\"listAutocomplete" + fieldName + "\">\n");

      out.println(textInput.toString());

      out.println("<div id=\"container" + fieldName + "\"/>\n");
      out.println("</div>\n");

      if (image != null) {
        image.setStyle("position:absolute;left:16em;top:5px");
        out.println(image.toString());
      }

      TextFieldImpl.printSuggestionsScripts(pageContext, fieldName, suggestions, out);
    } else {
      if (image != null) {
        ElementContainer container = new ElementContainer();
        container.addElement(textInput);
        container.addElement("&nbsp;");
        container.addElement(image);
        out.println(container.toString());
      } else {
        out.println(textInput.toString());
      }
    }
  }

  @Override
  public List<String> update(String newValue, TextField field, FieldTemplate template,
      PagesContext pagesContext)
      throws FormException {
    if (!TextField.TYPE.equals(field.getTypeName())) {
      throw new FormException("TextFieldDisplayer.update", "form.EX_NOT_CORRECT_TYPE",
          TextField.TYPE);
    }
    if (field.acceptValue(newValue, pagesContext.getLanguage())) {
      field.setValue(newValue, pagesContext.getLanguage());
    } else {
      throw new FormException("TextFieldDisplayer.update", "form.EX_NOT_CORRECT_VALUE",
          TextField.TYPE);
    }
    return new ArrayList<String>();
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
