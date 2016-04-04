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

/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent)
 ---*/

/*
 * NavigationListWA2.java
 *
 * Created on 28 mars 2001, 10:32
 */

package org.silverpeas.core.web.util.viewgenerator.html.navigationlist;

import java.util.Collection;

/**
 * @author lloiseau
 * @version 1.0 modif. Marc Raverdy 24/07/2001
 */
public class NavigationListSilverpeasV5 extends AbstractNavigationList {

  /**
   * Creates new NavigationListKudelski
   */
  public NavigationListSilverpeasV5() {
    super();
  }

  /**
   * @return the HTML code of the navigation list
   */
  public String print() {
    StringBuilder result = new StringBuilder(50);
    String iconsPath = getIconsPath() + "/navigationList/";
    String title = getTitle();
    int nbCol = getNbcol();
    Collection<Item> items = getItems();
    boolean endRaw = false;
    int nbTd = 0;

    result.append("<CENTER>");
    result
        .append("<table width=\"98%\" border=\"0\" cellspacing=\"0\" cellpadding=\"1\" class=tableNavigationList>\n");
    result.append("<tr>\n");
    result
        .append("<td class=\"navigationListTitle\" nowrap align=center height=\"19\">\n");
    result.append(title);
    result.append("</td>\n");
    result.append("</tr>\n");
    result.append("<tr><td>\n");
    result
        .append("<table width=\"100%\" border=\"0\" cellspacing=\"0\" cellpadding=\"3\">\n");

    int j = 1;

    for (Item item : items) {
      Collection<Link> links = item.getLinks();

      if (j == 1) {
        result.append("<tr>\n");
        result.append("<td width=\"2%\">&nbsp;</td>\n");
        endRaw = false;
      }
      if (j <= nbCol) {
        result.append("<td valign=\"top\" width=\"").append((98 / nbCol))
            .append("%\">\n");
        result
            .append("\t\t\t<table width=\"100%\" border=\"0\" cellspacing=\"0\" cellpadding=\"0\">\n");
        result.append("\t\t\t\t<tr>\n");
        result.append("\t\t\t\t\t<td width=\"5\" valign=middle><img src=\"")
            .append(iconsPath).append("puce.gif\" border=\"0\">&nbsp;</td>\n");
        result.append("\t\t\t\t\t<td valign=middle>\n");
        result.append("\t\t\t\t\t<a href=\"").append(item.getURL()).append(
            "\"><B>").append(item.getLabel()).append("</B></a>");
        if (item.getNbelem() >= 0) {
          result.append("<i>(").append(item.getNbelem()).append(")</i>\n");
        }
        if (item.getUniversalLink() != null) {
          result.append("&nbsp;").append(item.getUniversalLink());
        }
        result.append("\t\t\t\t\t</td>\n");
        result.append("\t\t\t\t</tr>\n");
        if (item.getInfo() != null) {
          result.append("\t\t\t\t<tr>\n");
          result.append("\t\t\t\t\t<td>&nbsp;</td>\n");
          result.append("\t\t\t\t\t<td>").append(item.getInfo()).append(
              "</td>\n");
          result.append("\t\t\t\t</tr>\n");
        }
        if (links != null) {
          result.append("\t\t\t\t<tr>\n");
          result.append("\t\t\t\t\t<td>&nbsp;</td>\n");
          result.append("\t\t\t\t\t<td>");

          for (Link link : links) {
            result.append("\n\t\t<a href=\"").append(link.getURL()).append(
                "\" class=\"txtnote\">").append(link.getLabel()).append(
                "</a>&nbsp&nbsp");
          }
          result.append("</td>\n");
          result.append("\t\t\t\t</tr>\n");
        }
        result.append("\t\t\t</table>\n");
        result.append("\n\t\t</td>");
        j++;
      }
      if (j > nbCol) {
        result.append("\t</tr>");
        endRaw = true;
        j = 1;
      }
    }
    if (!endRaw) {
      nbTd = nbCol - j + 1;
      int k = 1;

      while (k <= nbTd) {
        result.append("<td valign=\"top\">&nbsp;</td>\n");
        k++;
      }
      result.append("</tr>\n");
    }
    result.append("</table></td></tr></table>");
    result.append("</CENTER>");
    return result.toString();
  }
}