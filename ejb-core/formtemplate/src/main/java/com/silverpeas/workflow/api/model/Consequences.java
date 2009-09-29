package com.silverpeas.workflow.api.model;

import java.util.Iterator;
import java.util.Vector;

/**
 * Interface describing a representation of the &lt;consequences&gt; element of
 * a Process Model.
 */
public interface Consequences {
  /**
   * Get the target consequences
   * 
   * @return the target consequences as a Vector
   */
  public Vector getConsequenceList();

  /**
   * Iterate through the Consequence objects
   * 
   * @return an Iterator
   */
  Iterator iterateConsequence();

  /**
   * Crate a Consequence
   * 
   * @return a object implementing Consequence
   */
  Consequence createConsequence();

  /**
   * Add a Consequence to the collection
   * 
   * @param consequence
   *          to be added
   */
  void addConsequence(Consequence consequence);

  // void removeConsequence( );

}