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

import java.util.ArrayList;
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
 * PortletWindowRegistry represents the PortletWindowRegistry Element in portlet-window-registry.xml
 */
public class PortletWindowRegistry implements PortletRegistryTags, PortletRegistryObject {

  private String version;
  private Map<String, PortletRegistryElement> portletWindowTable;
  private List<PortletRegistryElement> portletWindowList;

  public PortletWindowRegistry() {
    portletWindowTable = new LinkedHashMap<>();
    portletWindowList = new ArrayList<>();
  }

  @Override
  public void read(Document document) throws PortletRegistryException {
    Element root = PortletRegistryHelper.getRootElement(document);
    if (root != null)
      populate(root);
  }

  @Override
  public void addRegistryElement(PortletRegistryElement portletWindow) {
    portletWindowTable.put(portletWindow.getName(), portletWindow);
    portletWindowList.add(portletWindow);
  }

  @Override
  public PortletRegistryElement getRegistryElement(String name) {
    return portletWindowTable.get(name);
  }

  @Override
  public List<PortletRegistryElement> getRegistryElements() {
    return this.portletWindowList;
  }

  @Override
  public boolean removeRegistryElement(PortletRegistryElement portletWindow) {
    portletWindowTable.remove(portletWindow.getName());
    return portletWindowList.remove(portletWindow);
  }

  private void populate(Element root) {
    // Get the attributes for PortletWindowRegistry Tag.
    Map<String, String> portletWindowRegistryAttributes = XMLDocumentHelper
        .createAttributeTable(root);
    setVersion(portletWindowRegistryAttributes.get(PortletRegistryTags.VERSION_KEY));
    // Get a list of PortletWindow tags and populate values from it.
    List<Element> portletWindowTags = XMLDocumentHelper.createElementList(root);
    PortletWindow portletWindow;
    for (Element portletWindowTag: portletWindowTags) {
      portletWindow = new PortletWindow();
      portletWindow.populateValues(portletWindowTag);
      addRegistryElement(portletWindow);
    }
  }

  public String getVersion() {
    if (this.version == null)
      return PortletRegistryConstants.VERSION;
    return this.version;
  }

  public void setVersion(String version) {
    this.version = version;
  }

  @Override
  public void write(Document document) {
    Element rootTag = XMLDocumentHelper.createElement(document,
        PORTLET_WINDOW_REGISTRY_TAG);
    // Add the atribute to the child
    rootTag.setAttribute(VERSION_KEY, getVersion());
    document.appendChild(rootTag);
    int newRow = -1;
    for (PortletRegistryElement portletWindowElt: portletWindowTable.values()) {
      PortletWindow portletWindow = (PortletWindow) portletWindowElt;
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
