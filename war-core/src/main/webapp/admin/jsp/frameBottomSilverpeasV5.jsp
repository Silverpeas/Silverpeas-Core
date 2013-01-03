<%--

    Copyright (C) 2000 - 2012 Silverpeas

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

<%@ page import="com.stratelia.webactiv.beans.admin.SpaceInstLight"%>
<%@ page import="com.silverpeas.look.LookSilverpeasV5Helper"%>
<%@ include file="importFrameSet.jsp" %>

<%
String strGoToNew 	= (String) session.getAttribute("gotoNew");
String spaceId 		= request.getParameter("SpaceId");
String subSpaceId 	= request.getParameter("SubSpaceId");
String componentId	= request.getParameter("ComponentId");
String login		= request.getParameter("Login");

LookSilverpeasV5Helper 	helper = (LookSilverpeasV5Helper) session.getAttribute(LookSilverpeasV5Helper.SESSION_ATT);

ResourceLocator rsc = gef.getFavoriteLookSettings();
int framesetWidth = Integer.parseInt(rsc.getString("domainsBarFramesetWidth"));

String paramsForDomainsBar = "";
if ("1".equals(request.getParameter("FromTopBar"))) {
	paramsForDomainsBar = (spaceId == null) ? "" : "?privateDomain="+spaceId+"&privateSubDomain="+subSpaceId+"&FromTopBar=1";
}  else if (componentId != null)  {
	paramsForDomainsBar = "?privateDomain=&component_id="+componentId;
} else {
	paramsForDomainsBar = "?privateDomain="+spaceId;
}

//Allow to force a page only on login and when user clicks on logo
boolean displayLoginHomepage = false;
String loginHomepage = rsc.getString("loginHomepage");
if (StringUtil.isDefined(loginHomepage) && StringUtil.isDefined(login) && 
    (!StringUtil.isDefined(spaceId) && !StringUtil.isDefined(subSpaceId) && !StringUtil.isDefined(componentId) && !StringUtil.isDefined(strGoToNew))) {
	displayLoginHomepage = true;
}

String frameURL = "";
if (displayLoginHomepage) {
	frameURL = loginHomepage;
} else if (strGoToNew == null) {
	if (StringUtil.isDefined(componentId)) {
		frameURL = URLManager.getApplicationURL()+URLManager.getURL(null, componentId)+"Main";
	} else {
		String homePage = rsc.getString("defaultHomepage", "/dt");
		String param = "";
		if (StringUtil.isDefined(spaceId)) {
		    param = "?SpaceId=" + spaceId;
		}
		frameURL = URLManager.getApplicationURL()+homePage+param;
	}
} else {
    frameURL = URLManager.getApplicationURL()+strGoToNew;
    if(strGoToNew.startsWith(URLManager.getApplicationURL())) {
      frameURL = strGoToNew;
    }	
} 

session.removeAttribute("goto");
session.removeAttribute("gotoNew");
session.removeAttribute("RedirectToComponentId");
session.removeAttribute("RedirectToSpaceId");
%>


<%@page import="com.silverpeas.util.StringUtil"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Frameset//EN"
    "http://www.w3.org/TR/xhtml1/DTD/xhtml1-frameset.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<title></title>
<script type="text/javascript">
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

function init() {
	if (!document.all && !document.getElementById) 
    return
	if (document.body != null){
		columntype=(document.body.cols)? "cols" : "rows"
		defaultsetting=(document.body.cols)? document.body.cols : document.body.rows
	} 
	else
		setTimeout("init()",100)
}

setTimeout("init()",100);

</script>
<script type="text/javascript">
<!--
function MM_reloadPage(init) {  //reloads the window if Nav4 resized
  if (init==true) with (navigator) {if ((appName=="Netscape")&&(parseInt(appVersion)==4)) {
    document.MM_pgW=innerWidth; document.MM_pgH=innerHeight; onresize=MM_reloadPage; }}
  else if (innerWidth!=document.MM_pgW || innerHeight!=document.MM_pgH) location.reload();
}
MM_reloadPage(true);
//-->
</script>
</head>
	<frameset cols="<%=framesetWidth%>,*">
		<frame src="DomainsBarSilverpeasV5.jsp<%=paramsForDomainsBar%>" marginwidth="0" marginheight="10" name="SpacesBar" frameborder="0" scrolling="auto"/>
		<frame src="<%=frameURL%>" marginwidth="0" name="MyMain" marginheight="0" frameborder="0" scrolling="auto"/>
		<noframes>
			<body>Votre navigateur ne prend pas en charge les frames</body>
		</noframes>
  	</frameset>
</html>