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

import javax.servlet.http.HttpSession;
import javax.servlet.jsp.PageContext;

import org.silverpeas.core.silvertrace.SilverTrace;
import org.silverpeas.core.web.util.viewgenerator.html.arraypanes.pagination.WADataPage;
import org.silverpeas.core.web.util.viewgenerator.html.arraypanes.pagination.WADataPaginator;
import org.silverpeas.core.web.util.viewgenerator.html.arraypanes.pagination.WAItem;

/**
 * The default implementation of ArrayPane interface.
 * @author squere
 * @version 1.0
 */
public class ArrayPaneWithDataSource extends AbstractArrayPane {

  WADataPaginator m_DataSource = null;

  /**
   * This pseudo array line is used for compatibility with old cell code, only used when working in
   * Data Source Mode.
   */
  ArrayLine m_ArrayLine = null;

  /**
   * Generic class to display a typical WA array table pane. A unique name identifier is to be used
   * in html pages for this array specific actions (exemple : sort on a specific column)
   * @param name A unique name in the page to display
   */
  public ArrayPaneWithDataSource(String name, PageContext pageContext) {
    if (pageContext != null) {
      init(name, null, pageContext.getRequest(), pageContext.getSession());
    } else {
      init(name, null, null, null);
    }
  }

  /**
   * Constructor declaration
   * @param name
   * @param request
   * @param session
   * @see
   */
  public ArrayPaneWithDataSource(String name,
      javax.servlet.ServletRequest request, HttpSession session) {
    init(name, null, request, session);
  }

  /**
   * Method declaration
   * @param p
   * @see
   */
  public void setDataSource(WADataPaginator p) {
    m_DataSource = p;
    m_ArrayLine = new ArrayLine(this);
  }

  /**
   * Constructor declaration
   * @param name
   * @param url
   * @param request
   * @param session
   * @see
   */
  public ArrayPaneWithDataSource(String name, String url,
      javax.servlet.ServletRequest request, HttpSession session) {
    init(name, url, request, session);
  }

  /**
   * Method declaration
   * @return
   * @see
   */
  private String printPseudoColumn() {
    return "<td>&nbsp;</td>";
  }

  /**
   * Method declaration
   * @return
   * @see
   */
  public String print() {
    if (m_DataSource == null) {
      return standardPrint();
    } else {
      return dataSourcePrint();
    }
  }

  /**
   * Method declaration
   * @return
   * @see
   */
  private String dataSourcePrint() {
    int columnsCount = 0;
    int sortCol = 0;
    // load data
    WADataPage page = null;
    String pageActionString = "none";

    try {
      columnsCount = m_DataSource.getHeader().getFieldCount();
      sortCol = getColumnToSort();
      if (sortCol != 0) {
        if (sortCol < 0) {
          sortCol = -sortCol;
        }
        m_DataSource.getHeader().toggleFieldSortOrder(sortCol - 1);
        setColumnToSort(0);
      }
      if (getState() != null) {
        if (getState().getFirstVisibleLine() > 0) {
          pageActionString = "next";
          page = m_DataSource.getNextPage();
        } else if (getState().getFirstVisibleLine() < 0) {
          pageActionString = "previous";
          page = m_DataSource.getPreviousPage();
        } else {
          pageActionString = "current";
          page = m_DataSource.getCurrentPage();
          if (page == null) {
            pageActionString = "first";
            page = m_DataSource.getFirstPage();
          }
        }
        getState().setFirstVisibleLine(0);
      } else {
        pageActionString = "next";
        page = m_DataSource.getNextPage();
      }

    } catch (Exception x) {
      SilverTrace.error("viewgenerator",
          "ArrayPaneWithDataSource.dataSourcePrint()",
          "viewgenerator.EX_CANT_LOAD_PAGE");
      return "";
    }

    if (page == null) {
      return "";
    }
    int pageNumber = m_DataSource.getCurrentPageNumber();
    int firstItemNumber = page.getStartItemDocumentIndex();
    int lastItemNumber = page.getEndItemDocumentIndex();
    int itemCount = m_DataSource.getItemCount();
    int pageItemCount = page.getItemCount();

    String result = "";

    result +=
        "<table width=\"98%\" class=\"ArrayColumn\" cellspacing=0 cellpadding=2 border=0><tr><td>\n";
    result += "<table bgcolor=\"ffffff\" width=\"100%\" cellspacing=\""
        + getCellSpacing() + "\" cellpadding=\"" + getCellPadding()
        + "\" border=\"" + getCellBorderWidth() + "\">";
    if (getTitle() != null) {
      result += "<tr>";
      result += "<td class=\"txttitrecol\" colspan=\"" + columnsCount + "\">";
      result += getTitle();
      result += "</td>";
      result += "</tr>\n";
    }
    result += "<tr>";
    for (int i = 0; i < columnsCount; i++) {
      ArrayColumn ac = new ArrayColumn(m_DataSource.getHeader()
          .getFieldDisplayName(i), i + 1, this);
      String fra = m_DataSource.getHeader().getFieldRoutingAddress(i);

      if (fra == null) {
        ac.setRoutingAddress(getUrl());
      } else {
        ac.setRoutingAddress(fra);
        if ("".equals(fra)) {
          ac.setSortable(false);
        }
      }
      if (getRequest() != null) {
        result += ac.print();
      }
      if (getCellSpacing() == 0) {
        result += printPseudoColumn();
      }
    }
    result += "</tr>\n";
    for (int i = 0; i < pageItemCount; i++) {
      WAItem item = null;

      if (i == 0) {
        item = page.getFirstItem();
      } else {
        item = page.getNextItem();
      }
      result += "<tr id=\"" + i + "\">\n";
      result = result + "<!-- column count is + " + columnsCount + " -->\n";
      for (int j = 0; j < columnsCount; j++) {
        String name = m_DataSource.getHeader().getFieldName(j);
        String value = item.getFieldByName(name);
        String anchor = item.getAnchorByName(name);
        String style = item.getStyle();

        if (anchor == null) {
          ArrayCellText ct = new ArrayCellText(value, m_ArrayLine);

          if (style != null) {
            ct.setStyleSheet(style);
          }
          result += ct.print();
        } else {
          ArrayCellLink ct = new ArrayCellLink(value, anchor, m_ArrayLine);

          if (style != null) {
            ct.setStyleSheet(style);
          }
          result += ct.print();
        }
        if (getCellSpacing() == 0) {
          result += new ArrayEmptyCell().print();
        }
      }
      result += "</tr>\n";
    }
    result += "</table>\n";

    if (-1 != getState().getMaximumVisibleLine()) {
      String iconPath = getIconsPath();

      result +=
          "<table width=\"100%\" border=\"0\" cellspacing=\"0\" cellpadding=\"0\" class=\"ArrayColumn\">\n"
              +
              "<tr align=\"center\" bgcolor=\"#999999\">\n"
              + "<td><img src=\""
              + iconPath
              + "1px.gif\" width=\"1\" height=\"1\" alt=\"\"/></td>\n"
              + "</tr>\n"
              + "<tr align=\"center\" bgcolor=\"#FFFFFF\">\n"
              + "<td><img src=\""
              + iconPath
              + "1px.gif\" width=\"1\" height=\"1\" alt=\"\"/></td>\n"
              + "</tr>\n"
              + "<tr align=\"center\"> \n"
              + "<td class=\"ArrayNavigation\" height=\"20\">";

      if (pageNumber > 0) {
        result += "<a class=\"ArrayNavigation\" href=\"";
        String url = getUrl();

        if (!url.contains("?")) {
          url += "?";
        } else {
          url += "&";
        }
        url += ACTION_PARAMETER_NAME + "=Previous&" + TARGET_PARAMETER_NAME
            + "=" + getName();
        result += url + "\"><< Pr&eacute;c&eacute;dent </a>  | ";
      }
      if (firstItemNumber != lastItemNumber) {
        result += (firstItemNumber + 1) + " - " + (lastItemNumber);
      } else {
        result += (firstItemNumber + 1);
      }
      result += " / " + itemCount;
      if (lastItemNumber + 1 < itemCount) {
        result += " | <a class=\"ArrayNavigation\" href=\"";
        String url = getUrl();

        if (!url.contains("?")) {
          url += "?";
        } else {
          url += "&";
        }
        url += ACTION_PARAMETER_NAME + "=Next&" + TARGET_PARAMETER_NAME + "="
            + getName();
        result += url + "\">Suivant >></a>";
      }

      result += "</td>" + "</tr>\n"
          + "<tr align=\"center\" bgcolor=\"#999999\">\n" + "<td><img src=\""
          + iconPath + "1px.gif\" width=\"1\" height=\"1\" alt=\"\"/></td>\n" + "</tr>\n"
          + "<tr align=\"center\" bgcolor=\"#666666\">\n" + "<td><img src=\""
          + iconPath + "1px.gif\" width=\"1\" height=\"1\" alt=\"\"/></td>\n" + "</tr>\n"
          + "</table>";

    }
    result += "</td></tr></table>\n";
    return result;
  }

  /**
   * Default print mode entirely identical to ArrayPaneWA, used when the data source wasn't
   * specified
   */
  private String standardPrint() {
    int first = -1;
    int last = -1;

    if (getColumnToSort() != 0) {
      Collections.sort(getLines());
    }

    int columnsCount = getColumns().size();

    // when there is no cell spacing, add pseudo columns as fillers
    if (getCellSpacing() == 0) {
      columnsCount *= 2;
    }
    String result = "";

    result +=
        "<table width=\"98%\" class=\"ArrayColumn\" cellspacing=0 cellpadding=2 border=0><tr><td>\n";
    result += "<table bgcolor=\"ffffff\" width=\"100%\" cellspacing=\""
        + getCellSpacing() + "\" cellpadding=\"" + getCellPadding()
        + "\" border=\"" + getCellBorderWidth() + "\">";
    if (getTitle() != null) {
      result += "<tr>";
      result += "<td class=\"txttitrecol\" colspan=\"" + columnsCount + "\">";
      result += getTitle();
      result += "</td>";
      result += "</tr>\n";
    }
    result += "<tr>";
    for (ArrayColumn column : getColumns()) {
      result += column.print(isXHTML());
      if (getCellSpacing() == 0) {
        result += printPseudoColumn();
      }
    }
    result += "</tr>\n";
    if (getLines().isEmpty()) {
      result += "<tr><td>&nbsp;</td></tr>\n";
    } else {
      int max = getState().getMaximumVisibleLine();

      if (max == -1) {
        max = getLines().size();
      }
      first = getState().getFirstVisibleLine();
      if (first > getLines().size() - max) {
        first = getLines().size() - max;
      }
      if (first < 0) {
        first = 0;
      }
      getState().setFirstVisibleLine(first);

      for (int i = first; (i < getLines().size()) && (i < first + max); i++) {
        if (getCellSpacing() == 0) {
          result += getLines().get(i).printWithPseudoColumns();
        } else {
          result += getLines().get(i).print();
        }
        last = i;
      }
    }
    result += "</table>\n";

    if (-1 != getState().getMaximumVisibleLine()) {
      String iconPath = getIconsPath();

      result +=
          "<table width=\"100%\" border=\"0\" cellspacing=\"0\" cellpadding=\"0\" class=\"ArrayColumn\">\n"
              +
              "<tr align=\"center\" bgcolor=\"#999999\">\n"
              + "<td><img src=\""
              + iconPath
              + "1px.gif\" width=\"1\" height=\"1\" alt=\"\"/></td>\n"
              + "</tr>\n"
              + "<tr align=\"center\" bgcolor=\"#FFFFFF\">\n"
              + "<td><img src=\""
              + iconPath
              + "1px.gif\" width=\"1\" height=\"1\" alt=\"\"/></td>\n"
              + "</tr>\n"
              + "<tr align=\"center\"> \n"
              + "<td class=\"ArrayNavigation\" height=\"20\">";

      if (first > 0) {
        result += "<a class=\"ArrayNavigation\" href=\"";
        String url = getUrl();

        if (!url.contains("?")) {
          url += "?";
        } else {
          url += "&";
        }
        url += ACTION_PARAMETER_NAME + "=Previous&" + TARGET_PARAMETER_NAME
            + "=" + getName();
        result += url + "\"><< Pr&eacute;c&eacute;dent </a>  | ";
      }
      if (first != last) {
        result += (first + 1) + " - " + (last + 1);
      } else {
        result += (first + 1);
      }
      result += " / " + getLines().size();
      if (last + 1 < getLines().size()) {
        result += " | <a class=\"ArrayNavigation\" href=\"";
        String url = getUrl();

        if (!url.contains("?")) {
          url += "?";
        } else {
          url += "&";
        }
        url += ACTION_PARAMETER_NAME + "=Next&" + TARGET_PARAMETER_NAME + "="
            + getName();
        result += url + "\">Suivant >></a>";
      }

      result += "</td>" + "</tr>\n"
          + "<tr align=\"center\" bgcolor=\"#999999\">\n" + "<td><img src=\""
          + iconPath + "1px.gif\" width=\"1\" height=\"1\" alt=\"\"/></td>\n" + "</tr>\n"
          + "<tr align=\"center\" bgcolor=\"#666666\">\n" + "<td><img src=\""
          + iconPath + "1px.gif\" width=\"1\" height=\"1\" alt=\"\"/></td>\n" + "</tr>\n"
          + "</table>";

    }
    result += "</td></tr></table>\n";
    return result;
  }
}