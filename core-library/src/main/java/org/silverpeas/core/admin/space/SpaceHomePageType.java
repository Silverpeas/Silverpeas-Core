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
 * "https://www.silverpeas.com/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 *
 */

package org.silverpeas.core.admin.space;

/**
 * Defines the different types of home page a collaborative space can have in Silverpeas.
 * @author mmoquillon
 */
public enum SpaceHomePageType {

  /**
   * Standard home page. The default one.
   */
  STANDARD,
  /**
   * The home page is the main page of a component instance, meaning a redirection to the
   * application instance's main page.
   */
  COMPONENT_INST,
  /**
   * The home page is container of portlets.
   */
  PORTLET,
  /**
   * The home page is an HTML page.
   */
  HTML_PAGE
}