package com.silverpeas.workflow.api.instance;

/**
 * A Participant object represents a 3-tuple user/roleName/state
 */
public interface Participant extends Actor {

  /**
   * Get the action the participant has done
   * 
   * @return the action's name
   */
  public String getAction();
}