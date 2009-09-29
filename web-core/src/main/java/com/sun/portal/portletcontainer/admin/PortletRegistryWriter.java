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

import java.io.File;
import java.util.List;

import org.w3c.dom.Document;

import com.silverpeas.util.StringUtil;
import com.sun.portal.portletcontainer.context.registry.PortletRegistryException;

/**
 * PortletRegistryWriter is responsible for writing to the Registry xmls. There
 * will be concrete classes to write to portlet-app-registry.xml
 * portlet-window-registry.xml portlet-window-preference-registry.xml.
 */
public abstract class PortletRegistryWriter {

  private File file;
  protected String registryLocation;
  protected String context;

  public PortletRegistryWriter(String registryLocation, String filename,
      String context) {
    this.context = context;
    if (!StringUtil.isDefined(context))
      context = "";
    else
      context = context + "-";

    file = new File(registryLocation + File.separator + context + filename);
    this.registryLocation = registryLocation;
  }

  /*
   * public PortletRegistryWriter(String registryLocation, String filename) {
   * file = new File(registryLocation + File.separator + filename);
   * this.registryLocation = registryLocation; }
   */

  protected void write(PortletRegistryObject portletRegistryObject)
      throws PortletRegistryException {
    // Create blank DOM Document
    Document document = PortletRegistryHelper.getDocumentBuilder()
        .newDocument();
    portletRegistryObject.write(document);
    PortletRegistryHelper.writeFile(document, file);
  }

  /**
   * Appends contents in the list to the specified registry xml file in the file
   * system.
   * 
   * @param portletRegistryElementList
   *          a <code>List</code> of Portlet registry elements
   */
  public abstract void appendDocument(List portletRegistryElementList)
      throws PortletRegistryException;

  /**
   * Writes the contents in the list to the specified registry xml file in the
   * file system.
   * 
   * @param portletRegistryElementList
   *          a <code>List</code> of Portlet registry elements
   */
  public abstract void writeDocument(List portletRegistryElementList)
      throws PortletRegistryException;
}
