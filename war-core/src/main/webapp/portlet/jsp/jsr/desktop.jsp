<%--

    Copyright (C) 2000 - 2012 Silverpeas

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

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<c:set var="spaceId" value="${requestScope['SpaceId']}"/>

<%@ include file="header.jsp"%>

<%
  String currentSpaceId = request.getParameter("SpaceId");
    if(SpaceInst.PERSONAL_SPACE_ID.equals(currentSpaceId)) {
      currentSpaceId = null;
    }
  request.setAttribute("spaceId", currentSpaceId);
    Boolean disableMove = (Boolean) request.getAttribute("DisableMove");
    if (disableMove == null)
        disableMove = Boolean.FALSE;

    Window window = gef.getWindow();

    BrowseBar browseBar = window.getBrowseBar();
  browseBar.setSpaceId(currentSpaceId);
  browseBar.setComponentId(null);
  browseBar.setDomainName(message.getString("portlets.homepage"));

    if (!disableMove.booleanValue())
    {
        OperationPane operationPane = window.getOperationPane();
        if (currentSpaceId != null) {
          operationPane.setType(OperationPaneType.space);
        } else {
          operationPane.setType(OperationPaneType.personalSpace);
        }
        operationPane.addOperation("", message.getString("portlets.createPortlet"), "javascript:openAdmin()");
    }

    out.println(window.printBefore());
%>

<div id="portal-content">

  <c:if test="${layout==null}">
    <c:set var="layout" value="1" scope="session" />
  </c:if>
  <c:if test="${param.layout!=null}">
    <c:set var="layout" value="${param.layout}" scope="session" />
  </c:if>

  <c:choose>
    <c:when test='${layout == "1"}'>
      <jsp:include page="layout-1.jsp" flush="true"/>
    </c:when>
    <c:when test='${layout == "2"}'>
      <jsp:include page="layout-2.jsp" flush="true"/>
    </c:when>
    <c:otherwise>
      <jsp:include page="layout-3.jsp" flush="true"/>
    </c:otherwise>
  </c:choose>

</div> <!-- closes portal-content -->

</div> <!-- closes portal-page -->

<%
    out.println(window.printAfter());
%>

<% if (!disableMove.booleanValue()) { %>
<script type="text/javascript" src="<%=m_context%>/portlet/jsp/jsr/js/demo.js"></script>
<% } %>

<script type="text/javascript">
  function openAdmin()
  {
    SP_openWindow("<%=m_context%>/portletAdmin?<%=WindowInvokerConstants.DRIVER_SPACEID%>=<c:out value="${spaceId}"/>", "PortletAdmin","770", "550", "toolbar=no, directories=no, menubar=no, locationbar=no ,resizable, scrollbars");
  }

  function getSilverpeasContext()
  {
    return "<%=m_context%>";
  }

  function getSpaceId()
  {
    return "<c:out value="${spaceId}"/>";
  }
</script>

</body>


<%@page import="com.sun.portal.portletcontainer.invoker.WindowInvokerConstants"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.operationPanes.OperationPaneType" %>
</html>