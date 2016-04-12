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

import java.io.File;
import java.util.List;

import org.w3c.dom.Document;

import org.silverpeas.core.util.StringUtil;
import com.sun.portal.portletcontainer.context.registry.PortletRegistryException;

/**
 * PortletRegistryWriter is responsible for writing to the Registry xmls. There will be concrete
 * classes to write to portlet-app-registry.xml portlet-window-registry.xml
 * portlet-window-preference-registry.xml.
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
   * public PortletRegistryWriter(String registryLocation, String filename) { file = new
   * File(registryLocation + File.separator + filename); this.registryLocation = registryLocation; }
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
   * Appends contents in the list to the specified registry xml file in the file system.
   * @param portletRegistryElementList a <code>List</code> of Portlet registry elements
   */
  public abstract void appendDocument(List<PortletRegistryElement> portletRegistryElementList)
      throws PortletRegistryException;

  /**
   * Writes the contents in the list to the specified registry xml file in the file system.
   * @param portletRegistryElementList a <code>List</code> of Portlet registry elements
   */
  public abstract void writeDocument(List<PortletRegistryElement> portletRegistryElementList)
      throws PortletRegistryException;
}
