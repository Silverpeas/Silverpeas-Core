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
 * Link.java
 *
 * Created on 17 avril 2001, 17:44
 */

package org.silverpeas.core.web.util.viewgenerator.html.navigationlist;

/**
 * @author lloiseau
 * @version 1.0
 */
public class Link {

  private String label;
  private String URL;

  /**
   * Creates new Link
   */
  public Link(String label, String URL) {
    this.URL = URL;
    this.label = label;
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

}
