/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent) 
 ---*/

/*
 * FrameWA.java
 * 
 * Created on 27 mars 2001, 15:22
 */

package com.stratelia.webactiv.util.viewGenerator.html.board;

/**
 * 
 * @author mraverdy&lloiseau
 * @version 1.0
 */
public class BoardSilverpeasV5 extends AbstractBoard {

  /**
   * Creates new FrameWA
   */
  public BoardSilverpeasV5() {
    super();
  }

  /**
   * Method declaration
   * 
   * 
   * @return
   * 
   * @see
   */
  public String printBefore() {
    StringBuffer result = new StringBuffer();

    result.append("<CENTER>");
    result
        .append("<TABLE CELLPADDING=\"5\" CELLSPACING=\"0\" BORDER=\"0\" WIDTH=\"98%\" CLASS=\"tableBoard\"><TR><TD nowrap=\"nowrap\">");

    return result.toString();
  }

  /**
   * Method declaration
   * 
   * 
   * @return
   * 
   * @see
   */
  public String printAfter() {
    StringBuffer result = new StringBuffer();

    result.append("</TD></TR></TABLE>");
    result.append("</CENTER>");

    return result.toString();
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
    StringBuffer result = new StringBuffer();

    result.append(printBefore());
    result.append(getBody());
    result.append(printAfter());

    return result.toString();
  }
}