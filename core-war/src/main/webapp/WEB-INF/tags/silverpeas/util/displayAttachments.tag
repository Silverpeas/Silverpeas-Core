<%--
  Copyright (C) 2000 - 2015 Silverpeas

  This program is free software: you can redistribute it and/or modify
  it under the terms of the GNU Affero General Public License as
  published by the Free Software Foundation, either version 3 of the
  License, or (at your option) any later version.

  As a special exception to the terms and conditions of version 3.0 of
  the GPL, you may redistribute this Program in connection with Free/Libre
  Open Source Software ("FLOSS") applications as described in Silverpeas's
  FLOSS exception. You should have recieved a copy of the text describing
  the FLOSS exception, and it is also available here:
  "http://www.silverpeas.org/docs/core/legal/floss_exception.html"

  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU Affero General Public License for more details.

  You should have received a copy of the GNU Affero General Public License
  along with this program.  If not, see <http://www.gnu.org/licenses/>.
  --%>

<%@ tag language="java" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://www.silverpeas.com/tld/silverFunctions" prefix="silfn" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view" %>

<%@ attribute name="componentInstanceId" required="true"
              type="java.lang.String"
              description="The component instance identifier" %>
<%@ attribute name="componentInstanceIdAlias" required="false"
              type="java.lang.String"
              description="The identifier of the component which is rendering the alias" %>
<%@ attribute name="resourceId" required="true"
              type="java.lang.String"
              description="The identifier of the resource which attachments are linked to" %>
<%@ attribute name="greatestUserRole" required="true"
              type="org.silverpeas.core.admin.user.model.SilverpeasRole"
              description="The greatest role the user has" %>

<%@ attribute name="hasToBeIndexed" required="false"
              type="java.lang.Boolean"
              description="Indicates if the attachment must be indexed (false by default)" %>
<%@ attribute name="reloadCallbackUrl" required="false"
              type="java.lang.String"
              description="The callback url to use for reloading" %>
<%@ attribute name="contentLanguage" required="false"
              type="java.lang.String"
              description="The current content language" %>

<%@ attribute name="attachmentPosition" required="false"
              type="java.lang.String"
              description="The position, 'right' or 'bottom', of the block of attachments ('right' by default)" %>
<%@ attribute name="showIcon" required="false"
              type="java.lang.Boolean"
              description="True to display the attachment icons, false otherwise (true by default)" %>
<%@ attribute name="showTitle" required="false"
              type="java.lang.Boolean"
              description="True to display the attachment title, false otherwise (true by default)" %>
<%@ attribute name="showDescription" required="false"
              type="java.lang.Boolean"
              description="True to display the attachment description, false otherwise (true by default)" %>
<%@ attribute name="showFileSize" required="false"
              type="java.lang.Boolean"
              description="True to display the attachment file size, false otherwise (true by default)" %>
<%@ attribute name="showMenuNotif" required="false"
              type="java.lang.Boolean"
              description="True to display the attachment file size, false otherwise (false by default)" %>

<%@ attribute name="subscriptionManagementContext" required="false"
              type="org.silverpeas.core.subscription.util.SubscriptionManagementContext"
              description="The context of the subscription notification to manage." %>

<c:set var="_paramHandledSubscriptionType" value=""/>
<c:set var="_paramHandledSubscriptionResourceId" value=""/>
<c:if test="${not empty subscriptionManagementContext}">
  <c:if test="${subscriptionManagementContext.entityStatusBeforePersistAction.validated
              and subscriptionManagementContext.entityStatusAfterPersistAction.validated
              and subscriptionManagementContext.entityPersistenceAction.update}">
    <c:set var="_paramHandledSubscriptionType" value="${subscriptionManagementContext.linkedSubscriptionResource.type}"/>
    <c:set var="_paramHandledSubscriptionResourceId" value="${subscriptionManagementContext.linkedSubscriptionResource.id}"/>
  </c:if>
</c:if>
<c:set var="isHandledSubscriptionConfirmation"
       value="${not empty _paramHandledSubscriptionType and not empty _paramHandledSubscriptionResourceId}"/>

<c:if test="${isHandledSubscriptionConfirmation}">
  <view:includePlugin name="subscription"/>
</c:if>

<c:set var="_paramHasToBeIndexed" value="${hasToBeIndexed != null ? hasToBeIndexed : ''}"/>
<c:set var="_paramContentLanguage" value="${silfn:isDefined(contentLanguage) ? contentLanguage : ''}"/>
<c:set var="_paramAliasContext" value="${silfn:isDefined(componentInstanceIdAlias) ? componentInstanceIdAlias : ''}"/>
<c:set var="_paramReloadCallbackUrl" value="${silfn:isDefined(reloadCallbackUrl) ? reloadCallbackUrl : ''}"/>
<c:set var="_paramAttachmentPosition" value="${silfn:isDefined(attachmentPosition) ? attachmentPosition : ''}"/>
<c:set var="_paramShowIcon" value="${showIcon != null ? showIcon : ''}"/>
<c:set var="_paramShowTitle" value="${showTitle != null ? showTitle : ''}"/>
<c:set var="_paramShowDescription" value="${showDescription != null ? showDescription : ''}"/>
<c:set var="_paramShowFileSize" value="${showFileSize != null ? showFileSize : ''}"/>
<c:set var="_paramShowMenuNotif" value="${showMenuNotif != null ? showMenuNotif : ''}"/>

<c:import url="/attachment/jsp/displayAttachedFiles.jsp">
  <c:param name="ComponentId" value="${componentInstanceId}"/>
  <c:param name="Id" value="${resourceId}"/>
  <c:param name="Context" value="attachment"/>
  <c:param name="Profile" value="${greatestUserRole.name}"/>
  <c:param name="IndexIt" value="${_paramHasToBeIndexed}"/>
  <c:param name="Language" value="${_paramContentLanguage}"/>
  <c:param name="Alias" value="${_paramAliasContext}"/>
  <c:param name="CallbackUrl" value="${_paramReloadCallbackUrl}"/>
  <c:param name="AttachmentPosition" value="${_paramAttachmentPosition}"/>
  <c:param name="ShowIcon" value="${_paramShowIcon}"/>
  <c:param name="ShowTitle" value="${_paramShowTitle}"/>
  <c:param name="ShowInfo" value="${_paramShowDescription}"/>
  <c:param name="ShowFileSize" value="${_paramShowFileSize}"/>
  <c:param name="ShowMenuNotif" value="${_paramShowMenuNotif}"/>
  <c:param name="HandledSubscriptionType" value="${_paramHandledSubscriptionType}"/>
  <c:param name="HandledSubscriptionResourceId" value="${_paramHandledSubscriptionResourceId}"/>
</c:import>