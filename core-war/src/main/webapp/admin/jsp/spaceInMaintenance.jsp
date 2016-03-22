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

<%@ include file="importFrameSet.jsp" %>
<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.browsebars.BrowseBar"%>
<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.frame.Frame"%>

<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
   "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<%
String userId = m_MainSessionCtrl.getUserId();

Window 		window 		= gef.getWindow();
BrowseBar 	browseBar 	= window.getBrowseBar();
Frame 		frame 		= gef.getFrame();

// Get Emails Admins
  String[] idAdmins;
  OrganizationController organizationController =
      OrganizationControllerProvider.getOrganisationController();
  idAdmins = organizationController.getAdministratorUserIds(userId);
  UserDetail[] listAdmins = organizationController.getUserDetails(idAdmins);

%>
<title></title>
<view:looknfeel/>
</head>
<body>
<table width="100%" border="0" cellspacing="0" cellpadding="0">
  <tr>
    <td valign="top" width="45%">

 <table width="101" border="0" cellspacing="0" cellpadding="0">
          <tr>
            <td width="80" align="right" valign="middle"><img src="../../admin/jsp/icons/1px.gif" alt=""/></td>
            <td width="111" valign="middle"><img src="../../admin/jsp/icons/1px.gif" alt=""/></td>
          </tr>
          <tr>
            <td width="80" align="right" valign="top" nowrap="nowrap"><img src="../../admin/jsp/icons/1px.gif" alt=""/></td>
            <td width="111" valign="top"><img src="../../admin/jsp/icons/1px.gif" alt=""/></td>
          </tr>
          <tr>
            <td width="80" align="right" valign="top" nowrap="nowrap"><img src="../../admin/jsp/icons/1px.gif" alt=""/></td>
            <td width="111" valign="top"><img src="../../admin/jsp/icons/1px.gif" alt=""/></td>
          </tr>
          <tr>
            <td colspan="2">&nbsp;</td>
          </tr>
          <tr>
            <td colspan="2"><img src="../../admin/jsp/icons/1px.gif" alt=""/></td>
          </tr>
          <tr bgcolor="#FFFFFF">
            <td align="center">&nbsp;</td>
            <td align="center"><img src="../../admin/jsp/icons/1px.gif" alt=""/></td>
          </tr>
          <tr>
            <td colspan="2" align="center" bgcolor="#FFFFFF">
              <table width="100%" border="0" cellspacing="0" cellpadding="0">
                <tr>
                  <td><img src="../../admin/jsp/icons/1px.gif" width="1" height="1" alt=""/></td>
                </tr>
                <tr>
                  <td><img src="../../admin/jsp/icons/1px.gif" width="1" height="1" alt=""/></td>
                </tr>
                <tr>
                  <td><img src="../../admin/jsp/icons/1px.gif" width="1" height="1" alt=""/></td>
                </tr>
                <tr>
                  <td><img src="../../admin/jsp/icons/1px.gif" width="1" height="1" alt=""/></td>
                </tr>
              </table>
            </td>
          </tr>
        </table>
    </td>
    <td width="23%" nowrap="nowrap"><div align="left" class="textePetitBold"></div></td>
    <td width="32%" align="center"></td>
  </tr>
  <tr align="center">
    <td colspan="3"><img src="../../admin/jsp/icons/fond_logoTrameAppInM2.gif" alt=""/></td>
  </tr>
  <tr align="center">
    <td colspan="3" bgcolor="#CCCCCC"><img src="../../admin/jsp/icons/1px.gif" alt=""/></td>
  </tr>
  <tr align="right">
    <td colspan="3"><font size="1" face="Verdana, Arial, Helvetica, sans-serif" color="#999999"><%=message.getString("homePage.trademark")%></font></td>
  </tr>
  <tr align="center">
    <td colspan="3"><br/><br/>
 <div align="center" class="textePetitBold"><%=message.getString("homePage.spaceMaintenanceOn")%></div><br/>
<div align="center" class="textePetitBold"><img src="../../util/icons/attachment_to_upload.gif" alt=""/></div><br/>
&nbsp;&nbsp;
<div align="center"><%=message.getString("homePage.feedback")%><br/>
<% for (int i = 0; i < listAdmins.length; i++) { %>
	<a href="mailto:<%=listAdmins[i].geteMail() %>"><img src="../../admin/jsp/icons/icoOutilsMail.gif" align="absmiddle" border="0" alt=""/><view:username userId="<%=listAdmins[i].getId()%>"/></a><br/>
<% } %>
</div>
 </td>
  </tr>
</table>
</body>
</html>