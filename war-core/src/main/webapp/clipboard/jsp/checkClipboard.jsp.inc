<%@ page import="com.stratelia.silverpeas.clipboardPeas.control.*"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.*"%>
<%@ page import="com.stratelia.webactiv.util.GeneralPropertiesManager"%>
<%@ page import="com.stratelia.silverpeas.util.ResourcesWrapper"%>
<%@ page errorPage="../../admin/jsp/errorpage.jsp"%>

<%
	GraphicElementFactory graphicFactory = (GraphicElementFactory) session.getAttribute("SessionGraphicElementFactory");
	ClipboardSessionController clipboardSC = (ClipboardSessionController) request.getAttribute("clipboardScc");

	if (clipboardSC == null)
	{
	    // No quickinfo session controller in the request -> security exception
	    String sessionTimeout = GeneralPropertiesManager.getGeneralResourceLocator().getString("sessionTimeout");
	    getServletConfig().getServletContext().getRequestDispatcher(sessionTimeout).forward(request, response);
	    return;
	}
	
	String m_context = GeneralPropertiesManager.getGeneralResourceLocator().getString("ApplicationURL");
	ResourcesWrapper resources = (ResourcesWrapper)request.getAttribute("resources");
%>
