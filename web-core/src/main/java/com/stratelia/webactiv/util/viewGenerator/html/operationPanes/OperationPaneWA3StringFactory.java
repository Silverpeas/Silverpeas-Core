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

package com.stratelia.webactiv.util.viewGenerator.html.operationPanes;

import com.stratelia.webactiv.util.viewGenerator.html.GraphicElementFactory;

public class OperationPaneWA3StringFactory extends Object {
  /**
   * Hashtable which contains the specifics code encoded as key and their values
   * are right code encoded
   */
  private static StringBuffer printString1 = null;
  private static StringBuffer printString2 = null;

  public static StringBuffer getPrintString1() {
    if (printString1 == null) {
      synchronized (OperationPaneWA3StringFactory.class) {
        if (printString1 == null) {
          String iconsPath = GraphicElementFactory.getIconsPath();
          printString1 = new StringBuffer();
          printString1.append("<tr>\n");
          printString1.append(
              "<td class=intfdcolor rowspan=2 width=1><img src=\"").append(
              iconsPath).append("/1px.gif\" width=1></td>\n");
          printString1.append(
              "<td rowspan=2 class=intfdcolor4 width=1><img src=\"").append(
              iconsPath).append("/1px.gif\" width=1></td>\n");
          printString1.append("<td class=intfdcolor51><img src=\"").append(
              iconsPath).append("/1px.gif\" width=7 height=1></td>\n");
          printString1.append("<td class=intfdcolor51><img src=\"").append(
              iconsPath).append("/1px.gif\" width=27 height=1></td>\n");
          printString1.append("<td class=intfdcolor4><img src=\"").append(
              iconsPath).append("/1px.gif\"></td>\n");
          printString1.append(
              "<td rowspan=5 class=intfdcolor width=1><img src=\"").append(
              iconsPath).append("/1px.gif\" width=1></td>\n");
          printString1.append(
              "<td rowspan=6 class=intfdcolor51 width=2><img src=\"").append(
              iconsPath).append("/1px.gif\" width=2 height=2></td>\n");
          printString1.append("</tr>\n");
          printString1.append("<tr>\n");
          printString1
              .append("<td width=\"34\" class=intfdcolor51 colspan=2 align=center>\n");
          printString1
              .append("<table border=\"0\" cellspacing=\"0\" cellpadding=\"1\">");
        }
      }
    }
    return printString1;
  }

  public static StringBuffer getPrintString2() {
    if (printString2 == null) {
      synchronized (OperationPaneWA3StringFactory.class) {
        if (printString2 == null) {
          String iconsPath = GraphicElementFactory.getIconsPath();
          printString2 = new StringBuffer();
          printString2.append("</table>\n");
          printString2.append("</td>\n");
          printString2.append(
              "<td class=intfdcolor4 rowspan=3 width=1><img src=\"").append(
              iconsPath).append("/1px.gif\" width=1></td>\n");
          printString2.append("</tr>\n");
          printString2.append("<tr>\n");
          printString2.append("<td rowspan=4 width=1><img src=\"").append(
              iconsPath).append("/1px.gif\" width=1></td>\n");
          printString2
              .append(
                  "<td rowspan=4 valign=top align=right colspan=2 class=intfdcolor51><img src=\"")
              .append(iconsPath).append(
                  "/operationPane/angle.gif\" width=8 height=11></td>\n");
          printString2.append("<td class=intfdcolor51><img src=\"").append(
              iconsPath).append("/1px.gif\" height=6 width=27></td>\n");
          printString2.append("</tr>\n");
          printString2.append("<tr>\n");
          printString2.append("<td class=intfdcolor4><img src=\"").append(
              iconsPath).append("/1px.gif\" width=27 height=1></td>\n");
          printString2.append("</tr>\n");
          printString2.append("<tr>\n");
          printString2.append("<td class=intfdcolor colspan=2><img src=\"")
              .append(iconsPath).append("/1px.gif\" width=28 height=1></td>\n");
          printString2.append("</tr>\n");
          printString2.append("<tr>\n");
          printString2.append("<td colspan=3 class=intfdcolor51><img src=\"")
              .append(iconsPath).append("/1px.gif\" width=29 height=2></td>\n");
          printString2.append("</tr>\n");
          printString2.append("</table>\n");
        }
      }
    }
    return printString2;
  }

}