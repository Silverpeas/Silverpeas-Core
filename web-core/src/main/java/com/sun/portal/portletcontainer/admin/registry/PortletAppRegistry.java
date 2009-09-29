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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.sun.portal.portletcontainer.admin.PortletRegistryElement;
import com.sun.portal.portletcontainer.admin.PortletRegistryHelper;
import com.sun.portal.portletcontainer.admin.PortletRegistryObject;
import com.sun.portal.portletcontainer.context.registry.PortletRegistryException;

/**
 * PortletAppRegistry represents the PortletAppRegistry Element in
 * portlet-app-registry.xml
 */
public class PortletAppRegistry implements PortletRegistryTags,
    PortletRegistryObject {
  private String version;
  private Map portletAppTable;
  private List portletAppList;

  public PortletAppRegistry() {
    portletAppTable = new LinkedHashMap();
    portletAppList = new ArrayList();
  }

  public void read(Document document) throws PortletRegistryException {
    Element root = PortletRegistryHelper.getRootElement(document);
    if (root != null)
      populate(root);
  }

  public void addRegistryElement(PortletRegistryElement portletApp) {
    portletAppTable.put(portletApp.getName(), portletApp);
    portletAppList.add(portletApp);
  }

  public PortletRegistryElement getRegistryElement(String name) {
    return (PortletRegistryElement) portletAppTable.get(name);
  }

  public List getRegistryElements() {
    return this.portletAppList;
  }

  public boolean removeRegistryElement(PortletRegistryElement portletApp) {
    portletAppTable.remove(portletApp.getName());
    return portletAppList.remove(portletApp);
  }

  private Map getPortletAppTable() {
    return this.portletAppTable;
  }

  private void populate(Element root) {
    // Get the attributes for PortletAppRegistry Tag.
    Map portletAppRegistryAttributes = XMLDocumentHelper
        .createAttributeTable(root);
    setVersion((String) portletAppRegistryAttributes.get(VERSION_KEY));
    // Get a list of PortletApp tags and populate values from it.
    List portletAppTags = XMLDocumentHelper.createElementList(root);
    int numOfPortletAppTags = portletAppTags.size();
    PortletApp portletApp;
    for (int i = 0; i < numOfPortletAppTags; i++) {
      Element portletAppTag = (Element) portletAppTags.get(i);
      portletApp = new PortletApp();
      portletApp.populateValues(portletAppTag);
      addRegistryElement(portletApp);
    }
    // System.out.println(portletAppTable);
  }

  public String getVersion() {
    if (this.version == null)
      return PortletRegistryConstants.VERSION;
    return this.version;
  }

  public void setVersion(String version) {
    this.version = version;
  }

  public void write(Document document) {
    Element rootTag = XMLDocumentHelper.createElement(document,
        PORTLET_APP_REGISTRY_TAG);
    // Add the atribute to the child
    rootTag.setAttribute(VERSION_KEY, getVersion());
    document.appendChild(rootTag);
    Collection collection = getPortletAppTable().values();
    Iterator itr = collection.iterator();
    PortletApp portletApp;
    while (itr.hasNext()) {
      portletApp = (PortletApp) itr.next();
      portletApp.create(document, rootTag);
    }
  }
}
