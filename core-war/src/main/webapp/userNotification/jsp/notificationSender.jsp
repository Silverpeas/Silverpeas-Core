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
<%@page import="org.silverpeas.core.notification.user.client.NotificationParameters" %>
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view" %>
<%@ taglib uri="http://www.silverpeas.com/tld/silverFunctions" prefix="silfn" %>
<%@ taglib tagdir="/WEB-INF/tags/silverpeas/util" prefix="viewTags" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<%
  response.setHeader("Cache-Control", "no-store"); //HTTP 1.1
  response.setHeader("Pragma", "no-cache");        //HTTP 1.0
  response.setDateHeader("Expires", -1);           //prevents caching at the proxy server
%>

<%@ page errorPage="../../admin/jsp/errorpage.jsp" %>

<c:set var="language" value="${sessionScope.SilverSessionController.favoriteLanguage}"/>
<view:setBundle bundle="${requestScope.resources.multilangBundle}"/>

<c:set var="users" value="${requestScope.recipientUsers}"/>
<c:set var="groups" value="${requestScope.recipientGroups}"/>
<c:set var="recipientsEditable" value="${requestScope.recipientEdition}"/>
<c:set var="simpleDetailsWhenRecipientTotalExceed" value="${requestScope.simpleDetailsWhenRecipientTotalExceed}"/>
<c:set var="componentId" value="${requestScope.componentId}"/>
<c:set var="resourceId" value="${requestScope.resourceId}"/>
<c:set var="subject" value="${requestScope.title}"/>

<c:set var="requiredReceiversErrorMessage"><fmt:message key="GML.thefield"/> <strong><fmt:message key="addressees"/></strong> <fmt:message key="GML.isRequired"/></c:set>
<c:set var="requiredSubjectErrorMessage"><fmt:message key="GML.thefield"/> <strong><fmt:message key="GML.notification.subject"/></strong> <fmt:message key="GML.isRequired"/></c:set>
<view:link href="/util/styleSheets/fieldset.css"/>
<view:includePlugin name="wysiwyg"/>
<view:loadScript src="/util/javaScript/checkForm.js"/>
<script type="text/javascript">
  var userSelectApi;

  function onPageReady() {
    var __editor = <view:wysiwyg replace="notification-data-message" language="${language}"
        toolbar="userNotification"
        activateWysiwygBackupManager="false"
        height="300"/>
    __editor.on('instanceReady', function() {
      sp.messager.deferredContentReady.promise.then(function() {
        ${recipientsEditable ? 'userSelectApi.focus();' : 'document.querySelector("#notification-data-subject").focus();'}
      });
      sp.messager.deferredContentReady.resolve();
    });
  }

  function sendNotification(notification) {
    var normalizedTitle = stripInitialWhitespace(
        document.querySelector("#notification-data-subject").value);
    if (isWhitespace(normalizedTitle)) {
      SilverpeasError.add("${silfn:escapeJs(requiredSubjectErrorMessage)}");
    }
    if (!userSelectApi.existsSelection()) {
      SilverpeasError.add("${silfn:escapeJs(requiredReceiversErrorMessage)}");
    }
    if (!SilverpeasError.show()) {
      var elements = ['#notification-data-manual', 'recipientUsers', 'recipientGroups',
        '#notification-data-subject'];
      for (var i = 0; i < elements.length; i++) {
        var $input;
        if (elements[i].indexOf('#') === 0) {
          $input = document.querySelector(elements[i]);
        } else {
          $input = document.querySelector('input[name=' + elements[i]);
        }
        notification[$input.name] = $input.value;
      }
      notification['content'] = CKEDITOR.instances['notification-data-message'].getData();
      return sp.messager.send(notification);
    }
    return false;
  }
</script>
<div id="notification-data-container">
  <div class="skinFieldset">
    <input id="notification-data-manual" type="hidden" name="manual" value="${recipientsEditable}"/>
    <div class="fields">
      <div id="notification-data-container-recipients" class="field entireWidth">
        <label class="txtlibform"><fmt:message key="addressees"/></label>
        <div class="champs">
          <fmt:message key="Opane_addressees" var="chooseReceiverLabel"/>
          <viewTags:selectUsersAndGroups id="messager"
                                         selectionType="USER_GROUP"
                                         componentIdFilter="${componentId}"
                                         resourceIdFilter="${resourceId}"
                                         multiple="true"
                                         mandatory="${recipientsEditable}"
                                         simpleDetailsWhenRecipientTotalExceed="${simpleDetailsWhenRecipientTotalExceed}"
                                         userManualNotificationUserReceiverLimit="${recipientsEditable}"
                                         userPanelButtonLabel="${chooseReceiverLabel}"
                                         users="${users}"
                                         groups="${groups}"
                                         readOnly="${not recipientsEditable}"
                                         onReadyJsCallback="onPageReady"
                                         userInputName="recipientUsers"
                                         groupInputName="recipientGroups"
                                         jsApiVar="userSelectApi"/>
        </div>
      </div>
      <div id="notification-data-container-subject" class="field entireWidth">
        <label class="txtlibform" for="notification-data-subject"><fmt:message key="GML.notification.subject"/></label>
        <div class="champs">
          <input id="notification-data-subject" type="text" name="title" size="50" maxlength="<%=NotificationParameters.MAX_SIZE_TITLE%>" value="${subject}">
          <img src="<c:url value='/util/icons/mandatoryField.gif' />" width="5" height="5" alt=""/>
        </div>
      </div>
      <div id="notification-data-container-message" class="field entireWidth">
        <label class="txtlibform" for="notification-data-message"><fmt:message key="GML.notification.message"/></label>
        <div class="champs">
          <textarea id="notification-data-message" name="content" cols="49" rows="9"></textarea>
        </div>
      </div>
    </div>
  </div>
</div>