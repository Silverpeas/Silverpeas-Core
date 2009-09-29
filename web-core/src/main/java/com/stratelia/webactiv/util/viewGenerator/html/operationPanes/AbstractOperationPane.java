/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent) 
 ---*/

/*
 * ArrayPaneWA.java
 * 
 * Created on 10 octobre 2000, 16:11
 */

package com.stratelia.webactiv.util.viewGenerator.html.operationPanes;

import java.util.Vector;

import com.stratelia.webactiv.util.viewGenerator.html.GraphicElementFactory;

/**
 * The default implementation of ArrayPane interface
 * 
 * @author squere
 * @version 1.0
 */
public abstract class AbstractOperationPane implements OperationPane {

  private Vector stack = null;

  // private String iconsPath = null;

  /**
   * Constructor declaration
   * 
   * 
   * @see
   */
  public AbstractOperationPane() {
    stack = new Vector();
  }

  /**
   * Method declaration
   * 
   * 
   * @return
   * 
   * @see
   */
  public String getIconsPath() {
    /*
     * if (iconsPath == null) { ResourceLocator generalSettings = new
     * ResourceLocator("com.stratelia.webactiv.general", "fr");
     * 
     * iconsPath = generalSettings.getString("ApplicationURL") +
     * GraphicElementFactory.getSettings().getString("IconsPath"); } return
     * iconsPath;
     */
    return GraphicElementFactory.getIconsPath();
  }

  /**
   * Method declaration
   * 
   * 
   * @return
   * 
   * @see
   */
  public Vector getStack() {
    return this.stack;
  }

  /**
   * Method declaration
   * 
   * 
   * @return
   * 
   * @see
   */
  public int nbOperations() {
    int nbOperations = 0;

    if (getStack() != null) {
      nbOperations = getStack().size();
    }
    return nbOperations;
  }

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
  public abstract void addOperation(String iconPath, String altText,
      String action);

  /**
   * Method declaration
   * 
   * 
   * @see
   */
  public abstract void addLine();

  /**
   * Method declaration
   * 
   * 
   * @return
   * 
   * @see
   */
  public abstract String print();
}
