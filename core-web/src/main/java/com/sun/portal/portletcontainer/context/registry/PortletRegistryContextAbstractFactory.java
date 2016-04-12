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
package com.sun.portal.portletcontainer.context.registry;

import com.sun.portal.container.service.ServiceFinder;

/**
 * PortletRegistryContextAbstractFactory is responsible to creating the
 * PortletRegistryContextFactory object.
 */
public class PortletRegistryContextAbstractFactory {

  private PortletRegistryContextFactory portletRegistryContextFactory;

  public PortletRegistryContextAbstractFactory() {
  }

  public PortletRegistryContextFactory getPortletRegistryContextFactory()
      throws PortletRegistryException {
    if (portletRegistryContextFactory == null) {
      Object instance = null;
      try {
        instance = ServiceFinder
            .getServiceImplementationInstance(PortletRegistryContextAbstractFactory.class
            .getName());
      } catch (Exception ex) {
        throw new PortletRegistryException(ex);
      }
      portletRegistryContextFactory = (PortletRegistryContextFactory) instance;
    }
    return portletRegistryContextFactory;
  }

}
