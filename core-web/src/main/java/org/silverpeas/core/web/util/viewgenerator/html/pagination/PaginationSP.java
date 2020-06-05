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

import org.apache.ecs.html.IMG;
import org.silverpeas.core.util.StringUtil;

import java.util.Date;
import java.util.List;

public class PaginationSP extends AbstractPagination {

  private static final String INDEX_PARAM = "?Index=";

  public PaginationSP() {
    super();
  }

  @Override
  public String printCounter() {
    return PaginationUtil.formatFromFirstIndexOfItem(getNbItemsPerPage(), getNbItems(),
        getFirstItemIndex());
  }

  @Override
  public String printIndex() {
    return printIndex(null, true);
  }

  @Override
  public String printIndex(String javascriptFunc) {
    return printIndex(javascriptFunc, StringUtil.isNotDefined(javascriptFunc));
  }

  @Override
  public String printIndex(String javascriptFunc, boolean nbItemsPerPage) {
    StringBuilder result = new StringBuilder();

    if (getNbItems() > getIndexThreshold()) {
      result.append("<div class=\"pageNav\">");
      result.append("<div class=\"pageNavContent\">");

      // display current area (1-25/143)
      int maxCurrentIndex = Math.min(getFirstItemIndex() + getNbItemsPerPage(), getNbItems());
      result.append("<div class=\"itemIndex\">");
      result.append(getFirstItemIndex() + 1).append("-").append(maxCurrentIndex);
      result.append("/").append(getNbItems());
      result.append("</div>");

      // display current page
      result.append(getPageIndexFragment());

      result.append(getPageBrowsingFragment(javascriptFunc));

      result.append(getNbItemsPerPageFragment(nbItemsPerPage, javascriptFunc));

      result.append(getJumperFragment("</div></div>", javascriptFunc));
    }
    return result.toString();
  }

  private String getPageBrowsingFragment(String javascriptFunc) {
    StringBuilder result = new StringBuilder();
    result.append("<div class=\"pageClicToGo\">");

    // display previous link (or nothing if current page is first one)
    if (getFirstItemIndex() >= getNbItemsPerPage()) {
      if (getCurrentPage() - getNumberOfPagesAround() > 1) {
        // display first page link
        result.append("<a class=\"pageOff\"");
        result.append(getLink(javascriptFunc, 0, getString("GEF.pagination.firstPage")));
        result.append(getImg("/arrows/arrowDoubleLeft.png", "GEF.pagination.firstPage"));
        result.append("</a>");
      }

      // display previous page link
      result.append("<a class=\"pageOff\"");
      int index = getIndexForPreviousPage();
      result.append(getLink(javascriptFunc, index, getString("GEF.pagination.previousPage")));
      result.append(getImg("/arrows/arrowLeft.png", "GEF.pagination.previousPage"));
      result.append("</a>");
    }

    // display all pages
    if (getNbPage() > 1) {
      for (int i = 1; i <= getNbPage(); i++) {
        if (i == getCurrentPage()) {
          result.append("<a class=\"pageOn\">");
          result.append(i).append("</a>");
        } else {
          int nbDisplayPages = getNumberOfPagesAround();
          // display 3 pages (or less) before current page
          // display 3 pages (or less) after current page
          if (getCurrentPage() - nbDisplayPages <= i && i <= getCurrentPage() + nbDisplayPages) {
            int index = getIndexForDirectPage(i);
            result.append("<a class=\"pageOff\"");
            result.append(getLink(javascriptFunc, index,
                getStringWithParam("GEF.pagination.gotoPage", String.valueOf(i))));
            result.append(i).append("</a>");
          }
        }
      }
    }

    // display next link (or nothing if current page is last one)
    if (!isLastPage()) {
      // display next page link
      int index = getIndexForNextPage();
      result.append("<a class=\"pageOff\"");
      result.append(getLink(javascriptFunc, index, getString("GEF.pagination.nextPage")));
      result.append(getImg("/arrows/arrowRight.png", "GEF.pagination.nextPage"));
      result.append("</a>");

      if (getCurrentPage() + getNumberOfPagesAround() < getNbPage()) {
        // display last page link
        result.append("<a class=\"pageOff\"");
        result.append(getLink(javascriptFunc, getIndexForLastPage(),
            getString("GEF.pagination.lastPage")));
        result.append(getImg("/arrows/arrowDoubleRight.png", "GEF.pagination.lastPage"));
        result.append("</a>");
      }
    }
    result.append("</div>");
    return result.toString();
  }

  private String getPageIndexFragment() {
    StringBuilder fragment = new StringBuilder();
    if (displayTotalNumberOfPages() && getNbPage() > getNumberOfPagesAround()) {
      fragment.append("<div class=\"pageIndex\">");
      fragment.append(getStringWithParam("GEF.pagination.pageOn", String.valueOf(getCurrentPage()),
          String.valueOf(getNbPage())));
      fragment.append("</div>");
    }
    return fragment.toString();
  }

  private String getJumperFragment(String beforeScript, String javascriptFunc) {
    StringBuilder fragment = new StringBuilder();
    boolean displayJumper = getNbPage() > getJumperThreshold();
    long timeStamp = new Date().getTime();
    String jumperName = "jumper" + timeStamp;
    if (displayJumper) {
      // display page jumper
      fragment.append("<div class=\"pageJumper\">");
      fragment.append("<a href=\"javascript:display").append(jumperName).append(
          "()\" onfocus=\"this.blur()\" title=\"").append(getString("GEF.pagination.jumptoPage"))
          .append("\">").append(
          getString("GEF.pagination.jumper")).append(" </a>");
      fragment
          .append("<input type=\"text\" class=\"jumper\" id=\"").append(jumperName).append(
          "\" size=\"3\" onkeydown=\"check").append(jumperName).append("Submit(event)\"/>");
      fragment.append("</div>\n");
    }

    fragment.append(beforeScript);

    if (displayJumper) {
      fragment.append(getJumperScript(jumperName, javascriptFunc));
    }

    return fragment.toString();
  }

  private String getNbItemsPerPageFragment(boolean nbItemsPerPage, String javascriptFunc) {
    StringBuilder fragment = new StringBuilder();
    if (nbItemsPerPage && getNbItems() > getNumberPerPageThreshold()) {
      fragment.append("<div class=\"pageIndex numberPerPage\">");
      List<Integer> values = getNbItemPerPageList();
      for (int i = 0; i < values.size(); i++) {
        int value = values.get(i);
        if (getNbItems() > value || (i != 0 && getNbItems() > values.get(i - 1))) {
          if (getNbItemsPerPage() == value) {
            fragment.append("<a class=\"selected\">").append(value).append("</a>");
          } else {
            fragment.append("<a href=\"").append(getNbItemsPerPageLink(javascriptFunc, value))
                .append("\" title=\"")
                .append(getStringWithParam("GEF.pagination.perPage", String.valueOf(value))).append("\">")
                .append(value).append("</a>");
          }
        }
      }
      int allThreshold = getPaginationAllThreshold();
      if (getNbItems() < allThreshold) {
        // add special feature : All
        if (getNbItemsPerPage() == allThreshold) {
          fragment.append("<a class=\"selected\">").append(getString("GEF.pagination.all"))
              .append("</a>");
        } else {
          fragment.append("<a href=\"").append(getNbItemsPerPageLink(javascriptFunc, allThreshold))
              .append("\" title=\"").append(getString("GEF.pagination.all.title")).append("\">")
              .append(getString("GEF.pagination.all")).append("</a>");
        }
      }
      fragment.append("</div>");
    }
    return fragment.toString();
  }

  // formatage du lien de la source de la balise href
  private String getNbItemsPerPageLink(String javascriptFunc, int nbItems) {
    StringBuilder link = new StringBuilder();
    if (StringUtil.isNotDefined(javascriptFunc)) {
      if (getBaseURL() != null) {
        link.append(getBaseURL()).append("0").append("&" + ITEMS_PER_PAGE_PARAM + "=").append(nbItems);
      }
    } else {
      link.append("javascript:onclick=").append(javascriptFunc).append("(0, ").append(nbItems).append(")");
    }
    return link.toString();
  }

  // formatage du lien de la source de la balise href
  private String getLink(String javascriptFunc, int index, String title) {
    StringBuilder link = new StringBuilder();
    link.append(" title=\"").append(title).append("\"").append(" href=\"");
    String action = "Pagination" + getActionSuffix();
    if (StringUtil.isNotDefined(javascriptFunc)) {
      if (getBaseURL() != null) {
        link.append(getBaseURL()).append(index);
      } else {
        // action pagination
        link.append(action).append(INDEX_PARAM).append(index);
      }
    } else {
      link.append("javascript:onClick=").append(javascriptFunc).append("(").append(index).append(")");
    }
    link.append("\">");
    return link.toString();
  }

  private String getImg(String imgSrc, String altKey) {
    IMG img = new IMG(getIconsPath()+imgSrc);
    img.setBorder(0);
    img.setAlign("absmiddle");
    img.setAlt(getString(altKey));
    return img.toString();
  }

  private String getJumperScript(String jumperName, String javascriptFunc) {
    StringBuilder result = new StringBuilder();
    // display page jumper script
    result.append("<script type=\"text/javascript\">");
    result.append("function display").append(jumperName).append("() {");
    result.append("var $jumperInput = jQuery('#").append(jumperName).append("');");
    result.append("if ($jumperInput.css('visibility') !== 'visible') {");
    result.append("$jumperInput.css('visibility', 'visible');");
    result.append("$jumperInput.hide();$jumperInput.focus();");
    result.append("$jumperInput.show();$jumperInput.focus();");
    result.append("} else {");
    result.append("$jumperInput.css('visibility', 'hidden');");
    result.append("}");
    result.append("}");

    result.append("function check").append(jumperName).append("Submit(ev) {");
    result.append("var touche = ev.keyCode;");
    result.append("if (touche == 13) {");
    result.append("var index = parseInt(document.getElementById(\"").append(jumperName).append(
        "\").value);");
    result.append("if (isNaN(index) || index <= 0) { index = 1; }");
    result.append("if (index > ").append(getNbPage()).append(") { index = ").append(getNbPage())
        .append("; }");
    result.append("index = (index-1)*").append(getNbItemsPerPage()).append(";");
    if (StringUtil.isDefined(javascriptFunc)) {
      result.append(javascriptFunc).append("(index);");
    } else {
      if (getBaseURL() != null) {
        result.append("if (typeof ").append(jumperName).append(".ajax === 'function') {");
        result.append(jumperName).append(".ajax('").append(getBaseURL()).append("'+index").append(")");
        result.append("} else {location.href=\"").append(getBaseURL()).append("\"+index;}");
      } else {
        String action = "Pagination" + getActionSuffix();
        result.append("if (typeof ").append(jumperName).append(".ajax === 'function') {");
        result.append(jumperName).append(".ajax('").append(action).append(INDEX_PARAM).append("'+index").append(")");
        result.append("} else {location.href=\"").append(action).append(INDEX_PARAM).append("\"+index;}");
      }
    }
    result.append("}");
    result.append("}");
    result.append("</script>\n");
    return result.toString();
  }

  @Override
  public String print() {
    return printIndex();
  }
}