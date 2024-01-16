<%--
  ~ Copyright (C) 2000 - 2024 Silverpeas
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
  ~ along with this program.  If not, see <https://www.gnu.org/licenses/>.
  --%>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://www.silverpeas.com/tld/silverFunctions" prefix="silfn" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view" %>

<c:set var="userLanguage" value="${sessionScope['SilverSessionController'].favoriteLanguage}"/>
<fmt:setLocale value="${userLanguage}"/>
<view:setBundle basename="org.silverpeas.notificationserver.channel.silvermail.multilang.silvermail"/>

<fmt:message key="GML.seeMore" var="seeMoreLabel"/>
<fmt:message key="GML.close" var="closeLabel"/>
<fmt:message key="MarkNotifAsRead" var="markNotifAsReadLabel"/>
<fmt:message key="GML.delete" var="deleteLabel"/>
<fmt:message key="GML.cancelDeletion" var="cancelDeletionLabel"/>

<!-- ########################################################################################### -->
<silverpeas-component-template name="user-notifications">
  <div class="silverpeas-user-notifications"
       v-if="displayed"
       v-bind:class="{'unread-user-notification':nbUnread,'all-read-user-notification':!nbUnread}"
       v-on:click="api.toggleView()">
    <slot></slot>
    <silverpeas-attached-popin v-if="displayPopin"
                               v-bind:to-element="$el"
                               v-on:click.native.prevent.stop=""
                               v-bind:anchor="anchor"
                               v-bind:scroll-end-event="75"
                               v-on:scroll-end="loadMoreNotifications()"
                               fade-duration-type="long">
      <template v-slot:header>
        <silverpeas-link class="view-all" v-on:click.native.prevent.stop="viewAll()">${seeMoreLabel}</silverpeas-link>
        <silverpeas-link class="close" v-on:click.native.prevent.stop="api.toggleView()" title="${closeLabel}"></silverpeas-link>
      </template>
      <silverpeas-list v-if="notifications"
                       v-bind:items="notifications"
                       v-bind:item-feminine-gender="true"
                       v-bind:with-fade-transition="true">
        <silverpeas-list-item v-for="notification in notifications" v-bind:key="notification.id">
          <silverpeas-user-notification-list-item
              v-on:notification-content-view="markAsRead(notification)"
              v-on:notification-read="markAsRead(notification)"
              v-on:notification-delete="markAsDeleted(notification)"
              v-on:notification-cancel-deletion="unmarkAsDeleted(notification)"
              v-on:click.native.prevent.stop="!notification.deleted && notification.resourceViewUrl ? viewResourceOf(notification) : undefined"
              v-bind:notification="notification"></silverpeas-user-notification-list-item>
        </silverpeas-list-item>
      </silverpeas-list>
    </silverpeas-attached-popin>
  </div>
</silverpeas-component-template>

<!-- ########################################################################################### -->
<silverpeas-component-template name="user-notification-list-item">
  <div class="silverpeas-user-notification-list-item"
       v-bind:class="mainClasses"
       v-on:mouseover="toggleButtons(true)"
       v-on:mouseleave="toggleButtons(false)">
    <div class="user-notification-subject">{{notification.subject}}</div>
    <div class="date-from">
      <span class="user-notification-date">{{notification.date | displayAsDate}}</span>
      <span class="user-notification-from">{{notification.senderName}}</span>
    </div>
    <div class="user-notification-source">{{notification.source}}</div>
    <a href="javascript:void(0)"
       class="content-button"
       v-bind:class="{'content-button-open':displayContent,'content-button-close':!displayContent}"
       v-if="notification.content && !displayCancelDeletion"
       v-on:click.prevent.stop="toggleContent()">{{displayContent ? '-' : '+'}}</a>
    <silverpeas-fade-transition>
      <a href="javascript:void(0)"
         class="read-button"
         v-if="displayMarkAsRead"
         v-on:click.prevent.stop="$emit('notification-read',notification)">${markNotifAsReadLabel}</a>
    </silverpeas-fade-transition>
    <silverpeas-fade-transition>
      <a href="javascript:void(0)"
         class="delete-button"
         v-if="displayDelete"
         v-on:click.prevent.stop="$emit('notification-delete',notification)">${deleteLabel}</a>
    </silverpeas-fade-transition>
    <silverpeas-fade-transition>
      <a href="javascript:void(0)"
         class="cancel-deletion-button"
         v-if="displayCancelDeletion"
         v-on:click.prevent.stop="$emit('notification-cancel-deletion',notification)">${cancelDeletionLabel}</a>
    </silverpeas-fade-transition>
    <div class="user-notification-content" v-if="displayContent" v-html="notification.content"></div>
  </div>
</silverpeas-component-template>
