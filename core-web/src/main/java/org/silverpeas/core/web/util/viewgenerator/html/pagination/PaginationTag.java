/*
 * Copyright (C) 2000 - 2020 Silverpeas
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
package org.silverpeas.core.web.util.viewgenerator.html.pagination;

import org.silverpeas.core.web.util.viewgenerator.html.GraphicElementFactory;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.TagSupport;
import java.io.IOException;

public class PaginationTag extends TagSupport {

  private static final long serialVersionUID = 7931703988418922022L;
  private int currentPage;
  private int nbItemsPerPage;
  private int totalNumberOfItems;
  private String action;
  boolean actionIsJsFunction = false;

  public void setCurrentPage(Integer currentPage) {
    this.currentPage = currentPage;
  }

  public void setNbItemsPerPage(final int nbItemsPerPage) {
    this.nbItemsPerPage = nbItemsPerPage;
  }

  public void setTotalNumberOfItems(final int totalNumberOfItems) {
    this.totalNumberOfItems = totalNumberOfItems;
  }

  public void setAction(String action) {
    this.action = action;
  }

  public void setActionIsJsFunction(final boolean actionIsJsFunction) {
    this.actionIsJsFunction = actionIsJsFunction;
  }

  @Override
  public int doEndTag() throws JspException {
    GraphicElementFactory gef = (GraphicElementFactory) pageContext.getSession()
        .getAttribute(GraphicElementFactory.GE_FACTORY_SESSION_ATT);

    Pagination pagination = gef.getPagination(totalNumberOfItems, nbItemsPerPage, currentPage);

    JspWriter out = pageContext.getOut();
    try {
      if (!actionIsJsFunction) {
        pagination.setBaseURL(action);
        out.println(pagination.printIndex());
      } else {
        out.println(pagination.printIndex(action, true));
      }
      return EVAL_PAGE;
    } catch (IOException e) {
      throw new JspException("Pagination Tag", e);
    }
  }
}