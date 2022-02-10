<%--

    Copyright (C) 2000 - 2022 Silverpeas

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

<%
response.setHeader("Cache-Control","no-store"); //HTTP 1.1
response.setHeader("Pragma","no-cache");        //HTTP 1.0
response.setDateHeader ("Expires",-1);          //prevents caching at the proxy server
%>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://www.silverpeas.com/tld/silverFunctions" prefix="silfn" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view" %>

<%@ include file="checkSilvermail.jsp" %>
<%@ page import="org.silverpeas.core.util.URLUtil"%>

<c:set var="_userLanguage" value="${requestScope.resources.language}" scope="request"/>
<jsp:useBean id="_userLanguage" type="java.lang.String" scope="request"/>
<fmt:setLocale value="${_userLanguage}"/>
<view:setBundle bundle="${requestScope.resources.multilangBundle}"/>
<view:setBundle bundle="${requestScope.resources.iconsBundle}" var="icons"/>

<c:url var="deleteIconUrl" value="/util/icons/delete.gif"/>
<c:url var="userSettingUrl" value='<%=URLUtil.getURL(URLUtil.CMP_PERSONALIZATION, null, null) + "ParametrizeNotification"%>'/>

<fmt:message var="deleteLabel" key="delete"/>
<fmt:message var="deleteMessageConfirm" key="ConfirmDeleteMessage"/>
<fmt:message var="deleteAllConfirm" key="ConfirmDeleteAllSentNotif"/>
<fmt:message var="componentName" key="silverMail"/>
<fmt:message var="inboxBrowseBarLabel" key="bbar1_inbox"/>
<fmt:message var="deleteAllLabel" key="DeleteAllSentNotif"/>
<fmt:message var="inboxLabel" key="LireNotification"/>
<fmt:message var="outboxLabel" key="SentUserNotifications"/>
<fmt:message var="userSettingLabel" key="ParametrerNotification"/>
<fmt:message var="dateLabel" key="date"/>
<fmt:message var="sourceLabel" key="source"/>
<fmt:message var="subjectLabel" key="subject"/>
<fmt:message var="operationLabel" key="operation"/>

<c:set var="sentUserNotifications" value="${requestScope.SentNotifs}"/>
<jsp:useBean id="sentUserNotifications" type="java.util.List<org.silverpeas.web.notificationserver.channel.silvermail.SentUserNotificationItem>"/>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html>
<head>
  <title></title>
  <view:looknfeel/>
  <script type="text/javascript">

  var arrayPaneAjaxControl;

  function readMessage(id){
    SP_openWindow("ReadSentNotification.jsp?NotifId=" + id,"ReadSentNotification","600","380","scrollable=yes,scrollbars=yes");
  }

  function deleteMessage(id, skipConfirmation) {
    if(skipConfirmation){
      reallyDeleteMessage(id);
    } else {
      jQuery.popup.confirm("${deleteMessageConfirm}", function() {
        reallyDeleteMessage(id);
      });
    }
  }

  function reallyDeleteMessage(id) {
    var ajaxConfig = sp.ajaxConfig("DeleteSentNotification.jsp").byPostMethod();
    ajaxConfig.withParam("NotifId", id);
    spProgressMessage.show();
    silverpeasAjax(ajaxConfig).then(arrayPaneAjaxControl.refreshFromRequestResponse);
  }

  function deleteAllMessages() {
    jQuery.popup.confirm("${deleteAllConfirm}", function() {
      var ajaxConfig = sp.ajaxConfig("DeleteAllSentNotifications.jsp").byPostMethod();
      spProgressMessage.show();
      silverpeasAjax(ajaxConfig).then(arrayPaneAjaxControl.refreshFromRequestResponse);
    });
  }
  </script>
</head>
<body>
<view:browseBar clickable="false">
  <view:browseBarElt link="#" label="${componentName}"/>
  <view:browseBarElt link="#" label="${inboxBrowseBarLabel}"/>
</view:browseBar>
<view:operationPane>
  <view:operation action="javascript:deleteAllMessages()" altText="${deleteAllLabel}"/>
</view:operationPane>
<view:window>
  <view:tabs>
    <view:tab label="${inboxLabel}" action="Main" selected="false"/>
    <view:tab label="${outboxLabel}" action="SentUserNotifications" selected="true"/>
    <view:tab label="${userSettingLabel}" action="${userSettingUrl}" selected="false"/>
  </view:tabs>
  <view:frame>
    <div id="silvermail-sent-list">
      <view:arrayPane var="userNotificationOutbox" routingAddress="SentUserNotifications.jsp" numberLinesPerPage="25">
        <view:arrayColumn width="80" title="${dateLabel}" compareOn="${n -> n.id}"/>
        <view:arrayColumn title="${subjectLabel}" compareOn="${n -> fn:toLowerCase(silfn:escapeHtml(n.data.title))}"/>
        <view:arrayColumn title="${sourceLabel}" compareOn="${n -> fn:toLowerCase(silfn:escapeHtml(n.data.source))}"/>
        <view:arrayColumn width="10" title="${operationLabel}" sortable="false"/>
        <view:arrayLines var="sentUserNotification" items="${sentUserNotifications}">
          <view:arrayLine>
            <c:set var="viewUrl" value="javascript:onclick=readMessage('${sentUserNotification.id}');"/>
            <c:set var="deleteUrl" value="javascript:onclick=deleteMessage('${sentUserNotification.id}');"/>
            <view:arrayCellText>
              <a href="${viewUrl}">${silfn:formatDate(sentUserNotification.data.notifDate, _userLanguage)}</a>
            </view:arrayCellText>
            <view:arrayCellText>
              <a href="${viewUrl}">${silfn:escapeHtml(sentUserNotification.data.title)}</a>
            </view:arrayCellText>
            <view:arrayCellText>
              <a href="${viewUrl}">${silfn:escapeHtml(sentUserNotification.data.source)}</a>
            </view:arrayCellText>
            <view:arrayCellText>
              <view:icons>
                <view:icon iconName="${deleteIconUrl}" action="${deleteUrl}" altText="${deleteLabel}"/>
              </view:icons>
            </view:arrayCellText>
          </view:arrayLine>
        </view:arrayLines>
      </view:arrayPane>
      <script type="text/javascript">
        whenSilverpeasReady(function() {
          arrayPaneAjaxControl = sp.arrayPane.ajaxControls('#silvermail-sent-list');
        });
      </script>
    </div>
  </view:frame>
</view:window>
<view:progressMessage/>
</body>
</html>