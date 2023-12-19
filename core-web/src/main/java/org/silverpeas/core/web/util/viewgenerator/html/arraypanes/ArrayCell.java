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

public abstract class ArrayCell {
  private final ArrayLine line;

  private final String name;

  private String css = null;

  public ArrayCell(String name, ArrayLine line) {
    this.name = name;
    this.line = line;
  }

  public void setStyleSheet(String css) {
    this.css = css;
  }

  public String getStyleSheet() {
    if (css != null) {
      return css;
    }
    if (line.getStyleSheet() != null) {
      return line.getStyleSheet();
    }
    return "ArrayCell";
  }

  public String getName() {
    return name;
  }

  public abstract String getSyntax();

  public final String print() {
    String result = "<td";
    if (StringUtil.isDefined(getStyleSheet())) {
      result += " class=\"" + getStyleSheet() + "\"";
    }
    result += ">";
    result += getSyntax();
    result += "</td>\n";
    return result;
  }

}
