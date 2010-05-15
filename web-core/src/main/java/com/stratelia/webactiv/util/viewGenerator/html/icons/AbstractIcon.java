/**
 * Copyright (C) 2000 - 2009 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://repository.silverpeas.com/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

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
 * @author
 */
public abstract class AbstractIcon implements Icon {

  public String iconName;
  public String altText;
  public String action = "";
  public String imagePath = "";

  /**
   * This is prepended to the image path if need be.
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
   * @param iconName
   * @param altTex
   * @see
   */
  public AbstractIcon(String iconName, String altText) {
    this.iconName = iconName;
    this.altText = altText;
  }

  /**
   * Constructor declaration
   * @param iconName
   * @param altText
   * @param action
   * @see
   */
  public AbstractIcon(String iconName, String altText, String action) {
    this.iconName = iconName;
    this.altText = altText;
    this.action = action;
  }

  /**
   * Constructor declaration
   * @param iconName
   * @param altText
   * @param action
   * @param imagePath
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
   * @param iconName
   * @param altText
   * @see
   */
  public void setProperties(String iconName, String altText) {
    this.iconName = iconName;
    this.altText = altText;
  }

  /**
   * Method declaration
   * @param iconName
   * @param altText
   * @param action
   * @see
   */
  public void setProperties(String iconName, String altText, String action) {
    this.iconName = iconName;
    this.altText = altText;
    this.action = action;
  }

  /**
   * Method declaration
   * @param iconName
   * @param altText
   * @param action
   * @param imagePath
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
   * @param s
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
   * @return
   * @see
   */
  public String getAction() {
    return this.action;
  }

  /**
   * Method declaration
   * @return
   * @see
   */
  public String getIconName() {
    return this.iconName;
  }

  /**
   * Method declaration
   * @return
   * @see
   */
  public String getAltText() {
    return this.altText;
  }

  /**
   * Method declaration
   * @return
   * @see
   */
  public String getImagePath() {
    return this.imagePath;
  }

  /**
   * Method declaration
   * @return
   * @see
   */
  public String getRootImagePath() {
    return this.m_RootImagePath;
  }

  /**
   * Method declaration
   * @return
   * @see
   */
  public abstract String print();
}
