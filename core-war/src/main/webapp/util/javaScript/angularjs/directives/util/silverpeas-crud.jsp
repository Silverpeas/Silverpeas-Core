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
<%@ page import="java.sql.Date" %>
<%@ page import="java.time.Instant" %>
<%@ page import="org.silverpeas.core.web.http.HttpRequest" %>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib tagdir="/WEB-INF/tags/silverpeas/util" prefix="viewTags" %>

<c:set var="createDate" value='<%=request.getParameter("createDate") != null ? Date.from(Instant.ofEpochMilli(HttpRequest.decorate(request).getParameterAsLong("createDate"))) : null%>'/>
<c:set var="createdBy" value="${param.createdBy}"/>
<c:set var="lastUpdateDate" value='<%=request.getParameter("lastUpdateDate") != null ? Date.from(Instant.ofEpochMilli(HttpRequest.decorate(request).getParameterAsLong("lastUpdateDate"))) : null%>'/>
<c:set var="lastUpdatedBy" value="${param.lastUpdatedBy}"/>
<c:set var="permalink" value="${param.permalink}"/>
<c:set var="permalinkLabel" value="${param.permalinkLabel}"/>
<c:set var="permalinkHelp" value="${param.permalinkHelp}"/>
<c:set var="permalinkIconUrl" value="${param.permalinkIconUrl}"/>

<viewTags:displayLastUserCRUD displayHour="true"
                              createDate="${createDate}"
                              createdById="${createdBy}"
                              updateDate="${lastUpdateDate}"
                              updatedById="${lastUpdatedBy}"
                              permalink="${permalink}"
                              permalinkLabel="${permalinkLabel}"
                              permalinkHelp="${permalinkHelp}"
                              permalinkIconUrl="${permalinkIconUrl}">
  <jsp:attribute name="beforeCommonContentBloc"><div class="beforeCommonContentBloc"></div></jsp:attribute>
  <jsp:attribute name="afterCommonContentBloc"><div class="afterCommonContentBloc"></div></jsp:attribute>
</viewTags:displayLastUserCRUD>
