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

import java.util.List;

import org.silverpeas.core.web.portlets.portal.PortletAppData;
import org.silverpeas.core.web.portlets.portal.PortletWindowData;
import com.sun.portal.portletcontainer.context.registry.PortletRegistryContext;
import com.sun.portal.portletcontainer.context.registry.PortletRegistryException;

/**
 * The PortletAdminData is responsible for administering portlets. The includes
 * deploying/undeploying portlets, creating/modifying portlet windows.
 */
public interface PortletAdminData {
  public void init(PortletRegistryContext portletRegistryContext) throws PortletRegistryException;

  public boolean deploy(String warName, boolean deployToContainer) throws Exception;

  public boolean deploy(String warName, String rolesFilename, String userInfoFilename,
      boolean deployToContainer) throws Exception;

  public boolean undeploy(String warName, boolean undeployFromContainer) throws Exception;

  public List<PortletAppData> getPortlets(String locale);

  public List<String> getPortletNames();

  public List<String> getPortletDisplayNames(String locale);

  public List<String> getPortletApplicationNames();

  public List<String> getPortletWindowNames();

  public boolean createPortletWindow(String portletName, String portletWindowName, String title)
      throws Exception;

  public boolean modifyPortletWindow(String portletWindowName, String width, boolean visible,
      String row) throws Exception;

  public boolean movePortletWindows(List<PortletWindowData> portletWindows) throws Exception;

  public boolean isVisible(String portletWindowName) throws Exception;

  public String getWidth(String portletWindowName) throws Exception;
}
