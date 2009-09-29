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
import java.util.logging.Logger;

import com.sun.portal.portletcontainer.admin.PortletRegistryHelper;

/**
 * Factory class to provide access to the WebAppDeployer implementation class.
 * This is a singleton class which reads the configuration file to obtain the
 * name of the class which implements WebAppDeployer interface and loads the
 * class, creates an instance of that class, and returns the same for the
 * callers.
 */
public class WebAppDeployerFactory {
  private static final WebAppDeployerFactory factory = new WebAppDeployerFactory();
  private WebAppDeployer manager = null;

  private static final String DEPLOYMENT_MANAGER_CLASS = "deployment.manager.class";
  private static final String DEFAULT_DEPLOYMENT_MANAGER_CLASS = "com.sun.portal.portletadmin.deployment.DefaultWebAppDeployer";
  private static Logger logger = Logger.getLogger(WebAppDeployerFactory.class
      .getPackage().getName(), "com.silverpeas.portlets.PALogMessages");

  /**
   * Reads the configuration file to intialize the manager with the appropriate
   * WebAppDeployer implementation class.
   */
  private WebAppDeployerFactory() {
    FileInputStream config = null;
    try {
      String portletContainerConfigDir = PortletRegistryHelper
          .getConfigFileLocation();
      if (portletContainerConfigDir != null) {
        String configFileName = portletContainerConfigDir + File.separator
            + WebAppDeployer.CONFIG_FILE;
        config = new FileInputStream(configFileName);
        Properties properties = new Properties();
        properties.load(config);
        String deploymentManagerClass = properties
            .getProperty(DEPLOYMENT_MANAGER_CLASS);
        manager = (WebAppDeployer) Class.forName(deploymentManagerClass)
            .newInstance();
      }
    } catch (Throwable t) {
      System.out.println("Exception: " + t.toString()
          + ". Using DefaultWebAppDeployer");
      try {
        manager = (WebAppDeployer) Class.forName(
            DEFAULT_DEPLOYMENT_MANAGER_CLASS).newInstance();
      } catch (Throwable t1) {
        System.out.println("Exception initializing DefaultWebAppDeployer: "
            + t1.toString());
      }
    } finally {
      if (config != null) {
        try {
          config.close();
        } catch (IOException ex) {
          // ignore
        }
      }
    }
  }

  /**
   * Returns the singleton instance of the Factory class.
   * 
   * @return WebAppDeployerFactory The singleton instance of this Factory class.
   */
  public static WebAppDeployerFactory getInstance() {
    return factory;
  }

  /**
   * Returns the WebAppDeployer implementation instance as provided by the
   * configuration.
   * 
   * @return WebAppDeployer The instance of the class implementing
   *         WebAppDeployer.
   */
  public WebAppDeployer getDeploymentManager() {
    return manager;
  }

}
