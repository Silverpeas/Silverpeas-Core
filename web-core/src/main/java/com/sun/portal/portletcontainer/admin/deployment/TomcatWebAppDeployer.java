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
package com.sun.portal.portletcontainer.admin.deployment;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sun.portal.portletcontainer.admin.PortletRegistryHelper;
import com.sun.portal.portletcontainer.context.registry.PortletRegistryException;
import com.sun.portal.portletcontainer.warupdater.PortletWarUpdaterUtil;

/**
 * The class which implements the WebAppDeployer interface to provide the
 * deployment and undeployment functionality for Portlets on Tomcat container.
 */
public class TomcatWebAppDeployer implements WebAppDeployer {

  private static Logger logger = Logger.getLogger(TomcatWebAppDeployer.class
      .getPackage().getName(), "com.silverpeas.portlets.PALogMessages");
  private static final String TOMCAT_WEBAPPS_DIR = "TOMCAT_WEBAPPS_DIR";
  private String autoDeployDirectory;

  /**
   * Initialize the autoDeployDirectory by reading the configuration data from
   * the config file.
   */
  public TomcatWebAppDeployer() throws Exception {
    String portletContainerConfigDir = PortletRegistryHelper
        .getConfigFileLocation();
    if (portletContainerConfigDir != null) {
      FileInputStream config = null;
      String configFileName = portletContainerConfigDir + File.separator
          + CONFIG_FILE;
      try {
        config = new FileInputStream(configFileName);
        Properties properties = new Properties();
        properties.load(config);
        autoDeployDirectory = properties.getProperty(TOMCAT_WEBAPPS_DIR);
      } catch (Exception e) {
        logger.log(Level.SEVERE, "PSPL_CSPPAM0025", e);
        throw e;
      } finally {
        try {
          if (config != null) {
            config.close();
          }
        } catch (Exception ignored) {
        }
      }
    }
  }

  /**
   * Provides the implementation of deploying Portlet war on Tomcat.
   * 
   * @param warFileName
   *          The complete path to the Portlet war file.
   * @return boolean Returns true if the deployment is successful.
   */
  public boolean deploy(String warFileName) throws WebAppDeployerException {
    boolean success = false;
    String warFileLocation = null;
    try {
      warFileLocation = PortletRegistryHelper.getWarFileLocation();
    } catch (PortletRegistryException pre) {
    }

    String warName = PortletWarUpdaterUtil.getWarName(warFileName);
    // Copy in auto-deploy directory
    try {
      if (autoDeployDirectory != null) {
        String warFile = warFileLocation + File.separator + warName;
        WebAppDeployerUtil.copyFile(warFile, autoDeployDirectory
            + File.separator + warName);
        if (logger.isLoggable(Level.INFO)) {
          logger.log(Level.INFO, "PSPL_CSPPAM0020", new String[] { warFile,
              autoDeployDirectory });
        }
        success = true;
      }
    } catch (IOException ioe) {
      logger.log(Level.WARNING, "PSPL_CSPPAM0021", ioe);
      throw new WebAppDeployerException(ioe.getMessage(), ioe);
    }
    return success;
  }

  /**
   * Provides the implementation of undeploying Portlet war from Tomcat.
   * 
   * @param warFileName
   *          The complete path to the Portlet war file.
   * @return boolean Returns true if the deployment is successful.
   */
  public boolean undeploy(String warFileName) throws WebAppDeployerException {
    boolean success = false;
    try {
      if (autoDeployDirectory != null) {
        int index = warFileName.indexOf(".war");
        if (index == -1) {
          warFileName = warFileName + ".war";
        }
        String warFile = autoDeployDirectory + File.separator + warFileName;
        File file = new File(warFile);
        success = file.delete();
        if (logger.isLoggable(Level.INFO)) {
          logger.log(Level.INFO, "PSPL_CSPPAM0022", new String[] { warFile,
              String.valueOf(success) });
        }
      }
    } catch (Exception e) {
      logger.log(Level.WARNING, "PSPL_CSPPAM0023", e);
      throw new WebAppDeployerException(e.getMessage(), e);
    }
    return success;
  }
}