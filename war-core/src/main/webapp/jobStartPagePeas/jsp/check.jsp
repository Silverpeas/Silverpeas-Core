<%
response.setHeader("Cache-Control","no-store"); //HTTP 1.1
response.setHeader("Pragma","no-cache");        //HTTP 1.0
response.setDateHeader ("Expires",-1);          //prevents caching at the proxy server
%>

<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.*"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.window.Window"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.operationPanes.OperationPane"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.browseBars.BrowseBar"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.frame.Frame"%>
<%@ page import="com.stratelia.silverpeas.util.ResourcesWrapper"%>

<%// En fonction de ce dont vous avez besoin %>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.arrayPanes.ArrayPane"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.arrayPanes.ArrayLine"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.arrayPanes.ArrayColumn"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.arrayPanes.ArrayCellText"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.arrayPanes.ArrayCellLink"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.arrayPanes.*"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.buttonPanes.ButtonPane"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.buttons.Button"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.board.Board"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.tabs.*"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.icons.*"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.iconPanes.*"%>

<%@ page import="com.stratelia.webactiv.util.*"%>
<%@ page import="com.stratelia.webactiv.util.ResourceLocator"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.GraphicElementFactory"%>
<%@ page import="com.stratelia.silverpeas.peasCore.URLManager"%>
<%@ page import="com.stratelia.webactiv.beans.admin.instance.control.*"%>
<%@ page import="com.stratelia.webactiv.beans.admin.spaceTemplates.*"%>
<%@ page import="com.stratelia.webactiv.beans.admin.*"%>

<%@ page import="com.stratelia.webactiv.util.GeneralPropertiesManager"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.Encode"%>
<%@ page import="com.stratelia.webactiv.util.ResourceLocator"%>
<%@ page import="com.silverpeas.jobStartPagePeas.*"%>
<%@ page import="com.silverpeas.jobStartPagePeas.control.JobStartPagePeasSessionController"%>
<%@ page import="java.util.*"%>

<%@ page import="com.silverpeas.util.i18n.*"%>
<%@ page import="com.silverpeas.util.StringUtil"%>

<%@ page errorPage="../../admin/jsp/errorpageMain.jsp"%>

<%
GraphicElementFactory gef = (GraphicElementFactory) session.getAttribute("SessionGraphicElementFactory");
JobStartPagePeasSessionController jobStartPageSC = (JobStartPagePeasSessionController) request.getAttribute("jobStartPageSC");

String iconsPath = GeneralPropertiesManager.getGeneralResourceLocator().getString("ApplicationURL");
String m_context = iconsPath;

Boolean haveToRefreshNavBar = (Boolean)request.getAttribute("haveToRefreshNavBar");

ResourcesWrapper resource = (ResourcesWrapper)request.getAttribute("resources");
Window window = gef.getWindow();
BrowseBar browseBar = window.getBrowseBar();
OperationPane operationPane = window.getOperationPane();
Frame frame = gef.getFrame();
Board board = gef.getBoard();
%>
<script language="JavaScript1.2">
function refreshNavBar(){
    if (window.name == "startPageContent")
    {
        window.parent.startPageNavigation.location.href="jobStartPageNav";
    }
    else if (window.name == "IdleFrame")
    {
			parent.frames["bottomFrame"].frames["startPageNavigation"].location.href="<%=m_context%>/RjobStartPagePeas/jsp/jobStartPageNav";
		}
    else
    {
        window.opener.parent.startPageNavigation.location.href="jobStartPageNav";
    }
}
<%  if ((haveToRefreshNavBar != null) && (haveToRefreshNavBar.booleanValue()))
    {
%>
refreshNavBar();
<%  
    }
%>
</SCRIPT>
