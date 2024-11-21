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

import org.apache.ecs.ElementContainer;
import org.apache.ecs.Printable;
import org.apache.ecs.xhtml.img;
import org.apache.ecs.xhtml.input;
import org.owasp.encoder.Encode;
import org.silverpeas.core.contribution.content.form.FieldTemplate;
import org.silverpeas.core.contribution.content.form.FormException;
import org.silverpeas.core.contribution.content.form.PagesContext;
import org.silverpeas.core.contribution.content.form.Util;
import org.silverpeas.core.contribution.content.form.field.TextField;
import org.silverpeas.core.util.WebEncodeHelper;
import org.silverpeas.kernel.annotation.Nullable;
import org.silverpeas.kernel.util.StringUtil;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public abstract class AbstractTextFieldDisplayer extends AbstractFieldDisplayer<TextField> {

  private static final String[] MANAGED_TYPES = new String[]{TextField.TYPE};
  private static final String ERROR_MSG_WITH_HYPHEN = "     errorMsg+=\"  - '";
  private static final String JS_NB_ERROR_INCREMENT = "     errorNb++;\n";
  private static final String JS_CARRIAGE_RETURN = "\\n\";\n";
  private static final String JS_CLOSING_BRACE = "   }\n";

  /**
   * Returns the name of the managed types.
   */
  public String[] getManagedTypes() {
    return MANAGED_TYPES;
  }

  protected void addSpecificScript(PrintWriter out, FieldTemplate template, PagesContext pageContext) {

  }

  @Override
  public void displayScripts(PrintWriter out, FieldTemplate template, PagesContext pagesContext) {
    String language = pagesContext.getLanguage();
    String label = WebEncodeHelper.javaStringToJsString(template.getLabel(language));

    StringBuilder script = new StringBuilder(10000);

    produceMandatoryCheck(out, template, pagesContext);
    addSpecificScript(out, template, pagesContext);

    Map<String, String> parameters = template.getParameters(pagesContext.getLanguage());
    String contentType = parameters.get(TextField.CONTENT_TYPE);
    if (contentType != null) {
      if (contentType.equals(TextField.CONTENT_TYPE_INT)) {
        script.append("   if (field.value != \"\" && !(/^-?\\d+$/.test(field.value))) {\n");
        script.append(ERROR_MSG_WITH_HYPHEN).append(label).append("' ").
            append(Util.getString("GML.MustContainsNumber", language)).append(JS_CARRIAGE_RETURN);
        script.append(JS_NB_ERROR_INCREMENT);
        script.append(JS_CLOSING_BRACE);
      } else if (contentType.equals(TextField.CONTENT_TYPE_FLOAT)) {
        script.append("   field.value = field.value.replace(\",\", \".\")\n");
        script.append("   if (field.value != \"\" && !(/^([+-]?(((\\d+(\\.)?)|(\\d*\\.\\d+))");
        script.append("([eE][+-]?\\d+)?))$/.test(field.value))) {\n");
        script.append(ERROR_MSG_WITH_HYPHEN).append(label).append("' ");
        script.append(Util.getString("GML.MustContainsFloat", language)).append(JS_CARRIAGE_RETURN);
        script.append(JS_NB_ERROR_INCREMENT);
        script.append(JS_CLOSING_BRACE);
      }
    }

    String nbMaxCar = (parameters.containsKey(TextField.PARAM_MAXLENGTH) ? parameters.get(TextField.PARAM_MAXLENGTH) : Util.
        getSetting("nbMaxCar"));
    script.append("   if (! isValidText(field, ").append(nbMaxCar).append(")) {\n");
    script.append(ERROR_MSG_WITH_HYPHEN).append(label).append("' ").
        append(Util.getString("ContainsTooLargeText", language)).append(nbMaxCar).append(" ").
        append(Util.getString("Characters", language)).append(JS_CARRIAGE_RETURN);
    script.append(JS_NB_ERROR_INCREMENT);
    script.append(JS_CLOSING_BRACE);
    out.print(script);

    Util.getJavascriptChecker(template.getFieldName(), pagesContext, out);
  }

  @Override
  public boolean isDisplayedMandatory() {
    return true;
  }

  @Override
  public int getNbHtmlObjectsDisplayed(FieldTemplate template, PagesContext pagesContext) {
    return 1;
  }

  @Override
  public List<String> update(String newValue, TextField field, FieldTemplate template,
      PagesContext pagesContext) throws FormException {
    if (!TextField.TYPE.equals(field.getTypeName())) {
      throw new FormException("Incorrect field type '{0}', expected; {0}", field.getTypeName(),
          TextField.TYPE);
    }

    if (field.acceptValue(newValue, pagesContext.getLanguage())) {
      field.setValue(newValue, pagesContext.getLanguage());
    } else {
      throw new FormException("Incorrect field value type. Expected {0}", TextField.TYPE);
    }
    return new ArrayList<>();
  }

  protected Printable setImage(input textInput, @Nullable img image) {
    if (image != null) {
      ElementContainer container = new ElementContainer();
      container.addElement(textInput);
      container.addElement("&nbsp;");
      container.addElement(image);
      return container;
    } else {
      return textInput;
    }
  }

  protected static input makeTextInput(FieldProperties fieldProps,
      final String cssClass) {
    var template = fieldProps.getTemplate();
    var fieldName = fieldProps.getFieldName();
    var parameters = fieldProps.getParameters();
    input textInput = new input();
    textInput.setName(fieldName);
    textInput.setID(fieldName);
    textInput.setValue(fieldProps.getValue());
    textInput.setType(template.isHidden() ? input.hidden : input.text);
    textInput.setMaxlength(parameters.getOrDefault(TextField.PARAM_MAXLENGTH, "1000"));
    textInput.setSize(parameters.getOrDefault("size", "50"));
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
    return textInput;
  }

  protected FieldProperties getFieldProperties(FieldTemplate template, TextField field,
      PagesContext pagesContext) {
    String fieldName = Util.getFieldOccurrenceName(template.getFieldName(), field.getOccurrence());
    Map<String, String> parameters = template.getParameters(pagesContext.getLanguage());

    String defaultValue = getDefaultValue(template, pagesContext);
    String value = (!field.isNull() ? field.getValue(pagesContext.getLanguage()) : defaultValue);
    if (pagesContext.isBlankFieldsUse()) {
      value = "";
    }
    return new FieldProperties()
        .setTemplate(template)
        .setFieldName(fieldName)
        .setParameters(parameters)
        .setValue(value);
  }

  protected static class FieldProperties {
    private FieldTemplate template;
    private String fieldName;
    private String value;
    private Map<String, String> parameters;

    public FieldTemplate getTemplate() {
      return template;
    }

    private FieldProperties setTemplate(FieldTemplate template) {
      this.template = template;
      return this;
    }

    public String getFieldName() {
      return fieldName;
    }

    private FieldProperties setFieldName(String fieldName) {
      this.fieldName = fieldName;
      return this;
    }

    public String getValue() {
      return Encode.forHtml(value);
    }

    private FieldProperties setValue(String value) {
      this.value = value;
      return this;
    }

    public Map<String, String> getParameters() {
      return parameters;
    }

    private FieldProperties setParameters(Map<String, String> parameters) {
      this.parameters = parameters;
      return this;
    }
  }

}