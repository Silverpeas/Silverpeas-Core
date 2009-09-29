package com.silverpeas.workflow.engine.model;

import java.io.Serializable;

import com.silverpeas.workflow.api.model.UserInRole;
import com.silverpeas.workflow.engine.AbstractReferrableObject;

/**
 * Class implementing the representation of the &lt;userInRole&gt; element of a
 * Process Model.
 **/
public class UserInRoleImpl extends AbstractReferrableObject implements
    UserInRole, Serializable {
  private String roleName;

  /**
   * Constructor
   */
  public UserInRoleImpl() {
    super();
  }

  /**
   * Get name of the role
   */
  public String getRoleName() {
    return this.roleName;
  }

  /**
   * Set name of the role
   * 
   * @param roleName
   */
  public void setRoleName(String roleName) {
    this.roleName = roleName;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.silverpeas.workflow.engine.AbstractReferrableObject#getKey()
   */
  public String getKey() {
    if (roleName != null)
      return roleName;
    else
      return "";
  }
}