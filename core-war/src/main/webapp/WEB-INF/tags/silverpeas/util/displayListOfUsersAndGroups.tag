<%--
  Copyright (C) 2000 - 2013 Silverpeas

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
<%@ taglib prefix="plugins" tagdir="/WEB-INF/tags/silverpeas/plugins" %>

<%-- Creator --%>
<%@ attribute name="users" required="false" type="java.util.List"
              description="The list of users to display" %>

<%@ attribute name="groups" required="false" type="java.util.List"
              description="The list of groups to display" %>

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

<%@ attribute name="id" required="false" type="java.lang.String"
              description="CSS id" %>

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

<c:set var="readOnly" value="${empty updateCallback}"/>

<c:set var="currentUserId" value="${sessionScope['SilverSessionController'].userId}"/>
<view:includePlugin name="listOfUsersAndGroups"/>

<c:set var="hideEmptyList" value="${hideEmptyList && empty groups && empty users}"/>
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
    new ListOfUsersAndGroups({
      userPanelId : '${id}',
      currentUserId : ${currentUserId},
      rootContainerId : "root-profile-list-${id}",
      initialUserIds : [<c:forEach items="${users}" var="user" varStatus="status"><c:if test="${not status.first}">, </c:if>${user.id}</c:forEach>],
      initialGroupIds : [<c:forEach items="${groups}" var="group" varStatus="status"><c:if test="${not status.first}">, </c:if>${group.id}</c:forEach>],
      userPanelCallback : '${updateCallback}',
      jsSaveCallback : ${empty jsSaveCallback ? false : silfn:escapeJs(jsSaveCallback)},
      formSaveSelector : '${empty formSaveSelector ? '' : silfn:escapeJs(formSaveSelector)}',
      displayUserZoom : ${displayUserZoom},
      displayAvatar : ${displayAvatar}
    });
  });
</script>
</c:if>