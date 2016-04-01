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
<%@ page import="org.silverpeas.core.web.look.LookHelper" %>
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<%@ include file="check.jsp" %>

<%-- Include tag library --%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>

<fmt:setLocale value="${sessionScope['SilverSessionController'].favoriteLanguage}" />
<view:setBundle bundle="${requestScope.resources.multilangBundle}" />
<view:setBundle bundle="${requestScope.resources.iconsBundle}" var="icons" />

<c:set var="ctxPath" value="${pageContext.request.contextPath}"/>
<%
LookHelper 	helper 	= LookHelper.getLookHelper(session);

String frontOfficeURL = (gef.getLookFrame().startsWith("/") ? m_context : m_context+"/admin/jsp/")+gef.getLookFrame()+"?Login=1";
if (helper.getURLOfLastVisitedCollaborativeSpace() != null) {
  frontOfficeURL = helper.getURLOfLastVisitedCollaborativeSpace();
}
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<view:looknfeel />
<style type="text/css">
body {  background-image: url(<%=gef.getFavoriteLookSettings().getString("FondManager")%>); background-repeat: repeat-x}
</style>
<title><%=resource.getString("GML.popupTitle")%></title>
<script type="text/javascript">
function routPage() {
<%
  if (request.getAttribute("URL") != null) {
    out.println("window.parent.frames.bottomFrame.location.href = '" + (String)request.getAttribute("URL") + "';");
  }
%>
}

function exit(){
  window.parent.location.href="<%=m_context%>/LogoutServlet";
}

// User Notification Popup
function notifyPopup(context,compoId,users,groups) {
    top.scriptFrame.SP_openWindow(context+'/RnotificationUser/jsp/Main.jsp?popupMode=Yes&editTargets=No&compoId=' + compoId + '&theTargetsUsers='+users+'&theTargetsGroups='+groups, 'notifyUserPopup', '700', '400', 'menubar=no,scrollbars=no,statusbar=no');
}
</script>
</head>
<body onload="javascript:routPage()" id="topBarManager">

<div id="outilsAdmin">
                &nbsp;&nbsp;<span class="textePetitBold"><%=resource.getString("JMP.tools")%>&nbsp;:</span>&nbsp;&nbsp;<img src="<%=resource.getIcon("JMP.arrow") %>" border="0"/>&nbsp;&nbsp;
                <a href="javascript:notifyPopup('<%=m_context%>','','Administrators','')"><img border="0" src="<%=resource.getIcon("JMP.mailAdmin")%>" alt="<%=resource.getString("JMP.feedback") %>" title="<%=resource.getString("JMP.feedback") %>"/></a>&nbsp;
                <a href="javascript:exit()"><img border="0" src="<%=resource.getIcon("JMP.login")%>" alt="<%=resource.getString("JMP.exit") %>" title="<%=resource.getString("JMP.exit") %>"/></a>&nbsp;
                <a href="<%=helper.getSettings("helpURL", "/help_fr/Silverpeas.htm")%>" target="_blank"><img border="0" src="<%=resource.getIcon("JMP.help")%>" alt="<%=resource.getString("JMP.help") %>" title="<%=resource.getString("JMP.help") %>"/></a>&nbsp;
                <a href="<%=m_context + URLUtil.getURL(URLUtil.CMP_CLIPBOARD) + "Idle.jsp?message=SHOWCLIPBOARD"%>" target="IdleFrame"><img src="<%=resource.getIcon("JMP.clipboardIcon")%>" border="0" alt="<%=resource.getString("JMP.clipboard")%>" onfocus="self.blur()" title="<%=resource.getString("JMP.clipboard")%>"/></a>&nbsp;
                <a href="<%=frontOfficeURL%>" target="_top"><img border="0" src="<%=resource.getIcon("JMP.peas")%>" alt="<%=resource.getString("JMP.backSilverpeas") %>" title="<%=resource.getString("JMP.backSilverpeas") %>"/></a>&nbsp;
                &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
                <span class="textePetitBold"><%=resource.getString("GML.date")%> :
                &nbsp;&nbsp;<img src="<%=resource.getIcon("JMP.arrow") %>" border="0"/>&nbsp;&nbsp;
                <%=helper.getDate()%></span>
</div>

<table width="100%" cellspacing="0" cellpadding="0" border="0">
<tr>
    <td width="120" align="left">
    <a href="<%=frontOfficeURL%>" target="_top">
    <img src="<%=resource.getIcon("JMP.px")%>" width="120" height="88" border="0" alt="<%=resource.getString("JMP.backSilverpeas") %>" title="<%=resource.getString("JMP.backSilverpeas") %>"/>
    </a>
    </td>
    <td rowspan="2" width="100%" valign="bottom">
      <table width="100%" cellspacing="0" cellpadding="0" border="0">
      <tr>
          <td align="left" valign="bottom" colspan="2">
            <table width="100%" cellspacing="0" cellpadding="0" border="0">
            <tr>
                <td><img src="<%=resource.getIcon("JMP.px")%>" width="82" height="25"/></td>

<%// *********************   Gestion des onglets  ********************* %>
                <%
                  JobManagerService[] services = (JobManagerService[])request.getAttribute("Services");
                  JobManagerService[] operation = (JobManagerService[])request.getAttribute("Operation");

                  int nbOnglet = services.length;//4; // 1 onglet = 1 service du job manager
                  boolean actif = true;
                  for (int i=0; i<nbOnglet; i++)
                  {
                %>
                <td>
                  <%
                    if (!services[i].isActif())
                    {
                  %>
                  <%//------------ debut onglets off------------------ %>
                  <table width="150" cellspacing="0" cellpadding="0" border="0">
                  <tr>
                      <td rowspan="3" width="1"><img src="<%=resource.getIcon("JMP.px")%>" width="1" height="24"/></td>
                      <td valign="top"><img src="<%=resource.getIcon("JMP.ongletGchOff")%>"/></td>
                      <td rowspan="2" colspan="2" width="100%" nowrap class="ongletAdminOff" align="center">
                        <a href="ChangeService?Id=<%=services[i].getId()%>">
                        <span class="txtPetitBlanc"><%=resource.getString(services[i].getLabel())%></span>
                        </a>
                      </td>
                      <td align="right" valign="top"><img src="<%=resource.getIcon("JMP.ongletDtOff")%>"/></td>
                  </tr>
                  <tr>
                      <td class="ongletAdminOff"><img src="<%=resource.getIcon("JMP.px")%>" width="1" height="12"/></td>
                      <td class="ongletAdminOff"><img src="<%=resource.getIcon("JMP.px")%>" width="1" height="12"/></td>
                  </tr>
                  <tr>
                      <td colspan="4" class="line"><img src="<%=resource.getIcon("JMP.px")%>" width="1" height="1"/></td>
                  </tr>
                  <tr>
                      <td><img src="<%=resource.getIcon("JMP.px")%>" width="1" height="1"/></td>
                      <td colspan="4" class="intfdcolor4"><img src="<%=resource.getIcon("JMP.px")%>" width="1" height="1"/></td>
                  </tr>
                  </table>
                  <%//------------ fin onglets off------------------ %>
                  <%
                  }
                  else
                  {
                  %>
                  <%//------------ debut onglets on------------------ %>
                  <table width="150" cellspacing="0" cellpadding="0" border="0">
                  <tr>
                      <td rowspan="3" width="1"><img src="<%=resource.getIcon("JMP.px")%>" width="1" height="24"/></td>
                      <td valign="top"><img src="<%=resource.getIcon("JMP.ongletGchOn")%>"/></td>
                      <td rowspan="2" colspan="2" width="100%" nowrap class="ongletAdminOn" align="center">
                      <span class="textePetitBold"><%=resource.getString(services[i].getLabel())%></span>
                      </td>
                      <td align="right" valign="top"><img src="<%=resource.getIcon("JMP.ongletDtOn")%>"/></td>
                  </tr>
                  <tr>
                      <td class="ongletAdminOn"><img src="<%=resource.getIcon("JMP.px")%>" width="1" height="12"/></td>
                      <td class="ongletAdminOn"><img src="<%=resource.getIcon("JMP.px")%>" width="1" height="12"/></td>
                  </tr>
                  <tr>
                      <td colspan="4" class="ongletAdminOn"><img src="<%=resource.getIcon("JMP.px")%>" width="1" height="1"/></td>
                  </tr>
                  <tr>
                      <td><img src="<%=resource.getIcon("JMP.px")%>" width="1" height="1"/></td>
                      <td colspan="4" class="ongletAdminOn"><img src="<%=resource.getIcon("JMP.px")%>" width="1" height="1"/></td>
                  </tr>
                  </table>
                  <%actif = false; // juste pour faire beau, histoire d'avoir des onglets on et off :o) %>
                  <%//------------ fin onglets on------------------ %>
                  <%
                  }
                  %>
                </td>
                <%
                }
                %>
<%// *********************   Fin gestion des onglets  ********************* %>
              <td width="100%">&nbsp;</td>
            </tr>
            </table>
          </td>
          <td width="100%">&nbsp;</td>
      </tr>
      </table>
    </td>
    <td width="1"><img src="<%=resource.getIcon("JMP.px")%>" width="1" height="75"/></td>
</tr>
<tr class="intfdcolor4">
    <td width="120"><img src="<%=resource.getIcon("JMP.px")%>" width="120" height="1"/></td>
    <td width="1"><img src="<%=resource.getIcon("JMP.px")%>" width="1" height="1"/></td>
</tr>
<tr class="ongletAdminOn">
    <td colspan="2">
    <img src="<%=resource.getIcon("JMP.px")%>" width="80" height="1"/>

<%// *********************   Gestion des des composants de chaque onglet  ********************* %>
<%
  int nbOption = operation.length;
  for (int i=0; i < nbOption; i++) {
    if(!operation[i].isActif()){
    %>
      |<img src="<%=resource.getIcon("JMP.px")%>" width="10" height="1"/>
      <strong><a href="ChangeOperation?Id=<%=operation[i].getId()%>"><%=resource.getString(operation[i].getLabel())%></a></strong>
      <img src="<%=resource.getIcon("JMP.px")%>" width="10" height="1"/>
    <%}
    else{
      %>
      |<img src="<%=resource.getIcon("JMP.px")%>" width="10" height="1"/>
      <span class="txtPetitBlanc"><%=resource.getString(operation[i].getLabel())%></span>
      <img src="<%=resource.getIcon("JMP.px")%>" width="10" height="1"/>
      <%
      }
      %>
  <%}
  if (nbOption != 0){
%>
  |
<%
  }
%>
<%// *********************   Fin gestion des des composants de chaque onglet  ********************* %>

    </td>
    <td width="1"><img src="<%=resource.getIcon("JMP.px")%>" width="1" height="18"/></td>
</tr>
<tr class="line">
    <td colspan="2"><img src="<%=resource.getIcon("JMP.px")%>" width="1" height="1"/></td>
    <td width="1"><img src="<%=resource.getIcon("JMP.px")%>" width="1" height="1"/></td>
</tr>
</table>
</body>
</html>
