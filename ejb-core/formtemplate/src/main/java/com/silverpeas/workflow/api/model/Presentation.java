package com.silverpeas.workflow.api.model;

import java.util.Iterator;

import com.silverpeas.workflow.api.WorkflowException;

/**
 * Interface describing a representation of the &lt;presentation&gt; element of
 * a Process Model.
 */
public interface Presentation {
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
   * Get all the titles
   * 
   * @return an object containing the collection of the titles
   */
  public ContextualDesignations getTitles();

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
   * Get the contents of the Columns object with the given role name, or of the
   * 'Columns' for the default role if nothing for the specified role can be
   * found.
   * 
   * @param the
   *          name of the role
   * @return the contents of 'Columns' as an array of 'Column'
   */
  public Column[] getColumns(String roleName);

  /**
   * Get the Columns object referenced by the given role name
   * 
   * @param strRoleName
   *          the name of the Columns object
   * @return a Columns implementation
   */
  public Columns getColumnsByRole(String strRoleName);

  /**
   * Iterate through the Column objects
   * 
   * @return an iterator
   */
  public Iterator iterateColumns();

  /**
   * Add an column to the collection
   * 
   * @param column
   *          to be added
   */
  public void addColumns(Columns columns);

  /**
   * Create an Column
   * 
   * @return an object implementing Column
   */
  public Columns createColumns();

  /**
   * Delete the Columns object with the given name
   * 
   * @param strRoleName
   *          the name of the Columns object (a role name)
   * @throws WorkflowException
   *           when the Columns for the given name have not been found.
   */
  public void deleteColumns(String strRoleName) throws WorkflowException;
}