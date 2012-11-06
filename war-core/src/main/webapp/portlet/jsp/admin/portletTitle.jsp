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

<%
response.setHeader("Cache-Control","no-store"); //HTTP 1.1
response.setHeader("Pragma","no-cache");       //HTTP 1.0
response.setDateHeader ("Expires",-1);        //prevents caching at the proxy server
%>

<%@ include file="language.jsp" %>

<jsp:useBean id="portlet" scope="request" class="com.stratelia.silverpeas.portlet.Portlet"/>

<% String lastPortlet = request.getParameter("lastPortlet") ;
   String col = request.getParameter("col") ;
   String row = request.getParameter("row") ;
   String target ;
   if (lastPortlet.equalsIgnoreCase("yes")) {
     target = "adminMain" ;
   } else {
     target = "column" + request.getParameter("col") ;
   }
%>


<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<% out.println(gef.getLookStyleSheet()); %>
</head>


<body bgcolor="#FFFFFF" leftmargin="0" topmargin="0" marginwidth="0" marginheight="0">
<center>
  <table width="100%" border="0" cellspacing="0" cellpadding="0" bgcolor="#AFB8C9">
    <tr>
      <td><img src="../../util/icons/portlet/1px.gif" width="1" height="2"></td>
    </tr>
    <tr> 
      <td> 
        <table width="100%" border="0" cellspacing="0" cellpadding="0" bgcolor=#FFFFFF>
          <tr> 
            <td valign="bottom" bgcolor="#AFB8C9"> 
              <table width="100%" border="0" cellspacing="0" cellpadding="0">
                <tr> 
                  <td nowrap colspan="2"><img src="../../util/icons/portlet/1px.gif" width="1" height="1"></td>
                </tr>
                <tr> 
                  <td bgcolor="#FFFFFF" nowrap>&nbsp;<img src="../../util/icons/component/<%=portlet.getComponentName()%>Small.gif" width="15" height="15" align="absmiddle"> 
                    <span class="txtnav"> 
                    <jsp:getProperty name="portlet" property="name"/>
                    </span> </td>
                  <td bgcolor="#AFB8C9"><img src="../../util/icons/portlet/rond.gif" width="20" height="20"></td>
                </tr>
              </table>
              <!--img src="../../util/icons/portlet/1px.gif" width="200" height="1"-->
            </td>
            <td bgcolor="#AFB8C9" align="center"> 
              <table border="0" cellspacing="0" cellpadding="0">
                <tr> 
                  <td rowspan="2"><a href="left?col=<%=col + 
                                 "&row=" + row +
                                 "&spaceId=" + request.getParameter("spaceId")%>"
                       target="adminMain"><img src="../../util/icons/portlet/PortletMoveLeft.gif" border="0" width="15" height="15" alt="D&eacute;placer la portlet vers la  gauche" title="D&eacute;placer la portlet vers la  gauche"> 
                    </a></td>
                  <td><a href="up?col=<%=col + 
                                 "&row=" + row +
                                 "&spaceId=" + request.getParameter("spaceId")%>"
                         target="column<%=col%>"><img src="../../util/icons/portlet/PortletMoveUp.gif" border="0" width="15" height="15" alt="D&eacute;placer la portlet en haut" title="D&eacute;placer la portlet en haut"> 
                    </a></td>
                  <td rowspan="2"><a href="right?col=<%=col + 
                                  "&row=" + row +
                                  "&spaceId=" + request.getParameter("spaceId")%>"
                       target="adminMain"> <img src="../../util/icons/portlet/PortletMoveRight.gif" border="0" width="15" height="15" alt="D&eacute;placer la portlet vers la droite" title="D&eacute;placer la portlet vers la droite"></a></td>
                </tr>
                <tr> 
                  <td><a href="down?col=<%=col + 
                              "&row=" + row +
                              "&spaceId=" + request.getParameter("spaceId")%>"
                         target="column<%=col%>"><img src="../../util/icons/portlet/PortletMoveDown.gif" border="0" width="15" height="15" alt="D&eacute;placer la portlet en bas" title="D&eacute;placer la portlet en bas"></a></td>
                </tr>
              </table>
            </td>
            <td bgcolor="#AFB8C9" align="right"><img src="../../util/icons/portlet/arrond.gif"></td>
            <td align="right" valign="top"> 
              <!--<img src="../../util/icons/portlet/PortletReduce.gif"><img src="../../util/icons/portlet/FullPagePortlet.gif">-->
              <a href="removePortlet?col=<%=col + 
                                    "&row=" + row +
                                    "&spaceId=" + request.getParameter("spaceId") +
                                    "&portletIndex=" + request.getParameter("portletIndex") +
                                    "&lastPortlet=" + lastPortlet
                                    %>" 
                 target="<%=target%>"><img src="../../util/icons/portlet/PortletClose.gif" border="0" alt="Supprimer la portlet" title="Supprimer la portlet"> 
              </a></td>
          </tr>
          <tr bgcolor="#000000"> 
            <td valign="bottom" colspan="4"><img src="../../util/icons/1px.gif" width="1" height="1"></td>
          </tr>
        </table>
      </td>
    </tr>
  </table>
</center>
</body>
</html>
