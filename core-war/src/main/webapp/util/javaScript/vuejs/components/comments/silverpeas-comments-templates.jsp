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

<c:set var="language" value="${requestScope.resources.language}"/>
<fmt:setLocale value="${language}"/>
<view:setBundle basename="org.silverpeas.util.comment.multilang.comment"/>

<fmt:message var="commentNewTitle" key="comment.add"/>
<fmt:message var="commentUpdateTitle" key="comment.comment"/>
<fmt:message var="commentUpdateAction" key="GML.update"/>
<fmt:message var="commentDeletionAction" key="GML.delete"/>
<fmt:message var="commentDeletionConfirmation" key="comment.suppressionConfirmation"/>
<fmt:message var="commentErrorSingleCharAtLeast" key="comment.pleaseFill_single"/>
<fmt:message var="commentErrorFieldTooLong" key="comment.champsTropLong"/>
<fmt:message var="textMandatory" key="GML.requiredField"/>

<c:url var="deletionIcon" value="/util/icons/delete.gif"/>
<c:url var="updateIcon" value="/util/icons/update.gif"/>

<!--
List of comments on a contribution in which is included an edition block if the current user has
enough rights to add a new comment.
The user can update or delete its own comments. The update opens a popup to modify the text of
the comment.
-->
<silverpeas-component-template name="comments">
  <div id="commentaires" class="commentaires">
    <div v-sp-init>
      {{ addMessages({
      commentDeletionConfirmation : '${silfn:escapeJs(commentDeletionConfirmation)}',
      commentErrorSingleCharAtLeast : '${silfn:escapeJs(commentErrorSingleCharAtLeast)}',
      commentErrorFieldTooLong : '${silfn:escapeJs(commentErrorFieldTooLong)}'
    }) }}
    </div>

    <silverpeas-comment-edition
        v-on:comment-adding="createComment"
        v-bind:current-user="user"
        v-model="commentText"
        v-if="!user.anonymous && !user.guestAccess">
    </silverpeas-comment-edition>



    <silverpeas-popin title="${commentUpdateTitle}"
                      type="validation"
                      v-on:api="updatePopin = $event"
                      v-bind:minWidth="650">
      <div id="comments-update-box">
        <div class="mandatoryField">
          <textarea id="comment-update-text"
                    class="text"
                    v-model="updatedCommentText">
          </textarea>
          <span>&nbsp;</span>
          <view:image src="/util/icons/mandatoryField.gif" alt="${textMandatory}"/>
          <div class="legende">
            <view:image src="/util/icons/mandatoryField.gif" alt="${textMandatory}"/>
            <span>&nbsp;:&nbsp;${textMandatory}</span>
          </div>
        </div>
      </div>
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
</silverpeas-component-template>

<!--
Edition block of a new comment.
-->
<silverpeas-component-template name="comment-edition">
  <div id="edition-box">
    <p class="title"><fmt:message key="comment.add"/></p>
    <div class="avatar">
      <img v-bind:src="currentUser.avatar" alt="avatar">
    </div>
    <textarea id="comment-edition-text"
              class="text"
              v-bind:value="value"
              v-on:input="$emit('input', $event.target.value)">
    </textarea>
    <div class="buttons">
      <button class="button" v-on:click="$emit('comment-adding', comment)">
        <fmt:message key="GML.validate"/>
      </button>
    </div>
  </div>
</silverpeas-component-template>

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
        <span class="name" v-bind:class="{ userToZoom : displayUserZoom}"
              v-bind:rel="comment.author.id">{{ comment.author.fullName }}</span>
        <span class="date"> - {{ comment.creationDate }}</span>
      </p>
      <pre class="text">{{ comment.text }}</pre>
    </div>
  </div>
</silverpeas-component-template>