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

import java.util.Properties;

/**
 * PortletAdminMBean provides methods for deploying and undeploying the portlet.
 */
public interface PortletAdminMBean {
  public static final String TYPE = "Portal.PortletAdmin";

  /**
   * Performs the following three operations.. 1. Preparing the portlet
   * webapplication 2. Registering the portlet with the portlet driver 3.
   * Deploying the portlet webapplication in the webcontainer
   * 
   * @param warFileName
   *          the portlet webapplication
   * @param roles
   *          the roles the user is in
   * @param userinfo
   *          the user information for the user
   * @param deployToContainer
   *          true if the application is to be deployed to the webcontainer
   * 
   * @return true if the deployment is successful.
   */
  public Boolean deploy(String warFileName, Properties roles,
      Properties userinfo, boolean deployToContainer) throws Exception;

  /**
   * Performs the following two operations.. 1. Unregistering the portlet from
   * the portlet driver 2. Undeploying the portlet webapplication from the
   * webcontainer
   * 
   * @param warFileName
   *          the portlet webapplication
   * @param undeployFromContainer
   *          true if the application is to be undeployed from the webcontainer
   * 
   * @return true if the undeployment is successful.
   */
  public Boolean undeploy(String warName, boolean undeployFromContainer)
      throws Exception;

}
