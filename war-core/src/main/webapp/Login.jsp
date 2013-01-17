<%--
  Copyright (C) 2000 - 2013 Silverpeas

  This program is free software: you can redistribute it and/or modify
  it under the terms of the GNU Affero General Public License as
  published by the Free Software Foundation, either version 3 of the
  License, or (at your option) any later version.

  As a special exception to the terms and conditions of version 3.0 of
  the GPL, you may redistribute this Program in connection with Free/Libre
  Open Source Software ("FLOSS") applications as described in Silverpeas's
  FLOSS exception.  You should have recieved a copy of the text describing
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
<%@ page import="com.stratelia.webactiv.util.GeneralPropertiesManager"%>
<%@ page import="com.stratelia.webactiv.util.ResourceLocator"%>
<%@ page import="com.stratelia.silverpeas.authentication.*"%>
<%@ page import="com.silverpeas.util.StringUtil"%>

<%
response.setHeader("Cache-Control","no-store"); //HTTP 1.1
response.setHeader("Pragma","no-cache"); //HTTP 1.0
response.setDateHeader ("Expires",-1); //prevents caching at the proxy server
%>

<%
String errorCode = request.getParameter("ErrorCode");
String domainId = null;
if(StringUtil.isInteger(request.getParameter("DomainId"))) {
  domainId = request.getParameter("DomainId");
}

ResourceLocator general = new ResourceLocator("com.stratelia.silverpeas.lookAndFeel.generalLook", "");
String loginPage = general.getString("loginPage");
if (! StringUtil.isDefined(loginPage)){
  loginPage = request.getContextPath()+"/defaultLogin.jsp";
}
loginPage += "?DomainId="+domainId+"&ErrorCode="+errorCode+"&logout="+request.getParameter("logout");
response.sendRedirect(response.encodeRedirectURL(loginPage));
%>
