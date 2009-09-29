package com.silverpeas.workflow.api.model;

/**
 * User: hani Date: May 28, 2003 Time: 12:44:54 AM
 */
public interface AbstractDescriptor {
  // ~ Instance fields ////////////////////////////////////////////////////////

  /*
   * private AbstractDescriptor parent; private boolean hasId = false; private
   * int id;
   */

  // ~ Methods ////////////////////////////////////////////////////////////////

  /*
   * public void setId(int id) { this.id = id; hasId = true; }
   * 
   * public int getId() { return id; }
   * 
   * public void setParent(AbstractDescriptor parent) { this.parent = parent; }
   * 
   * public AbstractDescriptor getParent() { return parent; }
   * 
   * public boolean hasId() { return hasId; }
   */

  public void setId(int id);

  public int getId();

  public void setParent(AbstractDescriptor parent);

  public AbstractDescriptor getParent();

  public boolean hasId();
}