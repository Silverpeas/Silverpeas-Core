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

<%@ taglib uri="/WEB-INF/c.tld" prefix="c"%>

<c:set var="spaceId" value="${requestScope['SpaceId']}"/>

<%@ include file="header.jsp"%>

<%
	Boolean disableMove = (Boolean) request.getAttribute("DisableMove");
	if (disableMove == null)
		disableMove = Boolean.FALSE;

	Window window = gef.getWindow();

	if (!disableMove.booleanValue())
	{
		BrowseBar browseBar = window.getBrowseBar();
		browseBar.setDomainName(message.getString("portlets.homepage"));
		
		OperationPane operationPane = window.getOperationPane();
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

<script type="text/javascript" src="<%=m_context%>/portlet/jsp/jsr/js/jquery-1.2.6.js"></script>
<script type="text/javascript" src="<%=m_context%>/portlet/jsp/jsr/js/ui.core.js"></script>
<script type="text/javascript" src="<%=m_context%>/portlet/jsp/jsr/js/ui.dialog.js"></script>
<script type="text/javascript" src="<%=m_context%>/portlet/jsp/jsr/js/ui.resizable.js"></script>
<script type="text/javascript" src="<%=m_context%>/portlet/jsp/jsr/js/ui.draggable.js"></script>
<script type="text/javascript" src="<%=m_context%>/portlet/jsp/jsr/js/ui.droppable.js"></script>
<script type="text/javascript" src="<%=m_context%>/portlet/jsp/jsr/js/ui.sortable.js"></script>

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


<%@page import="com.sun.portal.portletcontainer.invoker.WindowInvokerConstants"%></html>