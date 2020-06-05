<%--
  ~ Copyright (C) 2000 - 2020 Silverpeas
  ~
  ~ This program is free software: you can redistribute it and/or modify
  ~ it under the terms of the GNU Affero General Public License as
  ~ published by the Free Software Foundation, either version 3 of the
  ~ License, or (at your option) any later version.
  ~
  ~ As a special exception to the terms and conditions of version 3.0 of
  ~ the GPL, you may redistribute this Program in connection with Free/Libre
  ~ Open Source Software ("FLOSS") applications as described in Silverpeas's
  ~ FLOSS exception.  You should have received a copy of the text describing
  ~ the FLOSS exception, and it is also available here:
  ~ "https://www.silverpeas.org/legal/floss_exception.html"
  ~
  ~ This program is distributed in the hope that it will be useful,
  ~ but WITHOUT ANY WARRANTY; without even the implied warranty of
  ~ MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  ~ GNU Affero General Public License for more details.
  ~
  ~ You should have received a copy of the GNU Affero General Public License
  ~ along with this program.  If not, see <http://www.gnu.org/licenses/>.
  --%>
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://www.silverpeas.com/tld/silverFunctions" prefix="silfn" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view" %>

<fmt:setLocale value="${sessionScope['SilverSessionController'].favoriteLanguage}"/>
<view:setBundle bundle="${requestScope.resources.multilangBundle}"/>
<view:setBundle bundle="${requestScope.resources.iconsBundle}" var="icons"/>

<c:set var="spaces" value="${requestScope.Spaces}"/>
<jsp:useBean id="spaces" type="org.silverpeas.web.jobstartpage.DisplaySorted[]"/>
<c:set var="subSpaces" value="${requestScope.SubSpaces}"/>
<jsp:useBean id="subSpaces" type="org.silverpeas.web.jobstartpage.DisplaySorted[]"/>
<c:set var="components" value="${requestScope.SpaceComponents}"/>
<jsp:useBean id="components" type="org.silverpeas.web.jobstartpage.DisplaySorted[]"/>
<c:set var="subComponents" value="${requestScope.SubSpaceComponents}"/>
<jsp:useBean id="subComponents" type="org.silverpeas.web.jobstartpage.DisplaySorted[]"/>
<c:set var="currentSpaceId" value="${requestScope.CurrentSpaceId}"/>
<c:set var="currentSubSpaceId" value="${requestScope.CurrentSubSpaceId}"/>

<fmt:message var="domainsLabel" key="GML.domains"/>
<fmt:message var="chooseLabel" key="JSPP.Choose"/>
<fmt:message var="pxUrl" key="JSPP.px" bundle="${icons}"/>
<c:url var="pxUrl" value="${pxUrl}"/>
<fmt:message var="homeSpaceIconUrl" key="JSPP.homeSpaceIcon" bundle="${icons}"/>
<c:url var="homeSpaceIconUrl" value="${homeSpaceIconUrl}"/>
<fmt:message var="backToMainSpaceLabel" key="JSPP.BackToMainSpacePage"/>

<%@ include file="check.jsp" %>

<script type="text/javascript">
  function jumpToSpace(spaceId) {
    spAdminWindow.loadSpace(spaceId);
  }
  function jumpToSubSpace(spaceId) {
    spAdminWindow.loadSubSpace(spaceId);
  }
  function jumpToComponent(componentId) {
    spAdminWindow.loadComponent(componentId);
  }
</script>

<style type="text/css">
  .component-icon {
    margin: 1px;
    vertical-align: middle;
  }

  #space-icon {
    vertical-align: middle;
  }
</style>
<form name="privateDomainsForm" action="javascript:void(0)">
  <div class="intfdcolor">
    <span class="treeview-label">${domainsLabel} : </span>
  </div>
  <div class="intfdcolor51">
    <div class="treeview_selectSpace">
      <input name="privateSubDomain" type="hidden"/>
      <img src="${pxUrl}" height="20" width="0" align="middle"/>
      <span class="selectNS">
        <select name="privateDomain" size=1 onchange="jumpToSpace(document.privateDomainsForm.privateDomain.value)">
          <option value="">${chooseLabel}</option>
          <option value="">--------------------</option>
          <c:forEach var="space" items="${spaces}">
            ${space.htmlLine}
          </c:forEach>
        </select>
      </span>
      <a href="javascript:onclick=jumpToSpace(${currentSpaceId})"><img id="space-icon" src="${homeSpaceIconUrl}" align="middle" alt="${backToMainSpaceLabel}" title="${backToMainSpaceLabel}"/></a>
    </div>
  </div>
  <c:if test="${silfn:isDefined(currentSpaceId)}">
    <div class="intfdcolor51">
      <div class="treeview_contentSpace">
        <c:forEach var="subSpace" items="${subSpaces}">
          ${subSpace.htmlLine}
        <c:if test="${silfn:isDefined(currentSubSpaceId) and currentSubSpaceId eq subSpace.id}">
        <c:forEach var="subComponent" items="${subComponents}">
          ${subComponent.htmlLine}
        </c:forEach>
        </c:if>
        </c:forEach>
        <c:forEach var="component" items="${components}">
          ${component.htmlLine}
        </c:forEach>
    </div>
  </c:if>
</form>

<script type="text/javascript">
  <c:if test="${silfn:isDefined(currentSubSpaceId)}">
  var $subSpaceElement = document.getElementById(
      'navSpace${currentSubSpaceId}').nextSibling.nextSibling.nextSibling;
  var $view = spAdminLayout.getBody().getNavigation().getContainer();
  sp.element.scrollToIfNotFullyInView($subSpaceElement, $view);
  </c:if>
  spAdminLayout.getBody().getNavigation().dispatchEvent('load');
</script>