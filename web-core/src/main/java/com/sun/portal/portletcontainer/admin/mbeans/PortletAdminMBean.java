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
package com.sun.portal.portletcontainer.admin.mbeans;

import java.util.Properties;

/**
 * PortletAdminMBean provides methods for deploying and undeploying the portlet.
 */
public interface PortletAdminMBean {
  public static final String TYPE = "Portal.PortletAdmin";

  /**
   * Performs the following three operations.. 1. Preparing the portlet webapplication 2.
   * Registering the portlet with the portlet driver 3. Deploying the portlet webapplication in the
   * webcontainer
   * @param warFileName the portlet webapplication
   * @param roles the roles the user is in
   * @param userinfo the user information for the user
   * @param deployToContainer true if the application is to be deployed to the webcontainer
   * @return true if the deployment is successful.
   */
  public Boolean deploy(String warFileName, Properties roles,
      Properties userinfo, boolean deployToContainer) throws Exception;

  /**
   * Performs the following two operations.. 1. Unregistering the portlet from the portlet driver 2.
   * Undeploying the portlet webapplication from the webcontainer
   * @param warName the portlet web application
   * @param undeployFromContainer true if the application is to be undeployed from the webcontainer
   * @return true if the undeployment is successful.
   */
  public Boolean undeploy(String warName, boolean undeployFromContainer)
      throws Exception;

}
