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

/**
 * Create a new cell in an ArrayPane
 * @author cdm
 */
public class ArrayCellCheckboxTag extends AbstractArrayCellTag {

  private String name;
  private String value;
  private boolean checked;
  private boolean readOnly = false;
  private String action = "";

  @Override
  ArrayCell doCreateCell() {
    ArrayCellCheckbox checkbox = getArrayLine().addArrayCellCheckbox(name, getContentValue(value),
        action, checked);
    checkbox.setReadOnly(readOnly);
    return checkbox;
  }

  public void setName(final String name) {
    this.name = name;
  }

  public void setValue(final String value) {
    this.value = value;
  }

  public void setChecked(final boolean checked) {
    this.checked = checked;
  }

  public void setReadonly(boolean readOnly) {
    this.readOnly = readOnly;
  }

  public String getAction() {
    return action;
  }

  public void setAction(String action) {
    this.action = action;
  }
}
