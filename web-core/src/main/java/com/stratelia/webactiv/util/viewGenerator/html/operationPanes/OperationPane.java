/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent) 
 ---*/

/*
 * BrowseBar.java
 * 
 * Created on 07 decembre 2000, 11:26
 */

package com.stratelia.webactiv.util.viewGenerator.html.operationPanes;

import com.stratelia.webactiv.util.viewGenerator.html.SimpleGraphicElement;

/**
 * The Browse interface gives us the skeleton for all funtionnalities we need to
 * display typical WA browse bar
 * 
 * @author neysseri
 * @version 1.0
 */
public interface OperationPane extends SimpleGraphicElement {

  /**
   * Method declaration
   * 
   * 
   * @return
   * 
   * @see
   */
  public int nbOperations();

  /**
   * Method declaration
   * 
   * 
   * @param iconPath
   * @param altText
   * @param action
   * 
   * @see
   */
  public void addOperation(String iconPath, String altText, String action);

  /**
   * Method declaration
   * 
   * 
   * @see
   */
  public void addLine();

  /**
   * Print the browseBar in an html format.
   * 
   * @return The html based line code
   */
  public String print();

}
