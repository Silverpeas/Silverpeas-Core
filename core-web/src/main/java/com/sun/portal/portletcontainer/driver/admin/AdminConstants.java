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
package com.sun.portal.portletcontainer.driver.admin;

/**
 * AdminConstants contains keys that are in DesktopMessages.properties file.
 */
public interface AdminConstants {
  // Attributes used in the session
  public final String PORTLETS_ATTRIBUTE = "com.sun.portal.portletcontainer.driver.admin.portlets";
  public final String PORTLET_APPLICATIONS_ATTRIBUTE =
      "com.sun.portal.portletcontainer.driver.admin.portletApplications";
  public final String PORTLETS_LABEL = "com.sun.portal.portletcontainer.driver.admin.portletsLabel";
  public final String PORTLET_WINDOWS_ATTRIBUTE =
      "com.sun.portal.portletcontainer.driver.admin.portletWindows";
  public final String SHOW_WINDOW_ATTRIBUTE =
      "com.sun.portal.portletcontainer.driver.admin.showWindow";
  public final String HIDE_WINDOW_ATTRIBUTE =
      "com.sun.portal.portletcontainer.driver.admin.hideWindow";
  public final String THICK_WINDOW_ATTRIBUTE =
      "com.sun.portal.portletcontainer.driver.admin.thickWindow";
  public final String THIN_WINDOW_ATTRIBUTE =
      "com.sun.portal.portletcontainer.driver.admin.thinWindow";
  public final String CREATION_SUCCEEDED_ATTRIBUTE =
      "com.sun.portal.portletcontainer.driver.admin.creationSucceeded";
  public final String CREATION_FAILED_ATTRIBUTE =
      "com.sun.portal.portletcontainer.driver.admin.creationFailed";
  public final String DEPLOYMENT_SUCCEEDED_ATTRIBUTE =
      "com.sun.portal.portletcontainer.driver.admin.deploymentSucceeded";
  public final String DEPLOYMENT_FAILED_ATTRIBUTE =
      "com.sun.portal.portletcontainer.driver.admin.deploymentFailed";
  public final String UNDEPLOYMENT_SUCCEEDED_ATTRIBUTE =
      "com.sun.portal.portletcontainer.driver.admin.undeploymentSucceeded";
  public final String UNDEPLOYMENT_FAILED_ATTRIBUTE =
      "com.sun.portal.portletcontainer.driver.admin.undeploymentFailed";
  public final String MODIFY_SUCCEEDED_ATTRIBUTE =
      "com.sun.portal.portletcontainer.driver.admin.modifySucceeded";
  public final String MODIFY_FAILED_ATTRIBUTE =
      "com.sun.portal.portletcontainer.driver.admin.modifyFailed";
  public final String NO_WINDOW_DATA_ATTRIBUTE =
      "com.sun.portal.portletcontainer.driver.admin.noWindowData";
  public final String SELECTED_PORTLET_WINDOW_ATTRIBUTE =
      "com.sun.portal.portletcontainer.driver.admin.selectedPortletWindow";
  public final String CURRENT_SILVERPEAS_ELEMENT_ID =
      "com.silverpeas.portletcontainer.driver.admin.silverpeasElementId";
  public final String CURRENT_SILVERPEAS_SPACE_ID =
      "com.silverpeas.portletcontainer.driver.admin.silverpeasSpaceId";
  public final String CURRENT_SILVERPEAS_USER_ID =
      "com.silverpeas.portletcontainer.driver.admin.silverpeasUserId";

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
  public final String PORTLET_WINDOW_NAME_ALREADY_EXISTS =
      "portlets.admin.portletWindowNameAlreadyExists";
}
