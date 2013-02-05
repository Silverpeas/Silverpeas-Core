/**
 * Copyright (C) 2000 - 2012 Silverpeas
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

package com.stratelia.webactiv.util.viewGenerator.html.pagination;

import java.io.IOException;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.TagSupport;
import com.stratelia.webactiv.util.viewGenerator.html.GraphicElementFactory;

public class PaginationTag extends TagSupport {

  private static final long serialVersionUID = 7931703988418922022L;
  private int currentPage;
  private int nbPages;
  private String action;
  private String pageParam;
  private String altPreviousAction;
  private String altNextAction;
  private String altGoToAction;

  public void setCurrentPage(Integer currentPage) {
    this.currentPage = currentPage;
  }

  public void setNbPages(Integer nbPages) {
    this.nbPages = nbPages;
  }

  public void setAction(String action) {
    this.action = action;
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
    if (this.nbPages <= 1) {
      return EVAL_PAGE;
    }
    boolean hasParam = action.indexOf('?') > 0;
    JspWriter out = pageContext.getOut();
    try {
      out.println("<table id=\"pagination\" width=\"100%\" border=\"0\" cellspacing=\"0\" cellpadding=\"2\" align=\"center\">");
      out.println("<tr valign=\"middle\" class=\"intfdcolor\">");
      out.println("<td align=\"center\" class=\"ArrayNavigation\">");
      out.println("<div class=\"pageNav\">");
      out.println("<div class=\"pageNavContent\">");

      // display previous link (or nothing if current page is first one)
      if (this.currentPage > 0) {

        // display previous page link
        out.println("<div class=\"pageOff\">");
        out.println(getUrl(action, hasParam, this.altPreviousAction, (this.currentPage - 1)));
            out.println("<img src=\""+getIconsPath()+
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
          out.println("<div class=\"pageOff\">");
          out.println(getUrl(action, hasParam, this.altGoToAction + " " + (i+1), (i)));
          out.println(i+1);
          out.println("</a>");
          out.println("</div>");
        }
      }

      // display next link (or nothing if current page is last one)
      if ((this.currentPage + 1) < this.nbPages) {
        // display next page link
        out.println("<div class=\"pageOff\">");
        out.println(getUrl(action, hasParam, this.altNextAction, (this.currentPage + 1)));
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

  public String getUrl(String elAction, boolean hasParam, String title, int page) {
    StringBuilder buffer = new StringBuilder(200);
    buffer.append(" <a class=\"ArrayNavigation\"").append(" title=\"").append(title).append("\"");
    buffer.append(" href=\"").append(elAction);
    if (hasParam) {
      buffer.append('&');
    } else {
      buffer.append('?');
    }
    buffer.append(pageParam).append('=').append(page);
    buffer.append("\">");
    return buffer.toString();
  }

  public String getIconsPath() {
    return GraphicElementFactory.getIconsPath();
  }
}
