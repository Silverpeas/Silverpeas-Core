<%@ include file="../portletImport.jsp"%>

<%@ taglib uri="/WEB-INF/portlet.tld" prefix="portlet" %>
<%@ taglib uri="/WEB-INF/fmt.tld" prefix="fmt" %>

<portlet:defineObjects/>

<fmt:setBundle basename="com.silverpeas.portlets.multilang.portletsBundle"/>

<%
RenderRequest pReq = (RenderRequest)request.getAttribute("javax.portlet.request");
WindowState windowState = pReq.getWindowState();

String height = "350px";
if (windowState.equals(WindowState.MAXIMIZED))
	height = "750px";

String url = (String) pReq.getAttribute("URL");

if (StringUtil.isDefined(url)) { %>
	<iframe src="<%=m_sContext+url%>" frameborder="0" scrolling="auto" width="98%" height="<%=height%>"></iframe>
<% } else { %>
	<fmt:message key="portlets.portlet.componentInstance.error.accessForbidden"/>
<% } %>