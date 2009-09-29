package com.silverpeas.workflow.api.model;

import java.util.Iterator;

import com.silverpeas.form.DataRecord;
import com.silverpeas.form.FormException;
import com.silverpeas.form.RecordTemplate;
import com.silverpeas.workflow.api.WorkflowException;

/**
 * Interface describing a representation of the &lt;Form&gt; element of a
 * Process Model.
 */
public interface Form {
  /**
   * Get the name of this form
   * 
   * @return form's name
   */
  public String getName();

  /**
   * Set the name of this form
   * 
   * @param name
   *          form's name
   */
  public void setName(String name);

  /**
   * Get the role
   * 
   * @return the role name
   */
  public String getRole();

  /**
   * Set the role name
   * 
   * @param role
   *          name to set
   */
  public void setRole(String role);

  /**
   * Get the name of HTML file to show this form if no HTML file is defined,
   * XMLForm will be used to display the form
   * 
   * @return form's name
   */
  public String getHTMLFileName();

  /**
   * Set the name of HTML file to show this form if no HTML file is defined,
   * XMLForm will be used to display the form
   * 
   * @return form's name
   */
  public void setHTMLFileName(String HTMLFileName);

  /**
   * Get the inputs
   * 
   * @return the inputs as a Vector
   */
  public Input[] getInputs();

  /**
   * Get the input specified by the index
   * 
   * @param idx
   *          the index
   * @return the items as a Vector
   */
  public Input getInput(int idx);

  /**
   * Get the input specified by item and / or value
   * 
   * @param reference
   *          the reference object
   * @return the items as a Vector
   */
  public Input getInput(Input reference);

  /**
   * Create and return and object implementing Input
   */
  public Input createInput();

  /**
   * Iterate through the inputs
   * 
   * @return an iterator
   */
  public Iterator iterateInput();

  /**
   * Add an input Method needed primarily by Castor
   */
  public void addInput(Input input);

  /**
   * Remove the input specified by the index
   * 
   * @param idx
   *          the index
   */
  public void removeInput(int idx) throws WorkflowException;

  /**
   * Get all the titles
   * 
   * @return an object containing the collection of the tables
   */
  public ContextualDesignations getTitles();

  /**
   * Get title in specific language for the given role
   * 
   * @param lang
   *          title's language
   * @param role
   *          role for which the title is
   * @return wanted title as a String object. If title is not found, search
   *         title with given role and default language, if not found again,
   *         return the default title in given language, if not found again,
   *         return the default title in default language, if not found again,
   *         return empty string.
   */
  public String getTitle(String role, String language);

  /**
   * Iterate through the titles
   * 
   * @return an iterator
   */
  public Iterator iterateTitle();

  /**
   * Add a title Method needed primarily by Castor
   */
  public void addTitle(ContextualDesignation description);

  /**
   * Create an object implementing ContextualDesignation Method needed primarily
   * by Castor
   */
  public ContextualDesignation createDesignation();

  /**
   * Converts this object in a DataRecord object
   * 
   * @return the resulting DataRecord object with the default values set
   */
  public DataRecord getDefaultRecord(String role, String lang, DataRecord data)
      throws FormException, WorkflowException;

  /**
   * Converts this object in a RecordTemplate object
   * 
   * @return the resulting RecordTemplate
   */
  public RecordTemplate toRecordTemplate(String roleName, String language)
      throws WorkflowException;

  /**
   * Converts this object in a RecordTemplate object
   * 
   * @return the resulting RecordTemplate
   */
  public RecordTemplate toRecordTemplate(String roleName, String language,
      boolean readOnly) throws WorkflowException;
}
