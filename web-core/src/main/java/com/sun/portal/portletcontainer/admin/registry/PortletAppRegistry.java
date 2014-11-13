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

import com.sun.portal.portletcontainer.admin.PortletRegistryElement;
import com.sun.portal.portletcontainer.admin.PortletRegistryHelper;
import com.sun.portal.portletcontainer.admin.PortletRegistryObject;
import com.sun.portal.portletcontainer.context.registry.PortletRegistryException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * PortletAppRegistry represents the PortletAppRegistry Element in portlet-app-registry.xml
 */
public class PortletAppRegistry implements PortletRegistryTags, PortletRegistryObject {
  private String version;
  private Map<String, PortletRegistryElement> portletAppTable;
  private List<PortletRegistryElement> portletAppList;

  public PortletAppRegistry() {
    portletAppTable = new LinkedHashMap<>();
    portletAppList = new ArrayList<>();
  }

  @Override
  public void read(Document document) throws PortletRegistryException {
    Element root = PortletRegistryHelper.getRootElement(document);
    if (root != null)
      populate(root);
  }

  @Override
  public void addRegistryElement(PortletRegistryElement portletApp) {
    portletAppTable.put(portletApp.getName(), portletApp);
    portletAppList.add(portletApp);
  }

  @Override
  public PortletRegistryElement getRegistryElement(String name) {
    return portletAppTable.get(name);
  }

  @Override
  public List<PortletRegistryElement> getRegistryElements() {
    return this.portletAppList;
  }

  @Override
  public boolean removeRegistryElement(PortletRegistryElement portletApp) {
    portletAppTable.remove(portletApp.getName());
    return portletAppList.remove(portletApp);
  }

  private Map<String, PortletRegistryElement> getPortletAppTable() {
    return this.portletAppTable;
  }

  private void populate(Element root) {
    // Get the attributes for PortletAppRegistry Tag.
    Map<String, String> portletAppRegistryAttributes = XMLDocumentHelper
        .createAttributeTable(root);
    setVersion(portletAppRegistryAttributes.get(VERSION_KEY));
    // Get a list of PortletApp tags and populate values from it.
    List<Element> portletAppTags = XMLDocumentHelper.createElementList(root);
    PortletApp portletApp;
    for (Element portletAppTag : portletAppTags) {
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

  @Override
  public void write(Document document) {
    Element rootTag = XMLDocumentHelper.createElement(document,
        PORTLET_APP_REGISTRY_TAG);
    // Add the attribute to the child
    rootTag.setAttribute(VERSION_KEY, getVersion());
    document.appendChild(rootTag);
    Collection<PortletRegistryElement> collection = getPortletAppTable().values();
    PortletApp portletApp;
    for (PortletRegistryElement portletAppElt : collection) {
      portletApp = (PortletApp) portletAppElt;
      portletApp.create(document, rootTag);
    }
  }
}
