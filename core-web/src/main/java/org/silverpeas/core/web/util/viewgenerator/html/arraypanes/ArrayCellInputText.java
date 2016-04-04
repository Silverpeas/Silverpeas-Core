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
 * @author jboulet
 * @version
 */

public class ArrayCellInputText extends ArrayCell implements SimpleGraphicElement {

  // -----------------------------------------------------------------------------------------------------------------
  // Attributs
  // -----------------------------------------------------------------------------------------------------------------
  private String name;
  private String value = null;
  private String size = null;
  private String maxlength = null;

  private String cellAlign = null;

  private String color = null;
  private String bgcolor = null;
  private String textAlign = null;
  private boolean readOnly = false;
  private String action = null; // Action javaScript

  private String syntax = "";

  // -----------------------------------------------------------------------------------------------------------------
  // Constructeur
  // -----------------------------------------------------------------------------------------------------------------

  /**
   * Constructor declaration
   * @param name
   * @param value
   * @param line
   * @see
   */
  public ArrayCellInputText(String name, String value, ArrayLine line) {
    super(line);
    this.name = name;
    this.value = value;
  }

  /**
   * @return
   */
  public String getCellAlign() {
    return cellAlign;
  }

  /**
   * @param CellAlign
   */
  public void setCellAlign(String cellAlign) {
    this.cellAlign = cellAlign;
  }

  /**
   * @return
   */
  public String getName() {
    return name;
  }

  /**
   * @return
   */
  public String getValue() {
    return value;
  }

  /**
   * @return
   */
  public String getSize() {
    return size;
  }

  /**
   * @param size
   */
  public void setSize(String size) {
    this.size = size;
  }

  /**
   * @return
   */
  public String getMaxlength() {
    return maxlength;
  }

  /**
   * @param maxlength
   */
  public void setMaxlength(String maxlength) {
    this.maxlength = maxlength;
  }

  /**
   * @return
   */
  public String getColor() {
    return color;
  }

  /**
   * @param maxlength
   */
  public void setColor(String color) {
    this.color = color;
  }

  /**
   * @return
   */
  public String getBgcolor() {
    return bgcolor;
  }

  /**
   * @param bgcolor
   */
  public void setBgcolor(String bgcolor) {
    this.bgcolor = bgcolor;
  }

  /**
   * @return
   */
  public String getTextAlign() {
    return textAlign;
  }

  /**
   * @param textAlign
   */
  public void setTextAlign(String textAlign) {
    this.textAlign = textAlign;
  }

  /**
   * @return
   */
  public String getAction() {
    return action;
  }

  /**
   * @param action
   */
  public void setAction(String action) {
    this.action = action;
  }

  /**
   * @return
   */
  public boolean getReadOnly() {
    return readOnly;
  }

  /**
   * @param likeText
   */
  public void setReadOnly(boolean readOnly) {
    this.readOnly = readOnly;
  }

  // -----------------------------------------------------------------------------------------------------------------
  // Ecriture de l'input en fonction de son type, de sa valeur et de son nom
  // -----------------------------------------------------------------------------------------------------------------

  /**
   * Method declaration
   * @return
   * @see
   */
  public String getSyntax() {

    syntax += " <input type=\"text\" name=\"";

    // param name
    if (getName() == null) {
      syntax += "textfield\" value=\"";
    } else {
      syntax += getName() + "\" value=\"";
    }

    // param value
    if (getValue() == null) {
      syntax += "\"";
    } else {
      syntax += getValue() + "\"";
    }

    // param size
    if (getSize() != null) {
      syntax += " size=\"" + getSize() + "\"";
    }

    // param maxlength
    if (getMaxlength() != null) {
      syntax += " maxlength=\"" + getMaxlength() + "\"";
    }

    // set Style
    syntax += " style=\"";

    // param likeText
    if (getReadOnly() == true) {
      syntax += "border: 1 solid rgb(255,255,255);";
    }

    // param textAlign
    if (getTextAlign() != null) {
      syntax += "text-align:" + getTextAlign() + ";";
    }

    // param color
    if (getColor() != null) {
      syntax += " color:" + getColor() + ";";
    }

    // param background color
    if (getBgcolor() != null) {
      syntax += " background-color:" + getBgcolor() + ";";
    }

    syntax += "\"";

    // param action JavaScript
    if (getAction() != null) {
      syntax += " " + getAction();
    }

    // readOnly ???
    if (getReadOnly() == true) {
      syntax += " readOnly";
    }

    syntax += "/>";

    return syntax;
  }

  // -----------------------------------------------------------------------------------------------------------------

  /**
   * Method declaration
   * @return
   * @see
   */
  public String print() {
    String result = "<td ";

    if (getCellAlign() != null) {
      if (getCellAlign().equalsIgnoreCase("center")
          || getCellAlign().equalsIgnoreCase("right")) {
        result += " align=\"" + getCellAlign() + "\"";
      }
    }

    result += " class=\"" + getStyleSheet() + "\">";

    result += getSyntax();

    result += "</td>\n";
    return result;
  }

}
