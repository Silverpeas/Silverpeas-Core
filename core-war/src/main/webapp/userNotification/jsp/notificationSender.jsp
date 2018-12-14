<%--

    Copyright (C) 2000 - 2018 Silverpeas

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
<%@page import="org.silverpeas.core.notification.user.client.NotificationParameters" %>
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view" %>
<%@ taglib tagdir="/WEB-INF/tags/silverpeas/util" prefix="viewTags" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<%
  response.setHeader("Cache-Control", "no-store"); //HTTP 1.1
  response.setHeader("Pragma", "no-cache");        //HTTP 1.0
  response.setDateHeader("Expires", -1);          //prevents caching at the proxy server
%>

<%@ page errorPage="../../admin/jsp/errorpage.jsp" %>

<fmt:setLocale value="${sessionScope[sessionController].language}"/>
<view:setBundle bundle="${requestScope.resources.multilangBundle}"/>

<c:set var="users" value="${requestScope.recipientUsers}"/>
<c:set var="groups" value="${requestScope.recipientGroups}"/>
<c:set var="recipientsEditable" value="${requestScope.recipientEdition}"/>
<c:set var="componentId" value="${requestScope.componentId}"/>
<c:set var="subject" value="${requestScope.title}"/>

<c:set var="requiredReceiversErrorMessage"><fmt:message key="GML.thefield"/> <fmt:message key="addressees"/> <fmt:message key="GML.isRequired"/></c:set>
<c:set var="requiredSubjectErrorMessage"><fmt:message key="GML.thefield"/> <fmt:message key="GML.notification.subject"/> <fmt:message key="GML.isRequired"/></c:set>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
  <view:looknfeel withFieldsetStyle="true" withCheckFormScript="true"/>
  <c:url var="sendUrl" value="/RuserNotification/jsp/SendNotif"/>
  <script type="text/javascript">
    var userSelectApi;
    function onPageReady() {
      ${recipientsEditable ? 'userSelectApi.focus();' : 'document.notificationSenderForm.title.focus();'}
      currentPopupResize();
    }

    function sendNotification(notification) {
      var normalizedTitle = stripInitialWhitespace(document.notificationSenderForm.title.value);
      if (isWhitespace(normalizedTitle)) {
        SilverpeasError.add("${requiredSubjectErrorMessage}");
      }
      if (!userSelectApi.existsSelection()) {
        SilverpeasError.add("${requiredReceiversErrorMessage}");
      }
      if (!SilverpeasError.show()) {
        var elements = document.forms['notificationSenderForm'].elements;
        for (var i = 0; i < elements.length; i++) {
          var element = elements[i];
          notification[element.name] = element.value;
        }
        sp.messager.send(notification);
      }
    }
  </script>
</head>
<body>
<fmt:message key="GML.notification.send" var="msgAction"/>
<view:browseBar extraInformations="${msgAction}"/>
<view:window popup="true">

  <form name="notificationSenderForm" action="" method="post" accept-charset="UTF-8">
    <input type="hidden" name="manual" value="${recipientsEditable}"/>
    <fieldset class="skinFieldset" id="send-notification">
      <legend>Notification</legend>
      <div class="fields">
        <div id="recipientsArea" class="field">
          <label class="txtlibform"><fmt:message key="addressees"/></label>
          <div class="champs">
            <fmt:message key="Opane_addressees" var="chooseReceiverLabel"/>
            <viewTags:selectUsersAndGroups selectionType="USER_GROUP"
                                           componentIdFilter="${componentId}"
                                           multiple="true"
                                           mandatory="${recipientsEditable}"
                                           userManualNotificationUserReceiverLimit="${recipientsEditable}"
                                           userPanelButtonLabel="${chooseReceiverLabel}"
                                           users="${users}"
                                           groups="${groups}"
                                           readOnly="${not recipientsEditable}"
                                           onReadyJsCallback="onPageReady"
                                           onChangeJsCallback="currentPopupResize"
                                           userInputName="recipientUsers"
                                           groupInputName="recipientGroups"
                                           jsApiVar="userSelectApi"/>
          </div>
        </div>
        <div id="subjectArea" class="field">
          <label class="txtlibform"><fmt:message key="GML.notification.subject"/></label>
          <div class="champs">
            <input id="subject" type="text" name="title" size="50" maxlength="<%=NotificationParameters.MAX_SIZE_TITLE%>" value="${subject}"/>
            <img src="<c:url value='/util/icons/mandatoryField.gif' />" width="5" height="5"/>
          </div>
        </div>
        <div id="messageArea" class="field">
          <label class="txtlibform"><fmt:message key="GML.notification.message"/></label>
          <div class="champs">
              <textarea id="message" name="content" cols="49" rows="9"></textarea>
          </div>
        </div>
      </div>
    </fieldset>
  </form>
</view:window>
</body>
</html>
