<%@ page import="com.stratelia.silverpeas.clipboardPeas.control.ClipboardSessionController"%>
<%@ page import="org.silverpeas.util.MultiSilverpeasBundle"%>
<%@ page import="org.silverpeas.util.ResourceLocator"%>
<%@ page import="org.silverpeas.util.viewGenerator.html.GraphicElementFactory" %>
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
