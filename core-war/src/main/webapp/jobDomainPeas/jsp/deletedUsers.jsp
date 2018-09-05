<%--
  ~ Copyright (C) 2000 - 2018 Silverpeas
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
<%@ taglib tagdir="/WEB-INF/tags/silverpeas/util" prefix="viewTags" %>
<c:set var="language" value="${sessionScope.sessionController.language}"/>
<fmt:setLocale value="${language}"/>
<view:setBundle bundle="${requestScope.resources.multilangBundle}"/>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
  <view:looknfeel withFieldsetStyle="true"/>
  <script type="application/javascript">
    function validate() {
      document.BlankUsers.submit();
    }

    function cancel() {
      document.BlankUsers.action = 'domainContent';
      document.BlankUsers.submit();
    }
  </script>
</head>
<c:set var="domain"       value="${requestScope.domain}"/>
<c:set var="deletedUsers" value="${requestScope.deletedUsers}"/>
<c:set var="currentUser"  value="${requestScope.theUser}"/>
<jsp:useBean id="domain"
             type="org.silverpeas.core.admin.domain.model.Domain"/>
<jsp:useBean id="deletedUsers"
             type="java.util.List<org.silverpeas.core.admin.user.model.UserDetail>"/>
<jsp:useBean id="currentUser"
             type="org.silverpeas.core.admin.user.model.UserDetail"/>
<body id="domainContent" class="page_content_admin">
<fmt:message var="domainTitle" key="JDP.domains"/>
<view:browseBar componentId="${domainTitle}">
  <view:browseBarElt label="${domain.name}" link="domainContent?Iddomain=${domain.id}"/>
</view:browseBar>
<view:window>
  <view:frame>
    <div class="principalContent">
      <h2 class="principal-content-title sql-domain">${silfn:escapeHtml(domain.name)}</h2>
      <div id="number-user-group-domainContent">
        <span id="number-user-domainContent">${deletedUsers.size()} <fmt:message key="JDP.deletedUsers"/></span>
      </div>
      <c:if test="${fn:length(domain.description) > 0}">
        <p id="description-domainContent">${silfn:escapeHtml(domain.description)}</p>
      </c:if>
    </div>
    <c:if test="${currentUser.accessAdmin}">
      <fmt:message var="firstName"    key="GML.surname"/>
      <fmt:message var="lastName"     key="GML.lastName"/>
      <fmt:message var="deletionDate" key="JDP.userDeletionDate"/>
      <fmt:message var="anonymize"    key="JDP.blankUser"/>
      <fmt:message var="validate"     key="GML.validate"/>
      <fmt:message var="cancel"       key="GML.cancel"/>
      <form name="BlankUsers" action="blankUsers" method="POST">
        <view:arrayPane var="deletedUsers" routingAddress="displayDeletedUsers" numberLinesPerPage="25">
          <view:arrayColumn title="${lastName}"     compareOn="${u -> u.lastName}" />
          <view:arrayColumn title="${firstName}"    compareOn="${u -> u.firstName}"/>
          <view:arrayColumn title="${deletionDate}" compareOn="${u -> u.stateSaveDate}"/>
          <view:arrayColumn title="${anonymize}"    sortable="false"/>
          <view:arrayLines var="aUser" items="${deletedUsers}">
            <jsp:useBean id="aUser" type="org.silverpeas.core.admin.user.model.UserDetail"/>
            <view:arrayLine>
              <view:arrayCellText text="${silfn:escapeHtml(aUser.lastName)}"/>
              <view:arrayCellText text="${silfn:escapeHtml(aUser.firstName)}"/>
              <view:arrayCellText text="${silfn:formatDateAndHour(aUser.stateSaveDate, language)}"/>
              <view:arrayCellCheckbox name="blank_${aUser.id}" value="${aUser.id}" checked="false"/>
            </view:arrayLine>
          </view:arrayLines>
        </view:arrayPane>
        <span>&nbsp;</span>
        <view:buttonPane>
          <view:button label="${validate}" action="javascript:onClick=validate()"/>
          <view:button label="${cancel}"   action="javascript:onClick=cancel()"/>
        </view:buttonPane>
      </form>
    </c:if>
  </view:frame>
</view:window>
</body>

