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

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.Map;

/**
 * PortletWindowPreference represents the PortletWindowPreference Element in
 * portlet-window-preferences.xml
 */
public class PortletWindowPreference extends AbstractPortletRegistryElement {
  public PortletWindowPreference() {
  }

  public String toString() {
    StringBuilder buffer = new StringBuilder();
    buffer.append("PortletName:");
    buffer.append(getPortletName());
    buffer.append(";Name:");
    buffer.append(getName());
    buffer.append(";Collections(Map):");
    buffer.append(getMapCollectionTable());
    buffer.append(";Collections(String):");
    buffer.append(getStringCollectionTable());
    return buffer.toString();
  }

  protected void populateValues(Element portletWindowPrefTag) {
    // Get the attributes for PortletWindowPreference Tag.
    Map<String, String> portletWindowPreferenceAttributes = XMLDocumentHelper
        .createAttributeTable(portletWindowPrefTag);
    setName(portletWindowPreferenceAttributes.get(NAME_KEY));
    setPortletName(portletWindowPreferenceAttributes
        .get(PORTLET_NAME_KEY));
    setUserName(portletWindowPreferenceAttributes.get(USER_NAME_KEY));
    super.populateValues(portletWindowPrefTag);
  }

  protected void create(Document document, Element rootTag) {
    // Create PortletWindow tag, addPreference attributes and append it to the
    // document
    Element portletWindowPrefTag = XMLDocumentHelper.createElement(document,
        PORTLET_WINDOW_PREFERENCE_TAG);
    portletWindowPrefTag.setAttribute(NAME_KEY, getName());
    portletWindowPrefTag.setAttribute(PORTLET_NAME_KEY, getPortletName());
    portletWindowPrefTag.setAttribute(USER_NAME_KEY, getUserName());
    rootTag.appendChild(portletWindowPrefTag);

    // Create Properties tag and append it to the PorletWindowPreference tag
    Element propertiesTag = XMLDocumentHelper.createElement(document,
        PROPERTIES_TAG);
    portletWindowPrefTag.appendChild(propertiesTag);
    super.create(document, propertiesTag);
  }
}
