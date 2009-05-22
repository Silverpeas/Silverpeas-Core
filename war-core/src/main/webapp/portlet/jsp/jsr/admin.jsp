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

<%@page contentType="text/html"%>

<%@ include file="header.jsp"%>

<%
	Window window = gef.getWindow();
	BrowseBar browseBar = window.getBrowseBar();
	browseBar.setDomainName(message.getString("portlets.homepage"));
	browseBar.setExtraInformation(message.getString("portlets.createPortlet"));
	
	out.println(window.printBefore());
%> 
<view:frame>

<div id="portal-content">
  <jsp:include page="create-portlet.jsp" flush="true" />
</div> <!-- closes portal-content -->

</div> <!-- closes portal-page -->

</view:frame>
<%
	out.println(window.printAfter());
%>
</body>
</html>