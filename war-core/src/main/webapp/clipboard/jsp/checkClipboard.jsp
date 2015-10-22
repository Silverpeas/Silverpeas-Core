<%@ page import="com.stratelia.silverpeas.clipboardPeas.control.*"%>
<%@ page import="org.silverpeas.util.viewGenerator.html.*"%>
<%@ page import="org.silverpeas.util.MultiSilverpeasBundle"%>
<%@ page import="static org.silverpeas.util.ResourceLocator.*" %>
<%@ page errorPage="../../admin/jsp/errorpage.jsp"%>

<%
	GraphicElementFactory graphicFactory = (GraphicElementFactory) session.getAttribute("SessionGraphicElementFactory");
	ClipboardSessionController clipboardSC = (ClipboardSessionController) request.getAttribute("clipboardScc");

	if (clipboardSC == null)
	{
	    // No quickinfo session controller in the request -> security exception
	    String sessionTimeout = getGeneralSettingBundle().getString("sessionTimeout");
	    getServletConfig().getServletContext().getRequestDispatcher(sessionTimeout).forward(request, response);
	    return;
	}
	
	String m_context = getGeneralSettingBundle().getString("ApplicationURL");
	MultiSilverpeasBundle resources = (MultiSilverpeasBundle)request.getAttribute("resources");
%>
