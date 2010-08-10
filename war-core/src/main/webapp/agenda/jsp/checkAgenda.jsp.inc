<%@ page import="com.stratelia.silverpeas.util.ResourcesWrapper"%>
<%@ page import="com.stratelia.webactiv.agenda.control.*"%>
<%@ page import="java.util.*"%>
<%@ page import="java.io.*"%>
<%@ page import="com.stratelia.webactiv.agenda.view.*"%>
<%@ page import="com.stratelia.webactiv.agenda.model.*"%>
<%@ page import="com.stratelia.webactiv.calendar.model.*"%>
<%@ page import="com.stratelia.webactiv.util.*"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.*"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.tabs.*"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.window.*"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.browseBars.*"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.operationPanes.*"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.frame.*"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.buttons.*"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.buttonPanes.ButtonPane"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.tabs.TabbedPane"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.board.*"%>

<%@ page import="com.stratelia.silverpeas.peasCore.URLManager"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.Encode"%>
<%@ page import="com.silverpeas.util.StringUtil"%>
<%@ page import="com.stratelia.webactiv.util.FileRepositoryManager"%>
<%@ page import="com.stratelia.webactiv.servlets.FileServer"%>

<%!
String m_context         	= GeneralPropertiesManager.getGeneralResourceLocator().getString("ApplicationURL");

// Icones operationBar
String agendaAddSrc         = m_context + "/util/icons/agenda_to_add.gif";
String agendaDelSrc         = m_context + "/util/icons/agenda_to_del.gif";
String agendaChronoSrc      = m_context + "/util/icons/agenda_to_viewTime.gif";
String agendaAssignmentSrc  = m_context + "/util/icons/agenda_assignment.gif";
String agendaCategorySrc    = m_context + "/util/icons/agenda_to_categorize.gif";
String viewOtherAgenda				  = "icons/viewAgenda.gif";
String viewCurrentAgenda		  = "icons/viewCurrentAgenda.gif";
String importSettingsSrc	= "icons/importSettings.gif";
String importIcalSrc	= "icons/importIcal.gif";
String exportIcalSrc	= "icons/exportIcal.gif";
String synchroIcalSrc	= "icons/synchroIcal.gif";

// Pixels
String orangePix            = m_context + "/util/icons/colorPix/orange.gif";
String noColorPix           = m_context + "/util/icons/colorPix/1px.gif";
String separator            = "<table cellpadding=\"2\" cellspacing=\"0\" border=\"0\"><tr><td><img src=\""+noColorPix+"\" alt=\"\"/></td></tr></table>";

// Divers
String btOk                 = m_context + "/util/icons/almanach_ok.gif";
String arrRight				= m_context + "/util/icons/arrow/arrowRight.gif";
String arrLeft				= m_context + "/util/icons/arrow/arrowLeft.gif";
%>
<%
  GraphicElementFactory 	graphicFactory 	= (GraphicElementFactory) session.getAttribute("SessionGraphicElementFactory");
  AgendaSessionController 	agenda 			= (AgendaSessionController) request.getAttribute("agenda");
  ResourcesWrapper 			resources 		= (ResourcesWrapper)request.getAttribute("resources");

  if (agenda == null)
  {
    String sessionTimeout = GeneralPropertiesManager.getGeneralResourceLocator().getString("sessionTimeout");
    getServletConfig().getServletContext().getRequestDispatcher(sessionTimeout).forward(request, response);
    return;
  }
  
  Board board = graphicFactory.getBoard();
%>
<%@ page errorPage="../../admin/jsp/errorpage.jsp"%>