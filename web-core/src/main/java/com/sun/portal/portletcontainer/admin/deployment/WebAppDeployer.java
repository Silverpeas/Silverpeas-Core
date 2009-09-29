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

/**
 * An interface which defines methods to deploy and undeploy Portlet war files
 * on a Web Container. The realizations of this inteface will provide Container
 * specific implementations for deploying and undeploying on the particular web
 * containers.
 */
public interface WebAppDeployer {
  public static String CONFIG_FILE = "pcenv.conf";
  public static String WAR_NOT_DEPLOYED = "warNotDeployed";
  public static String WAR_NOT_UNDEPLOYED = "warNotUndeployed";

  /**
   * Deploys the Portlet war on the web container.
   * 
   * @param warFileName
   *          The complete path to the Portlet war file.
   * @return boolean Returns true if the deployment is successful.
   */
  public boolean deploy(String warFileName) throws WebAppDeployerException;

  /**
   * Undeploys the Portlet war from the web container.
   * 
   * @param warFileName
   *          The name of the Portlet war file.
   * @return boolean Returns true if the undeployment is successful.
   */
  public boolean undeploy(String warFileName) throws WebAppDeployerException;
}
