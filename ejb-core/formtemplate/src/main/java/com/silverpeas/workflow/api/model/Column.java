package com.silverpeas.workflow.api.model;

/**
 * Interface describing a representation of the &lt;column&gt; element of a
 * Process Model.
 **/
public interface Column {
  /**
   * Get the item to show in this column
   * 
   * @return the item
   */
  public Item getItem();

  /**
   * Set the item to show in this column
   * 
   * @return the item
   */
  public void setItem(Item item);
}