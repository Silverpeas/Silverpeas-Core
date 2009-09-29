/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent) 
 ---*/

/*
 * IconWA.java
 * 
 * @author  neysseri
 * Created on 12 decembre 2000, 11:37
 */

package com.stratelia.webactiv.util.viewGenerator.html.icons;

/*
 * CVS Informations
 * 
 * $Id: AbstractIcon.java,v 1.3 2004/06/24 17:16:38 neysseri Exp $
 * 
 * $Log: AbstractIcon.java,v $
 * Revision 1.3  2004/06/24 17:16:38  neysseri
 * nettoyage eclipse
 *
 * Revision 1.2  2003/12/03 19:18:37  neysseri
 * no message
 *
 * Revision 1.1.1.1  2002/08/06 14:48:19  nchaix
 * no message
 *
 * Revision 1.3  2002/01/04 14:04:24  mmarengo
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
public abstract class AbstractIcon implements Icon {

  public String iconName;
  public String altText;
  public String action = "";
  public String imagePath = "";

  /**
   * This is prepended to the image path if need be.
   * 
   * @see #print()
   */
  public String m_RootImagePath = "";

  /**
   * Creates new IconWA
   */
  public AbstractIcon() {
  }

  public AbstractIcon(String iconName) {
    this.iconName = iconName;
    this.altText = null;
  }

  /**
   * Constructor declaration
   * 
   * 
   * @param iconName
   * @param altTex
   * 
   * @see
   */
  public AbstractIcon(String iconName, String altText) {
    this.iconName = iconName;
    this.altText = altText;
  }

  /**
   * Constructor declaration
   * 
   * 
   * @param iconName
   * @param altText
   * @param action
   * 
   * @see
   */
  public AbstractIcon(String iconName, String altText, String action) {
    this.iconName = iconName;
    this.altText = altText;
    this.action = action;
  }

  /**
   * Constructor declaration
   * 
   * 
   * @param iconName
   * @param altText
   * @param action
   * @param imagePath
   * 
   * @see
   */
  public AbstractIcon(String iconName, String altText, String action,
      String imagePath) {
    this.iconName = iconName;
    this.altText = altText;
    this.action = action;
    this.imagePath = imagePath;
  }

  /**
   * Method declaration
   * 
   * 
   * @param iconName
   * @param altText
   * 
   * @see
   */
  public void setProperties(String iconName, String altText) {
    this.iconName = iconName;
    this.altText = altText;
  }

  /**
   * Method declaration
   * 
   * 
   * @param iconName
   * @param altText
   * @param action
   * 
   * @see
   */
  public void setProperties(String iconName, String altText, String action) {
    this.iconName = iconName;
    this.altText = altText;
    this.action = action;
  }

  /**
   * Method declaration
   * 
   * 
   * @param iconName
   * @param altText
   * @param action
   * @param imagePath
   * 
   * @see
   */
  public void setProperties(String iconName, String altText, String action,
      String imagePath) {
    this.iconName = iconName;
    this.altText = altText;
    this.action = action;
    this.imagePath = imagePath;
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
    m_RootImagePath = s;
    if (!m_RootImagePath.endsWith("/")) // should use URL separator
    {
      m_RootImagePath = m_RootImagePath + "/";
    }
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
    return this.action;
  }

  /**
   * Method declaration
   * 
   * 
   * @return
   * 
   * @see
   */
  public String getIconName() {
    return this.iconName;
  }

  /**
   * Method declaration
   * 
   * 
   * @return
   * 
   * @see
   */
  public String getAltText() {
    return this.altText;
  }

  /**
   * Method declaration
   * 
   * 
   * @return
   * 
   * @see
   */
  public String getImagePath() {
    return this.imagePath;
  }

  /**
   * Method declaration
   * 
   * 
   * @return
   * 
   * @see
   */
  public String getRootImagePath() {
    return this.m_RootImagePath;
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
