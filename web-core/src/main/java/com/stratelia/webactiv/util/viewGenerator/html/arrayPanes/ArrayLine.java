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

import java.util.ArrayList;
import java.util.List;

import com.silverpeas.util.StringUtil;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.util.viewGenerator.html.GraphicElementFactory;
import com.stratelia.webactiv.util.viewGenerator.html.SimpleGraphicElement;
import com.stratelia.webactiv.util.viewGenerator.html.iconPanes.IconPane;

/**
 * @author squere
 * @version
 */
public class ArrayLine implements SimpleGraphicElement, Comparable {

  private List<SimpleGraphicElement> cells = null;
  private ArrayPane pane;
  private String css = null;
  private String id = null;

  private List<ArrayLine> sublines = null;

  /**
   * Constructor declaration
   * @param pane
   * @see
   */
  public ArrayLine(ArrayPane pane) {
    cells = new ArrayList<SimpleGraphicElement>();
    sublines = new ArrayList<ArrayLine>();
    this.pane = pane;
  }

  public void addSubline(ArrayLine subline) {
    sublines.add(subline);
  }

  /**
   * Method declaration
   * @param css
   * @see
   */
  public void setStyleSheet(String css) {
    this.css = css;
  }

  /**
   * Method declaration
   * @return
   * @see
   */
  public String getStyleSheet() {
    return css;
  }

  /**
   * Method declaration
   * @param text
   * @return
   * @see
   */
  public ArrayCellText addArrayCellText(String text) {
    ArrayCellText cell = new ArrayCellText(text, this);

    if (pane != null) {
      cell.setSortMode(pane.getSortMode());
    }
    cells.add(cell);
    return cell;
  }

  public ArrayCellText addArrayCellText(int number) {
    return addArrayCellText(Integer.toString(number));
  }

  public ArrayCellText addArrayCellText(float number) {
    return addArrayCellText(Float.toString(number));
  }

  /**
   * Method declaration
   * @param text
   * @param link
   * @return
   * @see
   */
  public ArrayCellLink addArrayCellLink(String text, String link) {
    ArrayCellLink cell = new ArrayCellLink(text, link, this);

    cells.add(cell);
    return cell;
  }

  /**
   * Add an ArrayCellLink with Target
   * @author dlesimple
   * @param text
   * @param link
   * @param target
   * @return ArrayCellLink
   */
  public ArrayCellLink addArrayCellLink(String text, String link, String target) {
    ArrayCellLink cell = new ArrayCellLink(text, link, this);
    cell.setTarget(target);
    cells.add(cell);
    return cell;
  }

  /**
   * Method declaration
   * @return
   * @see
   */
  public ArrayEmptyCell addArrayEmptyCell() {
    ArrayEmptyCell cell = new ArrayEmptyCell();

    cells.add(cell);
    return cell;
  }

  /**
   * Method declaration
   * @param iconPane
   * @return
   * @see
   */
  public ArrayCellIconPane addArrayCellIconPane(IconPane iconPane) {
    ArrayCellIconPane cell = new ArrayCellIconPane(iconPane, this);

    cells.add(cell);
    return cell;
  }

  /**
   * This method permit to add a input box without format in the arrayPane. Input box parameters are
   * name and value
   * @param name
   * @param value
   */
  public ArrayCellInputText addArrayCellInputText(String name, String value) {
    ArrayCellInputText cell = new ArrayCellInputText(name, value, this);

    cells.add(cell);
    return cell;
  }

  /**
   * To add an ArrayCellInputText to an ArrayLine
   * @param cell
   * @return
   */
  public ArrayCellInputText addArrayCellInputText(ArrayCellInputText cell) {
    cells.add(cell);
    return cell;
  }

  /**
   * This method permits to add a select drop-down box without format in the arrayPane. Select box
   * parameters are name, labels and values
   * @param name The name of the element
   * @param astrLabels an array of Labels to display
   * @param astrValues an array of Values to return
   * @return an ArrayCellSelect object.
   */
  public ArrayCellSelect addArrayCellSelect(String name, String[] astrLabels,
      String[] astrValues) {
    ArrayCellSelect cell = new ArrayCellSelect(name, astrLabels, astrValues,
        this);

    cells.add(cell);
    return cell;
  }

  /**
   * This method permit to add a button in the arrayPane. Button parameters are name, value, and if
   * the button is disabled or not.
   * @param name
   * @param value
   * @param activate
   */
  public ArrayCellButton addArrayCellButton(String name, String value,
      boolean activate) {
    ArrayCellButton cell = new ArrayCellButton(name, value, activate, this);

    cells.add(cell);
    return cell;
  }

  /**
   * This method permit to add a radiobutton in the arrayPane.
   * @param name
   * @param value
   * @param checked
   */
  public ArrayCellRadio addArrayCellRadio(String name, String value,
      boolean checked) {
    ArrayCellRadio cell = new ArrayCellRadio(name, value, checked, this);

    cells.add(cell);
    return cell;
  }

  /**
   * This method permit to add a checkbox in the arrayPane.
   * @param name
   * @param value
   * @param checked
   */
  public ArrayCellCheckbox addArrayCellCheckbox(String name, String value,
      boolean checked) {
    ArrayCellCheckbox cell = new ArrayCellCheckbox(name, value, checked, this);

    cells.add(cell);
    return cell;
  }

  /**
   * Method declaration
   * @param column
   * @return
   * @see
   */
  public SimpleGraphicElement getCellAt(int column) {
    try {
      return cells.get(column - 1);
    } catch (Exception e) {
      return null;
    }
  }

  /**
   * Method declaration
   * @return
   * @see
   */
  public static String printPseudoColumn() {
    return ("<td><img src=\"" + GraphicElementFactory.getIconsPath() + "/1px.gif\" width=\"2\" height=\"2\" alt=\"\"/></td>");
  }

  /**
   * Method declaration
   * @return
   * @see
   */
  public String print() {
    String result = "";

    if (StringUtil.isDefined(getId())) {
      result += "<tr id=\""+getId()+"\">";
    } else {
      result += "<tr>";
    }
    for (SimpleGraphicElement element : cells) {
      result += element.print();
    }
    result += "</tr>\n";
    for (ArrayLine line : sublines) {
      result += line.print();
    }
    return result;
  }

  /**
   * This method works like the {@link #print()} method, but inserts pseudocolumns after each
   * column. This is useful when a 0 cellspacing is used.
   * @see printPseudoColumn()
   */
  public String printWithPseudoColumns() {
    String result = "";

    result += "<tr>\n";
    result += printPseudoColumn();
    for (SimpleGraphicElement element : cells) {
      result += element.print();
      result += printPseudoColumn();
    }
    result += "</tr>\n";
    for (ArrayLine line : sublines) {
      result += line.printWithPseudoColumns();
    }
    return result;
  }

  /**
   * Method declaration
   * @param other
   * @return
   * @see
   */
  public int compareTo(final java.lang.Object other) {
    if (pane.getColumnToSort() == 0) {
      SilverTrace.info("viewgenerator", "ArrayLine.compareTo()",
          "root.MSG_GEN_PARAM_VALUE", " columnToSort = 0 ");
      return 0;
    }
    if (!(other instanceof ArrayLine)) {
      SilverTrace.info("viewgenerator", "ArrayLine.compareTo()",
          "root.MSG_GEN_PARAM_VALUE", " other not an ArrayLine : other="
          + other.toString());
      return 0;
    }
    ArrayLine tmp = (ArrayLine) other;
    int sort = pane.getColumnToSort();

    if (sort < 0) {
      sort = -sort;
    }
    Object cell = getCellAt(sort);

    if (cell == null)
      return 0;

    if (!(cell instanceof Comparable)) {
      SilverTrace.info("viewgenerator", "ArrayLine.compareTo()",
          "root.MSG_GEN_PARAM_VALUE", " cell not Comparable : cell="
          + cell.toString());
      return 0;
    }
    sort = ((Comparable) cell).compareTo(tmp.getCellAt(sort));
    if (pane.getColumnToSort() < 0) {
      return -sort;
    } else {
      return sort;
    }
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getId() {
    return id;
  }

}
