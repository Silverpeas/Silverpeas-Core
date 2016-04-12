<%@ page import="org.silverpeas.web.clipboard.control.ClipboardSessionController"%>
<%@ page import="org.silverpeas.core.util.MultiSilverpeasBundle"%>
<%@ page import="org.silverpeas.core.util.ResourceLocator"%>
<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.GraphicElementFactory" %>
<%@ page errorPage="../../admin/jsp/errorpage.jsp"%>

<%
	GraphicElementFactory graphicFactory = (GraphicElementFactory) session.getAttribute("SessionGraphicElementFactory");
	ClipboardSessionController clipboardSC = (ClipboardSessionController) request.getAttribute("clipboardScc");

	if (clipboardSC == null)
	{
	    // No quickinfo session controller in the request -> security exception
	    String sessionTimeout = ResourceLocator.getGeneralSettingBundle().getString("sessionTimeout");
	    getServletConfig().getServletContext().getRequestDispatcher(sessionTimeout).forward(request, response);
	    return;
	}

	String m_context = ResourceLocator.getGeneralSettingBundle().getString("ApplicationURL");
	MultiSilverpeasBundle resources = (MultiSilverpeasBundle)request.getAttribute("resources");
%>
