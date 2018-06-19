<%--
  ~ Copyright (C) 2000 - 2018 Silverpeas
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
  ~ along with this program.  If not, see <http://www.gnu.org/licenses/>.
  --%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://www.silverpeas.com/tld/silverFunctions" prefix="silfn" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view" %>
<c:set var="language" value="${sessionScope['SilverSessionController'].favoriteLanguage}"/>
<fmt:setLocale value="${language}"/>
<view:setBundle basename="org.silverpeas.multilang.generalMultilang"/>

<c:url var="mandatoryIcons" value="/util/icons/mandatoryField.gif"/>

<fmt:message var="validateLabel" key="GML.validate"/>
<fmt:message var="cancelLabel" key="GML.cancel"/>
<c:set var="mandatoryMessage">@name@ <fmt:message key='GML.MustBeFilled'/></c:set>
<c:set var="mustBePositiveIntegerMessage">@name@<fmt:message key='GML.MustContainsPositiveNumber'/></c:set>
<c:set var="nbMaxMessage">@name@<fmt:message key='GML.data.error.message.string.limit'><fmt:param value="@length@"/></fmt:message></c:set>
<c:set var="correctDateMessage">@name@<fmt:message key='GML.MustContainsCorrectDate'/></c:set>
<c:set var="correctHourMessage">@name@<fmt:message key='GML.MustContainsCorrectHour'/></c:set>
<c:set var="correctPeriodMessage">@end@ <fmt:message key='GML.MustContainsPostDateTo'/> @start@</c:set>

<div class="silverpeas-form-pane">
  <div v-init>
    {{addMessages({
    mandatory : '${silfn:escapeJs(mandatoryMessage)}',
    mustBePositiveInteger : '${silfn:escapeJs(mustBePositiveIntegerMessage)}',
    nbMax : '${silfn:escapeJs(nbMaxMessage)}',
    correctDate : '${silfn:escapeJs(correctDateMessage)}',
    correctTime : '${silfn:escapeJs(correctHourMessage)}',
    correctPeriod : '${silfn:escapeJs(correctPeriodMessage)}'
    })}}
  </div>
  <div v-if="isHeader" class="header"><slot name="header"></slot></div>
  <div v-if="isBody" class="body"><slot></slot></div>
  <div v-if="isFooter" class="footer"><slot name="footer"></slot></div>

  <div v-if="isLegend" class="legend">
    <slot name="legend"></slot>
    <template v-if="mandatoryLegend">
      <img alt="mandatory" src="${mandatoryIcons}" width="5" height="5"/>&nbsp;
      <fmt:message key='GML.requiredField'/>
    </template>
  </div>

  <div v-if="!isManualActions">
    <silverpeas-button-pane>
      <silverpeas-button v-on:click.native="api.validate()"> ${validateLabel} </silverpeas-button>
      <silverpeas-button v-on:click.native="api.cancel()">${cancelLabel}</silverpeas-button>
    </silverpeas-button-pane>
  </div>
</div>