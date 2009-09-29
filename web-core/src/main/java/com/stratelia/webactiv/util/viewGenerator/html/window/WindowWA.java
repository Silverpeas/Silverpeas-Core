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

/*
 * WindowWA.java
 * 
 * Created on 10 octobre 2000, 16:11
 */

package com.stratelia.webactiv.util.viewGenerator.html.window;

/**
 * The default implementation of Window interface
 * 
 * @author neysseri
 * @version 1.0
 */
public class WindowWA extends AbstractWindow {

  /**
   * Constructor declaration
   * 
   * 
   * @see
   */
  public WindowWA() {
    super();
  }

  /**
   * Method declaration
   * 
   * 
   * @return
   * 
   * @see
   */
  public String printBefore() {
    StringBuffer result = new StringBuffer();
    String width = getWidth();

    int nbCols = 1;

    if (getOperationPane().nbOperations() > 0)
      nbCols = 2;

    result.append("<table width=\"").append(width).append(
        "\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\">");
    result.append("<tr><td colspan=\"").append(nbCols).append("\">");
    result.append(getBrowseBar().print());
    result.append("</td></tr>");
    result.append("<tr><td width=\"100%\" valign=\"top\">");
    result
        .append("<table border=\"0\" width=\"100%\" cellpadding=\"5\" cellspacing=\"5\"><tr><td align=\"center\" valign=\"top\">");
    return result.toString();
  }

  /**
   * Method declaration
   * 
   * 
   * @return
   * 
   * @see
   */
  public String printAfter() {
    StringBuffer result = new StringBuffer();
    String iconsPath = getIconsPath();

    int nbCols = 1;

    if (getOperationPane().nbOperations() > 0)
      nbCols = 2;

    result.append("</td></tr></table>");
    result.append("</td>");
    if (getOperationPane().nbOperations() > 0) {
      result.append("<td valign=\"top\" align=\"right\">");
      result.append(getOperationPane().print());
      result.append("</td>");
    }
    result.append("</tr>");
    result.append("<tr><td>&nbsp;</td></tr>");
    result.append("<tr><td colspan=\"").append(nbCols).append(
        "\" bgcolor=\"000000\"><img src=\"").append(iconsPath).append(
        "/1px.gif\" width=\"1\" height=\"1\" border=\"0\"></td></tr>");
    result.append("</table>");

    return result.toString();
  }

  /**
   * Method declaration
   * 
   * 
   * @return
   * 
   * @see
   */
  public String print() {
    StringBuffer result = new StringBuffer();

    result.append(printBefore());
    result.append(getBody());
    result.append(printAfter());

    return result.toString();
  }

}