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

package org.silverpeas.core.web.portlets.portal.portletwindow;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import com.sun.portal.container.ChannelMode;
import com.sun.portal.container.ChannelState;
import com.sun.portal.container.ChannelURLType;
import com.sun.portal.container.WindowRequestReader;
import com.sun.portal.portletcontainer.invoker.WindowInvokerConstants;

public class PortletWindowRequestReader implements WindowRequestReader {

  @Override
  public ChannelMode readNewPortletWindowMode(HttpServletRequest request) {
    String newChannelMode =
        request.getParameter(WindowInvokerConstants.NEW_PORTLET_WINDOW_MODE_KEY);
    if (newChannelMode != null) {
      return new ChannelMode(newChannelMode);
    }
    return null;
  }

  @Override
  public ChannelState readNewWindowState(HttpServletRequest request) {
    String newWindowState =
        request.getParameter(WindowInvokerConstants.NEW_PORTLET_WINDOW_STATE_KEY);
    if (newWindowState != null) {
      return new ChannelState(newWindowState);
    }
    return null;
  }

  @Override
  public ChannelURLType readURLType(HttpServletRequest request) {
    return new ChannelURLType(request.getParameter(WindowInvokerConstants.PORTLET_ACTION));
  }

  @Override
  public Map<String, String[]> readParameterMap(HttpServletRequest request) {
    Map<String, String[]> params =
        (Map) request.getAttribute(WindowInvokerConstants.PORTLET_PARAM_MAP);
    return params;
  }

  @Override
  public String getCacheLevel(HttpServletRequest request) {
    return request.getParameter(WindowInvokerConstants.RESOURCE_URL_CACHE_LEVEL_KEY);
  }

  @Override
  public String getResourceID(HttpServletRequest request) {
    return request.getParameter(WindowInvokerConstants.RESOURCE_ID_KEY);
  }
}
