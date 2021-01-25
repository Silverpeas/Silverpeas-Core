/*
 * Copyright (C) 2000 - 2020 Silverpeas
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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.web.util.viewgenerator.html.arraypanes;

import org.apache.commons.lang3.tuple.Pair;
import org.silverpeas.core.cache.model.SimpleCache;
import org.silverpeas.core.cache.service.CacheServiceProvider;
import org.silverpeas.core.web.util.viewgenerator.html.GraphicElementFactory;
import org.silverpeas.core.web.util.viewgenerator.html.pagination.Pagination;

import javax.servlet.jsp.PageContext;
import java.util.Collections;

import static org.silverpeas.core.util.StringUtil.getBooleanValue;
import static org.silverpeas.core.web.util.viewgenerator.html.arraypanes.ArrayLinesTag.AJAX_EXPORT_PARAMETER_NAME;
import static org.silverpeas.core.web.util.viewgenerator.html.pagination.Pagination.INDEX_PARAMETER_NAME;

/**
 * The default implementation of ArrayPane interface.
 * @author squere
 * @version 1.0
 */
public class ArrayPaneSilverpeasV5 extends AbstractArrayPane {

  /**
   * Default constructor as this class may be instanciated by method newInstance(), constructor
   * contains no parameter. init methods must be used to initialize properly the instance.
   * @see #init(String, PageContext)
   */
  public ArrayPaneSilverpeasV5() {
    // initialisation is made in init methods
  }

  /**
   * Method declaration
   * @return
   *
   */
  private String printPseudoColumn() {
    return "<td><img src=\"" + GraphicElementFactory.getIconsPath() + "/1px.gif\" width=\"2\" height=\"2\" alt=\"\"/></td>";
  }

  /**
   * Method declaration
   * @return
   *
   */
  @Override
  public String print() {
    GraphicElementFactory gef = (GraphicElementFactory) getSession()
        .getAttribute(GraphicElementFactory.GE_FACTORY_SESSION_ATT);

    Pagination pagination = getPagination(getNbItems());

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

    if (!isPaginationOptimized() && !getLines().isEmpty() && (getColumnToSort() != 0) &&
        (getColumnToSort() <= columnsCount)) {
      Collections.sort(getLines());
    }

    // when there is no cell spacing, add pseudo columns as fillers
    if (getCellSpacing() == 0) {
      columnsCount = columnsCount * 2 + 1;
    }
    StringBuilder result = new StringBuilder();
    result.append("<div class=\"arrayPane\">\n").append("<table width=\"100%\" cellspacing=\"");
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
    final boolean ajaxExport = getBooleanValue(getRequest().getParameter(AJAX_EXPORT_PARAMETER_NAME));
    if (getLines().isEmpty() || ajaxExport) {
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
        if (isPaginationOptimized()) {
          getLines().forEach(l -> printArrayPaneLine(result, l));
        } else {
          final Pair<Integer, Integer> indexes = pagination.getStartLastIndexes();
          final int firstIndex = indexes.getLeft();
          final int lastIndex = indexes.getRight();
          for (int i = firstIndex; i < lastIndex; i++) {
            printArrayPaneLine(result, getLines().get(i));
          }
        }
      }
    }
    result.append("</tbody>\n");

    boolean paginationVisible = -1 != getState().getMaximumVisibleLine();

    if (paginationVisible || getExportData()) {
      result.append("<tfoot class=\"footerNav\">");
      result.append("<td colspan=\"").append(columnsCount).append("\">\n");
      if (paginationVisible) {
        result.append(pagination.printIndex(getPaginationJavaScriptCallback()));
      }
      if (getExportData()) {
        // Add export data GUI
        result.append("<div class=\"exportlinks\">");
        result.append("<a href=\"").append(getExportUrl()).append(
            "\"><span class=\"export csv\">");
        result.append(gef.getMultilang().getString("GEF.export.option.csv")).append("</span></a>");
        result.append("</div>");
        // Be careful we only put a unique name in the session, so we cannot export two ArrayPanes
        // which are displayed in the same page (use this.name instead of "arraypane")
        SimpleCache cache = CacheServiceProvider.getSessionCacheService().getCache();
        cache.put("Silverpeas_arraypane" + CACHE_COLUMNS_KEY_SUFFIX, getColumns());
        cache.put("Silverpeas_arraypane" + CACHE_LINES_KEY_SUFFIX, getLines());
        result.append("</div>");
      }
      result.append("</td>\n");
      result.append("</tfoot>");
    }

    result.append("</table>\n");
    result.append("</div>\n");

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
