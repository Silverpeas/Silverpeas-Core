<%--
  ~ Copyright (C) 2000 - 2019 Silverpeas
  ~
  ~ This program is free software: you can redistribute it and/or modify
  ~ it under the terms of the GNU Affero General Public License as
  ~ published by the Free Software Foundation, either version 3 of the
  ~ License, or (at your option) any later version.
  ~
  ~ As a special exception to the terms and conditions of version 3.0 of
  ~ the GPL, you may redistribute this Program in connection with Free/Libre
  ~ Open Source Software ("FLOSS") applications as described in Silverpeas's
  ~ FLOSS exception. You should have received a copy of the text describing
  ~ the FLOSS exception, and it is also available here:
  ~ "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
  ~
  ~ This program is distributed in the hope that it will be useful,
  ~ but WITHOUT ANY WARRANTY; without even the implied warranty of
  ~ MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  ~ GNU Affero General Public License for more details.
  ~
  ~ You should have received a copy of the GNU Affero General Public License
  ~ along with this program.  If not, see <http://www.gnu.org/licenses/>.
  --%>

<%@ tag language="java" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://www.silverpeas.com/tld/silverFunctions" prefix="silfn" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view" %>
<%@ taglib prefix="plugins" tagdir="/WEB-INF/tags/silverpeas/plugins" %>

<%@ attribute name="users" required="false" type="java.util.Collection"
              description="The list of users to display" %>

<%@ attribute name="userIds" required="false" type="java.util.Collection"
              description="The list of userIds to display" %>

<%@ attribute name="groups" required="false" type="java.util.Collection"
              description="The list of groups to display" %>

<%@ attribute name="groupIds" required="false" type="java.util.Collection"
              description="The list of groupIds to display" %>

<%@ attribute name="initialQuery" required="false" type="java.lang.String"
              description="The initial query to set into input and to perform by default" %>

<%@ attribute name="selectOnTabulationKeyDown" required="false" type="java.lang.Boolean"
              description="Must select the active option on tabulation?" %>

<%@ attribute name="navigationalBehavior" required="false" type="java.lang.Boolean"
              description="Must observe a navigational behavior?" %>

<%@ attribute name="doNotSelectAutomaticallyOnDropDownOpen" required="false" type="java.lang.Boolean"
              description="Do not select the first option of the drop down on open?" %>

<%@ attribute name="noUserPanel" required="false" type="java.lang.Boolean"
              description="Has the user panel access to be hidden?" %>

<%@ attribute name="noSelectionClear" required="false" type="java.lang.Boolean"
              description="Has the selection clear to be hidden? If navigationalBehavior is true, forced at true." %>

<%@ attribute name="roleFilter" required="false" type="java.util.Collection"
              description="The list of roles to filter on" %>

<%@ attribute name="displayUserZoom" required="false" type="java.lang.Boolean"
              description="Activate the user zoom plugin on each user displayed" %>

<%@ attribute name="displayAvatar" required="false" type="java.lang.Boolean"
              description="Display avatar of each user or just user icon if false" %>

<%@ attribute name="hideDeactivatedState" required="false" type="java.lang.Boolean"
              description="Indicates if deactivated use account must not be taken into account (default as true)" %>

<%@ attribute name="domainIdFilter" required="false" type="java.lang.String"
              description="The domain id to filter on" %>

<%@ attribute name="domainsFilter" required="false" type="java.util.Collection"
              description="The domains to filter on" %>

<%@ attribute name="groupsFilter" required="false" type="java.util.Collection"
              description="The groups to filter on" %>

<%@ attribute name="componentIdFilter" required="false" type="java.lang.String"
              description="The component instance id to filter on" %>

<%@ attribute name="resourceIdFilter" required="false" type="java.lang.String"
              description="The resource id to filter on. It is a concat of the resource type and of the resource identifier. The component id has to be set if set to non null value" %>

<%@ attribute name="id" required="false" type="java.lang.String"
              description="CSS id" %>

<%@ attribute name="multiple" required="false" type="java.lang.Boolean"
              description="Is multiple selection authorized?" %>

<%@ attribute name="selectionType" required="false" type="java.lang.String"
              description="USER or GROUP or USER_GROUP (USER by default or if attribute cannot be parsed)" %>

<%@ attribute name="queryInputName" required="false" type="java.lang.String"
              description="Sets the name of the query HTML input, otherwise no name is set to the HTML input" %>

<%@ attribute name="userInputName" required="false" type="java.lang.String"
              description="Sets the name of the user input, otherwise default one is created" %>

<%@ attribute name="groupInputName" required="false" type="java.lang.String"
              description="Sets the name of the group input, otherwise default one is created" %>

<%@ attribute name="readOnly" required="false" type="java.lang.Boolean"
              description="Indicates if the readOnly mode is required" %>

<%@ attribute name="mandatory" required="false" type="java.lang.Boolean"
              description="Indicates if mandatory display is required" %>

<%@ attribute name="userManualNotificationUserReceiverLimit" required="false" type="java.lang.Boolean"
              description="Indicates if the limit about number of users the current users can select for notification send is required" %>

<%@ attribute name="userPanelButtonLabel" required="false" type="java.lang.String"
              description="The title of user panel button can be overrided" %>

<%@ attribute name="removeButtonLabel" required="false" type="java.lang.String"
              description="The title of remove button can be overrided" %>

<%@ attribute name="jsApiVar" required="false" type="java.lang.String"
              description="Name of the variable which represents the api instance of the plugin" %>

<%@ attribute name="onReadyJsCallback" required="false" type="java.lang.String"
              description="JS callback performed after the initialization of the selection" %>

<%@ attribute name="onChangeJsCallback" required="false" type="java.lang.String"
              description="JS callback performed after the selection has changed" %>

<%@ attribute name="simpleDetailsWhenRecipientTotalExceed" required="false" type="java.lang.Integer"
              description="Hide the recipent box when the number of recipent is over the indicated value (less or equal to ZERO means no hiding)" %>

<c:if test="${hideDeactivatedState == null}">
  <c:set var="hideDeactivatedState" value="${true}"/>
</c:if>

<c:if test="${selectOnTabulationKeyDown == null}">
  <c:set var="selectOnTabulationKeyDown" value="${false}"/>
</c:if>

<c:if test="${navigationalBehavior == null}">
  <c:set var="navigationalBehavior" value="${false}"/>
</c:if>

<c:if test="${doNotSelectAutomaticallyOnDropDownOpen == null}">
  <c:set var="doNotSelectAutomaticallyOnDropDownOpen" value="${false}"/>
</c:if>

<c:if test="${noUserPanel == null}">
  <c:set var="noUserPanel" value="${false}"/>
</c:if>

<c:if test="${noSelectionClear == null}">
  <c:set var="noSelectionClear" value="${false}"/>
</c:if>

<c:if test="${displayUserZoom == null}">
  <c:set var="displayUserZoom" value="${true}"/>
</c:if>

<c:if test="${displayAvatar == null}">
  <c:set var="displayAvatar" value="${true}"/>
</c:if>

<c:if test="${multiple == null}">
  <c:set var="multiple" value="${false}"/>
</c:if>

<c:if test="${selectionType == null}">
  <c:set var="selectionType" value="USER"/>
</c:if>

<c:if test="${readOnly == null}">
  <c:set var="readOnly" value="${false}"/>
</c:if>

<c:if test="${mandatory == null}">
  <c:set var="mandatory" value="${false}"/>
</c:if>

<c:if test="${userManualNotificationUserReceiverLimit == null}">
  <c:set var="userManualNotificationUserReceiverLimit" value="${false}"/>
</c:if>

<c:if test="${simpleDetailsWhenRecipientTotalExceed == null}">
  <c:set var="simpleDetailsWhenRecipientTotalExceed" value="${0}"/>
</c:if>

<c:set var="currentUserId" value="${sessionScope['SilverSessionController'].userId}"/>
<view:includePlugin name="listOfUsersAndGroups"/>

<div id="select-user-group-${id}"></div>

<view:progressMessage/>

<script type="text/javascript">
  whenSilverpeasReady(function() {
    var userIds = [<c:forEach items="${users}" var="user" varStatus="status"><c:if test="${not status.first}">, </c:if>${user.id}</c:forEach>];
    if (userIds.length === 0) {
      userIds = [<c:forEach items="${userIds}" var="userId" varStatus="status"><c:if test="${not status.first}">, </c:if>${userId}</c:forEach>];
    }
    var groupIds = [<c:forEach items="${groups}" var="group" varStatus="status"><c:if test="${not status.first}">, </c:if>${group.id}</c:forEach>];
    if (groupIds.length === 0) {
      groupIds = [<c:forEach items="${groupIds}" var="groupId" varStatus="status"><c:if test="${not status.first}">, </c:if>${groupId}</c:forEach>];
    }
    var roleFilter = [<c:forEach items="${roleFilter}" var="role" varStatus="status"><c:if test="${not status.first}">, </c:if>'${role}'</c:forEach>];
    var domainFilter = [<c:forEach items="${domainsFilter}" var="domain" varStatus="status"><c:if test="${not status.first}">, </c:if>'${domain.id}'</c:forEach>];
    if (domainFilter.length === 0) {
      <c:if test="${not empty domainIdFilter}">
        domainFilter = ['${domainIdFilter}'];
      </c:if>
    }
    var groupFilter = [<c:forEach items="${groupsFilter}" var="group" varStatus="status"><c:if test="${not status.first}">, </c:if>'${group.id}'</c:forEach>];
    var instance = new UserGroupSelect({
      rootContainerId : "select-user-group-${id}",
      simpleDetailsWhenRecipientTotalExceed : ${simpleDetailsWhenRecipientTotalExceed},
      hideDeactivatedState : ${hideDeactivatedState},
      domainIdFilter : domainFilter,
      componentIdFilter : '${componentIdFilter}',
      resourceIdFilter : '${resourceIdFilter}',
      roleFilter : roleFilter,
      groupFilter : groupFilter,
      initialQuery : '${silfn:escapeJs(initialQuery)}',
      selectOnTabulationKeyDown : ${selectOnTabulationKeyDown},
      navigationalBehavior : ${navigationalBehavior},
      doNotSelectAutomaticallyOnDropDownOpen : ${doNotSelectAutomaticallyOnDropDownOpen},
      noUserPanel : ${noUserPanel},
      noSelectionClear : ${noSelectionClear},
      queryInputName : '${queryInputName}',
      userInputName : '${userInputName}',
      groupInputName : '${groupInputName}',
      currentUserId : ${currentUserId},
      initialUserIds : userIds,
      initialGroupIds : groupIds,
      displayUserZoom : ${displayUserZoom},
      displayAvatar : ${displayAvatar},
      multiple : ${multiple},
      selectionType : '${selectionType}',
      readOnly : ${readOnly},
      mandatory : ${mandatory},
      userManualNotificationUserReceiverLimit : ${userManualNotificationUserReceiverLimit}
      <c:if test="${not empty userPanelButtonLabel}">
      ,userPanelButtonLabel : "${silfn:escapeJs(userPanelButtonLabel)}"
      </c:if>
      <c:if test="${not empty removeButtonLabel}">
      ,removeButtonLabel : "${silfn:escapeJs(removeButtonLabel)}"
      </c:if>
      <c:if test="${not empty onChangeJsCallback}">
      ,onChange : ${onChangeJsCallback}
      </c:if>
    });
    <c:if test="${not empty onReadyJsCallback}">
    instance.ready(function() {
      ${onReadyJsCallback}(instance);
    });
    </c:if>
    <c:if test="${not empty jsApiVar}">
    ${jsApiVar} = instance;
    </c:if>
  });
</script>