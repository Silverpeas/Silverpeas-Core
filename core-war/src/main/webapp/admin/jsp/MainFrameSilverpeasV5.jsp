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
StringBuilder			frameBottomParams		= new StringBuilder().append("{");
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

		frameBottomParams.append("'SpaceId':'").append(spaceId).append("'");
	} else {
		helper.setComponentIdAndSpaceIds(null, null, componentIdFromRedirect);
		frameBottomParams.append("'SpaceId':''").append(",'ComponentId':'")
        .append(componentIdFromRedirect).append("'");
	}

	gef.setSpaceIdForCurrentRequest(helper.getSubSpaceId());

	if (login) {
		frameBottomParams.append(",'Login':'1'");
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

      String bannerHeight = helper.getSettings("bannerHeight", "115") + "px";
      String footerHeight = helper.getSettings("footerHeight", "26") + "px";
      if (!helper.displayPDCFrame()) {
        footerHeight = "0px";
      }
%>

<c:set var="pdcActivated" value="<%=helper.displayPDCFrame()%>"/>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<title><%=generalMessage.getString("GML.popupTitle")%></title>
<link rel="SHORTCUT ICON" href="<%=request.getContextPath()%>/util/icons/favicon.ico"/>
<view:looknfeel/>
<style type="text/css">
  body {
    margin: 0;
    padding: 0;
    border: none;
    overflow: hidden;
  }

  .hidden-part {
    margin: 0;
    padding: 0;
    border: none;
    display: none;
  }

  #mainLayout {
    width: 100%;
    display: flex;
    flex-wrap: wrap;
    flex-direction: column;
  }

  #layoutHeaderPart, #layoutBodyPart, #layoutFooterPart {
    padding: 0;
    margin: 0;
    border: none;
  }

  #layoutHeaderPart {
    width: 100%;
    height: <%=bannerHeight%>;
  }

  #layoutFooterPart {
    width: 100%;
    height: <%=footerHeight%>;
  }

  #layoutBodyPart {
    width: 100%;
    display: table;
  }
</style>
<meta name="viewport" content="initial-scale=1.0"/>
</head>
<body>
<% if (attachmentId != null) {
	session.setAttribute("RedirectToAttachmentId", null);
	String mapping = (String) session.getAttribute("RedirectToMapping");
%>
	<script type="text/javascript">
		SP_openWindow('<%=m_sContext%>/<%=mapping%>/<%=attachmentId%>', 'Fichier', '800', '600', 'directories=0,menubar=1,toolbar=1,scrollbars=1,location=1,alwaysRaised');
	</script>
<% } %>

<div id="mainLayout">
  <div id="layoutHeaderPart"></div>
  <div id="layoutBodyPart"></div>
  <div id="layoutFooterPart" class="hidden-part"></div>
</div>
<div class="hidden-part" style="height: 0">
  <iframe src="../../clipboard/jsp/IdleSilverpeasV5.jsp" name="IdleFrame" marginwidth="0" marginheight="0" scrolling="no" frameborder="0"></iframe>
  <iframe src="<%=m_sContext%>/Ragenda/jsp/importCalendar" name="importFrame" marginwidth="0" marginheight="0" scrolling="no" frameborder="0"></iframe>
</div>

<script type="text/javascript">
  var mainContext = {
    headerLayout : document.querySelector("#layoutHeaderPart"),
    bodyLayout : document.querySelector("#layoutBodyPart"),
    footerLayout : document.querySelector("#layoutFooterPart")
  };

  function applyMainFrameAutoSize() {
    var bodyLayoutHeight = window.innerHeight - mainContext.headerLayout.offsetHeight;
    <c:if test="${pdcActivated}">
    bodyLayoutHeight -= mainContext.footerLayout.offsetHeight;
    </c:if>
    mainContext.bodyLayout.style.height = bodyLayoutHeight + 'px';
    if (typeof applyBodyLayoutPartAutoSize !== 'undefined') {
      applyBodyLayoutPartAutoSize();
    }
  }

  function loadPdcPart(urlParameters) {
    <c:if test="${pdcActivated}">
    var parameters = extendsObject({
      "action" : "ChangeSearchTypeToExpert",
      "SearchPage" : "/admin/jsp/pdcSearchSilverpeasV5.jsp"
    }, urlParameters);
    var action = parameters.action;
    delete parameters.action;
    var ajaxConfig = sp.ajaxConfig('<c:url value="/RpdcSearch/jsp/"/>' + action)
        .withParams(parameters);
    return sp.load(mainContext.footerLayout, ajaxConfig).then(function() {
      applyMainFrameAutoSize();
    });
    </c:if>
  }

  function showPdcPart() {
    <c:if test="${pdcActivated}">
    mainContext.footerLayout.classList.remove('hidden-part');
    applyMainFrameAutoSize();
    </c:if>
  }

  function hidePdcPart() {
    <c:if test="${pdcActivated}">
    mainContext.footerLayout.classList.add('hidden-part');
    applyMainFrameAutoSize();
    </c:if>
  }

  function reloadHeaderPart(urlParameters) {
    return sp.load(mainContext.headerLayout,
        sp.ajaxConfig('<c:url value="/admin/jsp/TopBarSilverpeasV5.jsp"/>').withParams(
            urlParameters));
  }

  function reloadBodyPart(urlParameters) {
    return sp.load(mainContext.bodyLayout,
        sp.ajaxConfig('<c:url value="/admin/jsp/bodyPartSilverpeasV5.jsp"/>').withParams(
            urlParameters));
  }

  var toggleHeaderPart = function() {
    var icon = this.querySelector('img');
    if (mainContext.headerLayout.style.display !== 'none') {
      mainContext.headerLayout.style.display = 'none';
      icon.src = "icons/silverpeasV5/extendTopBar.gif";
    } else {
      mainContext.headerLayout.style.display = '';
      icon.src = "icons/silverpeasV5/reductTopBar.gif";
    }
    icon.blur();
    applyMainFrameAutoSize();
  };

  var toggleMenuPart = function() {
    var icon = this.querySelector('img');
    if (bodyContext.menuContainer.style.display !== 'none') {
      bodyContext.menuContainer.style.display = 'none';
      icon.src = "icons/silverpeasV5/extend.gif";
    } else {
      bodyContext.menuContainer.style.display = '';
      icon.src = "icons/silverpeasV5/reduct.gif";
    }
    icon.blur();
    applyMainFrameAutoSize();
  };

  (function() {
    applyMainFrameAutoSize();
    reloadHeaderPart();
    reloadBodyPart(<%=frameBottomParams.append('}')%>);

    var timer_resize;
    window.addEventListener('resize', function() {
      clearTimeout(timer_resize);
      timer_resize = setTimeout(function() {
        applyMainFrameAutoSize();
      }, 0);
    });
  })();
</script>
<view:progressMessage/>
</body>
</html>
<% } %>