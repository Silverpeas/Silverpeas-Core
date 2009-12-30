<%@ page import="com.stratelia.webactiv.util.ResourceLocator"
%>
<%
	ResourceLocator general = new ResourceLocator("com.stratelia.silverpeas.lookAndFeel.generalLook", "");
	ResourceLocator generalMultilang = new ResourceLocator("com.stratelia.webactiv.multilang.generalMultilang", "");
	
	String sURI = request.getRequestURI();
	String sServletPath = request.getServletPath();
	String sPathInfo = request.getPathInfo();
	if (sPathInfo != null)
	{
	    sURI = sURI.substring(0, sURI.lastIndexOf(sPathInfo));
	}
	String m_context = "../../.." + sURI.substring(0, sURI.lastIndexOf(sServletPath));
	
	String styleSheet = general.getString("defaultStyleSheet", m_context + "/util/styleSheets/globalSP.css");
%>
<html>
<head>
	<title><%=generalMultilang.getString("GML.popupTitle")%></title>
	<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">
	<link rel="stylesheet" href="<%=styleSheet%>">
</head>

<body marginwidth="5" marginheight="5" leftmargin="5" topmargin="5">
	<table cellpadding="0" cellspacing="2" border="0" width="98%" class="intfdcolor">
		<tr>
			<td class="intfdcolor4" align="center">
				<br>
				<span class="txtnav"><%=generalMultilang.getString("GML.ForbiddenAccess")%></span>
				<br>
				<br>
			</td>
		</tr>
	</table>
</body>
</html>