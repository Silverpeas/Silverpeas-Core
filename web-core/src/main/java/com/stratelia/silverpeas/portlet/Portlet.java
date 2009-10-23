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
 * FLOSS exception.  You should have recieved a copy of the text describing
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

package com.stratelia.silverpeas.portlet;

import com.stratelia.webactiv.util.exception.SilverpeasException;

/**
 * Title: portlets Description: Enable portlet management in Silverpeas
 * Copyright: Copyright (c) 2001 Company: Stratelia
 * 
 * @author Eric BURGEL
 * @version 1.0
 */

public class Portlet {
  public static final int NORMAL = 0;
  public static final int MINIMIZED = 1;
  public static final int MAXIMIZED = 2;

  private int id; // DataBase id for this portlet
  private int rowId; // The PortletRowId in the database
  private int index; // index of the portlet in the SpaceModel.portlets array
  private int row; // index of the portlet in the SpaceColumn
  private int columnNumber; // column number where the portlet reside
  private String requestRooter; // The Component request rooter
  private String componentName; // The internal name of the component
  private String componentInstanceId;
  private String name; // this name will be in the title bar
  private String description;
  private String titlebarUrl = null;
  private String iconUrl = null;
  private String contentUrl = null; // The main property for the portlet
  private String maxContentUrl = null;
  private String headerUrl = null;
  private String footerUrl = null;
  private String helpUrl = null;
  private boolean maximizable; // true if the user is alowed to maximize the
  // portlet
  private boolean minimizable; // true si l'utilisateur est autorise a minimiser
  // la portlet : Seule sa title bar est visible.
  private int state; // Portlet state : 0:Normal, 1:Minimized, 2 Maximized

  /* Constructor */

  /**
   * Constructor declaration
   * 
   * 
   * @see
   */
  public Portlet() throws PortletException {
    throw new PortletException("Portlet.Portlet()", SilverpeasException.ERROR,
        "portlet.EX_CANT_USED_THIS_CONSTRUCTOR");
  }

  /**
   * Constructor declaration
   * 
   * 
   * @param aId
   * @param aRowId
   * @param aRequestRooter
   * @param aComponentName
   * @param aComponentInstanceId
   * @param aName
   * @param aDescription
   * @param aTitlebarUrl
   * @param aIconUrl
   * @param aContentUrl
   * @param aMaxContentUrl
   * @param aHeaderUrl
   * @param aFooterUrl
   * @param aHelpUrl
   * @param aMaximizable
   * @param aMinimizable
   * @param aState
   * 
   * @see
   */
  public Portlet(
      int aId, // now the instanceId in the database
      int aRowId, // The PortletRowId in the database
      String aRequestRooter, // The Component request rooter
      String aComponentName, // 
      String aComponentInstanceId,
      String aName, // This name will be in the title bar
      String aDescription, String aTitlebarUrl,
      String aIconUrl,
      String aContentUrl, // The main property for the portlet
      String aMaxContentUrl, String aHeaderUrl, String aFooterUrl,
      String aHelpUrl, boolean aMaximizable, // true if the user is aloud to
      // maximize the portlet : elle
      // occupe tout l'espace
      boolean aMinimizable, // true if the user is aloud to minimiser la portlet
      // : Seule sa title bar est visible.
      int aState) { // Current state of the portlet (Minimized, maximized, ...)

    id = aId;
    rowId = aRowId;
    setRequestRooter(aRequestRooter);
    setComponentName(aComponentName);
    setComponentInstanceId(aComponentInstanceId);
    setName(aName);
    setDescription(aDescription);
    setTitlebarUrl(aTitlebarUrl);
    setIconUrl(aIconUrl);
    setContentUrl(aContentUrl); // The main property for the portlet
    setMaxContentUrl(aMaxContentUrl);
    setHeaderUrl(aHeaderUrl);
    setFooterUrl(aFooterUrl);
    setHelpUrl(aHelpUrl);
    setMaximizable(aMaximizable); // true si l'utilisateur est autorise a
    // maximiser la portlet : elle occupe tout
    // l'espace
    setMinimizable(aMinimizable); // true si l'utilisateur est autorise a
    // minimiser la portlet : Seule sa title bar
    // est visible.
    setState(aState);
  }

  // Getters and setters

  /**
   * Method declaration
   * 
   * 
   * @return
   * 
   * @see
   */
  public int getId() {
    return id;
  }

  /**
   * Method declaration
   * 
   * 
   * @return
   * 
   * @see
   */
  public int getRowId() {
    return rowId;
  }

  /**
   * Method declaration
   * 
   * 
   * @return
   * 
   * @see
   */
  public int getIndex() {
    return index;
  }

  /**
   * Method declaration
   * 
   * 
   * @param aIndex
   * 
   * @see
   */
  public void setIndex(int aIndex) {
    index = aIndex;
  }

  /**
   * Method declaration
   * 
   * 
   * @return
   * 
   * @see
   */
  public int getRow() {
    return row;
  }

  /**
   * Method declaration
   * 
   * 
   * @param aRow
   * 
   * @see
   */
  public void setRow(int aRow) {
    row = aRow;
  }

  /**
   * Method declaration
   * 
   * 
   * @return
   * 
   * @see
   */
  public int getColumnNumber() {
    return columnNumber;
  }

  /**
   * Method declaration
   * 
   * 
   * @param aColumnNumber
   * 
   * @see
   */
  public void setColumnNumber(int aColumnNumber) {
    columnNumber = aColumnNumber;
  }

  /**
   * Method declaration
   * 
   * 
   * @return
   * 
   * @see
   */
  public String getRequestRooter() {
    return requestRooter;
  }

  /**
   * Method declaration
   * 
   * 
   * @param aRequestRooter
   * 
   * @see
   */
  public void setRequestRooter(String aRequestRooter) {
    requestRooter = aRequestRooter;
  }

  /**
   * Method declaration
   * 
   * 
   * @return
   * 
   * @see
   */
  public String getComponentName() {
    return componentName;
  }

  /**
   * Method declaration
   * 
   * 
   * @param aComponentName
   * 
   * @see
   */
  public void setComponentName(String aComponentName) {
    componentName = aComponentName;
  }

  /**
   * Method declaration
   * 
   * 
   * @return
   * 
   * @see
   */
  public String getComponentInstanceId() {
    return componentInstanceId;
  }

  /**
   * Method declaration
   * 
   * 
   * @param aComponentInstanceId
   * 
   * @see
   */
  public void setComponentInstanceId(String aComponentInstanceId) {
    componentInstanceId = aComponentInstanceId;
  }

  /**
   * Method declaration
   * 
   * 
   * @return
   * 
   * @see
   */
  public String getName() {
    return name;
  }

  /**
   * Method declaration
   * 
   * 
   * @param aName
   * 
   * @see
   */
  public void setName(String aName) {
    name = aName;
  }

  /**
   * Method declaration
   * 
   * 
   * @return
   * 
   * @see
   */
  public String getDescription() {
    return description;
  }

  /**
   * Method declaration
   * 
   * 
   * @param aDescription
   * 
   * @see
   */
  public void setDescription(String aDescription) {
    if (aDescription == null) {
      description = "";
    } else {
      description = aDescription;
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
  public String getTitlebarUrl() {
    return titlebarUrl;
  }

  /**
   * Method declaration
   * 
   * 
   * @param aTitlebarUrl
   * 
   * @see
   */
  public void setTitlebarUrl(String aTitlebarUrl) {
    if (aTitlebarUrl == null) {
      titlebarUrl = "portletTitlebar.jsp";
    } else {
      titlebarUrl = aTitlebarUrl;
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
  public String getIconUrl() {
    return iconUrl;
  }

  /**
   * Method declaration
   * 
   * 
   * @param aIconUrl
   * 
   * @see
   */
  public void setIconUrl(String aIconUrl) {
    if (aIconUrl == null) {
      iconUrl = "portletIcon.gif";
    } else {
      iconUrl = aIconUrl;
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
  public String getContentUrl() {
    return contentUrl;
  }

  /**
   * Method declaration
   * 
   * 
   * @param aContentUrl
   * 
   * @see
   */
  public void setContentUrl(String aContentUrl) {
    if (aContentUrl == null) {
      contentUrl = "portlet";
    } else {
      contentUrl = aContentUrl;
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
  public String getMaxContentUrl() {
    return maxContentUrl;
  }

  /**
   * Method declaration
   * 
   * 
   * @param aMaxContentUrl
   * 
   * @see
   */
  public void setMaxContentUrl(String aMaxContentUrl) {
    if (aMaxContentUrl == null) {
      maxContentUrl = "Main.jsp";
    } else {
      maxContentUrl = aMaxContentUrl;
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
  public String getHeaderUrl() {
    return headerUrl;
  }

  /**
   * Method declaration
   * 
   * 
   * @param aHeaderUrl
   * 
   * @see
   */
  public void setHeaderUrl(String aHeaderUrl) {
    headerUrl = aHeaderUrl;
  }

  /**
   * Method declaration
   * 
   * 
   * @return
   * 
   * @see
   */
  public String getFooterUrl() {
    return footerUrl;
  }

  /**
   * Method declaration
   * 
   * 
   * @param aFooterUrl
   * 
   * @see
   */
  public void setFooterUrl(String aFooterUrl) {
    footerUrl = aFooterUrl;
  }

  /**
   * Method declaration
   * 
   * 
   * @return
   * 
   * @see
   */
  public String getHelpUrl() {
    return helpUrl;
  }

  /**
   * Method declaration
   * 
   * 
   * @param aHelpUrl
   * 
   * @see
   */
  public void setHelpUrl(String aHelpUrl) {
    helpUrl = aHelpUrl;
  }

  /**
   * Method declaration
   * 
   * 
   * @return
   * 
   * @see
   */
  public boolean getMaximizable() {
    return maximizable;
  }

  /**
   * Method declaration
   * 
   * 
   * @param aMaximizable
   * 
   * @see
   */
  public void setMaximizable(boolean aMaximizable) {
    maximizable = aMaximizable;
  }

  /**
   * Method declaration
   * 
   * 
   * @return
   * 
   * @see
   */
  public boolean getMinimizable() {
    return minimizable;
  }

  /**
   * Method declaration
   * 
   * 
   * @param aMinimizable
   * 
   * @see
   */
  public void setMinimizable(boolean aMinimizable) {
    minimizable = aMinimizable;
  }

  /**
   * Method declaration
   * 
   * 
   * @return
   * 
   * @see
   */
  public int getState() {
    return state;
  }

  /**
   * Method declaration
   * 
   * 
   * @return
   * 
   * @see
   */
  public String getStateAsString() {
    String ret;

    switch (state) {
      case Portlet.MINIMIZED:
        ret = "min";
        break;
      case Portlet.MAXIMIZED:
        ret = "max";
        break;
      default:
        ret = "";
    }
    return ret;
  }

  /**
   * Method declaration
   * 
   * 
   * @param aState
   * 
   * @see
   */
  public void setState(int aState) {
    state = aState;
  }

  /**
   * Method declaration
   * 
   * 
   * @param aState
   * 
   * @see
   */
  public void setState(String aState) {
    if (aState == null) {
      state = Portlet.NORMAL;
    } else if (aState.equalsIgnoreCase("max")) {
      state = Portlet.MAXIMIZED;
    } else if (aState.equalsIgnoreCase("min")) {
      state = Portlet.MINIMIZED;
    } else {
      state = Portlet.NORMAL;
    }
  }

}
