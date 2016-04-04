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
 * Item.java
 *
 * Created on 28 mars 2001, 08:28
 */

package org.silverpeas.core.web.util.viewgenerator.html.navigationlist;

import java.util.Collection;

/**
 * Item of a list. This item must contain a label and an URL ; and optionnally it can have a number
 * of elements and a string (informations).
 * @author lloiseau
 * @version 1.0
 */
public class Item {

  private String label;
  private String URL;
  private int nbelem;
  private String info;
  private Collection<Link> links;
  private String universalLink;

  /**
   * Creates new Item Contain a label, number of elements and information
   * @param label
   * @param URL
   * @param nbelem
   * @param info
   */
  public Item(String label, String URL, int nbelem, Collection<Link> links) {
    this.URL = URL;
    this.label = label;
    this.nbelem = nbelem;
    this.links = links;
  }

  /**
   * Creates new Item Contain a label, number of elements and information
   * @param label
   * @param URL
   * @param nbelem
   * @param info
   */
  public Item(String label, String URL, int nbelem, String info) {
    this.URL = URL;
    this.label = label;
    this.nbelem = nbelem;
    this.info = info;
  }

  /**
   * Creates new Item Contain a label and information but no elements
   */
  public Item(String label, String URL, String info) {
    this.label = label;
    this.URL = URL;
    this.nbelem = -1;
    this.info = info;
  }

  /**
   * Creates new Item Contain a lonely label :o(
   */
  public Item(String label, String URL) {
    this.label = label;
    this.URL = URL;
    this.nbelem = -1;
    this.info = null;
  }

  // Return the label

  /**
   * Method declaration
   * @return
   * @see
   */
  public String getLabel() {
    return label;
  }

  // To set a new label

  /**
   * Method declaration
   * @param label
   * @see
   */
  public void setLabel(String label) {
    this.label = label;
  }

  // Return the URL

  /**
   * Method declaration
   * @return
   * @see
   */
  public String getURL() {
    return URL;
  }

  // To set a new URL

  /**
   * Method declaration
   * @param URL
   * @see
   */
  public void setURL(String URL) {
    this.URL = URL;
  }

  // Return the number of elements

  /**
   * Method declaration
   * @return
   * @see
   */
  public int getNbelem() {
    return nbelem;
  }

  // To set the number of elements

  /**
   * Method declaration
   * @param nbelem
   * @see
   */
  public void setNbelem(int nbelem) {
    this.nbelem = nbelem;
  }

  // Return informations

  /**
   * Method declaration
   * @return
   * @see
   */
  public String getInfo() {
    return info;
  }

  // To set the informations

  /**
   * Method declaration
   * @param info
   * @see
   */
  public void setInfo(String info) {
    this.info = info;
  }

  // Return collection of links

  /**
   * Method declaration
   * @return
   * @see
   */
  public Collection<Link> getLinks() {
    return links;
  }

  // To set the commection of links

  /**
   * Method declaration
   * @param links
   * @see
   */
  public void setLinks(Collection<Link> links) {
    this.links = links;
  }

  /**
   * @return
   */
  public String getUniversalLink() {
    return universalLink;
  }

  /**
   * @param string
   */
  public void setUniversalLink(String string) {
    universalLink = string;
  }

}
