<%--

    Copyright (C) 2000 - 2021 Silverpeas

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
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>

<%
  response.setHeader("Cache-Control", "no-store"); //HTTP 1.1
  response.setHeader("Pragma", "no-cache");        //HTTP 1.0
  response.setDateHeader("Expires", -1);          //prevents caching at the proxy server
%>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://www.silverpeas.com/tld/silverFunctions" prefix="silfn" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view" %>

<%@ include file="checkSilvermail.jsp" %>
<%@ page import="org.silverpeas.core.util.URLUtil" %>

<c:set var="_userLanguage" value="${requestScope.resources.language}" scope="request"/>
<jsp:useBean id="_userLanguage" type="java.lang.String" scope="request"/>
<fmt:setLocale value="${_userLanguage}"/>
<view:setBundle bundle="${requestScope.resources.multilangBundle}"/>
<view:setBundle bundle="${requestScope.resources.iconsBundle}" var="icons"/>

<c:url var="newUserNotificationUrl" value="/RuserNotification/jsp/Main.jsp?popinMode=Yes"/>
<c:url var="notifyIconUrl" value="/util/icons/create-action/send-notification.png"/>
<c:url var="userSettingUrl" value='<%=URLUtil.getURL(URLUtil.CMP_PERSONALIZATION, null, null) + "ParametrizeNotification"%>'/>
<fmt:message var="linkIconUrl" key="silvermail.link" bundle="${icons}"/>

<fmt:message var="componentName" key="silverMail"/>
<fmt:message var="inboxBrowseBarLabel" key="bbar1_inbox"/>
<fmt:message var="notyfyLabel" key="Notifier"/>
<fmt:message var="markSelectedReadLabel" key="MarkSelectedNotifAsRead"/>
<fmt:message var="markSelectedReadConfirm" key="MarkSelectedNotifAsReadConfirmation"/>
<fmt:message var="deleteSelectedLabel" key="DeleteSelectedNotif"/>
<fmt:message var="deleteSelectedConfirm" key="DeleteSelectedNotifConfirmation"/>
<fmt:message var="markAllReadLabel" key="MarkAllNotifAsRead"/>
<fmt:message var="markAllReadConfirm" key="ConfirmReadAllNotif"/>
<fmt:message var="deleteAllLabel" key="DeleteAllNotif"/>
<fmt:message var="deleteAllConfirm" key="ConfirmDeleteAllNotif"/>
<fmt:message var="inboxLabel" key="LireNotification"/>
<fmt:message var="outboxLabel" key="SentUserNotifications"/>
<fmt:message var="userSettingLabel" key="ParametrerNotification"/>
<fmt:message var="dateLabel" key="date"/>
<fmt:message var="sourceLabel" key="source"/>
<fmt:message var="fromLabel" key="from"/>
<fmt:message var="urlLabel" key="url"/>
<fmt:message var="subjectLabel" key="subject"/>

<view:sp-page>
<view:sp-head-part>
  <view:includePlugin name="userNotification"/>
  <script type="text/javascript">

    var arrayPaneAjaxControl;
    var checkboxMonitor = sp.selection.newCheckboxMonitor('#silvermail-list input[name=selection]');

    var _handleCheckboxesAndReloadList = function() {
      _reloadList(true);
    };

    var _reloadList = function(checkSelected) {
      var ajaxRequest = sp.ajaxRequest("Main");
      if (typeof checkSelected === 'boolean') {
        checkboxMonitor.prepareAjaxRequest(ajaxRequest);
      }
      return sp.load('#silvermail-list', ajaxRequest, true).then(function() {
        spProgressMessage.hide();
      });
    };

    function markAllMessagesAsRead() {
      jQuery.popup.confirm("${silfn:escapeJs(markAllReadConfirm)}", function() {
        var ajaxRequest = sp.ajaxRequest("MarkAllMessagesAsRead").byPostMethod();
        spProgressMessage.show();
        ajaxRequest.send().then(arrayPaneAjaxControl.refreshFromRequestResponse);
      });
    }

    function markSelectedMessagesAsRead() {
      jQuery.popup.confirm("${silfn:escapeJs(markSelectedReadConfirm)}", function() {
        var ajaxRequest = sp.ajaxRequest("MarkSelectedMessagesAsRead").byPostMethod();
        checkboxMonitor.prepareAjaxRequest(ajaxRequest);
        spProgressMessage.show();
        ajaxRequest.send().then(arrayPaneAjaxControl.refreshFromRequestResponse);
      });
    }

    function deleteAllMessages() {
      jQuery.popup.confirm("${silfn:escapeJs(deleteAllConfirm)}", function() {
        var ajaxRequest = sp.ajaxRequest("DeleteAllMessages").byPostMethod();
        ajaxRequest.withParam("folder", "INBOX");
        spProgressMessage.show();
        ajaxRequest.send().then(arrayPaneAjaxControl.refreshFromRequestResponse);
      });
    }

    function deleteSelectedMessages() {
      jQuery.popup.confirm("${silfn:escapeJs(deleteSelectedConfirm)}", function() {
        var ajaxRequest = sp.ajaxRequest("DeleteSelectedMessages").byPostMethod();
        ajaxRequest.withParam("folder", "INBOX");
        checkboxMonitor.prepareAjaxRequest(ajaxRequest);
        spProgressMessage.show();
        ajaxRequest.send().then(arrayPaneAjaxControl.refreshFromRequestResponse);
      });
    }

    window.USERNOTIFICATION_PROMISE.then(function() {
      spUserNotification.addEventListener('userNotificationRead', _handleCheckboxesAndReloadList,
          "SILVERMAIL_UserNotificationRead");
      spUserNotification.addEventListener('userNotificationDeleted', _handleCheckboxesAndReloadList,
          "SILVERMAIL_UserNotificationDeleted");
      spUserNotification.addEventListener('userNotificationReceived', _handleCheckboxesAndReloadList,
          "SILVERMAIL_UserNotificationReceived");
      spUserNotification.addEventListener('userNotificationCleared', _reloadList,
          "SILVERMAIL_UserNotificationCleared");
    });
  </script>
</view:sp-head-part>
<view:sp-body-part>
<view:browseBar clickable="false">
  <view:browseBarElt link="#" label="${componentName}"/>
  <view:browseBarElt link="#" label="${inboxBrowseBarLabel}"/>
</view:browseBar>
<view:operationPane>
  <view:operationOfCreation icon="${notifyIconUrl}" action="javascript:sp.messager.open()" altText="${notyfyLabel}"/>
  <view:operationSeparator/>
  <view:operation action="javascript:markSelectedMessagesAsRead()" altText="${markSelectedReadLabel}"/>
  <view:operation action="javascript:deleteSelectedMessages()" altText="${deleteSelectedLabel}"/>
  <view:operationSeparator/>
  <view:operation action="javascript:markAllMessagesAsRead()" altText="${markAllReadLabel}"/>
  <view:operation action="javascript:deleteAllMessages()" altText="${deleteAllLabel}"/>
</view:operationPane>
<view:window>
  <view:tabs>
    <view:tab label="${inboxLabel}" action="Main" selected="true"/>
    <view:tab label="${outboxLabel}" action="SentUserNotifications" selected="false"/>
    <view:tab label="${userSettingLabel}" action="${userSettingUrl}" selected="false"/>
  </view:tabs>
  <view:frame>
    <view:areaOfOperationOfCreation/>
    <div id="silvermail-list">
      <view:arrayPane var="userNotificationInbox" routingAddress="Main" numberLinesPerPage="<%=silvermailScc.getPagination().getPageSize()%>">
        <view:arrayColumn width="10" title="" sortable="false"/>
        <view:arrayColumn width="80" title="${dateLabel}" sortable="true"/>
        <view:arrayColumn width="30" title="${urlLabel}" sortable="false"/>
        <view:arrayColumn title="${subjectLabel}" sortable="true"/>
        <view:arrayColumn width="200" title="${fromLabel}" sortable="true"/>
        <view:arrayColumn title="${sourceLabel}" sortable="true"/>
        <view:arrayLines var="userNotification" items='<%=silvermailScc.getFolderMessageList("INBOX")%>'>
          <c:set var="unreadClasses" value="${userNotification.data.readen eq 0 ? 'unread-user-notification-inbox' : ''}"/>
          <view:arrayLine classes="ArrayCell ${unreadClasses}">
            <c:set var="viewUrl" value="javascript:onClick=spUserNotification.view(${userNotification.data.id})"/>
            <view:arrayCellCheckbox name="selection"
                                    checked="${userNotification.selected}"
                                    value="${userNotification.id}"/>
            <view:arrayCellText>
              <a href="${viewUrl}">${silfn:formatDate(userNotification.data.date, _userLanguage)}</a>
            </view:arrayCellText>
            <view:arrayCellText>
              <c:if test="${not empty userNotification.data.url}">
                <a href="${userNotification.data.url}" class="sp-permalink" target="_top"><img src="<c:url value="${linkIconUrl}"/>" alt="" border="0"/></a>
              </c:if>
            </view:arrayCellText>
            <view:arrayCellText>
              <a href="${viewUrl}">${silfn:escapeHtml(userNotification.data.subject)}</a>
            </view:arrayCellText>
            <view:arrayCellText>
              <a href="${viewUrl}">${silfn:escapeHtml(userNotification.data.senderName)}</a>
            </view:arrayCellText>
            <view:arrayCellText>
              ${silfn:escapeHtml(userNotification.data.source)}
            </view:arrayCellText>
          </view:arrayLine>
        </view:arrayLines>
      </view:arrayPane>
      <script type="text/javascript">
        whenSilverpeasReady(function() {
          checkboxMonitor.pageChanged();
          arrayPaneAjaxControl = sp.arrayPane.ajaxControls('#silvermail-list', {
            before : checkboxMonitor.prepareAjaxRequest
          });
        });
      </script>
    </div>
  </view:frame>
</view:window>
<view:progressMessage/>
</view:sp-body-part>
</view:sp-page>