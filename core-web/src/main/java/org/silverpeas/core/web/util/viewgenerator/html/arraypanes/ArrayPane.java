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
import org.silverpeas.core.admin.PaginationPage;
import org.silverpeas.core.cache.model.SimpleCache;
import org.silverpeas.core.web.util.viewgenerator.html.SimpleGraphicElement;
import org.silverpeas.core.web.util.viewgenerator.html.pagination.Pagination;

import javax.portlet.RenderRequest;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.servlet.jsp.PageContext;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import static org.silverpeas.core.cache.service.CacheServiceProvider.getSessionCacheService;
import static org.silverpeas.core.util.StringUtil.defaultStringIfNotDefined;
import static org.silverpeas.core.util.StringUtil.isDefined;
import static org.silverpeas.core.web.portlets.PortletUtil.getHttpServletRequest;

/**
 * The ArrayPane interface gives us the skeleton for all funtionnalities we need to display typical
 * WA array table pane. Exemple : <BR>
 * <CODE>
 * // Build a new ArrayPane.<BR>
 * ArrayPane arrayPane = graphicFactory.getArrayPane("MyTodoArrayPane", pageContext);<BR>
 * // Add two columns.<BR>
 * arrayPane.addArrayColumn("Nom");<BR>
 * arrayPane.addArrayColumn("Priorite");<BR>
 * arrayPane.addArrayColumnWithAlignement("degres","center");<BR>
 * // Add one line.<BR>
 * ArrayLine arrayLine = arrayPane.addArrayLine();<BR>
 * arrayLine.addArrayCellText();<BR>
 * arrayLine.addArrayCellText();<BR>
 * arrayLine.addArrayCellLink("Un nom", "javascript:onClick=viewToDo()");<BR>
 * </CODE>
 * @author squere
 * @version 1.0
 */
public interface ArrayPane extends SimpleGraphicElement {

  String ACTION_PARAMETER_NAME = "ArrayPaneAction";
  String TARGET_PARAMETER_NAME = "ArrayPaneTarget";
  String COLUMN_PARAMETER_NAME = "ArrayPaneColumn";

  /**
   * Gets order by from given request and possible orderBies. The name of the array can be
   * specified via the attributes of given request with {@link #TARGET_PARAMETER_NAME} key.
   * @param renderRequest the request.
   * @param orderBiesByColumnIndex the possible order by indexed by the column index which starts
   * at 1.
   * @return the order by.
   */
  static <O> O getOrderByFrom(final RenderRequest renderRequest,
      final Map<Integer, Pair<O, O>> orderBiesByColumnIndex) {
    return getOrderByFrom(renderRequest, orderBiesByColumnIndex, null);
  }

  /**
   * Gets order by from given request and possible orderBies. The name of the array can be
   * specified via the attributes of given request with {@link #TARGET_PARAMETER_NAME} key.
   * @param renderRequest the request.
   * @param orderBiesByColumnIndex the possible order by indexed by the column index which starts
   * at 1.
   * @param defaultArrayPaneName the name of the array to search for in session in order to get
   * state and provide the right order by.
   * @return the order by.
   */
  static <O> O getOrderByFrom(final RenderRequest renderRequest,
      final Map<Integer, Pair<O, O>> orderBiesByColumnIndex, final String defaultArrayPaneName) {
    final Map<String, String> parameters = new HashMap<>();
    renderRequest.getParameterMap().forEach((key, value) -> parameters.put(key, value[0]));
    final HttpServletRequest request = getHttpServletRequest(renderRequest);
    return getOrderByFrom(request, parameters, orderBiesByColumnIndex, defaultArrayPaneName);
  }

  /**
   * Gets order by from given request and possible orderBies. The name of the array can be
   * specified via the attributes of given request with {@link #TARGET_PARAMETER_NAME} key.
   * @param request the request.
   * @param orderBiesByColumnIndex the possible order by indexed by the column index which starts
   * at 1.
   * @return the order by.
   */
  static <O> O getOrderByFrom(final HttpServletRequest request,
      final Map<Integer, Pair<O, O>> orderBiesByColumnIndex) {
    return getOrderByFrom(request, orderBiesByColumnIndex, null);
  }

  /**
   * Gets order by from given request and possible orderBies. If no order by is defined into
   * request, then the order by is retrieved from session if defaultArrayPaneName parameter is
   * defined.
   * @param request the request.
   * @param orderBiesByColumnIndex the possible order by indexed by the column index which starts
   * at 1.
   * @param defaultArrayPaneName the name of the array to search for in session in order to get
   * state and provide the right order by.
   * @return the order by.
   */
  static <O> O getOrderByFrom(final HttpServletRequest request,
      final Map<Integer, Pair<O, O>> orderBiesByColumnIndex, final String defaultArrayPaneName) {
    final Map<String, String> parameters = new HashMap<>();
    request.getParameterMap().forEach((key, value) -> parameters.put(key, value[0]));
    return getOrderByFrom(request, parameters, orderBiesByColumnIndex, defaultArrayPaneName);
  }

  /**
   * Gets order by from given request and possible orderBies. If no order by is defined into
   * request, then the order by is retrieved from session if defaultArrayPaneName parameter is
   * defined.
   * @param request the request.
   * @param params the request params.
   * @param orderBiesByColumnIndex the possible order by indexed by the column index which starts
   * at 1.
   * @param defaultArrayPaneName the name of the array to search for in session in order to get
   * state and provide the right order by.
   * @return the order by.
   */
  static <O> O getOrderByFrom(final HttpServletRequest request, final Map<String, String> params,
      final Map<Integer, Pair<O, O>> orderBiesByColumnIndex, final String defaultArrayPaneName) {
    final String action = params.get(ACTION_PARAMETER_NAME);
    final String column = params.get(COLUMN_PARAMETER_NAME);
    String name = params.get(TARGET_PARAMETER_NAME);
    if (name == null || !"Sort".equals(action)) {
      if (isDefined(defaultArrayPaneName)) {
        name = defaultArrayPaneName;
      } else {
        return null;
      }
    }
    final ArrayPaneStatusBean state = (ArrayPaneStatusBean) request.getSession(false).getAttribute(name);
    if (state == null) {
      return null;
    }
    return AbstractArrayPane.getOrderByFrom(state, column, orderBiesByColumnIndex);
  }

  /**
   * Gets new pagination page of array from given request.
   * @param renderRequest the request.
   * @return a pagination page.
   */
  static PaginationPage getPaginationPageFrom(RenderRequest renderRequest) {
    return getPaginationPageFrom(renderRequest, null);
  }

  /**
   * Gets new pagination page of array from given request.
   * @param renderRequest the request.
   * @param defaultArrayPaneName the name of the array to search for in session in order to get
   * current pagination.
   * @return a pagination page.
   */
  static PaginationPage getPaginationPageFrom(RenderRequest renderRequest,
      final String defaultArrayPaneName) {
    final Map<String, String> parameters = new HashMap<>();
    renderRequest.getParameterMap().forEach((key, value) -> parameters.put(key, value[0]));
    final HttpServletRequest request = getHttpServletRequest(renderRequest);
    return getPaginationPageFrom(request, parameters, defaultArrayPaneName);
  }

  /**
   * Gets new pagination page of array from given request.
   * @param request the request.
   * @return a pagination page.
   */
  static PaginationPage getPaginationPageFrom(final HttpServletRequest request) {
    return getPaginationPageFrom(request, null);
  }

  /**
   * Gets new pagination page of array from given request.
   * @param request the request.
   * @param defaultArrayPaneName the name of the array to search for in session in order to get
   * current pagination.
   * @return a pagination page.
   */
  static PaginationPage getPaginationPageFrom(final HttpServletRequest request,
      final String defaultArrayPaneName) {
    final Map<String, String> parameters = new HashMap<>();
    request.getParameterMap().forEach((key, value) -> parameters.put(key, value[0]));
    return getPaginationPageFrom(request, parameters, defaultArrayPaneName);
  }

  /**
   * Gets new pagination page of array from given request.
   * @param request the request.
   * @param params the request params.
   * @param defaultArrayPaneName the name of the array to search for in session in order to get
   * current pagination.
   * @return a pagination page.
   */
  static PaginationPage getPaginationPageFrom(final HttpServletRequest request,
      final Map<String, String> params, final String defaultArrayPaneName) {
    final String name = defaultStringIfNotDefined(params.get(TARGET_PARAMETER_NAME),
        defaultStringIfNotDefined(defaultArrayPaneName, "unknown-arraypane-state"));
    final ArrayPaneStatusBean state = (ArrayPaneStatusBean) request.getSession(false).getAttribute(name);
    final PaginationPage currentPagination;
    if (state == null) {
      currentPagination = null;
    } else {
      final int maximumVisibleLine = state.getMaximumVisibleLine();
      final int pageNumber = (state.getFirstVisibleLine() / maximumVisibleLine) + 1;
      currentPagination = new PaginationPage(pageNumber, maximumVisibleLine);
    }
    return Pagination.getPaginationPageFrom(params, currentPagination);
  }

  /**
   * Gets data from session from a given cache key or compute them from the given supplier if
   * absent.
   * <p>
   *   If the parameter <b>ajaxRequest</b> (set automatically by sp.arrayPane JavaScript API)
   *   exists, then the data are retrieved from the user session.
   * </p>
   * @param request the HTTP request.
   * @param cacheKey the key into the cache.
   * @param valueSupplier the data supplier.
   * @param <T> the type of data.
   * @return the data.
   */
  @SuppressWarnings("unchecked")
  static <T> T computeDataUserSessionIfAbsent(final HttpServletRequest request, final String cacheKey,
      final Supplier<T> valueSupplier) {
    final SimpleCache sessionCache = getSessionCacheService().getCache();
    if (request.getParameter("ajaxRequest") == null) {
      sessionCache.remove(cacheKey);
    }
    return (T) sessionCache
        .computeIfAbsent(cacheKey, Object.class, (Supplier<Object>) valueSupplier);
  }

  /**
   * Generic class to display a typical WA array table pane. A unique name identifier is to be used
   * in html pages for this array specific actions (exemple : sort on a specific column)
   * @param name A unique name in the page to display
   */
  void init(String name, PageContext pageContext);

  /**
   * Constructor declaration
   * @param name
   * @param request
   * @param session
   *
   */
  void init(String name, javax.servlet.ServletRequest request,
      HttpSession session);

  /**
   * Constructor declaration
   * @param name
   * @param url
   * @param request
   * @param session
   *
   */
  void init(String name, String url,
      javax.servlet.ServletRequest request, HttpSession session);

  /**
   * Add a new column to the table.
   * @param title The column title to display
   * @return The new column header. You can use this object to modify the default display options.
   */
  ArrayColumn addArrayColumn(String title);

  /**
   * Add a line to the table. Be carefull : each line form the array has to contain the same cell
   * number. If not, the array will contain some empty cells, and won't be sortable.
   * @return an ArrayLine, to be used to add cells and to modify default display options.
   */
  ArrayLine addArrayLine();

  /**
   * Get the title
   * @return The title
   */
  String getTitle();

  /**
   * Set the array title, to be displayed on the first html table.
   * @param title The title
   */
  void setTitle(String title);

  /**
   * Get the unique name
   * @return The name of this object in the http page
   */
  String getName();

  /**
   * Set the maximum line number visible in the table. If the number of line is greater than this
   * maximum, only the first lines will be visible, and some buttons to view next and previous lines
   * will be added.
   * @param maximum The maximum number of visible lines
   */
  void setVisibleLineNumber(int maximum);

  /**
   * Get the column to be sorted
   * @return The column number.
   */
  int getColumnToSort();

  /**
   * Modify the column number the sort will be based on.
   * @param columnNumber The column to be sorted
   */
  void setColumnToSort(int columnNumber);

  /**
   * Print the array line in an html format.
   * @return The html code, representing the array pane
   */
  String print();

  /**
   * Get the session in which the ArrayPane will keep its state.
   * @return The session
   */
  HttpSession getSession();

  /**
   * Get the request that can contains some parameters for the ArrayPane (sort action...)
   * @return The entering request
   */
  ServletRequest getRequest();

  /**
   * change the routing address (the url of the page to which the column header refer) in the rare
   * cases when you may not want it to be derived from the calling page. This method is called by
   * the constructor if you precise an url to the GraphicElementFactory.
   */
  void setRoutingAddress(String address);

  /**
   * Get global array columns behaviour for sort. By default, all colums are sortable.
   * @return True, if the array is sortable, false if not.
   */
  boolean getSortable();

  /**
   * Set all array columns to be sortable or not. By default, all colums are sortable.
   * @param sortable Set sortable to false if you want all the table to be unsortable.
   */
  void setSortable(boolean sortable);

  /**
   * Get the sort mode for all columns.
   * @return The sort mode.
   * @deprecated
   */
  @Deprecated
  int getSortMode();

  /**
   * Change the sort mode for all columns that could handle this mode.
   * @param mode The new sort mode.
   * @deprecated
   */
  @Deprecated
  void setSortMode(int mode);

  /**
   * Change presentation parameters for cells. Allows for more compact lines if need be.
   * @deprecated
   */
  @Deprecated
  void setCellsConfiguration(int spacing, int padding, int borderWidth);

  void setPaginationJavaScriptCallback(String callback);

  String getSummary();

  void setSummary(String summary);

  void setXHTML(boolean isXHTML);

  /**
   * @return true if the current array pane can be exported, false else if
   */
  boolean getExportData();

  /**
   * @param export enable/disable export data from array pane
   */
  void setExportData(boolean export);

  /**
   * @return export data URL used to export current ArrayPane data
   */
  String getExportDataURL();

  /**
   * @param exportDataURL the URL to set used to export array pane data
   */
  void setExportDataURL(String exportDataURL);

  void setSortableLines(boolean sortableLines);

  void setUpdateSortJavascriptCallback(String callback);
}
