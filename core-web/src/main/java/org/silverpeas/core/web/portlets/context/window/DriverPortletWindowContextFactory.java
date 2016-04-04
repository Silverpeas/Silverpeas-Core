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

package org.silverpeas.core.web.portlets.context.window;

import javax.servlet.http.HttpServletRequest;

import org.silverpeas.core.web.portlets.context.window.impl.PortletWindowContextImpl;
import com.sun.portal.container.PortletWindowContext;
import com.sun.portal.container.PortletWindowContextException;
import com.sun.portal.container.PortletWindowContextFactory;

/**
 * DriverPortletWindowContextFactory provides the implementation of the abstract methods of
 * PortletWindowContextFactory.
 */
public class DriverPortletWindowContextFactory implements PortletWindowContextFactory {

  private PortletWindowContext portletWindowContext;

  public DriverPortletWindowContextFactory() {
  }

  @Override
  public PortletWindowContext getPortletWindowContext(HttpServletRequest request)
      throws PortletWindowContextException {
    if (portletWindowContext == null) {
      portletWindowContext = new PortletWindowContextImpl();
      portletWindowContext.init(request);
    }
    return portletWindowContext;
  }

  @Override
  public PortletWindowContext getPortletWindowContext(
      HttpServletRequest request, String userID)
      throws PortletWindowContextException {
    if (portletWindowContext == null) {
      portletWindowContext = new PortletWindowContextImpl(userID);
      portletWindowContext.init(request);
    }
    return portletWindowContext;
  }

}
