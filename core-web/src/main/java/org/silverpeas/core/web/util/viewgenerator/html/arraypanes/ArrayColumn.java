/*
 * Copyright (C) 2000 - 2024 Silverpeas
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
 * "https://www.silverpeas.org/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.web.util.viewgenerator.html.arraypanes;

import org.silverpeas.core.web.util.viewgenerator.html.SimpleGraphicElement;

import javax.servlet.http.HttpServletRequest;
import java.util.function.BiFunction;

import static org.silverpeas.core.web.util.viewgenerator.html.arraypanes.ArrayPane.*;

/**
 * @author squere
 */
public class ArrayColumn implements SimpleGraphicElement {

  /**
   * This behaviour is set when a column header is not sortable. No hyperlink will be anchored
   * around the title of the column.
   */
  public static final int COLUMN_BEHAVIOUR_NO_TRIGGER = 1;
  /**
   * Default behaviour in which the column header is sortable. An hyperlink will be anchored
   * around the title of the column.
   */
  public static final int COLUMN_BEHAVIOUR_DEFAULT = 0;
  private String title;
  private final int columnNumber;
  private final ArrayPane pane;
  private int sortBehaviour = COLUMN_BEHAVIOUR_DEFAULT;
  private String width = null;
  private BiFunction<Object, Integer, Comparable<Object>> compareOn;

  /**
   * In some cases, it may be preferable to specify the routing address (via
   * {@link #setRoutingAddress(String address)}) If not the {@link #print()} method defaults to an
   * address derived from the request URL. Note that te routing address may start with the protocol
   * string <strong>arraypane:</strong>, in which case a javascript:doArrayPane() URL is issued
   * instead of a standard URL.
   */
  protected String routingAddress = null;

  public ArrayColumn(String title, int columnNumber, ArrayPane pane) {
    this.title = title;
    this.columnNumber = columnNumber;
    this.pane = pane;
  }

  /**
   * This method sets the routing address. This is actually the URL of the page to which requests
   * will be routed when the user clicks on a column header link.
   * @param address the address of the service for processing the requests.
   */
  public void setRoutingAddress(String address) {
    routingAddress = address;
  }

  /**
   * Set the column to be sortable or not. If the array is already unsortable, this method will have
   * no effect.
   * @param sortable A true value will enable this column to be sorted
   */
  public void setSortable(boolean sortable) {
    if (sortable) {
      sortBehaviour = ArrayColumn.COLUMN_BEHAVIOUR_DEFAULT;
    } else {
      sortBehaviour = ArrayColumn.COLUMN_BEHAVIOUR_NO_TRIGGER;
    }
  }

  public boolean isSortable() {
    if (pane.getSortable()) {
      return (sortBehaviour == ArrayColumn.COLUMN_BEHAVIOUR_DEFAULT);
    } else {
      return false;
    }
  }

  public BiFunction<Object, Integer, Comparable<Object>> getCompareOn() {
    return compareOn;
  }

  public void setCompareOn(final BiFunction<Object, Integer, Comparable<Object>> compareOn) {
    setSortable(true);
    this.compareOn = compareOn;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getTitle() {
    return title;
  }

  public int getColumnNumber() {
    return columnNumber;
  }

  public void setWidth(String width) {
    this.width = width;
  }

  public String getWidth() {
    return width;
  }

  protected boolean isArrayPaneURL(String address) {
    return (address != null && address.trim().toLowerCase().startsWith("arraypane:"));
  }

 @Override
  public String print() {
   StringBuilder result = new StringBuilder();
   boolean isAP = false;

   result.append("<th scope=\"col\"");

   if (getWidth() != null) {
     result.append(" width=\"").append(getWidth()).append("\" ");
   }

   if (title != null) {
     result.append(" class=\"ArrayColumn\">");

     if (isSortable()) {
       setHeaderHref(isAP, result);
     } // behaviour 'no trigger'
      else {
        result.append(title).append("</th>");
      }
    } else {
      result.append(" class=\"ArrayColumn\">&nbsp;</th>");
    }
    return result.toString();
  }

  private void setHeaderHref(boolean isAP, StringBuilder result) {
    String address;
    String jsStartString = "";
    String jsEndString = "";

    // routing address computation. By default, route to the short name for
    // the calling page
    if (routingAddress == null) {
      address = ((HttpServletRequest) pane.getRequest()).getRequestURI();
      // only get a relative http address
      address = address.substring(address.lastIndexOf('/') + 1);
      // if the previous request had parameters, remove them
      if (address.lastIndexOf('?') >= 0) {
        address = address.substring(0, address.lastIndexOf('?'));
      }
    } else {
      address = routingAddress;
      isAP = isArrayPaneURL(address);
      if (isAP) {
        address = "javascript:doArrayPane";
        jsStartString = "(";
        jsEndString = ");";
      }
    }

    String sep = "&";


    StringBuilder href = new StringBuilder();
    href.append(address).append(jsStartString);

    // standard non-javascript url. Add parameters to the url
    if (!isAP) {
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
    href.append(jsEndString);
    result.append("<a class=\"ArrayColumn\" href=\"").append(href);
    result.append("\">").append(title).append("</a>");

    result.append("</th>");
  }
}