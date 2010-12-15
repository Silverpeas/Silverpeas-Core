/**
 * Copyright (C) 2000 - 2009 Silverpeas
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
 * "http://repository.silverpeas.com/legal/licensing"
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

import com.stratelia.webactiv.util.viewGenerator.html.GraphicElementFactory;
import java.io.IOException;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.TagSupport;

public class PaginationTag extends TagSupport {

  private static final long serialVersionUID = 7931703988418922022L;
  private int currentPage;
  private int nbPages;
  private String pageParam;
  private String altPreviousAction;
  private String altNextAction;
  private String action;

  public void setCurrentPage(Integer currentPage) {
    this.currentPage = currentPage.intValue();
  }

  public void setNbPages(Integer nbPages) {
    this.nbPages = nbPages.intValue();
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

  public void setPageParam(String pageParam) {
    this.pageParam = pageParam;
  }

  @Override
  public int doEndTag() throws JspException {
    if (nbPages <= 1) {
      return EVAL_PAGE;
    }
    boolean hasParam = action.indexOf('?') > 0;
    JspWriter out = pageContext.getOut();
    try {
      out.println(
          "<table id=\"pagination\" width=\"100%\" border=\"0\" cellspacing=\"0\" cellpadding=\"2\" align=\"center\">");
      out.println("<tr valign=\"middle\" class=\"intfdcolor\">");
      out.println("<td align=\"center\" class=\"ArrayNavigation\">");
      if (currentPage > 0) {
        out.print("<a class=\"ArrayNavigation\" href=\"");
        out.print(getUrl(action, hasParam, (currentPage - 1)));
        out.print("\">");
        out.print("<img src=\"");
        out.print(getIconsPath());
        out.print("/arrows/arrowLeft.gif\" border=\"0\" align=\"absmiddle\" alt=\"");
        out.print(altPreviousAction);
        out.print("\"/></a>");
      } else {
        out.println("&#160;&#160;&#160;");
      }
      for (int i = 0; i < nbPages; i++) {
        if (i == currentPage) {
          out.print(" <span class=\"ArrayNavigationOn\">&#160;");
          out.print(i + 1);
          out.print("&#160;</span>");
        } else {
          out.print("<a class=\"ArrayNavigation\" href=\"");
          out.print(getUrl(action, hasParam, (i)));
          out.print("\">");
          out.print(i + 1);
          out.print("</a> ");
        }
      }
      if ((currentPage + 1) >= nbPages) {
        out.print("&#160;&#160;&#160;");
      } else {
        out.print("<a class=\"ArrayNavigation\" href=\"");
        out.print(getUrl(action, hasParam, (currentPage + 1)));
        out.print("\">");
        out.print("<img src=\"");
        out.print(getIconsPath());
        out.print("/arrows/arrowRight.gif\" border=\"0\" align=\"absmiddle\" alt=\"");
        out.print(altNextAction);
        out.print("\"/></a>");
      }
      out.println("</td>");
      out.println("</tr>");
      out.println("</table>");
      return EVAL_PAGE;

    } catch (IOException e) {
      throw new JspException("Pagination Tag", e);
    }
  }

  public String getUrl(String elAction, boolean hasParam, int page) {
    StringBuilder buffer = new StringBuilder(200);
    buffer.append(elAction);
    if (hasParam) {
      buffer.append('&');
    } else {
      buffer.append('?');
    }
    buffer.append(pageParam).append('=').append(page);
    return buffer.toString();
  }

  public String getIconsPath() {
    return GraphicElementFactory.getIconsPath();
  }
}
