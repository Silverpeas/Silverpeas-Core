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

public class PaginationSilverpeasV5 extends AbstractPagination {

  public PaginationSilverpeasV5() {
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
      result
          .append(
          "<table width=\"100%\" border=\"0\" cellspacing=\"0\" cellpadding=\"2\" align=\"center\">");
      result.append("<tr valign=\"middle\" class=\"intfdcolor\">");
      result.append("<td align=\"center\">");

      String action = "Pagination" + getActionSuffix();
      String altNextPage = getAltNextPage();
      String altPreviousPage = getAltPreviousPage();

      int nbPage = getNbPage();
      int currentPage = getCurrentPage();
      int index = -1;

      if (getFirstItemIndex() >= getNbItemsPerPage()) {
        index = getIndexForPreviousPage();

        // formatage du lien de la source de la balise href
        if (javascriptFunc == null) {
          result.append(" <a href=\"").append(action).append("?Index=").append(
              index);
        } else {
          result.append(" <a href=\"").append("javascript:onClick=").append(
              javascriptFunc).append("(").append(index).append(")");
        }

        result.append("\"><img src=\"").append(getIconsPath()).append(
            "/arrows/arrowLeft.gif\" border=\"0\" align=\"absmiddle\" alt=\"").append(
            altPreviousPage).append("\"/></a> ");
      } else {
        result.append("&#160;&#160;&#160;");
      }
      for (int i = 1; i <= nbPage; i++) {
        if (i == currentPage) {
          result.append(" ").append(i).append(" ");
        } else {
          index = getIndexForDirectPage(i);
          // formatage du lien de la source de la balise href
          if (javascriptFunc == null) {
            result.append(" <a href=\"").append(action).append("?Index=").append(index);
          } else {
            result.append(" <a href=\"").append("javascript:onClick=").append(
                javascriptFunc).append("(").append(index).append(")");
          }
          result.append("\">").append(i).append("</a> ");
        }
      }
      if (!isLastPage()) {
        index = getIndexForNextPage();

        // formatage du lien de la source de la balise href
        if (javascriptFunc == null) {
          result.append(" <a href=\"").append(action).append("?Index=").append(
              index);
        } else {
          result.append(" <a href=\"").append("javascript:onClick=").append(
              javascriptFunc).append("(").append(index).append(")");
        }

        result.append("\"><img src=\"").append(getIconsPath()).append(
            "/arrows/arrowRight.gif\" border=\"0\" align=\"absmiddle\" alt=\"").append(altNextPage)
            .
            append("\"/></a>");
      } else {
        result.append("&#160;&#160;&#160;");
      }
      result.append("</td>");
      result.append("</tr>");
      result.append("</table>");
    }
    return result.toString();
  }

  @Override
  public String print() {
    return printIndex();
  }
}