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
public class BoardSP extends AbstractBoard {

  /**
   * Creates new FrameWA
   */
  public BoardSP() {
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
    String result = "";

    result += "<CENTER>";
    result += "<TABLE CELLPADDING=5 CELLSPACING=2 BORDER=0 WIDTH=\"98%\" CLASS=intfdcolor><TR><TD CLASS=intfdcolor4 NOWRAP>";

    return result;
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
    String result = "";

    result += "</TD></TR></TABLE>";
    result += "</CENTER>";

    return result;
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
    String result = "";

    result += printBefore();
    result += getBody();
    result += printAfter();

    return result;
  }

}
