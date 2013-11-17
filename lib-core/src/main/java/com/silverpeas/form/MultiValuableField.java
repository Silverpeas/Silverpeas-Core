/**
 * Copyright (C) 2000 - 2013 Silverpeas
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

package com.silverpeas.form;

import java.io.Serializable;
import java.util.List;

/**
 * A Field is an item of a DataRecord. The fields of a record may have different types, but they are
 * all managed via this interface. To be displayed in a jsp page a field must be handled by a
 * specific FieldDisplayer which is aware of the internal data type and format of the field. The
 * links between Fields and FieldDisplayers are managed by a RecordTemplate.
 * @see DataRecord
 * @see FieldDisplayer
 * @see RecordTemplate
 */
public interface MultiValuableField extends Field, Serializable, Comparable {

  public final static String TYPE_FILE = "file";
  public final static String FILE_PARAM_NAME_SUFFIX = "$$id";
  public final static String JOIN_SEPARATOR = "|";

  /**
   * Returns the type name of this field.
   */
  public String getTypeName();

  /**
   * Returns the normalized value of this field.
   */
  public List<String> getValues();

  /**
   * Set this field value from a normalized string value.
   * @throw FormException when the field is readOnly.
   * @throw FormException when the string value is ill formed.
   */
  public void setValues(List<String> values) throws FormException;

  /**
   * Returns true if the value isn't ill formed and this field isn't read only.
   */
  public boolean acceptValues(List<String> values);

  /**
   * Returns the local string value of this field.
   */
  public List<String> getValues(String lang);

  /**
   * Set this field value from a local string value.
   * @throw FormException when the field is readOnly.
   * @throw FormException when the string value is ill formed.
   */
  public void setValues(List<String> values, String lang) throws FormException;

  /**
   * Returns true if the local value isn't ill formed and this field isn't read only.
   */
  public boolean acceptValues(List<String> values, String lang);

  /**
   * Returns the normalized String value.
   */
  public List<String> getStringValues();

  /**
   * Set this field value from a normalized String value.
   * @throw FormException when the field is readOnly.
   * @throw FormException when the value is not a normalized.
   */
  public void setStringValues(List<String> values) throws FormException;

  /**
   * Returns true if the value isn't normalized and this field isn't read only.
   */
  public boolean acceptStringValues(List<String> values);

  /**
   * Returns the value of this field.
   */
  public List getObjectValues();

  /**
   * Set this field value.
   * @throw FormException when the field is readOnly.
   * @throw FormException when the value has a wrong type.
   */
  public void setObjectValues(List<Object> values) throws FormException;

  /**
   * Returns true if the value hasn't a wrong type and this field isn't read only.
   */
  public boolean acceptObjectValues(List<Object> values);

  /**
   * Returns true if this field is not set.
   */
  public boolean isNull();

  /**
   * Set to null this field.
   * @throw FormException when the field is mandatory.
   * @throw FormException when the field is read only.
   */
  public void setNull() throws FormException;
  
  public void addValue(String value);
  
  public void addValue(int index, String value);
}
