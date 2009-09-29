package com.silverpeas.form.dummy;

import java.util.*;

import com.silverpeas.form.*;
import com.silverpeas.form.fieldType.*;

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
   * Returns a Map (String -> String) of named parameters which can be used by
   * the displayer (max-size, length ...).
   */
  public Map getParameters(String language) {
    return new HashMap();
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