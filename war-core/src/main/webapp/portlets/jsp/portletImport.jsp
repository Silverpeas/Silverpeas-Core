<%--

    Copyright (C) 2000 - 2009 Silverpeas

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as
    published by the Free Software Foundation, either version 3 of the
    License, or (at your option) any later version.

    As a special exception to the terms and conditions of version 3.0 of
    the GPL, you may redistribute this Program in connection with Free/Libre
    Open Source Software ("FLOSS") applications as described in Silverpeas's
    FLOSS exception.  You should have received a copy of the text describing
    the FLOSS exception, and it is also available here:
    "http://repository.silverpeas.com/legal/licensing"

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.

--%>
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<%@ page import="javax.servlet.*"%>
<%@ page import="javax.servlet.http.*"%>
<%@ page import="javax.servlet.jsp.*"%>
<%@ page import="java.io.PrintWriter"%>
<%@ page import="java.io.IOException"%>
<%@ page import="java.io.FileInputStream"%>
<%@ page import="java.io.ObjectInputStream"%>
<%@ page import="java.beans.*"%>
<%@ page import="java.lang.String"%>
<%@ page import="java.util.*"%>

<%@ page import="javax.portlet.RenderRequest" %>
<%@ page import="javax.portlet.RenderResponse" %>
<%@ page import="javax.portlet.PortletPreferences" %>
<%@ page import="javax.portlet.PortletURL" %>
<%@ page import="javax.portlet.WindowState"%>

<%@ page import="com.stratelia.webactiv.util.*"%>
<%@ page import="com.stratelia.webactiv.homepage.*"%>
<%@ page import="com.stratelia.webactiv.util.ResourceLocator"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.GraphicElementFactory"%>

<%@ page import="com.stratelia.silverpeas.peasCore.URLManager"%>
<%@ page import="com.stratelia.silverpeas.peasCore.MainSessionController"%>
<%@ page import="com.stratelia.webactiv.beans.admin.OrganizationController"%>

<%@ page import="com.silverpeas.portlets.FormNames" %>

<%@ page import="com.stratelia.webactiv.util.DateUtil"%>
<%@ page import="com.silverpeas.util.StringUtil"%>

<%
MainSessionController 	m_MainSessionCtrl 	= (MainSessionController) session.getAttribute("SilverSessionController");
if (m_MainSessionCtrl == null)
{
  String sessionTimeout = GeneralPropertiesManager.getGeneralResourceLocator().getString("sessionTimeout");
  getServletConfig().getServletContext().getRequestDispatcher(sessionTimeout).forward(request, response);
  return;
}

GraphicElementFactory	gef = (GraphicElementFactory) session.getAttribute("SessionGraphicElementFactory");
String  language = m_MainSessionCtrl.getFavoriteLanguage();
ResourceLocator message 			= new ResourceLocator("com.stratelia.webactiv.homePage.multilang.homePageBundle", language);
ResourceLocator 		homePageSettings 	= new ResourceLocator("com.stratelia.webactiv.homePage.homePageSettings", "");
String 					m_sContext 			= GeneralPropertiesManager.getGeneralResourceLocator().getString("ApplicationURL");
%>