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

import java.io.IOException;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;

import com.stratelia.webactiv.util.viewGenerator.html.GraphicElementFactory;

public class PaginationTag extends TagSupport {
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

  public int doEndTag() throws JspException {
    if (nbPages <= 1) {
      return EVAL_PAGE;
    }
    boolean hasParam = action.indexOf('?') > 0;
    try {
      pageContext.getOut().println(
          "<table id=\"pagination\" width=\"100%\" border=\"0\" "
          + "cellspacing=\"0\" cellpadding=\"2\" align=\"center\">");
      pageContext.getOut().println(
          "<tr valign=\"middle\" class=\"intfdcolor\">");
      pageContext.getOut().println(
          "<td align=\"center\" class=\"ArrayNavigation\">");
      if (currentPage > 0) {
        pageContext.getOut().print("<a class=\"ArrayNavigation\" href=\"");
        pageContext.getOut().print(getUrl(action, hasParam, (currentPage - 1)));
        pageContext.getOut().print("\">");
        pageContext
            .getOut()
            .print(
            "<img src=\""
            + getIconsPath()
            + "/arrows/arrowLeft.gif\" border=\"0\" align=\"absmiddle\" alt=\""
            + altPreviousAction + "\"/></a>");
      } else {
        pageContext.getOut().println("&#160;&#160;&#160;");
      }
      for (int i = 0; i < nbPages; i++) {
        if (i == currentPage) {
          pageContext.getOut().println(
              " <span class=\"ArrayNavigationOn\">&#160;" + (i + 1)
              + "&#160;</span>");
        } else {
          pageContext.getOut().print("<a class=\"ArrayNavigation\" href=\"");
          pageContext.getOut().print(getUrl(action, hasParam, (i)));
          pageContext.getOut().print("\">");
          pageContext.getOut().print(i + 1);
          pageContext.getOut().print("</a> ");
        }
      }
      if ((currentPage + 1) >= nbPages) {
        pageContext.getOut().print("&#160;&#160;&#160;");
      } else {
        pageContext.getOut().print("<a class=\"ArrayNavigation\" href=\"");
        pageContext.getOut().print(getUrl(action, hasParam, (currentPage + 1)));
        pageContext.getOut().print("\">");
        pageContext
            .getOut()
            .print(
            "<img src=\""
            + getIconsPath()
            + "/arrows/arrowRight.gif\" border=\"0\" align=\"absmiddle\" alt=\""
            + altNextAction + "\"/></a>");
      }
      pageContext.getOut().println("</td>");
      pageContext.getOut().println("</tr>");
      pageContext.getOut().println("</table>");
      return EVAL_PAGE;

    } catch (IOException e) {
      throw new JspException("Pagination Tag", e);
    }
  }

  public String getUrl(String elAction, boolean hasParam, int page) {
    StringBuffer buffer = new StringBuffer(200);
    buffer.append(elAction);
    if (hasParam) {
      buffer.append('&');
    } else {
      buffer.append('?');
    }
    buffer.append(pageParam + '=' + page);
    return buffer.toString();
  }

  public String getIconsPath() {
    return GraphicElementFactory.getIconsPath();
  }
}
