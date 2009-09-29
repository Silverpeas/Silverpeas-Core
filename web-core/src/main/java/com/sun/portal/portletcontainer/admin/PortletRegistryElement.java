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

import com.sun.portal.portletcontainer.context.registry.PortletRegistryException;

/**
 * PortletRegistryElement represents a element in the portlet registry files.
 * The element can be PortletApp, PortletWindow or PortletWindowPreference
 */
public interface PortletRegistryElement {

  /**
   * Set the value of the "name" attribute of the registry element.
   * 
   * @param name
   *          the value of the "name" attribute of the registry element.
   */
  public void setName(String name);

  /**
   * Returns the value of "name" attribute of the registry element
   * 
   * @return a <code>String</code>, the value of "name" attribute of the
   *         registry element.
   */
  public String getName();

  /**
   * Set the value of the "portletName" attribute of the registry element.
   * 
   * @param name
   *          the value of the "portletName" attribute of the registry element.
   */
  public void setPortletName(String portletName);

  /**
   * Returns the value of "portletName" attribute of the registry element
   * 
   * @return a <code>String</code>, the value of "portletName" attribute of the
   *         registry element.
   */
  public String getPortletName();

  /**
   * Set the value of the "userName" attribute of the registry element.
   * 
   * @param name
   *          the value of the "userName" attribute of the registry element.
   */
  public void setUserName(String userName);

  /**
   * Returns the value of "userName" attribute of the registry element
   * 
   * @return a <code>String</code>, the value of "userName" attribute of the
   *         registry element.
   */
  public String getUserName();

  /**
   * Set the value of the "isRemote" attribute of the registry element.
   * 
   * @param name
   *          the value of the "isRemote" attribute of the registry element.
   */
  public void setRemote(String isRemote);

  /**
   * Returns the value of "isRemote" attribute of the registry element
   * 
   * @return a <code>String</code>, the value of "isRemote" attribute of the
   *         registry element.
   */
  public String getRemote();

  /**
   * Set the values for the Collection tag specified by the key. This Collection
   * tag is contained within the registry element. Each value in the list is
   * represented as a value of the "value" attribute in the String tag. This
   * String tag is contained within the Collection tag.
   * 
   * @param key
   *          the value for the "name" attribute of the Collection tag.
   * @param values
   *          represented by "value" attribute of the String tags.
   */
  public void setCollectionProperty(String key, List values)
      throws PortletRegistryException;

  /**
   * Set the values for the Collection tag specified by the key. This Collection
   * tag is contained within the registry element. Each key in the Map is
   * represented as a value of the "name" attribute in the String tag and value
   * for the key is represented as a value for the "value" attribute in the
   * String tag. This String tag is contained within the Collection tag
   * 
   * @param key
   *          the value for the "name" attribute of the Collection tag.
   * @param values
   *          represented by "name" and "value" attribute of the String tags.
   */
  public void setCollectionProperty(String key, Map values)
      throws PortletRegistryException;

  /**
   * Returns the values contained in the Collection tag specified by the key.
   * The Collection tag contains String tags. This String tag has either "name"
   * attribute or both "name" and "value" attribute. If the String tag has only
   * "name" attribute, the Map returned contains the value of the "name"
   * attribute as both key and value. If the String tag has both "name" and
   * "value" attribute, the Map returned contains the value of the "name"
   * attribute as key and value of the "value" attribute as value.
   * 
   * @return a <code>Map</code>, the value of attributes "name" and "value" of
   *         String tag.
   */
  public Map getCollectionProperty(String key) throws PortletRegistryException;

  /**
   * Set the value of the "name" attribute of the String tag.
   * 
   * @param name
   *          the value represented by the "name" attribute for the String tag
   */
  public void setStringProperty(String key, String value)
      throws PortletRegistryException;

  /**
   * Returns the value of the "name" attribute for the String tag specified by
   * the key.
   * 
   * @return a <code>String</code>, the value represented by the "name"
   *         attribute for the String tag
   */
  public String getStringProperty(String key) throws PortletRegistryException;

  /**
   * Set the value of the "lang" attribute of the String tag.
   * 
   * @param lang
   *          the value represented by the "lang" attribute for the String tag
   */
  public void setLang(String lang);

  /**
   * Returns the value of the "lang" attribute for the String tag specified by
   * the key.
   * 
   * @return a <code>String</code>, the value represented by the "lang"
   *         attribute for the String tag
   */
  public String getLang();
}
