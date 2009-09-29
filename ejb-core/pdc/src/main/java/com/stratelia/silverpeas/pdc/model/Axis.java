package com.stratelia.silverpeas.pdc.model;

import java.util.List;

/**
 * This class contains a full information about a tree. The user can access to
 * an axe.
 * 
 * @author Sébastien Antonio
 */
public class Axis implements java.io.Serializable {

  /**
   * The object which contains attributs of an axe
   */
  private AxisHeader header = null;

  /**
   * The list which contains sorted values of a tree
   */
  private List values = null;

  //
  // Constructor
  //

  public Axis(AxisHeader header, List values) {
    this.header = header;
    this.values = values;
  }

  //
  // public methods
  //

  /**
   * Returns attributs of an axe.
   * 
   * @return the AxisHeader object
   */
  public AxisHeader getAxisHeader() {
    return this.header;
  }

  /**
   * Returns the sorted List containing values of a tree.
   * 
   * @return the List
   */
  public List getValues() {
    return this.values;
  }

}