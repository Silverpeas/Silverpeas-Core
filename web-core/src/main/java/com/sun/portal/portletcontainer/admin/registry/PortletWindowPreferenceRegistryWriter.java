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
 * PortletWindowPreferenceRegistryWriter is responsible for updating the
 * portlet-window-preferences.xml file
 */
public class PortletWindowPreferenceRegistryWriter extends
    PortletRegistryWriter {

  public PortletWindowPreferenceRegistryWriter(String registryLocation,
      String context) {
    super(registryLocation,
        PortletRegistryFile.PORTLET_WINDOW_PREFERENCE_REGISTRY_FILE, context);
  }

  public void appendDocument(List portletWindowPreferenceElementList)
      throws PortletRegistryException {
    PortletRegistryReader portletWindowPreferenceRegistryReader = new PortletWindowPreferenceRegistryReader(
        registryLocation, context);
    PortletRegistryObject portletWindowPreferenceRegistry = portletWindowPreferenceRegistryReader
        .readDocument();
    write(portletWindowPreferenceElementList, portletWindowPreferenceRegistry);
  }

  public void writeDocument(List portletWindowPreferenceElementList)
      throws PortletRegistryException {
    // TODO: Not in use. Should be removed?
    // PortletRegistryReader portletWindowPreferenceRegistryReader = new
    // PortletWindowPreferenceRegistryReader(registryLocation);
    PortletRegistryObject portletWindowPreferenceRegistry = new PortletWindowPreferenceRegistry();
    write(portletWindowPreferenceElementList, portletWindowPreferenceRegistry);
  }

  private void write(List portletWindowPreferenceElementList,
      PortletRegistryObject portletWindowPreferenceRegistry)
      throws PortletRegistryException {
    int size = portletWindowPreferenceElementList.size();
    PortletRegistryElement portletWindowPreference;
    for (int i = 0; i < size; i++) {
      portletWindowPreference = (PortletRegistryElement) portletWindowPreferenceElementList
          .get(i);
      portletWindowPreferenceRegistry
          .addRegistryElement(portletWindowPreference);
    }
    write(portletWindowPreferenceRegistry);
    PortletRegistryCache.refreshPortletWindowPreferenceRegistryCache(true);
  }
}
