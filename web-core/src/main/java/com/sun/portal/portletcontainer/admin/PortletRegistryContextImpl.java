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

package com.sun.portal.portletcontainer.admin;

import java.util.List;
import java.util.Map;

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
 * This delegates the method invocations to the appropriate objects that deal with
 * a specific portlet registry.
 */
public class PortletRegistryContextImpl implements PortletRegistryContext {
    
    private static Object syncObject = new Object();
    private PortletAppRegistryContext portletAppRegistryContext;
    private PortletWindowRegistryContext portletWindowRegistryContext;
    private PortletWindowPreferenceRegistryContext portletWindowPreferenceRegistryContext;
        
    public PortletRegistryContextImpl() throws PortletRegistryException {
    }
    
    public void init(String context) throws PortletRegistryException
    {
    	synchronized(syncObject) {
            if(!PortletRegistryCache.readPortletAppRegistryCache()
                || PortletRegistryCache.getPortletAppRegistryContext() == null) {
                portletAppRegistryContext = new PortletAppRegistryContextImpl();
                PortletRegistryCache.setPortletAppRegistryContext(portletAppRegistryContext);
                PortletRegistryCache.refreshPortletAppRegistryCache(false);
            }
            portletAppRegistryContext = PortletRegistryCache.getPortletAppRegistryContext();
            //if(!PortletRegistryCache.readPortletWindowRegistryCache()
              //  || PortletRegistryCache.getPortletWindowRegistryContext() == null) {
                portletWindowRegistryContext = new PortletWindowRegistryContextImpl(context);
                PortletRegistryCache.setPortletWindowRegistryContext(portletWindowRegistryContext);
                PortletRegistryCache.refreshPortletWindowRegistryCache(false);
            //}
            portletWindowRegistryContext = PortletRegistryCache.getPortletWindowRegistryContext();
            //if(!PortletRegistryCache.readPortletWindowPreferenceRegistryCache()
              //  || PortletRegistryCache.getPortletWindowPreferenceRegistryContext() == null) {
                portletWindowPreferenceRegistryContext = new PortletWindowPreferenceRegistryContextImpl(context);
                PortletRegistryCache.setPortletWindowPreferenceRegistryContext(portletWindowPreferenceRegistryContext);
                PortletRegistryCache.refreshPortletWindowPreferenceRegistryCache(false);
            //}
            portletWindowPreferenceRegistryContext = PortletRegistryCache.getPortletWindowPreferenceRegistryContext();
        }
    }
    
    public List getMarkupTypes(String portletName) throws PortletRegistryException {
        return portletAppRegistryContext.getMarkupTypes(portletName);
    }
    
    public String getDescription(String portletName, String desiredLocale) throws PortletRegistryException {
        return portletAppRegistryContext.getDescription(portletName, desiredLocale);
    }
    
    public String getShortTitle(String portletName, String desiredLocale) throws PortletRegistryException {
        return portletAppRegistryContext.getShortTitle(portletName, desiredLocale);
    }
    
    public String getTitle(String portletName, String desiredLocale) throws PortletRegistryException {
        return portletAppRegistryContext.getTitle(portletName,desiredLocale);
    }
    
    public List getKeywords(String portletName, String desiredLocale) throws PortletRegistryException {
        return portletAppRegistryContext.getKeywords(portletName,desiredLocale);
    }
    
    public String getDisplayName(String portletName, String desiredLocale) throws PortletRegistryException {
        return portletAppRegistryContext.getDisplayName(portletName,desiredLocale);
    }
    
    public Map getRoleMap(String portletName) throws PortletRegistryException {
        return portletAppRegistryContext.getRoleMap(portletName);
    }
    
    public Map getUserInfoMap(String portletName) throws PortletRegistryException {
        return portletAppRegistryContext.getUserInfoMap(portletName);
    }
    
    public boolean hasView(String portletName) throws PortletRegistryException {
        return portletAppRegistryContext.hasView(portletName);
    }
    
    public boolean hasEdit(String portletName) throws PortletRegistryException {
        return portletAppRegistryContext.hasEdit(portletName);
    }
    
    public boolean hasHelp(String portletName) throws PortletRegistryException {
        return portletAppRegistryContext.hasHelp(portletName);
    }
    
    public List getAvailablePortlets() throws PortletRegistryException {
        return portletAppRegistryContext.getAvailablePortlets();
    }
    
    public List getVisiblePortletWindows(PortletType portletType) throws PortletRegistryException {
        return portletWindowRegistryContext.getVisiblePortletWindows(portletType);
    }
    
    public boolean isVisible(String portletWindowName) throws PortletRegistryException {
        return portletWindowRegistryContext.isVisible(portletWindowName);
    }
    
    public List<String> getAllPortletWindows(PortletType portletType) throws PortletRegistryException {
        return portletWindowRegistryContext.getAllPortletWindows(portletType);
    }
    
    public Integer getRowNumber(String portletWindowName) throws PortletRegistryException {
        return portletWindowRegistryContext.getRowNumber(portletWindowName);
    }
    
    public String getWidth(String portletWindowName) throws PortletRegistryException {
        return portletWindowRegistryContext.getWidth(portletWindowName);
    }
    
    public void setWidth(String portletWindowName, String width, String row) throws PortletRegistryException {
        portletWindowRegistryContext.setWidth(portletWindowName, width, row);
    }
    
    public void movePortletWindows(List portletWindows) throws PortletRegistryException {
    	portletWindowRegistryContext.movePortletWindows(portletWindows);
    }
    
    public EntityID getEntityId(String portletWindowName) throws PortletRegistryException {
        return portletWindowRegistryContext.getEntityId(portletWindowName);
    }
    
    public List<EntityID> getEntityIds() throws PortletRegistryException {
        return portletWindowRegistryContext.getEntityIds();
    }
    
    public String getPortletWindowTitle(String portletWindowName) throws PortletRegistryException {
        return portletWindowRegistryContext.getPortletWindowTitle(portletWindowName);
    }
    
    public void setPortletWindowTitle(String portletWindowName, String title) throws PortletRegistryException {
        portletWindowRegistryContext.setPortletWindowTitle(portletWindowName, title);
    }
    
    public void createPortletWindow(String portletName, String portletWindowName) throws PortletRegistryException {
        portletWindowRegistryContext.createPortletWindow(portletName, portletWindowName);
    }
    
    public void createPortletWindow(String portletName, String portletWindowName, String title, String locale) throws PortletRegistryException {
        portletWindowRegistryContext.createPortletWindow(portletName, portletWindowName, title, locale);
        Map exisitingPreferences = getPreferences(portletName, PortletRegistryContext.USER_NAME_DEFAULT);
        savePreferences(portletName, portletWindowName, PortletRegistryContext.USER_NAME_DEFAULT, exisitingPreferences, true);    
    }
    
    public void removePortletWindow(String portletWindowName) throws PortletRegistryException {
        portletWindowRegistryContext.removePortletWindow(portletWindowName);
        portletWindowPreferenceRegistryContext.removeWindowPreference(portletWindowName);
    }
    
    public void showPortletWindow(String portletWindowName, boolean visible) throws PortletRegistryException {
        portletWindowRegistryContext.showPortletWindow(portletWindowName, visible);
    }
    
    public void removePortlet(String portletName) throws PortletRegistryException {
        portletAppRegistryContext.removePortlet(portletName);
        portletWindowRegistryContext.removePortletWindows(portletName);
        portletWindowPreferenceRegistryContext.removePreferences(portletName);
    }
    
    public Map getPreferences(String portletWindowName, String userName) throws PortletRegistryException {
        return portletWindowPreferenceRegistryContext.getPreferences(portletWindowName, userName);
    }
    
    public Map getPreferencesReadOnly(String portletWindowName, String userName) throws PortletRegistryException {
        return portletWindowPreferenceRegistryContext.getPreferencesReadOnly(portletWindowName, userName);
    }
    
    public void savePreferences(String portletName, String portletWindowName, String userName, Map prefMap) throws PortletRegistryException {
        portletWindowPreferenceRegistryContext.savePreferences(portletName, portletWindowName, userName, prefMap);
    }
    
    private void savePreferences(String portletName, String portletWindowName, String userName, Map prefMap, boolean readOnly) throws PortletRegistryException {
        portletWindowPreferenceRegistryContext.savePreferences(portletName, portletWindowName, userName, prefMap, readOnly);
    }
    
    public String getPortletName(String portletWindowName) throws PortletRegistryException {
        return portletWindowRegistryContext.getPortletName(portletWindowName);
    }
    
    public List<String> getPortletWindows(String portletName) throws PortletRegistryException {
        return portletWindowRegistryContext.getPortletWindows(portletName);
    }
    
    public String getPortletID(String portletWindowName) throws PortletRegistryException {
        return portletWindowRegistryContext.getPortletID(portletWindowName);
    }
    
    public String getConsumerID(String portletWindowName) throws PortletRegistryException {
        return portletWindowRegistryContext.getConsumerID(portletWindowName);
    }
    
    public String getProducerEntityID(String portletWindowName) throws PortletRegistryException {
        return portletWindowRegistryContext.getProducerEntityID(portletWindowName);
    }
    
    public boolean isRemote(String portletWindowName) throws PortletRegistryException {
        return portletWindowRegistryContext.isRemote(portletWindowName);
    }
    
    public PortletLang getPortletLang(String portletWindowName) throws PortletRegistryException {
        return portletWindowRegistryContext.getPortletLang(portletWindowName);
    }    
}
