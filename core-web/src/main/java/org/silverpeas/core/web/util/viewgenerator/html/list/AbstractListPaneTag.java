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

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpSession;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.BodyTagSupport;

/**
 * Centralization of common code.
 * @author silveryocha
 */
public abstract class AbstractListPaneTag extends BodyTagSupport {
  private static final long serialVersionUID = -2052000122857193772L;
  static final int DEFAULT_NB_ITEM_PER_PAGE = 10;
  private String var;
  private String routingAddress = null;
  private int nbItems = 0;

  public String getRoutingAddress() {
    return routingAddress;
  }

  public void setRoutingAddress(String routingAddress) {
    this.routingAddress = routingAddress;
  }
  /**
   * The name of the HttpSession attribute that contains the Pagination.
   * @param name the name which references the pagination into the session.
   */
  public void setVar(final String name) {
    this.var = name;
  }

  int getNbItems() {
    return nbItems;
  }

  void setNbItems(final int nbItems) {
    this.nbItems = nbItems;
  }

  String getVar() {
    return var;
  }

  @Override
  public int doStartTag() throws JspException {
    nbItems = 0;
    return super.doStartTag();
  }

  protected ServletRequest getRequest() {
    return pageContext.getRequest();
  }

  protected HttpSession getSession() {
    return pageContext.getSession();
  }
}
