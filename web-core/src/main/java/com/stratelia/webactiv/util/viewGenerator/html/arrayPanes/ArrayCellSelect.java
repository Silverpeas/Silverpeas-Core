/**
 * Copyright (C) 2000 - 2011 Silverpeas
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

package com.stratelia.webactiv.util.viewGenerator.html.arrayPanes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

import com.stratelia.webactiv.util.viewGenerator.html.SimpleGraphicElement;

/**
 * @author jboulet
 * @version
 */

public class ArrayCellSelect extends ArrayCell implements SimpleGraphicElement {

  // -----------------------------------------------------------------------------------------------------------------
  // Attributs
  // -----------------------------------------------------------------------------------------------------------------
  private String name;
  private ArrayList<String> values = new ArrayList<String>();
  private String size = null;
  private ArrayList<String> labels = new ArrayList<String>();
  private ArrayList<Integer> selected = new ArrayList<Integer>();
  private String cellAlign = null;
  private String color = null;
  private String bgcolor = null;
  private String textAlign = null;
  private boolean readOnly = false;
  private boolean multiselect = false;
  private String action = null; // Action javaScript
  private StringBuffer syntax = new StringBuffer();

  // -----------------------------------------------------------------------------------------------------------------
  // Constructeur
  // -----------------------------------------------------------------------------------------------------------------

  /**
   * Constructor declaration
   * @param strName
   * @param astrLabels
   * @param astrValues
   * @param line
   * @see
   */
  public ArrayCellSelect(String strName, String[] astrLabels,
      String[] astrValues, ArrayLine line) {
    super(line);
    name = strName;
    values.addAll(Arrays.asList(astrValues));
    labels.addAll(Arrays.asList(astrLabels));
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
  public String[] getSelectedValues() {
    ArrayList<String> selectedValues = new ArrayList<String>();
    Iterator<Integer> iterator = selected.iterator();

    while (iterator.hasNext()) {
      selectedValues.add(values.get(iterator.next().intValue()));
    }

    return (String[]) selectedValues.toArray(new String[selectedValues.size()]);
  }

  /**
   * @return
   */
  public void setSelectedValues(String[] astrSelectedValues) {
    int index = -1;

    selected.clear();

    // Verify that the provided values exist among all values
    for (int i = 0; i < astrSelectedValues.length; i++) {
      index = values.indexOf(astrSelectedValues[i]);
      if (index != -1)
        selected.add(new Integer(index));
    }
  }

  /**
   * @return
   */
  public String getSize() {
    return size;
  }

  /**
   * @param strSize
   */
  public void setSize(String strSize) {
    size = strSize;
  }

  /**
   * @return
   */
  public boolean getMultiselect() {
    return multiselect;
  }

  /**
   * @param maxlength
   */
  public void setMultiselect(boolean fMultiselect) {
    multiselect = fMultiselect;
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
  public void setColor(String strColor) {
    color = strColor;
  }

  /**
   * @return
   */
  public String getBgcolor() {
    return bgcolor;
  }

  /**
   * @param strBgcolor
   */
  public void setBgcolor(String strBgcolor) {
    bgcolor = strBgcolor;
  }

  /**
   * @return
   */
  public String getTextAlign() {
    return textAlign;
  }

  /**
   * @param strTextAlign
   */
  public void setTextAlign(String strTextAlign) {
    textAlign = strTextAlign;
  }

  /**
   * @return
   */
  public String getAction() {
    return action;
  }

  /**
   * @param strAction
   */
  public void setAction(String strAction) {
    action = strAction;
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
  public void setReadOnly(boolean fReadOnly) {
    readOnly = fReadOnly;
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
    Iterator<Integer> iterSelected = selected.iterator();
    int iSelected = -1;

    syntax.setLength(0);

    syntax.append(" <select name=\"");

    // param name
    if (getName() == null) {
      syntax.append("selectfield\"");
    } else {
      syntax.append(getName());
      syntax.append("\"");
    }

    // param size
    if (getSize() != null) {
      syntax.append(" size=\"");
      syntax.append(getSize());
      syntax.append("\"");
    }

    // set Style
    syntax.append(" style=\"");

    // param likeText
    if (getReadOnly() == true) {
      syntax.append("border: 1 solid rgb(255,255,255); ");
    }

    // param textAlign
    if (getTextAlign() != null) {
      syntax.append("text-align:");
      syntax.append(getTextAlign());
      syntax.append(";");
    }

    // param color
    if (getColor() != null) {
      syntax.append(" color:");
      syntax.append(getColor());
      syntax.append(";");
    }

    // param background color
    if (getBgcolor() != null) {
      syntax.append(" background-color:");
      syntax.append(getBgcolor());
      syntax.append(";");
    }

    syntax.append("\"");

    // param action JavaScript
    if (getAction() != null) {
      syntax.append(" ");
      syntax.append(getAction());
    }

    // readOnly ???
    if (getReadOnly() == true) {
      syntax.append(" readOnly");
    }

    // multiple ???
    if (getMultiselect() == true) {
      syntax.append(" multiple");
    }

    syntax.append(">");

    // Options
    if (iterSelected.hasNext())
      iSelected = iterSelected.next().intValue();

    for (int i = 0; i < labels.size(); i++) {
      syntax.append("\n<option value=\"");
      syntax.append(values.get(i));
      syntax.append("\"");

      if (i == iSelected) {
        syntax.append(" selected");

        if (iterSelected.hasNext())
          iSelected = ((Integer) iterSelected.next()).intValue();
      }

      syntax.append(">");
      syntax.append(labels.get(i));
      syntax.append("</option>");
    }

    syntax.append("\n</select>");
    return syntax.toString();
  }

  // -----------------------------------------------------------------------------------------------------------------

  /**
   * Method declaration
   * @return
   * @see
   */
  public String print() {
    StringBuffer result = new StringBuffer("<td ");

    if (getCellAlign() != null) {
      if (getCellAlign().equalsIgnoreCase("center")
          || getCellAlign().equalsIgnoreCase("right")) {
        result.append(" align=\"");
        result.append(getCellAlign());
        result.append("\"");
      }
    }

    result.append(" class=\"");
    result.append(getStyleSheet());
    result.append("\">");

    result.append(getSyntax());

    result.append("</td>\n");
    return result.toString();
  }

}
