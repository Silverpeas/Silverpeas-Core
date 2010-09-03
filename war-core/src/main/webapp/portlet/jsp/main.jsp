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

<%@ page errorPage="../../admin/jsp/errorpagePopup.jsp"%>

<HTML>
<HEAD>
<jsp:useBean id="spaceModel" scope="request" class="com.stratelia.silverpeas.portlet.SpaceModel">
  <jsp:setProperty name="spaceModel" property="*" />
</jsp:useBean>

<%@ page import="com.stratelia.silverpeas.portlet.*"%>
<%
  SpaceColumn column ;
%>

<TITLE>
  <jsp:getProperty name="spaceModel" property="name"/>
</TITLE>
</HEAD>
<frameset cols="<%=spaceModel.getColumnsRatios()%>" bordercolor="#FFFFFF" marginheight=5 marginwidth=5>
  <% for (int colNum=0 ; colNum <spaceModel.getcolumnsCount() ; colNum++ ) {
       column = spaceModel.getColumn(colNum) ;
  %>
       <frame name="column<%=column.getColumnNumber()%>" scrolling="NO" src="column?col=<%=colNum%>&spaceId=<%=request.getParameter("spaceId")%>" marginheight=5 marginwidth=5>  <% } %>
</frameset>
<noframes>
</noframes>
</HTML>
