/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent) 
 ---*/

package com.stratelia.webactiv.util.coordinates.model;

import java.io.Serializable;
import java.util.Collection;

/**
 * Class declaration
 * 
 * 
 * @author
 * @version %I%, %G%
 */
public class Coordinate implements Serializable {

  private int coordinateId;
  private Collection coordinatePoints;

  /**
   * Empty Constructor needed for mapping Castor
   * 
   */
  public Coordinate() {
  }

  /**
   * Constructor declaration
   * 
   * 
   * @param coordinateId
   * @param coordinatePoints
   * 
   * @see
   */
  public Coordinate(int coordinateId, Collection coordinatePoints) {
    this.coordinateId = coordinateId;
    this.coordinatePoints = coordinatePoints;
  }

  /**
   * Method declaration
   * 
   * 
   * @return
   * 
   * @see
   */
  public int getCoordinateId() {
    return this.coordinateId;
  }

  /**
   * Method declaration
   * 
   * 
   * @return
   * 
   * @see
   */
  public Collection getCoordinatePoints() {
    return this.coordinatePoints;
  }

  /**
   * Method declaration
   * 
   * 
   * @param coordinateId
   * 
   * @see
   */
  public void setCoordinateId(int coordinateId) {
    this.coordinateId = coordinateId;
  }

  /**
   * Method declaration
   * 
   * 
   * @param coordinatePoints
   * 
   * @see
   */
  public void setCoordinatePoints(Collection coordinatePoints) {
    this.coordinatePoints = coordinatePoints;
  }

  /**
   * Method declaration
   * 
   * 
   * @return
   * 
   * @see
   */
  public String toString() {
    String result = "Coordinate {" + "\n";

    result = result + "  getCoordinateId() = " + getCoordinateId() + "\n";
    result = result + "  getCoordinatePoints() = "
        + getCoordinatePoints().toString() + "\n";
    result = result + "}";
    return result;
  }

}
