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
 * ButtonWA.java
 * 
 * Created on 10 octobre 2000, 16:18
 */

package com.stratelia.webactiv.util.viewGenerator.html.buttons;

/**
 * @author neysseri
 * @version
 */
public class ButtonWA2 extends AbstractButton {

  /**
   * Creates new ButtonWA
   */
  public ButtonWA2() {
  }

  /**
   * Method declaration
   * @return
   * @see
   */
  public String print() {
    String action = this.action;
    String iconsPath = getIconsPath();

    if (disabled) {
      action = "#";
    }

    StringBuffer str = new StringBuffer();
    str.append("<table border=\"0\" cellspacing=\"0\" cellpadding=\"0\">");
    str.append("<tr>");
    str.append("<td colspan=\"3\" rowspan=\"3\"><img src=\"").append(iconsPath)
        .append("/buttons/bt2_hg.gif\"></td>");
    str.append("<td bgcolor=\"#999999\"><img src=\"").append(iconsPath).append(
        "/1px.gif\"></td>");
    str.append("<td colspan=\"2\" rowspan=\"3\"><img src=\"").append(iconsPath)
        .append("/buttons/bt2_hd.gif\"></td>");
    str.append("</tr>");
    str.append("<tr>");
    str.append("<td bgcolor=\"#FFFFFF\"><img src=\"").append(iconsPath).append(
        "/1px.gif\"></td>");
    str.append("</tr>");
    str.append("<tr>");
    str.append("<td class=\"buttonColorLight\"><img src=\"").append(iconsPath)
        .append("/1px.gif\"></td>");
    str.append("</tr>");
    str.append("<tr>");
    str.append("<td bgcolor=\"#666666\"><img src=\"").append(iconsPath).append(
        "/1px.gif\"></td>");
    str.append("<td bgcolor=\"#CCCCCC\"><img src=\"").append(iconsPath).append(
        "/1px.gif\"></td>");
    str.append("<td class=\"buttonColorLight\"><img src=\"").append(iconsPath)
        .append("/1px.gif\"></td>");
    str.append("<td nowrap class=\"buttonStyle\"><a href=\"").append(action)
        .append("\" class=\"buttonStyle\">&nbsp;").append(label).append(
        "&nbsp;</a></td>");
    str.append("<td class=\"buttonColorDark\"><img src=\"").append(iconsPath)
        .append("/1px.gif\"></td>");
    str.append("<td bgcolor=\"#666666\"><img src=\"").append(iconsPath).append(
        "/1px.gif\" width=2></td>");
    str.append("</tr>");
    str.append("<tr>").append("<td colspan=\"3\" rowspan=\"2\"><img src=\"")
        .append(iconsPath).append("/buttons/bt2_bg.gif\"></td>");
    str.append("<td class=\"buttonColorDark\"><img src=\"").append(iconsPath)
        .append("/1px.gif\"></td>");
    str.append("<td colspan=\"2\" rowspan=\"2\"><img src=\"").append(iconsPath)
        .append("/buttons/bt2_bd.gif\"></td>");
    str.append("</tr>");
    str.append("<tr>");
    str.append("<td bgcolor=\"#666666\"><img src=\"").append(iconsPath).append(
        "/1px.gif\" height=2></td>");
    str.append("</tr>");
    str.append("</table>");

    return str.toString();
  }

}