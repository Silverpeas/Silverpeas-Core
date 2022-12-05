<%--
  ~ Copyright (C) 2000 - 2021 Silverpeas
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
<%@ taglib uri="http://www.silverpeas.com/tld/silverFunctions" prefix="silfn" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view" %>

<c:set var="language" value="${sessionScope['SilverSessionController'].favoriteLanguage}"/>
<c:set var="lookHelper" value="${sessionScope.Silverpeas_LookHelper}"/>
<fmt:setLocale value="${language}"/>
<view:setBundle basename="org.silverpeas.util.comment.multilang.comment"/>
<view:setBundle basename="org.silverpeas.lookSilverpeasV5.multilang.lookBundle" var="lookBundle"/>

<view:settings settings="org.silverpeas.util.comment.Comment" key="AnonymousViewMode"
               defaultValue="normal" var="anonymousViewMode"/>
<view:settings settings="org.silverpeas.authentication.settings.authenticationSettings"
               key="newRegistrationEnabled"
               defaultValue="false" var="selfRegistrationEnabled"/>

<fmt:message var="commentNewTitle" key="comment.add"/>
<fmt:message var="commentUpdateTitle" key="comment.comment"/>
<fmt:message var="commentUpdateAction" key="GML.update"/>
<fmt:message var="commentDeletionAction" key="GML.delete"/>
<fmt:message var="commentDeletionConfirmation" key="comment.suppressionConfirmation"/>
<fmt:message var="commentErrorSingleCharAtLeast" key="comment.pleaseFill_single"/>
<fmt:message var="commentErrorFieldTooLong" key="comment.champsTropLong"/>
<fmt:message var="textNoComment" key="comment.noComment"/>
<fmt:message var="textComment" key="comment.comment"/>
<fmt:message var="textComments" key="comment.comments"/>
<fmt:message var="textConnectToViewComments" key="comment.connectToView"/>
<fmt:message var="textConnectToPostComments" key="comment.connectToComment"/>
<fmt:message var="textMandatory" key="GML.requiredField"/>
<c:choose>
  <c:when test="${selfRegistrationEnabled}">
    <fmt:message var="loginAction" key="lookSilverpeasV5.loginOrRegister" bundle="${lookBundle}"/>
  </c:when>
  <c:otherwise>
    <fmt:message var="loginAction" key="lookSilverpeasV5.login" bundle="${lookBundle}"/>
  </c:otherwise>
</c:choose>

<c:url var="deletionIcon" value="/util/icons/delete.gif"/>
<c:url var="updateIcon" value="/util/icons/update.gif"/>
<c:url var="loginUrl" value="/Login"/>

<!-- ###############################################################################################
List of comments on a contribution in which is included an edition block if the current user has
enough rights to add a new comment.
The user can update or delete its own comments and, in the case he's an administrator, the ones of
other users. The update opens a popup to modify the text of the comment.
################################################################################################ -->
<silverpeas-component-template name="comments">
  <div id="commentaires" class="commentaires">

    <div v-sp-init>
      {{ addMessages({
      commentDeletionConfirmation : '${silfn:escapeJs(commentDeletionConfirmation)}',
      commentErrorSingleCharAtLeast : '${silfn:escapeJs(commentErrorSingleCharAtLeast)}',
      commentErrorFieldTooLong : '${silfn:escapeJs(commentErrorFieldTooLong)}',
      loginAction : '${silfn:escapeJs(loginAction)}',
      textNoComment : '${silfn:escapeJs(textNoComment)}',
      textComment : '${silfn:escapeJs(textComment)}',
      textComments : '${silfn:escapeJs(textComments)}',
      anonymousViewMode : '${silfn:escapeJs(anonymousViewMode)}'
    }) }}
    </div>

    <div v-if="!user.anonymous || messages.anonymousViewMode === 'normal'"
         class="commentsList">

      <silverpeas-comment-edition
          v-on:comment-adding="createComment"
          v-bind:current-user="user"
          v-model="commentText"
          v-if="!user.anonymous && !user.guestAccess">
      </silverpeas-comment-edition>

      <div class="inlineMessage connection" v-if="user.anonymous">
        <p>${textConnectToPostComments}</p>
        <div class="buttons">
          <button class="button logOn" v-on:click="goToLoginPage">
            ${loginAction}
          </button>
        </div>
      </div>

      <silverpeas-popin title="${commentUpdateTitle}"
                        type="validation"
                        v-on:api="updatePopin = $event"
                        v-bind:minWidth="650">
        <silverpeas-form-pane id="comments-update-box"
                              v-bind:manual-actions="true"
                              v-bind:mandatory-legend="true">
          <textarea id="comment-update-text"
                    class="text"
                    v-model="updatedCommentText">
          </textarea>
          <silverpeas-mandatory-indicator></silverpeas-mandatory-indicator>
        </silverpeas-form-pane>
      </silverpeas-popin>

      <silverpeas-fade-transition-group id="list-box"
                                        v-if="comments.length"
                                        tag="div">
        <silverpeas-comment v-for="comment in comments"
                            v-bind:key="comment.id"
                            v-bind:current-user="user"
                            v-bind:comment="comment"
                            v-bind:readonly="false"
                            v-on:comment-update="updateComment"
                            v-on:comment-deletion="deleteComment">
        </silverpeas-comment>
      </silverpeas-fade-transition-group>

    </div>

    <div v-if="user.anonymous && messages.anonymousViewMode === 'counter'">

      <div class="inlineMessage connection" v-if="user.anonymous">
        <p>${textConnectToViewComments}</p>
        <div class="buttons">
          <button class="button logOn" v-on:click="goToLoginPage">
            ${loginAction}
          </button>
        </div>
      </div>

      <div class="commentsCount">
        <span>{{ infoNbOfComments }}</span>
      </div>

    </div>

  </div>
</silverpeas-component-template>

<!-- ###############################################################################################
Edition block of a new comment.
################################################################################################ -->
<silverpeas-component-template name="comment-edition">
  <div id="edition-box">
    <p class="title"><fmt:message key="comment.add"/></p>
    <div class="avatar">
      <img v-bind:src="currentUser.avatar" alt="avatar">
    </div>
    <textarea id="comment-edition-text"
              class="text"
              v-bind:value="modelValue"
              v-on:input="$emit('update:modelValue', $event.target.value)">
    </textarea>
    <div class="buttons">
      <button class="button" v-on:click="$emit('comment-adding', comment)">
        <fmt:message key="GML.validate"/>
      </button>
    </div>
  </div>
</silverpeas-component-template>

<!-- ###############################################################################################
A comment with its content, the date it has been authored and the name of the author.
According to the rights of the current user, the comment can be updated and deleted.
################################################################################################ -->
<silverpeas-component-template name="comment">
  <div v-bind:id="'comment' + comment.id" class="oneComment">
    <div>
      <div class="action">
        <img v-if="canBeUpdated"
             v-on:click="$emit('comment-update', comment)"
             src="${updateIcon}" alt="${commentUpdateAction}">
        <span>&nbsp;</span>
        <img v-if="canBeDeleted"
             v-on:click="$emit('comment-deletion', comment)"
             src="${deletionIcon}" alt="${commentDeletionAction}">
      </div>
      <div class="avatar">
        <img v-bind:src="comment.author.avatar" alt="avatar">
      </div>
      <p class="author">
        <span class="name" v-bind:class="{ userToZoom : displayUserZoom }"
              v-bind:rel="comment.author.id">{{ comment.author.fullName }}</span>
        <span class="date"> - {{ comment.creationDate }}</span>
      </p>
      <pre class="text" v-html="commentText"></pre>
    </div>
  </div>
</silverpeas-component-template>