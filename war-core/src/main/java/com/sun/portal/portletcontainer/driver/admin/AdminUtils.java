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
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import com.sun.portal.portletcontainer.admin.registry.PortletRegistryConstants;
import com.sun.portal.portletcontainer.context.registry.PortletRegistryException;

/**
 * AdminUtils is a utility class for admin UI related tasks
 */
public class AdminUtils {
    
    private static Logger logger = Logger.getLogger(AdminUtils.class.getPackage().getName(), "com.silverpeas.portlets.PCDLogMessages");
    
    protected static HttpSession getClearedSession(HttpServletRequest request) {
        HttpSession session = request.getSession(true);
        session.removeAttribute(AdminConstants.PORTLETS_ATTRIBUTE);
        session.removeAttribute(AdminConstants.PORTLET_APPLICATIONS_ATTRIBUTE);
        session.removeAttribute(AdminConstants.PORTLET_WINDOWS_ATTRIBUTE);
        
        session.removeAttribute(AdminConstants.SHOW_WINDOW_ATTRIBUTE);
        session.removeAttribute(AdminConstants.HIDE_WINDOW_ATTRIBUTE);
        session.removeAttribute(AdminConstants.THICK_WINDOW_ATTRIBUTE);
        session.removeAttribute(AdminConstants.THIN_WINDOW_ATTRIBUTE);
        
        session.removeAttribute(AdminConstants.DEPLOYMENT_SUCCEEDED_ATTRIBUTE);
        session.removeAttribute(AdminConstants.DEPLOYMENT_FAILED_ATTRIBUTE);
        session.removeAttribute(AdminConstants.UNDEPLOYMENT_SUCCEEDED_ATTRIBUTE);
        session.removeAttribute(AdminConstants.UNDEPLOYMENT_FAILED_ATTRIBUTE);
        session.removeAttribute(AdminConstants.CREATION_SUCCEEDED_ATTRIBUTE);
        session.removeAttribute(AdminConstants.CREATION_FAILED_ATTRIBUTE);
        session.removeAttribute(AdminConstants.MODIFY_SUCCEEDED_ATTRIBUTE);
        session.removeAttribute(AdminConstants.MODIFY_FAILED_ATTRIBUTE);
        session.removeAttribute(AdminConstants.NO_WINDOW_DATA_ATTRIBUTE);
        session.removeAttribute(AdminConstants.SELECTED_PORTLET_WINDOW_ATTRIBUTE);
        
        session.removeAttribute(AdminConstants.CURRENT_SILVERPEAS_ELEMENT_ID);
        return session;
    }
    
    protected static void refreshList(HttpServletRequest request, String locale) {
    	refreshList(request, "useless", "useless", "useless", locale);
    }
    
    protected static void refreshList(HttpServletRequest request, String context, String userId, String spaceId, String locale) {
        try {
            PortletAdminData portletAdminData = PortletAdminDataFactory.getPortletAdminData(context);
            HttpSession session = request.getSession(true);
            setAttributes(session, portletAdminData, context, userId, spaceId, locale);
        } catch (PortletRegistryException pre) {
            logger.log(Level.SEVERE, "PSPCD_CSPPD0038", pre);
        }
    }
    
    protected static void setAttributes(HttpSession session, PortletAdminData portletAdminData, String elementId, String userId, String spaceId, String locale) {
        session.removeAttribute(AdminConstants.PORTLETS_ATTRIBUTE);
        session.removeAttribute(AdminConstants.PORTLET_APPLICATIONS_ATTRIBUTE);
        session.removeAttribute(AdminConstants.PORTLET_WINDOWS_ATTRIBUTE);
        session.removeAttribute(AdminConstants.CURRENT_SILVERPEAS_ELEMENT_ID);
        session.removeAttribute(AdminConstants.CURRENT_SILVERPEAS_USER_ID);
        session.removeAttribute(AdminConstants.CURRENT_SILVERPEAS_SPACE_ID);
        
        
        
        session.setAttribute(AdminConstants.PORTLETS_ATTRIBUTE, portletAdminData.getPortlets(locale));
        session.setAttribute(AdminConstants.PORTLET_APPLICATIONS_ATTRIBUTE, portletAdminData.getPortletApplicationNames());
        session.setAttribute(AdminConstants.PORTLET_WINDOWS_ATTRIBUTE, portletAdminData.getPortletWindowNames());
        session.setAttribute(AdminConstants.CURRENT_SILVERPEAS_ELEMENT_ID, elementId);
        session.setAttribute(AdminConstants.CURRENT_SILVERPEAS_USER_ID, userId);
        session.setAttribute(AdminConstants.CURRENT_SILVERPEAS_SPACE_ID, spaceId);
    }
    
    protected static void setPortletWindowAttributes(HttpSession session, PortletAdminData portletAdminData, String portletWindowName) throws Exception {
        // If portlet window name is null, get the name from the portlet window list
        if(portletWindowName == null) {
            List list = portletAdminData.getPortletWindowNames();
            if(list != null) {
                portletWindowName = (String)list.get(0);
            }
        }
        if(portletWindowName != null) {
            session.removeAttribute(AdminConstants.SHOW_WINDOW_ATTRIBUTE);
            session.removeAttribute(AdminConstants.HIDE_WINDOW_ATTRIBUTE);
            session.removeAttribute(AdminConstants.THICK_WINDOW_ATTRIBUTE);
            session.removeAttribute(AdminConstants.THIN_WINDOW_ATTRIBUTE);
            
            boolean visible = portletAdminData.isVisible(portletWindowName);
            String width = portletAdminData.getWidth(portletWindowName);
            if(visible) {
                session.setAttribute(AdminConstants.SHOW_WINDOW_ATTRIBUTE, "checked");
                session.setAttribute(AdminConstants.HIDE_WINDOW_ATTRIBUTE, "");
            } else {
                session.setAttribute(AdminConstants.HIDE_WINDOW_ATTRIBUTE, "checked");
                session.setAttribute(AdminConstants.SHOW_WINDOW_ATTRIBUTE, "");
            }
            if(PortletRegistryConstants.WIDTH_THICK.equals(width)) {
                session.setAttribute(AdminConstants.THICK_WINDOW_ATTRIBUTE, "selected");
                session.setAttribute(AdminConstants.THIN_WINDOW_ATTRIBUTE, "");
            } else {
                session.setAttribute(AdminConstants.THIN_WINDOW_ATTRIBUTE, "selected");
                session.setAttribute(AdminConstants.THICK_WINDOW_ATTRIBUTE, "");
            }
        }
    }
}
