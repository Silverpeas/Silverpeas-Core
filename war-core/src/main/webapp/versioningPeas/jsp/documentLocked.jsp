<%@ page isELIgnored="false"%>
<%@ taglib uri="/WEB-INF/c.tld" prefix="c"%>
<%@ taglib uri="/WEB-INF/jstl-fmt.tld" prefix="fmt"%>
<%@ include file="checkVersion.jsp"%>
<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<fmt:setLocale value="${userLanguage}" />
<fmt:setBundle basename="com.stratelia.silverpeas.versioningPeas.multilang.versioning" var="versioningMessages"  />
<html>
<head>
<%
out.println(gef.getLookStyleSheet());
%>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title><fmt:message key="versioning.warning.checkin.locked.title" bundle="${versioningMessages}" /></title>
</head>
<body>
<%
  ResourceLocator messages = new ResourceLocator("com.stratelia.silverpeas.versioningPeas.multilang.versioning", m_MainSessionCtrl.getFavoriteLanguage());
  ButtonPane warningButtonPane = gef.getButtonPane();
  warningButtonPane.addButton(gef.getFormButton(messages.getString("close"), "javascript:window.opener.parent.MyMain.location.reload(); window.close();", false));
%>
<p><h1><fmt:message key="versioning.warning.checkin.locked" bundle="${versioningMessages}" /></h1></p>
<p><center><%=warningButtonPane.print()%></center></p>
</body>
</html>
