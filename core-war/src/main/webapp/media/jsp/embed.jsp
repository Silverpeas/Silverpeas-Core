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
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://www.silverpeas.com/tld/silverFunctions" prefix="silfn" %>
<%@ taglib prefix="viewTags" tagdir="/WEB-INF/tags/silverpeas/util" %>

<c:set var="mediaUrl" value="${requestScope.mediaUrl}"/>
<c:set var="posterUrl" value="${requestScope.posterUrl}"/>
<c:set var="playerType" value="${requestScope.playerType}"/>
<c:set var="mimeType" value="${requestScope.mimeType}"/>
<c:set var="definition" value="${requestScope.definition}"/>
<c:set var="backgroundColor" value="${requestScope.backgroundColor}"/>
<c:set var="autoPlay" value="${silfn:booleanValue(requestScope.autoPlay)}"/>

<jsp:useBean id="definition" type="org.silverpeas.core.io.media.Definition"/>

<viewTags:fullHtmlEmbedPlayer
    url="${mediaUrl}"
    posterUrl="${posterUrl}"
    type="${playerType}"
    mimeType="${mimeType}"
    definition="${definition}"
    backgroundColor="${backgroundColor}"
    autoPlay="${autoPlay}"/>