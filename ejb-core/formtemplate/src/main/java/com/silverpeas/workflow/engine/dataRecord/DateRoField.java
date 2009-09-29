package com.silverpeas.workflow.engine.dataRecord;

import java.text.SimpleDateFormat;
import java.util.Date;

import com.silverpeas.form.fieldType.DateField;

/**
 * A read only DateField
 */
public class DateRoField extends DateField {
  private final String value;

  public DateRoField(Date value) {
    if (value != null)
      this.value = formatterBD.format(value);
    else
      this.value = null;
  }

  /**
   * Returns the string value of this field.
   */
  public String getStringValue() {
    return value;
  }

  /**
   * Changes nothing.
   */
  public void setStringValue(String value) {
  }

  /**
   * Returns true even if a set will changes nothing.
   */
  public boolean acceptStringValue(String value) {
    return true;
  }

  /**
   * Returns true.
   */
  public boolean isReadOnly() {
    return true;
  }

  private static final SimpleDateFormat formatterBD = new SimpleDateFormat(
      "yyyy/MM/dd");
}
