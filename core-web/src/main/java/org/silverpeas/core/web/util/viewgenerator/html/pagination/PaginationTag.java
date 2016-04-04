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

package org.silverpeas.core.web.util.viewgenerator.html.pagination;

import org.silverpeas.core.web.util.viewgenerator.html.GraphicElementFactory;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.TagSupport;
import java.io.IOException;

public class PaginationTag extends TagSupport {

  private static final long serialVersionUID = 7931703988418922022L;
  private int currentPage;
  private int nbPages;
  private int nbItemsPerPage;
  private int totalNumberOfItems;
  private String action;
  boolean actionIsJsFunction = false;
  private String pageParam;
  private String altPreviousAction;
  private String altNextAction;
  private String altGoToAction;

  boolean hasParam = false;

  public void setCurrentPage(Integer currentPage) {
    this.currentPage = currentPage;
  }

  public void setNbPages(Integer nbPages) {
    this.nbPages = nbPages;
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

  public void setAltPreviousAction(String altPreviousAction) {
    this.altPreviousAction = altPreviousAction;
  }

  public void setAltNextAction(String altNextAction) {
    this.altNextAction = altNextAction;
  }

  public void setAltGoToAction(String altGoToAction) {
    this.altGoToAction = altGoToAction;
  }

  public void setPageParam(String pageParam) {
    this.pageParam = pageParam;
  }

  @Override
  public int doEndTag() throws JspException {
    GraphicElementFactory gef = (GraphicElementFactory) pageContext.getSession()
        .getAttribute(GraphicElementFactory.GE_FACTORY_SESSION_ATT);

    if (actionIsJsFunction) {
      nbPages = PaginationUtil.countTotalNumberOfPages(nbItemsPerPage, totalNumberOfItems);
    }
    if (this.nbPages <= 1) {
      return EVAL_PAGE;
    }

    hasParam = action.indexOf('?') > 0;

    JspWriter out = pageContext.getOut();
    try {
      out.println("<table id=\"pagination\" width=\"100%\" border=\"0\" cellspacing=\"0\" cellpadding=\"2\" align=\"center\">");
      out.println("<tr valign=\"middle\" class=\"intfdcolor\">");
      out.println("<td align=\"center\" class=\"ArrayNavigation\">");
      out.println("<div class=\"pageNav\">");
      out.println("<div class=\"pageNavContent\">");

      // display previous link (or nothing if current page is first one)
      if (this.currentPage > 0) {
        if (this.altPreviousAction == null && gef != null) {
          this.altPreviousAction = gef.getMultilang().getString("GEF.pagination.previousPage");
        }

        // display previous page link
        out.println("<div class=\"pageOff\">");
        out.println(getUrl(action, this.altPreviousAction, (this.currentPage - 1)));
        out.println("<img src=\"" + getIconsPath() +
            "/arrows/arrowLeft.gif\" border=\"0\" align=\"absmiddle\" alt=\""+
            this.altPreviousAction+"\"/></a>");
        out.println("</div>");
      }

      // display all pages
      for (int i=0; i < this.nbPages; i++) {
        if (i == this.currentPage) {
          out.println("<div class=\"pageOn\">");
          out.println(i+1);
          out.println("</div>");
        } else {
          if (this.altGoToAction == null && gef != null) {
            this.altGoToAction = gef.getMultilang().getString("GEF.pagination.gotoPage");
          }
          out.println("<div class=\"pageOff\">");
          out.println(getUrl(action, this.altGoToAction + " " + (i + 1), (i)));
          out.println(i+1);
          out.println("</a>");
          out.println("</div>");
        }
      }

      // display next link (or nothing if current page is last one)
      if ((this.currentPage + 1) < this.nbPages) {
        if (this.altNextAction == null && gef != null) {
          this.altNextAction = gef.getMultilang().getString("GEF.pagination.nextPage");
        }
        // display next page link
        out.println("<div class=\"pageOff\">");
        out.println(getUrl(action, this.altNextAction, (this.currentPage + 1)));
        out.println("<img src=\""+getIconsPath()+
            "/arrows/arrowRight.gif\" border=\"0\" align=\"absmiddle\" alt=\""+
            this.altNextAction+"\"/></a>");
        out.println("</div>");
      }

      out.println("</div>");
      out.println("</div>");
      out.println("</td>");
      out.println("</tr>");
      out.println("</table>");

      return EVAL_PAGE;
    } catch (IOException e) {
      throw new JspException("Pagination Tag", e);
    }
  }

  public String getUrl(String elAction, String title, int page) {
    StringBuilder buffer = new StringBuilder(200);
    buffer.append(" <a class=\"ArrayNavigation\"").append(" title=\"").append(title).append("\"");
    buffer.append(" href=\"");
    if (!actionIsJsFunction) {
      buffer.append(elAction);
      if (hasParam) {
        buffer.append('&');
      } else {
        buffer.append('?');
      }
      buffer.append(pageParam).append('=').append(page);
    } else {
      buffer.append("javascript:onClick=").append(elAction).append("(").append(page)
          .append(")");
    }
    buffer.append("\">");
    return buffer.toString();
  }

  public String getIconsPath() {
    return GraphicElementFactory.getIconsPath();
  }
}
