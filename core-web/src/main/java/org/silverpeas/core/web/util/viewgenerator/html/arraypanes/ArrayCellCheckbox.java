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

import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.web.util.viewgenerator.html.SimpleGraphicElement;

public class ArrayCellCheckbox extends ActionableArrayCell implements SimpleGraphicElement {

  private final boolean checked;
  private boolean readOnly = false;
  private String syntax = "";
  private final String value;

  public ArrayCellCheckbox(String name, String value, boolean checked,
      ArrayLine line) {
    super(name, line);
    this.value = value;
    this.checked = checked;
  }

  public String getValue() {
    return value;
  }

  public boolean isChecked() {
    return checked;
  }

  public boolean isReadOnly() {
    return readOnly;
  }

  public void setReadOnly(boolean readOnly) {
    this.readOnly = readOnly;
  }

  @Override
  public String getSyntax() {

    syntax += " <input type=\"checkbox\" name=\"";

    // param name
    if (StringUtil.isNotDefined(getName())) {
      syntax += "checkbox\" value=\"";
    } else {
      syntax += getName() + "\" value=\"";
    }

    // param value
    if (StringUtil.isNotDefined(getValue())) {
      syntax += "checkbox\"";
    } else {
      syntax += getValue() + "\"";
    }

    if (isReadOnly()) {
      syntax += " disabled";
    }

    // param activate
    if (isChecked()) {
      syntax += " checked";
    }

    if (StringUtil.isDefined(getAction())) {
      syntax += " " + getAction();
    }

    syntax += ">";

    return syntax;
  }

}
