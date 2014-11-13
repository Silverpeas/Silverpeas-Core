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

import com.sun.portal.portletcontainer.context.registry.PortletRegistryException;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * PortletUndeployerInfo has the list of the portlets that are undeployed.
 */
public class PortletUndeployerInfo {

  private static final String UNDEPLOY_PORTLET_FILE = ".undeploy-portlet";
  private String undeployPortletFilename;

  // Create a logger for this class
  private static Logger logger = Logger.getLogger("com.sun.portal.portletcontainer.admin",
      "org.silverpeas.portlets.PALogMessages");

  public PortletUndeployerInfo() throws PortletRegistryException {
    undeployPortletFilename = PortletRegistryHelper.getRegistryLocation()
        + File.separator + UNDEPLOY_PORTLET_FILE;
  }

  public List<String> read() {
    List<String> list = new ArrayList<>();
    BufferedReader reader = null;
    File file = null;
    try {
      file = new File(undeployPortletFilename);
      reader = new BufferedReader(new FileReader(file));
      String line;
      while ((line = reader.readLine()) != null) {
        list.add(line);
      }
    } catch (Exception e) {
      logger.log(Level.FINEST, "PSPL_CSPPAM0029", e.getMessage());
    } finally {
      if (reader != null) {
        try {
          reader.close();
        } catch (IOException ignored) {
        }
      }
      if (file != null) {
        file.delete();
      }
    }
    return list;
  }

  public void write(String undeployedPortletWar) {
    PrintWriter writer = null;
    try {
      writer = new PrintWriter(new FileWriter(undeployPortletFilename, true));
      writer.println(undeployedPortletWar);
    } catch (Exception e) {
      logger.log(Level.WARNING, "PSPL_CSPPAM0030", e);
    } finally {
      if (writer != null) {
        writer.close();
      }
    }
  }
}
