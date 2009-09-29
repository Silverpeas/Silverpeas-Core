package com.silverpeas.workflow.api.model;

import java.util.Hashtable;
import java.util.Iterator;

import com.silverpeas.workflow.api.WorkflowException;

/**
 * Interface describing a representation of the &lt;item&gt; element of a
 * Process Model.
 */
public interface Item {
  /**
   * Get the name of this item
   * 
   * @return item's name
   */
  public String getName();

  /**
   * Set the name of this item
   * 
   * @param item
   *          's name
   */
  public void setName(String name);

  /**
   * Get description in specific language for the given role
   * 
   * @param lang
   *          description's language
   * @param role
   *          role for which the description is
   * @return wanted description as a String object. If description is not found,
   *         search description with given role and default language, if not
   *         found again, return the default description in given language, if
   *         not found again, return the default description in default
   *         language, if not found again, return empty string.
   */
  public String getDescription(String role, String language);

  /**
   * Get all the descriptions
   * 
   * @return an object containing the collection of the descriptions
   */
  public ContextualDesignations getDescriptions();

  /**
   * Iterate through the descriptions
   * 
   * @return an iterator
   */
  public Iterator iterateDescription();

  /**
   * Add a description Method needed primarily by Castor
   */
  public void addDescription(ContextualDesignation description);

  /**
   * Get label in specific language for the given role
   * 
   * @param lang
   *          label's language
   * @param role
   *          role for which the label is
   * @return wanted label as a String object. If label is not found, search
   *         label with given role and default language, if not found again,
   *         return the default label in given language, if not found again,
   *         return the default label in default language, if not found again,
   *         return empty string.
   */
  public String getLabel(String role, String language);

  /**
   * Get all the labels
   * 
   * @return an object containing the collection of the labels
   */
  public ContextualDesignations getLabels();

  /**
   * Iterate through the Labels
   * 
   * @return an iterator
   */
  public Iterator iterateLabel();

  /**
   * Add a label Method needed primarily by Castor
   */
  public void addLabel(ContextualDesignation label);

  /**
   * Create an object implementing ContextualDesignation Method needed primarily
   * by Castor
   */
  public ContextualDesignation createDesignation();

  /**
   * Get value of computed attribute
   * 
   * @return true if item must be computed
   */
  public boolean isComputed();

  /**
   * Set value of computed attribute
   * 
   * @param true if item must be computed
   */
  public void setComputed(boolean computed);

  /**
   * Get formula to use if item must be computed
   * 
   * @return formula of type 'action.Validation.actor'
   */
  public String getFormula();

  /**
   * Set formula to use if item must be computed
   * 
   * @param formula
   *          formula of type 'action.Validation.actor'
   */
  public void setFormula(String formula);

  /**
   * Get value of readOnly attribute
   * 
   * @return true if item must be readonly
   */
  public boolean isReadonly();

  /**
   * Set value of readOnly attribute
   * 
   * @param true if item must be readonly
   */
  public void setReadonly(boolean readonly);

  /**
   * Get the type of this item
   * 
   * @return item's type (text for text field)
   */
  public String getType();

  /**
   * Set the type of this item
   * 
   * @param item
   *          's type (text for text field)
   */
  public void setType(String type);

  /**
   * Get the full user field name, to which this item is map
   * 
   * @return full user field name
   */
  public String getMapTo();

  /**
   * Set the full user field name, to which this item is map
   * 
   * @param mapTo
   *          full user field name
   */
  public void setMapTo(String mapTo);

  /**
   * Get the parameter specified by name
   * 
   * @param strName
   *          the parameter name
   * @return the parameters
   */
  public Parameter getParameter(String strName);

  /**
   * Create an object implementing Parameter
   */
  public Parameter createParameter();

  /**
   * Add a Parameter to the collection
   */
  public void addParameter(Parameter parameter);

  /**
   * Return an Iterator over the parameters collection
   */
  public Iterator iterateParameter();

  /**
   * Remove the parameter specified by its name
   * 
   * @param name
   *          the name of the parameter
   * @throws WorkflowException
   *           when the parameter cannot be found
   */
  public void removeParameter(String strName) throws WorkflowException;

  public Hashtable getKeyValuePairs();
}