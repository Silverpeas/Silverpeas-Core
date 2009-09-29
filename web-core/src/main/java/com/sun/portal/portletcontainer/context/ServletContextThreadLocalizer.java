/**
 * Copyright (C) 2000 - 2009 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have recieved a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://repository.silverpeas.com/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.sun.portal.portletcontainer.context;

import javax.servlet.ServletContext;

/**
 * This class provides access to the ServletContext using ThreadLocal class It
 * is required by WSRP caching implementaion.
 */
public class ServletContextThreadLocalizer {

  private static ThreadLocal servletContextThreadLocal = new ThreadLocal();

  private ServletContextThreadLocalizer() {
    // nothing, cannot be called
  }

  /**
   * Returns ServletContext
   * 
   * @return
   */
  public static ServletContext get() {
    ServletContext sc = (ServletContext) servletContextThreadLocal.get();
    if (sc == null) {
      throw new Error(
          "ServletContextThreadLocalizer.get(): no thread local set for this thread");
    }

    return sc;
  }

  /**
   * Sets ServletContext in the ThreadLocal variable
   * 
   * @param sc
   *          ServletContextContext
   */
  public static void set(ServletContext sc) {
    servletContextThreadLocal.set(sc);
  }

  /**
   * Checks whether ServletContext is set in the ThreadLocal variable
   * 
   * @param boolean
   */
  public static synchronized boolean exists() {
    ServletContext sc = (ServletContext) servletContextThreadLocal.get();
    if (sc != null) {
      return true;
    } else {
      return false;
    }
  }
}
