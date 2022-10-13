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
    along with this program.  If not, see <https://www.gnu.org/licenses/>.

--%>
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>

<%@ page import="org.silverpeas.core.util.URLUtil" %>
<%@ page import="org.silverpeas.core.admin.component.model.SilverpeasComponentInstance" %>
<%@ page import="org.silverpeas.core.util.StringUtil" %>
<%@ include file="import.jsp" %>

<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view" %>

<view:sp-page>
  <view:sp-head-part noLookAndFeel="true">
    <script type='text/javascript'>
      function forwardToComponent() {
        <%
            String component = StringUtil.defaultStringIfNotDefined(request.getParameter("component"));
            String space = StringUtil.defaultStringIfNotDefined(request.getParameter("space"));
            if (StringUtil.isDefined(SilverpeasComponentInstance.getComponentName(component))) {
              out.println("window.location = \"" + m_context + URLUtil.getURL(space, component) + "Main\"");
            }
        %>
      }
    </script>
  </view:sp-head-part>
  <view:sp-body-part onLoad="javascript:forwardToComponent()">
  </view:sp-body-part>
</view:sp-page>
