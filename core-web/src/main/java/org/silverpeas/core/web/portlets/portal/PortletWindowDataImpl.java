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

package org.silverpeas.core.web.portlets.portal;

import org.silverpeas.core.util.StringUtil;
import com.sun.portal.container.ChannelMode;
import com.sun.portal.container.ChannelState;
import com.sun.portal.portletcontainer.admin.registry.PortletRegistryConstants;
import com.sun.portal.portletcontainer.context.registry.PortletRegistryContext;
import com.sun.portal.portletcontainer.context.registry.PortletRegistryException;
import com.sun.portal.portletcontainer.invoker.WindowInvokerConstants;

import javax.servlet.http.HttpServletRequest;
import java.io.Serializable;

/**
 * PortletWindowDataImpl provides concrete implementation of PortletWindowData interface
 */
public class PortletWindowDataImpl implements PortletWindowData, Comparable, Serializable {

  public static final long serialVersionUID = 1L;
  private String requestURL;
  private String portletWindowName;
  private String portletName;
  private String title;
  private CharSequence content;
  private boolean view;
  private boolean edit;
  private boolean help;
  private boolean remove;
  private Integer rowNumber;
  private String width;
  private String currentMode;
  private String currentWindowState;
  private String spaceId;
  private String role;

  public PortletWindowDataImpl() {
  }

  @Override
  public void init(HttpServletRequest request,
      PortletRegistryContext portletRegistryContext, String portletWindowName)
      throws PortletRegistryException {
    String thePortletName = portletRegistryContext
        .getPortletName(portletWindowName);
    setPortletName(thePortletName);
    setPortletWindowName(portletWindowName);
    setRequestURL(request.getRequestURL());
    setView(portletRegistryContext.hasView(thePortletName));
    setEdit(portletRegistryContext.hasEdit(thePortletName));
    setHelp(portletRegistryContext.hasHelp(thePortletName));
    setRemove(true);
    setRowNumber(portletRegistryContext.getRowNumber(portletWindowName));
    setWidth(portletRegistryContext.getWidth(portletWindowName));
  }

  public void setPortletName(String portletName) {
    this.portletName = portletName;
  }

  @Override
  public String getPortletName() {
    return portletName;
  }

  public void setPortletWindowName(String portletWindowName) {
    this.portletWindowName = portletWindowName;
  }

  public void setRequestURL(StringBuffer requestURL) {
    this.requestURL = requestURL.toString();
  }

  public String getRequestURL() {
    return requestURL;
  }

  @Override
  public String getPortletWindowName() {
    return this.portletWindowName;
  }

  @Override
  public String getTitle() {
    return this.title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  @Override
  public CharSequence getContent() {
    return this.content;
  }

  public void setContent(CharSequence content) {
    this.content = content;
  }

  @Override
  public boolean isView() {
    return this.view;
  }

  public void setView(boolean view) {
    this.view = view;
  }

  @Override
  public String getViewURL() {
    return getPortletModeURL(ChannelMode.VIEW.toString());
  }

  @Override
  public boolean isEdit() {
    return this.edit;
  }

  public void setEdit(boolean edit) {
    this.edit = edit;
  }

  @Override
  public boolean isRemove() {
    return this.remove;
  }

  public void setRemove(boolean remove) {
    this.remove = remove;
  }

  @Override
  public String getEditURL() {
    return getPortletModeURL(ChannelMode.EDIT.toString());
  }

  @Override
  public boolean isHelp() {
    return this.help;
  }

  public void setHelp(boolean help) {
    this.help = help;
  }

  @Override
  public String getHelpURL() {
    return getPortletModeURL(ChannelMode.HELP.toString());
  }

  @Override
  public boolean isNormalized() {
    String aCurrentWindowState = getCurrentWindowState();
    return ChannelState.NORMAL.toString().equals(aCurrentWindowState);
  }

  @Override
  public String getNormalizedURL() {
    return getPortletWindowStateURL(ChannelState.NORMAL.toString());
  }

  @Override
  public boolean isMaximized() {
    String aCurrentWindowState = getCurrentWindowState();
    return ChannelState.MAXIMIZED.toString().equals(aCurrentWindowState);
  }

  @Override
  public String getMaximizedURL() {
    return getPortletWindowStateURL(ChannelState.MAXIMIZED.toString());
  }

  @Override
  public boolean isMinimized() {
    String aCurrentWindowState = getCurrentWindowState();
    return ChannelState.MINIMIZED.toString().equals(aCurrentWindowState);
  }

  @Override
  public String getMinimizedURL() {
    return getPortletWindowStateURL(ChannelState.MINIMIZED.toString());
  }

  @Override
  public String getCurrentMode() {
    return this.currentMode;
  }

  public void setCurrentMode(ChannelMode currentMode) {
    if (currentMode == null) {
      this.currentMode = ChannelMode.VIEW.toString();
    } else {
      this.currentMode = currentMode.toString();
    }
  }

  @Override
  public String getCurrentWindowState() {
    return this.currentWindowState;
  }

  public void setCurrentWindowState(ChannelState currentWindowState) {
    if (currentWindowState == null) {
      this.currentWindowState = ChannelState.NORMAL.toString();
    } else {
      this.currentWindowState = currentWindowState.toString();
    }
  }

  @Override
  public String getRemoveURL() {
    StringBuilder processURL = new StringBuilder(getRequestURL());

    processURL.append("?").append(WindowInvokerConstants.DRIVER_ACTION).append(
        "=").append(WindowInvokerConstants.RENDER).append("&").append(
        WindowInvokerConstants.PORTLET_WINDOW_MODE_KEY).append("=").append(
        getCurrentMode()).append("&").append(
        WindowInvokerConstants.PORTLET_REMOVE_KEY).append("=").append("true")
        .append("&").append(WindowInvokerConstants.PORTLET_WINDOW_KEY).append(
        "=").append(getPortletWindowName());
    if (StringUtil.isDefined(getSpaceId())) {
      processURL.append("&").append(WindowInvokerConstants.DRIVER_SPACEID)
          .append("=").append(getSpaceId());
      if (StringUtil.isDefined(role))
        processURL.append("&").append(WindowInvokerConstants.DRIVER_ROLE)
            .append("=").append("admin");
    }
    return processURL.toString();
  }

  public void setRowNumber(Integer rowNumber) {
    this.rowNumber = rowNumber;
  }

  @Override
  public Integer getRowNumber() {
    return this.rowNumber;
  }

  public void setWidth(String width) {
    this.width = width;
  }

  @Override
  public String getWidth() {
    return width;
  }

  @Override
  public boolean isThin() {
    return width != null && PortletRegistryConstants.WIDTH_THIN.equals(width);
  }

  @Override
  public boolean isThick() {
    return width != null && PortletRegistryConstants.WIDTH_THICK.equals(width);
  }

  @Override
  public String getSpaceId() {
    return spaceId;
  }

  @Override
  public void setSpaceId(String context) {
    this.spaceId = context;
  }

  public String getRole() {
    return role;
  }

  public void setRole(String role) {
    this.role = role;
  }

  private String getPortletModeURL(String portletMode) {
    StringBuilder processURL = new StringBuilder(getRequestURL());

    processURL.append("?").append(WindowInvokerConstants.DRIVER_ACTION).append(
        "=").append(WindowInvokerConstants.RENDER).append("&").append(
        WindowInvokerConstants.PORTLET_WINDOW_MODE_KEY).append("=").append(
        portletMode).append("&").append(
        WindowInvokerConstants.PORTLET_WINDOW_STATE_KEY).append("=").append(
        getCurrentWindowState()).append("&").append(
        WindowInvokerConstants.PORTLET_WINDOW_KEY).append("=").append(
        getPortletWindowName());

    if (StringUtil.isDefined(getSpaceId())) {
      processURL.append("&").append(WindowInvokerConstants.DRIVER_SPACEID)
          .append("=").append(getSpaceId());
      if (StringUtil.isDefined(role))
        processURL.append("&").append(WindowInvokerConstants.DRIVER_ROLE)
            .append("=").append("admin");
    }

    return processURL.toString();
  }

  private String getPortletWindowStateURL(String portletWindowState) {
    StringBuilder processURL = new StringBuilder(getRequestURL());

    processURL.append("?").append(WindowInvokerConstants.DRIVER_ACTION).append(
        "=").append(WindowInvokerConstants.RENDER).append("&").append(
        WindowInvokerConstants.PORTLET_WINDOW_MODE_KEY).append("=").append(
        getCurrentMode()).append("&").append(
        WindowInvokerConstants.PORTLET_WINDOW_STATE_KEY).append("=").append(
        portletWindowState).append("&").append(
        WindowInvokerConstants.PORTLET_WINDOW_KEY).append("=").append(
        getPortletWindowName());

    if (StringUtil.isDefined(getSpaceId())) {
      processURL.append("&").append(WindowInvokerConstants.DRIVER_SPACEID)
          .append("=").append(getSpaceId());
      if (StringUtil.isDefined(role))
        processURL.append("&").append(WindowInvokerConstants.DRIVER_ROLE)
            .append("=").append("admin");
    }

    return processURL.toString();
  }

  @Override
  public int compareTo(Object o) {
    Integer otherRowNumber = ((PortletWindowDataImpl) o).getRowNumber();
    int value = getRowNumber().compareTo(otherRowNumber);
    return value;
  }

  @Override
  public boolean equals(Object o) {
    return super.equals(o);
  }

  @Override
  public int hashCode() {
    assert false : "hashCode not designed";
    return 42; // any arbitrary constant will do
  }
}
