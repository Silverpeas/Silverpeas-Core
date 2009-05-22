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


package com.silverpeas.portlets.portal.portletwindow;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import com.sun.portal.container.ChannelMode;
import com.sun.portal.container.ChannelState;
import com.sun.portal.container.ChannelURLType;
import com.sun.portal.container.WindowRequestReader;
import com.sun.portal.portletcontainer.invoker.WindowInvokerConstants;

public class PortletWindowRequestReader implements WindowRequestReader {
    
    public ChannelMode readNewPortletWindowMode(HttpServletRequest request) {
        String newChannelMode =
                request.getParameter(WindowInvokerConstants.NEW_PORTLET_WINDOW_MODE_KEY);
        if ( newChannelMode != null) {
            return new ChannelMode(newChannelMode);
        }
        return null;
    }
    
    public ChannelState readNewWindowState(HttpServletRequest request){
        String newWindowState =
                request.getParameter(WindowInvokerConstants.NEW_PORTLET_WINDOW_STATE_KEY);
        if ( newWindowState != null) {
            return new ChannelState(newWindowState);
        }
        return null;
    }
    
    public ChannelURLType readURLType(HttpServletRequest request) {
        return new ChannelURLType(request.getParameter(WindowInvokerConstants.PORTLET_ACTION));
    }
    
    
    public Map<String, String[]> readParameterMap(HttpServletRequest request) {
        Map<String, String[]> params = (Map)request.getAttribute(WindowInvokerConstants.PORTLET_PARAM_MAP);
        return params;
    }

    public String getCacheLevel(HttpServletRequest request) {
        return request.getParameter(WindowInvokerConstants.RESOURCE_URL_CACHE_LEVEL_KEY);
    }

    public String getResourceID(HttpServletRequest request) {
        return request.getParameter(WindowInvokerConstants.RESOURCE_ID_KEY);
    }
}
