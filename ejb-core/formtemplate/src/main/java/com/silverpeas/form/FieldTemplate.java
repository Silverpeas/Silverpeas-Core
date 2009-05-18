package com.silverpeas.form;

import java.util.Map;

/**
 * A FieldTemplate describes a specific field of a DataRecord.
 * 
 * A FieldTemplate gives the field name, type information
 * and display information.
 *
 * @see DataRecord
 * @see RecordTemplate
 */
public interface FieldTemplate
{
  /**
   * Returns the field name of the Field built on this template.
   */
  public String getFieldName();

  /**
   * Returns the type name of the described field.
   */
  public String getTypeName();

  /**
   * Returns the name of the FieldDisplayer to display the described field.
   */
  public String getDisplayerName();

  /**
   * Returns the label of the described field (in the default locale).
   */
  public String getLabel();

  /**
   * Returns the local label of the described field.
   */
  public String getLabel(String lang);

  /**
   * Returns the locals
   */
  public String[] getLanguages();

  /**
   * Returns true when the described field must have a value.
   */
  public boolean isMandatory();

  /**
   * Returns true when the described field can't be updated.
   */
  public boolean isReadOnly();

  /**
   * Returns true when the described field must be disabled.
   */
  public boolean isDisabled();

  /**
   * Returns true when the described field must be hidden.
   */
  public boolean isHidden();

  /**
   * Returns a Map (String -> String) of named parameters
   * which can be used by the displayer (max-size, length ...).
   */
  //public Map getParameters();
  
  /**
   * Returns a Map (String -> String) of named parameters
   * which can be used by the displayer (max-size, length ...).
   */
  public Map getParameters(String language);

  /**
   * Returns an empty Field built on this template.
   */
  public Field getEmptyField() throws FormException;
  
  public boolean isSearchable();
  
  public String getTemplateName();
}
