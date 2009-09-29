package com.silverpeas.workflow.api.model;

import java.util.Iterator;
import java.util.List;

/**
 * Interface describing a representation of the &lt;columns&gt; element of a
 * Process Model.
 */
public interface Columns {

  /**
   * Get the referenced Column objects as a list
   */
  public List getColumnList();

  /**
   * Get the role for which the list of items must be returned
   * 
   * @return role name
   */
  public String getRoleName();

  /**
   * Set the role for which the list of items must be returned
   * 
   * @param roleName
   *          role name
   */
  public void setRoleName(String roleName);

  /**
   * Get the column referencing the given item
   * 
   * @param strItemName
   *          the name of the item
   * @return a Column object
   */
  public Column getColumn(String strItemName);

  /**
   * Iterate through the Column objects
   * 
   * @return an iterator
   */
  public Iterator iterateColumn();

  /**
   * Add an column to the collection
   * 
   * @param column
   *          to be added
   */
  public void addColumn(Column column);

  /**
   * Create an Column
   * 
   * @return an object implementing Column
   */
  public Column createColumn();

  /**
   * Remove all column objects from the collection
   */
  public void removeAllColumns();
}