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

import org.silverpeas.core.util.StringUtil;

import java.util.Date;

public class PaginationSP extends AbstractPagination {
  public PaginationSP() {
    super();
  }

  @Override
  public void init(int nbItems, int nbItemsPerPage, int firstItemIndex) {
    super.init(nbItems, nbItemsPerPage, firstItemIndex);
  }

  @Override
  public String printCounter() {
    return PaginationUtil.formatFromFirstIndexOfItem(getNbItemsPerPage(), getNbItems(),
        getFirstItemIndex());
  }

  @Override
  public String printIndex() {
    return printIndex(null);
  }

  @Override
  public String printIndex(String javascriptFunc) {
    StringBuilder result = new StringBuilder();

    if (getNbItems() > 0 && getNbItems() > getNbItemsPerPage()) {
      result.append("<div class=\"pageNav\">");
      result.append("<div class=\"pageNavContent\">");

      int nbPage = getNbPage();
      int currentPage = getCurrentPage();

      if (displayTotalNumberOfPages() && nbPage > getNumberOfPagesAround()) {
        result.append("<div class=\"pageIndex\">");
        result.append(getString("GEF.pagination.page")).append(" ").append(currentPage).append(
            getString("GEF.pagination.pageOn")).append(nbPage);
        result.append("</div>");
      }

      // display previous link (or nothing if current page is first one)
      if (getFirstItemIndex() >= getNbItemsPerPage()) {

        if (currentPage - getNumberOfPagesAround() > 1) {
          // display first page link
          result.append("<div class=\"pageOff\">");
          result.append(getLink(javascriptFunc, 0, getString("GEF.pagination.firstPage")));
          result.append("<img src=\"").append(getIconsPath()).append(
              "/arrows/arrowDoubleLeft.gif\" border=\"0\" align=\"absmiddle\" alt=\"")
              .append(getString("GEF.pagination.firstPage")).append("\"/></a>");
          result.append("</div>");
        }

        // display previous page link
        result.append("<div class=\"pageOff\">");
        int index = getIndexForPreviousPage();
        result.append(getLink(javascriptFunc, index, getString("GEF.pagination.previousPage")));
        result.append("<img src=\"").append(getIconsPath()).append(
            "/arrows/arrowLeft.gif\" border=\"0\" align=\"absmiddle\" alt=\"")
            .append(getString("GEF.pagination.previousPage")).append("\"/></a>");
        result.append("</div>");

      }

      // display all pages
      for (int i = 1; i <= nbPage; i++) {
        if (i == currentPage) {
          result.append("<div class=\"pageOn\">");
          result.append(i).append("</div>");
        } else {
          int nbDisplayPages = getNumberOfPagesAround();
          // display 10 pages (or less) before current page
          // display 10 pages (or less) after current page
          if (currentPage - nbDisplayPages <= i && i <= currentPage + nbDisplayPages) {
            int index = getIndexForDirectPage(i);
            result.append("<div class=\"pageOff\">");
            result.append(getLink(javascriptFunc, index, getString("GEF.pagination.gotoPage") + i));
            result.append(i).append("</a>");
            result.append("</div>");
          }
        }
      }

      // display next link (or nothing if current page is last one)
      if (!isLastPage()) {
        // display next page link
        int index = getIndexForNextPage();
        result.append("<div class=\"pageOff\">");
        result.append(getLink(javascriptFunc, index, getString("GEF.pagination.nextPage")));
        result.append("<img src=\"").append(getIconsPath()).append(
            "/arrows/arrowRight.gif\" border=\"0\" align=\"absmiddle\" alt=\"")
            .append(getString("GEF.pagination.nextPage")).append("\"/></a>");
        result.append("</div>");

        if (currentPage + getNumberOfPagesAround() < nbPage) {
          // display last page link
          result.append("<div class=\"pageOff\">");
          result.append(getLink(javascriptFunc, getIndexForLastPage(),
              getString("GEF.pagination.lastPage")));
          result.append("<img src=\"").append(getIconsPath()).append(
              "/arrows/arrowDoubleRight.gif\" border=\"0\" align=\"absmiddle\" alt=\"")
              .append(getString("GEF.pagination.lastPage")).append("\"/></a> ");
          result.append("</div>");
        }
      }

      long timeStamp = new Date().getTime();
      String jumperName = "jumper" + timeStamp;

      boolean displayJumper = nbPage > getNumberOfPagesAround();
      if (displayJumper) {
        // display page jumper
        result.append("<div class=\"pageJumper\">");
        result.append("<a href=\"javascript:display").append(jumperName).append(
            "()\" onfocus=\"this.blur()\" title=\"").append(getString("GEF.pagination.jumptoPage"))
            .append("\">").append(
            getString("GEF.pagination.jumper")).append(" </a>");
        result
            .append("<input type=\"text\" class=\"jumper\" id=\"").append(jumperName).append(
            "\" size=\"3\" onkeydown=\"check").append(jumperName).append("Submit(event)\"/>");
        result.append("</div>");
      }

      result.append("</div>");
      result.append("</div>");

      if (displayJumper) {
        // display page jumper script
        result.append("<script type=\"text/javascript\">");
        result.append("function display").append(jumperName).append("() {");
        result.append("var ").append(jumperName).append(" = document.getElementById(\"").append(
            jumperName).append("\");");
        result.append("if (").append(jumperName).append(".style.visibility != \"visible\") {");
        result.append("").append(jumperName).append(".style.visibility = \"visible\";");
        result.append("").append(jumperName).append(".focus();");
        result.append("} else {");
        result.append("").append(jumperName).append(".style.visibility = \"hidden\";");
        result.append("}");
        result.append("}");

        result.append("function check").append(jumperName).append("Submit(ev) {");
        result.append("var touche = ev.keyCode;");
        result.append("if (touche == 13) {");
        result.append("var index = parseInt(document.getElementById(\"").append(jumperName).append(
            "\").value);");
        result.append("if (isNaN(index) || index < 0) { index = 0; }");
        result.append("if (index > ").append(nbPage).append(") { index = ").append(nbPage).append(
            "; }");
        result.append("index = (index-1)*").append(getNbItemsPerPage()).append(";");
        if (StringUtil.isDefined(javascriptFunc)) {
          result.append(javascriptFunc).append("(index);");
        } else {
          if (getBaseURL() != null) {
            result.append("location.href=\"").append(getBaseURL()).append("\"+index;");
          } else {
            String action = "Pagination" + getActionSuffix();
            result.append("location.href=\"").append(action).append("?Index=").append("\"+index;");
          }
        }
        result.append("}");
        result.append("}");
        result.append("</script>");
      }
    }
    return result.toString();
  }

  // formatage du lien de la source de la balise href
  private String getLink(String javascriptFunc, int index, String title) {
    StringBuilder link = new StringBuilder();
    String action = "Pagination" + getActionSuffix();
    if (javascriptFunc == null) {
      if (getBaseURL() != null) {
        link.append(" <a class=\"ArrayNavigation\"").append(" title=\"").append(title).append("\"")
            .append(" href=\"").append(
            getBaseURL()).append(index);
      } else {
        // action pagination
        link.append(" <a class=\"ArrayNavigation\"").append(" title=\"").append(title).append("\"")
            .append(" href=\"").append(action)
            .append("?Index=").append(index);
      }
    } else
      link.append(" <a class=\"ArrayNavigation\"").append(" title=\"").append(title).append("\"")
          .append(" href=\"").append(
          "javascript:onClick=").append(javascriptFunc).append("(").append(
          index).append(")");
    link.append("\">");
    return link.toString();
  }

  @Override
  public String print() {
    return printIndex();
  }
}