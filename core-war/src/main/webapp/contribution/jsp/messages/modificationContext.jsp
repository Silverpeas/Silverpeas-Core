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
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://www.silverpeas.com/tld/silverFunctions" prefix="silfn" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view" %>

<c:set var="language" value="${sessionScope['SilverSessionController'].favoriteLanguage}"/>
<fmt:setLocale value="${language}"/>
<view:setBundle basename="org.silverpeas.contribution.multilang.contribution"/>

<c:set var="isMinorModificationBahavior" value="${silfn:booleanValue(param.minorBehavior)}"/>
<c:set var="isSeveralContributions" value="${silfn:booleanValue(param.several)}"/>
<fmt:message var="minorQuestionLabel" key="contribution.modification.minor.question"/>
<fmt:message var="minorHelpMessage" key="contribution.modification.minor.help">
  <fmt:param value="${isSeveralContributions ? 2 : 1}"/>
</fmt:message>
<fmt:message var="minorImportantsLabel" key="contribution.modification.minor.importants"/>

<div class="contribution-modification-context">
    <c:if test="${isMinorModificationBahavior}">
      <div class="minor-major-behavior">
        <div class="label">
          <label class="txtlibform">${minorQuestionLabel}</label>
        </div>
        <div>
            ${minorHelpMessage}
        </div>
        <div class="field" style="display: none">
          <label>
            <input name="modificationLevelType" type="checkbox" value="major" checked> ${minorImportantsLabel}
          </label>
        </div>
      </div>
    </c:if>
</div>
