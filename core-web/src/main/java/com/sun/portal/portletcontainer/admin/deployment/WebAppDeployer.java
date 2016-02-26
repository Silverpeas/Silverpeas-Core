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

/**
 * An interface which defines methods to deploy and undeploy Portlet war files on a Web Container.
 * The realizations of this inteface will provide Container specific implementations for deploying
 * and undeploying on the particular web containers.
 */
public interface WebAppDeployer {
  public static String CONFIG_FILE = "pcenv.conf";
  public static String WAR_NOT_DEPLOYED = "warNotDeployed";
  public static String WAR_NOT_UNDEPLOYED = "warNotUndeployed";

  /**
   * Deploys the Portlet war on the web container.
   * @param warFileName The complete path to the Portlet war file.
   * @return boolean Returns true if the deployment is successful.
   */
  public boolean deploy(String warFileName) throws WebAppDeployerException;

  /**
   * Undeploys the Portlet war from the web container.
   * @param warFileName The name of the Portlet war file.
   * @return boolean Returns true if the undeployment is successful.
   */
  public boolean undeploy(String warFileName) throws WebAppDeployerException;
}
