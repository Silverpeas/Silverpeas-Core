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

import org.w3c.dom.Document;

import com.sun.portal.portletcontainer.context.registry.PortletRegistryException;

/**
 * 
 * PortletRegistryObject represents a Portlet registry document. It can be
 * either PortletAppRegistry, PortletWindowRegistry or
 * PortletWindowPreferenceRegistry.
 */
public interface PortletRegistryObject {

  /**
   * Write the portlet registry document to the repository.
   * 
   * @param document
   *          the <code>Document</code>
   */
  public void write(Document document) throws PortletRegistryException;

  /**
   * Read the portlet registry document and populate this object.
   * 
   * @param document
   *          the <code>Document</code>
   */
  public void read(Document document) throws PortletRegistryException;

  /**
   * Add the portlet registry element to this object.
   * 
   * @param portletRegistryElement
   *          the <code>PortletRegistryElement</code>
   */
  public void addRegistryElement(PortletRegistryElement portletRegistryElement);

  /**
   * Remove the portlet registry element from this object.
   * 
   * @param portletRegistryElement
   *          the <code>PortletRegistryElement</code>
   * 
   * @return a true, if the portlet registry element is successfully removed.
   */
  public boolean removeRegistryElement(
      PortletRegistryElement portletRegistryElement);

  /**
   * Returns the portlet registry element for the specified name. This can be
   * either be PortletAppRegistry, PortletWindowRegistry or
   * PortletWindowPreferenceRegistry objects.
   * 
   * @param name
   *          the value of "name" attribute of the portlet registry element
   * 
   * @return a <code>PortletRegistryElement</code>, the element in the portlet
   *         registry.
   */
  public PortletRegistryElement getRegistryElement(String name);

  /**
   * Returns a list of all portlet registry elements in the portlet registry.
   * 
   * @return a <code>List</code> of all portlet registry elements in the portlet
   *         registr.
   */
  public List getRegistryElements();

}
