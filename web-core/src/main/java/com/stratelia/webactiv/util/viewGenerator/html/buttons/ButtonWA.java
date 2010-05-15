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
public class ButtonWA extends AbstractButton {

  /**
   * Creates new ButtonWA
   */
  public ButtonWA() {
  }

  /**
   * Method declaration
   * @return
   * @see
   */
  public String print() {
    String cssStyle = "enableButtonText";
    String action = this.action;
    String iconsPath = getIconsPath();

    if (disabled) {
      cssStyle = "disableButtonText";
      action = "#";
    }

    StringBuffer str = new StringBuffer();
    str
        .append("<table border=\"0\" cellspacing=\"0\" cellpadding=\"0\" class=\"buttonStyle\">");
    str.append("<tr>");
    str.append("<td rowspan=\"3\"><a href=\"").append(action).append(
        "\"><img src=\"").append(iconsPath).append(
        "/buttons/g.gif\" border=\"0\"></a></td>");
    str.append("<td bgcolor=\"CCCCCC\" colspan=2><img src=\"")
        .append(iconsPath).append("/1px.gif\"></td>");
    str.append("<td rowspan=\"3\"><a href=\"").append(action).append(
        "\"><img src=\"").append(iconsPath).append(
        "/buttons/d.gif\" border=\"0\"></a></td>");
    str.append("</tr>");
    str.append("<tr>");
    str.append("<td><img src=\"").append(iconsPath).append(
        "/1px.gif\" height=17 width=1></td>");
    str.append("<td nowrap><a href=\"").append(action).append("\" class=\"")
        .append(cssStyle).append("\">").append(label).append("</a></td>");
    str.append("</tr>");
    str.append("<tr>");
    str.append("<td bgcolor=\"000000\" colspan=2><img src=\"")
        .append(iconsPath).append("/1px.gif\"></td>");
    str.append("</tr>");
    str.append("</table>");

    return str.toString();
  }

}