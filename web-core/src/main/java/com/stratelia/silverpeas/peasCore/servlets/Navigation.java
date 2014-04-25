/*
 * Copyright (C) 2000 - 2014 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception. You should have recieved a copy of the text describing
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
package com.stratelia.silverpeas.peasCore.servlets;

/**
 * Each method from Web Routers exposed must return an instance of this class.
 * For now, it permits to specify the destination of the navigation.
 * @author: Yohann Chastagnier
 */
public class Navigation {
  private final String destination;

  /**
   * Default constructor.
   * @param destination the destination after the end of treatment of the web router.
   */
  public Navigation(final String destination) {
    this.destination = destination;
  }

  /**
   * Gets the destination after the end of treatment of the web router.
   * @return the destination.
   */
  public String getDestination() {
    return destination;
  }
}
