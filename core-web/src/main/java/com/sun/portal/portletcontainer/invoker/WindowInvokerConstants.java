/*
 * CDDL HEADER START
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at
 * http://www.sun.com/cddl/cddl.html and legal/CDDLv1.0.txt
 * See the License for the specific language governing
 * permission and limitations under the License.
 *
 * When distributing Covered Code, include this CDDL
 * Header Notice in each file and include the License file
 * at legal/CDDLv1.0.txt.
 * If applicable, add the following below the CDDL Header,
 * with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Copyright 2006 Sun Microsystems Inc. All Rights Reserved
 * CDDL HEADER END
 */
package com.sun.portal.portletcontainer.invoker;

/**
 * WindowInvokerConstants has the contants used by the WindowInvoker and the Driver
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
