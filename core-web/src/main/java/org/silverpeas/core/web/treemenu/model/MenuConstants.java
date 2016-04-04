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

package org.silverpeas.core.web.treemenu.model;

public final class MenuConstants {

  /**
   *
   */
  private MenuConstants() {
  }

  // key used to retrieve a menu filter implementation
  /**
   * key used for default menu implementation (display all type of menu element)
   */
  public static final String DEFAULT_MENU_TYPE = "default";

  /**
   * key used for seealso implementation (display space, only kmelia component, theme)
   */
  public static final String SEE_ALSO_MENU_TYPE = "seealso";

  /**
   * key used for theme implementation (displays theme only)
   */
  public static final String THEME_MENU_TYPE = "theme";

  // key request
  /**
   * key used to store in the HTTP request the menu type to build
   */
  public static final String REQUEST_KEY_MENU_TYPE = "mtype";

  /**
   * key used to store in the HTTP request the node type of father elements to build
   */
  public static final String REQUEST_KEY_NODE_TYPE = "ntype";

  /**
   * key used to store in the HTTP request the level of the father of elements to build
   */
  public static final String REQUEST_KEY_MENU_LEVEL = "level";
  /**
   * key used to store in the HTTP request the compoId of the father of elements to build
   */
  public static final String REQUEST_KEY_COMPONENT_ID = "compoId";
  /**
   * key used to store in the HTTP request if he father elements to build is a leaf or not
   */
  public static final String REQUEST_KEY_LEAF = "leaf";
  /**
   * key used to store in the HTTP request the identifier of father elements to build
   */
  public static final String REQUEST_KEY_ITEM_MENU_ID = "key";

  // others
  /**
   * prefix used to create the style used to display component icon
   */
  public static final String ICON_STYLE_PREFIX = "icon-";

}
