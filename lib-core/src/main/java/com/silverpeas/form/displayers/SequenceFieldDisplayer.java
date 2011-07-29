/**
 * Copyright (C) 2000 - 2011 Silverpeas
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

package com.silverpeas.form.displayers;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.ecs.html.Input;

import com.silverpeas.form.Field;
import com.silverpeas.form.FieldDisplayer;
import com.silverpeas.form.FieldTemplate;
import com.silverpeas.form.Form;
import com.silverpeas.form.FormException;
import com.silverpeas.form.PagesContext;
import com.silverpeas.form.fieldType.SequenceField;
import com.silverpeas.util.EncodeHelper;
import com.stratelia.silverpeas.silvertrace.SilverTrace;

/**
 * A SequenceFieldDisplayer is an object which can display a value corresponding to the setting of
 * a sequence. A such value is only readable to guaranty validity of each value belonging to the
 * sequence (unicity for instance).
 * @see Field
 * @see FieldTemplate
 * @see Form
 * @see FieldDisplayer
 */
public class SequenceFieldDisplayer extends AbstractFieldDisplayer {

  /**
   * Prints the HTML value of the field. The displayed value must be readable for the end user. The
   * value format follows the field's setting. The field's name is used to name the html form input.
   */
  public void display(PrintWriter out, Field field, FieldTemplate template,
    PagesContext pagesContext)
  throws FormException {
    if (!template.getTypeName().equals(SequenceField.TYPE)) {
      SilverTrace.info("form", "SequenceFieldDisplayer.display", "form.INFO_NOT_CORRECT_TYPE",
        SequenceField.TYPE);
    }
    
    SequenceField sequenceField = null;
    if (!field.getTypeName().equals(SequenceField.TYPE)) {
      SilverTrace.info("form", "SequenceFieldDisplayer.display", "form.INFO_NOT_CORRECT_TYPE",
        SequenceField.TYPE);
    } else {
      sequenceField = (SequenceField) field;
    }
    
    String language = pagesContext.getLanguage();
    String fieldName = template.getFieldName();
    
    String value = "";
    if (field.isNull()) {
      if (sequenceField != null) {
        Map<String, String> parameters = template.getParameters(language);
        int minLength = 1;
        if (parameters.containsKey("minLength")) {
          minLength = Integer.parseInt(parameters.get("minLength"));
        }
        int startValue = 1;
        if (parameters.containsKey("startValue")) {
          startValue = Integer.parseInt(parameters.get("startValue"));
        }
        boolean reuseAvailableValues = (parameters.containsKey("reuseAvailableValues")
          && "true".equals(parameters.get("reuseAvailableValues")));
        value = sequenceField.getNextValue(fieldName, template.getTemplateName(),
          pagesContext.getComponentId(), minLength, startValue, reuseAvailableValues);
      }
    } else {
      value = field.getValue(language);
    }
    
    Input input = new Input();
    input.setID(fieldName);
    input.setName(fieldName);
    input.setValue(EncodeHelper.javaStringToHtmlString(value));
    input.setType(Input.text);
    input.setSize(value.length() + 2);
    input.setReadOnly(true);
    
    out.println(input.toString());
  }
  
  /**
   * Since the field is always in readonly state, no javascripts are needed to control the value
   * given to the field.
   */
  public void displayScripts(PrintWriter out, FieldTemplate template, PagesContext pagesContext)
  throws IOException {
    if (!template.getTypeName().equals(SequenceField.TYPE)) {
      SilverTrace.info("form", "SequenceFieldDisplayer.displayScripts",
        "form.INFO_NOT_CORRECT_TYPE", SequenceField.TYPE);
    }
  }
  
  /**
   * Returns the number of returned html objects. 
   */
  public int getNbHtmlObjectsDisplayed(FieldTemplate template, PagesContext pagesContext) {
    return 1;
  }

  /**
   * Returns true if the field can be set as mandatory.
   */
  public boolean isDisplayedMandatory() {
    return false;
  }
  
  /**
   * Updates the value of the field. The field's name is used to retrieve the HTTP parameter from
   * the request.
   */
  public List<String> update(String value, Field field, FieldTemplate template,
    PagesContext pagesContext)
  throws FormException {
    if (!field.getTypeName().equals(SequenceField.TYPE)) {
      throw new FormException("SequenceFieldDisplayer.update", "form.EX_NOT_CORRECT_TYPE",
        SequenceField.TYPE);
    }
    if (field.acceptValue(value, pagesContext.getLanguage())) {
      field.setValue(value, pagesContext.getLanguage());
    } else {
      throw new FormException("SequenceFieldDisplayer.update", "form.EX_NOT_CORRECT_VALUE",
        SequenceField.TYPE);
    }
    return new ArrayList<String>();
  }
  
}
