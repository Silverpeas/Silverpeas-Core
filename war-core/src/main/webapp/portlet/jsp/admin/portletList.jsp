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

<%@ page import="com.stratelia.silverpeas.portlet.model.*"%>
<%@ page import="com.stratelia.silverpeas.portlet.*"%>

<%@ include file="language.jsp" %>

<%
  PortletComponent[] portletList = (PortletComponent[]) request.getAttribute("portletList") ;
  String col=request.getParameter("col") ;
%>
<HTML>
<HEAD>
<% out.println(gef.getLookStyleSheet()); %>
</HEAD>


<script language="JavaScript">
//pour netscape 6.2.0
if( navigator.appName == "Netscape" )      
{
    self.scrollbars.visible=true;
}


<!--
function addPorletAndClose(instanceId, spaceId) {
window.opener.parent.frames[1].location.href = "addPortlet?col=<%=col%>&instanceId=" + instanceId +"&spaceId=" + spaceId;
  window.close() ;
  return true ;
}

//-->
</script>
<body bgcolor="#FFFFFF" leftmargin="5" topmargin="5" marginwidth="5" marginheight="5">
<table width="100%" border="0" cellspacing="0" cellpadding="0">
  <tr>
    <td class="intfdcolor">
      <table width="100%" border="0" cellpadding="3" cellspacing="1">
        <tr> 
          <td colspan="3"><span class="domainName">
<%
  String target ;

   // if it's a column creation
   if (col==null) {
     target = "adminMain" ;
     col="-1" ;
%>

<%=messageBundle.getString("addToNewCol")%>

<% } else {
     target = "column" + col ;

%>

<%=messageBundle.getString("addToCol")%> <%=col%>

<% }%>
</span></td>
        </tr>
        <tr valign="top">
          <td nowrap align="right" class="intfdcolor51">&nbsp;</td>
          <td class="intfdcolor51" nowrap><span class=txtnav><%=messageBundle.getString("portlet")%></span></td>
          <td class="intfdcolor51" width="100%"><span class=txtnav><%=messageBundle.getString("desc")%></span></td>
        </tr>
<%
  for (int i=0 ; i<portletList.length ; i++) {
    PortletComponent instance = portletList[i] ;
%>
        <tr valign="top"> 
          <td nowrap align="right" class="intfdcolor4"><img src="../../util/icons/portlet/1px.gif" width="50" height="1" ><a href="javascript:onClick=addPorletAndClose('<%=instance.id%>','<%=instance.spaceId%>')"><img src="../../util/icons/portlet/portlet.gif" border="0"></a></td>
          <td class="intfdcolor4" nowrap><a href= "javascript:onClick=addPorletAndClose('<%=instance.id%>','<%=instance.spaceId%>')"><%=instance.name%> 
            </a></td>
          <td class="intfdcolor4" width="100%"><%=instance.description%></td>
        </tr>
 <% } %>
      </table>
    </td>
  </tr>
</table>
</BODY>
</HTML>
