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

import org.w3c.dom.Document;

import com.sun.portal.portletcontainer.context.registry.PortletRegistryException;

/**
 * PortletRegistryObject represents a Portlet registry document. It can be either
 * PortletAppRegistry, PortletWindowRegistry or PortletWindowPreferenceRegistry.
 */
public interface PortletRegistryObject {

  /**
   * Write the portlet registry document to the repository.
   * @param document the <code>Document</code>
   */
  public void write(Document document) throws PortletRegistryException;

  /**
   * Read the portlet registry document and populate this object.
   * @param document the <code>Document</code>
   */
  public void read(Document document) throws PortletRegistryException;

  /**
   * Add the portlet registry element to this object.
   * @param portletRegistryElement the <code>PortletRegistryElement</code>
   */
  public void addRegistryElement(PortletRegistryElement portletRegistryElement);

  /**
   * Remove the portlet registry element from this object.
   * @param portletRegistryElement the <code>PortletRegistryElement</code>
   * @return a true, if the portlet registry element is successfully removed.
   */
  public boolean removeRegistryElement(
      PortletRegistryElement portletRegistryElement);

  /**
   * Returns the portlet registry element for the specified name. This can be either be
   * PortletAppRegistry, PortletWindowRegistry or PortletWindowPreferenceRegistry objects.
   * @param name the value of "name" attribute of the portlet registry element
   * @return a <code>PortletRegistryElement</code>, the element in the portlet registry.
   */
  public PortletRegistryElement getRegistryElement(String name);

  /**
   * Returns a list of all portlet registry elements in the portlet registry.
   * @return a <code>List</code> of all portlet registry elements in the portlet registr.
   */
  public List<PortletRegistryElement> getRegistryElements();

}
