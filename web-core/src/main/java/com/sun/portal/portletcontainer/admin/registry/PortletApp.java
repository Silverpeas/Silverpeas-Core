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

import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * PortletApp represents the PortletApp Element in portlet-app-registry.xml
 */
public class PortletApp extends AbstractPortletRegistryElement {

  public PortletApp() {
  }

  public String toString() {
    StringBuilder buffer = new StringBuilder();
    buffer.append("PortletName:");
    buffer.append(getName());
    buffer.append(";Collections(Map):");
    buffer.append(getMapCollectionTable());
    buffer.append(";Collections(String):");
    buffer.append(getStringCollectionTable());
    return buffer.toString();
  }

  protected void populateValues(Element portletAppTag) {
    // Get the attributes for PortletApp Tag.
    Map<String, String> portletAppAttributes = XMLDocumentHelper
        .createAttributeTable(portletAppTag);
    setName(portletAppAttributes.get(NAME_KEY));
    setPortletName(portletAppAttributes.get(PORTLET_NAME_KEY));
    super.populateValues(portletAppTag);
  }

  protected void create(Document document, Element rootTag) {
    // Create PortletApp tag, set attributes and append it to the document
    Element portletAppTag = XMLDocumentHelper.createElement(document,
        PORTLET_APP_TAG);
    portletAppTag.setAttribute(NAME_KEY, getName());
    portletAppTag.setAttribute(PORTLET_NAME_KEY, getPortletName());
    rootTag.appendChild(portletAppTag);
    // Create Properties tag and append it to the document
    Element propertiesTag = XMLDocumentHelper.createElement(document,
        PROPERTIES_TAG);
    portletAppTag.appendChild(propertiesTag);
    super.create(document, propertiesTag);
  }
}
