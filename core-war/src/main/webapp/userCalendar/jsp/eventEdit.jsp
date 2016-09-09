<%--
  ~ Copyright (C) 2000 - 2016 Silverpeas
  ~
  ~ This program is free software: you can redistribute it and/or modify
  ~ it under the terms of the GNU Affero General Public License as
  ~ published by the Free Software Foundation, either version 3 of the
  ~ License, or (at your option) any later version.
  ~
  ~ As a special exception to the terms and conditions of version 3.0 of
  ~ the GPL, you may redistribute this Program in connection with Free/Libre
  ~ Open Source Software ("FLOSS") applications as described in Silverpeas's
  ~ FLOSS exception. You should have received a copy of the text describing
  ~ the FLOSS exception, and it is also available here:
  ~ "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
  ~
  ~ This program is distributed in the hope that it will be useful,
  ~ but WITHOUT ANY WARRANTY; without even the implied warranty of
  ~ MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  ~ GNU Affero General Public License for more details.
  ~
  ~ You should have received a copy of the GNU Affero General Public License
  ~ along with this program.  If not, see <http://www.gnu.org/licenses/>.
  --%>

<%@ include file="check.jsp" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://www.silverpeas.com/tld/silverFunctions" prefix="silfn" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view" %>

<c:set var="currentUserLanguage" value="${requestScope.resources.language}"/>
<fmt:setLocale value="${currentUserLanguage}"/>
<view:setBundle bundle="${requestScope.resources.multilangBundle}"/>
<view:setBundle bundle="${requestScope.resources.iconsBundle}" var="icons"/>
<c:url var="componentUriBase" value="${requestScope.componentUriBase}"/>

<c:set var="currentUser"            value="${requestScope.currentUser}"/>
<c:set var="currentUserId"          value="${currentUser.id}"/>
<c:set var="componentId"            value="${requestScope.browseContext[3]}"/>
<c:set var="highestUserRole"        value="${requestScope.highestUserRole}"/>
<c:set var="timeWindowViewContext"  value="${requestScope.timeWindowViewContext}"/>

<c:set var="event" value="${requestScope.event}"/>
<c:set var="target" value="add"/>
<c:if test="${event != null}">
  <c:set var="target" value="${event.id}"/>
</c:if>

<view:setConstant var="adminRole" constant="org.silverpeas.core.admin.user.model.SilverpeasRole.admin"/>

<fmt:message var="save" key="GML.validate"/>
<fmt:message var="cancel" key="GML.cancel"/>

<fmt:message key="calendar.menu.item.event.add" var="addEventLabel"/>

<c:url var="backUri" value="${requestScope.navigationContext.previousNavigationStep.uri}"/>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
  <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
  <view:looknfeel/>
  <script type="text/javascript">

    function save() {
      notyWarning("Not yet implemented");
    }

    function cancel() {
      <c:choose>
      <c:when test="${event == null}">
      silverpeasFormSubmit(sp.formConfig('${backUri}'));
      </c:when>
      <c:otherwise>
      silverpeasFormSubmit(sp.formConfig('${componentUriBase}calendars/events/${target}'));
      </c:otherwise>
      </c:choose>
    }
  </script>
</head>
<body>
<view:operationPane>
  <c:if test="${highestUserRole.isGreaterThanOrEquals(adminRole)}">
    <fmt:message key="userCalendar.icons.addEvent" var="opIcon" bundle="${icons}"/>
    <c:url var="opIcon" value="${opIcon}"/>
    <view:operationOfCreation action="${componentUriBase}calendars/events/new" altText="${addEventLabel}" icon="${opIcon}"/>
  </c:if>
</view:operationPane>
<view:window>
  <view:frame>
    <view:buttonPane>
      <view:button label="${save}" action="javascript:save();"/>
      <view:button label="${cancel}" action="javascript:cancel();"/>
    </view:buttonPane>
  </view:frame>
</view:window>
</body>
</html>