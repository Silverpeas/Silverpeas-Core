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

package org.silverpeas.core.contribution.content.form.dummy;

import org.silverpeas.core.contribution.content.form.Field;
import org.silverpeas.core.contribution.content.form.FieldTemplate;
import org.silverpeas.core.contribution.content.form.FormException;
import org.silverpeas.core.contribution.content.form.field.TextFieldImpl;
import org.silverpeas.core.contribution.content.form.record.Parameter;
import org.silverpeas.core.util.ArrayUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A dummy FieldTemplate.
 */
public class DummyFieldTemplate implements FieldTemplate {

  private Field field;

  public DummyFieldTemplate() {
    field = new TextFieldImpl();
  }

  public DummyFieldTemplate(Field field) {
    this.field = field;
  }

  /**
   * Returns the field name of the Field built on this template.
   */
  @Override
  public String getFieldName() {
    return "field-name";
  }

  /**
   * Returns the type name of the described field.
   */
  @Override
  public String getTypeName() {
    return "text";
  }

  /**
   * Returns the name of the FieldDisplayer to display the described field.
   */
  @Override
  public String getDisplayerName() {
    return "text";
  }

  /**
   * Returns the label of the described field (in the default locale).
   */
  @Override
  public String getLabel() {
    return "";
  }

  /**
   * Returns the local label of the described field.
   */
  @Override
  public String getLabel(String lang) {
    return "";
  }

  /**
   * Returns the locals
   */
  @Override
  public String[] getLanguages() {
    return ArrayUtil.EMPTY_STRING_ARRAY;
  }

  /**
   * Returns true when the described field must have a value.
   */
  @Override
  public boolean isMandatory() {
    return false;
  }

  /**
   * Returns true when the described field can't be updated.
   */
  @Override
  public boolean isReadOnly() {
    return false;
  }

  /**
   * Returns true when the described field must be disabled.
   */
  @Override
  public boolean isDisabled() {
    return false;
  }

  /**
   * Returns true when the described field must be hidden.
   */
  @Override
  public boolean isHidden() {
    return false;
  }

  /**
   * Returns a Map (String -> String) of named parameters which can be used by the displayer
   * (max-size, length ...).
   */
  @Override
  public Map<String, String> getParameters(String language) {
    return new HashMap<>();
  }

  @Override
  public List<Parameter> getParametersObj() {
    return new ArrayList<>();
  }

  /**
   * Returns an empty Field built on this template.
   */
  @Override
  public Field getEmptyField() {
    return field;
  }

  @Override
  public boolean isSearchable() {
    return false;
  }

  @Override
  public String getTemplateName() {
    return "dummy";
  }

  public boolean isUsedAsFacet() {
    return false;
  }

  @Override
  public int getMaximumNumberOfOccurrences() {
    return 1;
  }

  @Override
  public boolean isRepeatable() {
    return false;
  }

  @Override
  public Field getEmptyField(int occurrence) throws FormException {
    return field;
  }

}