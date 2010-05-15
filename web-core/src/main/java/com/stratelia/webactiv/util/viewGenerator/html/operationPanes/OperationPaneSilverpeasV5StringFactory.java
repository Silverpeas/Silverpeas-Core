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

package com.stratelia.webactiv.util.viewGenerator.html.operationPanes;

import com.stratelia.webactiv.util.viewGenerator.html.GraphicElementFactory;

public class OperationPaneSilverpeasV5StringFactory extends Object {
  /**
   * Hashtable which contains the specifics code encoded as key and their values are right code
   * encoded
   */
  private static StringBuffer printString1 = null;
  private static StringBuffer printString2 = null;

  public static StringBuffer getPrintString1() {
    if (printString1 == null) {
      synchronized (OperationPaneSilverpeasV5StringFactory.class) {
        if (printString1 == null) {
          printString1 = new StringBuffer();
          printString1
              .append("<table border=\"0\" cellspacing=\"0\" cellpadding=\"1\">");
          printString1.append("<tr><td><img src=\"").append(
              GraphicElementFactory.getIconsPath()).append(
              "/tabs/1px.gif\"></td></tr>\n");
        }
      }
    }
    return printString1;
  }

  public static StringBuffer getPrintString2() {
    if (printString2 == null) {
      synchronized (OperationPaneSilverpeasV5StringFactory.class) {
        if (printString2 == null) {
          printString2 = new StringBuffer();
          printString2.append("</table>\n");
        }
      }
    }
    return printString2;
  }

}