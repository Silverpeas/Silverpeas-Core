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
 * NavigationList.java
 *
 * Created on 28 mars 2001, 09:07
 */

package org.silverpeas.core.web.util.viewgenerator.html.navigationlist;

import org.silverpeas.core.web.util.viewgenerator.html.SimpleGraphicElement;
import java.util.Collection;

/**
 * NavigationList is an interface to be implemented by a graphic element Very usefull to create a
 * list of items in an html format.
 * @author lloiseau
 * @version 1.0
 */
public interface NavigationList extends SimpleGraphicElement {
  /**
   * Add an item with label, number of elements, information in the navigation list and an universal
   * link
   * @param label - string that describe the item
   * @param nbelem - give the number of element contained by the item For exemple, if the item is a
   * directory, "nbelem" is the number of files you can find in this directory
   * @param info - It can be everything ... (only string)
   * @param universalLink - a link as string containing an universal link
   */
  public void addItem(String label, String URL, int nbelem, String info,
      String universalLink);

  /**
   * Add an item with label, number of elements and information in the navigation list
   * @param label string that describe the item
   * @param nbelem give the number of element contained by the item For exemple, if the item is a
   * directory, "nbelem" is the number of files you can find in this directory
   * @param info It can be everything ... (only string)
   */
  public void addItem(String label, String URL, int nbelem, String info);

  /**
   * Add an item with label, number of elements and sub links in the navigation list
   * @param label string that describe the item
   * @param nbelem give the number of element contained by the item For exemple, if the item is a
   * directory, "nbelem" is the number of files you can find in this directory
   */
  public void addItemSubItem(String label, String URL, int nbelem,
      Collection<Link> links);

  /**
   * Add an item with label and information in the navigation list
   * @param label string that describe the item
   * @param info It can be everything ... (only string)
   */
  public void addItem(String label, String URL, String info);

  /**
   * Add an item with label and information in the navigation list
   * @param label string that describe the item
   */
  public void addItem(String label, String URL);

  /**
   * Set the title of the NavigationList
   * @param title String that wil appear on the top of the NavigationList
   */
  public void setTitle(String title);

  /**
   * You can set the number of columns you want for your list Default is 3
   * @param col int Specify the number of column
   */
  public void setNbcol(int col);

  /**
   * Print the NavigationList in an html format
   * @return The NavigationList representation
   */
  public String print();

}
