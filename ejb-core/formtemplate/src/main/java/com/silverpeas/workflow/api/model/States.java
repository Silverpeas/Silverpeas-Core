package com.silverpeas.workflow.api.model;

import java.util.Iterator;

import com.silverpeas.workflow.api.WorkflowException;

/**
 * Interface describing a representation of the &lt;states&gt; element of a
 * Process Model.
 */
public interface States {

  /**
   * Iterate through the State objects
   * 
   * @return an iterator
   */
  public Iterator iterateState();

  /**
   * Create an State
   * 
   * @return an object implementing State
   */
  public State createState();

  /**
   * Add an state to the collection
   * 
   * @param state
   *          to be added
   */
  public void addState(State state);

  /**
   * Get the states defined for this process model
   * 
   * @return states defined for this process model
   */
  public State[] getStates();

  /**
   * Get the state definition with given name
   * 
   * @param name
   *          state name
   * @return wanted state definition
   */
  public State getState(String name);

  /**
   * Remove an state from the collection
   * 
   * @param strStateName
   *          the name of the state to be removed.
   * @throws WorkflowException
   *           when the state cannot be found
   */
  public void removeState(String strStateName) throws WorkflowException;
}
