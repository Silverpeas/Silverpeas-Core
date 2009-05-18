<%@ page isELIgnored="false"%>
<%@ taglib uri="/WEB-INF/c.tld" prefix="c"%>
<%@ taglib uri="/WEB-INF/jstl-fmt.tld" prefix="fmt"%>
<%@ include file="checkAttachment.jsp"%>
<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<fmt:setLocale value="${userLanguage}" />
<fmt:setBundle basename="com.stratelia.webactiv.util.attachment.multilang.attachment" var="attachmentMessages"  />
<html>
<head>
<%
out.println(gef.getLookStyleSheet());
%>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title><fmt:message key="attachment.warning.checkin.locked" bundle="${attachmentMessages}" /></title>
</head>
<body>
<%
  ButtonPane warningButtonPane = gef.getButtonPane();
  warningButtonPane.addButton(gef.getFormButton(messages.getString("fermer"), "javascript:onClick=closeMessage()", false));
%>
<p><fmt:message key="attachment.warning.checkin.locked" bundle="${attachmentMessages}" /></p>
<p><center><%=warningButtonPane.print()%></center></p>
</body>
</html>