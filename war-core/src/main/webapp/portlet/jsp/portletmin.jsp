<%--

    Copyright (C) 2000 - 2012 Silverpeas

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

<%@ page import="com.stratelia.silverpeas.portlet.*"%>
<%@ page import="com.stratelia.silverpeas.peasCore.URLManager"%>

<%@ page errorPage="../../admin/jsp/errorpagePopup.jsp"%>

<HTML>
<HEAD>
<jsp:useBean id="portlet" scope="request" class="com.stratelia.silverpeas.portlet.Portlet"/>
</HEAD>

<frameset rows="20,2,1*" cols="5,2,1*,2,5" frameborder="NO" border="no" framespacing="0">
<!-- Ligne 1 = HEADER -->
  <frame scrolling="NO" src="empty.htm" noresize>
  <frame scrolling="NO" src="bordure.htm" noresize>
  <frame src="portletTitle?id=<%=portlet.getIndex()%>&spaceId=<%=request.getParameter("spaceId")%>&portletState=min" scrolling="NO"  marginheight="0" marginwidth="0" frameborder="NO" noresize>
  <frame scrolling="NO" src="bordure.htm" noresize>
  <frame scrolling="NO" src="empty.htm" noresize>

  <!-- frame src="<%= URLManager.getApplicationURL()+portlet.getRequestRooter() + portlet.getContentUrl() +
                    "?space=WA" + request.getParameter("spaceId") + "&Component=" + portlet.getComponentName() +
                    portlet.getComponentInstanceId()%>"
         scrolling="YES" marginheight="0" marginwidth="0" frameborder="NO" noresize -->
   <!-- Ligne 3 = BORDURE DU BAS -->
  <frame scrolling="NO" src="empty.htm" noresize>
  <frame scrolling="NO" src="bordure.htm" noresize>
  <frame scrolling="NO" src="bordure.htm" noresize>
  <frame scrolling="NO" src="bordure.htm" noresize>
  <frame scrolling="NO" src="empty.htm" noresize>
    <!-- Ligne 100% -->
  <frame scrolling="NO" src="empty.htm" noresize>
  <frame scrolling="NO" src="empty.htm" noresize>
  <frame scrolling="NO" src="empty.htm" noresize>
  <frame scrolling="NO" src="empty.htm" noresize>
  <frame scrolling="NO" src="empty.htm" noresize>

</frameset>
<noframes></noframes>

</HTML>
