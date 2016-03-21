<%@ page import="com.stratelia.silverpeas.peasCore.URLManager"%>
<%@ page import="org.silverpeas.core.web.tools.agenda.control.AgendaSessionController"%>
<%@ page import="org.silverpeas.core.web.tools.agenda.control.AgendaUserException"%>
<%@ page import="org.silverpeas.core.web.tools.agenda.view.AgendaHtmlView"%>
<%@ page import="org.silverpeas.core.calendar.model.Attendee"%>
<%@ page import="org.silverpeas.core.calendar.model.Category"%>
<%@ page import="org.silverpeas.core.calendar.model.Classification"%>
<%@ page import="org.silverpeas.core.calendar.model.JournalHeader"%>
<%@ page import="org.silverpeas.core.calendar.model.ParticipationStatus"%>
<%@ page import="org.silverpeas.core.calendar.model.Priority"%>
<%@ page import="org.silverpeas.util.DBUtil"%>
<%@ page import="org.silverpeas.util.DateUtil"%>
<%@ page import="org.silverpeas.util.MultiSilverpeasBundle"%>
<%@ page import="org.silverpeas.util.ResourceLocator"%>
<%@ page import="org.silverpeas.util.SettingBundle"%>
<%@ page import="org.silverpeas.util.StringUtil"%>
<%@ page import="org.silverpeas.core.web.util.viewGenerator.html.Encode"%>
<%@ page import="org.silverpeas.core.web.util.viewGenerator.html.GraphicElementFactory"%>

<%@ page import="org.silverpeas.core.web.util.viewGenerator.html.board.Board"%>
<%@ page import="org.silverpeas.core.web.util.viewGenerator.html.browseBars.BrowseBar"%>
<%@ page import="org.silverpeas.core.web.util.viewGenerator.html.buttonPanes.ButtonPane"%>
<%@ page import="org.silverpeas.core.web.util.viewGenerator.html.buttons.Button"%>
<%@ page import="org.silverpeas.core.web.util.viewGenerator.html.frame.Frame" %>
<%@ page import="org.silverpeas.core.web.util.viewGenerator.html.operationPanes.OperationPane" %>
<%@ page import="org.silverpeas.core.web.util.viewGenerator.html.tabs.TabbedPane" %>
<%@ page import="org.silverpeas.core.web.util.viewGenerator.html.window.Window" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="java.util.Calendar" %>
<%@ page import="java.util.Collection" %>
<%@ page import="java.util.Date" %>
<%@ page import="java.util.Iterator" %>
<%@ page import="java.util.List" %>

<%!
String m_context = URLManager.getApplicationURL();

// Icones operationBar
String agendaAddSrc         = m_context + "/util/icons/create-action/add-event.png";
String agendaDelSrc         = m_context + "/util/icons/agenda_to_del.gif";
String agendaChronoSrc      = m_context + "/util/icons/agenda_to_viewTime.gif";
String agendaAssignmentSrc  = m_context + "/util/icons/agenda_assignment.gif";
String agendaCategorySrc    = m_context + "/util/icons/agenda_to_categorize.gif";
String viewOtherAgenda				  = "icons/viewAgenda.gif";
String viewCurrentAgenda		  = "icons/viewCurrentAgenda.gif";
String importSettingsSrc	= "icons/importSettings.gif";
String importIcalSrc	=  m_context + "/util/icons/create-action/import-ical.png";
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
  MultiSilverpeasBundle 			resources 		= (MultiSilverpeasBundle)request.getAttribute("resources");

  if (agenda == null)
  {
    String sessionTimeout = ResourceLocator.getGeneralSettingBundle().getString("sessionTimeout");
    getServletConfig().getServletContext().getRequestDispatcher(sessionTimeout).forward(request, response);
    return;
  }

  Board board = graphicFactory.getBoard();
%>
<%@ page errorPage="../../admin/jsp/errorpage.jsp"%>