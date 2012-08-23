/*
 * CDDL HEADER START
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at
 * http://www.sun.com/cddl/cddl.html and legal/CDDLv1.0.txt
 * See the License for the specific language governing
 * permission and limitations under the License.
 *
 * When distributing Covered Code, include this CDDL
 * Header Notice in each file and include the License file
 * at legal/CDDLv1.0.txt.
 * If applicable, add the following below the CDDL Header,
 * with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Copyright 2006 Sun Microsystems Inc. All Rights Reserved
 * CDDL HEADER END
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
 * PortletAppRegistryWriter is responsible for updating the portlet-app-registry.xml file
 */
public class PortletAppRegistryWriter extends PortletRegistryWriter {

  public PortletAppRegistryWriter(String registryLocation) {
    super(registryLocation, PortletRegistryFile.PORTLET_APP_REGISTRY_FILE, null);
  }

  public void appendDocument(List portletAppElementList)
      throws PortletRegistryException {
    PortletRegistryReader portletAppRegistryReader = new PortletAppRegistryReader(
        registryLocation);
    PortletRegistryObject portletAppRegistry = portletAppRegistryReader
        .readDocument();
    write(portletAppElementList, portletAppRegistry);
  }

  public void writeDocument(List portletAppElementList)
      throws PortletRegistryException {
    // TODO: Not in use. Should be removed?
    // PortletRegistryReader portletAppRegistryReader = new
    // PortletAppRegistryReader(registryLocation);
    PortletRegistryObject portletAppRegistry = new PortletAppRegistry();
    write(portletAppElementList, portletAppRegistry);
  }

  private void write(List portletAppElementList,
      PortletRegistryObject portletAppRegistry) throws PortletRegistryException {
    int size = portletAppElementList.size();
    PortletRegistryElement portletApp;
    for (int i = 0; i < size; i++) {
      portletApp = (PortletRegistryElement) portletAppElementList.get(i);
      portletAppRegistry.addRegistryElement(portletApp);
    }
    write(portletAppRegistry);
    PortletRegistryCache.refreshPortletAppRegistryCache(true);
  }
}
