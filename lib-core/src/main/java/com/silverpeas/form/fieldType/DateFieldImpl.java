package com.silverpeas.form.fieldType;

/**
 * A TextFieldImpl stores use a String attribute to store its value.
 */
public class DateFieldImpl extends DateField
{
  private String value = "";
   
  public DateFieldImpl() {}

  /**
   * Returns the string value of this field.
   */
  public String getStringValue()
  {
     return value;
  }

  /**
   * Set the string value of this field.
   */
  public void setStringValue(String value)
  {
     this.value = value;
  }

  /**
   * 
   */
  public boolean acceptStringValue(String value)
  {
     return ! isReadOnly();
  }

  /**
   * Returns true if the value is read only.
   */
  public boolean isReadOnly()
  {
     return false;
  }
}
