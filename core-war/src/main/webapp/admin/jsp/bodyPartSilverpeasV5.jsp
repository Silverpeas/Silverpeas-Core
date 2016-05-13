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

<%@ page import="org.silverpeas.core.web.look.LookHelper"%>
<%@ page import="org.silverpeas.core.util.StringUtil" %>
<%@ page import="org.silverpeas.core.util.URLUtil" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>
<%@ include file="importFrameSet.jsp" %>

<%-- Set resource bundle --%>
<fmt:setLocale value="${sessionScope['SilverSessionController'].favoriteLanguage}"/>
<view:setBundle basename="org.silverpeas.lookSilverpeasV5.multilang.lookBundle"/>

<fmt:message var="redExtLabel" key="lookSilverpeasV5.reductExtend" />

<%
String strGoToNew 	= (String) session.getAttribute("gotoNew");
String spaceId 		= request.getParameter("SpaceId");
String subSpaceId 	= request.getParameter("SubSpaceId");
String componentId	= request.getParameter("ComponentId");
String login		= request.getParameter("Login");

LookHelper helper = LookHelper.getLookHelper(session);

String menuWidth = helper.getSettings("domainsBarFramesetWidth", "255") + "px";

  StringBuilder paramsForDomainsBar = new StringBuilder().append("{");
  if ("1".equals(request.getParameter("FromTopBar"))) {
    if (spaceId != null) {
      paramsForDomainsBar.append("privateDomain:'").append(spaceId).append("', privateSubDomain:'")
          .append(subSpaceId).append("', FromTopBar:'1'");
    }
    ;
  } else if (componentId != null) {
    paramsForDomainsBar.append("privateDomain:'', component_id:'").append(componentId).append("'");
  } else {
    paramsForDomainsBar.append("privateDomain:'").append(spaceId).append("'");
  }

//Allow to force a page only on login and when user clicks on logo
boolean displayLoginHomepage = false;
String loginHomepage = helper.getSettings("loginHomepage", "");
if (StringUtil.isDefined(loginHomepage) && StringUtil.isDefined(login) &&
    (!StringUtil.isDefined(spaceId) && !StringUtil.isDefined(subSpaceId) && !StringUtil.isDefined(componentId) && !StringUtil.isDefined(strGoToNew))) {
	displayLoginHomepage = true;
}

String frameURL = "";
if (displayLoginHomepage) {
	frameURL = loginHomepage;
} else if (strGoToNew == null) {
	if (StringUtil.isDefined(componentId)) {
		frameURL = URLUtil.getApplicationURL()+ URLUtil.getURL(null, componentId)+"Main";
	} else {
		String homePage = helper.getSettings("defaultHomepage", "/dt");
		String param = "";
		if (StringUtil.isDefined(spaceId)) {
		    param = "?SpaceId=" + spaceId;
		}
		frameURL = URLUtil.getApplicationURL()+homePage+param;
	}
} else {
    frameURL = URLUtil.getApplicationURL()+strGoToNew;
    if(strGoToNew.startsWith(URLUtil.getApplicationURL())) {
      frameURL = strGoToNew;
    }
}

session.removeAttribute("goto");
session.removeAttribute("gotoNew");
session.removeAttribute("RedirectToComponentId");
session.removeAttribute("RedirectToSpaceId");
%>
<style type="text/css">

  #bodyLayout {
    width: 100%;
    flex: 1;
    display: flex;
    flex-direction: row;
  }

  #menuContainer {
    position: relative;
    overflow: auto;
    width: <%=menuWidth%>;
  }

  #contentContainer {
    flex: 1;
    height: 100%;
  }

  #redExp {
    display: table;
  }

  #redExp div {
    margin: 0;
    padding: 0 2px 0 0;
    border: none;
    display: table-cell;
    cursor: pointer;
  }
</style>
<div id="redExp" style="display: none">
  <div id="toggleMenu"><img src="icons/silverpeasV5/reduct.gif" alt="${redExtLabel}" title="${redExtLabel}"/></div>
  <div id="toggleHeader"><img src="icons/silverpeasV5/reductTopBar.gif" alt="${redExtLabel}" title="${redExtLabel}"/></div>
</div>
<div id="bodyLayout">
  <div id="menuContainer"></div>
  <div id="contentContainer">
    <iframe src="<%=frameURL%>" marginwidth="0" id="MyMain" name="MyMain" marginheight="0" frameborder="0" scrolling="auto" width="100%" height="100%"></iframe>
  </div>
</div>
<script type="text/javascript">
  var resolveContentFrameLoadPromise;
  var rejectContentFrameLoadPromise;
  function newContentFrameLoadPromise() {
    return new Promise(function(resolve, reject) {
      resolveContentFrameLoadPromise = resolve;
      rejectContentFrameLoadPromise = reject;
    });
  }

  var bodyContext = {
    toggleContainer: document.querySelector("#redExp"),
    bodyLayout: document.querySelector("#bodyLayout"),
    menuContainer : document.querySelector("#menuContainer"),
    menuFrame: document.querySelector("#SpacesBar"),
    contentFrame: document.querySelector("#MyMain"),
    contentContainer : document.querySelector("#contentContainer")
  };

  bodyContext.contentFrame.setAttribute('webkitallowfullscreen', 'true');
  bodyContext.contentFrame.setAttribute('mozallowfullscreen', 'true');
  bodyContext.contentFrame.setAttribute('allowfullscreen', 'true');

  function applyBodyLayoutPartAutoSize() {
    bodyContext.bodyLayout.style.height = bodyContext.bodyLayout.parentNode.style.height;
  }

  function reloadBodyMenuPart(urlParameters) {
    jQuery.progressMessage();
    bodyContext.toggleContainer.style.display = 'none';
    var parameters = extendsObject({
      "privateDomain" : "", "privateSubDomain" : "", "component_id" : ""
    }, urlParameters);
    var ajaxConfig = sp.ajaxConfig('<c:url value="/admin/jsp/DomainsBarSilverpeasV5.jsp"/>')
        .withParams(parameters);
    return sp.load(bodyContext.menuContainer, ajaxConfig).then(function() {
      bodyContext.toggleContainer.style.display = '';
    });
  }

  function reloadBodyMenuAndHeaderParts(urlParameters) {
    reloadHeaderPart();
    reloadBodyMenuPart(urlParameters);
  }

  function reloadBodyContentPart(url) {
    jQuery.progressMessage();
    var promise = newContentFrameLoadPromise();
    top.MyMain.location.href = url;
    return promise;
  }

  (function() {
    newContentFrameLoadPromise();
    applyBodyLayoutPartAutoSize();
    reloadBodyMenuPart(<%=paramsForDomainsBar.append('}')%>);

    document.querySelector("#toggleHeader").addEventListener('click', toggleHeaderPart);
    document.querySelector("#toggleMenu").addEventListener('click', toggleMenuPart);
    bodyContext.contentFrame.addEventListener('load', function() {
      resolveContentFrameLoadPromise();
      jQuery.closeProgressMessage();
    });
  })();
</script>