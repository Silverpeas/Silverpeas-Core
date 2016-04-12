<%@ tag import="org.silverpeas.core.ui.DisplayI18NHelper" %>
<%--
  Copyright (C) 2000 - 2015 Silverpeas

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
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://www.silverpeas.com/tld/silverFunctions" prefix="silfn" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view" %>

<c:set var="_language" value="${requestScope.resources.language}"/>
<fmt:setLocale value="${_language}"/>
<view:setBundle basename="org.silverpeas.social.multilang.socialNetworkBundle" var="_socialBundle"/>

<%-- TAG attributes --%>
<%@ attribute name="user" required="false" type="org.silverpeas.core.admin.user.model.UserDetail"
              description="the user associated to the display choice" %>
<%@ attribute name="userPreferences" required="false" type="org.silverpeas.core.personalization.UserPreferences"
              description="the user preferences associated to the display choice" %>
<%@ attribute name="readOnly" required="false" type="java.lang.Boolean"
              description="indicates if data must be displayed as read only mode" %>
<c:if test="${not empty user and empty userPreferences}">
  <c:set var="userPreferences" value="${user.userPreferences}"/>
</c:if>

<%-- Variables --%>
<c:set var="_allLanguages" value="<%=DisplayI18NHelper.getLanguages()%>"/>
<c:set var="_defaultUserLanguage" value="<%=DisplayI18NHelper.getDefaultLanguage()%>"/>
<c:set var="_currentPreferredUserLanguage" value="${not empty userPreferences ? userPreferences.language : _defaultUserLanguage}"/>

<c:choose>
  <c:when test="${empty readOnly || not readOnly}">
    <select name="SelectedUserLanguage" size="1">
      <c:forEach items="${_allLanguages}" var="_currentLanguage">
        <option value="${_currentLanguage}"${_currentPreferredUserLanguage eq _currentLanguage ? ' selected="selected"' : ''}>
          <fmt:message key="myProfile.settings.language_${_currentLanguage}" bundle="${_socialBundle}"/></option>
      </c:forEach>
    </select>
  </c:when>
  <c:otherwise>
    <fmt:message key="myProfile.settings.language_${_currentPreferredUserLanguage}" bundle="${_socialBundle}"/>
  </c:otherwise>
</c:choose>