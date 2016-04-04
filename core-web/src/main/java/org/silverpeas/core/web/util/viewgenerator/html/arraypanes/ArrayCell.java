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

/**
 * Class declaration
 * @author
 */
public class ArrayCell {
  final static public int CELLSORT_CASE_INSENSITIVE = 1;
  final static public int CELLSORT_CASE_SENSITIVE = 2;

  /**
   * the default sort mode, that may or may not be interpreted by the descendants of this class,
   * depending on their contents. They could define other modes, but the most common sould
   * reasonnably be put in here
   */
  protected int m_SortMode = CELLSORT_CASE_SENSITIVE;
  private ArrayLine line;
  private String css = null;

  /**
   * Constructor declaration
   * @param line
   * @see
   */
  public ArrayCell(ArrayLine line) {
    this.line = line;
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
    if (css != null) {
      return css;
    }
    if (line.getStyleSheet() != null) {
      return line.getStyleSheet();
    }
    return "ArrayCell";
  }

  /**
   * Method declaration
   * @param mode
   * @see
   */
  public void setSortMode(int mode) {
    m_SortMode = mode;
  }

}
