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

package com.silverpeas.portlets.portal;

import javax.servlet.http.HttpServletRequest;

import com.sun.portal.portletcontainer.context.registry.PortletRegistryContext;
import com.sun.portal.portletcontainer.context.registry.PortletRegistryException;

/**
 * The PortletWindowData is responsible for providing the
 * data related to the Portlet Window to the view. 
 * The information includes title, portlet content, view ,edit and help URLs.
 */
public interface PortletWindowData {
    
    public void init(HttpServletRequest request, PortletRegistryContext portletRegistryContext,
            String portletWindowName) throws PortletRegistryException;
    public String getPortletName();
    public String getPortletWindowName();
    public String getTitle();
    public StringBuffer getContent();
    public boolean isView();
    public String getViewURL();
    public boolean isEdit();
    public String getEditURL();
    public boolean isHelp();
    public String getHelpURL();
    public boolean isNormalized();
    public String getNormalizedURL();
    public boolean isMaximized();
    public String getMaximizedURL();
    public boolean isMinimized();
    public String getMinimizedURL();
    public String getCurrentMode();
    public String getCurrentWindowState();
    public boolean isRemove();
    public String getRemoveURL();
    public boolean isThin();
    public boolean isThick();
    public String getWidth();
    public Integer getRowNumber();
    public String getSpaceId();   
    public void setSpaceId(String spaceId);
}
