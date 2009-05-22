<%--
  CDDL HEADER START
  The contents of this file are subject to the terms
  of the Common Development and Distribution License
  (the License). You may not use this file except in
  compliance with the License.

  You can obtain a copy of the License at
  http://www.sun.com/cddl/cddl.html and legal/CDDLv1.0.txt
  See the License for the specific language governing
  permission and limitations under the License.

  When distributing Covered Code, include this CDDL
  Header Notice in each file and include the License file
  at legal/CDDLv1.0.txt.
  If applicable, add the following below the CDDL Header,
  with the fields enclosed by brackets [] replaced by
  your own identifying information:
  "Portions Copyrighted [year] [name of copyright owner]"

  Copyright 2006 Sun Microsystems Inc. All Rights Reserved
  CDDL HEADER END
--%>

<%@page contentType="text/html"%>

<%@page import="com.silverpeas.portlets.portal.DriverUtil" %>
<%@ page import="com.stratelia.webactiv.util.ResourceLocator"%>
<%@ page import="com.stratelia.webactiv.util.*"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.GraphicElementFactory"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.Encode"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.buttons.Button"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.buttonPanes.ButtonPane"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.operationPanes.OperationPane"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.arrayPanes.*"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.browseBars.BrowseBar"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.window.Window"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.frame.Frame"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.tabs.TabbedPane"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.board.Board"%>

<%@ page import="com.stratelia.silverpeas.peasCore.MainSessionController"%>


<%@ taglib uri="/WEB-INF/c.tld" prefix="c"%>
<%@ taglib uri="/WEB-INF/fmt.tld" prefix="fmt"%>
<%@ taglib uri="/WEB-INF/viewGenerator.tld" prefix="view"%>

<!--Load the resource bundle for the page -->
<fmt:setBundle basename="com.silverpeas.portlets.multilang.portletsBundle" />

<%
MainSessionController 	m_MainSessionCtrl 	= (MainSessionController) session.getAttribute("SilverSessionController");
if (m_MainSessionCtrl == null)
{	
  String sessionTimeout = GeneralPropertiesManager.getGeneralResourceLocator().getString("sessionTimeout");
  getServletConfig().getServletContext().getRequestDispatcher(sessionTimeout).forward(request, response);
  return;
}

GraphicElementFactory 	gef 				= (GraphicElementFactory) session.getAttribute("SessionGraphicElementFactory");
String 					language 			= m_MainSessionCtrl.getFavoriteLanguage();
ResourceLocator 		message 			= new ResourceLocator("com.silverpeas.portlets.multilang.portletsBundle", language);
String 					m_context 			= GeneralPropertiesManager.getGeneralResourceLocator().getString("ApplicationURL");
%>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1"></meta>
    <title><fmt:message key="portlets.homepage"/></title>
	<view:looknfeel />

    <c:set var="list" value="${sessionScope['com.sun.portal.portletcontainer.markupHeaders']}" />
    <c:forEach items="${list}" var="portlet">
        <c:out value="${portlet}" escapeXml="false" />
    </c:forEach>
	<script type="text/javascript" src="<%=m_context%>/util/javaScript/animation.js"></script>
</head>
<body id="portletPages">

<div id="portal-page">