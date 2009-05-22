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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.sun.portal.container.ChannelURL;
import com.sun.portal.container.ChannelURLFactory;

public class PortletWindowURLFactory implements ChannelURLFactory { 

    private String desktopURL;

    public PortletWindowURLFactory(String desktopURL) {
        this.desktopURL = desktopURL;
    }

    public ChannelURL createChannelURL() {
        return new PortletWindowURL(this.desktopURL);
    }

    public String encodeURL( HttpServletRequest req, HttpServletResponse res, String url ) {
        return res.encodeURL( url );
    }
    
    public String getRenderTemplate() {
        throw new RuntimeException("Method not implemented");
    }

    public String getActionTemplate() {
        throw new RuntimeException("Method not implemented");
    }

    public String getResourceTemplate() {
        throw new RuntimeException("Method not implemented");
    }

    public String getSecurityErrorURL() {
        throw new RuntimeException("Method not implemented");
    }
} 
