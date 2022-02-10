<%--

    Copyright (C) 2000 - 2022 Silverpeas

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as
    published by the Free Software Foundation, either version 3 of the
    License, or (at your option) any later version.

    As a special exception to the terms and conditions of version 3.0 of
    the GPL, you may redistribute this Program in connection with Free/Libre
    Open Source Software ("FLOSS") applications as described in Silverpeas's
    FLOSS exception.  You should have received a copy of the text describing
    the FLOSS exception, and it is also available here:
    "https://www.silverpeas.org/legal/floss_exception.html"

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.

--%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://www.silverpeas.com/tld/silverFunctions" prefix="silfn" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view" %>

<c:set var="language" value="${sessionScope['SilverSessionController'].favoriteLanguage}"/>
<fmt:setLocale value="${language}"/>
<view:setBundle basename="org.silverpeas.subscription.multilang.subscriptionBundle"/>

<c:set var="saveNoteIntoComment" value="${silfn:booleanValue(param.saveNoteIntoComment)}"/>

<c:set var="id" value="${param.hashCode() < 0 ? -param.hashCode() : param.hashCode()}"/>
<fmt:message var="confirmLabel" key="subscription.message.confirm.sending"/>
<fmt:message var="noteLabel" key="subscription.message.note"/>
<fmt:message var="noteCommentSaveLabel" key="subscription.message.note.saveIntoComments"/>

<div class="subscription-confirmation">
  <p>${confirmLabel}</p>
  <textarea rows="4" cols="80" placeholder="${noteLabel}" maxlength="2000"></textarea>
  <c:if test="${saveNoteIntoComment}">
    <p class="save-into-comment-block">
      <input id="${id}" class="saveNoteIntoComment" type="checkbox"><label for="${id}">${noteCommentSaveLabel}</label>
    </p>
  </c:if>
</div>
