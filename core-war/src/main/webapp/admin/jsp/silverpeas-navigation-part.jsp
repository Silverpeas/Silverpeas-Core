<%--

    Copyright (C) 2000 - 2020 Silverpeas

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as
    published by the Free Software Foundation, either version 3 of the
    License, or (at your option) any later version.

    As a special exception to the terms and conditions of version 3.0 of
    the GPL, you may redistribute this Program in connection with Free/Libre
    Open Source Software ("FLOSS") applications as described in Silverpeas's
    FLOSS exception.  You should have received a copy of the text describing
    the FLOSS exception, and it is also available here:
    "https://www.silverpeas.org/legal/floss_exception.html"

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.

--%>
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<%@ page import="org.silverpeas.core.web.look.LookHelper" %>
<%@ page import="org.silverpeas.core.util.URLUtil"%>
<%@ page import="org.silverpeas.core.util.WebEncodeHelper" %>
<%@ page import="org.silverpeas.core.util.ResourceLocator" %>

<%@ page import="org.silverpeas.core.util.SettingBundle" %>
<%@ page import="org.silverpeas.core.util.StringUtil" %>
<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.GraphicElementFactory" %>
<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.buttons.Button" %>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>
<%@ taglib uri="http://www.silverpeas.com/tld/silverFunctions" prefix="silfn" %>

<c:url var="icon_px" value="/util/viewGenerator/icons/px.gif"/>
<c:url var="urlLogin" value="/Login"/>

<%-- Retrieve user menu display mode --%>
<c:set var="curHelper" value="${sessionScope.Silverpeas_LookHelper}" />
<%-- Set resource bundle --%>
<fmt:setLocale value="${sessionScope['SilverSessionController'].favoriteLanguage}" />
<view:setBundle basename="org.silverpeas.lookSilverpeasV5.multilang.lookBundle"/>

<%
String          m_sContext      = URLUtil.getApplicationURL();

GraphicElementFactory   gef         = (GraphicElementFactory) session.getAttribute(GraphicElementFactory.GE_FACTORY_SESSION_ATT);
LookHelper  helper        = LookHelper.getLookHelper(session);

String spaceId    	= request.getParameter("privateDomain");
String subSpaceId   = request.getParameter("privateSubDomain");
String componentId  = request.getParameter("component_id");
boolean displayPersonalSpace = StringUtil.getBooleanValue(request.getParameter("FromMySpace"));

if (!StringUtil.isDefined(spaceId) && StringUtil.isDefined(componentId)) {
  spaceId = helper.getSpaceId(componentId);
} else if (StringUtil.isDefined(subSpaceId)) {
  spaceId = subSpaceId;
}
gef.setSpaceIdForCurrentRequest(spaceId);

Button button = gef.getFormButton(helper.getString("lookSilverpeasV5.logout"), "javaScript:onclick=spUserSession.logout();", false);
if (helper.isAnonymousUser()) {
  button = gef.getFormButton(helper.getString("lookSilverpeasV5.login"), "javaScript:goToLoginPage();", false);
}

  SettingBundle resourceSearchEngine =
      ResourceLocator.getSettingBundle("org.silverpeas.pdcPeas.settings.pdcPeasSettings");
int autocompletionMinChars = resourceSearchEngine.getInteger("autocompletion.minChars", 3);
%>
<!-- Add JQuery mask plugin css -->
<view:link href="/util/styleSheets/jquery.loadmask.css"/>

<!-- Add jQuery javascript library -->
<view:loadScript src="/util/javaScript/jquery/jquery.loadmask.js"/>

<script type="text/javascript">


	if (navigator.userAgent.match(/(android|iphone|ipad|blackberry|symbian|symbianos|symbos|netfront|model-orange|javaplatform|iemobile|windows phone|samsung|htc|opera mobile|opera mobi|opera mini|presto|huawei|blazer|bolt|doris|fennec|gobrowser|iris|maemo browser|mib|cldc|minimo|semc-browser|skyfire|teashark|teleca|uzard|uzardweb|meego|nokia|bb10|playbook)/gi)) {
		if ( ((screen.width  >= 480) && (screen.height >= 800)) || ((screen.width  >= 800) && (screen.height >= 480)) || navigator.userAgent.match(/ipad/gi) ) {
			var ss = document.createElement("link");
				ss.type = "text/css";
				ss.rel = "stylesheet";
				ss.href = "<%=m_sContext%>/util/styleSheets/domainsBar-tablette.css";
			document.getElementsByTagName("head")[0].appendChild(ss);

		}
	}

    function checkSubmitToSearch(ev)
  {
    var touche = ev.keyCode;
    if (touche === 13)
      searchEngine();
  }

  function openClipboard() {
    sp.navRequest('${silfn:applicationURL()}<%=URLUtil.getURL(URLUtil.CMP_CLIPBOARD)%>Idle.jsp')
        .withParam('message','SHOWCLIPBOARD')
        .toTarget('IdleFrame')
        .go();
  }

  function goToLoginPage() {
	  location.href="${urlLogin}";
  }

  function searchEngine() {
    if (document.menuSearchForm.query.value !== "") {
      executeSearchActionToBodyPartTarget("AdvancedSearch", true);
    }
  }

  function advancedSearchEngine(){
    executeSearchActionToBodyPartTarget("ChangeSearchTypeToExpert", true);
  }

  function lastResultsSearchEngine(){
    executeSearchActionToBodyPartTarget("LastResults", false);
  }

  function executeSearchActionToBodyPartTarget(action, hasToSerializeForm) {
    var urlParameters = hasToSerializeForm ?
        jQuery(document.menuSearchForm).serializeFormJSON() : {};
    var url = sp.url.format("<%=m_sContext%>/RpdcSearch/jsp/" + action, urlParameters);
    spWindow.loadContent(url);
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
      return "<%=WebEncodeHelper.javaStringToHtmlString(spaceId)%>";
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

    /**
     * Reload bottom frame
     */
    function reloadSpacesBarFrame(tabId) {
      spLayout.getBody().load({
        "UserMenuDisplayMode" : tabId
      });
    }

    function getPersonalSpaceLabels()
    {
        var labels = new Array(2);
        labels[0] = "<%=WebEncodeHelper.javaStringToJsString(helper.getString("lookSilverpeasV5.personalSpace.select"))%>";
        labels[1] = "<%=WebEncodeHelper.javaStringToJsString(helper.getString("lookSilverpeasV5.personalSpace.remove.confirm"))%>";
        labels[2] = "<%=WebEncodeHelper.javaStringToJsString(helper.getString("lookSilverpeasV5.personalSpace.add"))%>";
        return labels;
    }

  /**
   * Using "jQuery" instead of "$" at this level prevents of getting conficts with another
   * javascript plugin.
   */
  //used by keyword autocompletion
  whenSilverpeasReady(function() {
    <% if(resourceSearchEngine.getBoolean("enableAutocompletion", false)){ %>
    jQuery("#query").autocomplete({
      source: "<%=m_sContext%>/AutocompleteServlet",
      minLength : <%=autocompletionMinChars%>
    });
    <%}%>
    <% if(displayPersonalSpace){ %>
    openMySpace({
      itemIdToSelect : '<%=componentId%>'
    });
    <%}%>
  });

</script>
<div class="fondDomainsBar">
<div id="domainsBar">
  <div id="recherche">
    <div id="submitRecherche">
      <form name="menuSearchForm" action="javascript:searchEngine()" method="get">
      <input name="query" size="30" id="query"/>
      <input type="hidden" name="mode" value="clear"/>
      <a href="javascript:searchEngine()"><img src="${icon_px}" width="20" height="20" border="0" alt=""/></a>
      </form>
    </div>
        <div id="bodyRecherche">
            <a href="javascript:advancedSearchEngine()"><fmt:message key="lookSilverpeasV5.AdvancedSearch" /></a> | <a href="javascript:lastResultsSearchEngine()"><fmt:message key="lookSilverpeasV5.LastSearchResults" /></a> | <a href="#" onclick="javascript:SP_openWindow('<%=m_sContext%>/RpdcSearch/jsp/help.jsp', 'Aide', '700', '270','scrollbars=yes, resizable, alwaysRaised');"><fmt:message key="lookSilverpeasV5.Help" /></a>
    </div>
    </div>
  <div id="spaceTransverse"></div>
  <div id="basSpaceTransverse">
        <table border="0" cellpadding="0" cellspacing="0" width="100%">
            <tr>
                <td class="basSpacesGauche"><img src="${icon_px}" width="8" height="8" alt=""/></td>
                <td class="basSpacesMilieu"><img src="${icon_px}" width="8" height="8" alt=""/></td>
                <td class="basSpacesDroite"><img src="${icon_px}" width="8" height="8" alt=""/></td>
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
		<center><br/><br/><fmt:message key="lookSilverpeasV5.loadingSpaces" /><br/><br/><img src="<c:url value='/util/icons/inProgress.gif'/>" alt="<fmt:message key="lookSilverpeasV5.loadingSpaces" />"/></center>
	  </div>
      <% if (!helper.isAnonymousAccess()) { %>
        <div id="spacePerso" class="spaceLevelPerso"><a class="spaceURL" href="javaScript:openMySpace();"><fmt:message key="lookSilverpeasV5.PersonalSpace" /></a></div>
      <% } %>
    </div>
    <div id="basSpaces">
        <table border="0" cellpadding="0" cellspacing="0" width="100%">
            <tr>
                <td class="basSpacesGauche"><img src="${icon_px}" width="8" height="8" alt=""/></td>
                <td class="basSpacesMilieu"><img src="${icon_px}" width="8" height="8" alt=""/></td>
                <td class="basSpacesDroite"><img src="${icon_px}" width="8" height="8" alt=""/></td>
            </tr>
        </table>
    </div>

    <div id="loginBox">
        <table width="100%">
        <tr>
            <td align="right" valign="top">
                <% if (helper.isAnonymousUser()) { %>
                    <%=button.print()%>
                <% } else { %>
                    <table border="0" cellpadding="0" cellspacing="2" id="userNav">
                        <tr><td colspan="2" align="right"><%=helper.getUserFullName()%></td></tr>
                        <tr><td colspan="2" align="right"><%=button.print()%></td></tr>
                    </table>
                <% } %>
            </td>
        </tr>
        </table>
    </div>
</div>

<!-- Custom domains bar javascript -->
<view:loadScript src="/util/javaScript/lookV5/navigation.js"/>
<view:loadScript src="/util/javaScript/lookV5/personalSpace.js"/>
<view:loadScript src="/util/javaScript/lookV5/login.js"/>
</div>