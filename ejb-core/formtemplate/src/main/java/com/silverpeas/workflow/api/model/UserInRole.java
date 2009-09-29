package com.silverpeas.workflow.api.model;

/**
 * Interface describing a representation of the &lt;userInRole&gt; element of a
 * Process Model.
 */
public interface UserInRole {
  /**
   * Get name of the role
   */
  public String getRoleName();

  /**
   * Set name of the role
   * 
   * @param roleName
   */
  public void setRoleName(String roleName);
}