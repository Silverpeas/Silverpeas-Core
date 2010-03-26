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
 * $Id: SpaceColumn.java,v 1.1.1.1 2002/08/06 14:47:52 nchaix Exp $
 * 
 * $Log: SpaceColumn.java,v $
 * Revision 1.1.1.1  2002/08/06 14:47:52  nchaix
 * no message
 *
 * Revision 1.9  2002/01/09 09:56:57  groccia
 * stabilisation lot2
 *
 */

/**
 * Class declaration
 * @author
 */
public class SpaceColumn {

  /**
   * int columnNumber
   */
  private int columnNumber = 0;

  /**
   * String columnWidth : contain the HTML string, ex: '*', '40%' '150px'
   */
  private String columnWidth;

  /**
   * ArrayList portlets : list of all the portlet of the column
   */
  private ArrayList portlets = new ArrayList(5);

  // *****************************
  // * Constructors *
  // *****************************

  /**
   * Constructor for the &lt;jsp:usebean&gt; tag compatibility
   */
  public SpaceColumn() throws PortletException {
    throw new PortletException("SpaceColumn.SpaceColumn()",
        SilverpeasException.ERROR, "portlet.EX_CANT_USED_THIS_CONSTRUCTOR");
  }

  /**
   * <init>
   * @param aColumnNumber parameter for <init>
   */
  public SpaceColumn(int aColumnNumber) {
    columnNumber = aColumnNumber;
    columnWidth = null;
  }

  /**
   * SpaceColumn
   * @param aColumnNumber
   * @param aWidthType Used to construct the frameset tag. ex "40%", "150px", "*".
   */
  public SpaceColumn(int aColumnNumber, String aColumnWidth) {
    columnNumber = aColumnNumber;
    columnWidth = aColumnWidth;
  }

  // ****************************
  // * Getters and setters *
  // ****************************

  /**
   * getColumnNumber
   * @return the column number of this column : ie it's position in the column space ArrayList
   */
  public int getColumnNumber() {
    return columnNumber;
  }

  /**
   * setColumnWidth
   * @param aColumnWidth : Used to construct the frameset that contains this column examples : "40%"
   * , "340px" or "*"
   */
  public void setColumnWidth(String aColumnWidth) {
    columnWidth = aColumnWidth;
  }

  /**
   * getColumnWidth
   * @return the column width : Used to construct the frameset that contains this column examples :
   * "40%" , "340px" or "*"
   */
  public String getColumnWidth() {
    return columnWidth;
  }

  // ****************************
  // Portlet collection managment
  // ****************************

  /**
   * getPortlets
   * @param row the index of the portlet in the column
   * @return the portlet designed by the row index
   */
  public Portlet getPortlets(int row) {
    return (Portlet) portlets.get(row);
  }

  /**
   * getPortletCount
   * @return the portlet count for this column
   */
  public int getPortletCount() {
    return portlets.size();
  }

  /**
   * addPortlet : Add the portlet to the given row
   */
  void addPortlet(Portlet aPortlet, int row) {
    if ((row > portlets.size()) || (row < 0)) {
      row = portlets.size();
    }
    aPortlet.setColumnNumber(getColumnNumber());
    portlets.add(row, aPortlet);
    // Renumerote les portlets
    for (int i = 0; i < portlets.size(); i++) {
      Portlet p = (Portlet) portlets.get(i);

      p.setRow(i);
    }
  }

  /**
   * addPortlet
   * @param aPortlet parameter for addPortlet
   */
  void addPortlet(Portlet aPortlet) {
    aPortlet.setColumnNumber(getColumnNumber());
    aPortlet.setRow(portlets.size());
    portlets.add(aPortlet);
  }

  /**
   * removePortlet remove the portlet from this column
   * @param col index of the portlet in the column
   */
  void removePortlet(int col) {
    portlets.remove(col);
    for (int i = 0; i < portlets.size(); i++) {
      Portlet portlet = (Portlet) portlets.get(i);

      portlet.setRow(i);
    }
  }

  /**
   * getRowRatios Compute ratios for the portlets in the column
   * @return the ratios used to construct the column frameset
   */
  public String getRowRatios() {
    StringBuffer ratios = new StringBuffer();

    if (portlets.size() == 0) {
      ratios.append("100%");
    } else {
      // compute the number of Normal state portlet
      String comma = "";

      for (int i = 0; i < portlets.size(); i++) {
        Portlet portlet = getPortlets(i);

        if (portlet.getState() == Portlet.MINIMIZED) {
          ratios.append(comma).append("40");
        } else {
          ratios.append(comma).append("*");
        }
        comma = ", ";
      }
    }
    return ratios.toString();
  }

}
