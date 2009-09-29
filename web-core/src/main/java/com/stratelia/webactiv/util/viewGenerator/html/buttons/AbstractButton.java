/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent) 
 ---*/

/*
 * ButtonWA.java
 * 
 * Created on 10 octobre 2000, 16:18
 */

package com.stratelia.webactiv.util.viewGenerator.html.buttons;

import com.stratelia.webactiv.util.viewGenerator.html.GraphicElementFactory;

/**
 * 
 * @author neysseri
 * @version
 */
public abstract class AbstractButton implements Button {

  public String label;
  public String action;
  public boolean disabled;

  // private String iconsPath = null;

  /**
   * Creates new ButtonWA
   */
  public AbstractButton() {
  }

  /**
   * Method declaration
   * 
   * 
   * @param label
   * @param action
   * @param disabled
   * 
   * @see
   */
  public void init(String label, String action, boolean disabled) {
    this.label = label;
    this.action = action;
    this.disabled = disabled;
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
   * @param s
   * 
   * @see
   */
  public void setRootImagePath(String s) {
  }

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
