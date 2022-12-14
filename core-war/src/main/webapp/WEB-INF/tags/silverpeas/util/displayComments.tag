<%--
  Copyright (C) 2000 - 2022 Silverpeas

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
<%@ tag import="org.silverpeas.core.admin.user.model.User" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://www.silverpeas.com/tld/silverFunctions" prefix="silfn" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view" %>

<c:set var="user" value="${silfn:currentUser()}"/>

<%@ attribute name="fromComponentId" required="false" type="java.lang.String"
              description="The unique identifier of the Silverpeas component instance from which the tag is used. If empty, componentId is used" %>
<%@ attribute name="componentId" required="true" type="java.lang.String"
              description="The unique identifier of the Silverpeas component instance or the tool to which the commented resource belongs" %>
<%@ attribute name="resourceId" required="true" type="java.lang.String"
              description="The unique identifier of the resource in Silverpeas for which the comments block is displayed." %>
<%@ attribute name="resourceType" required="true" type="java.lang.String"
              description="The type of the resource in Silverpeas for which the comments block is displayed.
              Usually this value is got from the Contribution#getContributionType() method implemented by the resource." %>
<%@ attribute name="indexed" required="false" type="java.lang.Boolean"
              description="New comments should be indexed?
              If not set or valued to true, all new comments will be indexed by Silverpeas." %>
<%@ attribute name="callback" required="false" type="java.lang.String"
              description="A javascript function to invoke for each event on the comments.
               The function must accept one parameter: the receieved event as an object with two
                attributes:
               - the type of the event: 'listing', 'addition', 'deletion', or 'udpate',
               - and the object concerned by the event (either the comment or the list of comments
               for the 'listing' event." %>

<view:settings settings="org.silverpeas.general" key="ApplicationURL" var="webContext"/>
<view:settings settings="org.silverpeas.util.comment.Comment" key="AdminAllowedToUpdate"
               defaultValue="true" var="canBeUpdated"/>

<c:if test="${fromComponentId == null}">
  <c:set var="fromComponentId" value="${componentId}"/>
</c:if>

<c:set var="canBeUpdated" value="${silfn:booleanValue(canBeUpdated) and user.isPlayingAdminRole(componentId)}"/>
<c:if test="${silfn:isNotDefined(indexed)}">
  <c:set var="indexed" value="true"/>
</c:if>
<c:if test="${silfn:isNotDefined(callback)}">
  <c:set var="callback">function(){}</c:set>
</c:if>

<div class="comments-block-app">
  <silverpeas-comments class="comments-block"
                       resource-id='${resourceId}'
                       resource-type='${resourceType}'
                       component-id='${componentId}'
                       from-component-id='${fromComponentId}'
                       v-bind:indexed='${indexed}'
                       v-bind:user='user'
                       v-on:change='onChange'>
  </silverpeas-comments>
</div>

<view:script src="/util/javaScript/checkForm.js"/>
<view:script src="/util/javaScript/jquery/autoresize.jquery.min.js"/>
<view:script src="/util/javaScript/vuejs/components/comments/silverpeas-comments.js"/>
<view:includePlugin name="userZoom"/>

<script type="text/javascript">
  whenSilverpeasReady(function() {
    SpVue.createApp({
      data: function() {
        return {
          user: {
            id: '${user.id}',
            firstName: '${user.firstName}',
            lastName: '${user.lastName}',
            fullName: '${user.firstName} ${user.lastName}',
            avatar: '${webContext}${user.smallAvatar}',
            anonymous: ${user.anonymous},
            guestAccess: ${user.accessGuest},
            admin: ${user.isPlayingAdminRole(componentId)},
            canUpdateAll: ${canBeUpdated}
          }
        };
      },
      methods: {
        onChange: ${callback}
      }
    }).mount('.comments-block-app');
  });
</script>