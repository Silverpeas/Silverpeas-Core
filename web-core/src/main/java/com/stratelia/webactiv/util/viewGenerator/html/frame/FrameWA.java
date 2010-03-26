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
 * FrameWA.java
 * 
 * Created on 27 mars 2001, 15:22
 */

package com.stratelia.webactiv.util.viewGenerator.html.frame;

/**
 * @author mraverdy&lloiseau
 * @version 1.0
 */
public class FrameWA extends AbstractFrame {

  /**
   * Creates new FrameWA
   */
  public FrameWA() {
    super();
  }

  /**
   * Method declaration
   * @return
   * @see
   */
  public String printBefore() {
    StringBuffer result = new StringBuffer();
    String iconsPath = getIconsPath();

    result
        .append(
            "<table border=0 cellspacing=0 cellpadding=0 width=\"100%\"><tr><td bgcolor=#000000 width=\"100%\"><img src=\"")
        .append(iconsPath).append("/1px.gif\"></td><td><img src=\"").append(
            iconsPath).append(
            "/1px.gif\" width=5 height=1></td></tr></table>\n");
    result
        .append("<table border=0 cellspacing=0 cellpadding=0 width=\"100%\">\n");
    result.append("<tr>\n");
    result.append("<td bgcolor=#000000 rowspan=10><img src=\"").append(
        iconsPath).append("/1px.gif\"></td>\n");
    result.append("<td bgcolor=#FFFFFF colspan=5><img src=\"")
        .append(iconsPath).append("/1px.gif\"></td>\n");
    result
        .append(
        "<td class=couleurCadre valign=top align=right colspan=2 rowspan=2><img src=\"")
        .append(iconsPath).append("/frame/hautGaucheLeft.gif\"></td>\n");
    result.append("<td valign=top colspan=2 rowspan=2><img src=\"").append(
        iconsPath).append("/frame/hautGaucheRight.gif\"></td>\n");
    result.append("</tr>\n");
    result.append("<tr>\n");
    result.append("<td bgcolor=#FFFFFF rowspan=8><img src=\"")
        .append(iconsPath).append("/1px.gif\"></td>\n");
    result.append("<td class=couleurCadre colspan=4><img src=\"").append(
        iconsPath).append("/1px.gif\" width=1 height=5></td>\n");
    result.append("</tr>\n");
    result.append("<tr>\n");
    result.append("<td class=couleurCadre><img src=\"").append(iconsPath)
        .append("/1px.gif\"></td>\n");
    result.append("<td class=couleurCadre colspan=4>\n");
    result.append("<span class=titreFenetre>\n");

    if (getTitle() != null) {
      result.append(getTitle());
    } else {
      result.append("&nbsp;");
      result.append("</span>");
    }
    result.append("</td>\n");
    result.append(
        "<td valign=top class=couleurCadre rowspan=5 width=16><img src=\"")
        .append(iconsPath).append("/1px.gif\" width=16 height=1></td>\n");
    result.append("<td valign=top bgcolor=5A5A5A rowspan=6><img src=\"")
        .append(iconsPath).append("/1px.gif\" width=8 height=1></td>\n");
    result.append("<td valign=top bgcolor=#000000 rowspan=6><img src=\"")
        .append(iconsPath).append("/1px.gif\" width=1 height=1></td>\n");
    result.append("</tr>\n");
    result.append("<tr>\n");
    result.append("<td class=couleurCadre rowspan=4><img src=\"").append(
        iconsPath).append("/1px.gif\" width=9 height=1></td>\n");
    result.append("<td bgcolor=5A5A5A rowspan=2><img src=\"").append(iconsPath)
        .append("/1px.gif\" width=1 height=1></td>\n");
    result.append("<td bgcolor=#5A5A5A colspan=2><img src=\"")
        .append(iconsPath).append("/1px.gif\" width=1 height=1></td>\n");
    result
        .append("<td valign=top bgcolor=FFFFFF rowspan=4 width=1><img src=\"")
        .append(iconsPath).append("/1px.gif\" width=1 height=1></td>\n");
    result.append("</tr>\n");
    result.append("<tr>\n");
    result.append("<td class=couleurFenetre width=100%><img src=\"").append(
        iconsPath).append("/1px.gif\">\n");

    return result.toString();
  }

  /**
   * Method declaration
   * @return
   * @see
   */
  public String printMiddle() {
    StringBuffer result = new StringBuffer();
    String iconsPath = getIconsPath();

    result.append("<img src=\"").append(iconsPath).append(
        "/1px.gif\" height=12></td>\n");
    result.append("<td valign=top bgcolor=CCCCCC width=1><img src=\"").append(
        iconsPath).append("/1px.gif\" width=1 height=1></td>\n");
    result.append("</tr>\n");
    result.append("<tr>\n");
    result.append("<td bgcolor=5A5A5A><img src=\"").append(iconsPath).append(
        "/1px.gif\" width=1 height=1></td>\n");
    result.append("<td bgcolor=CCCCCC colspan=2><img src=\"").append(iconsPath)
        .append("/1px.gif\" width=1 height=1></td>\n");
    result.append("</tr>\n");
    result.append("<tr>\n");
    result.append("<td bgcolor=#5A5A5A><img src=\"").append(iconsPath).append(
        "/1px.gif\" width=1 height=1></td>\n");
    result.append("<td bgcolor=#FFFFFF colspan=2><img src=\"")
        .append(iconsPath).append("/1px.gif\" width=1 height=1></td>\n");
    result.append("</tr>\n");
    result.append("<tr>\n");
    result.append("<td class=couleurCadre colspan=2><img src=\"").append(
        iconsPath).append("/1px.gif\" width=1 height=1></td>\n");
    result.append("<td class=couleurCadre colspan=3>\n");

    setMiddle();
    return result.toString();
  }

  /**
   * Method declaration
   * @return
   * @see
   */
  public String printAfter() {
    StringBuffer result = new StringBuffer();
    String iconsPath = getIconsPath();

    if (!hasMiddle()) {
      result.append(printMiddle());

    }
    result.append("</td>\n");
    result.append(
        "<td class=couleurCadre valign=bottom align=right><img src=\"").append(
        iconsPath).append("/frame/arc1.gif\" width=13 height=12><img src=\"")
        .append(iconsPath)
        .append("/frame/arc2.gif\" width=5 height=12></td>\n");
    result.append("</tr>\n");
    result.append("<tr>\n");
    result.append("<td colspan=5 rowspan=2 align=right valign=top>\n");
    result.append("<table border=0 cellspacing=0 cellpadding=0>\n");
    result.append("<tr>\n");
    result
        .append("<td width=100% class=couleurCadre nowrap align=center><span class=legalInformation>\n");
    result
        .append("<img src=\"")
        .append(iconsPath)
        .append(
            "/1px.gif\" width=100 height=1>Powered by EJB technology - &copy; 2001 Silverpeas All Rights Reserved</span></td>\n");
    result.append(
        "<td class=couleurCadre valign=bottom align=right><img src=\"").append(
        iconsPath).append("/frame/basDroite.gif\" width=36 height=18></td>\n");
    result.append("</tr>\n");
    result.append("<tr>\n");
    result.append("<td bgcolor=#000000 width=100%><img src=\"").append(
        iconsPath).append("/1px.gif\" width=1 height=1></td>\n");
    result.append("<td width=4%><img src=\"").append(iconsPath).append(
        "/1px.gif\" width=1 height=1></td>\n");
    result.append("</tr>\n");
    result.append("</table>\n");
    result.append("</td>\n");
    result.append("<td valign=top nowrap><img src=\"").append(iconsPath)
        .append("/frame/angle_basDroite.gif\" width=18 height=18></td>\n");
    result.append("<td valign=top nowrap colspan=2><img src=\"").append(
        iconsPath).append(
        "/frame/angle_basDroite2.gif\" width=9 height=18></td>\n");
    result.append("</tr>\n");
    result.append("<tr>\n");
    result.append("<td bgcolor=#000000><img src=\"").append(iconsPath).append(
        "/1px.gif\" width=1 height=1></td>\n");
    result.append("<td><img src=\"").append(iconsPath).append(
        "/1px.gif\" width=1 height=1></td>\n");
    result.append("<td colspan=2><img src=\"").append(iconsPath).append(
        "/1px.gif\" width=1 height=1></td>\n");
    result.append("</tr>\n");
    result.append("</table>\n");

    return result.toString();
  }

  /**
   * Method declaration
   * @return
   * @see
   */
  public String print() {
    StringBuffer result = new StringBuffer();

    result.append(printBefore());
    result.append(getTop());
    result.append(printMiddle());
    result.append(getBottom());
    result.append(printAfter());

    return result.toString();
  }

}