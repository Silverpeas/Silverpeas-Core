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

<%@ page import="com.stratelia.silverpeas.portlet.*"%>
<%@ page import="com.stratelia.webactiv.util.ResourceLocator"%>
<%@ page import="com.stratelia.webactiv.util.GeneralPropertiesManager"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.GraphicElementFactory"%>


<%
	String m_context = GeneralPropertiesManager.getGeneralResourceLocator().getString("ApplicationURL");
	ResourceLocator generalMessage = GeneralPropertiesManager.getGeneralMultilang("fr");
	GraphicElementFactory gef = (GraphicElementFactory) session.getAttribute("SessionGraphicElementFactory");
	out.println(gef.getLookStyleSheet());
%>
<%@ page import="com.stratelia.silverpeas.portlet.*"%>
<%@ page errorPage="../../admin/jsp/errorpagePopup.jsp"%>
<jsp:useBean id="portlet" scope="request" class="com.stratelia.silverpeas.portlet.Portlet"/>

<HTML>
<HEAD>
  <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<%
out.println(gef.getLookStyleSheet());
%>
</HEAD>

<body bgcolor="#FFFFFF" leftmargin="0" topmargin="0" marginwidth="0" marginheight="0">
<table border="0" cellspacing="0" cellpadding="0" bgcolor="#AFB8C9" width="100%">
  <tr>
    <td><img src="<%=m_context%>/icons/1px.gif" height=2></td>
  </tr>
  <tr>
    <td>
      <table width="100%" border="0" cellspacing="0" cellpadding="0" bgcolor="#FFFFFF">
        <tr>
          <td valign="bottom" bgcolor="#AFB8C9">
            <table width="100%" border="0" cellspacing="0" cellpadding="0">
              <tr>
                <td nowrap colspan="2"><img src="<%=m_context%>/icons/1px.gif" width="1" height="1"></td>
              </tr>
              <tr>
                <td bgcolor="#FFFFFF" nowrap>&nbsp;<img src="<%=m_context%>/util/icons/component/<%=portlet.getComponentName()%>Small.gif"><span class="txtnav">&nbsp;<%=portlet.getName()%>
                  </span></td>
                <td bgcolor="#AFB8C9"><img src="<%=m_context%>/util/icons/portlet/rond.gif"></td>
              </tr>
            </table>
          </td>
          <td align="right" bgcolor="#AFB8C9" valign="top" nowrap><a href="state?id=<%=portlet.getIndex() +
                          "&spaceId=" + request.getParameter("spaceId") +
                          "&portletState=min"%>" target="column<%=portlet.getColumnNumber()%>"><img src="<%=m_context%>/util/icons/portlet/PortletReduce.gif" border="0"></a><a href="portlet?id=<%=portlet.getIndex() +
                            "&spaceId=" + request.getParameter("spaceId") +
                            "&portletState=max"%>" target="MyMain"><img src="<%=m_context%>/util/icons/portlet/FullPagePortlet.gif" border="0">
            </a></td>
        </tr>
        <tr bgcolor="#000000">
          <td valign="bottom" colspan="2"><img src="<%=m_context%>/icons/1px.gif" width="1" height="1"></td>
        </tr>
      </table>
    </td>
  </tr>
</table>
</body>
</html>
