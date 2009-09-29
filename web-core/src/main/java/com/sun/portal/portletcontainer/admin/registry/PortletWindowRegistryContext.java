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

package com.sun.portal.portletcontainer.admin.registry;

import java.util.List;

import com.sun.portal.container.EntityID;
import com.sun.portal.container.PortletLang;
import com.sun.portal.container.PortletType;
import com.sun.portal.portletcontainer.context.registry.PortletRegistryException;

/**
 * PortletWindowRegistryContext provides information pertaining to the 
 * portlet window registry.
 * This includes information about portlet windows.
 */
public interface PortletWindowRegistryContext {
    
    public EntityID getEntityId(String portletWindowName) throws PortletRegistryException;
    public List<EntityID> getEntityIds() throws PortletRegistryException;
    public String getPortletWindowTitle(String portletWindowName) throws PortletRegistryException;
    public void setPortletWindowTitle(String portletWindowName, String title) throws PortletRegistryException ;
    public void createPortletWindow(String portletName, String portletWindowName) throws PortletRegistryException;
    public void createPortletWindow(String portletName, String portletWindowName, String title, String locale) throws PortletRegistryException;
    public void removePortletWindow(String portletWindowName) throws PortletRegistryException;
    public void removePortletWindows(String portletName) throws PortletRegistryException;
    public void movePortletWindows(List portletWindows) throws PortletRegistryException;
    public void showPortletWindow(String portletWindowName, boolean visible) throws PortletRegistryException;
    public boolean isVisible(String portletWindowName) throws PortletRegistryException;
    public String getPortletName(String portletWindowName) throws PortletRegistryException;
    public List getPortletWindows(String portletName) throws PortletRegistryException;
    public Integer getRowNumber(String portletWindowName) throws PortletRegistryException;
    public String getWidth(String portletWindowName) throws PortletRegistryException;    
    public void setWidth(String portletWindowName, String width, String row) throws PortletRegistryException;    
    public String getProducerEntityID(String portletWindowName) throws PortletRegistryException;
    public String getConsumerID(String portletWindowName) throws PortletRegistryException;
    public String getPortletID(String portletWindowName) throws PortletRegistryException;
    public boolean isRemote(String portletWindowName) throws PortletRegistryException;    
    public List getVisiblePortletWindows(PortletType portletType) throws PortletRegistryException;
    public List getAllPortletWindows(PortletType portletType) throws PortletRegistryException;
    public List<String> getRemotePortletWindows() throws PortletRegistryException;
    public PortletLang getPortletLang(String portletWindowName) throws PortletRegistryException;
}
