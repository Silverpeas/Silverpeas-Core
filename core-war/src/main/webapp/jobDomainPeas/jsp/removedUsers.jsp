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
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="org.silverpeas.core.admin.AdminSettings" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://www.silverpeas.com/tld/silverFunctions" prefix="silfn" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view" %>
<%@ taglib tagdir="/WEB-INF/tags/silverpeas/util" prefix="viewTags" %>

<c:set var="language" value="${sessionScope.SilverSessionController.favoriteLanguage}"/>
<fmt:setLocale value="${language}"/>
<view:setBundle bundle="${requestScope.resources.multilangBundle}"/>

<c:set var="isAutomaticDeletionEnabled" value="<%=AdminSettings.isAutomaticDeletionOfRemovedUsersEnabled()%>"/>
<c:set var="automaticDeletionDelay" value="<%=AdminSettings.getDeletionOfRemovedUsersDayDelay()%>"/>
<c:set var="currentRequester" value="${sessionScope.SilverSessionController.currentUserDetail}"/>
<jsp:useBean id="currentRequester" type="org.silverpeas.core.admin.user.model.User"/>
<c:set var="userZoneId" value="${currentRequester.userPreferences.zoneId}"/>

<fmt:message var="removedUserLabel" key="JDP.removedUsers"/>
<fmt:message var="restoreSelection" key="GML.action.selection.restore"/>
<fmt:message var="restoreSelectionConfirm" key="GML.action.selection.restore.confirm"/>
<fmt:message var="deleteSelection" key="GML.action.selection.delete"/>
<fmt:message var="deleteSelectionConfirm" key="GML.action.selection.delete.confirm"/>
<fmt:message var="firstName" key="GML.surname"/>
<fmt:message var="lastName" key="GML.lastName"/>
<fmt:message var="login" key="GML.login"/>
<fmt:message var="removeDate" key="JDP.userRemoveDate"/>
<fmt:message var="automaticDeleteDate" key="JDP.userAutomaticDeletionDate"/>
<fmt:message var="back" key="GML.back"/>

<c:set var="help">
  <view:applyTemplate locationBase="core:admin/domain" name="removedUserHelp">
    <view:templateParam name="dayDelay" value="${isAutomaticDeletionEnabled ? automaticDeletionDelay : ''}"/>
  </view:applyTemplate>
</c:set>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
  <title></title>
  <view:looknfeel withFieldsetStyle="true"/>
  <view:includePlugin name="qtip"/>
  <script type="application/javascript">
    var arrayPaneAjaxControl;
    var checkboxMonitor = sp.selection.newCheckboxMonitor('#dynamic-container input[name=selection]');

    function restoreSelection() {
      jQuery.popup.confirm('${silfn:escapeJs(restoreSelectionConfirm)}', function() {
        var ajaxRequest = sp.ajaxRequest("restoreUsers").byPostMethod();
        checkboxMonitor.prepareAjaxRequest(ajaxRequest);
        ajaxRequest.send().then(arrayPaneAjaxControl.refreshFromRequestResponse);
      });
    }

    function deleteSelection() {
      jQuery.popup.confirm('${silfn:escapeJs(deleteSelectionConfirm)}', function() {
        var ajaxRequest = sp.ajaxRequest("deleteUsers").byPostMethod();
        checkboxMonitor.prepareAjaxRequest(ajaxRequest);
        ajaxRequest.send().then(arrayPaneAjaxControl.refreshFromRequestResponse);
      });
    }

    function back() {
      sp.navRequest('domainContent').go();
    }
  </script>
</head>
<c:set var="domain"       value="${requestScope.domain}"/>
<jsp:useBean id="domain" type="org.silverpeas.core.admin.domain.model.Domain"/>
<c:set var="removedUsers" value="${requestScope.removedUsers}"/>
<jsp:useBean id="removedUsers" type="java.util.List<org.silverpeas.web.jobdomain.servlets.RemovedUserUIEntity>"/>
<c:set var="currentUser"  value="${requestScope.theUser}"/>
<jsp:useBean id="currentUser" type="org.silverpeas.core.admin.user.model.UserDetail"/>
<body id="domainContent" class="page_content_admin">
<fmt:message var="domainTitle" key="JDP.domains"/>
<view:browseBar componentId="${domainTitle}">
  <view:browseBarElt label="${domain.name}" link="domainContent?Iddomain=${domain.id}"/>
  <view:browseBarElt label="${silfn:capitalize(removedUserLabel)}" link=""/>
</view:browseBar>
<view:operationPane>
  <view:operation action="javascript:restoreSelection()" icon="" altText="${restoreSelection}"/>
  <view:operationSeparator/>
  <view:operation action="javascript:deleteSelection()" icon="" altText="${deleteSelection}"/>
</view:operationPane>
<view:window>
  <view:frame>
    <div id="dynamic-container">
      <div class="principalContent">
        <h2 class="principal-content-title sql-domain">${silfn:escapeHtml(domain.name)}</h2>
        <div id="number-user-group-domainContent">
          <span id="number-user-domainContent">${removedUsers.size()} ${removedUserLabel}</span>
          <c:if test="${not empty fn:trim(help)}">
            <img class="infoBulle" src="<c:url value="/util/icons/help.png"/>" alt="info"/>
          </c:if>
        </div>
        <c:if test="${fn:length(domain.description) > 0}">
          <p id="description-domainContent">${silfn:escapeHtml(domain.description)}</p>
        </c:if>
      </div>
      <c:if test="${currentUser.accessAdmin}">
        <view:arrayPane var="listOfRemovedUsers" routingAddress="displayRemovedUsers" numberLinesPerPage="25">
          <view:arrayColumn title="" sortable="false"/>
          <view:arrayColumn title="${lastName}" compareOn="${u -> fn:toLowerCase(u.data.lastName)}"/>
          <view:arrayColumn title="${firstName}" compareOn="${u -> fn:toLowerCase(u.data.firstName)}"/>
          <view:arrayColumn title="${login}" compareOn="${u -> fn:toLowerCase(u.data.login)}"/>
          <view:arrayColumn title="${removeDate}" compareOn="${u -> u.data.stateSaveDate}"/>
          <c:if test="${isAutomaticDeletionEnabled}">
            <view:arrayColumn title="${automaticDeleteDate}" compareOn="${u -> u.data.stateSaveDate}"/>
          </c:if>
          <view:arrayLines var="aUser" items="${removedUsers}">
            <view:arrayLine>
              <view:arrayCellCheckbox name="selection" value="${aUser.id}" checked="false"/>
              <view:arrayCellText text="${silfn:escapeHtml(aUser.data.lastName)}"/>
              <view:arrayCellText text="${silfn:escapeHtml(aUser.data.firstName)}"/>
              <view:arrayCellText text="${silfn:escapeHtml(aUser.data.login)}"/>
              <view:arrayCellText text="${silfn:formatDateAndHour(aUser.data.stateSaveDate, language)}"/>
              <c:if test="${isAutomaticDeletionEnabled}">
                <fmt:message var="automaticDeleteDelay" key="GML.nbDays"><fmt:param value="${aUser.automaticDeletionDayDelay}"/></fmt:message>
                <view:arrayCellText text="${silfn:formatTemporal(aUser.automaticDeletionDayDate, userZoneId, language)
                                                   .concat(' (').concat(automaticDeleteDelay).concat(')')}"/>
              </c:if>
            </view:arrayLine>
          </view:arrayLines>
        </view:arrayPane>
        <script type="text/javascript">
          whenSilverpeasReady(function() {
            checkboxMonitor.pageChanged();
            arrayPaneAjaxControl = sp.arrayPane.ajaxControls('#dynamic-container');
            <c:if test="${not empty fn:trim(help)}">
            TipManager.simpleHelp(".infoBulle", "${silfn:escapeJs(help)}");
            </c:if>
          });
        </script>
        <span>&nbsp;</span>
        <view:buttonPane>
          <view:button label="${back}"   action="javascript:onClick=back()"/>
        </view:buttonPane>
      </c:if>
    </div>
  </view:frame>
</view:window>
</body>

