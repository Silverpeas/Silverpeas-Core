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

import com.sun.portal.portletcontainer.context.registry.PortletRegistryException;

/**
 * PortletRegistryElement represents a element in the portlet registry files. The element can be
 * PortletApp, PortletWindow or PortletWindowPreference
 */
public interface PortletRegistryElement {

  /**
   * Set the value of the "name" attribute of the registry element.
   * @param name the value of the "name" attribute of the registry element.
   */
  public void setName(String name);

  /**
   * Returns the value of "name" attribute of the registry element
   * @return a <code>String</code>, the value of "name" attribute of the registry element.
   */
  public String getName();

  /**
   * Set the value of the "portletName" attribute of the registry element.
   * @param portletName the value of the "portletName" attribute of the registry element.
   */
  public void setPortletName(String portletName);

  /**
   * Returns the value of "portletName" attribute of the registry element
   * @return a <code>String</code>, the value of "portletName" attribute of the registry element.
   */
  public String getPortletName();

  /**
   * Set the value of the "userName" attribute of the registry element.
   * @param userName the value of the "userName" attribute of the registry element.
   */
  public void setUserName(String userName);

  /**
   * Returns the value of "userName" attribute of the registry element
   * @return a <code>String</code>, the value of "userName" attribute of the registry element.
   */
  public String getUserName();

  /**
   * Set the value of the "isRemote" attribute of the registry element.
   * @param isRemote the value of the "isRemote" attribute of the registry element.
   */
  public void setRemote(String isRemote);

  /**
   * Returns the value of "isRemote" attribute of the registry element
   * @return a <code>String</code>, the value of "isRemote" attribute of the registry element.
   */
  public String getRemote();

  /**
   * Set the values for the Collection tag specified by the key. This Collection tag is contained
   * within the registry element. Each value in the list is represented as a value of the "value"
   * attribute in the String tag. This String tag is contained within the Collection tag.
   * @param key the value for the "name" attribute of the Collection tag.
   * @param values represented by "value" attribute of the String tags.
   */
  public void setCollectionProperty(String key, List<String> values)
      throws PortletRegistryException;

  /**
   * Set the values for the Collection tag specified by the key. This Collection tag is contained
   * within the registry element. Each key in the Map is represented as a value of the "name"
   * attribute in the String tag and value for the key is represented as a value for the "value"
   * attribute in the String tag. This String tag is contained within the Collection tag
   * @param key the value for the "name" attribute of the Collection tag.
   * @param values represented by "name" and "value" attribute of the String tags.
   */
  public void setCollectionProperty(String key, Map<String, Object> values)
      throws PortletRegistryException;

  /**
   * Returns the values contained in the Collection tag specified by the key. The Collection tag
   * contains String tags. This String tag has either "name" attribute or both "name" and "value"
   * attribute. If the String tag has only "name" attribute, the Map returned contains the value of
   * the "name" attribute as both key and value. If the String tag has both "name" and "value"
   * attribute, the Map returned contains the value of the "name" attribute as key and value of the
   * "value" attribute as value.
   * @return a <code>Map</code>, the value of attributes "name" and "value" of String tag.
   */
  public Map<String, Object> getCollectionProperty(String key) throws PortletRegistryException;

  public void setStringProperty(String key, String value)
      throws PortletRegistryException;

  /**
   * Returns the value of the "name" attribute for the String tag specified by the key.
   * @return a <code>String</code>, the value represented by the "name" attribute for the String tag
   */
  public String getStringProperty(String key) throws PortletRegistryException;

  /**
   * Set the value of the "lang" attribute of the String tag.
   * @param lang the value represented by the "lang" attribute for the String tag
   */
  public void setLang(String lang);

  /**
   * Returns the value of the "lang" attribute for the String tag specified by the key.
   * @return a <code>String</code>, the value represented by the "lang" attribute for the String tag
   */
  public String getLang();
}
