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

    result.append("<div class=\"tableNavigationList\">\n");

    result.append("<div class=\"navigationListTitle\">");
    result.append(title);
    result.append("</div>");
    
    result.append("</div>");

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

        result.append("<a href=\"").append(item.getURL()).append("\">").append(item.getLabel()).append("</a>");
        if (item.getNbelem() >= 0) {
          result.append("(").append(item.getNbelem()).append(")\n");
        }
        if (item.getUniversalLink() != null) {
          result.append("&nbsp;").append(item.getUniversalLink());
        }

        if (item.getInfo() != null) {
          result.append("<div>").append(item.getInfo()).append("</div>");
        }
        if (links != null) {


          for (Link link : links) {
            result.append("<a href=\"").append(link.getURL()).append("\" class=\"txtnote\">").append(link.getLabel()).append("</a>");
          }
        }
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
    result.append("</table>");
    return result.toString();
  }
}