<%@ page import="org.silverpeas.core.admin.user.model.User" %><%--
  ~ Copyright (C) 2000 - 2023 Silverpeas
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
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://www.silverpeas.com/tld/silverFunctions" prefix="silfn" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view" %>
<c:set var="language" value="${sessionScope['SilverSessionController'].favoriteLanguage}"/>
<fmt:setLocale value="${language}"/>
<view:setBundle basename="org.silverpeas.social.multilang.socialNetworkBundle"/>

<fmt:message key="profil.actions.changePhoto" var="changePhotoLabel"/>
<fmt:message key="profil.image" var="imageLabel"/>
<c:url var="defaultAvatar" value="<%=User.DEFAULT_AVATAR_PATH%>"/>

<!-- ########################################################################################### -->
<silverpeas-component-template name="my-profile-management">
  <div class="my-profile-management">
    <%--   MY PHOTO POPIN   --%>
    <silverpeas-popin v-on:api="myPhotoCtx.popin = $event"
                      v-bind:title="'${silfn:escapeJs(changePhotoLabel)}'"
                      v-bind:dialog-class="'my-photo-popin'"
                      type="validation">
      <silverpeas-form-pane v-on:api="myPhotoCtx.formPane = $event"
                            v-bind:mandatoryLegend="false"
                            v-bind:manualActions="true">
        <silverpeas-my-photo-form
            v-on:api="myPhotoCtx.form = $event"
            v-bind:my-photo-url="profile.avatarUrl"></silverpeas-my-photo-form>
      </silverpeas-form-pane>
    </silverpeas-popin>
  </div>
</silverpeas-component-template>

<!-- ########################################################################################### -->
<silverpeas-component-template name="my-photo-form">
  <div class="my-photo-form">
    <div>
      <silverpeas-label for="my-photo-input" class="label">${imageLabel}</silverpeas-label>
      <div class="champ-ui-dialog">
        <silverpeas-image-file-input
            id="my-photo-input"
            v-on:api="myPhotoInput = $event"
            v-bind:full-image-url="myPhotoModel.deleteOriginal || myPhotoUrl === '${defaultAvatar}' ? undefined : myPhotoUrl"
            v-model="myPhotoModel"></silverpeas-image-file-input>
      </div>
    </div>
    <div class="avatar-policy"><fmt:message key="profil.descriptionImage" /></div>
  </div>
</silverpeas-component-template>