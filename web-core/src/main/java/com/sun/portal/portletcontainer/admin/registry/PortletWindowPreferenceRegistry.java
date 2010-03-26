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
 * PortletWindowPreferenceRegistry represents the PortletWindowPreferenceRegistry Element in
 * portlet-window-preferences.xml
 */
public class PortletWindowPreferenceRegistry implements PortletRegistryTags, PortletRegistryObject {

  private String version;
  private Map portletWindowPreferenceTable;
  private List portletWindowPreferenceList;

  public PortletWindowPreferenceRegistry() {
    portletWindowPreferenceTable = new LinkedHashMap();
    portletWindowPreferenceList = new ArrayList();
  }

  public void read(Document document) throws PortletRegistryException {
    Element root = PortletRegistryHelper.getRootElement(document);
    if (root != null)
      populate(root);
  }

  public void addRegistryElement(PortletRegistryElement portletWindowPreference) {
    // The unique key is the combination of portletwindowname and username;
    portletWindowPreferenceTable.put(getUniqueName(portletWindowPreference),
        portletWindowPreference);
    portletWindowPreferenceList.add(portletWindowPreference);
  }

  public PortletRegistryElement getRegistryElement(String name) {
    return (PortletRegistryElement) portletWindowPreferenceTable.get(name);
  }

  public List getRegistryElements() {
    return this.portletWindowPreferenceList;
  }

  public boolean removeRegistryElement(
      PortletRegistryElement portletWindowPreference) {
    portletWindowPreferenceTable.remove(getUniqueName(portletWindowPreference));
    return portletWindowPreferenceList.remove(portletWindowPreference);
  }

  /*
   * private Map getPortletWindowPreferenceRegistryTable() { return
   * this.portletWindowPreferenceTable; }
   */

  private void populate(Element root) {
    // Get the attributes for PortletWindowPreferenceRegistry Tag.
    Map portletWindowPreferencesAttributes = XMLDocumentHelper
        .createAttributeTable(root);
    setVersion((String) portletWindowPreferencesAttributes
        .get(PortletRegistryTags.VERSION_KEY));
    // Get a list of PortletWindowPreference tags and populate values from it.
    List portletWindowPrefTags = XMLDocumentHelper.createElementList(root);
    int numOfPortletWindowPrefTags = portletWindowPrefTags.size();
    PortletWindowPreference portletWindowPreference;
    for (int i = 0; i < numOfPortletWindowPrefTags; i++) {
      Element portletWindowPreferenceTag = (Element) portletWindowPrefTags
          .get(i);
      portletWindowPreference = new PortletWindowPreference();
      portletWindowPreference.populateValues(portletWindowPreferenceTag);
      addRegistryElement(portletWindowPreference);
    }
    // System.out.println(portletWindowPreferenceTable);
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
        PORTLET_WINDOW_PREFERENCE_REGISTRY_TAG);
    // Add the atribute to the child
    rootTag.setAttribute(VERSION_KEY, getVersion());
    document.appendChild(rootTag);
    Iterator itr = portletWindowPreferenceTable.values().iterator();
    PortletWindowPreference portletWindowPreference;
    while (itr.hasNext()) {
      portletWindowPreference = (PortletWindowPreference) itr.next();
      portletWindowPreference.create(document, rootTag);
    }
  }

  private String getUniqueName(PortletRegistryElement portletWindowPreference) {
    return portletWindowPreference.getName()
        + portletWindowPreference.getUserName();
  }
}
