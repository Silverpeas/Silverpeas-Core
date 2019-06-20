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
package org.silverpeas.core.web.util.viewgenerator.html.list;

import org.apache.ecs.ElementContainer;
import org.apache.ecs.xhtml.div;
import org.silverpeas.core.admin.PaginationPage;
import org.silverpeas.core.web.util.viewgenerator.html.GraphicElementFactory;
import org.silverpeas.core.web.util.viewgenerator.html.pagination.Pagination;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpSession;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.BodyTagSupport;

import static org.silverpeas.core.util.StringUtil.isDefined;
import static org.silverpeas.core.web.util.viewgenerator.html.pagination.Pagination
    .INDEX_PARAMETER_NAME;
import static org.silverpeas.core.web.util.viewgenerator.html.pagination.Pagination
    .ITEMS_PER_PAGE_PARAM;

/**
 * Create a new ListPane.
 * @author silveryocha
 */
public class ListPaneTag extends BodyTagSupport {
  private static final long serialVersionUID = 1370094709020971998L;
  private static final int DEFAULT_NB_ITEM_PER_PAGE = 10;
  private String var;
  private String routingAddress = null;
  private int numberLinesPerPage = DEFAULT_NB_ITEM_PER_PAGE;
  private PaginationPage page;
  private int nbItems = 0;

  public String getRoutingAddress() {
    return routingAddress;
  }

  public void setRoutingAddress(String routingAddress) {
    this.routingAddress = routingAddress;
  }

  @Override
  public int doStartTag() throws JspException {
    nbItems = 0;
    initNbLinesPerPage();
    return EVAL_BODY_BUFFERED;
  }

  @Override
  public int doEndTag() throws JspException {
    final Pagination currentPagination = getPagination(getNbItems());
    String baseUrl = getRoutingAddress();
    StringBuilder url = new StringBuilder(baseUrl);
    if (baseUrl.indexOf('?') < 0) {
      url.append("?");
    } else {
      url.append("&");
    }
    url.append(INDEX_PARAMETER_NAME).append("=");
    currentPagination.setBaseURL(url.toString());

    div listPane = new div();
    if (isDefined(getId())) {
      listPane.setID(getId());
    }
    listPane.setClass("list-pane");
    div paginationNav = new div();
    paginationNav.setClass("list-pane-nav");
    paginationNav.addElement(currentPagination.print());
    listPane.addElement(getBodyContent().getString());
    listPane.addElement(paginationNav);

    new ElementContainer().addElement(listPane).output(pageContext.getOut());
    return EVAL_PAGE;
  }

  /**
   * The name of the HttpSession attribute that contains the Pagination.
   * @param name the name which references the pagination into the session.
   */
  public void setVar(final String name) {
    this.var = name;
  }

  public void setNumberLinesPerPage(int numberLinesPerPage) {
    this.numberLinesPerPage = numberLinesPerPage;
  }

  public void setPage(final PaginationPage page) {
    this.page = page;
  }

  /**
   * Gets the current pagination.
   * @param nbItems the current known number of items.
   * @return the {@link Pagination} instance.
   */
  Pagination getPagination(final int nbItems) {
    final String cacheKey = this.getClass().getSimpleName() + "pagination" + this.var;
    Pagination pagination = (Pagination) getRequest().getAttribute(cacheKey);
    if (pagination == null) {
      GraphicElementFactory gef = getGraphicElementFactory();
      pagination = gef.getPagination(nbItems, getState().getMaximumVisibleLine(),
          getState().getFirstVisibleLine());
      getRequest().setAttribute(cacheKey, pagination);
    } else {
      if (nbItems < getState().getFirstVisibleLine()) {
        getState().setFirstVisibleLine(0);
      }
      pagination
          .init(nbItems, getState().getMaximumVisibleLine(), getState().getFirstVisibleLine());
    }
    return pagination;
  }

  private void initNbLinesPerPage() {
    final String nbLines = getRequest().getParameter(ITEMS_PER_PAGE_PARAM);
    if (isDefined(nbLines)) {
      getState().setMaximumVisibleLine(Integer.valueOf(nbLines));
    }
    final String index = getRequest().getParameter(INDEX_PARAMETER_NAME);
    if (isDefined(index)) {
      getState().setFirstVisibleLine(Integer.parseInt(index));
    }
  }

  int getNbItems() {
    return nbItems;
  }

  void setNbItems(final int nbItems) {
    this.nbItems = nbItems;
  }

  State getState() {
    final String sessionKey = this.getClass().getSimpleName() + this.var;
    State state = (State) getSession().getAttribute(sessionKey);
    if (state == null) {
      state = new State(numberLinesPerPage);
      getSession().setAttribute(sessionKey, state);
    }
    if (page != null) {
      state.setFirstVisibleLine((page.getPageNumber() - 1) * page.getPageSize());
      state.setMaximumVisibleLine(page.getPageSize());
    }
    return state;
  }

  private GraphicElementFactory getGraphicElementFactory() {
    return (GraphicElementFactory) getSession()
        .getAttribute(GraphicElementFactory.GE_FACTORY_SESSION_ATT);
  }

  private ServletRequest getRequest() {
    return pageContext.getRequest();
  }

  private HttpSession getSession() {
    return pageContext.getSession();
  }

  static class State {
    private int firstVisibleLine = 0;
    private int maximumVisibleLine = DEFAULT_NB_ITEM_PER_PAGE;

    public State(final int maximumVisibleLine) {
      this.maximumVisibleLine = maximumVisibleLine;
    }

    int getFirstVisibleLine() {
      return firstVisibleLine;
    }

    void setFirstVisibleLine(final int firstVisibleLine) {
      this.firstVisibleLine = firstVisibleLine;
    }

    int getMaximumVisibleLine() {
      return maximumVisibleLine;
    }

    void setMaximumVisibleLine(final int maximumVisibleLine) {
      this.maximumVisibleLine = maximumVisibleLine;
    }
  }
}
