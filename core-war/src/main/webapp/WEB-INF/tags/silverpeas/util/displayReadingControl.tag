<%--
  Copyright (C) 2000 - 2026 Silverpeas

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
<%@ taglib uri="jakarta.tags.core" prefix="c" %>

<%@ attribute name="componentInstanceId" required="true"
              type="java.lang.String"
              description="The identifier of the component instance managing the contribution" %>
<%@ attribute name="contributionId" required="true"
              type="java.lang.String"
              description="The identifier of the contribution under reading control" %>
<%@ attribute name="type" required="true"
              type="java.lang.String"
              description="The type of the contribution" %>

<c:import url="/statistic/jsp/readingControl.jsp">
  <c:param name="componentId" value="${componentInstanceId}"/>
  <c:param name="id" value="${contributionId}"/>
  <c:param name="objectType" value="${type}"/>
</c:import>
