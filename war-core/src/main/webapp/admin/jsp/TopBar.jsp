<%--

    Copyright (C) 2000 - 2009 Silverpeas

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as
    published by the Free Software Foundation, either version 3 of the
    License, or (at your option) any later version.

    As a special exception to the terms and conditions of version 3.0 of
    the GPL, you may redistribute this Program in connection with Free/Libre
    Open Source Software ("FLOSS") applications as described in Silverpeas's
    FLOSS exception.  You should have received a copy of the text describing
    the FLOSS exception, and it is also available here:
    "http://repository.silverpeas.com/legal/licensing"

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.

--%>

<%@ include file="importFrameSet.jsp" %>
<%@ page import="com.stratelia.silverpeas.peasCore.URLManager"%>
<%@ page import="com.stratelia.silverpeas.peasCore.SessionManager"%>
<%!
public void put(String key, String champ, String defaut, Hashtable icons){
	if (champ==null)
		icons.put(key, defaut);
	else {
		if (champ.length()==0)
			icons.put(key, defaut);
		else
			icons.put(key, champ);
	}
}
%>
<%
Hashtable icons = new Hashtable();

String smallLogo = gef.getIcon("smallLogo");
put("smallLogo", smallLogo, "icons/logo_silverpeas.gif", icons);
String mailIcon = gef.getIcon("mailIcon");
put("mailIcon", mailIcon, "icons/icoOutilsMail.gif", icons);
String mapIcon = gef.getIcon("mapIcon");
put("mapIcon", mapIcon, "icons/icoOutilsMap.gif", icons);
String logIcon = gef.getIcon("logIcon");
put("logIcon", logIcon, "icons/icoOutilsExit.gif", icons);
String customIcon = gef.getIcon("customIcon");
put("customIcon", customIcon, "icons/icoOutilsProfil.gif", icons);
String helpIcon = gef.getIcon("helpIcon");
put("helpIcon", helpIcon, "icons/icoOutilsAide.gif", icons);
String clipboardIcon = gef.getIcon("clipboardIcon");
put("clipboardIcon", clipboardIcon, "icons/icoOutilsClipboard.gif", icons);
String adminConsol = gef.getIcon("adminConsol");
put("adminConsol", adminConsol, "icons/adminConsol.gif", icons);
String okIcon = gef.getIcon("okIcon");
put("okIcon", okIcon, "icons/rech_ok.gif", icons);
String personalSpaceIcon = gef.getIcon("personalSpaceIcon");
put("personalSpaceIcon", personalSpaceIcon, "icons/accueil/esp_persoLittle15.gif", icons);
String reductIcon = gef.getIcon("reductIcon");
put("reductIcon", reductIcon, "icons/reductdomainbar.gif", icons);
String extendIcon = gef.getIcon("extendIcon");
put("extendIcon", extendIcon, "icons/extenddomainbar.gif", icons);
String arrowRightIcon = gef.getIcon("arrowRightIcon");
put("arrowRightIcon", arrowRightIcon, "icons/arrow.gif", icons);
String wallPaper = gef.getIcon("wallPaper");
put("wallPaper", wallPaper, "icons/fond.gif", icons);
String glossary = gef.getIcon("glossary");
put("glossary", glossary, "icons/navigation2.gif", icons);
String homeSpaceIcon = gef.getIcon("homeSpaceIcon");
put("homeSpaceIcon", homeSpaceIcon, m_sContext+"/util/icons/HomePerso.gif", icons);
String lastResultsIcon = gef.getIcon("lastResultsIcon");
put("lastResultsIcon", lastResultsIcon, m_sContext+"/util/icons/indexer.gif", icons);

String workSpace = request.getParameter("SpaceId");
int nbConnectedUsers = 0;
String connectedUsers = message.getString("connectedUsers");

if ("yes".equals(homePageSettings.getString("displayConnectedUsers")))
{
				//Remove the current user
			nbConnectedUsers = SessionManager.getInstance().getNbConnectedUsersList() - 1;
			connectedUsers = message.getString("connectedUsers");
			if (nbConnectedUsers <= 1)
							connectedUsers = message.getString("connectedUser");
}
%>

<html>
<head>
<title>entete</title>
<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">
<script type="text/javascript" src="<%=m_sContext%>/util/javaScript/animation.js"></script>
<script language="JavaScript">
<!--
function searchEngine() {
	document.searchEngineForm.action = "<%=m_sContext%>/RpdcSearch/jsp/AdvancedSearch";
    document.searchEngineForm.query.value = document.searchForm.query.value;
    if (document.searchForm.query.value != "")
    {
    	document.searchEngineForm.submit();
    }
}

function advancedSearchEngine(){
	document.searchEngineForm.action = "<%=m_sContext%>/RpdcSearch/jsp/GlobalView";
	document.searchEngineForm.submit();
}

// User Notification Popup
function notifyPopup(context,compoId,users,groups)
{
    top.scriptFrame.SP_openWindow(context+'/RnotificationUser/jsp/Main.jsp?popupMode=Yes&editTargets=No&compoId=' + compoId + '&theTargetsUsers='+users+'&theTargetsGroups='+groups, 'notifyUserPopup', '700', '400', 'menubar=no,scrollbars=no,statusbar=no');
}

function goToPreferedSpace(preferedSpace) {
        top.bottomFrame.SpacesBar.location = "DomainsBar.jsp?privateDomain="+preferedSpace;
        top.bottomFrame.MyMain.location = "Main.jsp?SpaceId="+preferedSpace;
}

function viewPersonalHomePage() {
        top.bottomFrame.MyMain.location = "Main.jsp?ViewPersonalHomePage=true";
}

function openPdc()
{
        chemin="<%=m_sContext%>/RpdcSearch/jsp/AxisTree?query=";
        largeur = "700";
        hauteur = "500";
        SP_openWindow(chemin,"Pdc_Pop",largeur,hauteur,"scrollbars=yes, resizable=yes");
}
function openConnectedUsers()
{
        chemin = "<%=m_sContext%>/RcommunicationUser/jsp/Main";
        SP_openWindow(chemin,"users_pop",400,400,"scrollbars=yes, resizable=yes");
}

<% if ("yes".equals(homePageSettings.getString("displayConnectedUsers"))) 
{ %>
				ID = window.setTimeout("DoIdle();", <%=homePageSettings.getString("refreshTopbarTimeout")%>*1000);
				function DoIdle()
				{ self.location.href = "TopBar.jsp?SpaceId=<%=workSpace%>"; }
<% } %>

//-->
</script>
<style type="text/css">
<!--
body {  background-image: url(<%=icons.get("wallPaper")%>); background-repeat: repeat-x}
-->
</style>
<%
out.println(gef.getLookStyleSheet());
%>
</head>

<body bgcolor="#FFFFFF" leftmargin="0" topmargin="0" marginwidth="0" marginheight="0">
<table width="631" cellspacing="0" cellpadding="0" border="0">
<form name="searchForm" action="<%=m_sContext%>/RpdcSearch/jsp/AdvancedSearch" target="MyMain">
<input type="hidden" name="mode" value="clear">
<tr>
    <td rowspan="2" width="108" align="center"><a href="javascript:goToPreferedSpace('<%=workSpace%>');"><img src="<%=icons.get("smallLogo")%>" width="108" height="60" border="0"></a></td>
    <td colspan="2" width="296" nowrap>
			&nbsp;<span class=txtpetitblanc><%=message.getString("Tools")%> :&nbsp;</span>
			<img src="<%=icons.get("arrowRightIcon")%>" align="absmiddle">&nbsp;
			<a href="javascript:notifyPopup('<%=m_sContext%>','','Administrators','')"><img src="<%=icons.get("mailIcon")%>" align="absmiddle" alt="<%=message.getString("Feedback")%>" border="0" onFocus="self.blur()" title="<%=message.getString("Feedback")%>"></a>&nbsp;
			<a href="<%=m_sContext + "/admin/jsp/Map.jsp"%>" target="MyMain"><img src="<%=icons.get("mapIcon")%>" align="absmiddle" border="0" alt="<%=message.getString("MyMap")%>" onFocus="self.blur()" title="<%=message.getString("MyMap")%>"></a>&nbsp;
			<a href="<%=m_sContext + "/LogoutServlet"%>" target="_top"><img src="<%=icons.get("logIcon")%>" align="absmiddle" border="0" alt="<%=message.getString("Exit")%>" onFocus="self.blur()" title="<%=message.getString("Exit")%>"></a>&nbsp;
			<a href="<%=m_sContext + URLManager.getURL(URLManager.CMP_PERSONALIZATION) + "Main.jsp"%>" target="MyMain"><img src="<%=icons.get("customIcon")%>" align="absmiddle" border="0" alt="<%=message.getString("Personalization")%>" onFocus="self.blur()" title="<%=message.getString("Personalization")%>"></a>&nbsp;
			<a href="/help_fr/Silverpeas.htm" target="_blank"><img src="<%=icons.get("helpIcon")%>" align="absmiddle" border="0" alt="<%=message.getString("Help")%>" onFocus="self.blur()" title="<%=message.getString("Help")%>"></a>&nbsp;
			<a href="<%=m_sContext + URLManager.getURL(URLManager.CMP_CLIPBOARD) + "Idle.jsp?message=SHOWCLIPBOARD"%>" target="IdleFrame"><img src="<%=icons.get("clipboardIcon")%>" align="absmiddle" border="0" alt="<%=message.getString("Clipboard")%>" onFocus="self.blur()" title="<%=message.getString("Clipboard")%>"></a>&nbsp;&nbsp;

			<% if(m_MainSessionCtrl.getCurrentUserDetail().isBackOfficeVisible() || (m_MainSessionCtrl.getUserManageableSpaceIds() != null && m_MainSessionCtrl.getUserManageableSpaceIds().length>0)) {
			%>
				<a href="<%=m_sContext + URLManager.getURL(URLManager.CMP_JOBMANAGERPEAS) + "Main"%>" target="_top"><img src="<%=icons.get("adminConsol")%>" align="absmiddle" border="0" alt="<%=message.getString("adminConsol")%>" onFocus="self.blur()" title="<%=message.getString("adminConsol")%>"></a>&nbsp;
			<%}%>
			<a href="javascript:onClick=openPdc()"><img src="<%=icons.get("glossary")%>" align="absmiddle" border="0" alt="<%=message.getString("glossaire")%>" onFocus="self.blur()" title="<%=message.getString("glossaire")%>"></a>&nbsp;
		</td>
    <td colspan="2" width="226" nowrap>
                <span class=txtpetitblanc><%=message.getString("Date")%> :&nbsp;
                <img src="<%=icons.get("arrowRightIcon")%>"  align="absmiddle">
                &nbsp;<%=DateUtil.getOutputDate(new Date(), language)%></span>
                </td>
    <td></td>
    <td><img src="icons/1px.gif" width="1" height="26"></td>
</tr>
<tr>
    <td width="195" height="1%">

			<table cellspacing="0" cellpadding="0" border="0" height="1%">
				<tr valign="top">
					<td align="left" valign="top">
						<img src="<%=icons.get("personalSpaceIcon")%>" valign="top">
					</td>
					<td valign="top"><span class="Titre"><%=message.getString("SpacePersonal")%></span>
					</td>
				</tr>
				<tr valign="top">
					<td align="left" colspan="2" valign="top">
					                <span class="selectNS">
									 <select name="selection" onChange="top.scriptFrame.jumpTopbar()">
									   <option value="" selected><%=message.getString("Choose")%></option>
									   <option value="">----------------</option>
									   <option value="<%=m_sContext + URLManager.getURL(URLManager.CMP_AGENDA) + "agenda.jsp"%>"><%=message.getString("Diary")%></option>
									   <option value="<%=m_sContext + URLManager.getURL(URLManager.CMP_TODO) + "todo.jsp"%>"><%=message.getString("ToDo")%></option>
									   <option value="<%=m_sContext + URLManager.getURL(URLManager.CMP_SILVERMAIL) + "Main"%>"><%=message.getString("Mail")%></option>
									   <option value="<%=m_sContext + URLManager.getURL(URLManager.CMP_PDCSUBSCRIPTION) + "subscriptionList.jsp"%>"><%=message.getString("MyInterestCenters")%></option>
									   <option value="<%=m_sContext + URLManager.getURL(URLManager.CMP_INTERESTCENTERPEAS)+ "iCenterList.jsp"%>"><%=message.getString("FavRequests")%></option>
									 </select>
                        </span>				
					<a href="javascript:onClick=viewPersonalHomePage()" border=0><img src="<%=icons.get("homeSpaceIcon")%>" border="0" align="absmiddle" alt="<%=message.getString("BackToPersonalMainPage")%>" title="<%=message.getString("BackToMainPage")%>"></a>
					</td>
				</tr>
			</table>

				
				</td>
    <td colspan="2" width="202" align="center">
                        <span class=txtpetitblanc>
<%
    if(m_MainSessionCtrl != null) {
        UserDetail userDetail = m_MainSessionCtrl.getCurrentUserDetail();
        out.println(userDetail.getFirstName() + " " + userDetail.getLastName());
    }
%>
                        </span>
                </td>
    <td width="125">
                        &nbsp;<a href="javascript:advancedSearchEngine()" class="Titre"><%=message.getString("SearchAdvanced")%></a><br>
       <script language=javascript>
       <!--
       if (navigator.appName == 'Netscape') {
        document.write('&nbsp;<input type="text" name="query" value="" size="8">');
       } else {
        document.write('&nbsp;<input type="text" name="query" value="" size="12">');
       }
       //-->
			 </script>
			 <a href="javascript:searchEngine()"><img border="0" src="<%=icons.get("okIcon")%>" align="absmiddle"></a> <a href="<%=m_sContext%>/RpdcSearch/jsp/LastResults" target="MyMain"><img border="0" src="<%=icons.get("lastResultsIcon")%>" align="absmiddle" alt="<%=message.getString("SearchLastResults")%>"></a>
		</td>
    <td></td>
    <td><img src="icons/1px.gif" width="1" height="35"></td>
</tr>
<tr>
    <td colspan="4">
                        <img src="icons/1px.gif" name="spaceur" width="155" height="1">
                        <a href="javascript:top.scriptFrame.buildFrameset('<%=icons.get("reductIcon")%>','<%=icons.get("extendIcon")%>')">
                                <img src="<%=icons.get("reductIcon")%>" border="0" name="domainWidth" alt="<%=message.getString("reductExtend")%>" title="<%=message.getString("reductExtend")%>">
                        </a>
                </td>
                <td colspan=2 align=right>
                      <% if ("yes".equals(homePageSettings.getString("displayConnectedUsers")) && nbConnectedUsers>0)
                        { %>
                        <a class="txtnote" href="javascript:onClick=openConnectedUsers();"><span class="txtpetitblanc"><font color=ffffff><%=nbConnectedUsers%>&nbsp;<%=connectedUsers%></font></span></a>
                      <% } %>
                      &nbsp;&nbsp;
                </td>
    <td><img src="icons/1px.gif" width="1" height="18"></td>
</tr>
<tr>
    <td width="108"><img src="icons/1px.gif" width="108" height="1"></td>
    <td width="195"><img src="icons/1px.gif" width="195" height="1"></td>
    <td width="101"><img src="icons/1px.gif" width="101" height="1"></td>
    <td width="101"><img src="icons/1px.gif" width="101" height="1"></td>
    <td width="125"><img src="icons/1px.gif" width="125" height="1"></td>
    <td width="130"><img src="icons/1px.gif" width="130" height="1"></td>
    <td width="1"><img src="icons/1px.gif" width="1" height="1"></td>
</tr>
</form>
<form name="searchEngineForm" action="<%=m_sContext%>/RpdcSearch/jsp/AdvancedSearch" target="MyMain" >
    <input type="hidden" name="mode" value="clear">
	<input type="hidden" name="query">
</form>
</table>
</body>
</html>