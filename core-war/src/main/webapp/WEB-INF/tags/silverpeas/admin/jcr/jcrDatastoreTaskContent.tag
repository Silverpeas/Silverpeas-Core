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

<%@ tag language="java" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://www.silverpeas.com/tld/silverFunctions" prefix="silfn" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view" %>

<%@ attribute name="task" required="true"
              type="org.silverpeas.core.persistence.jcr.JcrDatastoreTaskMonitor"
              description="The DataStore task monitor instance" %>

<c:set var="taskType" value="${task.type}"/>

<c:set var="lang" value="${sessionScope['SilverSessionController'].favoriteLanguage}"/>
<c:set var="zoneId" value="${sessionScope['SilverSessionController'].favoriteZoneId}"/>
<fmt:setLocale value="${lang}"/>
<view:setBundle basename="org.silverpeas.jcrmonitor.multilang.jcrMonitor"/>

<div class="jcr-datastore-task">
  <ul class="task-content">
    <c:forEach var="taskStatus" items="${task.statuses}">
      <li class="task-status task-status-${taskStatus.type}">
        <div>
          <div class="date">${silfn:formatTemporal(taskStatus.at, zoneId, lang)}</div>
          <div class="report"><fmt:message key="jcrmonitor.${taskType}.status.${taskStatus.type.name()}"/></div>
        </div>
      </li>
    </c:forEach>
    <fmt:message var="processedNodeMsg" key="jcrmonitor.${taskType}.nodes.nbScanned">
      <fmt:param value="${''.concat(task.nbNodeProcessed)}"/>
    </fmt:message>
    <li class="task-nb-processed-node">${processedNodeMsg}</li>
    <c:if test="${task.error.present}">
      <li class="task-error">
        <div class="inlineMessage-nok">${task.error.get().message}</div>
      </li>
    </c:if>
  </ul>
</div>