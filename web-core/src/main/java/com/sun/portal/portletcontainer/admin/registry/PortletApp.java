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
 * PortletApp represents the PortletApp Element in portlet-app-registry.xml
 */
public class PortletApp extends AbstractPortletRegistryElement {

  public PortletApp() {
  }

  public String toString() {
    StringBuffer buffer = new StringBuffer();
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
    Map portletAppAttributes = XMLDocumentHelper
        .createAttributeTable(portletAppTag);
    setName((String) portletAppAttributes.get(NAME_KEY));
    setPortletName((String) portletAppAttributes.get(PORTLET_NAME_KEY));
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
