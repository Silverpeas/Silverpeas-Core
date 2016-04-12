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

import com.sun.portal.portletcontainer.context.registry.PortletRegistryContext;
import com.sun.portal.portletcontainer.context.registry.PortletRegistryContextAbstractFactory;
import com.sun.portal.portletcontainer.context.registry.PortletRegistryContextFactory;
import com.sun.portal.portletcontainer.context.registry.PortletRegistryException;

/**
 * PortletAdminDataFactory is a factory object that creates and returns a new instance of the
 * PortletAdminData.
 */
public class PortletAdminDataFactory {

  /**
   * Returns a new instance of the PortletAdminData object
   */

  public static PortletAdminData getPortletAdminData(String context)
      throws PortletRegistryException {
    PortletAdminData portletAdminData = new PortletAdminDataImpl();
    PortletRegistryContextAbstractFactory afactory = new PortletRegistryContextAbstractFactory();
    PortletRegistryContextFactory factory = afactory
        .getPortletRegistryContextFactory();
    PortletRegistryContext portletRegistryContext = factory
        .getPortletRegistryContext(context);
    portletAdminData.init(portletRegistryContext);
    return portletAdminData;
  }
}
