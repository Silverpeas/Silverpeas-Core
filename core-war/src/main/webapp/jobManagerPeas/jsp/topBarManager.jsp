<%--

    Copyright (C) 2000 - 2018 Silverpeas

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as
    published by the Free Software Foundation, either version 3 of the
    License, or (at your option) any later version.

    As a special exception to the terms and conditions of version 3.0 of
    the GPL, you may redistribute this Program in connection with Free/Libre
    Open Source Software ("FLOSS") applications as described in Silverpeas's
    FLOSS exception.  You should have received a copy of the text describing
    the FLOSS exception, and it is also available here:
    "https://www.silverpeas.org/legal/floss_exception.html"

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
  window.parent.location.href="<%=m_context%>/Logout";
}

// User Notification Popup
function notifyPopup(context,compoId,users,groups) {
  SP_openWindow(context+'/RnotificationUser/jsp/Main.jsp?popupMode=Yes&editTargets=No&compoId=' + compoId + '&theTargetsUsers='+users+'&theTargetsGroups='+groups, 'notifyUserPopup', '700', '400', 'menubar=no,scrollbars=no,statusbar=no');
}
</script>
</head>
<body onload="javascript:routPage()" id="topBarManager">          
<div id="outilsAdmin">
	<!-- Ajouter dans le a propos ? <a href="javascript:notifyPopup('<%=m_context%>','','Administrators','')"><img border="0" src="<%=resource.getIcon("JMP.mailAdmin")%>" alt="<%=resource.getString("JMP.feedback") %>" title="<%=resource.getString("JMP.feedback") %>"/><span><%=resource.getString("JMP.feedback") %></span></a> -->
	<a class="sp_back_front" href="<%=frontOfficeURL%>" target="_top"><%=resource.getString("JMP.backSilverpeas") %></a>
	<a href="<%=m_context + URLUtil.getURL(URLUtil.CMP_CLIPBOARD) + "Idle.jsp?message=SHOWCLIPBOARD"%>" target="IdleFrame"><img src="<%=resource.getIcon("JMP.clipboardIcon")%>" border="0" alt="<%=resource.getString("JMP.clipboard")%>" onfocus="self.blur()" title="<%=resource.getString("JMP.clipboard")%>"/><span><%=resource.getString("JMP.clipboard")%></span></a>
	<a href="<%=helper.getSettings("helpURL", "/help_fr/Silverpeas.htm")%>" target="_blank"><img border="0" src="<%=resource.getIcon("JMP.help")%>" alt="<%=resource.getString("JMP.help") %>" title="<%=resource.getString("JMP.help") %>"/><span><%=resource.getString("JMP.help") %></span></a>
	<a class="sp_logout" href="javascript:exit()"><img border="0" src="<%=resource.getIcon("JMP.login")%>" alt="<%=resource.getString("JMP.exit") %>" title="<%=resource.getString("JMP.exit") %>"/><span><%=resource.getString("JMP.exit") %></span></a>
	<!-- Date ? Util ? <%=resource.getString("GML.date")%><%=helper.getDate()%> -->
</div>
<ul class="sp_menuAdmin">
<%// *********************   Gestion des onglets  ********************* %>
                <%
                  JobManagerService[] services = (JobManagerService[])request.getAttribute("Services");
                  JobManagerService[] operation = (JobManagerService[])request.getAttribute("Operation");

                  int nbOnglet = services.length;//4; // 1 onglet = 1 service du job manager
                  for (int i=0; i<nbOnglet; i++)
                  {
                    if (!services[i].isActif())
                    {
                  %>
                  <%//------------ debut onglets off------------------ %>
                  <li>
                        <a href="ChangeService?Id=<%=services[i].getId()%>">
                        <span><%=resource.getString(services[i].getLabel())%></span>
                        </a>
				  </li>
                  <%//------------ fin onglets off------------------ %>
                  <%
                  }
                  else
                  {
                  %>
                  <%//------------ debut onglets on------------------ %>
                  <li class="select">
                      <span class="textePetitBold"><%=resource.getString(services[i].getLabel())%></span>
				  </li>
                  <%//------------ fin onglets on------------------ %>
                  <%
                  }
                  
                }
				%>
                
</ul>
<%// *********************   Fin gestion des onglets  ********************* %>
  

<%// *********************   Gestion des des composants de chaque onglet  ********************* %>
<%
  int nbOption = operation.length;
   if (nbOption != 0){
%>
 <div class="sp_sousMenuAdmin">
<%
  }
  for (int i=0; i < nbOption; i++) {
    if(!operation[i].isActif()){
    %>

      <a href="ChangeOperation?Id=<%=operation[i].getId()%>"><%=resource.getString(operation[i].getLabel())%></a>
    <%}
    else{
      %>
      <span class="select"><%=resource.getString(operation[i].getLabel())%></span>
      <%
      }
      %>
  <%}
   if (nbOption != 0){
%>
  </div>  
<%} %>
<%// *********************   Fin gestion des des composants de chaque onglet  ********************* %>
</body>
</html>