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

import java.net.URL;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.silverpeas.portlets.portal.portletwindow.PortletWindowInvoker;
import com.sun.portal.container.ChannelMode;
import com.sun.portal.container.ChannelState;
import com.sun.portal.portletcontainer.invoker.InvokerException;
import com.sun.portal.portletcontainer.invoker.ResponseProperties;
import com.sun.portal.portletcontainer.invoker.WindowInvoker;

/**
 * PortletContent is responsible for getting the portlet content and execting action
 * on the portlet. It delegates the calls to PortletWindowInvoker.
 */
public class PortletContent {
    
    private HttpServletRequest request;
    private HttpServletResponse response;
    private WindowInvoker windowInvoker;
    
    public PortletContent(ServletContext context, HttpServletRequest request,
            HttpServletResponse response) throws InvokerException {
        this.request = request;
        this.response = response;
        this.windowInvoker = getWindowInvoker(context, request, response);
    }
    
    public void setPortletWindowMode(ChannelMode portletWindowMode) {
        windowInvoker.setPortletWindowMode(portletWindowMode);
    }
    
    public void setPortletWindowName(String portletWindowName) {
        windowInvoker.setPortletWindowName(portletWindowName);
    }
    
    public void setPortletWindowState(ChannelState portletWindowState) {
        windowInvoker.setPortletWindowState(portletWindowState);
    }
    
    public StringBuffer getContent() throws InvokerException {
        return windowInvoker.render(request,response);
    }
    
    public String getTitle() throws InvokerException {
        return windowInvoker.getTitle();
    }
    
    public ResponseProperties getResponseProperties() {
        return windowInvoker.getResponseProperties();
    }
    
    public String getDefaultTitle() throws InvokerException {
        return windowInvoker.getDefaultTitle();
    }
    
    public URL executeAction() throws InvokerException {
        return windowInvoker.processAction(request,response);
    }
    
    public void getResources()
    throws InvokerException {
       windowInvoker.getResources(request, response);
    }
    
    protected WindowInvoker getWindowInvoker(ServletContext context, 
            HttpServletRequest request,
            HttpServletResponse response) 
            throws InvokerException  {
        WindowInvoker pwInvoker = new PortletWindowInvoker();
        pwInvoker.init(context, request, response); 
        return pwInvoker;
    }
}
