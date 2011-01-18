/**
 * Copyright (C) 2000 - 2009 Silverpeas
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
 * "http://repository.silverpeas.com/legal/licensing"
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.servlet.jsp.PageContext;

import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.util.ResourceLocator;
import com.stratelia.webactiv.util.viewGenerator.html.GraphicElementFactory;
import com.stratelia.webactiv.util.viewGenerator.html.pagination.Pagination;

/**
 * The default implementation of ArrayPane interface.
 * @author squere
 * @version 1.0
 */
public class ArrayPaneSilverpeasV5 implements ArrayPane {

  private List<ArrayColumn> columns;
  private List<ArrayLine> lines;
  private String title = null;
  private String summary = null;
  private boolean isXHTML = false;
  private String alignement = null;
  private String name;
  private ArrayPaneStatusBean state = null;
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
  private String paginationJavaScriptCallback = null;

  /**
   * Parameter attribute to enable/disable ArrayPane export feature
   */
  private boolean exportData = false;
  private String exportDataURL = null;

  /**
   * Default constructor as this class may be instanciated by method newInstance(), constructor
   * contains no parameter. init methods must be used to initialize properly the instance.
   * @see init
   */
  public ArrayPaneSilverpeasV5() {
    // initialisation is made in init methods
  }

  /**
   * Generic class to display a typical array table pane. A unique name identifier is to be used in
   * html pages for this array specific actions (exemple : sort on a specific column)
   * @param name A unique name in the page to display
   * @param pageContext
   */
  @Override
  public void init(String name, PageContext pageContext) {
    init(name, pageContext.getRequest(), pageContext.getSession());
  }

  /**
   * @param name
   * @param request
   * @param session
   */
  @Override
  public void init(String name, ServletRequest request, HttpSession session) {
    init(name, null, request, session);
  }

  /**
   * Constructor declaration
   * @param name
   * @param url
   * @param request
   * @param session
   * @see
   */
  @Override
  public void init(String name, String url, ServletRequest request, HttpSession session) {
    columns = new ArrayList<ArrayColumn>();
    lines = new ArrayList<ArrayLine>();
    this.name = name;
    setRoutingAddress(url);
    this.session = session;
    this.request = request;

    state = (ArrayPaneStatusBean) session.getAttribute(getName());
    if (state == null) {
      state = new ArrayPaneStatusBean();
      session.setAttribute(getName(), state);
    }

    String target = request.getParameter(TARGET_PARAMETER_NAME);

    if (target != null) {
      if (target.equals(name)) {
        String action = request.getParameter(ACTION_PARAMETER_NAME);
        SilverTrace.info("viewgenerator", "ArrayPaneSilverpeasV4.ArrayPaneSilverpeasV4()",
            "root.MSG_GEN_PARAM_VALUE", " ACTION_PARAMETER_NAME = '" + action + "'");
        if (action != null) {
          if ("Sort".equals(action)) {
            String newState = request.getParameter(COLUMN_PARAMETER_NAME);
            if (newState != null) {
              int ns = Integer.parseInt(newState);
              if ((ns == state.getSortColumn()) || (ns + state.getSortColumn() == 0)) {
                state.setSortColumn(-state.getSortColumn());
              } else {
                state.setSortColumn(ns);
              }
            }
          } else if ("ChangePage".equals(action)) {
            String index = request.getParameter(INDEX_PARAMETER_NAME);
            state.setFirstVisibleLine(Integer.parseInt(index));
          }
        }
      }
    }
    if (state.getSortColumn() >= 1) {
      setColumnToSort(state.getSortColumn());
    }
  }

  /**
   * Method declaration
   * @param maximum
   * @see
   */
  @Override
  public void setVisibleLineNumber(int maximum) {
    state.setMaximumVisibleLine(maximum);
  }

  /**
   * This method allows for the change of cell presentation values. A negative value means 'do not
   * change this value'
   * @param spacing
   * @param padding
   * @param borderWidth
   */
  @Override
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
   * Standard method that returns the CVS-managed version string
   * @return the version of the librairy.
   */
  public static String getVersion() {
    return "$Id: ArrayPaneSilverpeasV5.java,v 1.1 2008/06/13 07:06:36 neysseri Exp $";
  }

  /**
   * This method sets the routing address. This is actually the URL of the page to which requests
   * will be routed when the user clicks on a column header link.
   * @param address
   */
  @Override
  public void setRoutingAddress(String address) {
    m_RoutingAddress = address;
  }

  /**
   * Add a new column to the table.
   * @param title The column title to display
   * @return The new column header. You can use this object to modify the default display options.
   */
  @Override
  public ArrayColumn addArrayColumn(String title) {
    ArrayColumn col = new ArrayColumn(title, columns.size() + 1, this);
    columns.add(col);
    col.setRoutingAddress(m_RoutingAddress);
    return col;
  }

  /**
   * @return
   */
  @Override
  public ArrayLine addArrayLine() {
    ArrayLine line = new ArrayLine(this);
    lines.add(line);
    return line;
  }

  /**
   * @param title
   */
  @Override
  public void setTitle(String title) {
    this.title = title;
  }

  /**
   * @return
   */
  public String getAlignement() {
    return alignement;
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
  @Override
  public String getTitle() {
    return title;
  }

  /**
   * Method declaration
   * @return
   * @see
   */
  @Override
  public String getName() {
    return name;
  }

  /**
   * @param columnNumber
   */
  @Override
  public void setColumnToSort(int columnNumber) {
    SilverTrace.info("viewgenerator", "ArrayPaneSilverpeasV4.setColumnToSort()",
        "root.MSG_GEN_PARAM_VALUE", " columNumber = '" + columnNumber + "'");
    state.setSortColumn(columnNumber);
  }

  /**
   * Method declaration
   * @return
   * @see
   */
  @Override
  public int getColumnToSort() {
    return state.getSortColumn();
  }

  /**
   * This methods sets the sort mode for all columns. The columns cells may or may not take this
   * value into account.
   */
  @Override
  public void setSortMode(int mode) {
    m_SortMode = mode;
  }

  /**
   * Method declaration
   * @return
   * @see
   */
  @Override
  public int getSortMode() {
    return (m_SortMode);
  }

  /**
   * Method declaration
   * @param columnNumber
   * @param mode
   * @see
   */
  @Override
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
  @Override
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
  @Override
  public boolean getSortable() {
    return (getSortMode() == ArrayColumn.COLUMN_BEHAVIOUR_DEFAULT);
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
    GraphicElementFactory gef = (GraphicElementFactory) session.getAttribute(
        GraphicElementFactory.GE_FACTORY_SESSION_ATT);
    Pagination pagination = gef.getPagination(lines.size(), state.getMaximumVisibleLine(), state.
        getFirstVisibleLine());

    String sep = "&";
    if (isXHTML) {
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

    int columnsCount = columns.size();

    if ((lines.size() > 0) && (getColumnToSort() != 0) && (getColumnToSort() <= columnsCount)) {
      SilverTrace.info("viewgenerator", "ArrayPaneSilverpeasV5.print()",
          "root.MSG_GEN_PARAM_VALUE",
          "Tri des lignes");
      Collections.sort(lines);
    }

    // when there is no cell spacing, add pseudo columns as fillers
    if (m_CellsSpacing == 0) {
      columnsCount = columnsCount * 2 + 1;
    }
    StringBuilder result = new StringBuilder();
    result.append("<table width=\"98%\" cellspacing=\"0\" cellpadding=\"2\" border=\"0\" ");
    result.append("class=\"arrayPane\"><tr><td>\n").append("<table width=\"100%\" cellspacing=\"");
    result.append(m_CellsSpacing).append("\" cellpadding=\"").append(m_CellsPadding);
    result.append("\" border=\"");
    result.append(m_CellsBorderWidth).append("\" class=\"tableArrayPane\" summary=\"");
    result.append(getSummary()).append("\">");
    if (getTitle() != null) {
      result.append("<caption>").append(getTitle()).append("</caption>");
    }
    if (m_CellsSpacing == 0) {
      result.append("<tr>");
      result.append("<td colspan=\"").append(columnsCount).append("\">");
      result.append("<img src=\"").append(getIconsPath()).append(
          "/1px.gif\" width=\"1\" height=\"1\" alt=\"\"/>");
      result.append("</td>");
      result.append("</tr>\n");
    }
    result.append("<thead>\n");
    result.append("<tr>\n");
    if (m_CellsSpacing == 0) {
      result.append(printPseudoColumn());
    }
    for (int i = 0; i < columns.size(); i++) {
      result.append(columns.get(i).print(isXHTML));
      if (m_CellsSpacing == 0) {
        result.append(printPseudoColumn());
      }
    }
    result.append("</tr>\n").append("</thead>\n").append("<tbody>\n");
    if (lines.isEmpty()) {
      result.append("<tr><td>&nbsp;</td></tr>\n");
    } else {

      state.setFirstVisibleLine(pagination.getIndexForCurrentPage());
      int first = pagination.getIndexForCurrentPage();
      int lastIndex;
      if (pagination.isLastPage()) {
        lastIndex = pagination.getLastItemIndex();
      } else {
        lastIndex = pagination.getIndexForNextPage();
      }
      for (int i = first; i < lastIndex; i++) {
        if (m_CellsSpacing == 0) {
          result.append(lines.get(i).printWithPseudoColumns());
        } else {
          result.append(lines.get(i).print());
        }
      }
    }
    result.append("</tbody>\n");
    result.append("</table>\n");

    if (-1 != state.getMaximumVisibleLine()
        && lines.size() > state.getMaximumVisibleLine()) {
      result.append(
          "<table width=\"100%\" border=\"0\" cellspacing=\"0\" cellpadding=\"0\">\n").append(
          "<tr class=\"intfdcolor\"> \n").append("<td align=\"center\">");
      result.append(pagination.printIndex(paginationJavaScriptCallback));
      result.append("</td>").append("</tr>\n").append("</table>");
    }

    result.append("</td></tr></table>\n");

    // Add export data GUI
    if (this.exportData) {
      result.append("<div class=\"exportlinks\">");
      result.append(gef.getMultilang().getString("GEF.export.label")).append(":");
      result.append("<a href=\"").append(getUrl()).append(exportDataURL).append(
          "\"><span class=\"export csv\">");
      result.append(gef.getMultilang().getString("GEF.export.option.csv")).append("</span></a>");
      result.append("</div>");
    }
    return result.toString();
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
      String address = ((HttpServletRequest) getRequest()).getRequestURI();
      // only get a relative http address
      address = address.substring(address.lastIndexOf('/') + 1, address.length());
      // if the previous request had parameters, remove them
      if (address.lastIndexOf('?') >= 0) {
        address = address.substring(0, address.lastIndexOf('?'));
      }
      return address;
    } else {
      return m_RoutingAddress;
    }
  }

  /**
   * Method declaration
   * @return
   * @see
   */
  @Override
  public HttpSession getSession() {
    return session;
  }

  /**
   * Method declaration
   * @return
   * @see
   */
  @Override
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

    return generalSettings.getString("ApplicationURL")
        + GraphicElementFactory.getSettings().getString("IconsPath");
  }

  @Override
  public void setPaginationJavaScriptCallback(String callback) {
    paginationJavaScriptCallback = callback;
  }

  @Override
  public String getSummary() {
    return summary;
  }

  @Override
  public void setSummary(String summary) {
    this.summary = summary;
  }

  @Override
  public void setXHTML(boolean isXHTML) {
    this.isXHTML = isXHTML;
  }

  @Override
  public boolean getExportData() {
    return exportData;
  }

  @Override
  public void setExportData(boolean exportData) {
    this.exportData = exportData;
  }

  @Override
  public String getExportDataURL() {
    return this.exportDataURL;
  }

  @Override
  public void setExportDataURL(String exportDataURL) {
    this.exportDataURL = exportDataURL;
  }

}