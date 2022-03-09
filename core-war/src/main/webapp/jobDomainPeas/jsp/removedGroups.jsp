<%--
  ~ Copyright (C) 2000 - 2022 Silverpeas
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

<c:set var="isAutomaticDeletionEnabled" value="<%=AdminSettings.isAutomaticDeletionOfRemovedGroupsEnabled()%>"/>
<c:set var="automaticDeletionDelay" value="<%=AdminSettings.getDeletionOfRemovedGroupsDayDelay()%>"/>
<c:set var="currentRequester" value="${sessionScope.SilverSessionController.currentUserDetail}"/>
<jsp:useBean id="currentRequester" type="org.silverpeas.core.admin.user.model.User"/>
<c:set var="userZoneId" value="${currentRequester.userPreferences.zoneId}"/>

<c:set var="reloadDomainNavigationFrame" value="${requestScope.reloadDomainNavigationFrame}"/>

<fmt:message var="mixedDomainLabel" key="JDP.domainMixt"/>
<fmt:message var="removedGroupLabel" key="JDP.removedGroups"/>
<fmt:message var="restoreSelection" key="GML.action.selection.restore"/>
<c:set var="restoreSelectionConfirm">
  <div class="confirm-dialog">
    <fmt:message key="GML.action.selection.restore.confirm"/>
    <div class="help"><fmt:message key="JDP.restoreSelectionConfirmHelp"/></div>
  </div>
</c:set>
<fmt:message var="deleteSelection" key="GML.action.selection.delete"/>
<c:set var="deleteSelectionConfirm">
  <div class="confirm-dialog">
    <fmt:message key="GML.action.selection.delete.confirm"/>
    <div class="help"><fmt:message key="JDP.deleteSelectionConfirmHelp"/></div>
  </div>
</c:set>
<fmt:message var="name" key="GML.name"/>
<fmt:message var="path" key="GML.groupPath"/>
<fmt:message var="login" key="GML.login"/>
<fmt:message var="removeDate" key="JDP.groupRemoveDate"/>
<fmt:message var="automaticDeleteDate" key="JDP.groupAutomaticDeletionDate"/>
<fmt:message var="back" key="GML.back"/>

<c:set var="help">
  <view:applyTemplate locationBase="core:admin/domain" name="removedGroupHelp">
    <view:templateParam name="dayDelay" value="${isAutomaticDeletionEnabled ? automaticDeletionDelay : ''}"/>
  </view:applyTemplate>
</c:set>

<view:sp-page>
<view:sp-head-part withFieldsetStyle="true">
  <view:includePlugin name="qtip"/>
  <style type="text/css">
    .confirm-dialog .help {
      padding-top: 5px;
      font-size: 0.8em;
    }
  </style>
  <script type="application/javascript">
    var arrayPaneAjaxControl;
    var checkboxMonitor = sp.selection.newCheckboxMonitor('#dynamic-container input[name=selection]');

    function restoreSelection() {
      jQuery.popup.confirm('${silfn:escapeJs(restoreSelectionConfirm)}', function() {
        var ajaxRequest = sp.ajaxRequest("restoreGroups").byPostMethod();
        checkboxMonitor.prepareAjaxRequest(ajaxRequest);
        ajaxRequest.send().then(arrayPaneAjaxControl.refreshFromRequestResponse);
      });
    }

    function deleteSelection() {
      jQuery.popup.confirm('${silfn:escapeJs(deleteSelectionConfirm)}', function() {
        var ajaxRequest = sp.ajaxRequest("deleteGroups").byPostMethod();
        checkboxMonitor.prepareAjaxRequest(ajaxRequest);
        ajaxRequest.send().then(arrayPaneAjaxControl.refreshFromRequestResponse);
      });
    }

    function back() {
      sp.navRequest('domainContent').go();
    }
  </script>
</view:sp-head-part>
<c:set var="domain"       value="${requestScope.domain}"/>
<jsp:useBean id="domain" type="org.silverpeas.core.admin.domain.model.Domain"/>
<c:set var="domainName" value="${domain.mixedOne ? mixedDomainLabel : domain.name}"/>
<c:set var="domainDescription" value="${domain.mixedOne ? '' : domain.description}"/>
<c:set var="removedGroups" value="${requestScope.removedGroups}"/>
<jsp:useBean id="removedGroups" type="java.util.List<org.silverpeas.web.jobdomain.servlets.RemovedGroupUIEntity>"/>
<c:set var="currentUser"  value="${requestScope.theUser}"/>
<jsp:useBean id="currentUser" type="org.silverpeas.core.admin.user.model.UserDetail"/>
<view:sp-body-part id="domainContent" cssClass="page_content_admin">
<fmt:message var="domainTitle" key="JDP.domains"/>
<view:browseBar componentId="${domainTitle}">
  <view:browseBarElt label="${domainName}" link="domainContent?Iddomain=${domain.id}"/>
  <view:browseBarElt label="${silfn:capitalize(removedGroupLabel)}" link=""/>
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
        <h2 class="principal-content-title sql-domain">${silfn:escapeHtml(domainName)}</h2>
        <div id="number-group-group-domainContent">
          <span id="number-group-domainContent">${removedGroups.size()} ${removedGroupLabel}</span>
          <c:if test="${not empty fn:trim(help)}">
            <img class="infoBulle" src="<c:url value="/util/icons/help.png"/>" alt="info"/>
          </c:if>
        </div>
        <c:if test="${fn:length(domainDescription) > 0}">
          <p id="description-domainContent">${silfn:escapeHtml(domainDescription)}</p>
        </c:if>
      </div>
      <c:if test="${currentUser.accessAdmin}">
        <view:arrayPane var="listOfRemovedGroups" routingAddress="displayRemovedGroups" numberLinesPerPage="25">
          <view:arrayColumn title="" sortable="false"/>
          <view:arrayColumn title="${name}" compareOn="${g -> fn:toLowerCase(g.data.name)}"/>
          <view:arrayColumn title="${path}" compareOn="${g -> fn:toLowerCase(g.path)}"/>
          <view:arrayColumn title="${removeDate}" compareOn="${g -> g.data.stateSaveDate}"/>
          <c:if test="${isAutomaticDeletionEnabled}">
            <view:arrayColumn title="${automaticDeleteDate}" compareOn="${g -> g.data.stateSaveDate}"/>
          </c:if>
          <view:arrayLines var="aGroup" items="${removedGroups}">
            <view:arrayLine>
              <view:arrayCellCheckbox name="selection" value="${aGroup.id}" checked="false"/>
              <view:arrayCellText text="${silfn:escapeHtml(aGroup.data.name)}"/>
              <view:arrayCellText text="${silfn:escapeHtml(aGroup.path)}"/>
              <view:arrayCellText text="${silfn:formatDateAndHour(aGroup.data.stateSaveDate, language)}"/>
              <c:if test="${isAutomaticDeletionEnabled}">
                <fmt:message var="automaticDeleteDelay" key="GML.nbDays"><fmt:param value="${aGroup.automaticDeletionDayDelay}"/></fmt:message>
                <view:arrayCellText text="${silfn:formatTemporal(aGroup.automaticDeletionDayDate, userZoneId, language)
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
            <c:if test="${reloadDomainNavigationFrame}">
            whenSilverpeasReady(function() {
              parent.refreshCurrentLevel();
            });
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
</view:sp-body-part>
</view:sp-page>