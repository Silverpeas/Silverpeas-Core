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
package com.sun.portal.portletcontainer.driver.admin;

import java.util.List;

import com.silverpeas.portlets.portal.PortletAppData;
import com.sun.portal.portletcontainer.context.registry.PortletRegistryContext;
import com.sun.portal.portletcontainer.context.registry.PortletRegistryException;

/**
 * The PortletAdminData is responsible for administering portlets.
 * The includes deploying/undeploying portlets, creating/modifying portlet windows.
 */
public interface PortletAdminData {
    public void init(PortletRegistryContext portletRegistryContext) throws PortletRegistryException;
    public boolean deploy(String warName, boolean deployToContainer) throws Exception;
    public boolean deploy(String warName, String rolesFilename, String userInfoFilename, boolean deployToContainer) throws Exception;
    public boolean undeploy(String warName, boolean undeployFromContainer) throws Exception;
    public List<PortletAppData> getPortlets(String locale);
    public List<String> getPortletNames();
    public List<String> getPortletDisplayNames(String locale);
    public List<String> getPortletApplicationNames();
    public List<String> getPortletWindowNames();
    public boolean createPortletWindow(String portletName, String portletWindowName, String title) throws Exception;
    public boolean modifyPortletWindow(String portletWindowName, String width, boolean visible, String row) throws Exception;
    public boolean movePortletWindows(List portletWindows) throws Exception;
    public boolean isVisible(String portletWindowName) throws Exception;
    public String getWidth(String portletWindowName) throws Exception;
}
