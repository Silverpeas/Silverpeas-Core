<%--

    Copyright (C) 2000 - 2013 Silverpeas

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as
    published by the Free Software Foundation, either version 3 of the
    License, or (at your option) any later version.

    As a special exception to the terms and conditions of version 3.0 of
    the GPL, you may redistribute this Program in connection with Free/Libre
    Open Source Software ("FLOSS") applications as described in Silverpeas's
    FLOSS exception.  You should have received a copy of the text describing
    the FLOSS exception, and it is also available here:
    "http://www.silverpeas.org/docs/core/legal/floss_exception.html"

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.

--%>

<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<%
	response.setHeader( "Expires", "Tue, 21 Dec 1993 23:59:59 GMT" );
	response.setHeader( "Pragma", "no-cache" );
	response.setHeader( "Cache-control", "no-cache" );
	response.setHeader( "Last-Modified", "Fri, Jan 25 2099 23:59:59 GMT" );
	response.setStatus( HttpServletResponse.SC_CREATED );
%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>
<%@ include file="importFrameSet.jsp" %>
<%@ page import="org.silverpeas.core.web.look.LookHelper"%>
<%@ page import="org.silverpeas.core.util.StringUtil" %>
<%@ page import="org.silverpeas.core.util.ResourceLocator" %>
<%@ page import="org.silverpeas.core.util.LocalizationBundle" %>

<%
String			componentIdFromRedirect = (String) session.getAttribute("RedirectToComponentId");
String			spaceIdFromRedirect 	= (String) session.getAttribute("RedirectToSpaceId");
if (!StringUtil.isDefined(spaceIdFromRedirect)) {
	spaceIdFromRedirect 	= request.getParameter("RedirectToSpaceId");
}
String			attachmentId		 	= (String) session.getAttribute("RedirectToAttachmentId");
LocalizationBundle generalMessage			= ResourceLocator.getGeneralLocalizationBundle(language);
String			topBarParams			= "";
String			frameBottomParams		= "";
boolean			login					= StringUtil.getBooleanValue(request.getParameter("Login"));

if (m_MainSessionCtrl == null) {
%>
	<script type="text/javascript">
		top.location="../../Login.jsp";
	</script>
<%
} else {
	LookHelper 	helper 	= LookHelper.getLookHelper(session);
	if (helper == null) {
		helper = LookHelper.newLookHelper(session);
		helper.setMainFrame("MainFrameSilverpeasV5.jsp");
		login = true;
	}

	boolean componentExists = false;
	if (StringUtil.isDefined(componentIdFromRedirect)) {
		componentExists = (organizationCtrl.getComponentInstLight(componentIdFromRedirect) != null);
	}

	if (!componentExists) {
		String spaceId = helper.getDefaultSpaceId();
		boolean spaceExists = false;
		if (StringUtil.isDefined(spaceIdFromRedirect)) {
			spaceExists = (organizationCtrl.getSpaceInstById(spaceIdFromRedirect) != null);
		}

		if (spaceExists) {
			spaceId = spaceIdFromRedirect;
		} else {
			if (helper != null && helper.getSpaceId() != null) {
				spaceId = helper.getSpaceId();
			}
		}
		helper.setSpaceIdAndSubSpaceId(spaceId);

		frameBottomParams 	= "?SpaceId="+spaceId;
	} else {
		helper.setComponentIdAndSpaceIds(null, null, componentIdFromRedirect);
		frameBottomParams 	= "?SpaceId=&ComponentId="+componentIdFromRedirect;
	}

	gef.setSpaceIdForCurrentRequest(helper.getSubSpaceId());

	if (login) {
		frameBottomParams += "&amp;Login=1";
	}

	if (!"MainFrameSilverpeasV5.jsp".equalsIgnoreCase(helper.getMainFrame())
    && ! "/admin/jsp/MainFrameSilverpeasV5.jsp".equalsIgnoreCase(helper.getMainFrame())) {
		session.setAttribute("RedirectToSpaceId", spaceIdFromRedirect);
	String topLocation = gef.getLookFrame();
    if(!topLocation.startsWith("/")) {
      topLocation = "/admin/jsp/" + topLocation;
    }
		%>
			<c:set var="topLocation"><%=topLocation%></c:set>
			<script type="text/javascript">
				top.location="<c:url value="${topLocation}" />";
			</script>
		<%
	}

	String bannerHeight = helper.getSettings("bannerHeight", "115");
	String footerHeight = helper.getSettings("footerHeight", "26");
	String framesetRows = bannerHeight+",100%,*,*,*";
	if (helper.displayPDCFrame()) {
      framesetRows = bannerHeight+",100%,"+footerHeight+",*,*,*";
	}
%>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Frameset//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-frameset.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<title><%=generalMessage.getString("GML.popupTitle")%></title>
<link rel="SHORTCUT ICON" href="<%=request.getContextPath()%>/util/icons/favicon.ico"/>
<script type="text/javascript" src="<%=m_sContext%>/util/javaScript/silverpeas.js"></script>
<script type="text/javascript">
<!--
var columntype=""
var defaultsetting=""

function getCurrentSetting(){
	if (document.body)
		return (document.body.cols)? document.body.cols : document.body.rows
}

function setframevalue(coltype, settingvalue){
	if (coltype=="rows")
		document.body.rows=settingvalue
	else if (coltype=="cols")
		document.body.cols=settingvalue
}

function resizeFrame(contractsetting){
	if (getCurrentSetting()!=defaultsetting)
		setframevalue(columntype, defaultsetting)
	else
		setframevalue(columntype, contractsetting)
}

function init(){
	if (!document.all && !document.getElementById) return
	if (document.body!=null){
		columntype=(document.body.cols)? "cols" : "rows"
		defaultsetting=(document.body.cols)? document.body.cols : document.body.rows
	}
	else
		setTimeout("init()",100)
}

function showPdcFrame() {
	setframevalue(columntype, "<%=bannerHeight%>,100%,<%=footerHeight%>,*,*,*");
}

function hidePdcFrame() {
	setframevalue(columntype, "<%=bannerHeight%>,100%,*,*,*,*");
}

setTimeout("init()",100);

//-->
</script>
<style type="text/css">
/* Nettoyage des balises */
* {
margin: 0px;
padding: 0px;
border: none;
}
</style>
<meta name="viewport" content="initial-scale=1.0"/>
</head>
<% if (attachmentId != null) {
	session.setAttribute("RedirectToAttachmentId", null);
	String mapping = (String) session.getAttribute("RedirectToMapping");
%>
	<script type="text/javascript">
		SP_openWindow('<%=m_sContext%>/<%=mapping%>/<%=attachmentId%>', 'Fichier', '800', '600', 'directories=0,menubar=1,toolbar=1,scrollbars=1,location=1,alwaysRaised');
	</script>
<% } %>

<frameset rows="<%=framesetRows%>" id="mainFramesetId" border="0"> <!-- Do not remove frameset's attribute "border" -->
	<frame src="TopBarSilverpeasV5.jsp" name="topFrame" marginwidth="0" marginheight="0" scrolling="no" noresize="noresize" frameborder="0"/>
	<frame src="frameBottomSilverpeasV5.jsp<%=frameBottomParams%>" name="bottomFrame" marginwidth="0" marginheight="0" scrolling="no" noresize="noresize" frameborder="0"/>
	<% if (helper.displayPDCFrame()) { %>
		<!--  Content of this frame is processed when DomainsBar is initialized -->
		<frame src="" name="pdcFrame" marginwidth="0" marginheight="0" scrolling="no" noresize="noresize" frameborder="0"/>
	<% } %>
	<frame src="../../clipboard/jsp/IdleSilverpeasV5.jsp" name="IdleFrame" marginwidth="0" marginheight="0" scrolling="no" noresize="noresize" frameborder="0"/>
	<frame src="javascript.htm" name="scriptFrame" marginwidth="0" marginheight="0" scrolling="no" noresize="noresize" frameborder="0"/>
	<frame src="<%=m_sContext%>/Ragenda/jsp/importCalendar" name="importFrame" marginwidth="0" marginheight="0" scrolling="no" noresize="noresize" frameborder="0"/>
	<noframes>
		<body>Votre navigateur ne prend pas en charge les frames</body>
	</noframes>
</frameset>
</html>
<% } %>