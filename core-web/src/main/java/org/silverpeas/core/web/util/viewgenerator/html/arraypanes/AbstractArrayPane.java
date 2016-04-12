/*
 * Copyright (C) 2000 - 2016 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of the GPL, you may
 * redistribute this Program in connection with Free/Libre Open Source Software ("FLOSS")
 * applications as described in Silverpeas's FLOSS exception. You should have received a copy of the
 * text describing the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.web.util.viewgenerator.html.arraypanes;

import org.silverpeas.core.util.URLUtil;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.web.util.viewgenerator.html.GraphicElementFactory;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.servlet.jsp.PageContext;
import java.util.ArrayList;
import java.util.List;

public class AbstractArrayPane implements ArrayPane {

  private List<ArrayColumn> columns;
  private List<ArrayLine> lines;
  private String title = null;
  private String summary = null;
  private boolean xhtml = false;
  private String alignement = null;
  private String name;
  private ArrayPaneStatusBean state = null;
  private String updateSortJavascriptCallback = null;
  private ServletRequest request = null;
  private HttpSession session = null;
  private int m_SortMode = 0;
  /**
   * configurable values for cells spacing and padding (of the internal table).
   */
  private int m_CellsSpacing = 2;
  private int m_CellsPadding = 2;
  private int m_CellsBorderWidth = 0;
  /**
   * In some cases, it may be preferable to specify the routing address
   *
   * @see ArrayColumn#setRoutingAddress(String)
   */
  private String m_RoutingAddress = null;
  private String paginationJavaScriptCallback = null;
  /**
   * Parameter attribute to enable/disable ArrayPane export feature
   */
  private boolean exportData = false;
  private String exportDataURL = null;
  protected static final String EXPORT_URL_SERVLET_MAPPING = "/Export/ArrayPane";
  private boolean sortableLines = false;

  @Override
  public void init(String name, PageContext pageContext) {
    init(name, pageContext.getRequest(), pageContext.getSession());
  }

  @Override
  public void init(String name, ServletRequest request, HttpSession session) {
    init(name, null, request, session);
  }

  @Override
  public void init(String name, String url, ServletRequest request, HttpSession session) {
    columns = new ArrayList<>();
    lines = new ArrayList<>();
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

    if (target != null && target.equals(name)) {
      String action = request.getParameter(ACTION_PARAMETER_NAME);

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

    if (state.getSortColumn() >= 1) {
      setColumnToSort(state.getSortColumn());
    }

  }

  public ArrayPaneStatusBean getState() {
    return state;
  }

  /**
   * Add a new column to the table.
   *
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

  @Override
  public ArrayLine addArrayLine() {
    ArrayLine line = new ArrayLine(this);
    lines.add(line);
    return line;
  }

  @Override
  public void setTitle(String title) {
    this.title = title;
  }

  @Override
  public String getTitle() {
    return title;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public void setVisibleLineNumber(int maximum) {
    state.setMaximumVisibleLine(maximum);
  }

  @Override
  public void setColumnToSort(int columnNumber) {

    state.setSortColumn(columnNumber);
  }

  @Override
  public void setColumnBehaviour(int columnNumber, int mode) {
    if (columns == null || columnNumber <= 0 || columnNumber > columns.size()) {
      return;
    }
    ArrayColumn col = columns.get(columnNumber - 1);
    col.setSortable(mode == ArrayColumn.COLUMN_BEHAVIOUR_DEFAULT);
  }

  @Override
  public int getColumnToSort() {
    if (state != null) {
      return state.getSortColumn();
    } else {
      return 0;
    }
  }

  @Override
  public String print() {
    return null;
  }

  @Override
  public HttpSession getSession() {
    return session;
  }

  @Override
  public ServletRequest getRequest() {
    return request;
  }

  /**
   * This method sets the routing address. This is actually the URL of the page to which requests
   * will be routed when the user clicks on a column header link.
   *
   * @param address
   */
  @Override
  public void setRoutingAddress(String address) {
    m_RoutingAddress = address;
  }

  /**
   * Set all array columns to be sortable or not. By default, all colums are sortable.
   *
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

  @Override
  public boolean getSortable() {
    return (getSortMode() == ArrayColumn.COLUMN_BEHAVIOUR_DEFAULT);
  }

  /**
   * This methods sets the sort mode for all columns. The columns cells may or may not take this
   * value into account.
   */
  @Override
  public void setSortMode(int mode) {
    m_SortMode = mode;
  }

  @Override
  public int getSortMode() {
    return m_SortMode;
  }

  /**
   * This method allows for the change of cell presentation values. A negative value means 'do not
   * change this value'
   *
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

  public int getCellSpacing() {
    return m_CellsSpacing;
  }

  public int getCellPadding() {
    return m_CellsPadding;
  }

  public int getCellBorderWidth() {
    return m_CellsBorderWidth;
  }

  @Override
  public void setPaginationJavaScriptCallback(String callback) {
    paginationJavaScriptCallback = callback;
  }

  public String getPaginationJavaScriptCallback() {
    return paginationJavaScriptCallback;
  }

  @Override
  public void setSummary(String summary) {
    this.summary = summary;
  }

  @Override
  public String getSummary() {
    return summary;
  }

  @Override
  public void setXHTML(boolean xhtml) {
    this.xhtml = xhtml;
  }

  public boolean isXHTML() {
    return xhtml;
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

  /**
   * @return the export array pane URL
   */
  protected String getExportUrl() {
    if (StringUtil.isDefined(getExportDataURL())) {
      return getExportDataURL();
    }
    StringBuilder exportUrl = new StringBuilder();
    String contextPath = URLUtil.getApplicationURL();
    exportUrl.append(contextPath).append(EXPORT_URL_SERVLET_MAPPING);
    exportUrl.append("?type=ArrayPane&name=");
    // Change the name parameter if you want to export 2 arrays which are displayed in the same page
    exportUrl.append("Silverpeas_arraypane");
    return exportUrl.toString();
  }

  @Override
  public void setSortableLines(boolean sortableLines) {
    this.sortableLines = sortableLines;
  }

  public boolean isSortableLines() {
    return sortableLines;
  }

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

  public String getIconsPath() {
    return GraphicElementFactory.getIconsPath();
  }

  public String getAlignement() {
    return alignement;
  }

  public void setAlignement(String alignement) {
    this.alignement = alignement;
  }

  public List<ArrayLine> getLines() {
    return lines;
  }

  public List<ArrayColumn> getColumns() {
    return columns;
  }

  protected String printSortJavascriptFunction() {
    StringBuilder sb = new StringBuilder(50);
    sb.append("<script type=\"text/javascript\">");

    sb.append("var fixArrayPaneWidthHelper = function(e, ui) {");
    sb.append("ui.children().each(function() {");
    sb.append("$(this).width($(this).width());");
    sb.append("});");
    sb.append("return ui;");
    sb.append("};");

    sb.append("$(\"#").append(getName()).append(" tbody\").sortable({");
    sb.append("placeholder: \"arraypane-sortable-placeholder\",");
    sb.append("cursor: \"move\",");
    sb.append("forcePlaceholderSize: true,");
    sb.append("helper: fixArrayPaneWidthHelper,");
    if (StringUtil.isDefined(getUpdateSortJavascriptCallback())) {
      sb.append("update: function(e, ui){").append(getUpdateSortJavascriptCallback()).append(";}");
    }
    sb.append("}).disableSelection();");
    sb.append("</script>");
    return sb.toString();
  }

  /**
   * standard method that returns the CVS-managed version string
   * @deprecated
   */
  public static String getVersion() {
    return "Deprecated";
  }

  public String getUpdateSortJavascriptCallback() {
    return updateSortJavascriptCallback;
  }

  public void setUpdateSortJavascriptCallback(String callback) {
    this.updateSortJavascriptCallback = callback;
  }
}