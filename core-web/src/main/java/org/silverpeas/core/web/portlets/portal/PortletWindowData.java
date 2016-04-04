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

import javax.servlet.http.HttpServletRequest;

import com.sun.portal.portletcontainer.context.registry.PortletRegistryContext;
import com.sun.portal.portletcontainer.context.registry.PortletRegistryException;

/**
 * The PortletWindowData is responsible for providing the data related to the Portlet Window to the
 * view. The information includes title, portlet content, view ,edit and help URLs.
 */
public interface PortletWindowData {

  public void init(HttpServletRequest request,
      PortletRegistryContext portletRegistryContext, String portletWindowName)
      throws PortletRegistryException;

  public String getPortletName();

  public String getPortletWindowName();

  public String getTitle();

  public CharSequence getContent();

  public boolean isView();

  public String getViewURL();

  public boolean isEdit();

  public String getEditURL();

  public boolean isHelp();

  public String getHelpURL();

  public boolean isNormalized();

  public String getNormalizedURL();

  public boolean isMaximized();

  public String getMaximizedURL();

  public boolean isMinimized();

  public String getMinimizedURL();

  public String getCurrentMode();

  public String getCurrentWindowState();

  public boolean isRemove();

  public String getRemoveURL();

  public boolean isThin();

  public boolean isThick();

  public String getWidth();

  public Integer getRowNumber();

  public String getSpaceId();

  public void setSpaceId(String spaceId);
}
