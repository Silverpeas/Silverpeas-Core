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

import org.silverpeas.core.web.util.viewgenerator.html.SimpleGraphicElement;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class ArrayCellSelect extends ActionableArrayCell implements SimpleGraphicElement {

  private final ArrayList<String> values = new ArrayList<>();
  private String size = null;
  private final ArrayList<String> labels = new ArrayList<>();
  private final ArrayList<Integer> selected = new ArrayList<>();
  private String color = null;
  private String bgcolor = null;
  private String textAlign = null;
  private boolean readOnly = false;
  private boolean multiselect = false;
  private final StringBuilder syntax = new StringBuilder();

  public ArrayCellSelect(String name, String[] labels,
      String[] values, ArrayLine line) {
    super(name, line);
    this.values.addAll(Arrays.asList(values));
    this.labels.addAll(Arrays.asList(labels));
  }

  public ArrayCellSelect(String name, List<String> values, ArrayLine line) {
    super(name, line);
    this.values.addAll(values);
    labels.addAll(values);
  }

  public String[] getSelectedValues() {
    ArrayList<String> selectedValues = new ArrayList<>();

    for (Integer aSelected : selected) {
      selectedValues.add(values.get(aSelected));
    }

    return selectedValues.toArray(new String[0]);
  }

  public void setSelectedValues(String[] selectedValues) {
    selected.clear();
    // Verify that the provided values exist among all values
    for (String selectedValue : selectedValues) {
      int index = values.indexOf(selectedValue);
      if (index != -1) {
        selected.add(index);
      }
    }
  }

  public String getSize() {
    return size;
  }

  public void setSize(String strSize) {
    size = strSize;
  }

  public boolean isMultiselect() {
    return multiselect;
  }

  public void setMultiselect(boolean fMultiselect) {
    multiselect = fMultiselect;
  }

  public String getColor() {
    return color;
  }

  public void setColor(String strColor) {
    color = strColor;
  }

  public String getBgcolor() {
    return bgcolor;
  }

  public void setBgcolor(String strBgcolor) {
    bgcolor = strBgcolor;
  }

  public String getTextAlign() {
    return textAlign;
  }

  public void setTextAlign(String strTextAlign) {
    textAlign = strTextAlign;
  }

  public boolean isReadOnly() {
    return readOnly;
  }

  public void setReadOnly(boolean fReadOnly) {
    readOnly = fReadOnly;
  }

  @Override
  public String getSyntax() {
    syntax.append(" <select name=\"");
    // param name
    if (getName() == null) {
      syntax.append("selectfield\"");
    } else {
      syntax.append(getName()).append("\"");
    }

    // param size
    if (getSize() != null) {
      syntax.append(" size=\"").append(getSize()).append("\"");
    }

    // set Style
    syntax.append(" style=\"");

    // param likeText
    if (isReadOnly()) {
      syntax.append("border: 1 solid rgb(255,255,255); ");
    }

    // param textAlign
    if (getTextAlign() != null) {
      syntax.append("text-align:").append(getTextAlign()).append(";");
    }

    // param color
    if (getColor() != null) {
      syntax.append(" color:").append(getColor()).append(";");
    }

    // param background color
    if (getBgcolor() != null) {
      syntax.append(" background-color:").append(getBgcolor()).append(";");
    }

    syntax.append("\"");

    // param action JavaScript
    if (getAction() != null) {
      syntax.append(" ").append(getAction());
    }

    // readOnly ???
    if (isReadOnly()) {
      syntax.append(" readOnly");
    }

    // multiple ???
    if (isMultiselect()) {
      syntax.append(" multiple");
    }

    syntax.append(">");

    // Options
    generateOptions(syntax);

    syntax.append("\n</select>");
    return syntax.toString();
  }

  private void generateOptions(StringBuilder output) {
    int iSelected = -1;
    Iterator<Integer> iterSelected = selected.iterator();
    if (iterSelected.hasNext())
      iSelected = iterSelected.next();

    for (int i = 0; i < labels.size(); i++) {
      output.append("\n<option value=\"").append(values.get(i)).append("\"");

      if (i == iSelected) {
        output.append(" selected");

        if (iterSelected.hasNext())
          iSelected = iterSelected.next();
      }

      output.append(">").append(labels.get(i)).append("</option>");
    }
  }
}
