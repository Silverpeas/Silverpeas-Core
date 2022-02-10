/*
 * Copyright (C) 2000 - 2022 Silverpeas
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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.web.util.viewgenerator.html.frame;

/**
 * @author mraverdy&lloiseau
 * @version 1.0
 */
public class FrameSilverpeasV5 extends AbstractFrame {

  /**
   * Creates new FrameWA2
   */
  public FrameSilverpeasV5() {
    super();
  }

  /**
   * Method declaration
   * @return
   *
   */
  @Override
  public String printBefore() {
    String result = "";

    result +=
        "<div class=\"tableFrame\">\n";
    result += "<div class=\"hautFrame\">\n";
    if (getTitle() != null) {
      result += "<span class=\"titreFenetre\">\n";
      result += getTitle();
      result += "</span>\n";
    }

    return result;

  }

  /**
   * Method declaration
   * @return
   *
   */
  @Override
  public String printMiddle() {
    String result = "";

    result += "</div>\n";
    result += "<div class=\"milieuFrame\">\n";

    setMiddle();
    return result;
  }

  /**
   * Method declaration
   * @return
   *
   */
  @Override
  public String printAfter() {
    String result = "";
    String iconsPath = getIconsPath();

    if (!hasMiddle()) {
      result += printMiddle();

    }

    result += "</div>\n";
    result += "</div>\n";
    return result;
  }

  /**
   * Method declaration
   * @return
   *
   */
  @Override
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
