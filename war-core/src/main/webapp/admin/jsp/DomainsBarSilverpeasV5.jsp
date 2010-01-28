<%--

    Copyright (C) 2000 - 2009 Silverpeas

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as
    published by the Free Software Foundation, either version 3 of the
    License, or (at your option) any later version.

    As a special exception to the terms and conditions of version 3.0 of
    the GPL, you may redistribute this Program in connection with Free/Libre
    Open Source Software ("FLOSS") applications as described in Silverpeas's
    FLOSS exception.  You should have recieved a copy of the text describing
    the FLOSS exception, and it is also available here:
    "http://repository.silverpeas.com/legal/licensing"

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.

--%>
<%@ page import="java.util.*"%>
<%@ page import="com.silverpeas.util.StringUtil"%>
<%@ page import="com.stratelia.webactiv.util.*"%>
<%@ page import="com.stratelia.silverpeas.peasCore.URLManager"%>
<%@ page import="com.silverpeas.look.LookSilverpeasV5Helper"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.buttons.Button"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.GraphicElementFactory"%>

<%@ page import="com.stratelia.silverpeas.authentication.*"%>

<%
String 					m_sContext 			=  GeneralPropertiesManager.getGeneralResourceLocator().getString("ApplicationURL");

GraphicElementFactory 	gef 				= (GraphicElementFactory) session.getAttribute("SessionGraphicElementFactory");
LookSilverpeasV5Helper 	helper 				= (LookSilverpeasV5Helper) session.getAttribute("Silverpeas_LookHelper");

String spaceId 		= request.getParameter("privateDomain");
String componentId 	= request.getParameter("component_id");

if (!StringUtil.isDefined(spaceId) && StringUtil.isDefined(componentId))
{
	spaceId = helper.getSpaceId(componentId);
}
%>

<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">
<%
out.println(gef.getLookStyleSheet());
%>
<script type="text/javascript" src="<%=m_sContext%>/util/javaScript/animation.js"></script>
<script type="text/javascript" src="<%=m_sContext%>/util/ajax/prototype.js"></script>
<script type="text/javascript" src="<%=m_sContext%>/util/ajax/rico.js"></script>
<script type="text/javascript" src="<%=m_sContext%>/util/ajax/ricoAjax.js"></script>
<script type="text/javascript" src="<%=m_sContext%>/util/javaScript/lookV5/navigation.js"></script>
<script type="text/javascript" src="<%=m_sContext%>/util/javaScript/lookV5/login.js"></script>
<script language="JavaScript1.2">
<!--

	function reloadTopBar(reload)
	{
		if (reload)
			top.topFrame.location.href="<%=m_sContext%>/admin/jsp/TopBarSilverpeasV5.jsp";
	}
    
    function checkSubmitToSearch(ev)
	{
		var touche = ev.keyCode;
		if (touche == 13)
			searchEngine();
	}
    
    function notifyAdministrators(context,compoId,users,groups)
	{
	    SP_openWindow('<%=m_sContext%>/RnotificationUser/jsp/Main.jsp?popupMode=Yes&editTargets=No&compoId=&theTargetsUsers=Administrators&theTargetsGroups=', 'notifyUserPopup', '700', '400', 'menubar=no,scrollbars=no,statusbar=no');
	}
    
    function openClipboard()
	{
    	document.clipboardForm.submit();
	}
    
	function searchEngine() {
        if (document.searchForm.query.value != "")
        {
	    	document.searchForm.action = "<%=m_sContext%>/RpdcSearch/jsp/AdvancedSearch";
	        document.searchForm.submit();
        }
	}

	function advancedSearchEngine(){
		document.searchForm.action = "<%=m_sContext%>/RpdcSearch/jsp/ChangeSearchTypeToExpert";
		document.searchForm.submit();
	}
	
	var navVisible = true;
	function resizeFrame()
	{
		parent.resizeFrame('10,*');
		if (navVisible)
		{
			document.body.scroll = "no"; 
			document.images['expandReduce'].src="icons/silverpeasV5/extend.gif";
		}
		else
		{
			document.body.scroll = "auto";
			document.images['expandReduce'].src="icons/silverpeasV5/reduct.gif";
		}
		document.images['expandReduce'].blur();
		navVisible = !navVisible;
	}
	    
	// Callback methods to navigation.js
    function getContext()
    {
    	return "<%=m_sContext%>";
    }
    
    function getHomepage()
    {
    	return "<%=gef.getFavoriteLookSettings().getString("defaultHomepage", "/dt")%>";
    }

    function getPersoHomepage()
    {
    	return "<%=gef.getFavoriteLookSettings().getString("persoHomepage", "/dt")%>";
    }
    
    function getSpaceIdToInit()
    {
    	return "<%=spaceId%>";
    }
    
    function getComponentIdToInit()
    {
    	return "<%=componentId%>";
    }
       
    function displayComponentsIcons()
    {
    	return <%=helper.getSettings("displayComponentIcons")%>;
    }
    
    function getPDCLabel()
    {
    	return "<%=helper.getString("lookSilverpeasV5.pdc")%>";
    }
    
    function getLook()
    {
    	return "<%=gef.getCurrentLookName()%>";
    }
    
    function getWallpaper()
    {
    	return "<%=helper.getWallPaper(spaceId)%>";
    }

    function displayPDC()
    {
        return "<%=helper.displayPDCInNavigationFrame()%>";
    }

    function getTopBarPage()
    {
        return "TopBarSilverpeasV5.jsp";
    }

    function getFooterPage()
    {
        return getContext()+"/RpdcSearch/jsp/ChangeSearchTypeToExpert?mode=clear&SearchPage=/admin/jsp/pdcSearchSilverpeasV5.jsp&ResultPage=searchDocuments.jsp&";
    }
-->
</script>
</head>
<body leftmargin="0" topmargin="0" marginwidth="0" marginheight="0" class="fondDomainsBar">
<div id="redExp"><a href="javascript:resizeFrame();"><img src="icons/silverpeasV5/reduct.gif" border="0" align="absmiddle" name="expandReduce" alt="<%=helper.getString("lookSilverpeasV5.reductExtend")%>" title="<%=helper.getString("lookSilverpeasV5.reductExtend")%>"/></a></div>
<div id="domainsBar">
	<div id="recherche">
		<div id="submitRecherche">
			<form name="searchForm" action="<%=m_sContext%>/RpdcSearch/jsp/AdvancedSearch" method="POST" target="MyMain">
			<input name="query" size="30"/><input type="hidden" name="mode" value="clear"/>
			<a href="javascript:searchEngine()"><img src="icons/silverpeasV5/px.gif" width="20" height="20" border="0" /></a>
			</form>
		</div>
        <div id="bodyRecherche">
            <a href="javascript:advancedSearchEngine()"><%=helper.getString("lookSilverpeasV5.AdvancedSearch")%></a> | <a href="<%=m_sContext%>/RpdcSearch/jsp/LastResults" target="MyMain"><%=helper.getString("lookSilverpeasV5.LastSearchResults")%></a> | <a href="#" onClick="javascript:SP_openWindow('<%=m_sContext%>/RpdcSearch/jsp/help.jsp', 'Aide', '700', '220','scrollbars=yes, resizable, alwaysRaised');"><%=helper.getString("lookSilverpeasV5.Help")%></a>
		</div>
    </div>
	<div id="spaceTransverse"></div>
	<div id="basSpaceTransverse">
        <table border="0" cellpadding="0" cellspacing="0" width="100%">
            <tr>
                <td class="basSpacesGauche"><img src="icons/silverpeasV5/px.gif" width="8" height="8"></td>
                <td class="basSpacesMilieu"><img src="icons/silverpeasV5/px.gif" width="8" height="8"></td>
                <td class="basSpacesDroite"><img src="icons/silverpeasV5/px.gif" width="8" height="8"></td>
            </tr>
        </table>
    </div>

    <div id="spaces"><center><br/><br/><%=helper.getString("lookSilverpeasV5.loadingSpaces")%><br/><br/><img src="icons/silverpeasV5/inProgress.gif"/></center></div>
    <% if (!helper.isAnonymousAccess()) { %>
    	<div id="spacePerso" class="spaceLevelPerso"><a class="spaceURL" href="javaScript:openMySpace();"><%=helper.getString("lookSilverpeasV5.PersonalSpace")%></a></div>
    <% } %>
    <div id="basSpaces">
        <table border="0" cellpadding="0" cellspacing="0" width="100%">
            <tr>
                <td class="basSpacesGauche"><img src="icons/silverpeasV5/px.gif" width="8" height="8"></td>
                <td class="basSpacesMilieu"><img src="icons/silverpeasV5/px.gif" width="8" height="8"></td>
                <td class="basSpacesDroite"><img src="icons/silverpeasV5/px.gif" width="8" height="8"></td>
            </tr>
        </table>
    </div>
            
    <div id="loginBox">
    	<form name="authForm" action="<%=m_sContext%>/AuthenticationServlet" method="POST" target="_top">
        <table width="100%">
        <tr>
            <td align="right" valign="top"> 
                <% if (helper.isAnonymousAccess()) { 
                    //------------------------------------------------------------------
                    // domains are used by 'selectDomain.jsp.inc'
                    // Get a LoginPasswordAuthentication object
                    LoginPasswordAuthentication lpAuth = new LoginPasswordAuthentication();
                    Hashtable domains = lpAuth.getAllDomains();
                    //------------------------------------------------------------------
                    Button button = gef.getFormButton(helper.getString("lookSilverpeasV5.login"), "javaScript:login();", false);
                %>
                    <table border="0" cellpadding="0" cellspacing="2">
                        <tr><td><%=helper.getString("lookSilverpeasV5.login")%> : </td><td><%@ include file="../../inputLogin.jsp" %></td></tr>
                        <tr><td nowrap><%=helper.getString("lookSilverpeasV5.password")%> : </td><td><%@ include file="inputPasswordSilverpeasV5.jsp.inc" %></td></tr>
                        <% if (domains.size() == 1) { %>
                            <tr><td colspan="2"><input type="hidden" name="DomainId" value="0"></td></tr>
                        <% } else { %>
                            <tr><td><%=helper.getString("lookSilverpeasV5.domain")%> : </td><td><%@ include file="../../selectDomain.jsp.inc" %></td></tr>
                        <% } %>
                        <tr>
                            <td colspan="2" align="right"><%=button.print()%></td>
                        </tr>
                    </table>
                <% } else { 
                    Button button = gef.getFormButton(helper.getString("lookSilverpeasV5.logout"), "javaScript:logout();", false);
                %>
                    <table border="0" cellpadding="0" cellspacing="2">
                        <tr><td colspan="2" align="right"><%=helper.getUserFullName()%></td></tr>
                        <tr><td colspan="2" align="right"><%=button.print()%></td></tr>
                    </table>
                <% } %>
            </td>
        </tr>
        </table>
        </form>
    </div>
</div>
<form name="clipboardForm" action="<%=m_sContext+URLManager.getURL(URLManager.CMP_CLIPBOARD)%>Idle.jsp" method="POST" target="IdleFrame">
<input type="hidden" name="message" value="SHOWCLIPBOARD"/>
</form>
<!-- Form below is used only to refresh this page according to external link (ie search engine, homepage,...) -->
<form name="privateDomainsForm" action="DomainsBarSilverpeasV5.jsp" method="POST">
<input type="hidden" name ="component_id"/>
<input type="hidden" name ="privateDomain"/>
<input type="hidden" name ="privateSubDomain"/>
</form>
</body>
</html>