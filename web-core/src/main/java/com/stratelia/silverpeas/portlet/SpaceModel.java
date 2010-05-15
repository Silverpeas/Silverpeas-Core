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

/**
 * Title:        portlets
 * Description:  Enable portlet management in Silverpeas
 * Copyright:    Copyright (c) 2001
 * Company:      Stratelia
 * @author       Eric BURGEL
 * @version 1.0
 */

package com.stratelia.silverpeas.portlet;

import java.util.ArrayList;

import com.stratelia.webactiv.util.exception.SilverpeasException;

/*
 * CVS Informations
 * 
 * $Id: SpaceModel.java,v 1.1.1.1 2002/08/06 14:47:52 nchaix Exp $
 * 
 * $Log: SpaceModel.java,v $
 * Revision 1.1.1.1  2002/08/06 14:47:52  nchaix
 * no message
 *
 * Revision 1.10  2002/01/09 09:56:57  groccia
 * stabilisation lot2
 *
 */

/**
 * Class declaration
 * @author
 */
public class SpaceModel {

  /**
   * String spaceId : The database Id of the table "ST_Space" ex : 17
   */
  private String spaceId;

  /**
   * ArrayList spaceColumns : List of this space columns
   */
  private ArrayList spaceColumns;

  /**
   * ArrayList portlets : Linear list of all portlets of this space whatever is the column where it
   * reside
   */
  private ArrayList portlets;

  /**
   * boolean isAdministrator : true if the current user is an administrator of the space
   */
  private boolean isAdministrator;

  /**
   * int userId : current user id for this space model if any.
   */
  private int userId;

  // *****************************************************
  // * Constructors
  // *****************************************************

  /**
   * Empty constructor to satisfy the J2EE requirement for the &lt;jsp:useBean&gt; tag Should never
   * be called
   */
  public SpaceModel() throws PortletException {
    throw new PortletException("SpaceModel.SpaceModel()",
        SilverpeasException.ERROR, "portlet.EX_CANT_USED_THIS_CONSTRUCTOR");
  }

  /**
   * SpaceModel
   * @param String aSpaceId
   */
  public SpaceModel(String aSpaceId) {
    spaceId = aSpaceId;
    spaceColumns = new ArrayList(3);
    portlets = new ArrayList();
    isAdministrator = true;
  }

  // ***************************************
  // * Getters and setters
  // ***************************************

  /**
   * setIsAdministrator
   * @param isAdmin true if the user is administrator
   */
  public void setIsAdministrator(boolean isAdmin) {
    isAdministrator = isAdmin;
  }

  /**
   * getIsAdministrator
   * @return the returned boolean
   */
  public boolean getIsAdministrator() {
    return isAdministrator;
  }

  /**
   * setUserId
   * @param aUserId parameter for setUserId
   */
  public void setUserId(int aUserId) {
    userId = aUserId;
  }

  /**
   * getUserId
   * @return the returned int
   */
  public int getUserId() {
    return userId;
  }

  /**
   * getSpaceId
   * @return the returned int
   */
  public int getSpaceId() {
    return Integer.parseInt(spaceId);
  }

  /**
   * Method declaration
   * @return
   * @see
   */
  public String getname() {
    return "spaceName";
  }

  /**
   * getcolumnsCount
   * @return the returned int
   */
  public int getcolumnsCount() {
    if (spaceColumns == null) {
      return 0;
    }
    return spaceColumns.size();
  }

  /**
   * getColumn
   * @param col parameter for getColumn
   * @return the returned SpaceColumn
   */
  public SpaceColumn getColumn(int col) {
    return (SpaceColumn) spaceColumns.get(col);
  }

  /**
   * getColumnsRatios
   * @return the ratios to be used in the frameset tag.
   */
  public String getColumnsRatios() {
    // return columnRatios ;
    String columnRatios;

    if (spaceColumns.size() > 0) {
      columnRatios = null;
      for (int col = 0; col < spaceColumns.size(); col++) {
        SpaceColumn sc = (SpaceColumn) spaceColumns.get(col);

        if (columnRatios == null) {
          columnRatios = sc.getColumnWidth();
        } else {
          columnRatios = columnRatios + ", " + sc.getColumnWidth();
        }
      }
    } else {
      columnRatios = "100%";
    }
    return columnRatios;
  }

  /**
   * Retrieve a portlet by Its index in the space Model
   */
  public Portlet getPortlets(int index) {
    return (Portlet) portlets.get(index);
  }

  /**
   * Method declaration
   * @return
   * @see
   */
  public int getPortletCount() {
    return portlets.size();
  }

  /**
   * getPortlet
   * @param col parameter for getPortlet
   * @return the returned Portlet
   */
  public Portlet getPortlet(int col, int row) {
    SpaceColumn sc = (SpaceColumn) spaceColumns.get(col);

    return sc.getPortlets(row);
  }

  // Retrieve the portlet index in the spaceModel, given the instance Id

  /**
   * return -1 if not found.
   */
  public int getPortletIndex(int instanceId) {
    int ret = -1;

    for (int i = 0; i < portlets.size(); i++) {
      Portlet portlet = (Portlet) portlets.get(i);

      if (portlet.getId() == instanceId) {
        ret = i;
        break;
      }
    }
    return ret;
  }

  // ****************************************************************
  // Updating the structure

  /**
   * Update all the indexes of the portlet.
   */
  private void updatePortletIndexes() {
    for (int i = 0; i < portlets.size(); i++) {
      Portlet portlet = (Portlet) portlets.get(i);

      portlet.setIndex(i);
    }

    for (int col = 0; col < spaceColumns.size(); col++) {
      SpaceColumn sc = (SpaceColumn) spaceColumns.get(col);

      for (int row = 0; row < sc.getPortletCount(); row++) {
        Portlet portlet = sc.getPortlets(row);

        portlet.setRow(row);
        portlet.setColumnNumber(col);
      }
    }
  }

  /**
   * Insert a column to the space model at the specified position
   */
  public void addColumn(SpaceColumn aColumn, int col) {
    spaceColumns.add(col, aColumn);
    updatePortletIndexes();
  }

  /**
   * Add a column to the space model
   */
  public void addColumn(SpaceColumn aColumn) {
    spaceColumns.add(aColumn);
    // 
    for (int i = 0; i < aColumn.getPortletCount(); i++) {
      Portlet aPortlet = aColumn.getPortlets(i);

      aPortlet.setIndex(portlets.size());
      portlets.add(aPortlet);
    }
  }

  /**
   * Insert a single portlet to the SpaceModel at the specified col, before the specified row if the
   * col is invalid a new one is created and added to the spaceModel
   */
  public void addPortlet(int col, int row, Portlet aPortlet) {
    SpaceColumn sc;

    if ((col >= spaceColumns.size()) || (col < 0)) {
      col = spaceColumns.size();
    }
    // if the portlet is to be added to a non existant column
    if (col == spaceColumns.size()) {
      // we have to create it
      sc = new SpaceColumn(spaceColumns.size() + 1, "50%");
      // and to add it to the current space
      addColumn(sc);
    }
    // Retrieve the existant column
    sc = (SpaceColumn) spaceColumns.get(col);

    // add the portlet to the column
    sc.addPortlet(aPortlet, row);

    // add the portlet to the global spaceModel portlet collection
    aPortlet.setIndex(portlets.size());
    portlets.add(aPortlet);
  }

  /**
   * Append a single portlet to the SpaceModel to the specified column. if the column is invalid,
   * create a new one.
   */
  public void addPortlet(int colNum, Portlet aPortlet) {
    SpaceColumn sc;

    if ((colNum >= spaceColumns.size()) || (colNum < 0)) {
      colNum = spaceColumns.size();
    }
    // if the portlet is to be added to a non existant column
    if (colNum == spaceColumns.size()) {
      // we have to create it
      sc = new SpaceColumn(spaceColumns.size() + 1, "50%");

      sc.addPortlet(aPortlet);

      // and to add it to the current space
      addColumn(sc);
    } else {
      // Retrieve the existant column
      sc = (SpaceColumn) spaceColumns.get(colNum);
      aPortlet.setIndex(portlets.size());
      sc.addPortlet(aPortlet);
      portlets.add(aPortlet);
    }
  }

  /**
   * removeColumn
   * @param col parameter for removeColumn
   */
  public void removeColumn(int col) {
    // get the columns
    SpaceColumn sc = (SpaceColumn) spaceColumns.get(col);

    // remove all the portlet from the column
    while (sc.getPortletCount() > 0) {
      sc.removePortlet(0);
    }
    // remove the column
    spaceColumns.remove(col);
  }

  /**
   * // portletIndex is the general portlet space index, i.e. index in the portlets ArrayList
   */

  /*
   * public void removePortlet(int portletIndex) { // retrieve the portlet Portlet portlet =
   * (Portlet)portlets.get(portletIndex) ; // retrieve the portletIndex in the column int col =
   * portlet.getColumnNumber() ; // remove the portlet from the SpaceColumn SpaceColumn sc =
   * (SpaceColumn)spaceColumns.get(col) ; int row = portlet.getRow() ; sc.removePortlet(row) ; //
   * remove the portlet from the space portlets.remove(portletIndex) ; if (sc.getPortletCount() ==
   * 0) { spaceColumns.remove(col) ; } // renumerote les portlets updatePortletIndexes() ; }
   */

  /**
   * Remove the portlet from the in memory spaceModel
   * @param col is the index of the column in spaceColumns, first col is 0
   * @param row is the index of the portlet in the column, first row is 0
   */
  public void removePortlet(int col, int row) {
    // retrieve the column where the portlet reside
    SpaceColumn sc = (SpaceColumn) spaceColumns.get(col);
    // retrieve the portlet
    Portlet portlet = sc.getPortlets(row);
    // remove the portlet from the portlet list
    int portletIndex = portlet.getIndex();

    portlets.remove(portletIndex);
    // remove the portlet from the SpaceColumn
    sc.removePortlet(row);
    if (sc.getPortletCount() == 0) {
      spaceColumns.remove(col);
    }
    // renumerote les portlets
    updatePortletIndexes();
  }

}
