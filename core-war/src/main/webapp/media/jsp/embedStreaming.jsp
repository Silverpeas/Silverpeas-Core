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
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view" %>

<c:set var="entity" value="${requestScope.entity}"/>
<c:set var="definition" value="${requestScope.definition}"/>

<jsp:useBean id="entity" type="org.silverpeas.core.webapi.media.streaming.StreamingProviderDataEntity"/>
<jsp:useBean id="definition" type="org.silverpeas.core.io.media.Definition"/>

<view:sp-page>
<view:sp-head-part noLookAndFeel="true">
  <meta charset="utf-8">
  <!-- optimize mobile versions -->
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <style type="text/css">
    body,
    html {
      width: 100%;
      height: 100%;
      margin: 0;
      overflow: hidden;
    }
  </style>
</view:sp-head-part>
<body>
<div id="streamingContainer"></div>
<c:set var="width" value="${(definition != null and definition.widthDefined) ? ''.concat(definition.width).concat('px') : '100%'}"/>
<c:set var="height" value="${(definition != null and definition.heightDefined) ? ''.concat(definition.height).concat('px') : '100%'}"/>
<script type="text/javascript">
  const player = '${silfn:escapeJs(entity.embedHtml)}'
      .replace(/width="[0-9]+"/i, 'width="${width}"')
      .replace(/height="[0-9]+"/i, 'height="${height}"');
  const $playerContainer = document.querySelector('#streamingContainer');
  $playerContainer.style.width = '${width}';
  $playerContainer.style.height = '${height}';
  $playerContainer.innerHTML = player;
</script>
</body>
</view:sp-page>