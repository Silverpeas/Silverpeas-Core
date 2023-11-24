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
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.web.util.viewgenerator.html.arraypanes;

import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.web.util.viewgenerator.html.GraphicElementFactory;
import org.silverpeas.core.web.util.viewgenerator.html.SimpleGraphicElement;
import org.silverpeas.core.web.util.viewgenerator.html.iconpanes.IconPane;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

/** A row in an HTML array
 * @author squere
 */
public class ArrayLine implements SimpleGraphicElement, Comparable<ArrayLine> {

  private final List<SimpleGraphicElement> cells;
  private final ArrayPane pane;
  private String css = null;
  private String id = null;

  private final List<ArrayLine> subLines;

  public ArrayLine(ArrayPane pane) {
    cells = new ArrayList<>();
    subLines = new ArrayList<>();
    this.pane = pane;
  }

  public void addSubline(ArrayLine subline) {
    subLines.add(subline);
  }

  public void setStyleSheet(String css) {
    this.css = css;
  }

  public String getStyleSheet() {
    return css;
  }

  /**
   * The text of the cell is computed from:
   * <ul>
   *   <li>a text if it is defined</li>
   *   <li>a {@link Function} applied to the given instance parameter otherwise.</li>
   * </ul>
   * About the function, it takes in input the given instance and the result must be a
   * {@link String}.<br>
   * The advantage of this way of use is that the text is computed only when the line is
   * displayed. So that can be see as a lazy computation.<br>
   * Once the text
   * computation is done, it is cached so that the computation is performed at most one time.
   * @param text a text.
   * @param instance the instance in input of the function, ignored if text is not null.
   * @param lazyText the function to apply to the instance,  ignored if text is not null.
   * @return itself.
   */
  private <T> ArrayCellText addArrayCellText(String text, T instance,
      Function<T, String> lazyText) {
    ArrayCellText cell = (text == null && instance != null && lazyText != null) ?
        new ArrayCellText(instance, lazyText, this) : new ArrayCellText(text, this);
    cells.add(cell);
    return cell;
  }

  /**
   * The text of the cell is computed from a {@link Function} applied to the given
   * instance parameter.<br>
   * The function takes in input the given instance and the result must be a {@link String}.<br>
   * The advantage of this way of use is that the text is computed only when the line is displayed.
   * So that can be see as a lazy computation.<br>
   * Once the text computation is done, it is cached so that the computation is performed at most
   * one time.
   * @param instance the instance in input of the function.
   * @param lazyText the function to apply to the instance.
   * @return an array cell text.
   */
  public <T> ArrayCellText addArrayCellText(T instance, Function<T, String> lazyText) {
    return addArrayCellText(null, instance, lazyText);
  }

  public ArrayCellText addArrayCellText(String text) {
    return addArrayCellText(text, null, null);
  }

  public ArrayCellText addArrayCellText(int number) {
    return addArrayCellText(Integer.toString(number));
  }

  public ArrayCellText addArrayCellText(float number) {
    return addArrayCellText(Float.toString(number));
  }

  public ArrayCellLink addArrayCellLink(String text, String link) {
    ArrayCellLink cell = new ArrayCellLink(text, link, this);

    cells.add(cell);
    return cell;
  }

  public ArrayCellLink addArrayCellLink(String text, String link, String target) {
    ArrayCellLink cell = new ArrayCellLink(text, link, this);
    cell.setTarget(target);
    cells.add(cell);
    return cell;
  }

  public void addArrayEmptyCell() {
    ArrayEmptyCell cell = new ArrayEmptyCell();

    cells.add(cell);
  }

  public ArrayCellIconPane addArrayCellIconPane(IconPane iconPane) {
    ArrayCellIconPane cell = new ArrayCellIconPane(iconPane, this);

    cells.add(cell);
    return cell;
  }

  /**
   * This method permit to add a input box without format in the arrayPane. Input box parameters are
   * name and value
   * @param name name of the input
   * @param value value of the input
   */
  public ArrayCellInputText addArrayCellInputText(String name, String value) {
    ArrayCellInputText cell = new ArrayCellInputText(name, value, this);

    cells.add(cell);
    return cell;
  }

  /**
   * To add an ArrayCellInputText to an ArrayLine
   * @param cell the cell to add
   * @return the added cell
   */
  public ArrayCellInputText addArrayCellInputText(ArrayCellInputText cell) {
    cells.add(cell);
    return cell;
  }

  /**
   * This method permits to add a select drop-down box without format in the arrayPane. Select box
   * parameters are name, labels and values
   * @param name The name of the element
   * @param labels an array of Labels to display
   * @param values an array of Values to return
   * @return an ArrayCellSelect object.
   */
  public ArrayCellSelect addArrayCellSelect(String name, String[] labels,
      String[] values) {
    ArrayCellSelect cell = new ArrayCellSelect(name, labels, values,
        this);

    cells.add(cell);
    return cell;
  }

  public ArrayCellSelect addArrayCellSelect(String name, List<String> values) {
    ArrayCellSelect cell = new ArrayCellSelect(name, values, this);
    cells.add(cell);
    return cell;
  }

  /**
   * This method permit to add a button in the arrayPane. Button parameters are name, value, and if
   * the button is disabled or not.
   * @param name the button name.
   * @param value the button value.
   * @param activate if the button is enabled or not.
   */
  @SuppressWarnings("unused")
  public ArrayCellButton addArrayCellButton(String name, String value,
      boolean activate) {
    ArrayCellButton cell = new ArrayCellButton(name, value, activate, this);

    cells.add(cell);
    return cell;
  }

  public ArrayCellRadio addArrayCellRadio(String name, String value,
      boolean checked) {
    ArrayCellRadio cell = new ArrayCellRadio(name, value, checked, this);

    cells.add(cell);
    return cell;
  }

  public ArrayCellCheckbox addArrayCellCheckbox(String name, String value, String onchange,
      boolean checked) {
    ArrayCellCheckbox cell = new ArrayCellCheckbox(name, value, checked, this);
    if (StringUtil.isDefined(onchange)) {
      cell.setAction(onchange);
    }

    cells.add(cell);
    return cell;
  }

  public SimpleGraphicElement getCellAt(int column) {
    try {
      return cells.get(column - 1);
    } catch (Exception e) {
      return null;
    }
  }

  public static String printPseudoColumn() {
    return ("<td><img src=\"" + GraphicElementFactory.getIconsPath() + "/1px.gif\" width=\"2\" height=\"2\" alt=\"\"/></td>");
  }

  public String print() {
    StringBuilder result = new StringBuilder();

    if (StringUtil.isDefined(getId())) {
      result.append("<tr id=\"").append(getId()).append("\">");
    } else {
      result.append("<tr>");
    }
    for (SimpleGraphicElement element : cells) {
      result.append(element.print());
    }
    result.append("</tr>\n");
    for (ArrayLine line : subLines) {
      result.append(line.print());
    }
    return result.toString();
  }

  /**
   * This method works like the {@link #print()} method, but inserts pseudo columns after each
   * column. This is useful when a 0 cell spacing is used.
   */
  public String printWithPseudoColumns() {
    StringBuilder result = new StringBuilder();

    result.append("<tr>\n");
    result.append(printPseudoColumn());
    for (SimpleGraphicElement element : cells) {
      result.append(element.print());
      result.append(printPseudoColumn());
    }
    result.append("</tr>\n");
    for (ArrayLine line : subLines) {
      result.append(line.printWithPseudoColumns());
    }
    return result.toString();
  }

  /**
   * Compares this array line with the specified one by the cell in the same sortable column.
   * </p>
   * This comparing function is not about array line equality meaning the following
   * property <code>(x.compareTo(y)==0) == (x.equals(y))</code> is broken.
   */
  public int compareTo(@Nonnull final ArrayLine other) {
    Objects.requireNonNull(other);
    if (pane.getColumnToSort() == 0) {
      return 0;
    }
    int sort = pane.getColumnToSort();

    if (sort < 0) {
      sort = -sort;
    }
    Object cell = getCellAt(sort);

    if (cell == null)
      return 0;

    if (!(cell instanceof Comparable)) {
      return 0;
    }
    //noinspection unchecked,rawtypes
    sort = ((Comparable) cell).compareTo(other.getCellAt(sort));
    if (pane.getColumnToSort() < 0) {
      return -sort;
    } else {
      return sort;
    }
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof ArrayLine)) {
      return false;
    }

    return hashCode() == o.hashCode();
  }

  /**
   * Computes the hash code of this ArrayLine. Two same array lines have the same hash code.
   * @return the hash code of this ArrayLine.
   */
  @Override
  public int hashCode() {
    HashCodeBuilder builder = new HashCodeBuilder();
    for (SimpleGraphicElement element : cells) {
      builder.append(element.hashCode());
    }
    return builder.toHashCode();
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getId() {
    return id;
  }

}
