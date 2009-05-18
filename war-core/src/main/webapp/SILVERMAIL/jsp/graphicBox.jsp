<%@ page import="java.util.*"%>
<%@ page import="com.stratelia.webactiv.util.*"%>

<%//____/ VIEW GENERATOR \_________________________________________________________________________%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.GraphicElementFactory"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.buttons.*"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.buttonPanes.*"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.icons.*"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.iconPanes.*"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.arrayPanes.*"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.tabs.*"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.operationPanes.OperationPane"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.browseBars.BrowseBar"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.window.Window"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.navigationList.NavigationList"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.frame.Frame"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.monthCalendar.*"%>

<%
// Ze graffik factory
GraphicElementFactory gef = (GraphicElementFactory) session.getAttribute("SessionGraphicElementFactory");

String graphicPath        = GeneralPropertiesManager.getGeneralResourceLocator().getString("ApplicationURL");
String m_context          = graphicPath;

// Icones operationBar
String addNotif           = graphicPath + "/util/icons/newNotification.gif";
String deleteAllNotif     = graphicPath + "/util/icons/deleteAllNotif.gif";
String paramNotif         = graphicPath + "/util/icons/confServer.gif";

// Icones diverses
String delete             = graphicPath + "/util/icons/delete.gif";
String modif              = graphicPath + "/util/icons/update.gif";
String up                 = graphicPath + "/util/icons/arrow/arrowUp.gif";
String down               = graphicPath + "/util/icons/arrow/arrowDown.gif";
String mandatoryField     = graphicPath + "/util/icons/squareRed.gif";
String test               = graphicPath + "/util/icons/ok.gif";

// Pixels
String noColorPix         = graphicPath + "/util/icons/colorPix/1px.gif";

// Divers
String separator = "<TABLE CELLPADDING=2 CELLSPACING=0 BORDER=0><TR><TD><img src="+noColorPix+"></TD></TR></TABLE>";
%>