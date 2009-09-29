package com.silverpeas.workflow.engine.dataRecord;

import java.util.HashMap;
import java.util.Map;

import com.silverpeas.form.Field;
import com.silverpeas.form.FieldTemplate;
import com.silverpeas.form.FormException;
import com.silverpeas.workflow.api.instance.ProcessInstance;

/**
 * A ProcessInstanceFieldTemplate describes a field of a process instance.
 */
public abstract class ProcessInstanceFieldTemplate implements FieldTemplate {
  public ProcessInstanceFieldTemplate(String fieldName, String typeName,
      String displayerName, String label) {
    this.fieldName = fieldName;
    this.typeName = typeName;
    this.displayerName = displayerName;
    this.label = label;
  }

  /**
   * Returns the field name of the Field built on this template.
   */
  public String getFieldName() {
    return fieldName;
  }

  /**
   * Returns the type name of the described field.
   */
  public String getTypeName() {
    return typeName;
  }

  /**
   * Returns the name of the FieldDisplayer to display the described field.
   */
  public String getDisplayerName() {
    return displayerName;
  }

  /**
   * Returns the default label of the described field.
   */
  public String getLabel() {
    return label;
  }

  /**
   * Returns the local label of the described field.
   */
  public String getLabel(String language) {
    return label;
  }

  /**
   * Returns an empty array : this implementation use only a default local.
   */
  public String[] getLanguages() {
    return new String[0];
  }

  /**
   * Returns false since a process instance field is read only.
   */
  public boolean isMandatory() {
    return false;
  }

  /**
   * Returns true since a process instance field is read only.
   */
  public boolean isReadOnly() {
    return true;
  }

  /**
   * Returns false.
   */
  public boolean isDisabled() {
    return false;
  }

  /**
   * Returns false.
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
   * Throws an illegal call exception, since an empty field can't be built from
   * this template.
   * 
   * @see getField
   */
  public Field getEmptyField() throws FormException {
    throw new FormException("workflowEngine", "workflowEngine.EXP_ILLEGAL_CALL");
  }

  public boolean isSearchable() {
    return false;
  }

  public String getTemplateName() {
    return "unknown";
  }

  /**
   * Returns a field built from this template and filled from the given process
   * instance.
   */
  abstract public Field getField(ProcessInstance instance) throws FormException;

  /**
   * The field name.
   */
  private final String fieldName;

  /**
   * The field type name.
   */
  private final String typeName;

  /**
   * The final displayer name.
   */
  private final String displayerName;

  /**
   * The label
   */
  private final String label;

}
