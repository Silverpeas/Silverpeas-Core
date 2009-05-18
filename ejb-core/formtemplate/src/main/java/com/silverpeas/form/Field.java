package com.silverpeas.form;

import java.io.Serializable;

/**
 * A Field is an item of a DataRecord.
 *
 * The fields of a record may have different types,
 * but they are all managed via this interface.
 *
 * To be displayed in a jsp page a field must be handled by
 * a specific FieldDisplayer which is aware of the internal data type
 * and format of the field.
 *
 * The links between Fields and FieldDisplayers are managed by a RecordTemplate.
 * 
 * @see DataRecord
 * @see FieldDisplayer
 * @see RecordTemplate
 */
public interface Field extends Serializable, Comparable
{
  /**
   * Returns the type name of this field.
   */
  public String getTypeName();

  /**
   * Returns the normalized value of this field.
   */
  public String getValue();

  /**
   * Set this field value from a normalized string value.
   *
   * @throw FormException when the field is readOnly.
   * @throw FormException when the string value is ill formed.
   */
  public void setValue(String value) throws FormException;

  /**
   * Returns true if the value isn't ill formed and this field isn't read only.
   */
  public boolean acceptValue(String value);

  /**
   * Returns the local string value of this field.
   */
  public String getValue(String lang);

  /**
   * Set this field value from a local string value.
   *
   * @throw FormException when the field is readOnly.
   * @throw FormException when the string value is ill formed.
   */
  public void setValue(String value, String lang) throws FormException;

  /**
   * Returns true if the local value isn't ill formed
	* and this field isn't read only.
   */
  public boolean acceptValue(String value, String lang);

  /**
   * Returns the normalized String value.
   */
  public String getStringValue();

  /**
   * Set this field value from a  normalized String value.
   *
   * @throw FormException when the field is readOnly.
   * @throw FormException when the value is not a normalized.
   */
  public void setStringValue(String value) throws FormException;

  /**
   * Returns true if the value isn't normalized
   * and this field isn't read only.
   */
  public boolean acceptStringValue(String value);

  /**
   * Returns the value of this field.
   */
  public Object getObjectValue();

  /**
   * Set this field value.
   *
   * @throw FormException when the field is readOnly.
   * @throw FormException when the value has a wrong type.
   */
  public void setObjectValue(Object value) throws FormException;

  /**
   * Returns true if the value hasn't a wrong type
   * and this field isn't read only.
   */
  public boolean acceptObjectValue(Object value);

  /**
   * Returns true if this field is not set.
   */
  public boolean isNull();

  /**
   * Set to null this field.
   *
   * @throw FormException when the field is mandatory.
   * @throw FormException when the field is read only.
   */
  public void setNull() throws FormException;
}
