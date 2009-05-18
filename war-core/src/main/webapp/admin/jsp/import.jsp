
<%@ page import="java.util.*"%>
<%@ page import="com.stratelia.webactiv.util.*"%>

<%//____/ VIEW GENERATOR \_________________________________________________________________________%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.GraphicElementFactory"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.buttons.*"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.buttonPanes.*"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.arrayPanes.*"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.operationPanes.OperationPane"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.browseBars.BrowseBar"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.window.Window"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.frame.Frame"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.board.Board"%>
<%@ page import="com.stratelia.silverpeas.peasCore.MainSessionController"%>

<%
// Ze graffik factory
GraphicElementFactory gef = (GraphicElementFactory) session.getAttribute("SessionGraphicElementFactory");

MainSessionController m_MainSessionCtrl = (MainSessionController) session.getAttribute("SilverSessionController");

String language = m_MainSessionCtrl.getFavoriteLanguage();
ResourceLocator generalMessage = GeneralPropertiesManager.getGeneralMultilang(language);

String  m_context                           = GeneralPropertiesManager.getGeneralResourceLocator().getString("ApplicationURL");


// Icones operationBar
String addNotif                           = m_context + "/util/icons/addEvent.gif";
String paramNotif                           = m_context + "/util/icons/confServer.gif";

// Icones diverses
String delete																	= m_context + "/util/icons/delete.gif";
String modif																	= m_context + "/util/icons/update.gif";
String up																			= m_context + "/util/icons/arrow/arrowUp.gif";
String down																		= m_context + "/util/icons/arrow/arrowDown.gif";
String mandatoryField													= m_context + "/util/icons/squareRed.gif";

// Pixels
String noColorPix                             = m_context + "/util/icons/colorPix/1px.gif";

// Divers
String separator = "<TABLE CELLPADDING=2 CELLSPACING=0 BORDER=0><TR><TD><img src="+noColorPix+"></TD></TR></TABLE>";
%>