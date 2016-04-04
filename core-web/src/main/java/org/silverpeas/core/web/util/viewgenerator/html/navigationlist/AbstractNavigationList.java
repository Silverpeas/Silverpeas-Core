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
 * AbstractNavigationList.java
 *
 * Created on 28 mars 2001, 10:09
 */

package org.silverpeas.core.web.util.viewgenerator.html.navigationlist;

import java.util.ArrayList;
import java.util.Collection;

import org.silverpeas.core.web.util.viewgenerator.html.GraphicElementFactory;

/**
 * Abstract class of the NavigationList
 * @author lloiseau
 * @version 1.0
 */
public abstract class AbstractNavigationList implements NavigationList {
  private Collection<Item> items = null;
  private String title = null;
  private int nbCol = 3;

  /**
   * Creates new AbstractNavigationList
   */
  public AbstractNavigationList() {
    items = new ArrayList<Item>();
  }

  public void addItem(String label, String URL, int nbelem, String info,
      String universalLink) {
    Item item = new Item(label, URL, nbelem, info);
    item.setUniversalLink(universalLink);
    items.add(item);
  }

  /**
   * Add an item with label, number of elements and information in the navigation list
   * @param label string that describe the item
   * @param nbelem give the number of element contained by the item For exemple, if the item is a
   * directory, "nbelem" is the number of files you can find in this directory
   * @param info It can be everything ... (only string)
   */
  public void addItem(String label, String URL, int nbelem, String info) {
    items.add(new Item(label, URL, nbelem, info));
  }

  /**
   * Add an item with label and information in the navigation list
   * @param label string that describe the item
   * @param info It can be everything ... (only string)
   */
  public void addItem(String label, String URL, String info) {
    items.add(new Item(label, URL, info));
  }

  /**
   * Add an item with label and information in the navigation list
   * @param label string that describe the item
   */
  public void addItem(String label, String URL) {
    items.add(new Item(label, URL));
  }

  /**
   * Get the items collection
   * @return The items collection
   */
  public Collection<Item> getItems() {
    return this.items;
  }

  /**
   * Set the title of the NavigationList
   * @param title String that wil appear on the top of the NavigationList
   */
  public void setTitle(String title) {
    this.title = title;
  }

  /**
   * Get the list's title
   * @return The title
   */
  public String getTitle() {
    return title;
  }

  /**
   * You can set the number of columns you want for your list Default is 3
   * @param col int Specify the number of column
   */
  public void setNbcol(int col) {
    this.nbCol = col;
  }

  /**
   * Get the number of column of the list
   * @return The number of columns
   */
  public int getNbcol() {
    return nbCol;
  }

  /**
   * Print the NavigationList in an html format
   * @return The NavigationList representation
   */
  public abstract String print();

  /**
   * Give the path for the pics...
   * @return String Return the path
   */
  public String getIconsPath() {
    return GraphicElementFactory.getIconsPath();
  }

  /**
   * Add an item with label, number of elements and sub links in the navigation list
   * @param label string that describe the item
   * @param nbelem give the number of element contained by the item For exemple, if the item is a
   * directory, "nbelem" is the number of files you can find in this directory
   */
  public void addItemSubItem(String label, String URL, int nbelem,
      Collection<Link> links) {
    items.add(new Item(label, URL, nbelem, links));
  }

}