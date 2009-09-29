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
package com.sun.portal.portletcontainer.driver.admin;

/**
 * AdminConstants contains keys that are in DesktopMessages.properties file.
 */
public interface AdminConstants {
  // Attributes used in the session
  public final String PORTLETS_ATTRIBUTE = "com.sun.portal.portletcontainer.driver.admin.portlets";
  public final String PORTLET_APPLICATIONS_ATTRIBUTE = "com.sun.portal.portletcontainer.driver.admin.portletApplications";
  public final String PORTLETS_LABEL = "com.sun.portal.portletcontainer.driver.admin.portletsLabel";
  public final String PORTLET_WINDOWS_ATTRIBUTE = "com.sun.portal.portletcontainer.driver.admin.portletWindows";
  public final String SHOW_WINDOW_ATTRIBUTE = "com.sun.portal.portletcontainer.driver.admin.showWindow";
  public final String HIDE_WINDOW_ATTRIBUTE = "com.sun.portal.portletcontainer.driver.admin.hideWindow";
  public final String THICK_WINDOW_ATTRIBUTE = "com.sun.portal.portletcontainer.driver.admin.thickWindow";
  public final String THIN_WINDOW_ATTRIBUTE = "com.sun.portal.portletcontainer.driver.admin.thinWindow";
  public final String CREATION_SUCCEEDED_ATTRIBUTE = "com.sun.portal.portletcontainer.driver.admin.creationSucceeded";
  public final String CREATION_FAILED_ATTRIBUTE = "com.sun.portal.portletcontainer.driver.admin.creationFailed";
  public final String DEPLOYMENT_SUCCEEDED_ATTRIBUTE = "com.sun.portal.portletcontainer.driver.admin.deploymentSucceeded";
  public final String DEPLOYMENT_FAILED_ATTRIBUTE = "com.sun.portal.portletcontainer.driver.admin.deploymentFailed";
  public final String UNDEPLOYMENT_SUCCEEDED_ATTRIBUTE = "com.sun.portal.portletcontainer.driver.admin.undeploymentSucceeded";
  public final String UNDEPLOYMENT_FAILED_ATTRIBUTE = "com.sun.portal.portletcontainer.driver.admin.undeploymentFailed";
  public final String MODIFY_SUCCEEDED_ATTRIBUTE = "com.sun.portal.portletcontainer.driver.admin.modifySucceeded";
  public final String MODIFY_FAILED_ATTRIBUTE = "com.sun.portal.portletcontainer.driver.admin.modifyFailed";
  public final String NO_WINDOW_DATA_ATTRIBUTE = "com.sun.portal.portletcontainer.driver.admin.noWindowData";
  public final String SELECTED_PORTLET_WINDOW_ATTRIBUTE = "com.sun.portal.portletcontainer.driver.admin.selectedPortletWindow";
  public final String CURRENT_SILVERPEAS_ELEMENT_ID = "com.silverpeas.portletcontainer.driver.admin.silverpeasElementId";
  public final String CURRENT_SILVERPEAS_SPACE_ID = "com.silverpeas.portletcontainer.driver.admin.silverpeasSpaceId";
  public final String CURRENT_SILVERPEAS_USER_ID = "com.silverpeas.portletcontainer.driver.admin.silverpeasUserId";

  public final String PORTLETS_TAB = "portlets";
  public final String ADMIN_TAB = "admin";
  public final String WSRP_TAB = "wsrp";

  public final String DEPLOY_PORTLET = "deploy";
  public final String UNDEPLOY_PORTLET = "undeploy";

  // Constants used in Undeploy
  public final String UNDEPLOY_PORTLET_SUBMIT = "UndeploySubmit";
  public final String PORTLETS_TO_UNDEPLOY = "portletsToUndeploy";

  // Constants used in Create Portlet Window
  public final String CREATE_PORTLET_WINDOW = "createPortletWindow";
  public final String CREATE_PORTLET_WINDOW_SUBMIT = "CreatePortletSubmit";
  public final String CREATE_PORTLET_WINDOW_SPACEID = "SpaceId";
  public final String PORTLET_WINDOW_NAME = "portletWindowName";
  public final String PORTLET_WINDOW_ROW = "portletWindowRow";
  public final String PORTLET_WINDOW_TITLE = "title";
  public final String PORTLET_LIST = "portletList";

  // Constants used in Modify Portlet Window
  public final String MODIFY_PORTLET_WINDOW = "modifyPortletWindow";
  public final String MODIFY_PORTLET_WINDOW_SUBMIT = "ModifyPortletSubmit";
  public final String PORTLET_WINDOW_LIST = "portletList";
  public final String WIDTH_LIST = "widthList";
  public final String VISIBLE_LIST = "visibleList";

  public final String MOVE_PORTLET_WINDOW = "movePortletWindow";

  public final String CREATION_SUCCEEDED = "portlets.admin.creationSucceeded";
  public final String CREATION_FAILED = "portlets.admin.creationFailed";
  public final String DEPLOYMENT_SUCCEEDED = "portlets.admin.deploymentSucceeded";
  public final String DEPLOYMENT_FAILED = "portlets.admin.deploymentFailed";
  public final String UNDEPLOYMENT_SUCCEEDED = "portlets.admin.undeploymentSucceeded";
  public final String UNDEPLOYMENT_FAILED = "portlets.admin.undeploymentFailed";
  public final String MODIFY_SUCCEEDED = "portlets.admin.modifySucceeded";
  public final String MODIFY_FAILED = "portlets.admin.modifyFailed";
  public final String NO_WINDOW_DATA = "portlets.admin.noWindowData";
  public final String WAR_NOT_DEPLOYED = "portlets.admin.warNotDeployed";
  public final String WAR_NOT_UNDEPLOYED = "portlets.admin.warNotUnDeployed";
  public final String INVALID_CHARACTERS = "portlets.admin.invalidCharacters";
  public final String NO_BASE_PORTLET = "portlets.admin.noBasePortlet";
  public final String INVALID_PORTLET_APP = "portlets.admin.invalidPortletApp";
  public final String NO_PORTLET_APP = "portlets.admin.noPortletApp";
  public final String NO_BASE_PORTLET_WINDOW = "portlets.admin.noBasePortletWindow";
  public final String PORTLET_WINDOW_NAME_ALREADY_EXISTS = "portlets.admin.portletWindowNameAlreadyExists";
}
