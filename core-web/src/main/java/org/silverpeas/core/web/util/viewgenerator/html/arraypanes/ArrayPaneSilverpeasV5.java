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

import java.util.Collections;

import org.silverpeas.core.web.util.viewgenerator.html.GraphicElementFactory;
import org.silverpeas.core.web.util.viewgenerator.html.pagination.Pagination;

/**
 * The default implementation of ArrayPane interface.
 * @author squere
 * @version 1.0
 */
public class ArrayPaneSilverpeasV5 extends AbstractArrayPane {

  /**
   * Default constructor as this class may be instanciated by method newInstance(), constructor
   * contains no parameter. init methods must be used to initialize properly the instance.
   * @see init
   */
  public ArrayPaneSilverpeasV5() {
    // initialisation is made in init methods
  }

  /**
   * Method declaration
   * @return
   * @see
   */
  private String printPseudoColumn() {
    return ("<td><img src=\"" + GraphicElementFactory.getIconsPath() + "/1px.gif\" width=\"2\" height=\"2\" alt=\"\"/></td>");
  }

  /**
   * Method declaration
   * @return
   * @see
   */
  @Override
  public String print() {
    GraphicElementFactory gef =
        (GraphicElementFactory) getSession().getAttribute(GraphicElementFactory.GE_FACTORY_SESSION_ATT);
    Pagination pagination =
        gef.getPagination(getLines().size(), getState().getMaximumVisibleLine(), getState().getFirstVisibleLine());

    String sep = "&";
    if (isXHTML()) {
      sep = "&amp;";
    }

    String baseUrl = getUrl();
    StringBuilder url = new StringBuilder(baseUrl);
    if (baseUrl.indexOf('?') < 0) {
      url.append("?");
    } else {
      url.append(sep);
    }
    url.append(ACTION_PARAMETER_NAME).append("=ChangePage").append(sep).
        append(TARGET_PARAMETER_NAME).append("=").append(getName()).append(sep).append(
        INDEX_PARAMETER_NAME).append("=");
    pagination.setBaseURL(url.toString());

    int columnsCount = getColumns().size();

    if ((getLines().size() > 0) && (getColumnToSort() != 0) && (getColumnToSort() <= columnsCount)) {

      Collections.sort(getLines());
    }

    // when there is no cell spacing, add pseudo columns as fillers
    if (getCellSpacing() == 0) {
      columnsCount = columnsCount * 2 + 1;
    }
    StringBuilder result = new StringBuilder();
    result.append("<table width=\"98%\" cellspacing=\"0\" cellpadding=\"2\" border=\"0\" ");
    result.append("class=\"arrayPane\"><tr><td>\n").append("<table width=\"100%\" cellspacing=\"");
    result.append(getCellSpacing()).append("\" cellpadding=\"").append(getCellPadding());
    result.append("\" border=\"").append(getCellBorderWidth()).append("\"");
    result.append(" id=\"").append(getName()).append("\" class=\"tableArrayPane\"");
    result.append(" summary=\"").append(getSummary()).append("\">");
    if (getTitle() != null) {
      result.append("<caption>").append(getTitle()).append("</caption>");
    }
    if (getCellSpacing() == 0) {
      result.append("<tr>");
      result.append("<td colspan=\"").append(columnsCount).append("\">");
      result.append("<img src=\"").append(getIconsPath()).append(
          "/1px.gif\" width=\"1\" height=\"1\" alt=\"\"/>");
      result.append("</td>");
      result.append("</tr>\n");
    }
    result.append("<thead>\n");
    result.append("<tr>\n");
    if (getCellSpacing() == 0) {
      result.append(printPseudoColumn());
    }
    for (ArrayColumn column : getColumns()) {
      result.append(column.print(isXHTML()));
      if (getCellSpacing() == 0) {
        result.append(printPseudoColumn());
      }
    }
    result.append("</tr>\n").append("</thead>\n").append("<tbody>\n");
    if (getLines().isEmpty()) {
      result.append("<tr><td>&nbsp;</td></tr>\n");
    } else {

      // No need pagination
      if (getState().getMaximumVisibleLine() == -1) {
        for (ArrayLine curLine : getLines()) {
          printArrayPaneLine(result, curLine);
        }
      } else {
        // Paginate ArrayPane result
        getState().setFirstVisibleLine(pagination.getIndexForCurrentPage());
        int first = pagination.getIndexForCurrentPage();
        int lastIndex;
        if (pagination.isLastPage()) {
          lastIndex = pagination.getLastItemIndex();
        } else {
          lastIndex = pagination.getIndexForNextPage();
        }
        for (int i = first; i < lastIndex; i++) {
          printArrayPaneLine(result, getLines().get(i));
        }
      }

    }
    result.append("</tbody>\n");
    result.append("</table>\n");

    boolean paginationVisible =
        -1 != getState().getMaximumVisibleLine() && getLines().size() > getState().getMaximumVisibleLine();

    if (paginationVisible || getExportData()) {
      result.append(
          "<table width=\"100%\" border=\"0\" cellspacing=\"0\" cellpadding=\"0\">\n");
      result.append("<tr class=\"intfdcolor\">\n");
      if (paginationVisible) {
        result.append("<td align=\"center\">");
        result.append(pagination.printIndex(getPaginationJavaScriptCallback()));
        result.append("</td>");
      }
      if (getExportData()) {
        // Add export data GUI
        result.append("<td class=\"exportlinks\">");
        result.append("<div>");
        result.append(gef.getMultilang().getString("GEF.export.label")).append(" :");
        result.append("<a href=\"").append(getExportUrl()).append(
            "\"><span class=\"export csv\">");
        result.append(gef.getMultilang().getString("GEF.export.option.csv")).append("</span></a>");
        result.append("</div>");
        // Be careful we only put a unique name in the session, so we cannot export two ArrayPanes
        // which are displayed in the same page (use this.name instead of "arraypane")
        getSession().setAttribute("Silverpeas_arraypane_columns", getColumns());
        getSession().setAttribute("Silverpeas_arraypane_lines", getLines());
        result.append("</td>");
      }
      result.append("</tr>\n");
      result.append("</table>");
    }

    result.append("</td></tr></table>\n");

    if (isSortableLines()) {
      result.append(printSortJavascriptFunction());
    }

    return result.toString();
  }

  /**
   * Print an ArrayPane line
   * @param result the string builder
   * @param curLine the array pane line to print
   */
  private void printArrayPaneLine(StringBuilder result, ArrayLine curLine) {
    if (getCellSpacing() == 0) {
      result.append(curLine.printWithPseudoColumns());
    } else {
      result.append(curLine.print());
    }
  }

}