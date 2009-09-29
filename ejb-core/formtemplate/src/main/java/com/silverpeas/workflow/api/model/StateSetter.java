package com.silverpeas.workflow.api.model;

/**
 * Interface describing a representation of one of the following elements of a
 * Process Model:
 * <ul>
 * <li>&lt;set&gt;</li>
 * <li>&lt;unset&gt;</li>
 * </ul>
 */
public interface StateSetter {

  /**
   * Get the state
   */
  public State getState();

  /**
   * Set the state
   */
  public void setState(State state);
}
