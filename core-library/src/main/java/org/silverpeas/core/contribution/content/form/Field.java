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
package org.silverpeas.core.contribution.content.form;

import java.io.Serializable;

/**
 * A Field is an item of a DataRecord. The fields of a record may have different types, but they are
 * all managed via this interface. To be displayed in a web page a field must be handled by a
 * specific {@link FieldDisplayer} which is aware of the internal data type and format of the field.
 * The links between Fields and FieldDisplayers are managed by a {@link RecordTemplate}.
 * @see DataRecord
 * @see FieldDisplayer
 * @see RecordTemplate
 */
public interface Field extends Serializable, Comparable<Field> {

  String TYPE_FILE = "file";
  String FILE_PARAM_NAME_SUFFIX = "$$id";

  /**
   * Gets the type name of this field.
   * @return the name of this field type.
   */
  String getTypeName();

  /**
   * Gets the normalized value of this field.
   * @return the value of this field.
   */
  String getValue();

  /**
   * Sets the specified normalized value.
   * @param value the normalized value to set.
   * @throws FormException when the field is readOnly or when the value format is wrong.
   */
  void setValue(String value) throws FormException;

  /**
   * Is this field is able to accept the specified value?
   * @return true if the value format is correct and this field isn't read only. False otherwise.
   */
  boolean acceptValue(String value);

  /**
   * Gets the textual value of this field in the specified language.
   * @param lang the ISO-631 code of a supported language.
   * @return the value in the specified language.
   */
  String getValue(String lang);

  /**
   * Sets the specified textual value in the given language.
   * @param value a textual value.
   * @param lang the ISO-631 code of a supported language.
   * @throws FormException when the field is readOnly or if the value isn't a text.
   */
  void setValue(String value, String lang) throws FormException;

  /**
   * Is this field is able to accept the specified value in the given language?
   * @param value a textual value.
   * @param lang the ISO-631 code of a supported language.
   * @return true if the local value isn't ill formed and this field isn't read only.
   */
  boolean acceptValue(String value, String lang);

  /**
   * Gets the normalized {@link String} value of this field.
   * @return the {@link String} representation of the value of this field.
   */
  String getStringValue();

  /**
   * Sets the specified {@link String} normalized value.
   * @param value the {@link String} value to set
   * @throws FormException when the field is readOnly or FormException when the value is not a
   * normalized.
   */
  void setStringValue(String value) throws FormException;

  /**
   * Is this field able to accept the specified {@link String} value?
   * @param value a {@link String} value.
   * @return true if the value isn't normalized and this field isn't read only.
   */
  boolean acceptStringValue(String value);

  /**
   * Gets the value of this field.
   * @return an object representing the value of this field.
   */
  Object getObjectValue();

  /**
   * Sets the specified value.
   * @param value an {@link Object} representing the value to set.
   * @throws FormException when the field is readOnly or when the value has a wrong type.
   */
  void setObjectValue(Object value) throws FormException;

  /**
   * Is this field able to accept the specified value?
   * @param value a value
   * @return true if the value hasn't a wrong type and this field isn't read only.
   */
  @SuppressWarnings("unused")
  boolean acceptObjectValue(Object value);

  /**
   * Is this field valued?
   * @return true if this field is not set. False otherwise.
   */
  boolean isNull();

  /**
   * Sets to null this field.
   * @throws FormException when the field is mandatory or when the field is read only.
   */
  void setNull() throws FormException;

  /**
   * Gets the occurrence position of this field in the case there is several identical fields in
   * a {@link DataRecord}. A field is identified by its name.
   * @return the occurrence position of this field.
   */
  int getOccurrence();

  /**
   * Sets the specified occurrence position of this field in a {@link DataRecord} when there is
   * several similar fields. A field is identified by its name.
   * @param i the occurrence position.
   */
  void setOccurrence(int i);

  /**
   * Sets the name of this field. Its name is its identifier in a {@link DataRecord} or in a
   * {@link RecordTemplate}.
   * @param name the name of the field.
   */
  void setName(String name);

  /**
   * Gets the name of this field.
   * @return the field name.
   */
  String getName();

  @Override
  int compareTo(Field field);
}
