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

import org.silverpeas.core.contribution.content.form.FieldTemplate;
import org.silverpeas.core.contribution.content.form.FormException;
import org.silverpeas.core.contribution.content.form.PagesContext;
import org.silverpeas.core.contribution.content.form.Util;
import org.silverpeas.core.contribution.content.form.field.TextField;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public abstract class AbstractTextFieldDisplayer extends AbstractFieldDisplayer<TextField> {

  private final static String[] MANAGED_TYPES = new String[]{TextField.TYPE};

  /**
   * Returns the name of the managed types.
   */
  public String[] getManagedTypes() {
    return MANAGED_TYPES;
  }

  protected void addMandatoryScript(StringBuilder script, FieldTemplate template, PagesContext pageContext) {
    if (template.isMandatory() && pageContext.useMandatory()) {
      String language = pageContext.getLanguage();
      String label = template.getLabel(language);
      script.append("   if (isWhitespace(stripInitialWhitespace(field.value))) {\n");
      script.append("     errorMsg+=\"  - '").append(label).append("' ").
          append(Util.getString("GML.MustBeFilled", language)).append("\\n\";\n");
      script.append("     errorNb++;\n");
      script.append("   }\n");
    }
  }

  protected void addSpecificScript(PrintWriter out, FieldTemplate template, PagesContext pageContext) {

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

    }
    StringBuilder script = new StringBuilder(10000);

    addMandatoryScript(script, template, pagesContext);

    addSpecificScript(out, template, pagesContext);

    Map<String, String> parameters = template.getParameters(pagesContext.getLanguage());
    String contentType = parameters.get(TextField.CONTENT_TYPE);
    if (contentType != null) {
      if (contentType.equals(TextField.CONTENT_TYPE_INT)) {
        script.append("   if (field.value != \"\" && !(/^-?\\d+$/.test(field.value))) {\n");
        script.append("     errorMsg+=\"  - '").append(label).append("' ").
            append(Util.getString("GML.MustContainsNumber", language)).append(
            "\\n\";\n");
        script.append("     errorNb++;\n");
        script.append("   }\n");
      } else if (contentType.equals(TextField.CONTENT_TYPE_FLOAT)) {
        script.append("   field.value = field.value.replace(\",\", \".\")\n");
        script.append("   if (field.value != \"\" && !(/^([+-]?(((\\d+(\\.)?)|(\\d*\\.\\d+))");
        script.append("([eE][+-]?\\d+)?))$/.test(field.value))) {\n");
        script.append("     errorMsg+=\"  - '").append(label).append("' ");
        script.append(Util.getString("GML.MustContainsFloat", language)).append("\\n\";\n");
        script.append("     errorNb++;\n");
        script.append("   }\n");
      }
    }

    String nbMaxCar = (parameters.containsKey(TextField.PARAM_MAXLENGTH) ? parameters.get(TextField.PARAM_MAXLENGTH) : Util.
        getSetting("nbMaxCar"));
    script.append("   if (! isValidText(field, ").append(nbMaxCar).append(")) {\n");
    script.append("     errorMsg+=\"  - '").append(label).append("' ").
        append(Util.getString("ContainsTooLargeText", language)).append(nbMaxCar).append(" ").
        append(Util.getString("Characters", language)).append("\\n\";\n");
    script.append("     errorNb++;\n");
    script.append("   }\n");
    out.print(script.toString());

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
      PagesContext PagesContext) throws FormException {
    if (!TextField.TYPE.equals(field.getTypeName())) {
      throw new FormException("AbstractTextFieldDisplayer.update", "form.EX_NOT_CORRECT_TYPE",
          TextField.TYPE);
    }

    if (field.acceptValue(newValue, PagesContext.getLanguage())) {
      field.setValue(newValue, PagesContext.getLanguage());
    } else {
      throw new FormException("AbstractTextFieldDisplayer.update", "form.EX_NOT_CORRECT_VALUE",
          TextField.TYPE);
    }
    return new ArrayList<>();
  }

}