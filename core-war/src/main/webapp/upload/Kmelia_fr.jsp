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
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<div>
  Créez des publications en déposant les fichiers dans cette zone.<br/>
  Si le navigateur WEB le permet, les répertoires sont également pris en
  charge<c:if test="${param.folders eq 'ignored'}">, cependant seuls les fichiers qu'ils contiennent sont pris en compte</c:if>.
  <br/><br/>
  Selon vos droits, les paramètres de la plate-forme et ceux de l'application, des options vous sont
  proposées pour paramétrer la création des publications.
  <c:choose>
    <c:when test="${param.mode eq 'onlyDraft'}"><br/><br/>Chaque publication créée sera dans l'état brouillon.</c:when>
    <c:when test="${param.mode eq 'noDraft'}"><br/><br/>Chaque publication créée sera directement publiée.</c:when>
  </c:choose>
</div>