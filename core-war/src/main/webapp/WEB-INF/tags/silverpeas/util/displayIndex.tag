<%@ tag import="org.silverpeas.core.admin.user.model.UserDetail" %>
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
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://www.silverpeas.com/tld/silverFunctions" prefix="silfn" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view" %>
<%@ taglib prefix="plugins" tagdir="/WEB-INF/tags/silverpeas/plugins" %>

<c:set var="_language" value="${requestScope.resources.language}"/>
<fmt:setLocale value="${_language}"/>
<view:setBundle basename="org.silverpeas.multilang.generalMultilang" var="generalBundle"/>

<c:url var="iconPrevious" value="/util/viewGenerator/icons/arrows/previous.gif"/>
<c:url var="iconPreviousOff" value="/util/viewGenerator/icons/arrows/previousOff.gif"/>
<c:url var="iconNext" value="/util/viewGenerator/icons/arrows/next.gif"/>
<c:url var="iconNextOff" value="/util/viewGenerator/icons/arrows/nextOff.gif"/>

<fmt:message key="GML.previous" bundle="${generalBundle}" var="labelPrevious"/>
<fmt:message key="GML.next" bundle="${generalBundle}" var="labelNext"/>

<%-- Creator --%>
<%@ attribute name="nbItems" required="true" type="java.lang.Integer"
              description="Total number of items" %>

<%@ attribute name="index" required="true" type="java.lang.Integer"
              description="Index of current item. Must start at 0." %>

<%@ attribute name="linkSuffix" required="false" type="java.lang.String"
              description="Links start with Previous or Next. Suffix can be set here." %>

<c:set var="first" value="${index == 0}"/>
<c:set var="last" value="${index == nbItems-1}"/>

<c:if test="${nbItems > 1}">

<div id="pagination">
  <c:if test="${first}">
    <img src="${iconPreviousOff}" alt="" />
  </c:if>
  <c:if test="${not first}">
    <a href="Previous${linkSuffix}" title="${labelPrevious}" id="previousButton"><img src="${iconPrevious}" alt="${labelPrevious}" /></a>
  </c:if>

  <span class="txtnav"><span class="currentPage">${index + 1}</span> / ${nbItems}</span>

  <c:if test="${last}">
    <img src="${iconNextOff}" alt="" />
  </c:if>
  <c:if test="${not last}">
    <a href="Next${linkSuffix}" title="${labelNext}" id="nextButton"><img src="${iconNext}" alt="${labelNext}" /></a>
  </c:if>
</div>

<script type="text/javascript">
  sp.navigation.previousNextOn(document, function(isPrevious) {
    var button;
    if (isPrevious) {
      button = $('#previousButton').get(0);
    } else {
      button = $('#nextButton').get(0);
    }
    if (button) {
      button.click();
    }
  });
</script>

</c:if>