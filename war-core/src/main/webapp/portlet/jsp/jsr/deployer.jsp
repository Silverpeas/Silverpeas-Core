<%@page contentType="text/html"%>

<%@ include file="header.jsp"%>

<%
	Window window = gef.getWindow();
	BrowseBar browseBar = window.getBrowseBar();
	browseBar.setDomainName("Portlets");
	
	browseBar.setPath(message.getString("portlets.admin.deployPortlets"));
	
	out.println(window.printBefore());
%> 
<view:frame>
  
<div id="portal-content">
  <jsp:include page="deploy-portlet.jsp" flush="true" />
  <jsp:include page="undeploy-portlet.jsp" flush="true" />
</div> <!-- closes portal-content -->

</div> <!-- closes portal-page -->

</view:frame>
<%
	out.println(window.printAfter());
%>
</body>
</html>