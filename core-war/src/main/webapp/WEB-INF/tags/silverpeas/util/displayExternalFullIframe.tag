<%--
  ~ Copyright (C) 2000 - 2019 Silverpeas
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

<%@ tag language="java" pageEncoding="UTF-8" %>
<%@ tag import="org.silverpeas.core.util.security.SecuritySettings" %>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://www.silverpeas.com/tld/silverFunctions" prefix="silfn" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view" %>

<%@ attribute name="url" required="true"
              type="java.lang.String"
              description="The IFrame url" %>
<%@ attribute name="iframeName" required="false"
              type="java.lang.String"
              description="The IFrame name" %>
<c:if test="${empty iframeName}">
  <c:set var="iframeName" value="SpExternalFullIFrameContainer"/>
</c:if>
<c:set var="sandbox" value="<%=SecuritySettings.getIFrameSandboxTagAttribute()%>"/>
<style type="text/css">
  html, body {
    height: 100%;
    margin: 0;
  }

  iframe {
    display: block;
    border: none;
    width: 100%;
    height: 100%;
  }
</style>
<iframe src="${url}" name="${iframeName}"
        marginwidth="0" marginheight="0" frameborder="0" scrolling="auto"
        webkitallowfullscreen="true" mozallowfullscreen="true" allowfullscreen="true" ${sandbox}></iframe>