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
package com.sun.portal.portletcontainer.admin;

import java.util.List;
import java.util.Map;

import org.silverpeas.core.web.portlets.portal.PortletWindowData;
import com.sun.portal.container.EntityID;
import com.sun.portal.container.PortletLang;
import com.sun.portal.container.PortletType;
import com.sun.portal.portletcontainer.admin.registry.PortletAppRegistryContext;
import com.sun.portal.portletcontainer.admin.registry.PortletAppRegistryContextImpl;
import com.sun.portal.portletcontainer.admin.registry.PortletWindowPreferenceRegistryContext;
import com.sun.portal.portletcontainer.admin.registry.PortletWindowPreferenceRegistryContextImpl;
import com.sun.portal.portletcontainer.admin.registry.PortletWindowRegistryContext;
import com.sun.portal.portletcontainer.admin.registry.PortletWindowRegistryContextImpl;
import com.sun.portal.portletcontainer.context.registry.PortletRegistryContext;
import com.sun.portal.portletcontainer.context.registry.PortletRegistryException;

/**
 * PortletRegistryContextImpl is a concrete implementation of the PortletRegistryContext interface.
 * This delegates the method invocations to the appropriate objects that deal with a specific
 * portlet registry.
 */
public class PortletRegistryContextImpl implements PortletRegistryContext {

  private static Object syncObject = new Object();
  private PortletAppRegistryContext portletAppRegistryContext;
  private PortletWindowRegistryContext portletWindowRegistryContext;
  private PortletWindowPreferenceRegistryContext portletWindowPreferenceRegistryContext;

  public PortletRegistryContextImpl() {
  }

  @Override
  public void init(String context) throws PortletRegistryException {
    synchronized (syncObject) {
      if (!PortletRegistryCache.readPortletAppRegistryCache()
          || PortletRegistryCache.getPortletAppRegistryContext() == null) {
        portletAppRegistryContext = new PortletAppRegistryContextImpl();
        PortletRegistryCache.setPortletAppRegistryContext(portletAppRegistryContext);
        PortletRegistryCache.refreshPortletAppRegistryCache(false);
      }
      portletAppRegistryContext = PortletRegistryCache.getPortletAppRegistryContext();
      // if(!PortletRegistryCache.readPortletWindowRegistryCache()
      // || PortletRegistryCache.getPortletWindowRegistryContext() == null) {
      portletWindowRegistryContext = new PortletWindowRegistryContextImpl(context);
      PortletRegistryCache.setPortletWindowRegistryContext(portletWindowRegistryContext);
      PortletRegistryCache.refreshPortletWindowRegistryCache(false);
      // }
      portletWindowRegistryContext = PortletRegistryCache.getPortletWindowRegistryContext();
      // if(!PortletRegistryCache.readPortletWindowPreferenceRegistryCache()
      // || PortletRegistryCache.getPortletWindowPreferenceRegistryContext() == null) {
      portletWindowPreferenceRegistryContext =
          new PortletWindowPreferenceRegistryContextImpl(context);
      PortletRegistryCache
          .setPortletWindowPreferenceRegistryContext(portletWindowPreferenceRegistryContext);
      PortletRegistryCache.refreshPortletWindowPreferenceRegistryCache(false);
      // }
      portletWindowPreferenceRegistryContext =
          PortletRegistryCache.getPortletWindowPreferenceRegistryContext();
    }
  }

  @Override
  public List<String> getMarkupTypes(String portletName) throws PortletRegistryException {
    return portletAppRegistryContext.getMarkupTypes(portletName);
  }

  @Override
  public String getDescription(String portletName, String desiredLocale)
      throws PortletRegistryException {
    return portletAppRegistryContext.getDescription(portletName, desiredLocale);
  }

  @Override
  public String getShortTitle(String portletName, String desiredLocale)
      throws PortletRegistryException {
    return portletAppRegistryContext.getShortTitle(portletName, desiredLocale);
  }

  @Override
  public String getTitle(String portletName, String desiredLocale) throws PortletRegistryException {
    return portletAppRegistryContext.getTitle(portletName, desiredLocale);
  }

  @Override
  public List<String> getKeywords(String portletName, String desiredLocale) throws PortletRegistryException {
    return portletAppRegistryContext.getKeywords(portletName, desiredLocale);
  }

  @Override
  public String getDisplayName(String portletName, String desiredLocale)
      throws PortletRegistryException {
    return portletAppRegistryContext.getDisplayName(portletName, desiredLocale);
  }

  @Override
  public Map<String, Object> getRoleMap(String portletName) throws PortletRegistryException {
    return portletAppRegistryContext.getRoleMap(portletName);
  }

  @Override
  public Map<String, Object> getUserInfoMap(String portletName) throws PortletRegistryException {
    return portletAppRegistryContext.getUserInfoMap(portletName);
  }

  @Override
  public boolean hasView(String portletName) throws PortletRegistryException {
    return portletAppRegistryContext.hasView(portletName);
  }

  @Override
  public boolean hasEdit(String portletName) throws PortletRegistryException {
    return portletAppRegistryContext.hasEdit(portletName);
  }

  @Override
  public boolean hasHelp(String portletName) throws PortletRegistryException {
    return portletAppRegistryContext.hasHelp(portletName);
  }

  @Override
  public List<String> getAvailablePortlets() throws PortletRegistryException {
    return portletAppRegistryContext.getAvailablePortlets();
  }

  @Override
  public List<String> getVisiblePortletWindows(PortletType portletType) throws PortletRegistryException {
    return portletWindowRegistryContext.getVisiblePortletWindows(portletType);
  }

  @Override
  public boolean isVisible(String portletWindowName) throws PortletRegistryException {
    return portletWindowRegistryContext.isVisible(portletWindowName);
  }

  @Override
  public List<String> getAllPortletWindows(PortletType portletType) throws PortletRegistryException {
    return portletWindowRegistryContext.getAllPortletWindows(portletType);
  }

  @Override
  public Integer getRowNumber(String portletWindowName) throws PortletRegistryException {
    return portletWindowRegistryContext.getRowNumber(portletWindowName);
  }

  @Override
  public String getWidth(String portletWindowName) throws PortletRegistryException {
    return portletWindowRegistryContext.getWidth(portletWindowName);
  }

  @Override
  public void setWidth(String portletWindowName, String width, String row)
      throws PortletRegistryException {
    portletWindowRegistryContext.setWidth(portletWindowName, width, row);
  }

  @Override
  public void movePortletWindows(List<PortletWindowData> portletWindows) throws PortletRegistryException {
    portletWindowRegistryContext.movePortletWindows(portletWindows);
  }

  @Override
  public EntityID getEntityId(String portletWindowName) throws PortletRegistryException {
    return portletWindowRegistryContext.getEntityId(portletWindowName);
  }

  @Override
  public List<EntityID> getEntityIds() throws PortletRegistryException {
    return portletWindowRegistryContext.getEntityIds();
  }

  @Override
  public String getPortletWindowTitle(String portletWindowName) throws PortletRegistryException {
    return portletWindowRegistryContext.getPortletWindowTitle(portletWindowName);
  }

  @Override
  public void setPortletWindowTitle(String portletWindowName, String title)
      throws PortletRegistryException {
    portletWindowRegistryContext.setPortletWindowTitle(portletWindowName, title);
  }

  @Override
  public void createPortletWindow(String portletName, String portletWindowName)
      throws PortletRegistryException {
    portletWindowRegistryContext.createPortletWindow(portletName, portletWindowName);
  }

  @Override
  public void createPortletWindow(String portletName, String portletWindowName, String title,
      String locale) throws PortletRegistryException {
    portletWindowRegistryContext.createPortletWindow(portletName, portletWindowName, title, locale);
    Map<String, Object> exisitingPreferences =
        getPreferences(portletName, PortletRegistryContext.USER_NAME_DEFAULT);
    savePreferences(portletName, portletWindowName, PortletRegistryContext.USER_NAME_DEFAULT,
        exisitingPreferences, true);
  }

  @Override
  public void removePortletWindow(String portletWindowName) throws PortletRegistryException {
    portletWindowRegistryContext.removePortletWindow(portletWindowName);
    portletWindowPreferenceRegistryContext.removeWindowPreference(portletWindowName);
  }

  @Override
  public void showPortletWindow(String portletWindowName, boolean visible)
      throws PortletRegistryException {
    portletWindowRegistryContext.showPortletWindow(portletWindowName, visible);
  }

  @Override
  public void removePortlet(String portletName) throws PortletRegistryException {
    portletAppRegistryContext.removePortlet(portletName);
    portletWindowRegistryContext.removePortletWindows(portletName);
    portletWindowPreferenceRegistryContext.removePreferences(portletName);
  }

  @Override
  public Map<String, Object> getPreferences(String portletWindowName, String userName)
      throws PortletRegistryException {
    return portletWindowPreferenceRegistryContext.getPreferences(portletWindowName, userName);
  }

  @Override
  public Map<String, Object> getPreferencesReadOnly(String portletWindowName, String userName)
      throws PortletRegistryException {
    return portletWindowPreferenceRegistryContext.getPreferencesReadOnly(portletWindowName,
        userName);
  }

  @Override
  public void savePreferences(String portletName, String portletWindowName, String userName,
      Map<String, Object> prefMap) throws PortletRegistryException {
    portletWindowPreferenceRegistryContext.savePreferences(portletName, portletWindowName,
        userName, prefMap);
  }

  private void savePreferences(String portletName, String portletWindowName, String userName,
      Map<String, Object> prefMap, boolean readOnly) throws PortletRegistryException {
    portletWindowPreferenceRegistryContext.savePreferences(portletName, portletWindowName,
        userName, prefMap, readOnly);
  }

  @Override
  public String getPortletName(String portletWindowName) throws PortletRegistryException {
    return portletWindowRegistryContext.getPortletName(portletWindowName);
  }

  @Override
  public List<String> getPortletWindows(String portletName) throws PortletRegistryException {
    return portletWindowRegistryContext.getPortletWindows(portletName);
  }

  @Override
  public String getPortletID(String portletWindowName) throws PortletRegistryException {
    return portletWindowRegistryContext.getPortletID(portletWindowName);
  }

  @Override
  public String getConsumerID(String portletWindowName) throws PortletRegistryException {
    return portletWindowRegistryContext.getConsumerID(portletWindowName);
  }

  @Override
  public String getProducerEntityID(String portletWindowName) throws PortletRegistryException {
    return portletWindowRegistryContext.getProducerEntityID(portletWindowName);
  }

  @Override
  public boolean isRemote(String portletWindowName) throws PortletRegistryException {
    return portletWindowRegistryContext.isRemote(portletWindowName);
  }

  @Override
  public PortletLang getPortletLang(String portletWindowName) throws PortletRegistryException {
    return portletWindowRegistryContext.getPortletLang(portletWindowName);
  }
}
