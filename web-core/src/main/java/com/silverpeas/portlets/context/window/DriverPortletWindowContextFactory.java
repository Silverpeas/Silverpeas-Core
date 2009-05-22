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

package com.silverpeas.portlets.context.window;

import javax.servlet.http.HttpServletRequest;

import com.silverpeas.portlets.context.window.impl.PortletWindowContextImpl;
import com.sun.portal.container.PortletWindowContext;
import com.sun.portal.container.PortletWindowContextException;
import com.sun.portal.container.PortletWindowContextFactory;

/**
 * DriverPortletWindowContextFactory provides the implementation of the abstract methods
 * of PortletWindowContextFactory.
 *
 */
public class DriverPortletWindowContextFactory implements PortletWindowContextFactory {
    
    private PortletWindowContext portletWindowContext;
    
    public DriverPortletWindowContextFactory() {
    }
    
    public PortletWindowContext getPortletWindowContext(HttpServletRequest request) 
                        throws PortletWindowContextException {
        if(portletWindowContext == null) {
            portletWindowContext = new PortletWindowContextImpl();
            portletWindowContext.init(request);
        }
        return portletWindowContext;
    }

    public PortletWindowContext getPortletWindowContext(HttpServletRequest request, String userID)
                        throws PortletWindowContextException {
        if(portletWindowContext == null) {
            portletWindowContext = new PortletWindowContextImpl(userID);
            portletWindowContext.init(request);
        }
        return portletWindowContext;
    }
    
}
