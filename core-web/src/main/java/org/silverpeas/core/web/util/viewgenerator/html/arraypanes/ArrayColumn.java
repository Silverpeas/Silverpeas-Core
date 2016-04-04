/*
 * Copyright (C) 2000 - 2016 Silverpeas
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
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.silverpeas.core.web.util.viewgenerator.html.arraypanes;

import static org.silverpeas.core.web.util.viewgenerator.html.arraypanes.ArrayPane.ACTION_PARAMETER_NAME;
import static org.silverpeas.core.web.util.viewgenerator.html.arraypanes.ArrayPane.COLUMN_PARAMETER_NAME;
import static org.silverpeas.core.web.util.viewgenerator.html.arraypanes.ArrayPane.TARGET_PARAMETER_NAME;

import javax.servlet.http.HttpServletRequest;

import org.silverpeas.core.web.util.viewgenerator.html.SimpleGraphicElement;

/**
 * @author squere
 * @version
 */
public class ArrayColumn implements SimpleGraphicElement {

  /**
   * This behaviour is set when a column header is not sortable. No hyperlink will be anchored
   * around the title of the column.
   * @see #setBehaviour(int behaviour)
   */
  final static public int COLUMN_BEHAVIOUR_NO_TRIGGER = 1;
  final static public int COLUMN_BEHAVIOUR_DEFAULT = 0;
  protected String title;
  protected String alignement;
  protected int columnNumber;
  protected ArrayPane pane;
  protected int m_Behaviour = COLUMN_BEHAVIOUR_DEFAULT;
  protected String width = null;
  /**
   * In some cases, it may be preferable to specify the routing address (via
   * {@link #setRoutingAddress(String address)}) If not the {@link #print()} method defaults to an
   * address derived from the request URL. Note that te routing address may start with the protocol
   * string <strong>arraypane:</strong>, in which case a javascript:doArrayPane() URL is issued
   * instead of a standard URL.
   */
  protected String m_RoutingAddress = null;

  /**
   * Constructor declaration
   * @param title
   * @param columnNumber
   * @param pane
   * @see
   */
  public ArrayColumn(String title, int columnNumber, ArrayPane pane) {
    this.title = title;
    this.columnNumber = columnNumber;
    this.pane = pane;
    this.alignement = null;
  }

  /**
   * standard method that returns the CVS-managed version string
   * @return
   */
  public static String getVersion() {
    String v = "$Id: ArrayColumn.java,v 1.6 2008/04/16 14:45:06 neysseri Exp $";

    return (v);
  }

  /**
   * This method sets the routing address. This is actually the URL of the page to which requests
   * will be routed when the user clicks on a column header link.
   * @param address
   */
  public void setRoutingAddress(String address) {
    m_RoutingAddress = address;
  }

  /**
   * Set the column to be sortable or not. If the array is already unsortable, this method will have
   * no effect.
   * @param sortable A true value will enable this column to be sorted
   */
  public void setSortable(boolean sortable) {
    if (sortable) {
      setBehaviour(ArrayColumn.COLUMN_BEHAVIOUR_DEFAULT);
    } else {
      setBehaviour(ArrayColumn.COLUMN_BEHAVIOUR_NO_TRIGGER);
    }
  }

  /**
   * Method declaration
   * @return
   * @see
   */
  public boolean getSortable() {
    if (pane.getSortable()) {
      return (m_Behaviour == ArrayColumn.COLUMN_BEHAVIOUR_DEFAULT);
    } else {
      return false;
    }
  }

  /**
   * This method changes the column behaviour, if the argument behaviour is valid
   * @param behaviour
   * @deprecated
   */
  public void setBehaviour(int behaviour) {
    switch (behaviour) {
      case COLUMN_BEHAVIOUR_NO_TRIGGER:
      case COLUMN_BEHAVIOUR_DEFAULT:
        m_Behaviour = behaviour;
        return;
    }
  }

  /**
   * Method declaration
   * @param title
   * @see
   */
  public void setTitle(String title) {
    this.title = title;
  }

  /**
   * Method declaration
   * @param alignement
   * @see
   */
  public void setAlignement(String alignement) {
    this.alignement = alignement;
  }

  /**
   * Method declaration
   * @return
   * @see
   */
  public String getTitle() {
    return title;
  }

  /**
   * Method declaration
   * @return
   * @see
   */
  public String getAlignement() {
    return alignement;
  }

  /**
   * Method declaration
   * @return
   * @see
   */
  public int getColumnNumber() {
    return columnNumber;
  }

  public void setWidth(String width) {
    this.width = width;
  }

  public String getWidth() {
    return width;
  }

  /**
   * Method declaration
   * @param address
   * @return
   * @see
   */
  protected boolean isArrayPaneURL(String address) {
    return (address != null && address.trim().toLowerCase().startsWith("arraypane:"));
  }

  /**
   * Method declaration
   * @return
   * @see
   */
  @Override
  public String print() {
    return print(false);
  }

  public String print(boolean xhtml) {
    StringBuilder result = new StringBuilder();
    boolean isAP = false;
    String JSStartString = "";
    String JSEndString = "";

    result.append("<th scope=\"col\"");

    // column alignement. By default, alignement is on the left
    if (alignement != null) {
      if (alignement.equalsIgnoreCase("center")
          || alignement.equalsIgnoreCase("right")) {
        if (alignement.equalsIgnoreCase("center")) {
          result.append(" align=\"center\"");
        } else {
          result.append(" align=\"right\"");
        }
      }
    }

    if (getWidth() != null) {
      result.append(" width=\"").append(getWidth()).append("\" ");
    }

    if (title != null) {
      result.append(" class=\"ArrayColumn\">");

      if (getSortable()) {
        String address = null;

        // routing address computation. By default, route to the short name for
        // the calling page
        if (m_RoutingAddress == null) {
          address = ((HttpServletRequest) pane.getRequest()).getRequestURI();
          // only get a relative http address
          address = address.substring(address.lastIndexOf('/') + 1, address.length());
          // if the previous request had parameters, remove them
          if (address.lastIndexOf('?') >= 0) {
            address = address.substring(0, address.lastIndexOf('?'));
          }
        } else {
          address = m_RoutingAddress;
          isAP = isArrayPaneURL(address);
          if (isAP) {
            address = "javascript:doArrayPane";
            JSStartString = "(";
            JSEndString = ");";
          }
        }



        String sep = "&";
        if (xhtml) {
          sep = "&amp;";
        }

        StringBuilder href = new StringBuilder();
        href.append(address).append(JSStartString);

        // standard non-javascript url. Add parameters to the url
        if (isAP == false) {
          String temp = result.toString();
          if (temp.indexOf('?') >= 0 || href.indexOf("?") >= 0) {// there are already some
            // parameters
            href.append(sep);
          } else {
            // there are no parameters
            href.append("?");
          }
          href.append(ACTION_PARAMETER_NAME).append("=Sort").append(sep);
          href.append(TARGET_PARAMETER_NAME).append('=').append(pane.getName());
          href.append(sep).append(COLUMN_PARAMETER_NAME).append('=').append(getColumnNumber());
        } // arraypane javascript function. Pass it parameters.
        else {
          href.append("'").append(pane.getName()).append("',").append(getColumnNumber());
        }
        href.append(JSEndString);
        result.append("<a class=\"ArrayColumn\" href=\"").append(href);
        result.append("\">").append(title).append("</a>");

        result.append("</th>");
      } // behaviour 'no trigger'
      else {
        result.append(title).append("</th>");
      }
    } else {
      result.append(" class=\"ArrayColumn\">&nbsp;</th>");
    }
    return result.toString();
  }
}