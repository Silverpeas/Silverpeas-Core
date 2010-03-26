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
 * FLOSS exception.  You should have recieved a copy of the text describing
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
package com.silverpeas.form.dummy;

import java.util.HashMap;
import java.util.Map;

import com.silverpeas.form.Field;
import com.silverpeas.form.FieldTemplate;
import com.silverpeas.form.fieldType.TextFieldImpl;

/**
 * A dummy FieldTemplate.
 */
public class DummyFieldTemplate implements FieldTemplate {

  private Field field;

  public DummyFieldTemplate() {
    field = new TextFieldImpl();
  }

  /**
   * Returns the field name of the Field built on this template.
   */
  public String getFieldName() {
    return "field-name";
  }

  /**
   * Returns the type name of the described field.
   */
  public String getTypeName() {
    return "text";
  }

  /**
   * Returns the name of the FieldDisplayer to display the described field.
   */
  public String getDisplayerName() {
    return "text";
  }

  /**
   * Returns the label of the described field (in the default locale).
   */
  public String getLabel() {
    return "";
  }

  /**
   * Returns the local label of the described field.
   */
  public String getLabel(String lang) {
    return "";
  }

  /**
   * Returns the locals
   */
  public String[] getLanguages() {
    return new String[0];
  }

  /**
   * Returns true when the described field must have a value.
   */
  public boolean isMandatory() {
    return false;
  }

  /**
   * Returns true when the described field can't be updated.
   */
  public boolean isReadOnly() {
    return false;
  }

  /**
   * Returns true when the described field must be disabled.
   */
  public boolean isDisabled() {
    return false;
  }

  /**
   * Returns true when the described field must be hidden.
   */
  public boolean isHidden() {
    return false;
  }

  /**
   * Returns a Map (String -> String) of named parameters which can be used by the displayer
   * (max-size, length ...).
   */
  public Map<String, String> getParameters(String language) {
    return new HashMap<String, String>();
  }

  /**
   * Returns an empty Field built on this template.
   */
  public Field getEmptyField() {
    return field;
  }

  public boolean isSearchable() {
    return false;
  }

  public String getTemplateName() {
    return "dummy";
  }
}