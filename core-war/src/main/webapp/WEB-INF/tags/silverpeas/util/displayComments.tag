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
<%@ taglib prefix="plugins" tagdir="/WEB-INF/tags/silverpeas/plugins" %>

<c:set var="language" value="${requestScope.resources.language}"/>
<fmt:setLocale value="${language}"/>

<view:setBundle basename="org.silverpeas.util.comment.multilang.comment"/>

<c:set var="user" value="${silfn:currentUser()}"/>

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

<c:set var="canBeUpdated" value="${canBeUpdated || user.isPlayingAdminRole(componentId)}"/>
<c:if test="${silfn:isNotDefined(indexed)}">
  <c:set var="indexed" value="true"/>
</c:if>
<c:if test="${silfn:isNotDefined(callback)}">
  <c:set var="callback" value="undefined"/>
</c:if>

<style>
  <!--
  -->
</style>

<div id="commentaires" class="commentaires">
</div>

<view:script src="/util/javaScript/checkForm.js"/>
<view:script src="/util/javaScript/jquery/autoresize.jquery.min.js"/>
<view:script src="/util/javaScript/silverpeas-comment.js"/>
<!--
  <div id="edition-box">
    <p class="title">Ajouter un commentaire</p>
    <div class="avatar"><img
        src="/silverpeas/display/avatar/x140/miguel.moquillon@silverpeas.com.jpg"></div>
    <textarea class="text" style="resize: none; overflow-y: hidden; height: 46.2px;"></textarea>
    <div class="buttons">
      <button class="button">Valider</button>
    </div>
  </div>
  <div id="comments-update-box" style="display: none;">
    <div class="mandatoryField"><textarea class="text"
                                          style="resize: none; overflow-y: hidden;"></textarea><span>&nbsp;</span><img
        src="/silverpeas/util/icons/mandatoryField.gif"
        alt="/silverpeas/util/icons/mandatoryField.gif">
      <div class="legende"><img src="/silverpeas/util/icons/mandatoryField.gif"
                                alt="/silverpeas/util/icons/mandatoryField.gif"><span>&nbsp;:&nbsp;Obligatoire</span>
      </div>
    </div>
  </div>
  <div id="list-box">
    <div id="comment3079" class="oneComment">
      <div>
        <div class="action"><img src="/silverpeas/util/icons/update.gif"
                                 alt="Modifier"><span>&nbsp;</span><img
            src="/silverpeas/util/icons/delete.gif" alt="Supprimer"></div>
        <div class="avatar"><img
            src="/silverpeas/display/avatar/x140/david.lesimple@silverpeas.com.jpg"></div>
        <p class="author"><span>David Lesimple</span><span class="date"> - 09/10/2017</span></p>
        <pre class="text">moi aussi..</pre>
      </div>
    </div>
    <div id="comment3074" class="oneComment">
      <div>
        <div class="action"><img src="/silverpeas/util/icons/update.gif"
                                 alt="Modifier"><span>&nbsp;</span><img
            src="/silverpeas/util/icons/delete.gif" alt="Supprimer"></div>
        <div class="avatar"><img
            src="/silverpeas/display/avatar/x140/miguel.moquillon@silverpeas.com.jpg">
        </div>
        <p class="author">Miguel Moquillon<span class="date"> - 06/10/2017</span></p>
        <pre class="text">oui moi aussi:<br>"Publication créée et commentée dans la foulée a été publié."</pre>
      </div>
    </div>
    <div id="comment3073" class="oneComment">
      <div>
        <div class="action"><img src="/silverpeas/util/icons/update.gif"
                                 alt="Modifier"><span>&nbsp;</span><img
            src="/silverpeas/util/icons/delete.gif" alt="Supprimer"></div>
        <div class="avatar"><img
            src="/silverpeas/display/avatar/x140/sebastien.vuillet@silverpeas.com.jpg">
        </div>
        <p class="author"><span>Sébastien Vuillet</span><span class="date"> - 06/10/2017</span></p>
        <pre class="text">Moi oui !<br></pre>
      </div>
    </div>
    <div id="comment3072" class="oneComment">
      <div>
        <div class="action"><img src="/silverpeas/util/icons/update.gif"
                                 alt="Modifier"><span>&nbsp;</span><img
            src="/silverpeas/util/icons/delete.gif" alt="Supprimer"></div>
        <div class="avatar"><img
            src="/silverpeas/display/avatar/x140/yohann.chastagnier@silverpeas.com.jpg">
        </div>
        <p class="author"><span>Yohann Chastagnier</span><span class="date"> - 06/10/2017</span></p>
        <pre
            class="text">Est-ce que quelqu'un a reçu une notification concernant ce commentaire ?</pre>
      </div>
    </div>
  </div>
</div>
-->

<script type="text/javascript">
  (function() {
    let $comments = $('#commentaires');
    $comments.comment({
      uri : '${webContext}/services/comments/${componentId}/${resourceType}/${resourceId}',
      author : {
        avatar : '${webContext}${user.smallAvatar}',
        id : '${user.id}',
        anonymous : ${user.anonymous}
      },
      update : {
        activated : function(comment) {
          return  comment.author.id === '${user.id}' || ${canBeUpdated};
        }, icon : '${webContext}/util/icons/update.gif', altText : '<fmt:message key="GML.update"/>'
      },
      deletion : {
        activated : function(comment) {
          return comment.author.id === '${user.id}' || ${canBeUpdated};
        },
        confirmation : '<fmt:message key="comment.suppressionConfirmation"/>',
        icon : '${webContext}/util/icons/delete.gif',
        altText : '<fmt:message key="GML.delete"/>'
      },
      updateBox : {
        title : '<fmt:message key="comment.comment"/>'
      },
      editionBox : {
        title : '<fmt:message key="comment.add"/>', ok : '<fmt:message key="GML.validate"/>'
      },
      validate : function(text) {
        if (text === null || text === undefined || $.trim(text).length === 0) {
          notyError('<fmt:message key="comment.pleaseFill_single"/>');
        } else if (!isValidTextArea(text)) {
          notyError('<fmt:message key="comment.champsTropLong"/>');
        } else {
          return true;
        }
        return false;
      },
      mandatory : '${webContext}/util/icons/mandatoryField.gif',
      mandatoryText : '<fmt:message key="GML.requiredField"/>',
      callback: ${callback}
    });

    <c:if test="${!user.anonymous && !user.accessGuest}">
    $comments.comment('edition', function() {
      return {
        author: {id: '${user.id}' },
        componentId: '${componentId}',
        resourceId: '${resourceId}',
        resourceType: '${resourceType}',
        indexed: ${indexed}
      }
    });
    </c:if>

    $comments.comment('list');

  })();
</script>