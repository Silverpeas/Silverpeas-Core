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

/**
 * Class declaration
 * @author
 */
public class ArrayCellLink extends ArrayCell implements SimpleGraphicElement, Comparable {

  private String text = null;
  private String alignement = null;
  private String valignement = null;
  private String color = null;
  private String link = null;
  private String info = null;
  private String target = null;

  /**
   * Constructor declaration
   * @param text
   * @param link
   * @param line
   * @see
   */
  public ArrayCellLink(String text, String link, ArrayLine line) {
    super(line);
    this.text = text;
    this.link = link;
  }

  /**
   * Constructor declaration
   * @param text
   * @param link
   * @param info
   * @param line
   * @see
   */
  public ArrayCellLink(String text, String link, String info, ArrayLine line) {
    super(line);
    this.text = text;
    this.link = link;
    this.info = info;
  }

  /**
   * Method declaration
   * @return
   * @see
   */
  public String getText() {
    return text;
  }

  /**
   * @return
   */
  public String getAlignement() {
    return alignement;
  }

  /**
   * @param CellAlign
   */
  public void setAlignement(String alignement) {
    this.alignement = alignement;
  }

  public String getValignement() {
    return valignement;
  }

  /**
   * @param CellAlign
   */
  public void setValignement(String valignement) {
    this.valignement = valignement;
  }

  /**
   * @return
   */
  public String getColor() {
    return color;
  }

  /**
   * @param CellAlign
   */
  public void setColor(String color) {
    this.color = color;
  }

  /**
   * Method declaration
   * @return
   * @see
   */
  public String getLink() {
    return link;
  }

  /**
   * Method declaration
   * @return
   * @see
   */
  public String getInfo() {
    return info;
  }

  public String getTarget() {
    return target;
  }

  public void setTarget(String target) {
    this.target = target;
  }

  /**
   * Method declaration
   * @param other
   * @return
   * @see
   */
  public int compareTo(final java.lang.Object other) {
    if (other instanceof ArrayEmptyCell) {
      return 1;
    }
    if (!(other instanceof ArrayCellLink)) {
      return 0;
    }
    ArrayCellLink tmp = (ArrayCellLink) other;

    // return this.getText().compareTo(tmp.getText());
    return this.getText().compareToIgnoreCase(tmp.getText());
  }

  /**
   * Method declaration
   * @return
   * @see
   */
  public String print() {
    StringBuilder result = new StringBuilder();
    result.append("<td");

    if (getAlignement() != null) {
      if (getAlignement().equalsIgnoreCase("center")
          || getAlignement().equalsIgnoreCase("right")) {
        result.append(" align=\"").append(getAlignement()).append("\"");
      }
    }

    if (getValignement() != null) {
      if (getValignement().equalsIgnoreCase("bottom")
          || getValignement().equalsIgnoreCase("top")
          || getValignement().equalsIgnoreCase("baseline")) {
        result.append(" valign=\"").append(getValignement()).append("\"");
      }
    }

    result.append(" class=\"").append(getStyleSheet()).append("\">");

    if (getColor() != null) {
      result.append(" <font color=\"").append(getColor()).append("\">");
    }

    result.append("<a class=\"").append(getStyleSheet()).append("\" ");
    result.append("href=\"").append(getLink()).append("\"");

    if (getTarget() != null)
      result.append(" target=\"").append(getTarget()).append("\"");

    result.append(">");
    result.append(getText());
    result.append("</a>");

    if (getColor() != null) {
      result.append("</font>");
    }

    result.append("</td>\n");
    return result.toString();
  }

}
