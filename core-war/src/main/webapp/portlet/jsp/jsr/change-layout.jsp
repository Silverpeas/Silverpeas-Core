<%--

    Copyright (C) 2000 - 2013 Silverpeas

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as
    published by the Free Software Foundation, either version 3 of the
    License, or (at your option) any later version.

    As a special exception to the terms and conditions of version 3.0 of
    the GPL, you may redistribute this Program in connection with Free/Libre
    Open Source Software ("FLOSS") applications as described in Silverpeas's
    FLOSS exception.  You should have received a copy of the text describing
    the FLOSS exception, and it is also available here:
    "http://www.silverpeas.org/docs/core/legal/floss_exception.html"

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.

--%>

<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<%@page import="org.silverpeas.web.portlets.portal.DriverUtil" %>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>

<!--Load the resource bundle for the page -->
<fmt:setLocale value="${sessionScope['SilverSessionController'].favoriteLanguage}" />
<view:setBundle basename="DesktopMessages" />

<form id="portal-content-change-layout" method="POST" action="<%=DriverUtil.getPortletsURL(request)%>" >
  <label FOR="change-layout-select"><fmt:message key="changeLayout"/></label>
  <select name="layout" id="change-layout-select" onchange="this.form.submit();">
      <option value="1"><fmt:message key="thickThin"/></option>
      <option value="2"><fmt:message key="thinThick"/></option>
  </select>
</form>
<script>
var layout = <c:out value='${layout}' />;
setSelectedLayout = function() {
  var node = document.getElementById("change-layout-select");
  var options = node.options;
  for (n in options) {
    var option = options[n];
    if (option && option.value == layout) {
      option.selected = true;
      break;
    }
  }
}
window.onload = setSelectedLayout;
</script>
<NOSCRIPT>
Reloads the page with selected layout
</NOSCRIPT>
