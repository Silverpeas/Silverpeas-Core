/*
 * Copyright (C) 2000 - 2019 Silverpeas
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
import org.silverpeas.core.util.SilverpeasList;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.util.URLUtil;
import org.silverpeas.core.web.util.viewgenerator.html.GraphicElementFactory;
import org.silverpeas.core.web.util.viewgenerator.html.pagination.Pagination;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.servlet.jsp.PageContext;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static java.lang.Math.abs;
import static org.silverpeas.core.util.StringUtil.defaultStringIfNotDefined;
import static org.silverpeas.core.web.util.viewgenerator.html.pagination.Pagination.INDEX_PARAMETER_NAME;

public class AbstractArrayPane implements ArrayPane {

  protected static final String EXPORT_URL_SERVLET_MAPPING = "/Export/ArrayPane";
  private static final int DEFAULT_SPACING = 2;
  private static final int DEFAULT_PADDING = 2;
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
  private int mSortMode = 0;
  private int maxCountOfPaginationList = -1;
  private Pagination pagination;
  /**
   * configurable values for cells spacing and padding (of the internal table).
   */
  private int mCellsSpacing = DEFAULT_SPACING;
  private int mCellsPadding = DEFAULT_PADDING;
  private int mCellsBorderWidth = 0;
  /**
   * In some cases, it may be preferable to specify the routing address
   *
   * @see ArrayColumn#setRoutingAddress(String)
   */
  private String mRoutingAddress = null;
  private String paginationJavaScriptCallback = null;
  /**
   * Parameter attribute to enable/disable ArrayPane export feature
   */
  private boolean exportData = false;
  private String exportDataURL = null;
  private boolean sortableLines = false;

  static <O> O getOrderByFrom(final ArrayPaneStatusBean state, final String columnIndex,
      final Map<Integer, Pair<O, O>> orderBiesByColumnIndex) {
    O result = null;
    final int currentSortColumn = state.getSortColumn();
    final boolean fromRequest = StringUtil.isInteger(columnIndex);
    int columnSort = fromRequest ? Integer.parseInt(columnIndex) : currentSortColumn;
    if (columnSort != 0) {
      Pair<O, O> orderBy = orderBiesByColumnIndex.get(abs(columnSort));
      if (orderBy != null) {
        int sortColumn = fromRequest && abs(columnSort) == abs(currentSortColumn)
            ? currentSortColumn * -1
            : columnSort;
        if (sortColumn > 0) {
          result = orderBy.getLeft();
        } else {
          result = orderBy.getRight();
        }
      }
    } else {
      Pair<O, O> orderBy = orderBiesByColumnIndex.get(null);
      result = orderBy != null ? orderBy.getLeft() : null;
    }
    return result;
  }

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

    state = initState();

    String target = defaultStringIfNotDefined(request.getParameter(TARGET_PARAMETER_NAME));
    if (target.equals(name)) {
      String action = request.getParameter(ACTION_PARAMETER_NAME);
      if ("Sort".equals(action)) {
        initColumnSorting();
      } else if ("ChangePage".equals(action)) {
        initNbLinesPerPage();
      }
    }

    if (state.getSortColumn() >= 1) {
      setColumnToSort(state.getSortColumn());
    }
  }

  private ArrayPaneStatusBean initState() {
    ArrayPaneStatusBean sessionState = (ArrayPaneStatusBean) session.getAttribute(getName());
    if (sessionState == null) {
      sessionState = new ArrayPaneStatusBean();
      session.setAttribute(getName(), sessionState);
    }
    return sessionState;
  }

  private void initColumnSorting() {
    String newState = request.getParameter(COLUMN_PARAMETER_NAME);
    if (newState != null) {
      int ns = Integer.parseInt(newState);
      if ((ns == state.getSortColumn()) || (ns + state.getSortColumn() == 0)) {
        state.setSortColumn(-state.getSortColumn());
      } else {
        state.setSortColumn(ns);
      }
    }
  }

  private void initNbLinesPerPage() {
    String nbLines = request.getParameter("ItemsPerPage");
    if (StringUtil.isDefined(nbLines)) {
      state.setMaximumVisibleLine(Integer.valueOf(nbLines), true);
    }
    String index = request.getParameter(INDEX_PARAMETER_NAME);
    state.setFirstVisibleLine(Integer.parseInt(index));
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
    col.setRoutingAddress(mRoutingAddress);
    return col;
  }

  @Override
  public ArrayLine addArrayLine() {
    ArrayLine line = new ArrayLine(this);
    lines.add(line);
    return line;
  }

  @Override
  public String getTitle() {
    return title;
  }

  @Override
  public void setTitle(String title) {
    this.title = title;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public void setVisibleLineNumber(int maximum) {
    state.setMaximumVisibleLine(maximum, false);
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
  public void setColumnToSort(int columnNumber) {
    state.setSortColumn(columnNumber);
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
    mRoutingAddress = address;
  }

  @Override
  public boolean getSortable() {
    return getSortMode() == ArrayColumn.COLUMN_BEHAVIOUR_DEFAULT;
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
  public int getSortMode() {
    return mSortMode;
  }

  /**
   * This methods sets the sort mode for all columns. The columns cells may or may not take this
   * value into account.
   */
  @Override
  public void setSortMode(int mode) {
    mSortMode = mode;
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
      mCellsSpacing = spacing;
    }
    if (padding >= 0) {
      mCellsPadding = padding;
    }
    if (borderWidth >= 0) {
      mCellsBorderWidth = borderWidth;
    }
  }

  public int getCellSpacing() {
    return mCellsSpacing;
  }

  public int getCellPadding() {
    return mCellsPadding;
  }

  public int getCellBorderWidth() {
    return mCellsBorderWidth;
  }

  public String getPaginationJavaScriptCallback() {
    return paginationJavaScriptCallback;
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

  public boolean isXHTML() {
    return xhtml;
  }

  @Override
  public void setXHTML(boolean xhtml) {
    this.xhtml = xhtml;
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

  public boolean isSortableLines() {
    return sortableLines;
  }

  @Override
  public void setSortableLines(boolean sortableLines) {
    this.sortableLines = sortableLines;
  }

  public String getUrl() {
    // routing address computation. By default, route to the short name for the
    // calling page
    if (mRoutingAddress == null) {
      String address = ((HttpServletRequest) getRequest()).getRequestURI();
      // only get a relative http address
      address = address.substring(address.lastIndexOf('/') + 1, address.length());
      // if the previous request had parameters, remove them
      if (address.lastIndexOf('?') >= 0) {
        address = address.substring(0, address.lastIndexOf('?'));
      }
      return address;
    } else {
      return mRoutingAddress;
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
    StringBuilder sb = new StringBuilder();
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

  public String getUpdateSortJavascriptCallback() {
    return updateSortJavascriptCallback;
  }

  public void setUpdateSortJavascriptCallback(String callback) {
    this.updateSortJavascriptCallback = callback;
  }

  /**
   * Sets the pagination list which is able to provide only the necessary lines but with the
   * maximum items the array could to provide.
   * <p>For now, the elements of the pagination are not used, only max items is used</p>
   * @param paginationList the pagination list.
   */
  void setPaginationList(final SilverpeasList paginationList) {
    this.maxCountOfPaginationList = (int) paginationList.originalListSize();
  }

  /**
   * Indicates if the pagination is optimized, the is to say if the array contains only the lines
   * which will be printed to the user whereas the array could provides many other pages.
   * @return true if the pagination is optimized.
   */
  protected boolean isPaginationOptimized() {
    return maxCountOfPaginationList >= 0;
  }

  /**
   * Gets the number of items the list can provides.
   * @return the total number of items.
   */
  protected int getNbItems() {
    return isPaginationOptimized() ? maxCountOfPaginationList : getLines().size();
  }
  /**
   * Gets the current pagination.
   * @return the {@link Pagination} instance.
   * @param nbItems
   */
  Pagination getPagination(final int nbItems) {
    if (pagination == null) {
      GraphicElementFactory gef = (GraphicElementFactory) getSession()
          .getAttribute(GraphicElementFactory.GE_FACTORY_SESSION_ATT);
      pagination = gef.getPagination(nbItems, getState().getMaximumVisibleLine(),
          getState().getFirstVisibleLine());
    } else if (pagination.getNbItems() != nbItems) {
      pagination
          .init(nbItems, getState().getMaximumVisibleLine(), getState().getFirstVisibleLine());
    }
    return pagination;
  }
}