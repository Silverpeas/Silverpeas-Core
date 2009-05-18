<%@ page import="com.stratelia.silverpeas.containerManager.ContainerContext"%>
<%@ include file="checkPdc.jsp"%>

<%

// recuperation des parametres
ContainerContext containerContext = (ContainerContext) request.getAttribute("ContainerContext");

%>

<HTML>
<HEAD>
<TITLE><%=resource.getString("GML.popupTitle")%></TITLE>
<%
   out.println(gef.getLookStyleSheet());
%>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/animation.js"></script>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/checkForm.js"></script>

</HEAD>
<BODY marginheight=5 marginwidth=5 leftmargin=5 topmargin=5 bgcolor="#FFFFFF">
	<form name="viewAxis" action="Main" method="post">
	<input type="hidden" name="Ids">
<%
String sAction = containerContext.getReturnURL();
%>

	<a href="<%=sAction%>" class="buttonStyle">&nbsp;Return &nbsp;</a>

<%
out.println(frame.printAfter());
out.println(window.printAfter());
%>
</form>
<form name="refresh" action="Main" method="post"></form>
</BODY>
</HTML>