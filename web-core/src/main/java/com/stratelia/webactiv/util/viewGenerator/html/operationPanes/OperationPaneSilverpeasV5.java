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
 * ArrayPaneWA.java
 * 
 * Created on 10 octobre 2000, 16:11
 */

package com.stratelia.webactiv.util.viewGenerator.html.operationPanes;

import java.util.Vector;

/**
 * The default implementation of ArrayPane interface
 * @author squere
 * @version 1.0
 */
public class OperationPaneSilverpeasV5 extends AbstractOperationPane {

  /**
   * Constructor declaration
   * @see
   */
  public OperationPaneSilverpeasV5() {
    super();
  }

  /**
   * Method declaration
   * @param iconPath
   * @param altText
   * @param action
   * @see
   */
  public void addOperation(String iconPath, String altText, String action) {
    Vector stack = getStack();
    StringBuffer operation = new StringBuffer();

    operation.append("<tr>\n");
    operation
        .append(
            "<td valign=\"top\" align=center class=\"operationStyle\" width=\"34\" height=\"34\"><a id=\"")
        .append(altText).append("\" href=\"").append(action).append(
            "\"><img src=\"").append(iconPath).append("\" alt=\"").append(
            altText).append("\" title=\"").append(altText).append(
            "\" border=\"0\"></a></td>\n");
    operation.append("</tr>\n");
    stack.add(operation.toString());
  }

  /**
   * Method declaration
   * @see
   */
  public void addLine() {
    String iconsPath = getIconsPath();
    Vector stack = getStack();

    stack.add("<tr><td><img src=\"" + iconsPath
        + "/tabs/1px.gif\"></td></tr>\n");
  }

  /**
   * Method declaration
   * @return
   * @see
   */
  public String print() {
    StringBuffer result = new StringBuffer();
    Vector stack = getStack();

    result.append(OperationPaneSilverpeasV5StringFactory.getPrintString1());

    for (int i = 0; i < stack.size(); i++) {
      result.append((String) stack.elementAt(i));
    }

    result.append(OperationPaneSilverpeasV5StringFactory.getPrintString2());

    return result.toString();
  }

}