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
HttpSession 	Session 		= request.getSession();

String mainFrameParams 	= "";
String componentId 		= null;
String spaceId			= null;
if (strGoTo != null)
{
	Session.putValue("gotoNew", strGoTo);
	
	//System.out.println("strGoTo = "+strGoTo);

	//deux cas, deux parsing différents :
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
		int indexEnd 	= urlToParse.lastIndexOf("/");
		componentId = urlToParse.substring(indexBegin, indexEnd);
		//Agenda
		if (strGoTo.startsWith("/Ragenda/"))
		{
			componentId = null;
		}
	}
	//System.out.println("componentId = "+componentId);
	
	mainFrameParams = "?ComponentIdFromRedirect="+componentId;
	Session.putValue("RedirectToComponentId", componentId);
}
else if (componentGoTo != null)
{
	componentId = componentGoTo;
	mainFrameParams = "?ComponentIdFromRedirect="+componentId;
	Session.putValue("RedirectToComponentId", componentId);
	if (attachmentGoTo != null)
	{
		String foreignId = (String) request.getParameter("ForeignId");
		
		//Contruit l'url vers l'objet du composant contenant le fichier
		strGoTo = URLManager.getURL(null, componentId)+"searchResult?Type=Publication&Id="+foreignId;
		Session.putValue("gotoNew", strGoTo);
		
		//Ajoute l'id de l'attachment pour ouverture automatique
		Session.putValue("RedirectToAttachmentId", attachmentGoTo);
	}
}
else if (spaceGoTo != null)
{
	spaceId = spaceGoTo;
	Session.putValue("RedirectToSpaceId", spaceId);
}

SilverTrace.info("authentication", "autoRedirect.jsp", "root.MSG_GEN_PARAM_VALUE", "componentId = "+componentId+", spaceId = "+spaceId);

MainSessionController	m_MainSessionCtrl	= (MainSessionController) session.getAttribute("SilverSessionController");
GraphicElementFactory 	gef 				= (GraphicElementFactory) session.getAttribute("SessionGraphicElementFactory");

//L'utilisateur n'est pas connecté ou est connecté en anonyme. Il retourne à la page de login.
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
	//Il retourne à la page de login s'il n'est pas autorisé sur le composant cible.
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