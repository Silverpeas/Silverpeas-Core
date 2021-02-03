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
  ~ along with this program.  If not, see <http://www.gnu.org/licenses/>.
  --%>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://www.silverpeas.com/tld/silverFunctions" prefix="silfn" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view" %>

<c:set var="userLanguage" value="${sessionScope['SilverSessionController'].favoriteLanguage}"/>
<fmt:setLocale value="${userLanguage}"/>
<view:setBundle basename="org.silverpeas.calendar.multilang.calendarBundle"/>

<fmt:message var="firstPermalinkLabel" key="calendar.label.occurrence.first.permalink"/>
<fmt:message var="currentPermalinkLabel" key="calendar.label.occurrence.current.permalink"/>

<script type="text/ng-template" id="###silverpeas.calendar.event.view.crud.single">
  <silverpeas-crud create-date="{{$ctrl.ceo.createDate}}"
                   created-by="{{$ctrl.ceo.createdById}}"
                   last-update-date="{{$ctrl.ceo.lastUpdateDate}}"
                   last-updated-by="{{$ctrl.ceo.lastUpdatedById}}"
                   permalink="{{$ctrl.ceo.eventPermalinkUrl}}"></silverpeas-crud>
</script>

<script type="text/ng-template" id="###silverpeas.calendar.event.view.crud.several">
  <silverpeas-crud create-date="{{$ctrl.ceo.createDate}}"
                   created-by="{{$ctrl.ceo.createdById}}"
                   last-update-date="{{$ctrl.ceo.lastUpdateDate}}"
                   last-updated-by="{{$ctrl.ceo.lastUpdatedById}}"
                   permalink="{{$ctrl.ceo.eventPermalinkUrl}}"
                   permalink-label="${firstPermalinkLabel}"
                   show-after="true">
    <after-slot>
      <silverpeas-permalink simple="false"
                            link="$ctrl.ceo.occurrencePermalinkUrl"
                            label="${currentPermalinkLabel}"></silverpeas-permalink>
    </after-slot>
  </silverpeas-crud>
</script>

<div ng-include="$ctrl.getTemplate()"></div>

