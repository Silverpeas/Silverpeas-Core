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

package com.sun.portal.portletcontainer.context.registry;

import java.util.List;
import java.util.Map;

import com.sun.portal.container.EntityID;
import com.sun.portal.container.PortletLang;
import com.sun.portal.container.PortletType;

/**
 * PortletRegistryContext provides information pertaining to the portlet registry files.
 * This includes information about portlets, portlet windows, portlet preferences
 */

public interface PortletRegistryContext {
    
    public static final String USER_NAME_DEFAULT = "default";
    
    public void init(String context) throws PortletRegistryException; 

    /**
     * Returns the markup types for the portlet as specified in portlet.xml
     *
     * @param portletName the name of the portlet
     *
     * @return a <code>List</code> of the markup types.
     */
    public List getMarkupTypes(String portletName) throws PortletRegistryException;
    
    /**
     * Returns the description for a portlet for a locale  as specified in portlet.xml
     *
     * @param portletName the name of the portlet
     * @param desiredLocale the locale
     *
     * @return a <code>String</code>, the description
     */
    public String getDescription(String portletName, String desiredLocale) throws PortletRegistryException;
    
    /**
     * Returns the short title for a portlet for a locale  as specified in portlet.xml
     *
     * @param portletName the name of the portlet
     * @param desiredLocale the locale
     *
     * @return a <code>String</code>, the short title
     */
    public String getShortTitle(String portletName, String desiredLocale) throws PortletRegistryException;
    
    /**
     * Returns the title for a portlet for a locale as specified in portlet.xml
     *
     * @param portletName the name of the portlet
     * @param desiredLocale the locale
     *
     * @return a <code>String</code>, the title
     */
    public String getTitle(String portletName, String desiredLocale) throws PortletRegistryException;
    
    /**
     * Returns the keywords for a portlet for a locale as specified in portlet.xml
     *
     * @param portletName the name of the portlet
     * @param desiredLocale the locale
     *
     * @return a <code>List</code> of keywords
     */
    public List getKeywords(String portletName, String desiredLocale) throws PortletRegistryException;
    
    /**
     * Returns the displayname for a portlet for a locale as specified in portlet.xml
     *
     * @param portletName the name of the portlet
     * @param desiredLocale the locale
     *
     * @return a <code>String</code>, the displayname
     */
    public String getDisplayName(String portletName, String desiredLocale) throws PortletRegistryException;
    
    /**
     * Returns the role map for a portlet specified during deploying of the portlet.
     *
     * @param portletName the name of the portlet
     *
     * @return a <code>Map</code> of the roles
     */
    public Map getRoleMap(String portletName) throws PortletRegistryException;
    
    /**
     * Returns the userinfo map for a portlet specified during deploying of the portlet
     *
     * @param portletName the name of the portlet
     *
     * @return a <code>Map</code> of the user information
     */
    public Map getUserInfoMap(String portletName) throws PortletRegistryException;
    
    /**
     * Checks whether the portlet supports VIEW mode
     *
     * @param portletName the name of the portlet
     *
     * @return true, if the portlet supports VIEW mode
     */
    public boolean hasView(String portletName) throws PortletRegistryException;
    
    /**
     * Checks whether the portlet supports EDIT mode
     *
     * @param portletName the name of the portlet
     *
     * @return true, if the portlet supports EDIT mode
     */
    public boolean hasEdit(String portletName) throws PortletRegistryException;
    
    /**
     * Checks whether the portlet supports HELP mode
     *
     * @param portletName the name of the portlet
     *
     * @return true, if the portlet supports HELP mode
     */
    public boolean hasHelp(String portletName) throws PortletRegistryException;
    
    /**
     * Returns all the available portlets. 
     *
     * @return a <code>List</code> of portlets.
     */
    public List getAvailablePortlets() throws PortletRegistryException;
    
    /**
     * Returns the portletName associated with the portlet window
     *
     * @param portletWindowName the name of the portlet window
     *
     * @return a <code>String</code>, the portlet name.
     */
    public String getPortletName(String portletWindowName) throws PortletRegistryException;
    
    /**
     * Returns the portlet windows based on the portlet.
     *
     * @param portletName the name of the portlet
     *
     * @return a <code>List</code> of portlet windows.
     */
    public List getPortletWindows(String portletName) throws PortletRegistryException;
    
    /**
     * Returns true if the Portlet Window is visible, otherwise returns false.
     *
     * @param portletWindowName the name of the portlet window
     *
     * @return true if the portlet windows is visible
     */
    public boolean isVisible(String portletWindowName) throws PortletRegistryException;
    
    /**
     * Returns a list of visible portlet windows based on portlet type. The possible values for portlet type are PortletType.ALL, PortletType.LOCAL and PortletType.REMOTE.
     *
     * @param portletType the type of the portlet. Possible values are PortletType.ALL, PortletType.LOCAL and PortletType.REMOTE 
     *
     * @return a <code>List</code> of the visible portlet windows
     */
    public List getVisiblePortletWindows(PortletType portletType) throws PortletRegistryException;
    
    /**
     * Returns the portlet windows based on the portlet type. This returns both hidden as well as visible portlet windows.
     * The possible values for portlet type are PortletType.ALL, PortletType.LOCAL and PortletType.REMOTE.
     *
     * @param portletType the type of the portlet. Possible values are PortletType.ALL, PortletType.LOCAL and PortletType.REMOTE 
     *
     * @return a <code>List</code> of all portlet windows.
     */
    public List getAllPortletWindows(PortletType portletType) throws PortletRegistryException;
    
    /**
     * Returns the row number of the portlet window
     *
     * @param portletWindowName the name of the portlet window
     *
     * @return a <code>String</code>, the row number.
     */
    public Integer getRowNumber(String portletWindowName) throws PortletRegistryException;
    
    /**
     * Returns the width set for the the portlet window. The width can be either
     * thin or thick.
     *
     * @param portletWindowName the name of the portlet window
     *
     * @return a <code>String</code>, the width.
     */
    public String getWidth(String portletWindowName) throws PortletRegistryException;
    
    /**
     * Sets the width of the portlet window. The width can be either
     * thin or thick.
     *
     * @param portletWindowName the name of the portlet window
     * @param width the width, which can be either thick or thin
     */
    public void setWidth(String portletWindowName, String width, String row) throws PortletRegistryException;
    
    public void movePortletWindows(List portletWindows) throws PortletRegistryException;
    
    /**
     * Returns the entityID for a portlet window
     *
     * @param portletWindowName the name of the portlet window
     *
     * @return a <code>String</code>, the entity Id.
     */
    public EntityID getEntityId(String portletWindowName) throws PortletRegistryException;
    
    /**
     * Returns the entityIDs for all the portlet windows
     *
     * @return a <code>List</code> of entity Ids.
     */
    public List<EntityID> getEntityIds() throws PortletRegistryException;
    
    /**
     * Returns the title for a portlet window.
     *
     * @param portletWindowName the name of the portlet window
     *
     * @return a <code>String</code>, the title
     */
    public String getPortletWindowTitle(String portletWindowName) throws PortletRegistryException;
    
    /**
     * Set the title for the portlet window.
     *
     * @param portletWindowName the name of the portlet window
     * @param title the title for the portlet window
     */
    public void setPortletWindowTitle(String portletWindowName, String title) throws PortletRegistryException;
    
    /**
     * Creates a new portlet window based on the existing portlet window.
     *
     * @param newName the new name of the portlet window
     * @param existingName the name of the portlet window
     */
    public void createPortletWindow(String existingName, String newName) throws PortletRegistryException;
    
    /**
     * Creates a new portlet window based on the existing portlet window along with
     * title and locale.The new portlet window also gets the default preferences from the
     * existing portlet window
     *
     * @param newName the new name of the portlet window
     * @param existingName the name of the portlet window
     */
    public void createPortletWindow(String existingName, String newName, String title, String locale) throws PortletRegistryException;
    
    /**
     * Removes the specified portlet window from the registry.
     *
     * @param name the name of the portlet window to be removed
     */
    public void removePortletWindow(String name) throws PortletRegistryException;
    
    /**
     * Shows or Hides the Portlet Window. If visible is true, shows the Portlet Window
     * and is false, hides the Portlet Window.
     *
     * @param name the name of the portlet window to be shown
     * @param visible true or false
     */
    public void showPortletWindow(String name, boolean visible) throws PortletRegistryException;

    /**
     * Removes the specified portlet entry.
     *
     * @param name the name of the portlet to be removed
     */
    public void removePortlet(String name) throws PortletRegistryException;
    
    /**
     * Returns the preferences for a portlet window and a user name
     *
     * @param portletWindowName the name of the portlet window
     * @param userName the name of the user
     *
     * @return a <code>Map</code>, the preferences.
     */
    public Map getPreferences(String portletWindowName, String userName) throws PortletRegistryException;

    /**
     * Returns the read only information for the preferences obtained using <code>getPreferences</code>
     * The key in the map is the name of the preference, while the value is either
     * "true" or "false".
     *  "true" indicates that the preference is read only, while "false" indicates
     *  the preference can be read/written.
     *
     * @param portletWindowName the name of the portlet window
     * @param userName the name of the user
     *
     * @return a <code>Map</code>, the read only information for preferences.
     */
    public Map getPreferencesReadOnly(String portletWindowName, String userName) throws PortletRegistryException;

    /**
     * Save the preferences for a portlet window and for a user.
     *
     * @param portletName the name of the portlet
     * @param portletWindowName the name of the portlet window
     * @param userName the name of the user
     * @param prefMap the preferences to be saved
     */
    public void savePreferences(String portletName, String portletWindowName, String userName, Map prefMap) throws PortletRegistryException;

    /**
     * Returns the portletID for a portlet window
     *
     * @param portletWindowName the name of the portlet window
     *
     * @return a <code>String</code>, the portlet Id.
     */
    public String getPortletID(String portletWindowName) throws PortletRegistryException;
    
    /** Returns the producerEntityID for a portlet window
     *
     * @param portletWindowName the name of the portlet window
     *
     * @return a <code>String</code>, the producer entity Id.
     */
    public String getProducerEntityID(String portletWindowName) throws PortletRegistryException;
    
    /** Returns the consumerID for a portlet window
     *
     * @param portletWindowName the name of the portlet window
     *
     * @return a <code>String</code>, the consumer Id.
     */
    public String getConsumerID(String portletWindowName) throws PortletRegistryException;
            
    /**
     * Returns the isRemote attribute for a portlet window
     *
     * @param portletWindowName the name of the portlet window
     *
     * @return a <code>boolean</code>, the isRemote.
     */
    public boolean isRemote(String portletWindowName) throws PortletRegistryException;

    /** Returns the lang attribute for a portlet window
     *
     * @param portletWindowName the name of the portlet window
     *
     * @return a <code>PortletLang</code>, the PortletLang indicating the language of Portlet.
     * @throws com.sun.portal.portletcontainer.context.registry.PortletRegistryException 
     */    
    public PortletLang getPortletLang(String portletWindowName) throws PortletRegistryException;    
}
