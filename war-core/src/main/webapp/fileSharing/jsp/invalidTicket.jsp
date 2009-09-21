<%@ page isELIgnored="false"%>
<%@ page language="java" contentType="text/html; charset=ISO-8859-1"%>
<%@ taglib uri="/WEB-INF/c.tld" prefix="c"%>
<%@ taglib uri="/WEB-INF/fmt.tld" prefix="fmt"%>
<%@ taglib uri="/WEB-INF/viewGenerator.tld" prefix="view"%>
<fmt:setBundle basename="com.silverpeas.external.filesharing.multilang.fileSharingBundle"/>
<fmt:setBundle basename="com.silverpeas.external.filesharing.settings.fileSharingIcons" var="icons" />
<html>
<head>
<script type="text/javascript" src="<c:url value="/util/javaScript/animation.js" />"></script>
<style type="text/css">
td { font-family: "Verdana", "Arial", sans-serif; font-size: 10px}
</style>
</head>

<c:set var="attachment" value="${requestScope.attAttachment}" />

<body>
<br/>
<center>
<table>
	<tr><td><fmt:message key="fileSharing.expiredTicket" /></td></tr>
</table>
</center>
</body>
</html>