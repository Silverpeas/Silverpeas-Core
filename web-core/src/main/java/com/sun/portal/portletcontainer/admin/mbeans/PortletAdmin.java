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
package com.sun.portal.portletcontainer.admin.mbeans;

import java.io.File;
import java.util.Properties;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sun.portal.container.PortletLang;
import com.sun.portal.portletcontainer.admin.PortletRegistryGenerator;
import com.sun.portal.portletcontainer.admin.PortletRegistryHelper;
import com.sun.portal.portletcontainer.admin.PortletUndeployerInfo;
import com.sun.portal.portletcontainer.admin.deployment.WebAppDeployer;
import com.sun.portal.portletcontainer.admin.deployment.WebAppDeployerException;
import com.sun.portal.portletcontainer.admin.deployment.WebAppDeployerFactory;
import com.sun.portal.portletcontainer.context.registry.PortletRegistryException;
import com.sun.portal.portletcontainer.warupdater.PortletWarUpdater;
import com.sun.portal.portletcontainer.warupdater.PortletWarUpdaterUtil;

/**
 * PortletAdmin class is a concrete implementation of PortletAdminMBean
 */
public class PortletAdmin implements PortletAdminMBean {

  // Create a logger for this class
  private static Logger logger = Logger.getLogger(PortletAdmin.class
      .getPackage().getName(), "com.silverpeas.portlets.PALogMessages");

  public PortletAdmin() {
  }

  private File preparePortlet(String warFileName) throws Exception {
    logger.log(Level.INFO, "PSPL_CSPPAM0031", warFileName);
    String warFileLocation = PortletRegistryHelper.getWarFileLocation();
    String configFileLocation = PortletRegistryHelper.getConfigFileLocation();
    // Create the updated war file in the war file location
    PortletWarUpdater portletWarUpdater = new PortletWarUpdater(
        configFileLocation, warFileLocation);

    // Get the updated war file
    return portletWarUpdater.preparePortlet(warFileName);
  }

  private Boolean registerPortlet(String warFile, Properties roles,
      Properties userinfo, File preparedWarFile) throws Exception {
    String warName = PortletWarUpdaterUtil.getWarName(warFile);
    String warFileLocation = PortletRegistryHelper.getWarFileLocation();
    String destFile = warFileLocation + File.separator + warName;
    logger.log(Level.FINE, "PSPL_CSPPAM0001", destFile);
    PortletRegistryGenerator portletRegistryGenerator = new PortletRegistryGenerator();
    JarFile jarFile = new JarFile(warFile);
    Manifest manifest = jarFile.getManifest();
    PortletLang portletLanguage = PortletLang.JAVA;
    if (manifest != null) {
      Attributes attributes = manifest.getMainAttributes();
      if (attributes != null) {
        String typeOfPortlet = attributes.getValue("Portlet-Type");
        if (typeOfPortlet != null && typeOfPortlet.equalsIgnoreCase("ror")) {
          portletLanguage = PortletLang.ROR;
        }
      }
    }
    logger.log(Level.FINE, "PSPL_CSPPAM0033", portletLanguage.toString());
    portletRegistryGenerator.register(preparedWarFile, warFileLocation, roles,
        userinfo, portletLanguage);
    logger.log(Level.FINE, "PSPL_CSPPAM0004", warName);
    return Boolean.TRUE;
  }

  public Boolean deploy(String warFileName, Properties roles,
      Properties userinfo, boolean deployToContainer) throws Exception {

    File preparedWarFile = preparePortlet(warFileName);

    Boolean registerSuccess = registerPortlet(warFileName, roles, userinfo,
        preparedWarFile);

    if (registerSuccess.booleanValue()) {
      if (deployToContainer) {
        WebAppDeployer webAppDeployer = WebAppDeployerFactory.getInstance()
            .getDeploymentManager();
        if (webAppDeployer != null) {
          String warName = PortletWarUpdaterUtil.getWarName(warFileName);
          webAppDeployer.deploy(warName);
        } else {
          throw new WebAppDeployerException("No WebAppDeployer Found");
        }
      }
      return Boolean.TRUE;
    }
    return Boolean.FALSE;
  }

  public Boolean unregisterPortlet(String warFileName) throws Exception {
    logger.log(Level.INFO, "PSPL_CSPPAM0032", warFileName);
    String warFileLocation = PortletRegistryHelper.getWarFileLocation();
    String configFileLocation = PortletRegistryHelper.getConfigFileLocation();
    PortletRegistryGenerator portletRegistryGenerator = new PortletRegistryGenerator();
    String warName = PortletWarUpdaterUtil.getWarName(warFileName);
    logger.log(Level.FINE, "PSPL_CSPPAM0012", warName);
    Boolean value = portletRegistryGenerator.unregister(configFileLocation,
        warFileLocation, warName);
    if (value.booleanValue()) {
      PortletUndeployerInfo portletUndeployerInfo;
      try {
        portletUndeployerInfo = new PortletUndeployerInfo();
        portletUndeployerInfo.write(warName);
      } catch (PortletRegistryException pre) {
        logger.log(Level.WARNING, "PSPL_CSPPAM0028", pre);
      }
      // remove the portlet war created by PortletWarUpdater
      boolean remove = deletePortlet(warName, warFileLocation);
      // Remove the portlet war and portlet xml created in pc.home/war directory
      portletRegistryGenerator.removePortletWar(warFileLocation, warName);
      return Boolean.TRUE;
    } else {
      return Boolean.FALSE;
    }
  }

  public Boolean undeploy(String warFileName, boolean undeployFromContainer)
      throws Exception {
    Boolean value = unregisterPortlet(warFileName);
    if (value.booleanValue()) {
      if (undeployFromContainer) {
        WebAppDeployer webAppDeployer = WebAppDeployerFactory.getInstance()
            .getDeploymentManager();
        if (webAppDeployer != null) {
          String warName = PortletWarUpdaterUtil.getWarName(warFileName);
          boolean success = webAppDeployer.undeploy(warName);
          if (!success) {
            value = Boolean.FALSE;
            throw new WebAppDeployerException("Cannot undeploy");
          }
        } else {
          value = Boolean.FALSE;
          logger.log(Level.WARNING, "PSPL_CSPPAM0026");
          throw new WebAppDeployerException("No WebAppDeployer Found");
        }
      }
      value = Boolean.TRUE;
    }
    return value;
  }

  public void copyFile(String sourceFile, String destFile) throws Exception {
    PortletWarUpdaterUtil.copyFile(sourceFile, destFile);
  }

  /**
   * Deletes the portlet application from the stored location
   * 
   * @param warNameOnly
   *          name of the portlet application (without extension)
   * @param warFileLocation
   *          deployed location of the portlet
   * 
   * @return true if the deletion is successful.
   */
  public boolean deletePortlet(String warNameOnly, String warFileLocation) {
    String warName = warNameOnly + ".war";
    File destFile = new File(warFileLocation, warName);
    boolean remove = destFile.delete();
    if (logger.isLoggable(Level.FINEST)) {
      logger.log(Level.FINEST, "PSPL_CSPPCWU0011", new String[] {
          destFile.getAbsolutePath(), String.valueOf(remove) });
    }
    return remove;
  }

}
