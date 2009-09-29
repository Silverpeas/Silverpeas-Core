/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent) 
 ---*/

package com.stratelia.webactiv.util.viewGenerator.html.arrayPanes;

import com.stratelia.webactiv.util.viewGenerator.html.SimpleGraphicElement;

/*
 * CVS Informations
 * 
 * $Id: ArrayEmptyCell.java,v 1.1.1.1 2002/08/06 14:48:19 nchaix Exp $
 * 
 * $Log: ArrayEmptyCell.java,v $
 * Revision 1.1.1.1  2002/08/06 14:48:19  nchaix
 * no message
 *
 * Revision 1.2  2002/01/04 14:04:23  mmarengo
 * Stabilisation Lot 2
 * SilverTrace
 * Exception
 *
 */

/**
 * Class declaration
 * 
 * 
 * @author
 */
public class ArrayEmptyCell implements Comparable, SimpleGraphicElement {

  /**
   * Constructor declaration
   * 
   * 
   * @see
   */
  public ArrayEmptyCell() {
  }

  /**
   * Method declaration
   * 
   * 
   * @param other
   * 
   * @return
   * 
   * @see
   */
  public int compareTo(final java.lang.Object other) {
    if (!(other instanceof ArrayEmptyCell)) {
      return -1;
    }
    return 0;
  }

  /**
   * Method declaration
   * 
   * 
   * @return
   * 
   * @see
   */
  public String print() {
    return "<td>&nbsp;</td>";
  }

}
