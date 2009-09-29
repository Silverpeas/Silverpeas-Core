/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent) 
 ---*/

/*
 * ArrayPaneWA.java
 *
 * Created on 10 octobre 2000, 16:11
 */

package com.stratelia.webactiv.util.viewGenerator.html.arrayPanes;

import java.util.Vector;
import javax.servlet.jsp.PageContext;
import javax.servlet.http.*;
import javax.servlet.*;
import com.stratelia.webactiv.util.*;
import com.stratelia.webactiv.util.viewGenerator.html.*;
import com.stratelia.webactiv.util.viewGenerator.html.pagination.PaginationSP;
import com.stratelia.silverpeas.silvertrace.SilverTrace;

/**
 * The default implementation of ArrayPane interface.
 * 
 * @author squere
 * @version 1.0
 */
public class ArrayPaneWA implements ArrayPane {
  private Vector columns;
  private Vector lines;
  private String title = null;
  private String alignement = null;
  private String name;
  private ArrayPaneStatusBean state = null;

  private ServletRequest request = null;
  private HttpSession session = null;
  private int m_SortMode = 0;

  /**
   * configurable values for cells spacing and padding (of the inernal table).
   * These may be set via
   * {@link #setCellsConfiguration(int spacing,int padding,int borderWidth)}
   */
  private int m_CellsSpacing = 2;
  private int m_CellsPadding = 2;
  private int m_CellsBorderWidth = 0;

  /**
   * In some cases, it may be preferable to specify the routing address (via
   * {@link #setRoutingAddress(String address)})
   * 
   * @see ArrayColum.setRoutingAddress(String address)
   */
  private String m_RoutingAddress = null;

  /**
   * Default constructor as this class may be instanciated by method
   * newInstance(), constructor contains no parameter. init methods must be used
   * to initialize properly the instance.
   * 
   * @see init
   */
  public ArrayPaneWA() {
    // initialisation is made in init methods
  }

  /**
   * Generic class to display a typical WA array table pane. A unique name
   * identifier is to be used in html pages for this array specific actions
   * (exemple : sort on a specific column)
   * 
   * @param name
   *          A unique name in the page to display
   */
  public void init(String name, PageContext pageContext) {
    init(name, pageContext.getRequest(), pageContext.getSession());
  }

  /**
   * Constructor declaration
   * 
   * 
   * @param name
   * @param request
   * @param session
   * 
   * @see
   */
  public void init(String name, javax.servlet.ServletRequest request,
      HttpSession session) {
    init(name, null, request, session);
  }

  /**
   * Constructor declaration
   * 
   * 
   * @param name
   * @param url
   * @param request
   * @param session
   * 
   * @see
   */
  public void init(String name, String url,
      javax.servlet.ServletRequest request, HttpSession session) {
    columns = new Vector();
    lines = new Vector();
    this.name = name;
    setRoutingAddress(url);
    this.session = session;
    this.request = request;

    // setVisibleLineNumber(5);

    // String state = (String) session.getAttribute(getName());
    state = (ArrayPaneStatusBean) session.getAttribute(getName());
    if (state == null) {
      state = new ArrayPaneStatusBean();
      session.setAttribute(getName(), state);
    }

    String target = (String) request.getParameter(TARGET_PARAMETER_NAME);

    if (target != null) {
      if (target.equals(name)) {
        String action = (String) request.getParameter(ACTION_PARAMETER_NAME);

        SilverTrace.info("viewgenerator", "ArrayPaneWA.ArrayPaneWA()",
            "root.MSG_GEN_PARAM_VALUE", " ACTION_PARAMETER_NAME = '" + action
                + "'");
        if (action != null) {
          if (action.equals("Sort")) {
            String newState = (String) request
                .getParameter(COLUMN_PARAMETER_NAME);

            if (newState != null) {
              int ns = new Integer(newState).intValue();

              if ((ns == state.getSortColumn())
                  || (ns + state.getSortColumn() == 0)) {
                state.setSortColumn(-state.getSortColumn());
              } else {
                state.setSortColumn(ns);
              }
            }
          }
          /*
           * else if (action.equals("Next")) {
           * state.setFirstVisibleLine(state.getFirstVisibleLine() +
           * state.getMaximumVisibleLine()); } else if
           * (action.equals("Previous")) {
           * state.setFirstVisibleLine(state.getFirstVisibleLine() -
           * state.getMaximumVisibleLine()); }
           */
          else if (action.equals("ChangePage")) {
            String index = (String) request.getParameter(INDEX_PARAMETER_NAME);
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
   * 
   * 
   * @param maximum
   * 
   * @see
   */
  public void setVisibleLineNumber(int maximum) {
    state.setMaximumVisibleLine(maximum);
  }

  /**
   * This method allows for the change of cell presentation values. A negative
   * value means 'do not change this value'
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
    String v = "$Id: ArrayPaneWA.java,v 1.10 2008/04/16 14:45:06 neysseri Exp $";

    return (v);
  }

  /**
   * This method sets the routing address. This is actually the URL of the page
   * to which requests will be routed when the user clicks on a column header
   * link.
   */
  public void setRoutingAddress(String address) {
    m_RoutingAddress = address;
  }

  /**
   * Add a new column to the table.
   * 
   * @param title
   *          The column title to display
   * @return The new column header. You can use this object to modify the
   *         default display options.
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
  public String getTitle() {
    return title;
  }

  /**
   * Method declaration
   * 
   * 
   * @return
   * 
   * @see
   */
  public String getName() {
    return name;
  }

  /**
   * @param columnNumber
   */
  public void setColumnToSort(int columnNumber) {
    SilverTrace.info("viewgenerator", "ArrayPaneWA.setColumnToSort()",
        "root.MSG_GEN_PARAM_VALUE", " columNumber = '" + columnNumber + "'");
    state.setSortColumn(columnNumber);
  }

  /**
   * Method declaration
   * 
   * 
   * @return
   * 
   * @see
   */
  public int getColumnToSort() {
    return state.getSortColumn();
  }

  /**
   * This methods sets the sort mode for all columns. The columns cells may or
   * may not take this value into account.
   */
  public void setSortMode(int mode) {
    m_SortMode = mode;
  }

  /**
   * Method declaration
   * 
   * 
   * @return
   * 
   * @see
   */
  public int getSortMode() {
    return (m_SortMode);
  }

  /**
   * Method declaration
   * 
   * 
   * @param columnNumber
   * @param mode
   * 
   * @see
   */
  public void setColumnBehaviour(int columnNumber, int mode) {
    if (columns == null || columnNumber <= 0 || columnNumber > columns.size()) {
      return;
    }
    ArrayColumn col = (ArrayColumn) (columns.get(columnNumber - 1));
    col.setSortable(mode == ArrayColumn.COLUMN_BEHAVIOUR_DEFAULT);
  }

  /**
   * Set all array columns to be sortable or not. By default, all colums are
   * sortable.
   * 
   * @param sortable
   *          Set sortable to false if you want all the table to be unsortable.
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
   * 
   * 
   * @return
   * 
   * @see
   */
  public boolean getSortable() {
    return (getSortMode() == ArrayColumn.COLUMN_BEHAVIOUR_DEFAULT);
  }

  /**
   * Method declaration
   * 
   * 
   * @return
   * 
   * @see
   */
  private String printPseudoColumn() {
    return ("<td><img src=\"" + GraphicElementFactory.getIconsPath() + "/1px.gif\" width=\"2\" height=\"2\"></td>");
  }

  /**
   * Method declaration
   * 
   * 
   * @return
   * 
   * @see
   */
  public String print() {
    int first = -1;

    PaginationSP pagination = new PaginationSP();
    pagination.init(lines.size(), state.getMaximumVisibleLine(), state
        .getFirstVisibleLine());

    String baseUrl = getUrl();
    if (baseUrl.indexOf("?") < 0)
      baseUrl += "?";
    else
      baseUrl += "&";
    baseUrl += ACTION_PARAMETER_NAME + "=ChangePage&" + TARGET_PARAMETER_NAME
        + "=" + getName() + "&" + INDEX_PARAMETER_NAME + "=";
    pagination.setBaseURL(baseUrl);

    int columnsCount = columns.size();

    if ((lines.size() > 0) && (getColumnToSort() != 0)
        && (getColumnToSort() <= columnsCount)) {
      SilverTrace.info("viewgenerator", "ArrayPaneWA.print()",
          "root.MSG_GEN_PARAM_VALUE", "Tri des lignes");
      java.util.Collections.sort(lines);
    }

    // when there is no cell spacing, add pseudo columns as fillers
    if (m_CellsSpacing == 0) {
      columnsCount = columnsCount * 2 + 1;
    }
    StringBuffer result = new StringBuffer();

    result.append("<CENTER>\n");
    result
        .append("<table width=\"98%\" class=\"ArrayColumn\" cellspacing=0 cellpadding=2 border=0><tr><td>\n");
    result.append("<table bgcolor=\"ffffff\" width=\"100%\" cellspacing=\"")
        .append(m_CellsSpacing).append("\" cellpadding=\"").append(
            m_CellsPadding).append("\" border=\"").append(m_CellsBorderWidth)
        .append("\">");
    if (m_CellsSpacing == 0) {
      result.append("<tr>");
      result.append("<td colspan=\"").append(columnsCount).append("\">");
      result.append("<img src=\"").append(getIconsPath()).append(
          "/1px.gif\" width=\"1\" height=\"1\">");
      result.append("</td>");
      result.append("</tr>\n");
    }
    if (getTitle() != null) {
      result.append("<tr>");
      result.append("<td class=\"txttitrecol\" colspan=\"")
          .append(columnsCount).append("\">");
      result.append(getTitle());
      result.append("</td>");
      result.append("</tr>\n");
    }
    result.append("<tr>");
    if (m_CellsSpacing == 0) {
      result.append(printPseudoColumn());
    }
    for (int i = 0; i < columns.size(); i++) {
      result.append(((ArrayColumn) columns.elementAt(i)).print());
      if (m_CellsSpacing == 0) {
        result.append(printPseudoColumn());
      }
    }
    result.append("</tr>\n");
    if (lines.size() == 0) {
      result.append("<tr><td>&nbsp;</td></tr>\n");
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
          result.append(((ArrayLine) lines.elementAt(i))
              .printWithPseudoColumns());
        } else {
          result.append(((ArrayLine) lines.elementAt(i)).print());
        }
      }
    }
    result.append("</table>\n");

    if (-1 != state.getMaximumVisibleLine())
      result.append(pagination.printIndex());

    result.append("</td></tr></table>\n");
    result.append("</CENTER>\n");
    return result.toString();
  }

  /**
   * Method declaration
   * 
   * 
   * @return
   * 
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

  /**
   * Method declaration
   * 
   * 
   * @return
   * 
   * @see
   */
  public HttpSession getSession() {
    return session;
  }

  /**
   * Method declaration
   * 
   * 
   * @return
   * 
   * @see
   */
  public ServletRequest getRequest() {
    return request;
  }

  /**
   * Method declaration
   * 
   * 
   * @return
   * 
   * @see
   */
  public String getIconsPath() {
    ResourceLocator generalSettings = new ResourceLocator(
        "com.stratelia.webactiv.general", "fr");

    return generalSettings.getString("ApplicationURL")
        + GraphicElementFactory.getSettings().getString("IconsPath");
  }

}