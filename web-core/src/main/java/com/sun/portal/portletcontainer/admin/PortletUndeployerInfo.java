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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sun.portal.portletcontainer.context.registry.PortletRegistryException;

/**
 * PortletUndeployerInfo has the list of the portlets that are undeployed.
 */
public class PortletUndeployerInfo {

  private static final String UNDEPLOY_PORTLET_FILE = ".undeploy-portlet";
  private String undeployPortletFilename;

  // Create a logger for this class
  private static Logger logger = Logger.getLogger(
      "com.sun.portal.portletcontainer.admin",
      "com.silverpeas.portlets.PALogMessages");

  public PortletUndeployerInfo() throws PortletRegistryException {
    undeployPortletFilename = PortletRegistryHelper.getRegistryLocation()
        + File.separator + UNDEPLOY_PORTLET_FILE;
  }

  public List read() {
    List list = new ArrayList();
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
