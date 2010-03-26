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
 * FrameSogreah.java
 * 
 * Created on 27 mars 2001, 15:22
 */

package com.stratelia.webactiv.util.viewGenerator.html.frame;

/**
 * @author mraverdy&lloiseau
 * @version 1.0
 */
public class FrameSilverpeasWeb20 extends AbstractFrame {

  /**
   * Creates new FrameWA2
   */
  public FrameSilverpeasWeb20() {
    super();
  }

  /**
   * Method declaration
   * @return
   * @see
   */
  public String printBefore() {
    String result = "";
    String iconsPath = getIconsPath();

    result += "<table width=\"100%\"  border=\"0\" cellspacing=\"0\" cellpadding=\"0\"><tr><td>\n";
    result +=
        "<table width=\"100%\" border=\"0\" cellspacing=\"0\" cellpadding=\"0\" class=\"tableFrame\">\n";
    result += "\t<tr>\n";
    result += "\t\t<td>\n";
    if (getTitle() != null) {
      result += "<span class=titreFenetre>\n";
      result += getTitle();
      result += "</span>\n";
    } else {
      result += "<table cellpadding=0 cellspacing=0 border=0><tr><td><img src=\""
          + iconsPath + "/1px.gif\" height=\"5\"></td></tr></table>\n";
    }

    return result;

  }

  /**
   * Method declaration
   * @return
   * @see
   */
  public String printMiddle() {
    String result = "";

    result += "\t\t</td>\n";
    result += "\t</tr>\n";
    result += "\t<tr>\n";
    result += "\t\t<td>\n";

    setMiddle();
    return result;
  }

  /**
   * Method declaration
   * @return
   * @see
   */
  public String printAfter() {
    String result = "";
    String iconsPath = getIconsPath();

    if (!hasMiddle()) {
      result += printMiddle();

    }

    result += "\t\t</td>\n";
    result += "\t</tr>\n";
    result += "<tr><td><img src=\"" + iconsPath
        + "/1px.gif\" height=\"5\"></td></tr>\n";
    result += "</table>\n";
    result += "</td><td class=\"shadowFrame\" valign=\"top\"><img src=\""
        + iconsPath + "/pxFond.gif\" width=\"2\" align=\"top\"></td>\n";
    result += "</tr><tr>\n";
    result += "<td class=\"shadowFrame\" width=\"100%\"><img src=\""
        + iconsPath + "/pxFond.gif\" width=\"2\" align=\"left\"></td>\n";
    result += "<td class=\"shadowFrame\"><img src=\"" + iconsPath
        + "/1px.gif\"></td>\n";
    result += "</tr></table>\n";
    return result;
  }

  /**
   * Method declaration
   * @return
   * @see
   */
  public String print() {
    String result = "";

    result += printBefore();
    if (getTop() != null) {
      result += getTop();
    }
    result += printMiddle();
    if (getBottom() != null) {
      result += getBottom();
    }
    result += printAfter();

    return result;
  }

}
