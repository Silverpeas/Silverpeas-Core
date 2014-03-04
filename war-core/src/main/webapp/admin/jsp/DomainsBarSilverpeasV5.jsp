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

<%@ page import="java.util.*"%>
<%@ page import="com.silverpeas.util.StringUtil"%>
<%@ page import="com.silverpeas.util.EncodeHelper"%>
<%@ page import="com.stratelia.webactiv.util.*"%>
<%@ page import="com.stratelia.silverpeas.peasCore.URLManager"%>
<%@ page import="com.stratelia.webactiv.beans.admin.Domain"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.buttons.Button"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.GraphicElementFactory"%>

<%@ page import="com.silverpeas.look.LookHelper" %>
<%@ page import="org.silverpeas.authentication.AuthenticationService" %>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>

<%-- Retrieve user menu display mode --%>
<c:set var="curHelper" value="${sessionScope.Silverpeas_LookHelper}" />
<%-- Set resource bundle --%>
<fmt:setLocale value="${sessionScope['SilverSessionController'].favoriteLanguage}" />
<view:setBundle basename="com.silverpeas.lookSilverpeasV5.multilang.lookBundle"/>

<%
String          m_sContext      = URLManager.getApplicationURL();

GraphicElementFactory   gef         = (GraphicElementFactory) session.getAttribute(GraphicElementFactory.GE_FACTORY_SESSION_ATT);
LookHelper  helper        = (LookHelper) session.getAttribute(LookHelper.SESSION_ATT);

String spaceId    	= request.getParameter("privateDomain");
String subSpaceId   = request.getParameter("privateSubDomain");
String componentId  = request.getParameter("component_id");

if (!StringUtil.isDefined(spaceId) && StringUtil.isDefined(componentId)) {
  spaceId = helper.getSpaceId(componentId);
} else if (StringUtil.isDefined(subSpaceId)) {
  spaceId = subSpaceId;
}

ResourceLocator resourceSearchEngine = new ResourceLocator("com.stratelia.silverpeas.pdcPeas.settings.pdcPeasSettings", "");
int autocompletionMinChars = resourceSearchEngine.getInteger("autocompletion.minChars", 3);

//Is "forgotten password" feature active ?
ResourceLocator authenticationBundle = new ResourceLocator("com.silverpeas.authentication.multilang.authentication", "");
ResourceLocator general	= new ResourceLocator("com.stratelia.silverpeas.lookAndFeel.generalLook", "");
String pwdResetBehavior = general.getString("forgottenPwdActive", "reinit");
boolean forgottenPwdActive = !pwdResetBehavior.equalsIgnoreCase("false");
String urlToForgottenPwd = m_sContext+"/CredentialsServlet/ForgotPassword";
if ("personalQuestion".equalsIgnoreCase(pwdResetBehavior)) {
  urlToForgottenPwd = m_sContext+"/CredentialsServlet/LoginQuestion";
}
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
   "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<title></title>
<%
out.println(gef.getLookStyleSheet());
%>
<!-- Add JQuery mask plugin css -->
<link href="<%=m_sContext%>/util/styleSheets/jquery.loadmask.css" rel="stylesheet" type="text/css" />

<!-- Add RICO javascript library -->
<script type="text/javascript" src="<%=m_sContext%>/util/javaScript/animation.js"></script>
<script type="text/javascript" src="<%=m_sContext%>/util/ajax/prototype.js"></script>
<script type="text/javascript" src="<%=m_sContext%>/util/ajax/rico.js"></script>
<script type="text/javascript" src="<%=m_sContext%>/util/ajax/ricoAjax.js"></script>

<!-- Add jQuery javascript library -->
<script type="text/javascript" src="<%=m_sContext%>/util/javaScript/jquery/jquery.loadmask.js"></script>
<script type="text/javascript" src="<%=m_sContext%>/util/javaScript/jquery/jquery.bgiframe.min.js"></script>

<!-- Custom domains bar javascript -->
<script type="text/javascript" src="<%=m_sContext%>/util/javaScript/lookV5/navigation.js"></script>
<script type="text/javascript" src="<%=m_sContext%>/util/javaScript/lookV5/personalSpace.js"></script>
<script type="text/javascript" src="<%=m_sContext%>/util/javaScript/lookV5/login.js"></script>


<script type="text/javascript">

  function reloadTopBar(reload)
  {
    if (reload)
      top.topFrame.location.href="<%=m_sContext%>/admin/jsp/TopBarSilverpeasV5.jsp";
  }

    function checkSubmitToSearch(ev)
  {
    var touche = ev.keyCode;
    if (touche === 13)
      searchEngine();
  }

    function notifyAdministrators(context,compoId,users,groups)
  {
      SP_openWindow('<%=m_sContext%>/RnotificationUser/jsp/Main?popupMode=Yes&editTargets=No&theTargetsUsers=Administrators', 'notifyUserPopup', '700', '400', 'menubar=no,scrollbars=no,statusbar=no');
  }

    function openClipboard()
  {
      document.clipboardForm.submit();
  }

  function searchEngine() {
        if (document.searchForm.query.value !== "")
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
      return "<%=EncodeHelper.javaStringToHtmlString(spaceId)%>";
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
      return '<fmt:message key="lookSilverpeasV5.pdc" />';
    }

    function getLook()
    {
      return "<%=gef.getCurrentLookName()%>";
    }

    function getSpaceWithCSSToApply()
    {
      return "<%=helper.getSpaceWithCSSToApply()%>";
    }

    function displayPDC()
    {
        return "<%=helper.displayPDCInNavigationFrame()%>";
    }

    function displayContextualPDC() {
        return <%=helper.displayContextualPDC()%>;
    }

    function getTopBarPage()
    {
        return "TopBarSilverpeasV5.jsp";
    }

    function getFooterPage()
    {
    	return getContext()+"/RpdcSearch/jsp/ChangeSearchTypeToExpert?SearchPage=/admin/jsp/pdcSearchSilverpeasV5.jsp&";
    }

    /**
     * Reload bottom frame
     */
    function reloadSpacesBarFrame(tabId) {
       top.bottomFrame.location.href="<%=m_sContext%>/admin/jsp/frameBottomSilverpeasV5.jsp?UserMenuDisplayMode=" + tabId;
    }

    function getPersonalSpaceLabels()
    {
        var labels = new Array(2);
        labels[0] = "<%=EncodeHelper.javaStringToJsString(helper.getString("lookSilverpeasV5.personalSpace.select"))%>";
        labels[1] = "<%=EncodeHelper.javaStringToJsString(helper.getString("lookSilverpeasV5.personalSpace.remove.confirm"))%>";
        labels[2] = "<%=EncodeHelper.javaStringToJsString(helper.getString("lookSilverpeasV5.personalSpace.add"))%>";
        return labels;
    }

    function toForgottenPassword() {
    	var form = document.getElementById("authForm");
        if (form.elements["Login"].value.length === 0) {
            alert("<%=authenticationBundle.getString("authentication.logon.loginMissing") %>");
        } else {
        	form.action = "<%=urlToForgottenPwd%>";
        	form.target = "MyMain";
        	form.submit();
        }
    }

  /**
   * Using "jQuery" instead of "$" at this level prevents of getting conficts with another
   * javascript plugin.
   */
  //used by keyword autocompletion
  <%  if(resourceSearchEngine.getBoolean("enableAutocompletion", false)){ %>
  jQuery(document).ready(function() {
    jQuery("#query").autocomplete({
      source: "<%=m_sContext%>/AutocompleteServlet",
      minLength : <%=autocompletionMinChars%>
    });
  });
  <%}%>

</script>
</head>
<body class="fondDomainsBar">
<div id="redExp"><a href="javascript:resizeFrame();"><img src="icons/silverpeasV5/reduct.gif" border="0" name="expandReduce" alt="<fmt:message key="lookSilverpeasV5.reductExtend" />" title="<fmt:message key="lookSilverpeasV5.reductExtend" />"/></a></div>
<div id="domainsBar">
  <div id="recherche">
    <div id="submitRecherche">
      <form name="searchForm" action="<%=m_sContext%>/RpdcSearch/jsp/AdvancedSearch" method="post" target="MyMain">
      <input name="query" size="30" id="query"/>
      <input type="hidden" name="mode" value="clear"/>
      <a href="javascript:searchEngine()"><img src="icons/silverpeasV5/px.gif" width="20" height="20" border="0" alt=""/></a>
      </form>
    </div>
        <div id="bodyRecherche">
            <a href="javascript:advancedSearchEngine()"><fmt:message key="lookSilverpeasV5.AdvancedSearch" /></a> | <a href="<%=m_sContext%>/RpdcSearch/jsp/LastResults" target="MyMain"><fmt:message key="lookSilverpeasV5.LastSearchResults" /></a> | <a href="#" onclick="javascript:SP_openWindow('<%=m_sContext%>/RpdcSearch/jsp/help.jsp', 'Aide', '700', '220','scrollbars=yes, resizable, alwaysRaised');"><fmt:message key="lookSilverpeasV5.Help" /></a>
    </div>
    </div>
  <div id="spaceTransverse"></div>
  <div id="basSpaceTransverse">
        <table border="0" cellpadding="0" cellspacing="0" width="100%">
            <tr>
                <td class="basSpacesGauche"><img src="icons/silverpeasV5/px.gif" width="8" height="8" alt=""/></td>
                <td class="basSpacesMilieu"><img src="icons/silverpeasV5/px.gif" width="8" height="8" alt=""/></td>
                <td class="basSpacesDroite"><img src="icons/silverpeasV5/px.gif" width="8" height="8" alt=""/></td>
            </tr>
        </table>
    </div>
    <div id="spaceMenuDivId">
      <c:if test="${curHelper.menuPersonalisationEnabled}">
        <fmt:message key="lookSilverpeasV5.favoriteSpace.tabBookmarks" var="tabBookmarksLabel" />
        <fmt:message key="lookSilverpeasV5.favoriteSpace.tabAll" var="tabAllLabel" />
        <div id="tabDivId" class="tabSpace">
          <form name="spaceDisplayModeForm" action="#" method="get" >
            <input type="hidden" name="userMenuDisplayMode" id="userMenuDisplayModeId" value="<c:out value="${curHelper.displayUserMenu}"></c:out>" />
            <input type="hidden" name="enableAllUFSpaceStates" id="enableAllUFSpaceStatesId" value="<c:out value="${v.enableUFSContainsState}"></c:out>" />
            <input type="hidden" name="loadingMessage" id="loadingMessageId" value="<fmt:message key="lookSilverpeasV5.loadingSpaces" />" />
            <input type="hidden" name="noFavoriteSpaceMsg" id="noFavoriteSpaceMsgId" value="<fmt:message key="lookSilverpeasV5.noFavoriteSpace" />" />
          </form>

          <c:if test="${curHelper.displayUserMenu == 'BOOKMARKS'}">
            <div id="tabsBookMarkSelectedDivId">
             <view:tabs>
               <view:tab label="${tabBookmarksLabel}" action="javascript:openTab('BOOKMARKS');" selected="true"></view:tab>
               <view:tab label="${tabAllLabel}" action="javascript:openTab('ALL');" selected="false"></view:tab>
             </view:tabs>
            </div>
            <div id="tabsAllSelectedDivId" style="display:none">
              <view:tabs>
               <view:tab label="${tabBookmarksLabel}" action="javascript:openTab('BOOKMARKS');" selected="false"></view:tab>
               <view:tab label="${tabAllLabel}" action="javascript:openTab('ALL');" selected="true"></view:tab>
              </view:tabs>
            </div>
          </c:if>
          <c:if test="${curHelper.displayUserMenu == 'ALL'}">
            <div id="tabsBookMarkSelectedDivId" style="display:none">
             <view:tabs>
               <view:tab label="${tabBookmarksLabel}" action="javascript:openTab('BOOKMARKS');" selected="true"></view:tab>
               <view:tab label="${tabAllLabel}" action="javascript:openTab('ALL');" selected="false"></view:tab>
             </view:tabs>
            </div>
            <div id="tabsAllSelectedDivId">
              <view:tabs>
               <view:tab label="${tabBookmarksLabel}" action="javascript:openTab('BOOKMARKS');" selected="false"></view:tab>
               <view:tab label="${tabAllLabel}" action="javascript:openTab('ALL');" selected="true"></view:tab>
              </view:tabs>
            </div>
          </c:if>
        </div>
      </c:if>
      <div id="spaces">
		<center><br/><br/><fmt:message key="lookSilverpeasV5.loadingSpaces" /><br/><br/><img src="icons/silverpeasV5/inProgress.gif" alt="<fmt:message key="lookSilverpeasV5.loadingSpaces" />"/></center>
	  </div>
      <% if (!helper.isAnonymousAccess()) { %>
        <div id="spacePerso" class="spaceLevelPerso"><a class="spaceURL" href="javaScript:openMySpace();"><fmt:message key="lookSilverpeasV5.PersonalSpace" /></a></div>
      <% } %>
    </div>
    <div id="basSpaces">
        <table border="0" cellpadding="0" cellspacing="0" width="100%">
            <tr>
                <td class="basSpacesGauche"><img src="icons/silverpeasV5/px.gif" width="8" height="8" alt=""/></td>
                <td class="basSpacesMilieu"><img src="icons/silverpeasV5/px.gif" width="8" height="8" alt=""/></td>
                <td class="basSpacesDroite"><img src="icons/silverpeasV5/px.gif" width="8" height="8" alt=""/></td>
            </tr>
        </table>
    </div>

    <div id="loginBox">
      <form name="authForm" id="authForm" action="<%=m_sContext%>/AuthenticationServlet" method="post" target="_top">
        <table width="100%">
        <tr>
            <td align="right" valign="top">
                <% if (helper.isAnonymousAccess()) {
                    //------------------------------------------------------------------
                    // domains are used by 'selectDomain.jsp.inc'
                    // Get a AuthenticationService object
                    AuthenticationService lpAuth = new AuthenticationService();
                    List<Domain> listDomains = lpAuth.getAllDomains();
                    pageContext.setAttribute("listDomains", listDomains);
                    pageContext.setAttribute("multipleDomains", listDomains != null && listDomains.size() > 1);
                    //------------------------------------------------------------------
                    Button button = gef.getFormButton(helper.getString("lookSilverpeasV5.login"), "javaScript:login();", false);
                %>
                    <table border="0" cellpadding="0" cellspacing="2">
                        <tr><td><%=helper.getString("lookSilverpeasV5.login")%> : </td><td><%@ include file="../../inputLogin.jsp" %></td></tr>
                        <tr><td nowrap="nowrap"><%=helper.getString("lookSilverpeasV5.password")%> : </td><td><%@ include file="inputPasswordSilverpeasV5.jsp.inc" %></td></tr>
                        <c:choose>
                      		<c:when test="${!pageScope.multipleDomains}">
                            	<tr><td colspan="2"><input type="hidden" name="DomainId" value="<%=listDomains.get(0).getId()%>"/></td></tr>
                        	</c:when>
                      		<c:otherwise>
	                            <tr>
	                            	<td><fmt:message key="lookSilverpeasV5.domain" /> : </td>
	                            	<td><select id="DomainId" name="DomainId" size="1">
	                                  		<c:forEach var="domain" items="${pageScope.listDomains}">
	                                      		<option value="<c:out value="${domain.id}" />" <c:if test="${domain.id eq param.DomainId}">selected="selected"</c:if> ><c:out value="${domain.name}"/></option>
	                                  		</c:forEach>
										</select></td>
	                            </tr>
                        	</c:otherwise>
                        </c:choose>
                        <tr>
                            <td colspan="2" align="right"><%=button.print()%></td>
                        </tr>
                    </table>
                   	 <% if (forgottenPwdActive) { %>
						<span class="forgottenPwd">
						<% if ("personalQuestion".equalsIgnoreCase(pwdResetBehavior)) { %>
							<a href="javascript:toForgottenPassword()"><%=authenticationBundle.getString("authentication.logon.passwordForgotten") %></a>
						<% } else { %>
						 	<a href="javascript:toForgottenPassword()"><%=authenticationBundle.getString("authentication.logon.passwordReinit") %></a>
						<%} %>
						</span>
					 <% } %>
                <% } else {
                    Button button = gef.getFormButton(helper.getString("lookSilverpeasV5.logout"), "javaScript:logout();", false);
                %>
                    <table border="0" cellpadding="0" cellspacing="2" id="userNav">
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
<form name="clipboardForm" action="<%=m_sContext+URLManager.getURL(URLManager.CMP_CLIPBOARD)%>Idle.jsp" method="post" target="IdleFrame">
<input type="hidden" name="message" value="SHOWCLIPBOARD"/>
</form>
<!-- Form below is used only to refresh this page according to external link (ie search engine, homepage,...) -->
<form name="privateDomainsForm" action="DomainsBarSilverpeasV5.jsp" method="post">
<input type="hidden" name ="component_id"/>
<input type="hidden" name ="privateDomain"/>
<input type="hidden" name ="privateSubDomain"/>
</form>
</body>
</html>