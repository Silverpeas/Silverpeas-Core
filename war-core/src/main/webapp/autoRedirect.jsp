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
	response.setHeader( "Expires", "Tue, 21 Dec 1993 23:59:59 GMT" );
	response.setHeader( "Pragma", "no-cache" );
	response.setHeader( "Cache-control", "no-cache" );
	response.setHeader( "Last-Modified", "Fri, Jan 25 2099 23:59:59 GMT" );
	response.setStatus( HttpServletResponse.SC_CREATED );
%>
<%@ page import="com.stratelia.silverpeas.peasCore.MainSessionController"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.GraphicElementFactory"%>
<%@ page import="com.stratelia.webactiv.util.GeneralPropertiesManager"%>
<%@ page import="com.stratelia.silverpeas.peasCore.URLManager"%>
<%@ page import="com.stratelia.silverpeas.silvertrace.SilverTrace" %>

<%
String			strGoTo			= request.getParameter("goto");
String			domainId		= request.getParameter("domainId");
String			componentGoTo	= request.getParameter("ComponentId");
String			spaceGoTo		= request.getParameter("SpaceId");
String			attachmentGoTo	= request.getParameter("AttachmentId");
HttpSession 	mySession 		= request.getSession();

String mainFrameParams 	= "";
String componentId 		= null;
String spaceId			= null;
if (strGoTo != null)
{
	mySession.setAttribute("gotoNew", strGoTo);

	//System.out.println("strGoTo = "+strGoTo);

	//deux cas, deux parsing diff�rents :
	//1 - commence par /RpdcSearch --> PDC (moteur de recherche)
	//2 - commence par /R****/kmelia124/searchResult.jsp?... --> composants (moteur de recherche || notifications)

	String urlToParse = strGoTo;
	if (strGoTo.startsWith("/RpdcSearch/"))
	{
		int indexOf = urlToParse.indexOf("&componentId=");
		componentId = urlToParse.substring(indexOf+13, urlToParse.length());
	}
	else
	{
		urlToParse = urlToParse.substring(1); //remove first "/"
		int indexBegin 	= urlToParse.indexOf("/")+1;
		int indexEnd 	= urlToParse.indexOf("/", indexBegin);
		componentId = urlToParse.substring(indexBegin, indexEnd);
		//Agenda
		if (strGoTo.startsWith("/Ragenda/"))
		{
			componentId = null;
		}
	}
	//System.out.println("componentId = "+componentId);

	mainFrameParams = "?ComponentIdFromRedirect="+componentId;
	mySession.setAttribute("RedirectToComponentId", componentId);
}
else if (componentGoTo != null)
{
	componentId = componentGoTo;
	mainFrameParams = "?ComponentIdFromRedirect="+componentId;
	mySession.setAttribute("RedirectToComponentId", componentId);
	if (attachmentGoTo != null)
	{
		String foreignId = request.getParameter("ForeignId");
		String type		 = request.getParameter("Mapping");

		//Contruit l'url vers l'objet du composant contenant le fichier
		strGoTo = URLManager.getURL(null, componentId)+"searchResult?Type=Publication&Id="+foreignId;
		mySession.setAttribute("gotoNew", strGoTo);

		//Ajoute l'id de l'attachment pour ouverture automatique
		mySession.setAttribute("RedirectToAttachmentId", attachmentGoTo);
		mySession.setAttribute("RedirectToMapping", type);
	}
}
else if (spaceGoTo != null)
{
	spaceId = spaceGoTo;
	mySession.setAttribute("RedirectToSpaceId", spaceId);
}

SilverTrace.info("authentication", "autoRedirect.jsp", "root.MSG_GEN_PARAM_VALUE", "componentId = "+componentId+", spaceId = "+spaceId);

MainSessionController	m_MainSessionCtrl	= (MainSessionController) session.getAttribute("SilverSessionController");
GraphicElementFactory 	gef 				= (GraphicElementFactory) session.getAttribute("SessionGraphicElementFactory");

//L'utilisateur n'est pas connect� ou est connect� en anonyme. Il retourne � la page de login.
if (m_MainSessionCtrl == null || (gef != null && gef.getFavoriteLookSettings().getString("guestId").equals(m_MainSessionCtrl.getUserId())))
{
%>
	<script>
		top.location="Login.jsp?DomainId="+<%=domainId%>;
	</script>
<%
}
else
{
	//Il retourne � la page de login s'il n'est pas autoris� sur le composant cible.
	if ((componentId != null && !m_MainSessionCtrl.getOrganizationController().isComponentAvailable(componentId, m_MainSessionCtrl.getUserId()))
		|| (spaceId != null && !m_MainSessionCtrl.getOrganizationController().isSpaceAvailable(spaceId, m_MainSessionCtrl.getUserId())))
	{
		response.sendRedirect(GeneralPropertiesManager.getGeneralResourceLocator().getString("ApplicationURL")+"/admin/jsp/accessForbidden.jsp");
	}
	else if (m_MainSessionCtrl.isAppInMaintenance() && !m_MainSessionCtrl.getCurrentUserDetail().isAccessAdmin())
	{
    %>
        <script>
        	top.location="admin/jsp/appInMaintenance.jsp";
        </script>
    <%
  	}
  	else
    {
    %>
        <script>
        	top.location="admin/jsp/<%=gef.getLookFrame()%><%=mainFrameParams%>";
        </script>
    <%
    }
}
%>