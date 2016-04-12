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
import com.sun.portal.portletcontainer.admin.PortletRegistryReader;
import com.sun.portal.portletcontainer.admin.PortletRegistryWriter;
import com.sun.portal.portletcontainer.context.registry.PortletRegistryContext;
import com.sun.portal.portletcontainer.context.registry.PortletRegistryException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * PortletWindowPreferenceRegistryContextImpl is a concrete implementation of the
 * PortletWindowPreferenceRegistryContext interface.
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
    PortletRegistryReader portletWindowPreferenceRegistryReader =
        new PortletWindowPreferenceRegistryReader(
        registryLocation, context);
    portletWindowPreferenceRegistry = portletWindowPreferenceRegistryReader
        .readDocument();

    portletWindowPreferenceRegistryReader = new PortletWindowPreferenceRegistryReader(
        registryLocation, null);
    defaultPortletWindowPreferenceRegistry = portletWindowPreferenceRegistryReader
        .readDocument();
  }

  @Override
  public Map<String, Object> getPreferencesReadOnly(String portletWindowName, String userName)
      throws PortletRegistryException {
    PortletRegistryElement portletRegistryElement = getDefaultRegistryElement(portletWindowName
        + getDefaultUserName());
    Map<String, Object> map = portletRegistryElement
        .getCollectionProperty(PortletRegistryTags.PREFERENCE_READ_ONLY_KEY);
    return map;
  }

  @Override
  public Map<String, Object> getPreferences(String portletWindowName, String userName)
      throws PortletRegistryException {
    // PortletRegistryElement predefinedPortletRegistryElement =
    // getDefaultRegistryElement(portletWindowName+getDefaultUserName());
    PortletRegistryElement predefinedPortletRegistryElement = null;
    Map<String, Object> predefinedPrefMap = null;
    if (predefinedPortletRegistryElement != null) {
      predefinedPrefMap = predefinedPortletRegistryElement
          .getCollectionProperty(PortletRegistryTags.PREFERENCE_PROPERTIES_KEY);
    }
    PortletRegistryElement userPortletRegistryElement = portletWindowPreferenceRegistry
        .getRegistryElement(portletWindowName + userName);
    Map<String, Object> tempUserPrefMap = null;
    if (userPortletRegistryElement != null) {
      tempUserPrefMap = userPortletRegistryElement
          .getCollectionProperty(PortletRegistryTags.PREFERENCE_PROPERTIES_KEY);
    }
    // The Pref Map of the user has the same content as that of the predefined
    // map
    // And its overwritten by user customizations
    Map<String, Object> userPrefMap;
    if (predefinedPrefMap != null) {
      userPrefMap = new HashMap<>(predefinedPrefMap);
    } else {
      userPrefMap = new HashMap<>();
    }

    if (tempUserPrefMap != null) {
      userPrefMap.putAll(tempUserPrefMap);
    }
    return userPrefMap;
  }

  @Override
  public void savePreferences(String portletName, String portletWindowName, String userName,
      Map<String, Object> prefMap) throws PortletRegistryException {
    savePreferences(portletName, portletWindowName, userName, prefMap, false);
  }

  @Override
  public void savePreferences(String portletName, String portletWindowName, String userName,
      Map<String, Object> prefMap, boolean readOnly)
      throws PortletRegistryException {
    // if readOnly save readOnly preferences also
    Map<String, Object> readOnlyMap = null;
    if (readOnly) {
      readOnlyMap = getPreferencesReadOnly(portletName, userName);
    }
    PortletRegistryElement userPortletRegistryElement = portletWindowPreferenceRegistry
        .getRegistryElement(portletWindowName + userName);
    Map<String, Object> userPrefMap = null;
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
      userPrefMap = new HashMap<>();
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

  @Override
  public void removeWindowPreference(String portletWindowName)
      throws PortletRegistryException {
    // Prepare a list of portlet window preference that are based on the
    // portletName
    List<PortletRegistryElement> portletWindowPreferences = portletWindowPreferenceRegistry
        .getRegistryElements();
    // Maintains a list of portlet window preferences to be removed
    List<PortletRegistryElement> removeablePortletWindowPreferences = new ArrayList<>();
    boolean remove = false;
    for (PortletRegistryElement portletWindowPreference : portletWindowPreferences) {
      if (portletWindowPreference.getName().equals(portletWindowName)) {
        remove = true;
        removeablePortletWindowPreferences.add(portletWindowPreference);
      }
    }
    for (PortletRegistryElement portletWindowPreference : removeablePortletWindowPreferences) {
      portletWindowPreferenceRegistry
          .removeRegistryElement(portletWindowPreference);
    }
    if (remove) {
      writeDocument(portletWindowPreferenceRegistry);
    }
  }

  @Override
  public void removePreferences(String portletName)
      throws PortletRegistryException {
    // Prepare a list of portlet window preference that are based on the
    // portletName
    List<PortletRegistryElement> portletWindowPreferences = portletWindowPreferenceRegistry
        .getRegistryElements();
    // Maintains a list of portlet window preferences to be removed
    List<PortletRegistryElement> removeablePortletWindowPreferences = new ArrayList<>();
    boolean remove = false;
    for (PortletRegistryElement portletWindowPreference : portletWindowPreferences) {
      if (portletWindowPreference.getPortletName().equals(portletName)) {
        remove = true;
        removeablePortletWindowPreferences.add(portletWindowPreference);
      }
    }
    for (PortletRegistryElement portletWindowPreference : removeablePortletWindowPreferences) {
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
    List<PortletRegistryElement> portletWindowPreferenceElementList = new ArrayList<>();
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
    List<PortletRegistryElement> portletWindowPreferenceElementList =
        portletWindowPreferenceRegistry.getRegistryElements();
    try {
      portletWindowPreferenceRegistryWriter
          .writeDocument(portletWindowPreferenceElementList);
    } catch (Exception e) {
      throw new PortletRegistryException(e);
    }
  }
}
