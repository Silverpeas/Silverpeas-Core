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
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>
<%
response.setHeader("Cache-Control","no-store"); //HTTP 1.1
response.setHeader("Pragma","no-cache"); //HTTP 1.0
response.setDateHeader ("Expires",-1); //prevents caching at the proxy server
%>

<%@ page import="org.silverpeas.core.web.mvc.controller.MainSessionController"%>
<%@ page import="org.silverpeas.core.web.index.ApplicationDYMIndexer"%>
<%@ page import="org.silverpeas.core.admin.component.model.ComponentInst" %>
<%@ page import="org.silverpeas.core.admin.space.SpaceInst"%>
<%@ page import="org.silverpeas.core.admin.user.constant.UserAccessLevel"%>
<%@ page import="org.silverpeas.core.admin.service.OrganizationControllerProvider"%>


<%@ page import="org.silverpeas.core.util.ResourceLocator"%>
<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.GraphicElementFactory"%>

<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.frame.Frame" %>
<%@ page import="org.silverpeas.core.util.LocalizationBundle" %>
<%@ page import="org.silverpeas.core.admin.service.OrganizationController" %>
<%@ page errorPage="../../admin/jsp/errorpage.jsp"%>

<%!
  private String printSpaceAndSubSpaces(String spaceId, int depth,
      OrganizationController m_OrganizationController,
      String m_sContext) {
    ComponentInst 	compoInst 	= null;
    String 			compoName 	= null;
    String 			compoDesc 	= null;
    String 			compoId 	= null;
    String 			label 		= null;
    SpaceInst 		spaceInst 	= m_OrganizationController.getSpaceInstById(spaceId);
    StringBuffer	result 		= new StringBuffer();
    if (spaceInst!=null) {
        result.append("<table border=\"0\" cellspacing=\"0\" cellpadding=\"3\">\n");

        if (depth==0) result.append("<tr><td class=\"txtnote\">&nbsp;</td></tr>\n");

        result.append("<tr>\n");
        result.append("<td class=\"txttitrecol\">&#149; <A HREF=\"javaScript:index('Index','', '"+spaceId+"');\">").append(spaceInst.getName()).append("</a></td></tr>\n");

        result.append("<tr><td class=\"txtnote\">\n");

        String[] asAvailCompoForCurUser = m_OrganizationController.getAllComponentIds(spaceInst.getId());
        for(int nI = 0; nI <asAvailCompoForCurUser.length; nI++) {

            compoInst = m_OrganizationController.getComponentInst(asAvailCompoForCurUser[nI]);
            compoName = compoInst.getName();
            compoDesc = compoInst.getDescription();
            compoId = compoInst.getId();
            label = compoInst.getLabel();
            if ((label == null) || (label.length() == 0))
                label = compoName;

            result.append("&nbsp;<img src=").append(m_sContext).append("/util/icons/component/").append(compoName).append("Small.gif border=0 width=15 align=absmiddle>&nbsp;<A HREF=\"javaScript:index('Index','"+compoId+"','"+spaceId+"');\">").append(label).append("</A>\n");
        }

        // Get all sub spaces
        String [] subSpaceIds = m_OrganizationController.getAllSubSpaceIds(spaceId);
        for (int nI=0; nI<subSpaceIds.length; nI++) {
            result.append("<table border=\"0\" cellspacing=\"0\" cellpadding=\"0\">\n");
            result.append("<tr><td>&nbsp;&nbsp;</td>\n");
            result.append("<td class=\"txtnote\">\n");
            result.append(printSpaceAndSubSpaces(subSpaceIds[nI], depth+1, m_OrganizationController, m_sContext));
            result.append("</td></tr></table>\n");
        }

        result.append("</td>\n");
        result.append("</tr>\n");
        result.append("</table>\n");
    }
    return result.toString();
}
%>

<%
GraphicElementFactory gef = (GraphicElementFactory) session.getAttribute("SessionGraphicElementFactory");

MainSessionController m_MainSessionCtrl = (MainSessionController) session.getAttribute(MainSessionController.MAIN_SESSION_CONTROLLER_ATT);

if (m_MainSessionCtrl == null || !UserAccessLevel.ADMINISTRATOR.equals(m_MainSessionCtrl.getUserAccessLevel())) {
    // No session controller in the request -> security exception
    String sessionTimeout = ResourceLocator.getGeneralSettingBundle().getString("sessionTimeout");
    getServletConfig().getServletContext().getRequestDispatcher(sessionTimeout).forward(request, response);
    return;
}

  OrganizationController m_OrganizationController =
      OrganizationControllerProvider.getOrganisationController();

LocalizationBundle message = ResourceLocator.getLocalizationBundle("org.silverpeas.homePage.multilang.homePageBundle", m_MainSessionCtrl.getFavoriteLanguage());

String sURI = request.getRequestURI();
String sServletPath = request.getServletPath();
String sPathInfo = request.getPathInfo();
if(sPathInfo != null)
    sURI = sURI.substring(0,sURI.lastIndexOf(sPathInfo));
String m_sContext = sURI.substring(0,sURI.lastIndexOf(sServletPath));

String[] rootSpaceIds =  m_OrganizationController.getAllRootSpaceIds();

String spaceId 			= request.getParameter("SpaceId");
String componentId 		= request.getParameter("ComponentId");
String action 			= request.getParameter("Action");
String personalCompo 	= request.getParameter("PersonalCompo");
String indexMessage		= "";

if (action != null) {
  ApplicationDYMIndexer ai = ApplicationDYMIndexer.getInstance();
    if (action.equals("Index")) {
	ai.index(spaceId, componentId);
    } else if (action.equals("IndexPerso")) {
        ai.indexPersonalComponent(personalCompo);
    } else if (action.equals("IndexAllSpaces")) {
	ai.indexAllSpaces();
    } else if (action.equals("IndexAll")) {
	ai.indexAll();
    } else if (action.equals("IndexPdc")) {
	ai.indexPdc();
    }
    indexMessage = "Indexation lancée en tâche de fond !";
}

%>

<html>
<head>
<title>Navigation</title>
<!--link rel="stylesheet" href="styleSheets/admin.css"-->
<view:looknfeel/>
<script language="JavaScript">
function index(action, compo, space)
{
	var message = "Vous êtes sur le point de recréer un index pour la fonctionnalité \"voulez vous dire ?\" pour   ";
	if (action == "Index")
	{
		if (compo.length > 1)
			message += "un composant";
		else
			message += "un espace";
	}
	else if (action == "IndexPerso")
		message += "un composant de l'espace personnel";
	else if (action == "IndexAllSpaces")
		message += "tous les espaces collaboratifs";
	else if (action == "IndexAll")
		message += "tout le portail";
	else if (action == "IndexPdc")
		message += "le plan de classement";
	message += ". \nEtes-vous sûr de vouloir effectuer cette opération ?";
	if (confirm(message))
		location.href="didYouMeanIndexer.jsp?Action="+action+"&PersonalCompo="+compo+"&SpaceId="+space+"&ComponentId="+compo;
}
</script>
</head>
<body bgcolor="#FFFFFF" leftmargin="5" topmargin="5" marginwidth="5" marginheight="5">
<TABLE WIDTH="95%"><TR><TD>
<%
    Frame frame=gef.getFrame();
    frame.addTitle(message.getString("MyMap")+" - <a href=\"javaScript:index('IndexAll','','');\">INDEXER TOUT (espaces collaboratifs, espaces personnels, plan de classement)</a>");
    out.println(frame.printBefore());
%>
<BR>&nbsp;
<CENTER>
	<% if (indexMessage.length() > 0) { %>
		<table><tr><td><span class="txtnav"><font color="red"><%=indexMessage%></font></span></td></tr></table>
		<br>
	<% } %>
        <table border="0" cellspacing="0" cellpadding="0" width="90%" class=intfdcolor51>
          <tr>
            <td colspan="2" rowspan="2"><img src="../../admin/jsp/icons/accueil/angle_hg.gif" width="10" height="10"></td>
            <td bgcolor=2776A3><img src="../../admin/jsp/icons/1px.gif" width="1" height="1"></td>
            <td bgcolor=2776A3 rowspan="5"><img src="../../admin/jsp/icons/1px.gif"></td>
            <td bgcolor=2776A3><img src="../../admin/jsp/icons/1px.gif"></td>
            <td colspan="2" rowspan="2"><img src="../../admin/jsp/icons/accueil/angle_hd.gif" width="10" height="10"></td>
          </tr>
          <tr>
            <td><img src="../../admin/jsp/icons/1px.gif" height="9"></td>
            <td><img src="../../admin/jsp/icons/1px.gif"></td>
          </tr>
          <tr>
            <td bgcolor=2776A3 width="1"><img src="../../admin/jsp/icons/1px.gif"></td>
            <td width="9"><img src="../../admin/jsp/icons/1px.gif"></td>
            <td valign="top">
              <table border="0" cellspacing="0" cellpadding="0">
                <tr>
                  <td><img src="../../admin/jsp/icons/accueil/esp_perso.gif">&nbsp;</td>
                  <td width=250 nowrap><span class="txtnav" nowrap><%=message.getString("SpacePersonal")%></span></td>
                </tr>
                <tr>
                  <td class="txtnote" valign="top" align="right">&nbsp;<img src="<%=m_sContext%>/util/icons/component/agendaSmall.gif" border=0 width=15 align=absmiddle></td>
                  <td class="txtnote" nowrap><a href="javaScript:index('IndexPerso','Agenda','');">&nbsp;<%=message.getString("Diary")%></a></td>
                </tr>
                <tr>
                  <td class="txtnote" valign="top" align="right">&nbsp;<img src="<%=m_sContext%>/util/icons/component/todoSmall.gif" border=0 width=15 align=absmiddle></td>
                  <td class="txtnote" nowrap><a href="javaScript:index('IndexPerso','Todo','');">&nbsp;<%=message.getString("ToDo")%></a><br>
                    &nbsp;</td>
                </tr>
                <tr>
                  <td colspan="2" bgcolor=2776A3><img src="../../admin/jsp/icons/1px.gif"></td>
                </tr>
                <tr>
                <td colspan="2">
			<span class="txtnav" nowrap>Plan de classement</span><br><br>
			<a href="javaScript:index('IndexPdc','','');">INDEXER PDC</a>
                </td>
                </tr>
              </table>
            </td>
            <td valign="top">
              <table border="0" cellspacing="0" cellpadding="3">
                <tr>
                  <td nowrap ><img src="../../admin/jsp/icons/accueil/esp_collabo.gif" align=absmiddle>&nbsp;&nbsp;<span class="txtnav"><%=message.getString("SpaceCollaboration")%></span> - <a href="javaScript:index('IndexAllSpaces','','');">INDEXER TOUS LES ESPACES COLLABORATIFS</a></td>
                </tr>
                <tr><td>
                <%
                    for(int nK = 0; nK < rootSpaceIds.length; nK++) {
			out.println(printSpaceAndSubSpaces(rootSpaceIds[nK], 0, m_OrganizationController, m_sContext));
                    }
                %>
          </td>
            </tr>
              </table>
            </td>
            <td width="9"><img src="../../admin/jsp/icons/1px.gif"></td>
            <td bgcolor=2776A3 width="1"><img src="../../admin/jsp/icons/1px.gif"></td>
          </tr>
          <tr>
            <td colspan="2" rowspan="2"><img src="../../admin/jsp/icons/accueil/angle_bg.gif" width="10" height="10"></td>
            <td><img src="../../admin/jsp/icons/1px.gif" height="9"></td>
            <td><img src="../../admin/jsp/icons/1px.gif"></td>
            <td colspan="2" rowspan="2"><img src="../../admin/jsp/icons/accueil/angle_bd.gif" width="10" height="10"></td>
          </tr>
          <tr>
            <td bgcolor=2776A3><img src="../../admin/jsp/icons/1px.gif" width="1" height="1"></td>
            <td bgcolor=2776A3><img src="../../admin/jsp/icons/1px.gif"></td>
          </tr>
        </table>
</CENTER>
<%
out.println(frame.printMiddle());
out.println(frame.printAfter());
%>
</TD></TR></TABLE>
<p>&nbsp;</p>
</body>
</html>
