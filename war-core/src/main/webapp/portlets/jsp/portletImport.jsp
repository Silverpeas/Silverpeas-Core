<%@ page import="javax.servlet.*"%>
<%@ page import="javax.servlet.http.*"%>
<%@ page import="javax.servlet.jsp.*"%>
<%@ page import="java.io.PrintWriter"%>
<%@ page import="java.io.IOException"%>
<%@ page import="java.io.FileInputStream"%>
<%@ page import="java.io.ObjectInputStream"%>
<%@ page import="java.beans.*"%>
<%@ page import="java.lang.String"%>
<%@ page import="java.util.*"%>

<%@ page import="javax.portlet.RenderRequest" %>
<%@ page import="javax.portlet.RenderResponse" %>
<%@ page import="javax.portlet.PortletPreferences" %>
<%@ page import="javax.portlet.PortletURL" %>
<%@ page import="javax.portlet.WindowState"%>

<%@ page import="com.stratelia.webactiv.util.*"%>
<%@ page import="com.stratelia.webactiv.homepage.*"%>
<%@ page import="com.stratelia.webactiv.util.ResourceLocator"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.GraphicElementFactory"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.Encode"%>

<%@ page import="com.stratelia.silverpeas.peasCore.URLManager"%>
<%@ page import="com.stratelia.silverpeas.peasCore.MainSessionController"%>
<%@ page import="com.stratelia.webactiv.beans.admin.OrganizationController"%>

<%@ page import="com.silverpeas.portlets.FormNames" %>

<%@ page import="com.stratelia.webactiv.util.DateUtil"%>
<%@ page import="com.silverpeas.util.StringUtil"%>

<%
MainSessionController 	m_MainSessionCtrl 	= (MainSessionController) session.getAttribute("SilverSessionController");
if (m_MainSessionCtrl == null)
{	
  String sessionTimeout = GeneralPropertiesManager.getGeneralResourceLocator().getString("sessionTimeout");
  getServletConfig().getServletContext().getRequestDispatcher(sessionTimeout).forward(request, response);
  return;
}

GraphicElementFactory 	gef 				= (GraphicElementFactory) session.getAttribute("SessionGraphicElementFactory");
String 					language 			= m_MainSessionCtrl.getFavoriteLanguage();
ResourceLocator 		message 			= new ResourceLocator("com.stratelia.webactiv.homePage.multilang.homePageBundle", language);
ResourceLocator 		homePageSettings 	= new ResourceLocator("com.stratelia.webactiv.homePage.homePageSettings", "");
String 					m_sContext 			= GeneralPropertiesManager.getGeneralResourceLocator().getString("ApplicationURL");
%>