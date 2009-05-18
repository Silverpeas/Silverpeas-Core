<%
response.setHeader( "Expires", "Tue, 21 Dec 1993 23:59:59 GMT" );
response.setHeader( "Pragma", "no-cache" );
response.setHeader( "Cache-control", "no-cache" );
response.setHeader( "Last-Modified", "Fri, Jan 25 2099 23:59:59 GMT" );
%>
<%@ page import="com.stratelia.silverpeas.util.ResourcesWrapper"%>
<%@ page import="com.stratelia.webactiv.util.GeneralPropertiesManager,
                 com.stratelia.silverpeas.peasCore.MainSessionController,
                 com.stratelia.webactiv.util.ResourceLocator"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.GraphicElementFactory "%>
<%@ page import="com.stratelia.silverpeas.peasCore.URLManager"%>
<%@ page import="com.stratelia.webactiv.util.DateUtil"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.arrayPanes.*"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.browseBars.BrowseBar"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.iconPanes.IconPane"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.icons.Icon"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.*"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.window.Window"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.frame.Frame"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.operationPanes.OperationPane"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.buttonPanes.ButtonPane"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.buttons.Button"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.board.Board"%>
<%@ page import="com.silverpeas.util.ForeignPK"%>
<%@ page import="java.util.Collection"%>
<%@ page import="java.util.Iterator"%>
<%@ page import="com.stratelia.webactiv.util.statistic.model.HistoryByUser"%>
<%@ page import="com.stratelia.webactiv.util.statistic.model.HistoryObjectDetail"%>
<%@ page import="com.stratelia.webactiv.util.statistic.control.StatisticBm"%>
<%@ page import="com.stratelia.webactiv.util.statistic.control.StatisticBmHome"%>
<%@ page import="com.stratelia.webactiv.util.EJBUtilitaire"%>
<%@ page import="com.stratelia.webactiv.util.JNDINames"%>
<%@ page import="com.stratelia.webactiv.util.DateUtil"%>

<%@ page import="java.net.*"%>

<%@ page errorPage="../../admin/jsp/errorpageMain.jsp"%>

<%

GraphicElementFactory gef = (GraphicElementFactory) session.getAttribute("SessionGraphicElementFactory");
String m_context = GeneralPropertiesManager.getGeneralResourceLocator().getString("ApplicationURL");

Window window = gef.getWindow();
Frame frame = gef.getFrame();
BrowseBar browseBar = window.getBrowseBar();
OperationPane operationPane = window.getOperationPane();

MainSessionController 	m_MainSessionCtrl 	= (MainSessionController) session.getAttribute("SilverSessionController");
String 					language 			= m_MainSessionCtrl.getFavoriteLanguage();
ResourceLocator 		messages 			= new ResourceLocator("com.silverpeas.statistic.multilang.statistic", language);
ResourceLocator 		generalMessage 		= GeneralPropertiesManager.getGeneralMultilang(language);
ResourcesWrapper 		resource 			= (ResourcesWrapper)request.getAttribute("resources");

%>