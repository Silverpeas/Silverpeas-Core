<%--
  CDDL HEADER START
  The contents of this file are subject to the terms
  of the Common Development and Distribution License
  (the License). You may not use this file except in
  compliance with the License.

  You can obtain a copy of the License at
  http://www.sun.com/cddl/cddl.html and legal/CDDLv1.0.txt
  See the License for the specific language governing
  permission and limitations under the License.

  When distributing Covered Code, include this CDDL
  Header Notice in each file and include the License file
  at legal/CDDLv1.0.txt.
  If applicable, add the following below the CDDL Header,
  with the fields enclosed by brackets [] replaced by
  your own identifying information:
  "Portions Copyrighted [year] [name of copyright owner]"

  Copyright 2006 Sun Microsystems Inc. All Rights Reserved
  CDDL HEADER END
--%>

<%@page import="com.silverpeas.portlets.portal.DriverUtil" %>

<%@ taglib uri="/WEB-INF/c.tld" prefix="c"%>
<%@ taglib uri="/WEB-INF/fmt.tld" prefix="fmt"%>

<!--Load the resource bundle for the page -->
<fmt:setBundle basename="DesktopMessages" />

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
