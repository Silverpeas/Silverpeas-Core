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

import org.w3c.dom.Document;

import com.silverpeas.util.StringUtil;
import com.sun.portal.portletcontainer.context.registry.PortletRegistryException;

/**
 * PortletRegistryReader reads the specified registry xml file into a DOM
 * Document. The registry xml files can be portlet-app-registry.xml
 * portlet-window-registry.xml portlet-window-preference-registry.xml.
 * 
 */
public abstract class PortletRegistryReader {

  private File file;

  public PortletRegistryReader(String registryLocation, String filename,
      String context) {
    if (!StringUtil.isDefined(context))
      context = "";
    else
      context = context + "-";
    file = new File(registryLocation + File.separator + context + filename);

    if (!file.exists())
      file = new File(registryLocation + File.separator + filename);
  }

  public PortletRegistryReader(String registryLocation, String filename) {
    file = new File(registryLocation + File.separator + filename);
  }

  private Document getDocument() throws PortletRegistryException {
    if (file.exists()) {
      return PortletRegistryHelper.readFile(file);
    }
    return null;
  }

  /**
   * Reads the specified registry xml file in to the appropriate Portlet
   * Registry Object.
   * 
   * @return a <code>PortletRegistryObject</code>, that represents the registry
   *         xml file.
   */
  public PortletRegistryObject readDocument() throws PortletRegistryException {
    PortletRegistryObject portletRegistryObject = create();
    portletRegistryObject.read(getDocument());
    return portletRegistryObject;
  }

  /**
   * Creates specific Portlet Registry Object.
   * 
   * @return a specific <code>PortletRegistryObject</code>.
   */
  public abstract PortletRegistryObject create();
}
