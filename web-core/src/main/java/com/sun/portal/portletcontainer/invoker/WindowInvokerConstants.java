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
package com.sun.portal.portletcontainer.invoker;

/**
 * WindowInvokerConstants has the contants used by the WindowInvoker and the
 * Driver
 */

public interface WindowInvokerConstants {

  public static final String ACTION = "ACTION"; // Possible value for
  // DRIVER_ACTION

  public static final String RENDER = "RENDER"; // Possible value for
  // DRIVER_ACTION

  public static final String RESOURCE = "RESOURCE"; // Possible value for
  // DRIVER_ACTION

  public static final String KEYWORD_PREFIX = "pc.";

  public static final String DRIVER_PARAM_PREFIX = "dt.";

  public static final String PORTLET_WINDOW_MODE_KEY = KEYWORD_PREFIX
      + "portletMode";

  public static final String PORTLET_WINDOW_STATE_KEY = KEYWORD_PREFIX
      + "windowState";

  public static final String PORTLET_REMOVE_KEY = KEYWORD_PREFIX + "remove";

  public static final String PORTLET_WINDOW_KEY = KEYWORD_PREFIX + "portletId";

  public final static String NEW_PORTLET_WINDOW_MODE_KEY = KEYWORD_PREFIX
      + "newPortletMode";

  public final static String NEW_PORTLET_WINDOW_STATE_KEY = KEYWORD_PREFIX
      + "newWindowState";

  public final static String PORTLET_ACTION = KEYWORD_PREFIX + "portletAction";

  public final static String DRIVER_ACTION = DRIVER_PARAM_PREFIX
      + "driverAction";

  public final static String RESOURCE_ID_KEY = KEYWORD_PREFIX + "resourceID";

  public final static String RESOURCE_URL_CACHE_LEVEL_KEY = KEYWORD_PREFIX
      + "cacheLevel";

  public final static String DRIVER_SPACEID = DRIVER_PARAM_PREFIX + "SpaceId";
  public final static String DRIVER_ROLE = DRIVER_PARAM_PREFIX + "Role";

  // --------------------------------------------------------
  // Static String used for storing render parameters
  // -------------------------------------------------------

  public static final String PORTLET_PARAM_MAP = "PortletParameterMap";

}
