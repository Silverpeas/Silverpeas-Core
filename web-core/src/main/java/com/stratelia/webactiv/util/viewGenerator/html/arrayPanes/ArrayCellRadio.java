/**
 * Copyright (C) 2000 - 2012 Silverpeas
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
 * "http://www.silverpeas.org/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.stratelia.webactiv.util.viewGenerator.html.arrayPanes;

import com.stratelia.webactiv.util.viewGenerator.html.SimpleGraphicElement;

/**
 * Class declaration
 * @author
 */
public class ArrayCellRadio extends ArrayCell implements SimpleGraphicElement {

  // -----------------------------------------------------------------------------------------------------------------
  // Attributs
  // -----------------------------------------------------------------------------------------------------------------
  private String name;
  private String value = null;
  private boolean checked = false;
  private String cellAlign = null;
  private String action = null; // Action javaScript

  private String syntax = "";

  // -----------------------------------------------------------------------------------------------------------------
  // Constructeur
  // -----------------------------------------------------------------------------------------------------------------

  /**
   * Constructor declaration
   * @param name
   * @param value
   * @param checked
   * @param line
   * @see
   */
  public ArrayCellRadio(String name, String value, boolean checked,
      ArrayLine line) {
    super(line);
    this.name = name;
    this.value = value;
    this.checked = checked;
  }

  public String getCellAlign() {
    return cellAlign;
  }

  /**
   * Method declaration
   * @param cellAlign
   * @see
   */
  public void setCellAlign(String cellAlign) {
    this.cellAlign = cellAlign;
  }

  /**
   * Method declaration
   * @return
   * @see
   */
  public String getName() {
    return name;
  }

  /**
   * Method declaration
   * @return
   * @see
   */
  public String getValue() {
    return value;
  }

  /**
   * Method declaration
   * @return
   * @see
   */
  public boolean getChecked() {
    return checked;
  }

  /**
   * Get the JavaScript action
   */
  public String getAction() {
    return action;
  }

  /**
   * Set the JavaScript action e.g. "onLoad=doOnLoad();"
   * @param strAction the event name and the action
   */
  public void setAction(String strAction) {
    action = strAction;
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

    syntax += " <input style=\"background-color:#FFFFFF;\" type=\"radio\" name=\"";

    // param name
    if (getName() == null) {
      syntax += "radio\" value=\"";
    } else {
      syntax += getName() + "\" value=\"";
    }

    // param value
    if (getValue() == null) {
      syntax += "radio\"";
    } else {
      syntax += getValue() + "\"";
    }

    // param action JavaScript
    if (getAction() != null) {
      syntax += " " + getAction();
    }

    // param activate
    if (getChecked() == true) {
      syntax += " checked";
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

    result += " >";

    result += getSyntax();

    result += "</td>\n";
    return result;
  }

}
