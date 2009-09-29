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

import com.sun.portal.portletcontainer.admin.PortletRegistryCache;
import com.sun.portal.portletcontainer.admin.PortletRegistryElement;
import com.sun.portal.portletcontainer.admin.PortletRegistryFile;
import com.sun.portal.portletcontainer.admin.PortletRegistryObject;
import com.sun.portal.portletcontainer.admin.PortletRegistryReader;
import com.sun.portal.portletcontainer.admin.PortletRegistryWriter;
import com.sun.portal.portletcontainer.context.registry.PortletRegistryException;

/**
 * PortletWindowRegistryWriter is responsible for updating the
 * portlet-window-registry.xml file
 */
public class PortletWindowRegistryWriter extends PortletRegistryWriter {

  public PortletWindowRegistryWriter(String registryLocation, String context) {
    super(registryLocation, PortletRegistryFile.PORTLET_WINDOW_REGISTRY_FILE,
        context);
  }

  public void appendDocument(List portletWindowElementList)
      throws PortletRegistryException {
    PortletRegistryReader portletWindowRegistryReader = new PortletWindowRegistryReader(
        registryLocation, context);
    PortletRegistryObject portletWindowRegistry = portletWindowRegistryReader
        .readDocument();
    write(portletWindowElementList, portletWindowRegistry);
  }

  public void writeDocument(List portletWindowElementList)
      throws PortletRegistryException {
    // TODO: Not in use. Should be removed?
    // PortletRegistryReader portletWindowRegistryReader = new
    // PortletWindowRegistryReader(registryLocation);
    PortletRegistryObject portletWindowRegistry = new PortletWindowRegistry();
    write(portletWindowElementList, portletWindowRegistry);
  }

  private void write(List portletWindowElementList,
      PortletRegistryObject portletWindowRegistry)
      throws PortletRegistryException {
    int size = portletWindowElementList.size();
    PortletRegistryElement portletWindow;
    for (int i = 0; i < size; i++) {
      portletWindow = (PortletRegistryElement) portletWindowElementList.get(i);
      portletWindowRegistry.addRegistryElement(portletWindow);
    }
    write(portletWindowRegistry);
    PortletRegistryCache.refreshPortletWindowRegistryCache(true);

  }
}
