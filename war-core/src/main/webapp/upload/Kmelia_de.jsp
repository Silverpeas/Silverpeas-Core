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

<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<div>
  Drag and drop files into this zone in order to create publications.<br/>
  If the WEB browser handles it, folders are also taken into
  account<c:if test="${param.folders eq 'ignored'}">, but only the files they contains are processed</c:if>.
  <br/><br/>
  According to your rights, platform parameters and those of the application, options are proposed
  to setup the creation of publications.
  <c:choose>
    <c:when test="${param.mode eq 'onlyDraft'}"><br/><br/>Each created publication will be in draft state.</c:when>
    <c:when test="${param.mode eq 'noDraft'}"><br/><br/>Each created publication will be directly published.</c:when>
  </c:choose>
</div>