/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent) 
 ---*/

/*
 * SimpleGraphicElement.java
 * 
 * Created on 10 octobre 2000, 16:11
 */

package com.stratelia.webactiv.util.viewGenerator.html.tabs;

/**
 * 
 * @author squere
 * @version 1.0
 */
public class Tab {
  private String label;
  private String action;
  private boolean selected;
  private boolean enabled;

  /**
   * Constructor declaration
   * 
   * 
   * @param label
   * @param action
   * @param selected
   * 
   * @see
   */
  public Tab(String label, String action, boolean selected) {
    this.label = label;
    this.action = action;
    this.selected = selected;
    this.enabled = true;
  }

  /**
   * Constructor declaration
   * 
   * 
   * @param label
   * @param action
   * @param selected
   * @param enabled
   * 
   * @see
   */
  public Tab(String label, String action, boolean selected, boolean enabled) {
    this.label = label;
    this.action = action;
    this.selected = selected;
    this.enabled = enabled;
  }

  /**
   * Method declaration
   * 
   * 
   * @return
   * 
   * @see
   */
  public String getLabel() {
    return label;
  }

  /**
   * Method declaration
   * 
   * 
   * @return
   * 
   * @see
   */
  public String getAction() {
    return action;
  }

  /**
   * Method declaration
   * 
   * 
   * @return
   * 
   * @see
   */
  public boolean getSelected() {
    return selected;
  }

  /**
   * Method declaration
   * 
   * 
   * @return
   * 
   * @see
   */
  public boolean getEnabled() {
    return this.enabled;
  }

}
