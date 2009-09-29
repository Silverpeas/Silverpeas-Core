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
package com.sun.portal.portletcontainer.admin.registry;

import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * PortletWindowPreference represents the PortletWindowPreference Element in
 * portlet-window-preferences.xml
 */
public class PortletWindowPreference extends AbstractPortletRegistryElement {
  public PortletWindowPreference() {
  }

  public String toString() {
    StringBuffer buffer = new StringBuffer();
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
    Map portletWindowPreferenceAttributes = XMLDocumentHelper
        .createAttributeTable(portletWindowPrefTag);
    setName((String) portletWindowPreferenceAttributes.get(NAME_KEY));
    setPortletName((String) portletWindowPreferenceAttributes
        .get(PORTLET_NAME_KEY));
    setUserName((String) portletWindowPreferenceAttributes.get(USER_NAME_KEY));
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
