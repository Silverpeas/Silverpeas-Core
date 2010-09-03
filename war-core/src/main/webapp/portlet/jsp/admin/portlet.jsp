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

<%
response.setHeader("Cache-Control","no-store"); //HTTP 1.1
response.setHeader("Pragma","no-cache");       //HTTP 1.0
response.setDateHeader ("Expires",-1);        //prevents caching at the proxy server
%>

<%@ page errorPage="../../admin/jsp/errorpage.jsp"%>

<HTML>
<HEAD>
<jsp:useBean id="portlet" scope="request" class="com.stratelia.silverpeas.portlet.Portlet"/>
</HEAD>

<% String lastPortlet = request.getParameter("lastPortlet") ;
   String col = request.getParameter("col") ;
   String row = request.getParameter("row") ;
%>

<frameset rows="40,1*,2,10" cols="5,2,1*,2,5" frameborder="NO" border="0" framespacing="0"> 

  <!-- Ligne 1 = HEADER -->
  <frame scrolling="NO" src="empty.htm" noresize>
  <frame scrolling="NO" src="bordure.htm" noresize>
  <frame src="adminPortletTitle?col=<%=col + 
                               "&row=" + row +
                               "&portletIndex=" + portlet.getIndex() + 
                               "&spaceId=" + request.getParameter("spaceId") +
                               "&lastPortlet=" + lastPortlet
                               %>" scrolling="NO"  marginheight="0" marginwidth="0" frameborder="NO" noresize>
  <frame scrolling="NO" src="bordure.htm" noresize>
  <frame scrolling="NO" src="empty.htm" noresize>

  <!-- Ligne 2 = CONTENT -->
  <frame scrolling="NO" src="empty.htm" noresize>
  <frame scrolling="NO" src="bordure.htm" noresize>
  <frame src="adminPortletDummy?id=<%=request.getParameter("id")%>" 
         scrolling="AUTO" marginheight="0" marginwidth="0" frameborder="NO" noresize>
  <frame scrolling="NO" src="bordure.htm" noresize>
  <frame scrolling="NO" src="empty.htm" noresize>

  <!-- Ligne 3 = BORDURE DU BAS -->
  <frame scrolling="NO" src="empty.htm" noresize>
  <frame scrolling="NO" src="bordure.htm" noresize>
  <frame scrolling="NO" src="bordure.htm" noresize>
  <frame scrolling="NO" src="bordure.htm" noresize>
  <frame scrolling="NO" src="empty.htm" noresize>

  <!-- Ligne 4 = OMBRE PORTLET -->
  <frame scrolling="NO" src="empty.htm" noresize>
  <frame scrolling="NO" src="portletFooter" noresize>
  <frame scrolling="NO" src="portletFooter" noresize>
  <frame scrolling="NO" src="portletFooter" noresize>
  <frame scrolling="NO" src="empty.htm" noresize>
  
</frameset><noframes></noframes>

</HTML>
