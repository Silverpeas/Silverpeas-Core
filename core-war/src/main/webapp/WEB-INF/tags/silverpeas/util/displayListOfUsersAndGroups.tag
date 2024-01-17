<%--
  Copyright (C) 2000 - 2024 Silverpeas

  This program is free software: you can redistribute it and/or modify
  it under the terms of the GNU Affero General Public License as
  published by the Free Software Foundation, either version 3 of the
  License, or (at your option) any later version.

  As a special exception to the terms and conditions of version 3.0 of
  the GPL, you may redistribute this Program in connection with Free/Libre
  Open Source Software ("FLOSS") applications as described in Silverpeas's
  FLOSS exception. You should have received a copy of the text describing
  the FLOSS exception, and it is also available here:
  "http://www.silverpeas.org/docs/core/legal/floss_exception.html"

  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU Affero General Public License for more details.

  You should have received a copy of the GNU Affero General Public License
  along with this program.  If not, see <https://www.gnu.org/licenses/>.
  --%>

<%@ tag language="java" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://www.silverpeas.com/tld/silverFunctions" prefix="silfn" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view" %>
<%@ taglib prefix="plugins" tagdir="/WEB-INF/tags/silverpeas/plugins" %>

<%-- Creator --%>
<%@ attribute name="users" required="false" type="java.util.Collection"
              description="The list of users to display" %>

<%@ attribute name="userIds" required="false" type="java.util.Collection"
              description="The list of userIds to display" %>

<%@ attribute name="groups" required="false" type="java.util.Collection"
              description="The list of groups to display" %>

<%@ attribute name="groupIds" required="false" type="java.util.Collection"
              description="The list of groupIds to display" %>

<%@ attribute name="label" required="false" type="java.lang.String"
              description="Label to use as fieldset legend" %>

<%@ attribute name="updateCallback" required="false" type="java.lang.String"
              description="Javascript function or URL to update list" %>

<%@ attribute name="jsSaveCallback" required="false" type="java.lang.String"
              description="Javascript function that handles the save of the list from the caller.
              the function is called on user panel validation" %>

<%@ attribute name="formSaveSelector" required="false" type="java.lang.String"
              description="The selector that permits to find the form to submit on user panel validation" %>

<%@ attribute name="displayLabel" required="false" type="java.lang.Boolean"
              description="Display label to use as fieldset legend" %>

<%@ attribute name="displayUserZoom" required="false" type="java.lang.Boolean"
              description="Activate the user zoom plugin on each user displayed" %>

<%@ attribute name="displayAvatar" required="false" type="java.lang.Boolean"
              description="Display avatar of each user or just user icon if false" %>

<%@ attribute name="hideEmptyList" required="false" type="java.lang.Boolean"
              description="Hide empty list" %>

<%@ attribute name="domainIdFilter" required="false" type="java.lang.String"
              description="The domain id to filter on" %>

<%@ attribute name="componentIdFilter" required="false" type="java.lang.String"
              description="The component instance id to filter on" %>

<%@ attribute name="id" required="false" type="java.lang.String"
              description="CSS id" %>

<%@ attribute name="initUserPanelUserIdParamName" required="false" type="java.lang.String"
              description="Sets the user parameter name used to init the user panel, otherwise UserPanelCurrentUserIds is used" %>

<%@ attribute name="initUserPanelGroupIdParamName" required="false" type="java.lang.String"
              description="Sets the group parameter name used to init the user panel, otherwise UserPanelCurrentGroupIds is used" %>

<%@ attribute name="userInputName" required="false" type="java.lang.String"
              description="Sets the name of the user input, otherwise ${id}UserPanelCurrentUserIds is created" %>

<%@ attribute name="groupInputName" required="false" type="java.lang.String"
              description="Sets the name of the group input, otherwise ${id}UserPanelCurrentGroupIds is created" %>

<%@ attribute name="readOnly" required="false" type="java.lang.Boolean"
              description="Indicates if the readOnly mode is required" %>

<%@ attribute name="simpleDetailsWhenRecipientTotalExceed" required="false" type="java.lang.Integer"
              description="Hide the recipent box when the number of recipent is over the indicated value (less or equal to ZERO means no hiding)" %>

<c:if test="${displayUserZoom == null}">
  <c:set var="displayUserZoom" value="${true}"/>
</c:if>

<c:if test="${displayAvatar == null}">
  <c:set var="displayAvatar" value="${true}"/>
</c:if>

<c:if test="${hideEmptyList == null}">
  <c:set var="hideEmptyList" value="${false}"/>
</c:if>

<c:if test="${label != null && displayLabel == null}">
  <c:set var="displayLabel" value="${true}"/>
</c:if>

<c:if test="${initUserPanelUserIdParamName == null}">
  <c:set var="initUserPanelUserIdParamName" value="UserPanelCurrentUserIds"/>
</c:if>

<c:if test="${initUserPanelGroupIdParamName == null}">
  <c:set var="initUserPanelGroupIdParamName" value="UserPanelCurrentGroupIds"/>
</c:if>

<c:if test="${userInputName == null}">
  <c:set var="userInputName" value="${id}UserPanelCurrentUserIds"/>
</c:if>

<c:if test="${groupInputName == null}">
  <c:set var="groupInputName" value="${id}UserPanelCurrentGroupIds"/>
</c:if>

<c:if test="${readOnly == null}">
  <c:set var="readOnly" value="${empty updateCallback}"/>
</c:if>

<c:if test="${simpleDetailsWhenRecipientTotalExceed == null}">
  <c:set var="simpleDetailsWhenRecipientTotalExceed" value="${0}"/>
</c:if>

<c:set var="currentUserId" value="${sessionScope['SilverSessionController'].userId}"/>
<view:includePlugin name="listOfUsersAndGroups"/>

<c:set var="hideEmptyList" value="${hideEmptyList && empty groups && empty users && empty groupIds && empty userIds}"/>
<div>
  <c:choose>
    <c:when test="${hideEmptyList}">
      <!-- Do not display empty list -->
    </c:when>
    <c:otherwise>
      <c:if test="${displayLabel}">
        <fieldset id="${id}" class="skinFieldset">
        <legend class="without-img">${label}</legend>
      </c:if>
      <div id="root-profile-list-${id}"></div>
      <c:if test="${displayLabel}">
        </fieldset>
      </c:if>
    </c:otherwise>
  </c:choose>
</div>

<view:progressMessage/>

<c:if test="${not hideEmptyList}">
<script type="text/javascript">
  whenSilverpeasReady(function() {
    var userIds = [<c:forEach items="${users}" var="user" varStatus="status"><c:if test="${not status.first}">, </c:if>${user.id}</c:forEach>];
    if (userIds.length == 0) {
      userIds = [<c:forEach items="${userIds}" var="userId" varStatus="status"><c:if test="${not status.first}">, </c:if>${userId}</c:forEach>];
    }
    var groupIds = [<c:forEach items="${groups}" var="group" varStatus="status"><c:if test="${not status.first}">, </c:if>${group.id}</c:forEach>];
    if (groupIds.length == 0) {
      groupIds = [<c:forEach items="${groupIds}" var="groupId" varStatus="status"><c:if test="${not status.first}">, </c:if>${groupId}</c:forEach>];
    }
    new ListOfUsersAndGroups({
      simpleDetailsWhenRecipientTotalExceed : ${simpleDetailsWhenRecipientTotalExceed},
      readOnly : ${readOnly},
      domainIdFilter : '${domainIdFilter}',
      componentIdFilter : '${componentIdFilter}',
      userPanelId : '${id}',
      initUserPanelUserIdParamName : '${initUserPanelUserIdParamName}',
      initUserPanelGroupIdParamName : '${initUserPanelGroupIdParamName}',
      userInputName : '${userInputName}',
      groupInputName : '${groupInputName}',
      currentUserId : ${currentUserId},
      rootContainerId : "root-profile-list-${id}",
      initialUserIds : userIds,
      initialGroupIds : groupIds,
      userPanelInitUrl : '${updateCallback}',
      jsSaveCallback : ${empty jsSaveCallback ? false : silfn:escapeJs(jsSaveCallback)},
      formSaveSelector : '${empty formSaveSelector ? '' : silfn:escapeJs(formSaveSelector)}',
      displayUserZoom : ${displayUserZoom},
      displayAvatar : ${displayAvatar}
    });
  });
</script>
</c:if>