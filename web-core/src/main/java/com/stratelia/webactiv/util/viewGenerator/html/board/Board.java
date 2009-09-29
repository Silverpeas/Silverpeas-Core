/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent) 
 ---*/

/*
 * Board.java
 * 
 * Created on 27 march 2001, 16:11
 */

package com.stratelia.webactiv.util.viewGenerator.html.board;

import com.stratelia.webactiv.util.viewGenerator.html.SimpleGraphicElement;

/**
 * Board is an interface to be implemented by a graphic element to print a frame
 * in an html format.
 * 
 * @author lloiseau
 * @version 1.0
 */
public interface Board extends SimpleGraphicElement {
  /**
   * add a string on the board.
   */
  public void addBody(String body);

  /**
   * Print the board in an html format
   * 
   * @return The Frame representation
   */
  public String print();

  /**
   * Method declaration
   * 
   * 
   * @return
   * 
   * @see
   */
  public String printBefore();

  /**
   * Method declaration
   * 
   * 
   * @return
   * 
   * @see
   */
  public String printAfter();
}
