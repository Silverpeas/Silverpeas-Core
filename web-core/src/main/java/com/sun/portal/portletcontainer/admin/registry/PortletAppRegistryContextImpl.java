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
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.sun.portal.portletcontainer.admin.PortletRegistryElement;
import com.sun.portal.portletcontainer.admin.PortletRegistryHelper;
import com.sun.portal.portletcontainer.admin.PortletRegistryObject;
import com.sun.portal.portletcontainer.admin.PortletRegistryReader;
import com.sun.portal.portletcontainer.admin.PortletRegistryWriter;
import com.sun.portal.portletcontainer.context.registry.PortletRegistryException;

/**
 * PortletAppRegistryContextImpl is a concrete implementation of the PortletAppRegistryContext
 * interface.
 */
public class PortletAppRegistryContextImpl implements PortletAppRegistryContext {

  PortletRegistryObject portletAppRegistry;

  public PortletAppRegistryContextImpl() throws PortletRegistryException {
    String registryLocation = PortletRegistryHelper.getRegistryLocation();
    PortletRegistryReader portletAppRegistryReader = new PortletAppRegistryReader(registryLocation);
    portletAppRegistry = portletAppRegistryReader.readDocument();
  }

  public List getMarkupTypes(String portletName) throws PortletRegistryException {
    PortletRegistryElement portletRegistryElement = getRegistryElement(portletName);
    Map map =
        portletRegistryElement
        .getCollectionProperty(PortletRegistryTags.SUPPORTED_CONTENT_TYPES_KEY);
    List markupTypes = null;
    if (map != null)
      markupTypes = mapValuesToList(map);
    return markupTypes;
  }

  public String getDescription(String portletName, String desiredLocale)
      throws PortletRegistryException {
    PortletRegistryElement portletRegistryElement = getRegistryElement(portletName);
    Map map = portletRegistryElement.getCollectionProperty(PortletRegistryTags.DESCRIPTION_MAP_KEY);
    String description = null;
    if (map != null)
      description = (String) map.get(desiredLocale);
    return description;
  }

  public String getShortTitle(String portletName, String desiredLocale)
      throws PortletRegistryException {
    PortletRegistryElement portletRegistryElement = getRegistryElement(portletName);
    String shortTitle = portletRegistryElement.getStringProperty(PortletRegistryTags.TITLE_KEY);
    return shortTitle;
  }

  public String getTitle(String portletName, String desiredLocale) throws PortletRegistryException {
    PortletRegistryElement portletRegistryElement = getRegistryElement(portletName);
    String title = portletRegistryElement.getStringProperty(PortletRegistryTags.TITLE_KEY);
    return title;
  }

  public List getKeywords(String portletName, String desiredLocale) throws PortletRegistryException {
    PortletRegistryElement portletRegistryElement = getRegistryElement(portletName);
    Map map = portletRegistryElement.getCollectionProperty(PortletRegistryTags.KEYWORDS_KEY);
    List keywords = null;
    if (map != null)
      keywords = mapValuesToList(map);
    return keywords;
  }

  public String getDisplayName(String portletName, String desiredLocale)
      throws PortletRegistryException {
    PortletRegistryElement portletRegistryElement = getRegistryElement(portletName);
    Map map =
        portletRegistryElement.getCollectionProperty(PortletRegistryTags.DISPLAY_NAME_MAP_KEY);
    String displayName = null;
    if (map != null)
      displayName = (String) map.get(desiredLocale);
    return displayName;
  }

  public Map getRoleMap(String portletName) throws PortletRegistryException {
    PortletRegistryElement portletRegistryElement = getRegistryElement(portletName);
    Map roleMap = portletRegistryElement.getCollectionProperty(PortletRegistryTags.ROLE_MAP_KEY);
    return roleMap;
  }

  public Map getUserInfoMap(String portletName) throws PortletRegistryException {
    PortletRegistryElement portletRegistryElement = getRegistryElement(portletName);
    Map userInfoMap =
        portletRegistryElement.getCollectionProperty(PortletRegistryTags.USER_INFO_MAP_KEY);
    return userInfoMap;
  }

  public void removePortlet(String portletName) throws PortletRegistryException {
    // Prepare a list of portlet app that are based on the portletName
    List portletApps = portletAppRegistry.getRegistryElements();
    // Maintains a list of portlet apps to be removed
    List removeablePortletApps = new ArrayList();
    PortletRegistryElement portletApp;
    boolean remove = false;
    int size = portletApps.size();
    for (int i = 0; i < size; i++) {
      portletApp = (PortletRegistryElement) portletApps.get(i);
      if (portletApp.getPortletName().equals(portletName)) {
        remove = true;
        removeablePortletApps.add(portletApp);
      }
    }
    size = removeablePortletApps.size();
    for (int i = 0; i < size; i++) {
      portletApp = (PortletRegistryElement) removeablePortletApps.get(i);
      portletAppRegistry.removeRegistryElement(portletApp);
    }
    if (remove) {
      writeDocument(portletAppRegistry);
    }
  }

  public boolean hasView(String portletName) throws PortletRegistryException {
    return true;
  }

  public boolean hasEdit(String portletName) throws PortletRegistryException {
    List<String> list = getSupportedPortletModes(portletName);
    return list.contains("EDIT");
  }

  public boolean hasHelp(String portletName) throws PortletRegistryException {
    List<String> list = getSupportedPortletModes(portletName);
    return list.contains("HELP");
  }

  public List getAvailablePortlets() throws PortletRegistryException {
    List portlets = portletAppRegistry.getRegistryElements();
    List availablePortlets = new ArrayList();
    int size = portlets.size();
    PortletApp portletApp;
    String portletName;
    for (int i = 0; i < size; i++) {
      portletApp = (PortletApp) portlets.get(i);
      portletName = portletApp.getPortletName();
      availablePortlets.add(portletName);
    }
    return availablePortlets;
  }

  // TODO - should be based on mime-type
  private List<String> getSupportedPortletModes(String portletName) throws PortletRegistryException {
    PortletRegistryElement portletRegistryElement = getRegistryElement(portletName);
    Map map = portletRegistryElement.getCollectionProperty(PortletRegistryTags.SUPPORTS_MAP_KEY);
    List<String> list = new ArrayList<String>();
    Set<Map.Entry<String, List<String>>> entries = map.entrySet();
    for (Map.Entry<String, List<String>> mapEntry : entries) {
      for (String s : mapEntry.getValue()) {
        list.add(s);
      }
    }
    return list;
  }

  private PortletRegistryElement getRegistryElement(String name) throws PortletRegistryException {
    PortletRegistryElement portletRegistryElement = portletAppRegistry.getRegistryElement(name);
    if (portletRegistryElement == null)
      throw new PortletRegistryException(name + " does not exist");
    return portletRegistryElement;
  }

  private List mapValuesToList(Map map) {
    List list = new ArrayList();
    Set keys = map.keySet();
    Iterator itr = keys.iterator();
    while (itr.hasNext()) {
      list.add((String) map.get(itr.next()));
    }
    return list;
  }

  private PortletRegistryWriter getPortletRegistryWriter() throws PortletRegistryException {
    String registryLocation = PortletRegistryHelper.getRegistryLocation();
    return new PortletAppRegistryWriter(registryLocation);
  }

  /*
   * private void appendDocument(PortletRegistryElement portletRegistryElement) throws
   * PortletRegistryException { PortletRegistryWriter portletAppRegistryWriter =
   * getPortletRegistryWriter(); List portletAppElementList = new ArrayList();
   * portletAppElementList.add(portletRegistryElement); try {
   * portletAppRegistryWriter.appendDocument(portletAppElementList); } catch (Exception e) { throw
   * new PortletRegistryException(e); } }
   */

  private void writeDocument(PortletRegistryObject portletAppRegistry)
      throws PortletRegistryException {
    PortletRegistryWriter portletAppRegistryWriter = getPortletRegistryWriter();
    List portletAppElementList = portletAppRegistry.getRegistryElements();
    try {
      portletAppRegistryWriter.writeDocument(portletAppElementList);
    } catch (Exception e) {
      throw new PortletRegistryException(e);
    }
  }
}
