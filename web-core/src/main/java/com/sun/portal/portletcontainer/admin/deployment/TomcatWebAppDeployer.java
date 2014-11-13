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
package com.sun.portal.portletcontainer.admin.deployment;

import com.sun.portal.portletcontainer.admin.PortletRegistryHelper;
import com.sun.portal.portletcontainer.context.registry.PortletRegistryException;
import com.sun.portal.portletcontainer.warupdater.PortletWarUpdaterUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The class which implements the WebAppDeployer interface to provide the deployment and
 * undeployment functionality for Portlets on Tomcat container.
 */
public class TomcatWebAppDeployer implements WebAppDeployer {

  private static Logger logger = Logger.getLogger(TomcatWebAppDeployer.class
      .getPackage().getName(), "org.silverpeas.portlets.PALogMessages");
  private static final String TOMCAT_WEBAPPS_DIR = "TOMCAT_WEBAPPS_DIR";
  private String autoDeployDirectory;

  /**
   * Initialize the autoDeployDirectory by reading the configuration data from the config file.
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
   * @param warFileName The complete path to the Portlet war file.
   * @return boolean Returns true if the deployment is successful.
   */
  @Override
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
   * @param warFileName The complete path to the Portlet war file.
   * @return boolean Returns true if the deployment is successful.
   */
  @Override
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