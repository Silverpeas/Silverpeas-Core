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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.sun.portal.portletcontainer.admin.PortletRegistryElement;
import com.sun.portal.portletcontainer.admin.PortletRegistryHelper;
import com.sun.portal.portletcontainer.admin.PortletRegistryObject;
import com.sun.portal.portletcontainer.admin.PortletRegistryReader;
import com.sun.portal.portletcontainer.admin.PortletRegistryWriter;
import com.sun.portal.portletcontainer.context.registry.PortletRegistryContext;
import com.sun.portal.portletcontainer.context.registry.PortletRegistryException;

/**
 * PortletWindowPreferenceRegistryContextImpl is a concrete implementation of
 * the PortletWindowPreferenceRegistryContext interface.
 */
public class PortletWindowPreferenceRegistryContextImpl implements
    PortletWindowPreferenceRegistryContext {

  PortletRegistryObject portletWindowPreferenceRegistry;
  PortletRegistryObject defaultPortletWindowPreferenceRegistry;
  String context; // a userId or a spaceId

  public PortletWindowPreferenceRegistryContextImpl()
      throws PortletRegistryException {
    init(null);
  }

  public PortletWindowPreferenceRegistryContextImpl(String context)
      throws PortletRegistryException {
    init(context);
  }

  private void init(String context) throws PortletRegistryException {
    this.context = context;
    String registryLocation = PortletRegistryHelper.getRegistryLocation();
    PortletRegistryReader portletWindowPreferenceRegistryReader = new PortletWindowPreferenceRegistryReader(
        registryLocation, context);
    portletWindowPreferenceRegistry = portletWindowPreferenceRegistryReader
        .readDocument();

    portletWindowPreferenceRegistryReader = new PortletWindowPreferenceRegistryReader(
        registryLocation, null);
    defaultPortletWindowPreferenceRegistry = portletWindowPreferenceRegistryReader
        .readDocument();
  }

  public Map getPreferencesReadOnly(String portletWindowName, String userName)
      throws PortletRegistryException {
    PortletRegistryElement portletRegistryElement = getDefaultRegistryElement(portletWindowName
        + getDefaultUserName());
    Map map = portletRegistryElement
        .getCollectionProperty(PortletRegistryTags.PREFERENCE_READ_ONLY_KEY);
    return map;
  }

  public Map getPreferences(String portletWindowName, String userName)
      throws PortletRegistryException {
    // PortletRegistryElement predefinedPortletRegistryElement =
    // getDefaultRegistryElement(portletWindowName+getDefaultUserName());
    PortletRegistryElement predefinedPortletRegistryElement = null;
    Map predefinedPrefMap = null;
    if (predefinedPortletRegistryElement != null) {
      predefinedPrefMap = predefinedPortletRegistryElement
          .getCollectionProperty(PortletRegistryTags.PREFERENCE_PROPERTIES_KEY);
    }
    PortletRegistryElement userPortletRegistryElement = portletWindowPreferenceRegistry
        .getRegistryElement(portletWindowName + userName);
    Map tempUserPrefMap = null;
    if (userPortletRegistryElement != null) {
      tempUserPrefMap = userPortletRegistryElement
          .getCollectionProperty(PortletRegistryTags.PREFERENCE_PROPERTIES_KEY);
    }
    // The Pref Map of the user has the same content as that of the predefined
    // map
    // And its overwritten by user customizations
    Map userPrefMap;
    if (predefinedPrefMap != null) {
      userPrefMap = new HashMap(predefinedPrefMap);
    } else {
      userPrefMap = new HashMap();
    }

    if (tempUserPrefMap != null) {
      userPrefMap.putAll(tempUserPrefMap);
    }
    return userPrefMap;
  }

  public void savePreferences(String portletName, String portletWindowName,
      String userName, Map prefMap) throws PortletRegistryException {
    savePreferences(portletName, portletWindowName, userName, prefMap, false);
  }

  public void savePreferences(String portletName, String portletWindowName,
      String userName, Map prefMap, boolean readOnly)
      throws PortletRegistryException {
    // if readOnly save readOnly preferences also
    Map readOnlyMap = null;
    if (readOnly) {
      readOnlyMap = getPreferencesReadOnly(portletName, userName);
    }
    PortletRegistryElement userPortletRegistryElement = portletWindowPreferenceRegistry
        .getRegistryElement(portletWindowName + userName);
    Map userPrefMap = null;
    if (userPortletRegistryElement == null) {
      userPortletRegistryElement = new PortletWindowPreference();
      userPortletRegistryElement.setName(portletWindowName);
      userPortletRegistryElement.setPortletName(portletName);
      userPortletRegistryElement.setUserName(userName);
    } else {
      userPrefMap = userPortletRegistryElement
          .getCollectionProperty(PortletRegistryTags.PREFERENCE_PROPERTIES_KEY);
    }
    // If there is an exisiting content, override it with fresh content
    if (userPrefMap == null) {
      userPrefMap = new HashMap();
    }
    userPrefMap.putAll(prefMap);
    userPortletRegistryElement.setCollectionProperty(
        PortletRegistryTags.PREFERENCE_PROPERTIES_KEY, userPrefMap);
    if (readOnlyMap != null) {
      userPortletRegistryElement.setCollectionProperty(
          PortletRegistryTags.PREFERENCE_READ_ONLY_KEY, readOnlyMap);
    }
    appendDocument(userPortletRegistryElement);
  }

  public void removeWindowPreference(String portletWindowName)
      throws PortletRegistryException {
    // Prepare a list of portlet window preference that are based on the
    // portletName
    List portletWindowPreferences = portletWindowPreferenceRegistry
        .getRegistryElements();
    // Maintains a list of portlet window preferences to be removed
    List removeablePortletWindowPreferences = new ArrayList();
    PortletRegistryElement portletWindowPreference;
    boolean remove = false;
    int size = portletWindowPreferences.size();
    for (int i = 0; i < size; i++) {
      portletWindowPreference = (PortletRegistryElement) portletWindowPreferences
          .get(i);
      if (portletWindowPreference.getName().equals(portletWindowName)) {
        remove = true;
        removeablePortletWindowPreferences.add(portletWindowPreference);
      }
    }
    size = removeablePortletWindowPreferences.size();
    for (int i = 0; i < size; i++) {
      portletWindowPreference = (PortletRegistryElement) removeablePortletWindowPreferences
          .get(i);
      portletWindowPreferenceRegistry
          .removeRegistryElement(portletWindowPreference);
    }
    if (remove) {
      writeDocument(portletWindowPreferenceRegistry);
    }
  }

  public void removePreferences(String portletName)
      throws PortletRegistryException {
    // Prepare a list of portlet window preference that are based on the
    // portletName
    List portletWindowPreferences = portletWindowPreferenceRegistry
        .getRegistryElements();
    // Maintains a list of portlet window preferences to be removed
    List removeablePortletWindowPreferences = new ArrayList();
    PortletRegistryElement portletWindowPreference;
    boolean remove = false;
    int size = portletWindowPreferences.size();
    for (int i = 0; i < size; i++) {
      portletWindowPreference = (PortletRegistryElement) portletWindowPreferences
          .get(i);
      if (portletWindowPreference.getPortletName().equals(portletName)) {
        remove = true;
        removeablePortletWindowPreferences.add(portletWindowPreference);
      }
    }
    size = removeablePortletWindowPreferences.size();
    for (int i = 0; i < size; i++) {
      portletWindowPreference = (PortletRegistryElement) removeablePortletWindowPreferences
          .get(i);
      portletWindowPreferenceRegistry
          .removeRegistryElement(portletWindowPreference);
    }
    if (remove) {
      writeDocument(portletWindowPreferenceRegistry);
    }
  }

  private PortletRegistryElement getRegistryElement(String name)
      throws PortletRegistryException {
    PortletRegistryElement portletRegistryElement = portletWindowPreferenceRegistry
        .getRegistryElement(name);
    if (portletRegistryElement == null)
      throw new PortletRegistryException(name + " does not exist");
    return portletRegistryElement;
  }

  private PortletRegistryElement getDefaultRegistryElement(String name)
      throws PortletRegistryException {
    PortletRegistryElement portletRegistryElement = defaultPortletWindowPreferenceRegistry
        .getRegistryElement(name);
    if (portletRegistryElement == null)
      throw new PortletRegistryException(name + " does not exist");
    return portletRegistryElement;
  }

  private String getDefaultUserName() {
    return PortletRegistryContext.USER_NAME_DEFAULT;
  }

  private PortletRegistryWriter getPortletRegistryWriter()
      throws PortletRegistryException {
    String registryLocation = PortletRegistryHelper.getRegistryLocation();
    return new PortletWindowPreferenceRegistryWriter(registryLocation, context);
  }

  private void appendDocument(PortletRegistryElement portletRegistryElement)
      throws PortletRegistryException {
    List portletWindowPreferenceElementList = new ArrayList();
    portletWindowPreferenceElementList.add(portletRegistryElement);
    PortletRegistryWriter portletWindowPreferenceRegistryWriter = getPortletRegistryWriter();
    try {
      portletWindowPreferenceRegistryWriter
          .appendDocument(portletWindowPreferenceElementList);
    } catch (Exception e) {
      throw new PortletRegistryException(e);
    }
  }

  private void writeDocument(
      PortletRegistryObject portletWindowPreferenceRegistry)
      throws PortletRegistryException {
    PortletRegistryWriter portletWindowPreferenceRegistryWriter = getPortletRegistryWriter();
    List portletWindowPreferenceElementList = portletWindowPreferenceRegistry
        .getRegistryElements();
    try {
      portletWindowPreferenceRegistryWriter
          .writeDocument(portletWindowPreferenceElementList);
    } catch (Exception e) {
      throw new PortletRegistryException(e);
    }
  }
}
