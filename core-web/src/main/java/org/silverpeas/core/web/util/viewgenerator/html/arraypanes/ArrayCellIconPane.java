/*
 * Copyright (C) 2000 - 2016 Silverpeas
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
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.silverpeas.core.web.util.viewgenerator.html.arraypanes;

import org.silverpeas.core.web.util.viewgenerator.html.SimpleGraphicElement;
import org.silverpeas.core.web.util.viewgenerator.html.iconpanes.IconPane;

/**
 * Class declaration
 * @author
 */
public class ArrayCellIconPane extends ArrayCell implements SimpleGraphicElement {

  private IconPane iconPane;
  private String alignement;

  /**
   * Constructor declaration
   * @param iconPane
   * @param line
   * @see
   */
  public ArrayCellIconPane(IconPane iconPane, ArrayLine line) {
    super(line);
    this.iconPane = iconPane;
  }

  /**
   * Method declaration
   * @return
   * @see
   */
  public IconPane getIconPane() {
    return iconPane;
  }

  /**
   * @return
   */
  public String getAlignement() {
    return alignement;
  }

  /**
   * @param textAlign
   */
  public void setAlignement(String alignement) {
    this.alignement = alignement;
  }

  /**
   * Method declaration
   * @return
   * @see
   */
  public String print() {
    String result = "<td";

    if (getAlignement() != null) {
      if (getAlignement().equalsIgnoreCase("center")
          || getAlignement().equalsIgnoreCase("right")) {
        result += " align=\"" + getAlignement() + "\"";
      }
    }

    result += ">";
    result += getIconPane().print();
    result += "</td>";
    return result;
  }

}
