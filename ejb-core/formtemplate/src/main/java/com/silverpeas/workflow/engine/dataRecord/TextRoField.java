package com.silverpeas.workflow.engine.dataRecord;

import com.silverpeas.form.fieldType.TextField;

/**
 * A read only TextField
 */
public class TextRoField extends TextField {
  private final String value;

  public TextRoField(String value) {
    this.value = value;
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
   * Returns true.
   */
  public boolean isReadOnly() {
    return true;
  }
}
