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

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpSession;
import javax.servlet.jsp.PageContext;

import org.silverpeas.core.web.util.viewgenerator.html.SimpleGraphicElement;

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

  public static final String ACTION_PARAMETER_NAME = "ArrayPaneAction";
  public static final String TARGET_PARAMETER_NAME = "ArrayPaneTarget";
  public static final String COLUMN_PARAMETER_NAME = "ArrayPaneColumn";
  public static final String INDEX_PARAMETER_NAME = "ArrayPaneIndex";

  /**
   * Generic class to display a typical WA array table pane. A unique name identifier is to be used
   * in html pages for this array specific actions (exemple : sort on a specific column)
   * @param name A unique name in the page to display
   */
  public void init(String name, PageContext pageContext);

  /**
   * Constructor declaration
   * @param name
   * @param request
   * @param session
   * @see
   */
  public void init(String name, javax.servlet.ServletRequest request,
      HttpSession session);

  /**
   * Constructor declaration
   * @param name
   * @param url
   * @param request
   * @param session
   * @see
   */
  public void init(String name, String url,
      javax.servlet.ServletRequest request, HttpSession session);

  /**
   * Add a new column to the table.
   * @param title The column title to display
   * @return The new column header. You can use this object to modify the default display options.
   */
  public ArrayColumn addArrayColumn(String title);

  /**
   * Add a line to the table. Be carefull : each line form the array has to contain the same cell
   * number. If not, the array will contain some empty cells, and won't be sortable.
   * @return an ArrayLine, to be used to add cells and to modify default display options.
   */
  public ArrayLine addArrayLine();

  /**
   * Set the array title, to be displayed on the first html table.
   * @param title The title
   */
  public void setTitle(String title);

  /**
   * Get the title
   * @return The title
   */
  public String getTitle();

  /**
   * Get the unique name
   * @return The name of this object in the http page
   */
  public String getName();

  /**
   * Set the maximum line number visible in the table. If the number of line is greater than this
   * maximum, only the first lines will be visible, and some buttons to view next and previous lines
   * will be added.
   * @param maximum The maximum number of visible lines
   */
  public void setVisibleLineNumber(int maximum);

  /**
   * Modify the column number the sort will be based on.
   * @param columnNumber The column to be sorted
   */
  public void setColumnToSort(int columnNumber);

  /**
   * Modify the column behaviour. Useful if you have a passive column which does not need to trigger
   * an hyperlink when its header is cliked
   * @deprecated
   * @param columnNumber The column to be set
   */
  public void setColumnBehaviour(int columnNumber, int mode);

  /**
   * Get the column to be sorted
   * @return The column number.
   */
  public int getColumnToSort();

  /**
   * Print the array line in an html format.
   * @return The html code, representing the array pane
   */
  public String print();

  /**
   * Get the session in which the ArrayPane will keep its state.
   * @return The session
   */
  public HttpSession getSession();

  /**
   * Get the request that can contains some parameters for the ArrayPane (sort action...)
   * @return The entering request
   */
  public ServletRequest getRequest();

  /**
   * change the routing address (the url of the page to which the column header refer) in the rare
   * cases when you may not want it to be derived from the calling page. This method is called by
   * the constructor if you precise an url to the GraphicElementFactory.
   */
  public void setRoutingAddress(String address);

  /**
   * Set all array columns to be sortable or not. By default, all colums are sortable.
   * @param sortable Set sortable to false if you want all the table to be unsortable.
   */
  public void setSortable(boolean sortable);

  /**
   * Get global array columns behaviour for sort. By default, all colums are sortable.
   * @return True, if the array is sortable, false if not.
   */
  public boolean getSortable();

  /**
   * Change the sort mode for all columns that could handle this mode.
   * @param mode The new sort mode.
   * @deprecated
   */
  public void setSortMode(int mode);

  /**
   * Get the sort mode for all columns.
   * @return The sort mode.
   * @deprecated
   */
  public int getSortMode();

  /**
   * Change presentation parameters for cells. Allows for more compact lines if need be.
   * @deprecated
   */
  public void setCellsConfiguration(int spacing, int padding, int borderWidth);

  public void setPaginationJavaScriptCallback(String callback);

  public void setSummary(String summary);

  public String getSummary();

  public void setXHTML(boolean isXHTML);

  /**
   * @return true if the current array pane can be exported, false else if
   */
  public boolean getExportData();

  /**
   * @param enable/disable export data from array pane
   */
  public void setExportData(boolean export);

  /**
   * @return export data URL used to export current ArrayPane data
   */
  public String getExportDataURL();

  /**
   * @param exportDataURL the URL to set used to export array pane data
   */
  public void setExportDataURL(String exportDataURL);

  public void setSortableLines(boolean sortableLines);

  public void setUpdateSortJavascriptCallback(String callback);
}
