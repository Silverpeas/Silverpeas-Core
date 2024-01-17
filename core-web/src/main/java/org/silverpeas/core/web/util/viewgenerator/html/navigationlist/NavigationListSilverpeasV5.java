/*
 * Copyright (C) 2000 - 2024 Silverpeas
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
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent)
 ---*/

/*
 * NavigationListWA2.java
 *
 * Created on 28 mars 2001, 10:32
 */

package org.silverpeas.core.web.util.viewgenerator.html.navigationlist;

import org.silverpeas.core.util.StringUtil;

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
    String title = getTitle();
    int nbCol = getNbcol();
    Collection<Item> items = getItems();

    result.append("<div class=\"tableNavigationList\">\n");
    if(StringUtil.isDefined(title)) {
      result.append("<div class=\"navigationListTitle\">");
      result.append(title);
      result.append("</div>");
    }
    result.append("</div>");

    result
        .append("<ul class=\"navigationList cols").append(nbCol).append("\">\n");


    for (Item item : items) {
      Collection<Link> links = item.getLinks();
        
        String itemURL = item.getURL();
        if (itemURL.startsWith("javascript")) {
          result.append("<li class=\"navigationListItem\" onclick=\"").append(itemURL).append("\">");
        }else{
          result.append("<li class=\"navigationListItem\">");
        }
          result.append("<a href=\"").append(itemURL).append("\">").append(item.getLabel());
            if (item.getNbelem() >= 0) {
              result.append("(").append(item.getNbelem()).append(")\n");
            }
          result.append("</a>");
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
        result.append("\n</li>");
    }
    result.append("</ul>");
    return result.toString();
  }
}