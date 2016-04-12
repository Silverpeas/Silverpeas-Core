/*
 * Copyright (C) 2000 - 2016 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of the GPL, you may
 * redistribute this Program in connection with Free/Libre Open Source Software ("FLOSS")
 * applications as described in Silverpeas's FLOSS exception. You should have received a copy of the
 * text describing the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */

package org.silverpeas.core.web.util.viewgenerator.html.icons;

import org.silverpeas.core.util.StringUtil;

/**
 * Class declaration
 *
 * @author
 */
public class IconWA extends AbstractIcon {

  /**
   * Creates new IconWA
   */
  public IconWA() {
    super();
  }

  public IconWA(String iconName) {
    super(iconName);
  }

  /**
   * Constructor declaration
   *
   * @param iconName
   * @param altText
   * @see
   */
  public IconWA(String iconName, String altText) {
    super(iconName, altText);
  }

  /**
   * Constructor declaration
   *
   * @param iconName
   * @param altText
   * @param action
   * @see
   */
  public IconWA(String iconName, String altText, String action) {
    super(iconName, altText, action);
  }

  /**
   * Constructor declaration
   *
   * @param iconName
   * @param altText
   * @param action
   * @param imagePath
   * @see
   */
  public IconWA(String iconName, String altText, String action, String imagePath) {
    super(iconName, altText, action, imagePath);
  }

  /**
   * Method declaration
   *
   * @return
   * @see
   */
  @Override
  public String print() {
    String path = getRootImagePath() + getImagePath();
    String currentAction = getAction();
    String currentIcon = getIconName();
    String alt = getAltText();
    StringBuilder str = new StringBuilder(256);

    if (StringUtil.isDefined(currentAction)) {
      str.append("<a href=\"").append(currentAction).append("\"");
      if (StringUtil.isDefined(alt)) {
        str.append(" title=\"").append(alt).append("\"");
      } else {
        str.append(" title=\"\"");
      }
      str.append(">");
    }
    str.append("<img src=\"").append(path).append(currentIcon).append("\" border=\"0\"");

    if (StringUtil.isDefined(alt)) {
      str.append(" alt=\"").append(alt).append("\"");
      str.append(" title=\"").append(alt).append("\"");
    } else {
      str.append(" alt=\"\"");
    }
    str.append("/>");
    if (StringUtil.isDefined(currentAction)) {
      str.append("</a>");
    }

    return str.toString();
  }
}
