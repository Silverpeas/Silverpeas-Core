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
 * PortletWindowRegistry represents the PortletWindowRegistry Element in
 * portlet-window-registry.xml
 */
public class PortletWindowRegistry implements PortletRegistryTags,
    PortletRegistryObject {

  private String version;
  private Map portletWindowTable;
  private List portletWindowList;

  public PortletWindowRegistry() {
    portletWindowTable = new LinkedHashMap();
    portletWindowList = new ArrayList();
  }

  public Map getPortletWindowRegistryTable() {
    return this.portletWindowTable;
  }

  public void read(Document document) throws PortletRegistryException {
    Element root = PortletRegistryHelper.getRootElement(document);
    if (root != null)
      populate(root);
  }

  public void addRegistryElement(PortletRegistryElement portletWindow) {
    portletWindowTable.put(portletWindow.getName(), portletWindow);
    portletWindowList.add(portletWindow);
  }

  public PortletRegistryElement getRegistryElement(String name) {
    return (PortletRegistryElement) portletWindowTable.get(name);
  }

  public List getRegistryElements() {
    return this.portletWindowList;
  }

  public boolean removeRegistryElement(PortletRegistryElement portletWindow) {
    portletWindowTable.remove(portletWindow.getName());
    return portletWindowList.remove(portletWindow);
  }

  private void populate(Element root) {
    // Get the attributes for PortletWindowRegistry Tag.
    Map portletWindowRegistryAttributes = XMLDocumentHelper
        .createAttributeTable(root);
    setVersion((String) portletWindowRegistryAttributes
        .get(PortletRegistryTags.VERSION_KEY));
    // Get a list of PortletWindow tags and populate values from it.
    List portletWindowTags = XMLDocumentHelper.createElementList(root);
    int numOfPortletWindowTags = portletWindowTags.size();
    PortletWindow portletWindow;
    for (int i = 0; i < numOfPortletWindowTags; i++) {
      Element portletWindowTag = (Element) portletWindowTags.get(i);
      portletWindow = new PortletWindow();
      portletWindow.populateValues(portletWindowTag);
      addRegistryElement(portletWindow);
    }
    // System.out.println(portletWindowTable);
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
        PORTLET_WINDOW_REGISTRY_TAG);
    // Add the atribute to the child
    rootTag.setAttribute(VERSION_KEY, getVersion());
    document.appendChild(rootTag);
    Iterator itr = portletWindowTable.values().iterator();
    PortletWindow portletWindow;
    int newRow = -1;
    while (itr.hasNext()) {
      portletWindow = (PortletWindow) itr.next();
      // Get the new row number.
      int row = portletWindow.getRow();
      if (row == -1) {
        // row number has not been assigned, use the new row number
        newRow++;
        portletWindow.setStringProperty(ROW_KEY, String.valueOf(newRow));
      }
      if (row > newRow) {
        newRow = row;
      }
      if (portletWindow.getStringProperty(VISIBLE_KEY) == null) {
        portletWindow.setStringProperty(VISIBLE_KEY,
            PortletRegistryConstants.VISIBLE_TRUE);
      }
      if (portletWindow.getStringProperty(WIDTH_KEY) == null) {
        portletWindow.setStringProperty(WIDTH_KEY,
            PortletRegistryConstants.WIDTH_THICK);
      }
      portletWindow.create(document, rootTag);
    }
  }
}
