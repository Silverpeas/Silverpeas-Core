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

<%@page import="com.sun.portal.portletcontainer.driver.admin.AdminConstants"%>
<%@page import="com.sun.portal.portletcontainer.invoker.WindowInvokerConstants" %>
<%@page import="org.silverpeas.core.web.util.viewgenerator.html.operationpanes.OperationPaneType" %>
<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.browsebars.BrowseBar" %>
<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.operationpanes.OperationPane" %>
<%@ page import="org.silverpeas.core.admin.space.SpaceInst" %>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view" %>

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

    if (!disableMove)
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

<% if (!disableMove) { %>
<script type="text/javascript" src="<%=m_context%>/portlet/jsp/jsr/js/demo.js"></script>
<% } %>

<view:includePlugin name="popup"/>
<script type="text/javascript">

  function openAdmin() {
    $("#addPortletDialog").popup('validation', {
      title : "<fmt:message key="portlets.homepage"/> > <fmt:message key="portlets.createPortlet"/>",
      callback : function() {
        document.createForm.submit();
        return true;
      }
    });
  }

  function getSilverpeasContext()
  {
    return "<%=m_context%>";
  }

  function getSpaceId()
  {
    return "${spaceId}";
  }

  function selectPortlet() {
    $('#title').val($('#portletList option:selected').text());
  }

  $(document).ready(function() {
    selectPortlet();
  });

</script>

<!-- Dialog to add Portlet -->
<div id="addPortletDialog" style="display: none">
  <view:setConstant var="DRIVER_SPACEID" constant="com.sun.portal.portletcontainer.invoker.WindowInvokerConstants.DRIVER_SPACEID"/>
  <c:url value="/portletAdmin?${DRIVER_SPACEID}=${spaceId}" var="actionUrl" />
  <form id="create-portlet" name="createForm" method="post" action="${actionUrl}">
    <c:set value="${sessionScope['com.silverpeas.portletcontainer.driver.admin.silverpeasSpaceId']}" var="silverpeasSpaceId"/>
    <view:setConstant var="existingPortlets" constant="org.silverpeas.web.portlets.portal.DesktopConstants.AVAILABLE_PORTLET_WINDOWS"/>
    <c:set var="list" value="${sessionScope[existingPortlets]}"/>
    <table cellpadding="5">
      <tr>
        <td class="txtlibform"><fmt:message key="portlets.selectBasePortlet"/> :</td>
        <td>
          <select id="portletList" name="<%=AdminConstants.PORTLET_LIST%>" onchange="selectPortlet()">
            <c:forEach items="${list}" var="portlet">
              <c:forEach items="${portlet}" var="portletName">
                <option value="${portletName.key}">${portletName.value}</option>
              </c:forEach>
            </c:forEach>
          </select>
          <input type="hidden" name="<%=AdminConstants.CREATE_PORTLET_WINDOW_SUBMIT%>" value="1"/>
          <input type="hidden" name="<%=WindowInvokerConstants.DRIVER_SPACEID%>" value="${silverpeasSpaceId}"/>
        </td>
      </tr>
      <tr>
        <td class="txtlibform"><fmt:message key="portlets.portletTitle"/> :</td>
        <td>
          <input id="title" type="text" size="40" name="<%=AdminConstants.PORTLET_WINDOW_TITLE%>" value="" maxlength="50"/>
        </td>
      </tr>
    </table>
  </form>
</div>

</body>

</html>
