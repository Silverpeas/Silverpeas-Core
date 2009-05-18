<%
response.setHeader( "Expires", "Tue, 21 Dec 1993 23:59:59 GMT" );
response.setHeader( "Pragma", "no-cache" );
response.setHeader( "Cache-control", "no-cache" );
response.setHeader( "Last-Modified", "Fri, Jan 25 2099 23:59:59 GMT" );
%>

<%@ page import="com.stratelia.webactiv.util.GeneralPropertiesManager,
                 com.stratelia.silverpeas.peasCore.MainSessionController,
                 com.stratelia.webactiv.util.ResourceLocator"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.GraphicElementFactory "%>
<%@ page import="com.stratelia.silverpeas.peasCore.URLManager"%>
<%@ page import="com.stratelia.silverpeas.comment.model.CommentPK,
                 com.stratelia.silverpeas.comment.control.CommentController"%>
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
ResourceLocator 		messages 			= new ResourceLocator("com.stratelia.webactiv.util.comment.multilang.comment", language);
ResourceLocator 		generalMessage 		= GeneralPropertiesManager.getGeneralMultilang(language);

%>