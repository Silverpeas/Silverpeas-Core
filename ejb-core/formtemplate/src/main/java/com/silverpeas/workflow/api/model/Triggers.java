package com.silverpeas.workflow.api.model;

import java.util.Iterator;
import java.util.List;

/**
 * Interface describing a representation of the &lt;triggers&gt; element of a
 * Process Model.
 */
public interface Triggers {

  /**
   * Get the referenced Trigger objects as a list
   */
  public List getTriggerList();

  /**
   * Iterate through the Trigger objects
   * 
   * @return an iterator
   */
  public Iterator iterateTrigger();

  /**
   * Add a trigger to the collection
   * 
   * @param trigger
   *          to be added
   */
  public void addTrigger(Trigger trigger);

  /**
   * Create a trigger
   * 
   * @return an object implementing Trigger
   */
  public Trigger createTrigger();

  /**
   * Remove all trigger objects from the collection
   */
  public void removeAllTriggers();
}