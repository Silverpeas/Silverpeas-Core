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
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * PortletWindow represents the PortletWindow Element in portlet-window-registry.xml
 */
public class PortletWindow extends AbstractPortletRegistryElement {

  private static Logger logger = Logger.getLogger("com.sun.portal.portletcontainer.admin.registry",
      "org.silverpeas.portlets.PALogMessages");

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

  protected void populateValues(Element portletWindowTag) {
    // Get the attributes for PortletWindow Tag.
    Map<String, String> portletWindowAttributes = XMLDocumentHelper
        .createAttributeTable(portletWindowTag);
    setName(portletWindowAttributes.get(NAME_KEY));
    setPortletName(portletWindowAttributes.get(PORTLET_NAME_KEY));
    setRemote(portletWindowAttributes.get(REMOTE_KEY));
    setLang(portletWindowAttributes.get(LANG));
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
