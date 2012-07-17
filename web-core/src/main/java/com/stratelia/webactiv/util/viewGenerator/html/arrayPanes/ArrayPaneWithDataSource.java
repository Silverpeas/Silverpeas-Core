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

import java.util.Collections;
import java.util.Vector;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpSession;
import javax.servlet.jsp.PageContext;

import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.util.ResourceLocator;
import com.stratelia.webactiv.util.datapaginator.WADataPage;
import com.stratelia.webactiv.util.datapaginator.WADataPaginator;
import com.stratelia.webactiv.util.datapaginator.WAItem;
import com.stratelia.webactiv.util.viewGenerator.html.GraphicElementFactory;

/**
 * The default implementation of ArrayPane interface.
 * @author squere
 * @version 1.0
 */
public class ArrayPaneWithDataSource implements ArrayPane {

  private Vector<ArrayColumn> columns;
  private Vector<ArrayLine> lines;
  private String title = null;
  private String summary = null;
  private boolean isXHTML = false;
  private String alignement = null;
  private String name;
  private ArrayPaneStatusBean state = null;

  // private PageContext pageContext = null;
  private ServletRequest request = null;
  private HttpSession session = null;
  private int m_SortMode = 0;

  /**
   * configurable values for cells spacing and padding (of the inernal table). These may be set via
   * {@link #setCellsConfiguration(int spacing,int padding,int borderWidth)}
   */
  private int m_CellsSpacing = 2;
  private int m_CellsPadding = 2;
  private int m_CellsBorderWidth = 0;

  /**
   * In some cases, it may be preferable to specify the routing address (via
   * {@link #setRoutingAddress(String address)})
   * @see ArrayColum.setRoutingAddress(String address)
   */
  private String m_RoutingAddress = null;
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
   * Generic class to display a typical WA array table pane. A unique name identifier is to be used
   * in html pages for this array specific actions (exemple : sort on a specific column)
   * @param name A unique name in the page to display
   */
  public void init(String name, PageContext pageContext) {
    init(name, pageContext.getRequest(), pageContext.getSession());
  }

  /**
   * Constructor declaration
   * @param name
   * @param request
   * @param session
   * @see
   */
  public void init(String name, javax.servlet.ServletRequest request,
      HttpSession session) {
    init(name, null, request, session);
  }

  /**
   * Method declaration
   * @param name
   * @param url
   * @param request
   * @param session
   * @see
   */
  public void init(String name, String url,
      javax.servlet.ServletRequest request, HttpSession session) {
    String target = null;

    columns = new Vector<ArrayColumn>();
    lines = new Vector<ArrayLine>();
    this.name = name;
    setRoutingAddress(url);
    this.session = session;
    this.request = request;

    // setVisibleLineNumber(5);

    // String state = (String) session.getAttribute(getName());
    if (session != null) {
      state = (ArrayPaneStatusBean) session.getAttribute(getName());
      if (state == null) {
        state = new ArrayPaneStatusBean();
        session.setAttribute(getName(), state);
      }
    }
    if (request == null) {
      return;
    }

    target = request.getParameter(TARGET_PARAMETER_NAME);

    if (target != null) {
      if (target.equals(name)) {
        String action = request.getParameter(ACTION_PARAMETER_NAME);

        SilverTrace.info("viewgenerator", "ArrayPaneWithDataSource.init()",
            "root.MSG_GEN_PARAM_VALUE", " ACTION_PARAMETER_NAME = '" + action
            + "'");
        if (action != null) {
          if (action.equals("Sort")) {
            String newState = request
                .getParameter(COLUMN_PARAMETER_NAME);

            if (newState != null) {
              int ns = Integer.parseInt(newState);

              if ((ns == state.getSortColumn())
                  || (ns + state.getSortColumn() == 0)) {
                state.setSortColumn(-state.getSortColumn());
              } else {
                state.setSortColumn(ns);
              }
              // keep same page active
              state.setFirstVisibleLine(0);
            }
          } else if (action.equals("Next")) {
            state.setFirstVisibleLine(1);
            state.setSortColumn(0);
          } else if (action.equals("Previous")) {
            state.setFirstVisibleLine(-1);
            state.setSortColumn(0);
          }
        }
      }
    }
    if (state != null && state.getSortColumn() >= 1) {
      setColumnToSort(state.getSortColumn());
    }

  }

  /**
   * Method declaration
   * @param maximum
   * @see
   */
  public void setVisibleLineNumber(int maximum) {
    state.setMaximumVisibleLine(maximum);
  }

  /**
   * This method allows for the change of cell presentation values. A negative value means 'do not
   * change this value'
   */
  public void setCellsConfiguration(int spacing, int padding, int borderWidth) {
    if (spacing >= 0) {
      m_CellsSpacing = spacing;
    }
    if (padding >= 0) {
      m_CellsPadding = padding;
    }
    if (borderWidth >= 0) {
      m_CellsBorderWidth = borderWidth;
    }
  }

  /**
   * standard method that returns the CVS-managed version string
   */
  public static String getVersion() {
    String v = "$Id: ArrayPaneWithDataSource.java,v 1.5 2008/04/16 14:45:06 neysseri Exp $";

    return (v);
  }

  /**
   * This method sets the routing address. This is actually the URL of the page to which requests
   * will be routed when the user clicks on a column header link.
   */
  public void setRoutingAddress(String address) {
    m_RoutingAddress = address;
  }

  /**
   * Add a new column to the table.
   * @param title The column title to display
   * @return The new column header. You can use this object to modify the default display options.
   */
  public ArrayColumn addArrayColumn(String title) {
    ArrayColumn col = new ArrayColumn(title, columns.size() + 1, this);

    columns.add(col);
    col.setRoutingAddress(m_RoutingAddress);
    return col;
  }

  /**
   * @return
   */
  public ArrayLine addArrayLine() {
    ArrayLine line = new ArrayLine(this);

    lines.add(line);
    return line;
  }

  /**
   * @param title
   */
  public void setTitle(String title) {
    this.title = title;
  }

  /**
   * @return
   */
  public String getTitle() {
    return title;
  }

  /**
   * @param alignement
   */
  public void setAlignement(String alignement) {
    this.alignement = alignement;
  }

  /**
   * @return
   */
  public String getAlignement() {
    return alignement;
  }

  /**
   * Method declaration
   * @return
   * @see
   */
  public String getName() {
    return name;
  }

  /**
   * @param columnNumber
   */
  public void setColumnToSort(int columnNumber) {
    SilverTrace.info("viewgenerator",
        "ArrayPaneWithDataSource.setColumnToSort()",
        "root.MSG_GEN_PARAM_VALUE", " columNumber = '" + columnNumber + "'");
    state.setSortColumn(columnNumber);

    /*
     * if (getSession() != null) getSession().setAttribute(getName(), String.valueOf(columnNumber));
     */
  }

  /**
   * Method declaration
   * @return
   * @see
   */
  public int getColumnToSort() {
    if (state != null) {
      return state.getSortColumn();
    } else {
      return (0);
    }
  }

  /**
   * This methods sets the sort mode for all columns. The columns cells may or may not take this
   * value into account.
   */
  public void setSortMode(int mode) {
    m_SortMode = mode;
  }

  /**
   * Method declaration
   * @return
   * @see
   */
  public int getSortMode() {
    return (m_SortMode);
  }

  /**
   * Method declaration
   * @param columnNumber
   * @param mode
   * @see
   */
  public void setColumnBehaviour(int columnNumber, int mode) {
    if (columns == null || columnNumber <= 0 || columnNumber > columns.size()) {
      return;
    }
    ArrayColumn col = columns.get(columnNumber - 1);
    col.setSortable(mode == ArrayColumn.COLUMN_BEHAVIOUR_DEFAULT);
  }

  /**
   * Set all array columns to be sortable or not. By default, all colums are sortable.
   * @param sortable Set sortable to false if you want all the table to be unsortable.
   */
  public void setSortable(boolean sortable) {
    if (sortable) {
      setSortMode(ArrayColumn.COLUMN_BEHAVIOUR_DEFAULT);
    } else {
      setSortMode(ArrayColumn.COLUMN_BEHAVIOUR_NO_TRIGGER);
    }

  }

  /**
   * Method declaration
   * @return
   * @see
   */
  public boolean getSortable() {
    return (getSortMode() == ArrayColumn.COLUMN_BEHAVIOUR_DEFAULT);
  }

  /**
   * Method declaration
   * @return
   * @see
   */
  private String printPseudoColumn() {
    return ("<td>&nbsp;</td>");
  }

  /**
   * Method declaration
   * @return
   * @see
   */
  public String print() {
    if (m_DataSource == null) {
      return (standardPrint());
    } else {
      return (dataSourcePrint());
    }
  }

  /**
   * Method declaration
   * @return
   * @see
   */
  private String dataSourcePrint() {
    SilverTrace.info("viewgenerator",
        "ArrayPaneWithDataSource.dataSourcePrint()",
        "root.MSG_GEN_ENETR_METHOD");
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
        SilverTrace.info("viewgenerator",
            "ArrayPaneWithDataSource.dataSourcePrint()",
            "root.MSG_GEN_PARAM_VALUE",
            " Toggling column sort order on column = " + sortCol);
        m_DataSource.getHeader().toggleFieldSortOrder(sortCol - 1);
        setColumnToSort(0);
      }
      if (state != null) {
        if (state.getFirstVisibleLine() > 0) {
          pageActionString = "next";
          SilverTrace.info("viewgenerator",
              "ArrayPaneWithDataSource.dataSourcePrint()",
              "root.MSG_GEN_PARAM_VALUE", " pageActionString = "
              + pageActionString);
          page = m_DataSource.getNextPage();
        } else if (state.getFirstVisibleLine() < 0) {
          pageActionString = "previous";
          SilverTrace.info("viewgenerator",
              "ArrayPaneWithDataSource.dataSourcePrint()",
              "root.MSG_GEN_PARAM_VALUE", " pageActionString = "
              + pageActionString);
          page = m_DataSource.getPreviousPage();
        } else {
          pageActionString = "current";
          SilverTrace.info("viewgenerator",
              "ArrayPaneWithDataSource.dataSourcePrint()",
              "root.MSG_GEN_PARAM_VALUE", " pageActionString = "
              + pageActionString);
          page = m_DataSource.getCurrentPage();
          if (page == null) {
            pageActionString = "first";
            SilverTrace.info("viewgenerator",
                "ArrayPaneWithDataSource.dataSourcePrint()",
                "root.MSG_GEN_PARAM_VALUE", " pageActionString = "
                + pageActionString);
            page = m_DataSource.getFirstPage();
          }
        }
        state.setFirstVisibleLine(0);
      } else {
        pageActionString = "next";
        SilverTrace.info("viewgenerator",
            "ArrayPaneWithDataSource.dataSourcePrint()",
            "root.MSG_GEN_PARAM_VALUE", " pageActionString = "
            + pageActionString);

        page = m_DataSource.getNextPage();
      }

    } catch (Exception x) {
      SilverTrace.error("viewgenerator",
          "ArrayPaneWithDataSource.dataSourcePrint()",
          "viewgenerator.EX_CANT_LOAD_PAGE");
      return ("");
    }

    if (page == null) {
      return ("");
    }
    int pageNumber = m_DataSource.getCurrentPageNumber();
    int firstItemNumber = page.getStartItemDocumentIndex();
    int lastItemNumber = page.getEndItemDocumentIndex();
    int itemCount = m_DataSource.getItemCount();
    int pageItemCount = page.getItemCount();

    SilverTrace.info("viewgenerator",
        "ArrayPaneWithDataSource.dataSourcePrint()",
        "root.MSG_GEN_PARAM_VALUE", " PageNumber=" + pageNumber
        + ", pageItemCount=" + pageItemCount);
    String result = "";

    result +=
        "<table width=\"98%\" class=\"ArrayColumn\" cellspacing=0 cellpadding=2 border=0><tr><td>\n";
    result += "<table bgcolor=\"ffffff\" width=\"100%\" cellspacing=\""
        + m_CellsSpacing + "\" cellpadding=\"" + m_CellsPadding
        + "\" border=\"" + m_CellsBorderWidth + "\">";
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
        ac.setRoutingAddress(m_RoutingAddress);
      } else {
        ac.setRoutingAddress(fra);
        if ("".equals(fra)) {
          ac.setSortable(false);
        }
      }
      if (request != null) {
        result += ac.print();
      }
      if (m_CellsSpacing == 0) {
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
        if (m_CellsSpacing == 0) {
          result += new ArrayEmptyCell().print();
        }
      }
      result += "</tr>\n";
    }
    result += "</table>\n";

    if (-1 != state.getMaximumVisibleLine()) {
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
      Collections.sort(lines);
    }

    int columnsCount = columns.size();

    // when there is no cell spacing, add pseudo columns as fillers
    if (m_CellsSpacing == 0) {
      columnsCount *= 2;
    }
    String result = "";

    result +=
        "<table width=\"98%\" class=\"ArrayColumn\" cellspacing=0 cellpadding=2 border=0><tr><td>\n";
    result += "<table bgcolor=\"ffffff\" width=\"100%\" cellspacing=\""
        + m_CellsSpacing + "\" cellpadding=\"" + m_CellsPadding
        + "\" border=\"" + m_CellsBorderWidth + "\">";
    if (getTitle() != null) {
      result += "<tr>";
      result += "<td class=\"txttitrecol\" colspan=\"" + columnsCount + "\">";
      result += getTitle();
      result += "</td>";
      result += "</tr>\n";
    }
    result += "<tr>";
    for (int i = 0; i < columns.size(); i++) {
      result += columns.elementAt(i).print(isXHTML);
      if (m_CellsSpacing == 0) {
        result += printPseudoColumn();
      }
    }
    result += "</tr>\n";
    if (lines.size() == 0) {
      result += "<tr><td>&nbsp;</td></tr>\n";
    } else {
      int max = state.getMaximumVisibleLine();

      if (max == -1) {
        max = lines.size();
      }
      first = state.getFirstVisibleLine();
      if (first > lines.size() - max) {
        first = lines.size() - max;
      }
      if (first < 0) {
        first = 0;
      }
      state.setFirstVisibleLine(first);

      for (int i = first; (i < lines.size()) && (i < first + max); i++) {
        if (m_CellsSpacing == 0) {
          result += lines.elementAt(i).printWithPseudoColumns();
        } else {
          result += lines.elementAt(i).print();
        }
        last = i;
      }
    }
    result += "</table>\n";

    if (-1 != state.getMaximumVisibleLine()) {
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
      result += " / " + lines.size();
      if (last + 1 < lines.size()) {
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
   * Method declaration
   * @return
   * @see
   */
  public String getUrl() {

    // routing address computation. By default, route to the short name for the
    // calling page
    if (m_RoutingAddress == null) {
      String address = ((javax.servlet.http.HttpServletRequest) getRequest())
          .getRequestURI();

      // only get a relative http address
      address = address.substring(address.lastIndexOf("/") + 1, address
          .length());

      // if the previous request had parameters, remove them
      if (address.lastIndexOf("?") >= 0) {
        address = address.substring(0, address.lastIndexOf("?"));
      }
      return address;
    } else {
      return m_RoutingAddress;
    }
  }

  /*
   * public PageContext getPageContext() { return pageContext; }
   */

  /**
   * Method declaration
   * @return
   * @see
   */
  public HttpSession getSession() {
    return session;
  }

  /**
   * Method declaration
   * @return
   * @see
   */
  public ServletRequest getRequest() {
    return request;
  }

  /**
   * Method declaration
   * @return
   * @see
   */
  public String getIconsPath() {
    ResourceLocator generalSettings = new ResourceLocator(
        "com.stratelia.webactiv.general", "fr");

    String ip = generalSettings.getString("ApplicationURL")
        + GraphicElementFactory.getSettings().getString("IconsPath");

    if (ip == null) {
      return (ip);
    }
    if (!ip.endsWith("/")) {
      return (ip + "/");
    }
    return (ip);
  }

  @Override
  public void setPaginationJavaScriptCallback(String callback) {
    // TODO Auto-generated method stub
  }

  public String getSummary() {
    return summary;
  }

  public void setSummary(String summary) {
    this.summary = summary;
  }

  public void setXHTML(boolean isXHTML) {
    this.isXHTML = isXHTML;
  }

  @Override
  public boolean getExportData() {
    return false;
  }

  @Override
  public void setExportData(boolean export) {
  }

  @Override
  public String getExportDataURL() {
    return null;
  }

  @Override
  public void setExportDataURL(String exportDataURL) {
  }
}