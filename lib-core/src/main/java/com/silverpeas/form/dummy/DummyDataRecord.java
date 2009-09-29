package com.silverpeas.form.dummy;

import com.silverpeas.form.*;
import com.silverpeas.form.fieldType.*;

/**
 * A dummy DataRecord .
 */
public class DummyDataRecord implements DataRecord {
  private Field field;

  public DummyDataRecord() {
    field = new TextFieldImpl();
  }

  /**
   * Returns the data record id.
   */
  public String getId() {
    return "id";
  }

  /**
   * Gives an id to the data record.
   */
  public void setId(String externalId) {
  }

  /**
   * Return true if this record has not been inserted in a RecordSet.
   */
  public boolean isNew() {
    return true;
  }

  /**
   * Returns the named field.
   */
  public Field getField(String fieldName) {
    return field;
  }

  /**
   * Returns the field at the index position in the record.
   */
  public Field getField(int fieldIndex) {
    return field;
  }

  public String[] getFieldNames() {
    return new String[0];
  }

  public String getLanguage() {
    return null;
  }

  public void setLanguage(String language) {
  }

}
