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
package com.silverpeas.portlets.portal;

import java.net.URL;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.silverpeas.portlets.portal.portletwindow.PortletWindowInvoker;
import com.sun.portal.container.ChannelMode;
import com.sun.portal.container.ChannelState;
import com.sun.portal.portletcontainer.invoker.InvokerException;
import com.sun.portal.portletcontainer.invoker.ResponseProperties;
import com.sun.portal.portletcontainer.invoker.WindowInvoker;

/**
 * PortletContent is responsible for getting the portlet content and execting action on the portlet.
 * It delegates the calls to PortletWindowInvoker.
 */
public class PortletContent {

  private HttpServletRequest request;
  private HttpServletResponse response;
  private WindowInvoker windowInvoker;

  public PortletContent(ServletContext context, HttpServletRequest request,
      HttpServletResponse response) throws InvokerException {
    this.request = request;
    this.response = response;
    this.windowInvoker = getWindowInvoker(context, request, response);
  }

  public void setPortletWindowMode(ChannelMode portletWindowMode) {
    windowInvoker.setPortletWindowMode(portletWindowMode);
  }

  public void setPortletWindowName(String portletWindowName) {
    windowInvoker.setPortletWindowName(portletWindowName);
  }

  public void setPortletWindowState(ChannelState portletWindowState) {
    windowInvoker.setPortletWindowState(portletWindowState);
  }

  public StringBuffer getContent() throws InvokerException {
    return windowInvoker.render(request, response);
  }

  public String getTitle() throws InvokerException {
    return windowInvoker.getTitle();
  }

  public ResponseProperties getResponseProperties() {
    return windowInvoker.getResponseProperties();
  }

  public String getDefaultTitle() throws InvokerException {
    return windowInvoker.getDefaultTitle();
  }

  public URL executeAction() throws InvokerException {
    return windowInvoker.processAction(request, response);
  }

  public void getResources() throws InvokerException {
    windowInvoker.getResources(request, response);
  }

  protected WindowInvoker getWindowInvoker(ServletContext context,
      HttpServletRequest request, HttpServletResponse response)
      throws InvokerException {
    WindowInvoker pwInvoker = new PortletWindowInvoker();
    pwInvoker.init(context, request, response);
    return pwInvoker;
  }
}
