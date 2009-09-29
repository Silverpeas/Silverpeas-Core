package com.silverpeas.workflow.engine.model;

import java.io.Serializable;

import com.silverpeas.workflow.api.model.Action;
import com.silverpeas.workflow.api.model.AllowedAction;
import com.silverpeas.workflow.engine.AbstractReferrableObject;

/**
 * Class implementing the representation of the &lt;allow&gt; element of a
 * Process Model.
 **/
public class ActionRef extends AbstractReferrableObject implements
    Serializable, AllowedAction {
  private Action action; // The reference to the allowed action

  /**
   * Get the referred action
   */
  public Action getAction() {
    return action;
  }

  /**
   * Set the referred action
   * 
   * @param action
   *          action to refer
   */
  public void setAction(Action action) {
    this.action = action;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.silverpeas.workflow.engine.AbstractReferrableObject#getKey()
   */
  public String getKey() {
    if (getAction() != null)
      return getAction().getName();
    else
      return "";
  }
}