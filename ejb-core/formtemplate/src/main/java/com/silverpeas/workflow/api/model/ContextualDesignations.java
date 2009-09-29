package com.silverpeas.workflow.api.model;

import java.util.Iterator;

import com.silverpeas.workflow.api.WorkflowException;

/**
 * Interface describing an object holding a collection of
 * <code>ContextualDesignation</code> objects. Does not correspond to any
 * Process Model schema elements.
 */
public interface ContextualDesignations {

  /**
   * Iterate through the ContextualDesignation objects
   * 
   * @return an iterator
   */
  public Iterator iterateContextualDesignation();

  /**
   * Create a ContextualDesignation
   * 
   * @return an object implementing ContextualDesignation
   */
  public ContextualDesignation createContextualDesignation();

  /**
   * Add a contextualDesignation to the collection
   * 
   * @param contextualDesignation
   *          to be added
   */
  public void addContextualDesignation(
      ContextualDesignation contextualDesignation);

  /**
   * Remove a matching contextualDesignation from the collection. The collection
   * shall be searched for a Designation with the same language and role.
   * 
   * @param contextualDesignation
   *          a model of the contextualDesignation to be removed.
   * @throws WorkflowException
   *           when a matching contextualDescription could not be found.
   */
  public void removeContextualDesignation(
      ContextualDesignation contextualDesignation) throws WorkflowException;

  /**
   * Get the designation for the given role and language; make an exact match,
   * do not fall-back to the default values.
   * 
   * @param role
   *          the name of the role
   * @param language
   *          the code of the language
   * @return an object implementing ContextualDesignation or <code>null</code>
   */
  public ContextualDesignation getSpecificLabel(String role, String language);

  /**
   * Get the designation for the given role and language; make the best match if
   * the required language and/ or role are not supported fall-back to the
   * default values to the default values.
   * 
   * @param role
   *          the name of the role
   * @param language
   *          the code of the language
   * @return the label or an empty string if nothing found.
   */
  public String getLabel(String role, String language);
}
