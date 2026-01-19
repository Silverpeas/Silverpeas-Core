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
 * "https://www.silverpeas.org/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.web.portlets.portal;

import jakarta.servlet.http.HttpServletRequest;

import com.sun.portal.portletcontainer.context.registry.PortletRegistryContext;
import com.sun.portal.portletcontainer.context.registry.PortletRegistryException;

/**
 * The PortletWindowData is responsible for providing the data related to the Portlet Window to the
 * view. The information includes title, portlet content, view ,edit and help URLs.
 */
public interface PortletWindowData {

  void init(HttpServletRequest request,
      PortletRegistryContext portletRegistryContext, String portletWindowName)
      throws PortletRegistryException;

  String getPortletName();

  String getPortletWindowName();

  String getTitle();

  CharSequence getContent();

  boolean isView();

  String getViewURL();

  boolean isEdit();

  String getEditURL();

  boolean isHelp();

  String getHelpURL();

  boolean isNormalized();

  String getNormalizedURL();

  boolean isMaximized();

  String getMaximizedURL();

  boolean isMinimized();

  String getMinimizedURL();

  String getCurrentMode();

  String getCurrentWindowState();

  boolean isRemove();

  String getRemoveURL();

  boolean isThin();

  boolean isThick();

  String getWidth();

  Integer getRowNumber();

  String getSpaceId();

  void setSpaceId(String spaceId);
}
