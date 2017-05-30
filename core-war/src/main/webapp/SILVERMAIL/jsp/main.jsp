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

<c:url var="newUserNotificationUrl" value="/RnotificationUser/jsp/Main.jsp?popupMode=Yes"/>
<c:url var="notifyIconUrl" value="/util/icons/create-action/send-notification.png"/>
<c:url var="userSettingUrl" value='<%=URLUtil.getURL(URLUtil.CMP_PERSONALIZATION, null, null) + "ParametrizeNotification"%>'/>
<fmt:message var="linkIconUrl" key="silvermail.link" bundle="${icons}"/>

<fmt:message var="componentName" key="silverMail"/>
<fmt:message var="inboxLabel" key="bbar1_inbox"/>
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

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
  <title></title>
  <view:looknfeel/>
  <view:includePlugin name="userNotification"/>
  <script type="text/javascript">

    var checkboxMonitor = sp.selection.newCheckboxMonitor('#silvermail-list input[name=selection]');

    function newMessage() {
      SP_openWindow("${newUserNotificationUrl}", 'notifyUserPopup', '700', '430', 'menubar=no,scrollbars=yes,statusbar=no');
    }

    var _handleCheckboxesAndReloadList = function() {
      _reloadList(true);
    };

    var _reloadList = function(checkSelected) {
      var ajaxConfig = sp.ajaxConfig("Main");
      if (typeof checkSelected === 'boolean') {
        checkboxMonitor.applyToAjaxConfig(ajaxConfig);
      }
      return sp.load('#silvermail-list', ajaxConfig, true).then(function() {
        spProgressMessage.hide();
      });
    };

    var _updateFromRequest = function(request) {
      return sp.updateTargetWithHtmlContent('#silvermail-list', request.responseText, true).then(
          function() {
            spProgressMessage.hide();
          });
    };

    function markAllMessagesAsRead() {
      jQuery.popup.confirm("${silfn:escapeJs(markAllReadConfirm)}", function() {
        var ajaxConfig = sp.ajaxConfig("MarkAllMessagesAsRead").byPostMethod();
        spProgressMessage.show();
        silverpeasAjax(ajaxConfig).then(_updateFromRequest);
      });
    }

    function markSelectedMessagesAsRead() {
      jQuery.popup.confirm("${silfn:escapeJs(markSelectedReadConfirm)}", function() {
        var ajaxConfig = sp.ajaxConfig("MarkSelectedMessagesAsRead").byPostMethod();
        checkboxMonitor.applyToAjaxConfig(ajaxConfig);
        spProgressMessage.show();
        silverpeasAjax(ajaxConfig).then(_updateFromRequest);
      });
    }

    function deleteAllMessages() {
      jQuery.popup.confirm("${silfn:escapeJs(deleteAllConfirm)}", function() {
        var ajaxConfig = sp.ajaxConfig("DeleteAllMessages").byPostMethod();
        ajaxConfig.withParam("folder", "INBOX");
        spProgressMessage.show();
        silverpeasAjax(ajaxConfig).then(_updateFromRequest);
      });
    }

    function deleteSelectedMessages() {
      jQuery.popup.confirm("${silfn:escapeJs(deleteSelectedConfirm)}", function() {
        var ajaxConfig = sp.ajaxConfig("DeleteSelectedMessages").byPostMethod();
        ajaxConfig.withParam("folder", "INBOX");
        checkboxMonitor.applyToAjaxConfig(ajaxConfig);
        spProgressMessage.show();
        silverpeasAjax(ajaxConfig).then(_updateFromRequest);
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
</head>
<body>
<view:browseBar clickable="false">
  <view:browseBarElt link="#" label="${componentName}"/>
  <view:browseBarElt link="#" label="${inboxLabel}"/>
</view:browseBar>
<view:operationPane>
  <view:operationOfCreation icon="${notifyIconUrl}" action="javascript:newMessage()" altText="${notyfyLabel}"/>
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
      <view:arrayPane var="userNotificationInbox" routingAddress="Main">
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
            <view:arrayCellText compareOn="${userNotification.data.id}">
              ${silfn:formatDate(userNotification.data.date, _userLanguage)}
            </view:arrayCellText>
            <view:arrayCellText>
              <c:if test="${not empty userNotification.data.url}">
                <a href="${userNotification.data.url}" target="_top"><img src="<c:url value="${linkIconUrl}"/>" alt="" border="0"/></a>
              </c:if>
            </view:arrayCellText>
            <view:arrayCellText compareOn="${fn:toLowerCase(userNotification.data.subject)}">
              <a href="${viewUrl}">${silfn:escapeHtml(userNotification.data.subject)}</a>
            </view:arrayCellText>
            <view:arrayCellText compareOn="${fn:toLowerCase(userNotification.data.senderName)}">
              <a href="${viewUrl}">${silfn:escapeHtml(userNotification.data.senderName)}</a>
            </view:arrayCellText>
            <view:arrayCellText compareOn="${fn:toLowerCase(userNotification.data.source)}">
              ${silfn:escapeHtml(userNotification.data.source)}
            </view:arrayCellText>
          </view:arrayLine>
        </view:arrayLines>
      </view:arrayPane>
      <script type="text/javascript">
        whenSilverpeasReady(function() {
          checkboxMonitor.pageChanged();
          sp.arrayPane.ajaxControls('#silvermail-list', {
            before : checkboxMonitor.applyToAjaxConfig,
            success : _updateFromRequest
          });
        });
      </script>
    </div>
  </view:frame>
</view:window>
<view:progressMessage/>
</body>
</html>