<%--

    Copyright (C) 2000 - 2013 Silverpeas

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as
    published by the Free Software Foundation, either version 3 of the
    License, or (at your option) any later version.

    As a special exception to the terms and conditions of version 3.0 of
    the GPL, you may redistribute this Program in connection with Free/Libre
    Open Source Software ("FLOSS") applications as described in Silverpeas's
    FLOSS exception.  You should have received a copy of the text describing
    the FLOSS exception, and it is also available here:
    "http://www.silverpeas.org/docs/core/legal/floss_exception.html"

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.

--%>

<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page isELIgnored="false"%>
<%
  response.setHeader("Cache-Control","no-store"); //HTTP 1.1
  response.setHeader("Pragma","no-cache"); //HTTP 1.0
  response.setDateHeader ("Expires",-1); //prevents caching at the proxy server
%>


<%@ page import="com.stratelia.silverpeas.peasCore.URLManager"%>
<%@page import="java.util.List" %>
<%@page import="java.util.Date" %>
<%@ page import="com.stratelia.webactiv.util.DateUtil" %>
<%@page import="com.stratelia.silverpeas.peasCore.MainSessionController"%>
<%@ page import="com.stratelia.silverpeas.util.ResourcesWrapper"%>
<%@ page import="com.stratelia.webactiv.util.ResourceLocator"%>


<%
	MainSessionController 	m_MainSessionCtrl 	= (MainSessionController) session.getAttribute(MainSessionController.MAIN_SESSION_CONTROLLER_ATT);
	String					userId				= m_MainSessionCtrl.getUserId();
	String 					language 			= m_MainSessionCtrl.getFavoriteLanguage();
	ResourceLocator 		messages 			= new ResourceLocator("org.silverpeas.util.attachment.multilang.attachment", language);
	ResourceLocator 		attSettings 		= new ResourceLocator("org.silverpeas.util.attachment.Attachment", "");
	ResourcesWrapper attResources = new ResourcesWrapper(messages, null, attSettings, language);

	boolean useContextualMenu = attSettings.getBoolean("ui.useContextualMenu", false);
  pageContext.setAttribute("useContextualMenu", useContextualMenu);
%>