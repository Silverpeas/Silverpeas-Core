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
import java.util.logging.Level;
import java.util.logging.Logger;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * PortletWindow represents the PortletWindow Element in
 * portlet-window-registry.xml
 */
public class PortletWindow extends AbstractPortletRegistryElement {

  private static Logger logger = Logger.getLogger(
      "com.sun.portal.portletcontainer.admin.registry",
      "com.silverpeas.portlets.PALogMessages");

  public PortletWindow() {
  }

  public int getRow() {
    String row = getStringProperty(ROW_KEY);
    int rowNumber = -1;
    if (row != null) {
      try {
        rowNumber = Integer.parseInt(row);
      } catch (NumberFormatException nfe) {
        logger.log(Level.WARNING, "PSPL_CSPPAM0002", row);
      }
    }
    return rowNumber;
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

  protected void populateValues(Element portletWindowTag) {
    // Get the attributes for PortletWindow Tag.
    Map portletWindowAttributes = XMLDocumentHelper
        .createAttributeTable(portletWindowTag);
    setName((String) portletWindowAttributes.get(NAME_KEY));
    setPortletName((String) portletWindowAttributes.get(PORTLET_NAME_KEY));
    setRemote((String) portletWindowAttributes.get(REMOTE_KEY));
    setLang((String) portletWindowAttributes.get(LANG));
    super.populateValues(portletWindowTag);
  }

  protected void create(Document document, Element rootTag) {
    // Create PortletWindow tag, set attributes and append it to the document
    Element portletWindowTag = XMLDocumentHelper.createElement(document,
        PORTLET_WINDOW_TAG);
    portletWindowTag.setAttribute(NAME_KEY, getName());
    portletWindowTag.setAttribute(PORTLET_NAME_KEY, getPortletName());
    portletWindowTag.setAttribute(REMOTE_KEY, getRemote());
    portletWindowTag.setAttribute(LANG, getLang());
    rootTag.appendChild(portletWindowTag);
    // Create Properties tag and append it to the document
    Element propertiesTag = XMLDocumentHelper.createElement(document,
        PROPERTIES_TAG);
    portletWindowTag.appendChild(propertiesTag);
    super.create(document, propertiesTag);
  }

}
